"""Offline store workflow tests. No real API keys, store calls or runner changes."""
import base64
import importlib.util
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location(
    "release_store_ci", Path(__file__).resolve().parents[1] / "release-store-ci.py"
)
ci = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(ci)


class StoreWorkflowTests(unittest.TestCase):
    def setUp(self):
        self.fixture = tempfile.TemporaryDirectory()
        self.root = Path(self.fixture.name).resolve()
        self.runner = self.root / "runner"
        self.runner.mkdir()
        self.env_file = self.root / "github-env"
        self.env_file.touch()
        self.environment = patch.dict(os.environ, {
            "GITHUB_ACTIONS": "true",
            "RUNNER_ENVIRONMENT": "github-hosted",
            "RUNNER_TEMP": str(self.runner),
            "GITHUB_ENV": str(self.env_file),
            "RELEASE_VERSION": "2.0.0",
            "RELEASE_PLATFORM": "ios",
            "RELEASE_STORE_ACTION": "prepare",
            "RELEASE_STORE_ACTION_CONFIRMED": "ios:prepare:2.0.0",
        }, clear=True)
        self.environment.start()

    def tearDown(self):
        self.environment.stop()
        self.fixture.cleanup()

    def request(self, platform, action):
        os.environ.update({
            "RELEASE_PLATFORM": platform,
            "RELEASE_STORE_ACTION": action,
            "RELEASE_STORE_ACTION_CONFIRMED": f"{platform}:{action}:2.0.0",
        })

    def apply_github_env(self):
        for line in self.env_file.read_text().splitlines():
            name, value = line.split("=", 1)
            os.environ[name] = value

    def encode(self, name, data):
        os.environ[name] = base64.b64encode(data).decode()

    def test_each_ios_action_requires_its_own_exact_confirmation(self):
        for action in ("prepare", "submit", "publish"):
            self.request("ios", action)
            self.assertEqual(ci.check_request(), ("ios", action, "2.0.0"))
            os.environ["RELEASE_STORE_ACTION_CONFIRMED"] = "ios:other:2.0.0"
            with self.assertRaises(ci.StoreSetupError):
                ci.check_request()

    def test_confirmation_cannot_authorize_another_version_or_platform(self):
        for confirmation in ("ios:prepare:2.0.1", "android:prepare:2.0.0", "ios:prepare:2.0.0 ", ""):
            os.environ["RELEASE_STORE_ACTION_CONFIRMED"] = confirmation
            with self.subTest(confirmation=confirmation), self.assertRaises(ci.StoreSetupError):
                ci.check_request()

    def test_versions_cannot_inject_paths_or_shell(self):
        for value in ("../2.0.0", "2.0.0;echo bad", "02.0.0", "2.0.0\n", "2.0"):
            os.environ["RELEASE_VERSION"] = value
            os.environ["RELEASE_STORE_ACTION_CONFIRMED"] = f"ios:prepare:{value}"
            with self.subTest(value=value), self.assertRaises(ci.StoreSetupError):
                ci.check_request()

    def test_android_publish_stops_without_credentials(self):
        self.request("android", "publish")
        with self.assertRaisesRegex(ci.StoreSetupError, "Publishing overview"):
            ci.check_request()
        self.assertEqual(list(self.runner.iterdir()), [])
        self.assertEqual(self.env_file.read_text(), "")

    def test_android_prepare_does_not_require_managed_publish_attestation(self):
        self.request("android", "prepare")
        self.assertEqual(ci.check_request(), ("android", "prepare", "2.0.0"))

    def test_android_submit_requires_exact_managed_publish_attestation(self):
        self.request("android", "submit")
        for value in ("", "true", ci.GOOGLE_ATTESTATION + " "):
            os.environ["RELEASE_GOOGLE_MANAGED_PUBLISHING_ATTESTATION"] = value
            with self.subTest(value=value), self.assertRaises(ci.StoreSetupError):
                ci.check_request()
        os.environ["RELEASE_GOOGLE_MANAGED_PUBLISHING_ATTESTATION"] = ci.GOOGLE_ATTESTATION
        self.assertEqual(ci.check_request(), ("android", "submit", "2.0.0"))

    def test_staging_rejects_personal_or_self_hosted_runner(self):
        ci.runner_guard()
        for name, value in (("GITHUB_ACTIONS", "false"), ("RUNNER_ENVIRONMENT", "self-hosted")):
            with patch.dict(os.environ, {name: value}), self.assertRaises(ci.StoreSetupError):
                ci.runner_guard()

    def test_google_key_is_private_temporary_file_and_cleanup_is_scoped(self):
        data = json.dumps({
            "type": "service_account", "private_key": "TEST-ONLY-PRIVATE-KEY",
            "client_email": "test-only@example.invalid",
        }).encode()
        self.encode("PLAY_SERVICE_ACCOUNT_JSON_BASE64", data)
        ci.prepare_android()
        self.apply_github_env()
        key_path = Path(os.environ["PLAY_SERVICE_ACCOUNT_JSON"])
        self.assertEqual(key_path.read_bytes(), data)
        self.assertEqual(key_path.stat().st_mode & 0o777, 0o600)
        self.assertEqual(key_path.parent.stat().st_mode & 0o777, 0o700)
        self.assertNotIn("TEST-ONLY-PRIVATE-KEY", self.env_file.read_text())
        ci.cleanup()
        self.assertFalse(key_path.parent.exists())

    def test_individual_apple_key_can_have_empty_issuer(self):
        os.environ["ASC_KEY_ID"] = "TESTONLY01"
        os.environ["ASC_ISSUER_ID"] = ""
        data = b"-----BEGIN PRIVATE KEY-----\nTEST-ONLY\n-----END PRIVATE KEY-----\n"
        self.encode("ASC_PRIVATE_KEY_BASE64", data)
        ci.prepare_ios()
        self.apply_github_env()
        key_path = Path(os.environ["ASC_KEY_PATH"])
        self.assertEqual(key_path.read_bytes(), data)
        self.assertNotIn("TEST-ONLY", self.env_file.read_text())
        ci.cleanup()
        self.assertFalse(key_path.exists())

    def test_invalid_google_payload_never_creates_credential_directory(self):
        for data in (b"[]", b"not-json", b"{}", b'{"type":"service_account"}'):
            self.encode("PLAY_SERVICE_ACCOUNT_JSON_BASE64", data)
            with self.subTest(data=data), self.assertRaises(ci.StoreSetupError):
                ci.prepare_android()
        self.assertEqual(list(self.runner.iterdir()), [])

    def test_invalid_base64_error_does_not_echo_secret(self):
        os.environ["FAKE_SECRET"] = "secret-material-$"
        with self.assertRaises(ci.StoreSetupError) as context:
            ci.decode("FAKE_SECRET")
        self.assertNotIn("secret-material", str(context.exception))

    def test_cleanup_does_not_delete_outside_dedicated_directory(self):
        outside = self.root / "unrelated"
        outside.mkdir()
        os.environ["RELEASE_STORE_TEMP_DIR"] = str(outside)
        with self.assertRaises(ci.StoreSetupError):
            ci.cleanup()
        self.assertTrue(outside.exists())
        linked = self.runner / (ci.TEMP_PREFIX + "symlink")
        linked.symlink_to(outside, target_is_directory=True)
        os.environ["RELEASE_STORE_TEMP_DIR"] = str(linked)
        with self.assertRaises(ci.StoreSetupError):
            ci.cleanup()
        self.assertTrue(outside.exists())

    def test_second_staging_refuses_to_overwrite_keys(self):
        ci.new_directory()
        self.apply_github_env()
        with self.assertRaises(ci.StoreSetupError):
            ci.new_directory()
        self.assertEqual(len(list(self.runner.iterdir())), 1)

    def test_env_paths_reject_newlines(self):
        with self.assertRaises(ci.StoreSetupError):
            ci.emit_env({"ASC_KEY_PATH": "/tmp/a\nBAD=value"})
        self.assertEqual(self.env_file.read_text(), "")


if __name__ == "__main__":
    unittest.main()
