#!/usr/bin/env python3
"""Materialize existing release credentials on an ephemeral GitHub-hosted runner.

This script does not create credentials or change store/CI account permissions.
Never run it against a developer's personal keychain.
"""
from __future__ import annotations

import argparse
import base64
import binascii
import datetime as dt
import json
import os
from pathlib import Path
import plistlib
import re
import secrets
import shlex
import shutil
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]


class SetupError(Exception):
    pass


def require(name: str) -> str:
    value = os.environ.get(name, "")
    if not value:
        raise SetupError(f"Required environment variable {name} is missing.")
    return value


def decode(name: str) -> bytes:
    try:
        return base64.b64decode("".join(require(name).split()), validate=True)
    except (ValueError, binascii.Error):
        raise SetupError(f"{name} is not valid base64.") from None


def config() -> dict:
    return json.loads((ROOT / "release/config.json").read_text())


def project_path(value: str) -> Path:
    path = (ROOT / value).resolve()
    if Path(value).is_absolute() or not path.is_relative_to(ROOT) or path == ROOT:
        raise SetupError("A configured output path leaves the repository.")
    return path


def emit_env(values: dict[str, str]) -> None:
    # Only paths and public profile identifiers belong here, never secret values.
    for name, value in values.items():
        if "\n" in value or "\r" in value:
            raise SetupError(f"Invalid newline in {name}.")
    with Path(require("GITHUB_ENV")).open("a") as target:
        for name, value in values.items():
            target.write(f"{name}={value}\n")


def runner_guard() -> None:
    if os.environ.get("GITHUB_ACTIONS") != "true":
        raise SetupError("Credential setup is restricted to the release workflow.")
    if os.environ.get("RUNNER_ENVIRONMENT") != "github-hosted":
        raise SetupError("Credential setup requires an ephemeral GitHub-hosted runner.")


def state_path(directory: Path) -> Path:
    return directory / "state.json"


def save_state(directory: Path, state: dict) -> None:
    path = state_path(directory)
    path.write_text(json.dumps(state))
    path.chmod(0o600)


def directory_and_state() -> tuple[Path, dict]:
    existing = os.environ.get("RELEASE_TEMP_DIR")
    if existing:
        directory = Path(existing).resolve()
        runner_temp = Path(require("RUNNER_TEMP")).resolve()
        if directory.parent != runner_temp or not directory.name.startswith("release-credentials-"):
            raise SetupError("Invalid release credential temporary directory.")
        return directory, json.loads(state_path(directory).read_text())
    directory = Path(tempfile.mkdtemp(prefix="release-credentials-", dir=require("RUNNER_TEMP")))
    directory.chmod(0o700)
    state = {"created_files": [], "keychain": None, "original_keychains": None}
    save_state(directory, state)
    # Persist before any credential operation so the always() cleanup step works
    # even when preparation exits halfway through.
    emit_env({"RELEASE_TEMP_DIR": str(directory)})
    return directory, state


def write_new(directory: Path, state: dict, path: Path, data: bytes) -> None:
    if path.exists() or path.is_symlink():
        raise SetupError("Refusing to overwrite an existing credential/configuration file.")
    path.parent.mkdir(parents=True, exist_ok=True)
    state["created_files"].append(str(path))
    save_state(directory, state)
    with path.open("xb") as target:
        target.write(data)
    path.chmod(0o600)


def security(*args: str) -> bytes:
    result = subprocess.run(["security", *args], capture_output=True, check=False)
    if result.returncode:
        # security arguments and diagnostic text can include credential material.
        raise SetupError(f"macOS security operation '{args[0]}' failed; details suppressed.")
    return result.stdout


def validate_android_firebase(data: bytes, package: str) -> None:
    try:
        document = json.loads(data)
        packages = {
            client["client_info"]["android_client_info"]["package_name"]
            for client in document["client"]
            if "android_client_info" in client.get("client_info", {})
        }
    except (ValueError, KeyError, TypeError, AttributeError):
        raise SetupError("Android Firebase configuration has an invalid structure.") from None
    if package not in packages:
        raise SetupError("Android Firebase configuration does not match this application.")


def validate_ios_firebase(data: bytes, bundle_id: str) -> None:
    try:
        document = plistlib.loads(data)
    except Exception:
        raise SetupError("iOS Firebase configuration is not a valid plist.") from None
    if not isinstance(document, dict) or document.get("BUNDLE_ID") != bundle_id:
        raise SetupError("iOS Firebase configuration does not match this application.")


