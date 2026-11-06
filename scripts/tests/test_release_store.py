import copy
import importlib.util
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch


SCRIPTS = Path(__file__).resolve().parents[1]


def load_module(name, filename):
    spec = importlib.util.spec_from_file_location(name, SCRIPTS / filename)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


release = load_module("release_notes_test_module", "release.py")
store = load_module("release_store_test_module", "release_store.py")


class StorePlanTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name) / "project"
        (self.root / "release").mkdir(parents=True)
        self.config = {
            "app_name": "Test App", "source_locale": "en-US",
            "locales": {"en-US": {"android": "en-US", "ios": "en-US"},
                        "es": {"android": ["es-ES", "es-419"], "ios": "es-ES"}},
        }
        self.defaults = copy.deepcopy(store.DEFAULTS)
        self.save_config()
        self.save_defaults()
        self.manager = release.ReleaseManager(self.root)
        self.manager.init("1.2.3", 53, 8, "Improved reminders.")
        self.edit(lambda manifest: manifest["notes"].update({"es": "Recordatorios mejorados."}))
        self.review_notes()
        self.planner = store.StorePlanner(self.manager)

    def save_config(self):
        (self.root / "release/config.json").write_text(json.dumps(self.config), encoding="utf-8")

    def save_defaults(self):
        (self.root / "release/store-defaults.json").write_text(json.dumps(self.defaults), encoding="utf-8")

    def edit(self, change):
        path = self.manager.manifest_path("1.2.3")
        manifest = json.loads(path.read_text())
        change(manifest)
        path.write_text(json.dumps(manifest), encoding="utf-8")

    def review_notes(self):
        for locale in self.manager.locales:
            self.manager.review("1.2.3", locale)

    def metadata(self, platform, locale, field, text):
        path = self.root / "release/metadata" / platform / locale / (field + ".txt")
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")
        return path

    def select(self, selection):
        self.edit(lambda manifest: manifest["store"].update(metadata_updates=selection))

    def ready_defaults(self):
        self.defaults["checks"] = {key: True for key in store.CHECKS}
        self.defaults["ios"]["uses_non_exempt_encryption"] = False
        self.defaults["android"]["managed_publishing_confirmed"] = True
        self.save_defaults()

    def assert_missing(self, plan, suffix):
        self.assertTrue(any(item["field"].endswith(suffix) for item in plan["missing"]), plan["missing"])

    def test_empty_selection_never_copies_persistent_listing(self):
        self.metadata("android", "en-US", "title", "Existing title")
        self.metadata("ios", "en-US", "description", "Existing description")
        self.planner.review("1.2.3")
        plan, output = self.planner.prepare("1.2.3")
        self.assertEqual(plan["metadata_updates"], {"android": {}, "ios": {}})
        self.assertFalse((output / "store-metadata/android/en-US/title.txt").exists())
        self.assertFalse((output / "store-metadata/ios/en-US/description.txt").exists())
        self.assertEqual((output / "store-metadata/android/es-419/changelogs/53.txt").read_text(), "Recordatorios mejorados.")
        self.assertEqual(plan["missing"], [])
        self.assertTrue(plan["submission_missing"])

    def test_only_selected_field_is_exported(self):
        self.metadata("android", "en-US", "title", "New title\n")
        self.metadata("android", "en-US", "full_description", "Keep existing description.")
        self.metadata("ios", "en-US", "name", "Keep existing name")
        self.select({"android": {"en-US": ["title"]}})
        self.planner.review("1.2.3")
        plan, output = self.planner.prepare("1.2.3")
        self.assertEqual(plan["metadata_updates"]["android"], {"en-US": {"title": "New title"}})
        self.assertFalse((output / "store-metadata/android/en-US/full_description.txt").exists())
        self.assertFalse((output / "store-metadata/ios/en-US/name.txt").exists())

    def test_selected_bytes_invalidate_review_unselected_do_not(self):
        chosen = self.metadata("android", "en-US", "title", "New title")
        unselected = self.metadata("android", "en-US", "full_description", "Other copy")
        self.select({"android": {"en-US": ["title"]}})
        self.planner.review("1.2.3")
        unselected.write_text("Changed unselected copy")
        self.assertTrue(self.planner.plan("1.2.3")[0]["reviewed"])
        chosen.write_text("New title\n")
        self.assertFalse(self.planner.plan("1.2.3")[0]["reviewed"])

    def test_config_defaults_notes_and_builds_invalidate_store_review(self):
        self.planner.review("1.2.3")
        self.config["locales"]["es"]["ios"] = ["es-ES", "es-MX"]
        self.save_config()
        self.manager = release.ReleaseManager(self.root)
        self.planner = store.StorePlanner(self.manager)
        self.assertFalse(self.planner.plan("1.2.3")[0]["reviewed"])
        self.planner.review("1.2.3")
        self.defaults["checks"]["privacy_current"] = True
        self.save_defaults()
        self.assertFalse(self.planner.plan("1.2.3")[0]["reviewed"])
        self.planner.review("1.2.3")
        self.edit(lambda manifest: manifest.update(android_version_code=54))
        self.assertFalse(self.planner.plan("1.2.3")[0]["reviewed"])
        self.planner.review("1.2.3")
        self.edit(lambda manifest: manifest["notes"].update({"en-US": "Better reminders."}))
        plan = self.planner.plan("1.2.3")[0]
        self.assertFalse(plan["reviewed"])
        self.assert_missing(plan, ".reviews.en-US")
        self.assert_missing(plan, ".reviews.es")

    def test_platform_and_stage_do_not_invalidate_review(self):
        self.ready_defaults()
        self.planner.review("1.2.3")
        expected = self.planner.plan("1.2.3")[0]["input_sha256"]
        for platform in ("android", "ios", "both"):
            for stage in ("prepare", "submit", "publish"):
                plan = self.planner.plan("1.2.3", platform, stage)[0]
                self.assertEqual(plan["input_sha256"], expected)
                self.assertTrue(plan["reviewed"])
        self.assert_missing(self.planner.plan("1.2.3", "android", "publish")[0], "production_publish")

    def test_notes_and_all_selected_validation_problems_are_reported(self):
        self.edit(lambda manifest: manifest["notes"].update({"en-US": "", "es": "TODO"}))
        self.metadata("android", "en-US", "title", "x" * 33)
        self.metadata("ios", "en-US", "name", " ")
        self.metadata("ios", "en-US", "support_url", "https://user:secret@example.net/")
        self.select({"android": {"en-US": ["title", "screenshots"], "../escape": ["title"]},
                     "ios": {"en-US": ["name", "support_url", "description"]}})
        plan, output = self.planner.prepare("1.2.3")
        for field in (".notes.en-US", ".notes.es", "/title.txt", ".screenshots", ".android.../escape", "/name.txt", "/support_url.txt", "/description.txt"):
            self.assert_missing(plan, field)
        self.assertTrue((output / "review.md").exists())
        with self.assertRaises(store.StoreError):
            self.planner.review("1.2.3")

    def test_ios_keyword_and_review_note_limits_are_utf8_bytes(self):
        self.metadata("ios", "en-US", "keywords", "é" * 51)
        self.select({"ios": {"en-US": ["keywords"]}})
        self.defaults["ios"]["review_notes"] = "é" * 2001
        self.save_defaults()
        plan = self.planner.plan("1.2.3")[0]
        self.assert_missing(plan, "/keywords.txt")
        self.assert_missing(plan, ".ios.review_notes")
        self.assertTrue(any("UTF-8 bytes" in item["action"] for item in plan["missing"]))

    def test_store_title_name_boundaries(self):
        self.select({"android": {"en-US": ["title"]}, "ios": {"en-US": ["name"]}})
        self.metadata("android", "en-US", "title", "x" * 30)
        self.metadata("ios", "en-US", "name", "xx")
        self.planner.review("1.2.3")
        self.assertEqual(self.planner.plan("1.2.3")[0]["missing"], [])
        self.metadata("android", "en-US", "title", "x" * 31)
        self.metadata("ios", "en-US", "name", "x")
        plan = self.planner.plan("1.2.3")[0]
        self.assert_missing(plan, "/title.txt")
        self.assert_missing(plan, "/name.txt")

    def test_configured_locale_must_exist_in_provider_allowlist(self):
        self.config["locales"]["es"]["ios"] = "es-419"
        self.config["locales"]["es"]["android"] = "es-MX"
        self.save_config()
        planner = store.StorePlanner(release.ReleaseManager(self.root))
        plan = planner.plan("1.2.3")[0]
        self.assert_missing(plan, ".locales.es.ios")
        self.assert_missing(plan, ".locales.es.android")
        self.assertIn("es-US", store.STORE_LOCALES["android"])
        self.assertIn("es-MX", store.STORE_LOCALES["ios"])

    def test_urls_require_https_and_no_userinfo(self):
        for text in ("http://real-site.test/help", "javascript:alert(1)", "https://a:b@real-site.test", "https://example.com", "https://host:invalid"):
            with self.subTest(text=text):
                self.metadata("ios", "en-US", "support_url", text)
                self.select({"ios": {"en-US": ["support_url"]}})
                self.assert_missing(self.planner.plan("1.2.3")[0], "/support_url.txt")

    def test_submit_requires_boolean_confirmations_for_chosen_platform(self):
        for value in (None, False, "true"):
            with self.subTest(value=value):
                self.ready_defaults()
                self.defaults["checks"]["privacy_current"] = value
                self.save_defaults()
                self.assert_missing(self.planner.plan("1.2.3", "android", "submit")[0], ".privacy_current")
        self.ready_defaults()
        self.defaults["checks"]["export_compliance_current"] = None
        self.defaults["ios"]["uses_non_exempt_encryption"] = None
        self.save_defaults()
        self.planner.review("1.2.3")
        self.assertEqual(self.planner.plan("1.2.3", "android", "submit")[0]["missing"], [])
        self.assert_missing(self.planner.plan("1.2.3", "ios", "submit")[0], ".export_compliance_current")

    def test_per_release_checks_override_defaults(self):
        self.ready_defaults()
        self.edit(lambda manifest: manifest["store"].update(checks={"privacy_current": False}))
        self.assert_missing(self.planner.plan("1.2.3", "ios", "submit")[0], ".privacy_current")

    def test_reuse_review_details_delegates_remote_check(self):
        self.ready_defaults()
        self.defaults["ios"]["uses_non_exempt_encryption"] = None
        self.save_defaults()
        self.planner.review("1.2.3")
        plan = self.planner.plan("1.2.3", "ios", "submit")[0]
        self.assertEqual(plan["missing"], [])
        self.assertTrue(any(item["field"].endswith("app_review_details") for item in plan["remote_checks"]))
        self.assertTrue(any(item["field"].endswith("export_compliance") for item in plan["remote_checks"]))
        self.assertIsNone(plan["defaults"]["ios"]["review_contact"]["email"])

    def test_replace_review_details_requires_contact_and_demo_answer(self):
        self.ready_defaults()
        self.defaults["ios"]["review_details_mode"] = "replace"
        self.save_defaults()
        plan = self.planner.plan("1.2.3", "ios", "submit")[0]
        self.assert_missing(plan, ".review_contact.email")
        self.assert_missing(plan, ".demo_account_required")
        self.defaults["ios"]["review_contact"] = {"first_name": "Test", "last_name": "Reviewer", "email": "test@real-site.test", "phone": "+1 555 555 0100"}
        self.defaults["ios"]["demo_account_required"] = True
        self.save_defaults()
        self.planner.review("1.2.3")
        plan = self.planner.plan("1.2.3", "ios", "submit")[0]
        self.assertEqual(plan["missing"], [])
        self.assertTrue(any(item["field"].endswith("demo_account") for item in plan["remote_checks"]))
        self.defaults["ios"]["review_contact"]["phone"] = "hello"
        self.save_defaults()
        self.assert_missing(self.planner.plan("1.2.3", "ios", "submit")[0], ".review_contact.phone")

    def test_environment_values_are_never_read_or_exported(self):
        with patch.dict(os.environ, {"APP_REVIEW_DEMO_PASSWORD": "never-export-this-secret", "APP_REVIEW_DEMO_USER": "private-user"}):
            self.planner.review("1.2.3")
            plan, output = self.planner.prepare("1.2.3")
            text = (output / "store-plan.json").read_text() + (output / "review.md").read_text()
            self.assertNotIn("never-export-this-secret", text)
            self.assertNotIn("private-user", text)
            self.assertIn("APP_REVIEW_DEMO_PASSWORD", text)
            self.defaults["ios"]["demo_password"] = "another-never-export-secret"
            self.save_defaults()
            plan, output = self.planner.prepare("1.2.3")
            self.assert_missing(plan, ".demo_password")
            self.assertNotIn("another-never-export-secret", (output / "store-plan.json").read_text() + (output / "review.md").read_text())

    def test_repeated_prepare_deterministic_preserves_builds_and_removes_old_delta(self):
        self.metadata("android", "en-US", "title", "Selected title")
        self.select({"android": {"en-US": ["title"]}})
        self.planner.review("1.2.3")
        _, output = self.planner.prepare("1.2.3")
        before = (output / "store-plan.json").read_bytes()
        (output / "app.aab").write_bytes(b"artifact")
        (output / "ios-build.json").write_text("receipt")
        self.planner.prepare("1.2.3")
        self.assertEqual(before, (output / "store-plan.json").read_bytes())
        self.select({})
        self.planner.prepare("1.2.3")
        self.assertFalse((output / "store-metadata/android/en-US/title.txt").exists())
        self.assertEqual((output / "app.aab").read_bytes(), b"artifact")
        self.assertEqual((output / "ios-build.json").read_text(), "receipt")

    def handoff(self):
        path = Path(self.temporary.name) / "translations.json"
        store.export_translations(self.manager, "1.2.3", path)
        return path

    def test_translation_import_keeps_source_and_revokes_imported_review(self):
        self.planner.review("1.2.3")
        path = self.handoff()
        store.import_translations(self.manager, "1.2.3", path)
        manifest = self.manager.load("1.2.3")
        self.assertEqual(manifest["notes"]["en-US"], "Improved reminders.")
        self.assertIn("en-US", manifest["reviews"])
        self.assertNotIn("es", manifest["reviews"])
        self.assertNotIn("store_review", manifest)

    def test_translation_stale_unknown_or_modified_source_rejected_without_changes(self):
        path = self.handoff()
        handoff = json.loads(path.read_text())
        before = self.manager.manifest_path("1.2.3").read_bytes()
        for mutate in (
            lambda data: data.update(source_sha256="stale"),
            lambda data: data["notes"].update({"en-US": "Changed source"}),
            lambda data: data["notes"].update({"fr": "Bonjour"}),
            lambda data: data["notes"].update({"es": ""}),
        ):
            altered = copy.deepcopy(handoff)
            mutate(altered)
            path.write_text(json.dumps(altered))
            with self.assertRaises(store.StoreError):
                store.import_translations(self.manager, "1.2.3", path)
            self.assertEqual(self.manager.manifest_path("1.2.3").read_bytes(), before)

    def test_metadata_import_rejects_whole_conflicting_batch(self):
        existing = self.metadata("android", "en-US", "title", "Current title")
        snapshot = Path(self.temporary.name) / "snapshot.json"
        data = {"schema_version": 1, "metadata": {"android": {"en-US": {"short_description": "New field", "title": "Different title"}}}}
        snapshot.write_text(json.dumps(data))
        with self.assertRaisesRegex(store.StoreError, "before writing"):
            store.import_metadata(self.manager, snapshot)
        self.assertFalse(existing.with_name("short_description.txt").exists())
        self.assertEqual(existing.read_text(), "Current title")
        data["metadata"]["android"]["en-US"]["title"] = "Current title"
        snapshot.write_text(json.dumps(data))
        self.assertEqual(store.import_metadata(self.manager, snapshot), 1)
        self.assertEqual(store.import_metadata(self.manager, snapshot), 0)
        self.assertEqual(self.manager.load("1.2.3")["store"]["metadata_updates"], {})

    @unittest.skipUnless(hasattr(os, "mkfifo"), "FIFO inputs are a POSIX edge case")
    def test_metadata_fifo_is_rejected_without_reading(self):
        path = self.metadata("android", "en-US", "title", "Temporary regular file")
        path.unlink()
        os.mkfifo(path)
        self.select({"android": {"en-US": ["title"]}})
        self.assert_missing(self.planner.plan("1.2.3")[0], "/title.txt")
        snapshot = Path(self.temporary.name) / "snapshot.json"
        snapshot.write_text(json.dumps({"schema_version": 1, "metadata": {"android": {"en-US": {"title": "Imported title"}}}}))
        with self.assertRaises(store.StoreError):
            store.import_metadata(self.manager, snapshot)

    def test_cli_preview_succeeds_with_blockers_strict_prepare_fails(self):
        scripts = self.root / "scripts"
        scripts.mkdir()
        for filename in ("release.py", "release_store.py"):
            shutil.copyfile(SCRIPTS / filename, scripts / filename)
        command = [sys.executable, str(scripts / "release.py"), "prepare", "1.2.3"]
        preview = subprocess.run(command, cwd=self.temporary.name, capture_output=True, text=True)
        self.assertEqual(preview.returncode, 0, preview.stderr)
        self.assertIn("MISSING", preview.stdout)
        strict = subprocess.run(command + ["--reviewed"], cwd=self.temporary.name, capture_output=True, text=True)
        self.assertEqual(strict.returncode, 1, strict.stderr)


if __name__ == "__main__":
    unittest.main()
