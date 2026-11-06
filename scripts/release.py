#!/usr/bin/env python3
"""Prepare and review localized release notes locally; never contact a store."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import sys
import tempfile


ROOT = Path(__file__).resolve().parents[1]
VERSION_PATTERN = re.compile(r"(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)\.(?:0|[1-9][0-9]*)")
LOCALE_PATTERN = re.compile(r"[A-Za-z]{2,3}(?:-[A-Za-z0-9]{2,8})*")
MAX_NOTES_LENGTH = 500


class ReleaseError(ValueError):
    """A release input is incomplete, unsafe, or inconsistent."""


def require_version(version):
    if not isinstance(version, str) or not VERSION_PATTERN.fullmatch(version):
        raise ReleaseError("Version must contain three integers, such as 1.2.0 (no leading zeros).")
    return version


def require_integer(value, label, maximum):
    # bool is an int subclass, but is not a valid store version number.
    if type(value) is not int or not 1 <= value <= maximum:
        raise ReleaseError(f"{label} must be an integer from 1 through {maximum}.")
    return value


def read_json(path):
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, UnicodeError, json.JSONDecodeError) as exc:
        raise ReleaseError(f"Could not read JSON from {path}: {exc}") from exc
    if not isinstance(value, dict):
        raise ReleaseError(f"{path} must contain a JSON object.")
    return value


def write_json(path, value):
    """Replace JSON atomically, without leaving a partially written review."""
    path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(
        mode="w", encoding="utf-8", dir=path.parent, prefix=".release-", delete=False
    ) as stream:
        temporary = Path(stream.name)
        try:
            json.dump(value, stream, ensure_ascii=False, indent=2)
            stream.write("\n")
            stream.flush()
            os.fsync(stream.fileno())
        except BaseException:
            temporary.unlink(missing_ok=True)
            raise
    try:
        temporary.replace(path)
    finally:
        temporary.unlink(missing_ok=True)


def contained_path(root, *parts):
    """Reject symlinks that would put release inputs or exports outside root."""
    root = root.resolve()
    path = root.joinpath(*parts)
    if not path.resolve().is_relative_to(root):
        raise ReleaseError(f"Path escapes project root: {path}")
    return path


def digest(text):
    # Hash exactly what is reviewed; whitespace edits also invalidate reviews.
    return hashlib.sha256(text.encode("utf-8")).hexdigest()


def validate_note(note, locale):
    if not isinstance(note, str) or not note.strip():
        raise ReleaseError(f"Release notes for {locale} are blank or missing.")
    if len(note) > MAX_NOTES_LENGTH:
        raise ReleaseError(
            f"Release notes for {locale} have {len(note)} characters; maximum is {MAX_NOTES_LENGTH}."
        )
    if any(ord(char) < 32 and char not in "\n\t" for char in note):
        raise ReleaseError(f"Release notes for {locale} contain unsupported control characters.")
    return note


class ReleaseManager:
    def __init__(self, root):
        self.root = Path(root).resolve()
        self.config = read_json(contained_path(self.root, "release", "config.json"))
        self.locales = self.config.get("locales")
        self.source_locale = self.config.get("source_locale")
        if not isinstance(self.config.get("app_name"), str) or not self.config["app_name"].strip():
            raise ReleaseError("release/config.json requires a nonblank app_name.")
        if not isinstance(self.locales, dict) or not self.locales:
            raise ReleaseError("release/config.json requires a nonempty locales object.")
        if not isinstance(self.source_locale, str) or self.source_locale not in self.locales:
            raise ReleaseError("source_locale must be a configured locale.")
        destinations = {"android": set(), "ios": set()}
        self.store_locales = {}
        for locale, mapping in self.locales.items():
            if not isinstance(locale, str) or not LOCALE_PATTERN.fullmatch(locale):
                raise ReleaseError(f"Invalid configured locale: {locale!r}")
            if not isinstance(mapping, dict):
                raise ReleaseError(f"Locale {locale} requires android and ios locale mappings.")
            self.store_locales[locale] = {}
            for platform in destinations:
                configured = mapping.get(platform)
                mapped = [configured] if isinstance(configured, str) else configured
                if not isinstance(mapped, list) or not mapped:
                    raise ReleaseError(f"Invalid {platform} locale mapping for {locale}.")
                for destination in mapped:
                    if not isinstance(destination, str) or not LOCALE_PATTERN.fullmatch(destination):
                        raise ReleaseError(f"Invalid {platform} locale mapping for {locale}.")
                    if destination in destinations[platform]:
                        raise ReleaseError(f"Duplicate {platform} locale mapping: {destination}")
                    destinations[platform].add(destination)
                self.store_locales[locale][platform] = mapped

    def manifest_path(self, version):
        require_version(version)
        return contained_path(self.root, "release", "releases", version + ".json")

    def load(self, version):
        manifest = read_json(self.manifest_path(version))
        if manifest.get("version") != version:
            raise ReleaseError("Manifest version must match its requested version and filename.")
        require_integer(manifest.get("android_version_code"), "Android version code", 2_100_000_000)
        require_integer(manifest.get("ios_build_number"), "iOS build number", 9999)
        notes = manifest.get("notes")
        if not isinstance(notes, dict) or set(notes) != set(self.locales):
            raise ReleaseError("Manifest notes must contain exactly the locales in release/config.json.")
        if any(not isinstance(note, str) for note in notes.values()):
            raise ReleaseError("Every notes entry must be a string.")
        reviews = manifest.get("reviews")
        if not isinstance(reviews, dict) or not set(reviews).issubset(self.locales):
            raise ReleaseError("Manifest reviews must be an object containing only configured locales.")
        return manifest

    def init(self, version, android_code, ios_build, notes=""):
        path = self.manifest_path(version)
        require_integer(android_code, "Android version code", 2_100_000_000)
        require_integer(ios_build, "iOS build number", 9999)
        if path.exists():
            raise ReleaseError(f"Release {version} already exists; edit it instead of overwriting it.")
        if not isinstance(notes, str):
            raise ReleaseError("Source notes must be text.")
        if notes:
            validate_note(notes, self.source_locale)
        manifest = {
            "version": version,
            "android_version_code": android_code,
            "ios_build_number": ios_build,
            "notes": {locale: notes if locale == self.source_locale else "" for locale in self.locales},
            "reviews": {},
            "store": {"metadata_updates": {}, "checks": {}},
        }
        write_json(path, manifest)
        return path

    def review(self, version, locale):
        if locale not in self.locales:
            raise ReleaseError(f"Unknown locale: {locale}")
        manifest = self.load(version)
        source = validate_note(manifest["notes"][self.source_locale], self.source_locale)
        translation = validate_note(manifest["notes"][locale], locale)
        manifest["reviews"][locale] = {
            "source_sha256": digest(source),
            "translation_sha256": digest(translation),
        }
        write_json(self.manifest_path(version), manifest)
        return self.manifest_path(version)

    def validate(self, version, reviewed=False):
        manifest = self.load(version)
        for locale in self.locales:
            validate_note(manifest["notes"][locale], locale)
        if reviewed:
            source_hash = digest(manifest["notes"][self.source_locale])
            for locale in self.locales:
                expected = {
                    "source_sha256": source_hash,
                    "translation_sha256": digest(manifest["notes"][locale]),
                }
                if manifest["reviews"].get(locale) != expected:
                    raise ReleaseError(
                        f"Review missing or stale for {locale}; review this locale again after checking the current source and translation."
                    )
        return manifest

    def review_all(self, version):
        manifest = self.validate(version)
        for locale, note in manifest["notes"].items():
            print(f"\n--- {locale} ---\n{note}")
        source_hash = digest(manifest["notes"][self.source_locale])
        manifest["reviews"] = {
            locale: {"source_sha256": source_hash, "translation_sha256": digest(note)}
            for locale, note in manifest["notes"].items()
        }
        write_json(self.manifest_path(version), manifest)
        return self.manifest_path(version)

    def export(self, version, reviewed=False):
        manifest = self.validate(version, reviewed=reviewed)
        output = contained_path(self.root, "build", "release", version)
        output.parent.mkdir(parents=True, exist_ok=True)
        # Stage metadata first. Re-exporting removes obsolete metadata without
        # disturbing binaries or build receipts in the same version directory.
        stage = Path(tempfile.mkdtemp(prefix=".release-export-", dir=output.parent))
        try:
            for platform in ("android", "ios"):
                source = contained_path(self.root, "release", "metadata", platform)
                destination = stage / "metadata" / platform
                if source.exists():
                    self.copy_metadata(source, destination)
                else:
                    destination.mkdir(parents=True)
            for locale, mapping in self.store_locales.items():
                for android_locale in mapping["android"]:
                    android_dir = stage / "metadata" / "android" / android_locale / "changelogs"
                    android_dir.mkdir(parents=True, exist_ok=True)
                    (android_dir / f"{manifest['android_version_code']}.txt").write_text(
                        manifest["notes"][locale], encoding="utf-8"
                    )
                for ios_locale in mapping["ios"]:
                    ios_dir = stage / "metadata" / "ios" / ios_locale
                    ios_dir.mkdir(parents=True, exist_ok=True)
                    (ios_dir / "release_notes.txt").write_text(manifest["notes"][locale], encoding="utf-8")
            write_json(stage / "manifest.json", manifest)
            write_json(stage / "testflight.json", {
                "localized_build_info": {
                    ios_locale: {"whats_new": manifest["notes"][locale]}
                    for locale, mapping in self.store_locales.items()
                    for ios_locale in mapping["ios"]
                }
            })
            if output.is_symlink():
                raise ReleaseError(f"Export directory cannot be a symlink: {output}")
            output.mkdir(exist_ok=True)
            metadata = output / "metadata"
            if metadata.is_symlink():
                raise ReleaseError(f"Export metadata cannot be a symlink: {metadata}")
            if metadata.exists():
                metadata.replace(stage / "previous-metadata")
            (stage / "metadata").replace(metadata)
            (stage / "manifest.json").replace(output / "manifest.json")
            (stage / "testflight.json").replace(output / "testflight.json")
        finally:
            if stage.exists():
                shutil.rmtree(stage)
        return output

    @staticmethod
    def copy_metadata(source, destination):
        """Copy only ordinary metadata files; reject links and special files."""
        if source.is_symlink() or not source.is_dir():
            raise ReleaseError(f"Metadata source must be an ordinary directory: {source}")
        for entry in source.rglob("*"):
            if entry.is_symlink() or not (entry.is_dir() or entry.is_file()):
                raise ReleaseError(f"Metadata must not contain symlinks or special files: {entry}")
        shutil.copytree(source, destination)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    init = commands.add_parser("init", help="Create a release with blank translations and no approvals.")
    init.add_argument("version")
    init.add_argument("--android-code", type=int, required=True)
    init.add_argument("--ios-build", type=int, required=True)
    init.add_argument("--notes-file", type=Path)
    review = commands.add_parser("review", help="Record review of one current locale against the source notes.")
    review.add_argument("version")
    review_scope = review.add_mutually_exclusive_group(required=True)
    review_scope.add_argument("--locale")
    review_scope.add_argument("--all", action="store_true", help="Print every translation and record explicit review of all locales.")
    for command in ("validate", "export"):
        action = commands.add_parser(command)
        action.add_argument("version")
        action.add_argument("--reviewed", action="store_true", help="Require current explicit reviews for all locales.")
    # Keep the original standalone notes commands usable if only release.py was
    # copied; store commands require its optional sibling release_store.py.
    try:
        from release_store import add_commands, run_command
    except ModuleNotFoundError as exc:
        if exc.name != "release_store":
            raise
    else:
        add_commands(commands)
    generation_commands = {"notes-request", "generate-notes", "import-generated-notes"}
    try:
        from release_generate import add_commands as add_generation_commands, run_command as run_generation_command
    except ModuleNotFoundError as exc:
        if exc.name not in ("release_generate", "release_store"):
            raise
    else:
        add_generation_commands(commands)
    arguments = parser.parse_args(argv)
    try:
        manager = ReleaseManager(ROOT)
        if arguments.command == "init":
            notes = arguments.notes_file.read_text(encoding="utf-8") if arguments.notes_file else ""
            path = manager.init(arguments.version, arguments.android_code, arguments.ios_build, notes)
            print(f"Created {path}. Fill in translations and review every locale.")
        elif arguments.command == "review":
            path = manager.review_all(arguments.version) if arguments.all else manager.review(arguments.version, arguments.locale)
            print(f"Recorded review for {'all locales' if arguments.all else arguments.locale} in {path}.")
        elif arguments.command == "validate":
            manager.validate(arguments.version, reviewed=arguments.reviewed)
            print(f"Validated release {arguments.version}" + (" with current reviews." if arguments.reviewed else "."))
        elif arguments.command == "export":
            path = manager.export(arguments.version, reviewed=arguments.reviewed)
            print(f"Exported release {arguments.version} to {path}.")
        elif arguments.command in generation_commands:
            return run_generation_command(manager, arguments)
        else:
            return run_command(manager, arguments)
    except (ValueError, OSError, UnicodeError) as exc:
        print(f"Release error: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