def validate_profile(profile: dict, bundle_id: str, team_id: str) -> None:
    if not isinstance(profile, dict):
        raise SetupError("The provisioning profile has an invalid structure.")
    entitlements = profile.get("Entitlements", {})
    prefixes = profile.get("ApplicationIdentifierPrefix", [])
    if not isinstance(entitlements, dict) or not isinstance(prefixes, list):
        raise SetupError("The provisioning profile has invalid entitlements.")
    expected_ids = {f"{prefix}.{bundle_id}" for prefix in prefixes}
    if not expected_ids or entitlements.get("application-identifier") not in expected_ids:
        raise SetupError("The provisioning profile does not match the exact iOS bundle ID.")
    if team_id not in profile.get("TeamIdentifier", []):
        raise SetupError("The provisioning profile does not match the configured Apple team.")
    if entitlements.get("com.apple.developer.team-identifier") != team_id:
        raise SetupError("The profile's team entitlement does not match the configured Apple team.")
    if profile.get("ProvisionedDevices") or profile.get("ProvisionsAllDevices"):
        raise SetupError("Use an App Store distribution profile, not a device or enterprise profile.")
    if entitlements.get("get-task-allow") is not False:
        raise SetupError("The provisioning profile allows development debugging.")
    expiry = profile.get("ExpirationDate")
    if not isinstance(expiry, dt.datetime):
        raise SetupError("The provisioning profile has no valid expiry date.")
    if expiry.replace(tzinfo=dt.timezone.utc) <= dt.datetime.now(dt.timezone.utc):
        raise SetupError("The provisioning profile has expired.")
    if not re.fullmatch(r"[0-9A-Fa-f-]{36}", str(profile.get("UUID", ""))):
        raise SetupError("The provisioning profile UUID is invalid.")
    if not isinstance(profile.get("Name"), str) or not profile["Name"].strip():
        raise SetupError("The provisioning profile name is missing.")
    if any(character in profile["Name"] for character in "\r\n"):
        raise SetupError("The provisioning profile name contains a newline.")


def prepare_android_build(directory: Path, state: dict, settings: dict) -> None:
    for name in ("ANDROID_KEYSTORE_STORE_PASSWORD", "ANDROID_KEYSTORE_KEY_ALIAS", "ANDROID_KEYSTORE_KEY_PASSWORD"):
        require(name)
    firebase = decode("GOOGLE_SERVICES_JSON_BASE64")
    validate_android_firebase(firebase, settings["android_package"])
    keystore_path = directory / "android-upload.keystore"
    write_new(directory, state, keystore_path, decode("ANDROID_KEYSTORE_BASE64"))
    write_new(directory, state, project_path(settings["android_firebase_path"]), firebase)
    emit_env({"ANDROID_KEYSTORE_PATH": str(keystore_path)})


def prepare_android_upload(directory: Path, state: dict) -> None:
    data = decode("PLAY_SERVICE_ACCOUNT_JSON_BASE64")
    try:
        document = json.loads(data)
        valid = document.get("type") == "service_account" and document.get("private_key") and document.get("client_email")
    except (ValueError, AttributeError):
        valid = False
    if not valid:
        raise SetupError("Google Play credentials must be an existing service-account JSON key.")
    path = directory / "play-service-account.json"
    write_new(directory, state, path, data)
    emit_env({"PLAY_SERVICE_ACCOUNT_JSON": str(path)})


