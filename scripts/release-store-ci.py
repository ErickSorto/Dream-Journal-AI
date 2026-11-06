#!/usr/bin/env python3
"""Validate manual store requests and stage existing API keys on hosted runners.

This helper never builds an app, imports signing material, calls a store,
creates credentials, or changes account permissions.
"""
from __future__ import annotations

import argparse
import base64
import binascii
import json
import os
from pathlib import Path
import re
import shutil
import sys
import tempfile

VERSION_PATTERN = re.compile(r"(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)")
GOOGLE_ATTESTATION = "managed-publishing-on-no-unrelated-changes"
TEMP_PREFIX = "release-store-credentials-"


class StoreSetupError(ValueError):
    pass


def require(name: str) -> str:
    value = os.environ.get(name, "")
    if not value:
        raise StoreSetupError(f"Required environment variable {name} is missing.")
    return value


def check_request() -> tuple[str, str, str]:
    version = require("RELEASE_VERSION")
    platform = require("RELEASE_PLATFORM")
    action = require("RELEASE_STORE_ACTION")
    if not VERSION_PATTERN.fullmatch(version):
        raise StoreSetupError("The version must contain three integers with no leading zeros.")
    if platform not in ("android", "ios") or action not in ("prepare", "submit", "publish"):
        raise StoreSetupError("Unsupported platform or store action.")
    if os.environ.get("RELEASE_STORE_ACTION_CONFIRMED") != f"{platform}:{action}:{version}":
        raise StoreSetupError("Confirmation must exactly match <platform>:<action>:<version>.")
    if platform == "android" and action == "publish":
        raise StoreSetupError(
            "Android publishing is Console-only. Open Google Play Console > Publishing overview, "
            "inspect every change ready to publish, then separately authorize Publish changes. "
            "No API job has started."
        )
    if platform == "android" and action == "submit":
        if os.environ.get("RELEASE_GOOGLE_MANAGED_PUBLISHING_ATTESTATION") != GOOGLE_ATTESTATION:
            raise StoreSetupError(
                "Before Android submission, verify Managed publishing is on and no unrelated "
                "changes are pending in Play Console; then supply the exact attestation."
            )
    return platform, action, version


def runner_guard() -> None:
    if os.environ.get("GITHUB_ACTIONS") != "true" or os.environ.get("RUNNER_ENVIRONMENT") != "github-hosted":
        raise StoreSetupError("API key staging is restricted to ephemeral GitHub-hosted runners.")


def decode(name: str) -> bytes:
    encoded = require(name)
    try:
        return base64.b64decode("".join(encoded.split()), validate=True)
    except (ValueError, binascii.Error):
        raise StoreSetupError(f"{name} is not valid base64.") from None


def emit_env(values: dict[str, str]) -> None:
    if any("\n" in value or "\r" in value for value in values.values()):
        raise StoreSetupError("Invalid newline in a temporary file path.")
    # Paths only. Private key values never enter GITHUB_ENV or logs.
    with Path(require("GITHUB_ENV")).open("a", encoding="utf-8") as target:
        for name, value in values.items():
            target.write(f"{name}={value}\n")


def new_directory() -> Path:
    if os.environ.get("RELEASE_STORE_TEMP_DIR"):
        raise StoreSetupError("API keys have already been staged for this job.")
    directory = Path(tempfile.mkdtemp(prefix=TEMP_PREFIX, dir=require("RUNNER_TEMP"))).resolve()
    directory.chmod(0o700)
    emit_env({"RELEASE_STORE_TEMP_DIR": str(directory)})
    return directory


def write_private(path: Path, data: bytes) -> None:
    # Open exclusively and with owner-only permissions from the first write.
    descriptor = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    with os.fdopen(descriptor, "wb") as target:
        target.write(data)


def prepare_android() -> None:
    data = decode("PLAY_SERVICE_ACCOUNT_JSON_BASE64")
    try:
        document = json.loads(data)
        valid = isinstance(document, dict) and document.get("type") == "service_account" \
            and isinstance(document.get("private_key"), str) and document["private_key"] \
            and isinstance(document.get("client_email"), str) and document["client_email"]
    except (ValueError, UnicodeError):
        valid = False
    if not valid:
        raise StoreSetupError("Google Play requires an existing service-account JSON key.")
    directory = new_directory()
    path = directory / "play-service-account.json"
    write_private(path, data)
    emit_env({"PLAY_SERVICE_ACCOUNT_JSON": str(path)})


def prepare_ios() -> None:
    require("ASC_KEY_ID")
    # ASC_ISSUER_ID may be empty for an individual key. The Fastlane lane handles
    # team versus individual token configuration without modifying the account.
    data = decode("ASC_PRIVATE_KEY_BASE64")
    if b"-----BEGIN PRIVATE KEY-----" not in data or b"-----END PRIVATE KEY-----" not in data:
        raise StoreSetupError("The App Store Connect key must be an existing PEM private key.")
    directory = new_directory()
    path = directory / "app-store-connect.p8"
    write_private(path, data)
    emit_env({"ASC_KEY_PATH": str(path)})


def cleanup() -> None:
    raw_directory = os.environ.get("RELEASE_STORE_TEMP_DIR")
    if not raw_directory:
        return
    raw_path = Path(raw_directory)
    directory = raw_path.resolve()
    runner_temp = Path(require("RUNNER_TEMP")).resolve()
    if raw_path.is_symlink() or directory.parent != runner_temp or not directory.name.startswith(TEMP_PREFIX):
        raise StoreSetupError("Refusing cleanup outside the job's dedicated temporary directory.")
    if directory.exists():
        shutil.rmtree(directory)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("operation", choices=("check", "android", "ios", "cleanup"))
    args = parser.parse_args()
    try:
        if args.operation == "check":
            check_request()
        else:
            runner_guard()
            if args.operation == "cleanup":
                cleanup()
            else:
                platform, _, _ = check_request()
                if platform != args.operation:
                    raise StoreSetupError("API key platform does not match the confirmed request.")
                if platform == "android":
                    prepare_android()
                else:
                    prepare_ios()
        print(f"Store workflow {args.operation} completed.")
        return 0
    except StoreSetupError as error:
        print(f"Store workflow blocked: {error}", file=sys.stderr)
        return 1
    except (OSError, ValueError, UnicodeError):
        print("Store workflow blocked: invalid API key data or unavailable temporary file.", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
