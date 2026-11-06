"""Offline tests: fake credentials only; security commands are never executed."""
import base64
import datetime as dt
import importlib.util
import json
import os
from pathlib import Path
import plistlib
import tempfile
import unittest
from unittest.mock import patch

SPEC = importlib.util.spec_from_file_location("release_ci", Path(__file__).resolve().parents[1] / "release-ci.py")
ci = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(ci)


class ReleaseCredentialTests(unittest.TestCase):
    def setUp(self):
        self.fixture = tempfile.TemporaryDirectory()
        self.root = Path(self.fixture.name).resolve()
        self.repo = self.root / "repository"
        self.repo.mkdir()
        self.runner = self.root / "runner"
        self.runner.mkdir()
        self.env_file = self.root / "github-env"
        self.env_file.touch()
        self.environment = patch.dict(os.environ, {
            "GITHUB_ACTIONS": "true",
            "RUNNER_ENVIRONMENT": "github-hosted",
            "RUNNER_TEMP": str(self.runner),
            "GITHUB_ENV": str(self.env_file),
        }, clear=True)
        self.environment.start()
        self.root_patch = patch.object(ci, "ROOT", self.repo)
        self.root_patch.start()
        self.settings = {
            "android_package": "org.example.demo",
            "android_firebase_path": "app/google-services.json",
            "ios_bundle_id": "org.example.demo",
            "ios_team_id": "TEAM123456",
            "ios_firebase_path": "iosApp/GoogleService-Info.plist",
        }

    def tearDown(self):
        self.root_patch.stop()
        self.environment.stop()
        self.fixture.cleanup()

    def apply_github_env(self):
        for line in self.env_file.read_text().splitlines():
            name, value = line.split("=", 1)
            os.environ[name] = value

    def set_encoded(self, name, data):
        os.environ[name] = base64.b64encode(data).decode()

    def profile(self):
        return {
            "UUID": "00000000-0000-0000-0000-000000000001",
            "Name": "Demo App Store Profile",
            "TeamIdentifier": ["TEAM123456"],
            "ApplicationIdentifierPrefix": ["OLDPREFIX0"],
            "ExpirationDate": dt.datetime.now(dt.timezone.utc) + dt.timedelta(days=30),
            "Entitlements": {
                "application-identifier": "OLDPREFIX0.org.example.demo",
                "com.apple.developer.team-identifier": "TEAM123456",
                "get-task-allow": False,
            },
        }

    def test_runner_guard_rejects_personal_and_self_hosted_contexts(self):
        ci.runner_guard()
        os.environ["RUNNER_ENVIRONMENT"] = "self-hosted"
        with self.assertRaises(ci.SetupError):
            ci.runner_guard()
        os.environ.pop("GITHUB_ACTIONS")
        with self.assertRaises(ci.SetupError):
            ci.runner_guard()

    def test_config_paths_cannot_escape_checkout(self):
        for value in ("../outside.json", "/tmp/outside.json", "."):
            with self.subTest(value=value), self.assertRaises(ci.SetupError):
                ci.project_path(value)

    def test_existing_files_are_preserved(self):
        directory, state = ci.directory_and_state()
        target = self.repo / "existing.json"
        target.write_bytes(b"existing")
        with self.assertRaises(ci.SetupError):
            ci.write_new(directory, state, target, b"replacement")
        self.assertEqual(target.read_bytes(), b"existing")
        self.assertEqual(state["created_files"], [])

    def test_android_materialization_and_cleanup_use_only_temporary_files(self):
        for name in ("ANDROID_KEYSTORE_STORE_PASSWORD", "ANDROID_KEYSTORE_KEY_ALIAS", "ANDROID_KEYSTORE_KEY_PASSWORD"):
            os.environ[name] = "test-only"
        self.set_encoded("ANDROID_KEYSTORE_BASE64", b"fake-keystore")
        firebase = {"client": [{"client_info": {"android_client_info": {"package_name": "org.example.demo"}}}]}
        self.set_encoded("GOOGLE_SERVICES_JSON_BASE64", json.dumps(firebase).encode())
        directory, state = ci.directory_and_state()
        ci.prepare_android_build(directory, state, self.settings)
        self.apply_github_env()
        keystore = Path(os.environ["ANDROID_KEYSTORE_PATH"])
        self.assertEqual(keystore.read_bytes(), b"fake-keystore")
        self.assertEqual(keystore.stat().st_mode & 0o777, 0o600)
        self.assertNotIn("test-only", self.env_file.read_text())
        with patch.object(ci, "security") as security:
            ci.cleanup()
        security.assert_not_called()
        self.assertFalse(keystore.exists())
        self.assertFalse((self.repo / "app/google-services.json").exists())
        self.assertFalse(directory.exists())

    def test_cleanup_restores_keychains_even_after_partial_setup(self):
        directory, state = ci.directory_and_state()
        created_file = self.repo / "partial.plist"
        ci.write_new(directory, state, created_file, b"temporary")
        state.update({
            "keychain": str(directory / "release.keychain-db"),
            "original_keychains": ["/original/login.keychain-db"],
        })
        ci.save_state(directory, state)
        self.apply_github_env()
        with patch.object(ci, "security", return_value=b"") as security:
            ci.cleanup()
        self.assertEqual(security.call_args_list[0].args, (
            "list-keychains", "-d", "user", "-s", "/original/login.keychain-db"))
        self.assertEqual(security.call_args_list[1].args[0], "delete-keychain")
        self.assertFalse(created_file.exists())

    def test_android_firebase_rejects_other_package_and_invalid_shape(self):
        for data in (b"[]", b"{}", b"not json", b'{"client":[{}]}'):
            with self.subTest(data=data), self.assertRaises(ci.SetupError):
                ci.validate_android_firebase(data, "org.example.demo")

    def test_ios_firebase_rejects_other_bundle_and_invalid_shape(self):
        for data in (plistlib.dumps([]), plistlib.dumps({"BUNDLE_ID": "org.other"}), b"not plist"):
            with self.subTest(data=data), self.assertRaises(ci.SetupError):
                ci.validate_ios_firebase(data, "org.example.demo")

    def test_store_profile_accepts_legacy_app_prefix(self):
        ci.validate_profile(self.profile(), "org.example.demo", "TEAM123456")

    def test_store_profile_rejects_device_enterprise_debug_expired_and_wrong_app(self):
        variants = [
            {"ProvisionedDevices": ["fake-device"]},
            {"ProvisionsAllDevices": True},
            {"ExpirationDate": dt.datetime(2020, 1, 1)},
            {"TeamIdentifier": ["OTHERTEAM0"]},
            {"Name": "profile\nINJECTED_ENV=yes"},
        ]
        for change in variants:
            profile = self.profile()
            profile.update(change)
            with self.subTest(change=change), self.assertRaises(ci.SetupError):
                ci.validate_profile(profile, "org.example.demo", "TEAM123456")
        for change in (
            {"get-task-allow": True},
            {"application-identifier": "OLDPREFIX0.*"},
            {"com.apple.developer.team-identifier": "OTHERTEAM0"},
        ):
            profile = self.profile()
            profile["Entitlements"].update(change)
            with self.subTest(change=change), self.assertRaises(ci.SetupError):
                ci.validate_profile(profile, "org.example.demo", "TEAM123456")

    def test_environment_output_rejects_newline_injection(self):
        with self.assertRaises(ci.SetupError):
            ci.emit_env({"IOS_PROFILE_NAME": "good\nBAD=yes"})
        self.assertEqual(self.env_file.read_text(), "")

    def test_invalid_base64_does_not_echo_material(self):
        os.environ["FAKE_SECRET"] = "not-a-real-secret$"
        with self.assertRaises(ci.SetupError) as error:
            ci.decode("FAKE_SECRET")
        self.assertNotIn(os.environ["FAKE_SECRET"], str(error.exception))


if __name__ == "__main__":
    unittest.main()