def prepare_ios_build(directory: Path, state: dict, settings: dict) -> None:
    p12_password = require("IOS_DISTRIBUTION_P12_PASSWORD")
    team_id = settings["ios_team_id"]
    firebase = decode("GOOGLE_SERVICE_INFO_PLIST_BASE64")
    validate_ios_firebase(firebase, settings["ios_bundle_id"])
    profile_source = directory / "distribution.mobileprovision"
    write_new(directory, state, profile_source, decode("IOS_PROVISIONING_PROFILE_BASE64"))
    try:
        profile = plistlib.loads(security("cms", "-D", "-i", str(profile_source)))
    except (ValueError, plistlib.InvalidFileException):
        raise SetupError("The supplied provisioning profile cannot be decoded.") from None
    validate_profile(profile, settings["ios_bundle_id"], team_id)
    p12 = directory / "distribution.p12"
    write_new(directory, state, p12, decode("IOS_DISTRIBUTION_P12_BASE64"))
    # Xcode 16+ searches this directory; install the same existing profile in
    # the historical location as well for toolchain compatibility.
    for profile_dir in (
        Path.home() / "Library/Developer/Xcode/UserData/Provisioning Profiles",
        Path.home() / "Library/MobileDevice/Provisioning Profiles",
    ):
        write_new(directory, state, profile_dir / f"{profile['UUID']}.mobileprovision", profile_source.read_bytes())
    keychain = directory / "release.keychain-db"
    original_keychains = shlex.split(security("list-keychains", "-d", "user").decode())
    state.update({"keychain": str(keychain), "original_keychains": original_keychains})
    save_state(directory, state)
    password = secrets.token_urlsafe(32)  # Temporary local keychain only, not an account credential.
    security("create-keychain", "-p", password, str(keychain))
    security("set-keychain-settings", "-lut", "21600", str(keychain))
    security("unlock-keychain", "-p", password, str(keychain))
    security("import", str(p12), "-k", str(keychain), "-P", p12_password,
             "-T", "/usr/bin/codesign", "-T", "/usr/bin/security")
    security("set-key-partition-list", "-S", "apple-tool:,apple:,codesign:", "-s", "-k", password, str(keychain))
    security("list-keychains", "-d", "user", "-s", str(keychain), *original_keychains)
    identities = security("find-identity", "-v", "-p", "codesigning", str(keychain)).decode()
    if "Apple Distribution" not in identities and "iPhone Distribution" not in identities:
        raise SetupError("The imported keychain has no valid Apple distribution signing identity.")
    write_new(directory, state, project_path(settings["ios_firebase_path"]), firebase)
    emit_env({"IOS_PROFILE_NAME": profile["Name"], "IOS_TEAM_ID": team_id})


def prepare_ios_upload(directory: Path, state: dict) -> None:
    require("ASC_KEY_ID")
    require("ASC_ISSUER_ID")
    data = decode("ASC_PRIVATE_KEY_BASE64")
    if b"-----BEGIN PRIVATE KEY-----" not in data:
        raise SetupError("The App Store Connect API key is not a PEM private key.")
    path = directory / "app-store-connect.p8"
    write_new(directory, state, path, data)
    emit_env({"ASC_KEY_PATH": str(path)})


def cleanup() -> None:
    if not os.environ.get("RELEASE_TEMP_DIR"):
        return
    directory, state = directory_and_state()
    problems = []
    if state["original_keychains"] is not None:
        try:
            security("list-keychains", "-d", "user", "-s", *state["original_keychains"])
        except SetupError:
            problems.append("restoring the keychain search list")
    if state["keychain"]:
        try:
            security("delete-keychain", state["keychain"])
        except SetupError:
            # A failed create-keychain may not have produced a keychain.
            if Path(state["keychain"]).exists():
                problems.append("deleting the temporary keychain")
    for filename in reversed(state["created_files"]):
        try:
            Path(filename).unlink(missing_ok=True)
        except OSError:
            problems.append("deleting a temporary credential/configuration file")
    try:
        shutil.rmtree(directory)
    except OSError:
        problems.append("deleting the temporary credential directory")
    if problems:
        raise SetupError("Cleanup could not finish: " + ", ".join(problems) + ". Discard this hosted runner.")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=("android-build", "android-upload", "ios-build", "ios-upload", "cleanup"))
    args = parser.parse_args()
    try:
        runner_guard()
        if args.action == "cleanup":
            cleanup()
        else:
            directory, state = directory_and_state()
            settings = config()
            if args.action == "android-build":
                prepare_android_build(directory, state, settings)
            elif args.action == "android-upload":
                prepare_android_upload(directory, state)
            elif args.action == "ios-build":
                prepare_ios_build(directory, state, settings)
            else:
                prepare_ios_upload(directory, state)
        print(f"Release credential {args.action} completed.")
        return 0
    except (SetupError, OSError, KeyError, ValueError):
        # Config parse errors and OS messages might echo sensitive contents or
        # paths; the explicit setup errors above are safe to show.
        error = sys.exc_info()[1]
        message = str(error) if isinstance(error, SetupError) else "Invalid release configuration or unavailable credential file."
        print(f"Release credential setup failed: {message}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
