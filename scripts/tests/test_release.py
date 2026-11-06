import importlib.util
import json
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest


SCRIPT = Path(__file__).resolve().parents[1] / "release.py"
SPEC = importlib.util.spec_from_file_location("release_tool", SCRIPT)
release = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(release)


class ReleaseTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name) / "project"
        (self.root / "release").mkdir(parents=True)
        self.config = {
            "app_name": "Test App",
            "source_locale": "en-US",
            "locales": {
                "en-US": {"android": "en-US", "ios": "en-US"},
                "es": {"android": "es-ES", "ios": "es-ES"},
            },
        }
        self.save_config()
        self.manager = release.ReleaseManager(self.root)

    def save_config(self):
        (self.root / "release/config.json").write_text(json.dumps(self.config), encoding="utf-8")

    def prepare(self):
        self.manager.init("1.2.3", 53, 8, "Improved reminders.")
        self.edit(lambda manifest: manifest["notes"].update({"es": "Recordatorios mejorados."}))
        for locale in self.config["locales"]:
            self.manager.review("1.2.3", locale)

    def edit(self, update):
        path = self.manager.manifest_path("1.2.3")
        manifest = json.loads(path.read_text())
        update(manifest)
        path.write_text(json.dumps(manifest), encoding="utf-8")

    def test_init_has_no_invented_translation_or_approval(self):
        path = self.manager.init("1.2.3", 53, 8, "Improved reminders.")
        manifest = json.loads(path.read_text())
        self.assertEqual(manifest["notes"]["es"], "")
        self.assertEqual(manifest["reviews"], {})
        with self.assertRaisesRegex(release.ReleaseError, "blank"):
            self.manager.validate("1.2.3", reviewed=True)
        with self.assertRaisesRegex(release.ReleaseError, "already exists"):
            self.manager.init("1.2.3", 54, 9)

    def test_missing_review_is_rejected_but_draft_can_validate(self):
        self.prepare()
        self.edit(lambda manifest: manifest["reviews"].pop("es"))
        self.manager.validate("1.2.3")
        with self.assertRaisesRegex(release.ReleaseError, "missing or stale for es"):
            self.manager.validate("1.2.3", reviewed=True)

    def test_source_edit_invalidates_every_review(self):
        self.prepare()
        self.edit(lambda manifest: manifest["notes"].update({"en-US": "Better reminders."}))
        with self.assertRaisesRegex(release.ReleaseError, "missing or stale for en-US"):
            self.manager.validate("1.2.3", reviewed=True)
        self.manager.review("1.2.3", "en-US")
        with self.assertRaisesRegex(release.ReleaseError, "missing or stale for es"):
            self.manager.validate("1.2.3", reviewed=True)

    def test_translation_edit_invalidates_only_that_review(self):
        self.prepare()
        self.edit(lambda manifest: manifest["notes"].update({"es": "Mejoras en los recordatorios."}))
        with self.assertRaisesRegex(release.ReleaseError, "missing or stale for es"):
            self.manager.validate("1.2.3", reviewed=True)
        self.manager.review("1.2.3", "es")
        self.manager.validate("1.2.3", reviewed=True)

    def test_note_length_and_blank_review(self):
        self.manager.init("1.2.3", 53, 8, "é" * 500)
        self.manager.review("1.2.3", "en-US")
        with self.assertRaisesRegex(release.ReleaseError, "blank"):
            self.manager.review("1.2.3", "es")
        self.edit(lambda manifest: manifest["notes"].update({"es": "é" * 501}))
        with self.assertRaisesRegex(release.ReleaseError, "501 characters"):
            self.manager.review("1.2.3", "es")
        self.edit(lambda manifest: manifest["notes"].update({"es": " \n\t"}))
        with self.assertRaisesRegex(release.ReleaseError, "blank"):
            self.manager.review("1.2.3", "es")

    def test_version_traversal_and_mismatched_manifest(self):
        for version in ("../1.2.3", "/tmp/1.2.3", "1.2.3/../../x", "1.2", "1.2.3\n", "01.2.3"):
            with self.subTest(version=version), self.assertRaises(release.ReleaseError):
                self.manager.init(version, 53, 8)
        self.prepare()
        self.edit(lambda manifest: manifest.update(version="9.9.9"))
        with self.assertRaisesRegex(release.ReleaseError, "must match"):
            self.manager.validate("1.2.3", reviewed=True)

    def test_store_number_limits_and_types(self):
        for android, ios in ((0, 1), (2_100_000_001, 1), (1, 0), (1, 10_000), (True, 1), (1, 1.5)):
            with self.subTest(android=android, ios=ios), self.assertRaises(release.ReleaseError):
                self.manager.init("1.2.3", android, ios)
        self.manager.init("1.2.3", 2_100_000_000, 9999)
        self.edit(lambda manifest: manifest.update(android_version_code="53"))
        with self.assertRaisesRegex(release.ReleaseError, "Android version code"):
            self.manager.load("1.2.3")

    def test_unknown_locale_mapping_or_manifest_locale_is_rejected(self):
        self.config["locales"]["es"]["android"] = "../../escape"
        self.save_config()
        with self.assertRaisesRegex(release.ReleaseError, "Invalid android"):
            release.ReleaseManager(self.root)
        self.config["locales"]["es"]["android"] = "en-US"
        self.save_config()
        with self.assertRaisesRegex(release.ReleaseError, "Duplicate android"):
            release.ReleaseManager(self.root)
        self.prepare()
        self.edit(lambda manifest: manifest["notes"].update({"fr": "Bonjour"}))
        with self.assertRaisesRegex(release.ReleaseError, "exactly the locales"):
            self.manager.load("1.2.3")

    def test_export_copies_metadata_and_uses_platform_locales(self):
        self.prepare()
        for platform in ("android", "ios"):
            metadata = self.root / "release/metadata" / platform / "en-US"
            metadata.mkdir(parents=True)
            (metadata / "description.txt").write_text("Reusable description.", encoding="utf-8")
        output = self.manager.export("1.2.3", reviewed=True)
        self.assertEqual(
            (output / "metadata/android/es-ES/changelogs/53.txt").read_text(),
            "Recordatorios mejorados.",
        )
        self.assertEqual(
            (output / "metadata/ios/en-US/release_notes.txt").read_text(),
            "Improved reminders.",
        )
        testflight = json.loads((output / "testflight.json").read_text())
        self.assertEqual(
            testflight["localized_build_info"]["es-ES"]["whats_new"],
            "Recordatorios mejorados.",
        )
        self.assertEqual(json.loads((output / "manifest.json").read_text())["ios_build_number"], 8)
        self.assertEqual((output / "metadata/ios/en-US/description.txt").read_text(), "Reusable description.")
        (output / "metadata/obsolete.txt").write_text("stale")
        (output / "app.aab").write_bytes(b"built android artifact")
        (output / "app.ipa").write_bytes(b"built ios artifact")
        (output / "android-build.json").write_text('{"build":53}')
        original_manifest = (output / "manifest.json").read_bytes()
        self.manager.export("1.2.3", reviewed=True)
        self.assertFalse((output / "metadata/obsolete.txt").exists())
        self.assertEqual((output / "app.aab").read_bytes(), b"built android artifact")
        self.assertEqual((output / "app.ipa").read_bytes(), b"built ios artifact")
        self.assertEqual((output / "android-build.json").read_text(), '{"build":53}')
        self.assertEqual((output / "manifest.json").read_bytes(), original_manifest)

    def test_regional_reuse_exports_every_mapping(self):
        self.config["locales"]["es"] = {
            "android": ["es-ES", "es-419", "es-US"], "ios": ["es-ES", "es-MX"]
        }
        self.save_config()
        self.manager = release.ReleaseManager(self.root)
        self.prepare()
        output = self.manager.export("1.2.3", reviewed=True)
        for locale in ("es-ES", "es-419", "es-US"):
            self.assertEqual(
                (output / "metadata/android" / locale / "changelogs/53.txt").read_text(),
                "Recordatorios mejorados.",
            )
        testflight = json.loads((output / "testflight.json").read_text())
        for locale in ("es-ES", "es-MX"):
            self.assertEqual(
                testflight["localized_build_info"][locale]["whats_new"],
                "Recordatorios mejorados.",
            )
        self.config["locales"]["es"]["android"].append("es-419")
        self.save_config()
        with self.assertRaisesRegex(release.ReleaseError, "Duplicate android"):
            release.ReleaseManager(self.root)

    def test_rejected_export_leaves_previous_export_intact(self):
        self.prepare()
        output = self.manager.export("1.2.3", reviewed=True)
        before = (output / "manifest.json").read_bytes()
        self.edit(lambda manifest: manifest["notes"].update({"es": "Changed without review."}))
        with self.assertRaisesRegex(release.ReleaseError, "missing or stale"):
            self.manager.export("1.2.3", reviewed=True)
        self.assertEqual((output / "manifest.json").read_bytes(), before)

    def test_symlink_escape_is_rejected(self):
        outside = Path(self.temporary.name) / "outside"
        outside.mkdir()
        (self.root / "release/releases").symlink_to(outside, target_is_directory=True)
        with self.assertRaisesRegex(release.ReleaseError, "escapes project root"):
            self.manager.init("1.2.3", 53, 8)
        self.assertEqual(list(outside.iterdir()), [])

    def test_metadata_symlinks_are_rejected(self):
        self.prepare()
        metadata = self.root / "release/metadata/android"
        metadata.mkdir(parents=True)
        (metadata / "linked.txt").symlink_to(self.root / "release/config.json")
        with self.assertRaisesRegex(release.ReleaseError, "symlinks"):
            self.manager.export("1.2.3", reviewed=True)

    def test_cli_resolves_project_independently_of_working_directory(self):
        script = self.root / "scripts/release.py"
        script.parent.mkdir()
        shutil.copyfile(SCRIPT, script)
        result = subprocess.run(
            [sys.executable, str(script), "init", "1.2.3", "--android-code", "53", "--ios-build", "8"],
            cwd=self.temporary.name, capture_output=True, text=True,
        )
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertTrue((self.root / "release/releases/1.2.3.json").exists())


if __name__ == "__main__":
    unittest.main()
