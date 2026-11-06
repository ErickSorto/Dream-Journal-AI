import copy
import importlib.util
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

SCRIPTS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(SCRIPTS))
import release_generate as generation
from release_store import StoreError, StorePlanner

spec = importlib.util.spec_from_file_location("release_generation_test_manager", SCRIPTS / "release.py")
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)


class GenerationTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        (self.root / "release").mkdir()
        self.config = {
            "app_name": "DreamNorth", "source_locale": "en-US",
            "locales": {"en-US": {"android": "en-US", "ios": "en-US"},
                        "es-ES": {"android": ["es-ES", "es-419", "es-US"], "ios": ["es-ES", "es-MX"]}},
        }
        self.config_path = self.root / "release/config.json"
        self.config_path.write_text(json.dumps(self.config))
        self.git("init", "-q")
        self.git("config", "user.name", "Release Test")
        self.git("config", "user.email", "release-test@example.invalid")
        self.git("config", "commit.gpgsign", "false")
        (self.root / "app.kt").write_text("charge on error\n")
        self.git("add", "app.kt")
        self.git("commit", "-qm", "Initial app")
        self.base = self.git("rev-parse", "HEAD").strip()
        (self.root / "app.kt").write_text("charge only on success\n")
        self.git("add", "app.kt")
        self.git("commit", "-qm", "fix(ai): keep tokens after failed generation")
        self.head = self.git("rev-parse", "HEAD").strip()
        self.manager = release.ReleaseManager(self.root)
        self.manager.init("1.2.3", 112, 94, "Earlier source notes")
        self.path = self.manager.manifest_path("1.2.3")
        self.request_path = self.root / "request.json"
        self.request = self.make_request()
        self.notes = {"en-US": "Failed dream interpretations no longer spend tokens.",
                      "es-ES": "Las interpretaciones fallidas de sueños ya no gastan fichas."}

    def git(self, *args):
        return subprocess.run(["git", "-C", str(self.root), *args], check=True, capture_output=True, text=True).stdout

    def make_request(self):
        generation.create_request(self.manager, "1.2.3", self.base, "HEAD", self.request_path)
        return json.loads(self.request_path.read_text())

    def response(self):
        return {"schema_version": 1, "request_sha256": self.request["request_sha256"], "notes": self.notes.copy()}

    def assert_rejected_without_write(self, request, response):
        before = self.path.read_bytes()
        with self.assertRaises(StoreError):
            generation.import_generated(self.manager, "1.2.3", request, response)
        self.assertEqual(before, self.path.read_bytes())

    def test_evidence_is_committed_range_only_without_file_contents(self):
        (self.root / "app.kt").write_text("PRIVATE_UNCOMMITTED_VALUE")
        (self.root / "secret.txt").write_text("PRIVATE_UNTRACKED_VALUE")
        packet = generation.request_payload(self.manager, "1.2.3", self.base, "HEAD")
        self.assertEqual(packet["git"]["head_commit"], self.head)
        self.assertEqual(packet["evidence"]["changed_paths"], ["app.kt"])
        self.assertEqual(len(packet["evidence"]["commits"]), 1)
        self.assertNotIn("PRIVATE_", json.dumps(packet))
        self.assertNotIn("charge only on success", json.dumps(packet))

    def test_source_and_translations_use_existing_export_and_review_gates(self):
        manifest = self.manager.load("1.2.3")
        manifest["notes"]["es-ES"] = "Texto anterior"
        release.write_json(self.path, manifest)
        self.manager.review_all("1.2.3")
        StorePlanner(self.manager).review("1.2.3")
        self.request_path.unlink()
        self.request = self.make_request()
        before = self.manager.load("1.2.3")
        generation.import_generated(self.manager, "1.2.3", self.request, self.response())
        after = self.manager.load("1.2.3")
        self.assertEqual(after["notes"], self.notes)
        self.assertEqual(after["store"], before["store"])
        self.assertEqual(after["android_version_code"], 112)
        self.assertEqual(after["ios_build_number"], 94)
        self.assertEqual(after["reviews"], {})
        self.assertNotIn("store_review", after)
        with self.assertRaises(release.ReleaseError):
            self.manager.validate("1.2.3", reviewed=True)
        self.manager.review_all("1.2.3")
        output = self.manager.export("1.2.3", reviewed=True)
        self.assertEqual((output / "metadata/android/es-419/changelogs/112.txt").read_text(), self.notes["es-ES"])
        self.assertEqual((output / "metadata/ios/es-MX/release_notes.txt").read_text(), self.notes["es-ES"])

    def test_incomplete_invalid_and_extra_output_never_partially_updates_source(self):
        for change in (lambda r: r["notes"].pop("es-ES"),
                       lambda r: r["notes"].update(fr="Unexpected"),
                       lambda r: r["notes"].update({"es-ES": "x" * 501}),
                       lambda r: r["notes"].update({"es-ES": "TODO"}),
                       lambda r: r["notes"].update({"es-ES": ""}),
                       lambda r: r["notes"].update({"en-US": None}),
                       lambda r: r.update(reviews={"en-US": "approved"}),
                       lambda r: r.update(schema_version=True),
                       lambda r: r.update(request_sha256="wrong")):
            response = self.response()
            change(response)
            self.assert_rejected_without_write(self.request, response)

    def test_manifest_edits_invalidate_generation(self):
        data = self.manager.load("1.2.3")
        data["notes"]["en-US"] = "Another editor's source"
        release.write_json(self.path, data)
        self.assert_rejected_without_write(self.request, self.response())

    def test_config_edits_after_manager_created_invalidate_generation(self):
        self.config["app_name"] = "New product"
        self.config_path.write_text(json.dumps(self.config))
        self.assert_rejected_without_write(self.request, self.response())

    def test_advancing_head_invalidates_generation(self):
        (self.root / "app.kt").write_text("another release change")
        self.git("add", "app.kt")
        self.git("commit", "-qm", "Another change")
        self.assert_rejected_without_write(self.request, self.response())

    def test_request_tampering_rejected_even_with_recomputed_hash(self):
        request = copy.deepcopy(self.request)
        request["evidence"]["commits"][0]["subject"] = "Invented feature"
        request["request_sha256"] = generation.json_hash({k: v for k, v in request.items() if k != "request_sha256"})
        self.assert_rejected_without_write(request, self.response())

    def test_empty_or_reverse_ranges_and_unsafe_refs_rejected(self):
        for base, head in ((self.base, self.base), (self.head, self.base), ("--help", "HEAD"), ("missing", "HEAD")):
            with self.subTest(base=base, head=head), self.assertRaises(StoreError):
                generation.request_payload(self.manager, "1.2.3", base, head)

    def test_request_does_not_overwrite_existing_file_or_symlink(self):
        with self.assertRaises(StoreError):
            self.make_request()
        link = self.root / "link.json"
        link.symlink_to(self.root / "not-created.json")
        with self.assertRaises(StoreError):
            generation.create_request(self.manager, "1.2.3", self.base, "HEAD", link)
        self.assertFalse((self.root / "not-created.json").exists())

    def test_generator_receives_json_and_drafts_import_in_one_operation(self):
        program = "import json,sys; r=json.load(sys.stdin); print(json.dumps({'schema_version':1,'request_sha256':r['request_sha256'],'notes':" + repr(self.notes) + "}))"
        generation.generate(self.manager, "1.2.3", self.request_path, [sys.executable, "-c", program])
        self.assertEqual(self.manager.load("1.2.3")["notes"], self.notes)
        self.assertEqual(self.manager.load("1.2.3")["reviews"], {})

    def test_generator_failure_output_and_timeout_are_not_echoed_or_imported(self):
        for program, timeout in (("import sys; print('PRIVATE_OUTPUT'); print('PRIVATE_ERROR',file=sys.stderr); sys.exit(2)", 10),
                                 ("print('not json PRIVATE_OUTPUT')", 10),
                                 ("print('X' * 33000)", 10),
                                 ("import time; time.sleep(2)", 1)):
            before = self.path.read_bytes()
            with self.subTest(program=program), self.assertRaises(StoreError) as context:
                generation.generate(self.manager, "1.2.3", self.request_path, [sys.executable, "-c", program], timeout)
            self.assertNotIn("PRIVATE_", str(context.exception))
            self.assertEqual(self.path.read_bytes(), before)

    def test_config_change_during_generator_run_is_detected_before_import(self):
        before = self.path.read_bytes()
        program = (
            "import json,sys,pathlib; r=json.load(sys.stdin); "
            "p=pathlib.Path('release/config.json'); c=json.loads(p.read_text()); "
            "c['app_name']='Changed during generation'; p.write_text(json.dumps(c)); "
            "print(json.dumps({'schema_version':1,'request_sha256':r['request_sha256'],'notes':"
            + repr(self.notes) + "}))"
        )
        with self.assertRaisesRegex(StoreError, "stale"):
            generation.generate(self.manager, "1.2.3", self.request_path, [sys.executable, "-c", program])
        self.assertEqual(self.path.read_bytes(), before)

    def test_cli_current_agent_import_needs_no_generator_credentials(self):
        response = self.root / "response.json"
        response.write_text(json.dumps(self.response()))
        original = release.ROOT
        try:
            release.ROOT = self.root
            result = release.main(["import-generated-notes", "1.2.3", "--request", str(self.request_path), "--response", str(response)])
        finally:
            release.ROOT = original
        self.assertEqual(result, 0)
        self.assertEqual(self.manager.load("1.2.3")["notes"], self.notes)


if __name__ == "__main__":
    unittest.main()
