"""Local, reviewable store deltas and translation handoffs. No network or credentials."""

import copy
import hashlib
import json
from pathlib import Path
import re
import shutil
import tempfile
from urllib.parse import urlsplit


FIELDS = {
    # Google Play Console app details: https://support.google.com/googleplay/android-developer/answer/9859152
    "android": {"title": 30, "short_description": 80, "full_description": 4000},
    "ios": {
        "name": 30, "subtitle": 30, "description": 4000, "keywords": 100,
        "promotional_text": 170, "support_url": None, "marketing_url": None, "privacy_url": None,
    },
}
# Provider locale allowlists from pinned fastlane 2.241.1:
# supply/lib/supply/languages.rb (underscores normalized to hyphens) and
# fastlane_core/lib/fastlane_core/languages.rb. Review these when upgrading the
# pinned provider integration; newly added provider locales fail closed here.
STORE_LOCALES = {
    "android": frozenset("""af am ar az-AZ be bg bn-BD ca cs-CZ da-DK de-DE el-GR en-AU en-CA en-GB
        en-IN en-SG en-US en-ZA es-419 es-ES es-US et eu-ES fa fi-FI fil fr-CA fr-FR gl-ES hi-IN
        hr hu-HU hy-AM id is-IS it-IT iw-IL ja-JP ka-GE km-KH kn-IN ko-KR ky-KG lo-LA lt lv mk-MK
        ml-IN mn-MN mr-IN ms ms-MY my-MM ne-NP nl-NL no-NO pl-PL pt-BR pt-PT rm ro ru-RU si-LK
        sk sl sr sv-SE sw ta-IN te-IN th tr-TR uk vi zh-CN zh-HK zh-TW zu""".split()),
    "ios": frozenset("""ar-SA bn-BD ca cs da de-DE el en-AU en-CA en-GB en-US es-ES es-MX fi fr-CA fr-FR
        gu-IN he hi hr hu id it ja kn-IN ko ml-IN mr-IN ms nl-NL no or-IN pa-IN pl pt-BR pt-PT ro
        ru sk sl-SI sv ta-IN te-IN th tr uk ur-PK vi zh-Hans zh-Hant""".split()),
}
CHECKS = (
    "privacy_current", "age_rating_current", "content_rights_current", "app_access_current",
    "ads_declaration_current", "export_compliance_current",
)
DEFAULTS = {
    "ios": {
        "review_details_mode": "reuse",
        "review_contact": {"first_name": None, "last_name": None, "email": None, "phone": None},
        "demo_account_required": None,
        "demo_user_env": "APP_REVIEW_DEMO_USER",
        "demo_password_env": "APP_REVIEW_DEMO_PASSWORD",
        "review_notes": None,
        "uses_non_exempt_encryption": None,
    },
    "checks": {name: None for name in CHECKS},
    "android": {"managed_publishing_confirmed": None},
}
ENV_NAME = re.compile(r"[A-Z_][A-Z0-9_]*")
PLACEHOLDER = re.compile(
    r"\b(?:TODO|TBD|CHANGEME|REPLACE[_ -]?ME|YOUR_(?:NAME|EMAIL|PHONE|URL|TEXT|DESCRIPTION|PASSWORD))\b"
    r"|\{\{[^}]+\}\}|<(?:enter|insert|replace|your)[^>]*>", re.IGNORECASE
)


class StoreError(ValueError):
    pass


def sha(value):
    return hashlib.sha256(value.encode("utf-8") if isinstance(value, str) else value).hexdigest()


def json_hash(value):
    return sha(json.dumps(value, sort_keys=True, separators=(",", ":"), ensure_ascii=False))


def read_object(path):
    try:
        if not path.is_file():
            raise OSError("Expected an ordinary file.")
        result = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, UnicodeError, json.JSONDecodeError) as exc:
        # Do not repeat arbitrary input fragments from JSON parser messages.
        raise StoreError(f"Cannot read valid JSON from {path}.") from exc
    if not isinstance(result, dict):
        raise StoreError(f"Expected a JSON object in {path}.")
    return result


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile("w", encoding="utf-8", dir=path.parent, delete=False) as stream:
        temporary = Path(stream.name)
        json.dump(value, stream, indent=2, ensure_ascii=False, sort_keys=True)
        stream.write("\n")
    try:
        temporary.replace(path)
    finally:
        temporary.unlink(missing_ok=True)


def safe_path(root, *parts):
    path = root.joinpath(*parts)
    if not path.resolve().is_relative_to(root.resolve()):
        raise StoreError(f"Path escapes project root: {path}")
    return path


def issue(issues, field, action):
    item = {"field": field, "action": action}
    if item not in issues:
        issues.append(item)


def validate_text(value, maximum, field, issues, multiline=True, url=False, byte_limit=False, minimum=1):
    if not isinstance(value, str) or not value.strip():
        issue(issues, field, "Supply nonblank text; blank values never delete existing store content.")
        return False
    valid = True
    length = len(value.encode("utf-8")) if byte_limit else len(value)
    if length < minimum:
        issue(issues, field, f"Supply at least {minimum} characters.")
        valid = False
    if maximum is not None and length > maximum:
        unit = "UTF-8 bytes" if byte_limit else "characters"
        issue(issues, field, f"Shorten this field to at most {maximum} {unit}.")
        valid = False
    if PLACEHOLDER.search(value):
        issue(issues, field, "Replace placeholder text with reviewed final copy.")
        valid = False
    allowed = "\n\t" if multiline else ""
    if any(ord(char) < 32 and char not in allowed for char in value):
        issue(issues, field, "Remove unsupported control characters or line breaks.")
        valid = False
    if url:
        try:
            parsed = urlsplit(value)
            valid_url = (
                parsed.scheme == "https" and bool(parsed.hostname)
                and parsed.username is None and parsed.password is None
                and not any(char.isspace() for char in value)
                and parsed.hostname not in ("example.com", "example.org", "example.net", "localhost")
                and not parsed.hostname.endswith(".example")
            )
            parsed.port  # Reject a malformed explicit port.
        except ValueError:
            valid_url = False
        if not valid_url:
            issue(issues, field, "Use a real HTTPS URL with a hostname and no embedded credentials (pipeline policy).")
            valid = False
    return valid


class StorePlanner:
    def __init__(self, manager):
        self.manager = manager
        self.root = manager.root

    def _defaults(self, issues):
        path = safe_path(self.root, "release", "store-defaults.json")
        supplied = read_object(path) if path.exists() else {}

        def merge(template, raw, prefix):
            if not isinstance(raw, dict):
                issue(issues, prefix, "Use an object with the documented defaults fields.")
                raw = {}
            result = copy.deepcopy(template)
            for key in raw:
                if key not in template:
                    issue(issues, f"{prefix}.{key}", "Remove this unsupported field; credentials belong only in external environment variables.")
            for key, fallback in template.items():
                if key not in raw:
                    continue
                value = raw[key]
                field = f"{prefix}.{key}"
                if isinstance(fallback, dict):
                    result[key] = merge(fallback, value, field)
                    continue
                if key.endswith("_env"):
                    if not isinstance(value, str) or not ENV_NAME.fullmatch(value):
                        issue(issues, field, "Supply an environment-variable name, never its value.")
                        result[key] = None
                    else:
                        result[key] = value
                elif key == "review_details_mode":
                    if value not in ("reuse", "replace"):
                        issue(issues, field, "Choose reuse or replace.")
                        result[key] = None
                    else:
                        result[key] = value
                elif key in CHECKS or key in ("demo_account_required", "uses_non_exempt_encryption", "managed_publishing_confirmed"):
                    if value is not None and type(value) is not bool:
                        issue(issues, field, "Use JSON true, false, or null; never a guessed answer.")
                        result[key] = None
                    else:
                        result[key] = value
                elif value is not None and not isinstance(value, str):
                    issue(issues, field, "Use text or null.")
                    result[key] = None
                else:
                    result[key] = value
            return result

        defaults = merge(DEFAULTS, supplied, "release/store-defaults.json")
        # Contact values are intentionally user-supplied data; environment values
        # are never read, expanded, serialized, or included in the review hash.
        for name, value in defaults["ios"]["review_contact"].items():
            if value is not None:
                validate_text(value, 255, f"release/store-defaults.json.ios.review_contact.{name}", issues, multiline=False)
        if defaults["ios"]["review_notes"] is not None:
            validate_text(defaults["ios"]["review_notes"], 4000, "release/store-defaults.json.ios.review_notes", issues, byte_limit=True)
        return defaults

    def _notes(self, manifest, issues, review_issues, prefix):
        notes = manifest.get("notes")
        if not isinstance(notes, dict):
            issue(issues, prefix + ".notes", "Supply a notes object for every configured locale.")
            notes = {}
        for locale in notes:
            if locale not in self.manager.locales:
                issue(issues, prefix + ".notes." + locale, "Remove this unconfigured locale or explicitly update release/config.json.")
        clean = {}
        source = notes.get(self.manager.source_locale)
        reviews = manifest.get("reviews", {})
        if not isinstance(reviews, dict):
            reviews = {}
        for locale in self.manager.locales:
            text = notes.get(locale)
            validate_text(text, 500, prefix + ".notes." + locale, issues)
            clean[locale] = text if isinstance(text, str) else ""
            expected = {"source_sha256": sha(source or ""), "translation_sha256": sha(clean[locale])} if isinstance(source, str) else None
            if expected is None or reviews.get(locale) != expected:
                issue(review_issues, prefix + ".reviews." + locale,
                      f"Read the current source and translation, then run review {manifest.get('version', 'VERSION')} --locale {locale}.")
        return clean

    def _metadata(self, selection, issues, prefix):
        result = {"android": {}, "ios": {}}
        file_hashes = {}
        if not isinstance(selection, dict):
            issue(issues, prefix, "Use an object containing android and/or ios locale field lists.")
            return result, file_hashes
        for platform, locales in selection.items():
            if platform not in FIELDS:
                issue(issues, f"{prefix}.{platform}", "Only android and ios metadata selections are supported.")
                continue
            allowed_locales = {item for mapping in self.manager.store_locales.values() for item in mapping[platform]} & STORE_LOCALES[platform]
            if not isinstance(locales, dict):
                issue(issues, f"{prefix}.{platform}", "Map store locale names to lists of selected field names.")
                continue
            for locale, fields in locales.items():
                field_prefix = f"{prefix}.{platform}.{locale}"
                if locale not in allowed_locales:
                    issue(issues, field_prefix, "Select a store locale mapped in release/config.json.")
                    continue
                if not isinstance(fields, list) or any(not isinstance(field, str) for field in fields):
                    issue(issues, field_prefix, "Use a list of supported field names.")
                    continue
                if len(fields) != len(set(fields)):
                    issue(issues, field_prefix, "Remove duplicate selected fields.")
                for field in fields:
                    if field not in FIELDS[platform]:
                        issue(issues, f"{field_prefix}.{field}", "Select a supported text field; deletes, screenshots, pricing, and credentials are excluded.")
                        continue
                    relative = f"release/metadata/{platform}/{locale}/{field}.txt"
                    try:
                        path = safe_path(self.root, relative)
                        if path.is_symlink() or not path.is_file():
                            raise StoreError("Metadata must be an ordinary file.")
                        raw = path.read_bytes()
                        value = raw.decode("utf-8").rstrip("\r\n")
                        file_hashes[relative] = sha(raw)
                    except (OSError, UnicodeError, StoreError):
                        issue(issues, relative, "Create an ordinary UTF-8 metadata file at this path.")
                        continue
                    valid = validate_text(
                        value, FIELDS[platform][field], relative, issues,
                        multiline=field in ("description", "full_description", "promotional_text"),
                        url=field.endswith("_url"),
                        byte_limit=platform == "ios" and field == "keywords",
                        minimum=2 if platform == "ios" and field == "name" else 1,
                    )
                    if valid:
                        result[platform].setdefault(locale, {})[field] = value
        return result, file_hashes

    def _submission(self, defaults, platforms):
        missing, remote = [], []
        required_checks = ["privacy_current", "age_rating_current", "content_rights_current", "app_access_current"]
        if "android" in platforms:
            required_checks.append("ads_declaration_current")
            if defaults["android"]["managed_publishing_confirmed"] is not True:
                issue(missing, "release/store-defaults.json.android.managed_publishing_confirmed",
                      "Confirm Managed publishing is enabled in Play Console, then set true; production review must not auto-publish.")
            remote.append({"field": "google_play.managed_publishing", "action": "Verify current Managed publishing and track state in Play Console before submission; local confirmation cannot prove live state."})
        if "ios" in platforms:
            required_checks.append("export_compliance_current")
            ios = defaults["ios"]
            if ios["uses_non_exempt_encryption"] is None:
                remote.append({"field": "app_store_connect.selected_build.export_compliance", "action": "Reuse the selected build's existing export-compliance answer. If it has none, stop and obtain the app's actual answer before submission; never infer one."})
            if ios["review_details_mode"] == "reuse":
                remote.append({"field": "app_store_connect.app_review_details", "action": "Reuse existing contact and login details; verify required contact fields, demo-account requirements, and account availability remotely before submission."})
            elif ios["review_details_mode"] == "replace":
                for key, value in ios["review_contact"].items():
                    field = f"release/store-defaults.json.ios.review_contact.{key}"
                    if not isinstance(value, str) or not value.strip():
                        issue(missing, field, "Supply the App Review contact before replacing existing review details.")
                    elif key == "email" and not re.fullmatch(r"[^\s@]+@[^\s@]+\.[^\s@]+", value):
                        issue(missing, field, "Supply a valid review-contact email address.")
                    elif key == "phone" and (not re.fullmatch(r"\+[1-9][0-9 ()-]+", value) or not 7 <= len(re.sub(r"\D", "", value)) <= 15):
                        issue(missing, field, "Supply an international phone number beginning with + and a country code.")
                if type(ios["demo_account_required"]) is not bool:
                    issue(missing, "release/store-defaults.json.ios.demo_account_required", "Explicitly state whether App Review needs a demo account.")
                elif ios["demo_account_required"] is True:
                    for key in ("demo_user_env", "demo_password_env"):
                        if not isinstance(ios[key], str) or not ENV_NAME.fullmatch(ios[key]):
                            issue(missing, "release/store-defaults.json.ios." + key, "Supply the name of the external environment variable containing the review credential.")
                    remote.append({"field": "app_store_connect.demo_account", "action": "At submission, read the named credential environment variables without logging them and verify the demo account works."})
            remote.append({"field": "app_store_connect.submission_state", "action": "Verify the selected build, version, review state, and any additional Apple-required declarations remotely before submission."})
        for name in required_checks:
            if defaults["checks"][name] is not True:
                issue(missing, "release/store-defaults.json.checks." + name,
                      "Review the current store declaration and explicitly confirm true in defaults or this release's store.checks override.")
        return missing, remote

    def plan(self, version, platform="both", stage="prepare"):
        if platform not in ("both", "android", "ios") or stage not in ("prepare", "submit", "publish"):
            raise StoreError("Choose a supported platform and stage.")
        path = self.manager.manifest_path(version)
        manifest = read_object(path)
        prefix = f"release/releases/{version}.json"
        issues, review_issues = [], []
        for locale, mapping in self.manager.store_locales.items():
            for store_platform, destinations in mapping.items():
                if any(destination not in STORE_LOCALES[store_platform] for destination in destinations):
                    issue(issues, f"release/config.json.locales.{locale}.{store_platform}",
                          "Map to a provider-supported store locale; check the pinned fastlane locale allowlist before adding a new locale.")
        if manifest.get("version") != version:
            issue(issues, prefix + ".version", "Match the requested version and filename.")
        for key, maximum in (("android_version_code", 2_100_000_000), ("ios_build_number", 9999)):
            value = manifest.get(key)
            if type(value) is not int or not 1 <= value <= maximum:
                issue(issues, prefix + "." + key, f"Supply an unused positive integer no greater than {maximum}.")
        notes = self._notes(manifest, issues, review_issues, prefix)
        defaults = self._defaults(issues)
        store = manifest.get("store", {})
        if not isinstance(store, dict):
            issue(issues, prefix + ".store", "Use an object containing metadata_updates and optional checks.")
            store = {}
        for key in store:
            if key not in ("metadata_updates", "checks"):
                issue(issues, prefix + ".store." + key, "Remove this unsupported store-plan field.")
        checks = store.get("checks", {})
        if not isinstance(checks, dict):
            issue(issues, prefix + ".store.checks", "Use a checklist object with documented boolean or null values.")
            checks = {}
        for key, value in checks.items():
            if key not in CHECKS or (value is not None and type(value) is not bool):
                issue(issues, prefix + ".store.checks." + key, "Use a supported check with JSON true, false, or null.")
            else:
                defaults["checks"][key] = value
        selection = store.get("metadata_updates", {})
        updates, file_hashes = self._metadata(selection, issues, prefix + ".store.metadata_updates")
        inputs = {
            "version": version, "android_version_code": manifest.get("android_version_code"),
            "ios_build_number": manifest.get("ios_build_number"), "notes": notes,
            "config": self.manager.config, "selection": selection,
            "selected_file_sha256": file_hashes, "defaults": defaults,
        }
        input_hash = json_hash(inputs)
        reviewed = manifest.get("store_review") == {"input_sha256": input_hash}
        if not reviewed:
            issue(review_issues, prefix + ".store_review",
                  f"Read review.md and run review-store {version}; re-review whenever selected input changes.")
        platforms = ("android", "ios") if platform == "both" else (platform,)
        submission_missing, remote = self._submission(defaults, platforms)
        for item in submission_missing:
            name = item["field"].removeprefix("release/store-defaults.json.checks.")
            if name in checks:
                item["field"] = prefix + ".store.checks." + name
        local_missing = issues + review_issues
        if stage in ("submit", "publish"):
            local_missing += submission_missing
        if stage == "publish" and "android" in platforms:
            issue(local_missing, "google_play.production_publish", "Publish the approved release manually in Play Console; this workflow never publishes Android production.")
        localized = {"android": {}, "ios": {}}
        for locale, mapping in self.manager.store_locales.items():
            for target_platform in platforms:
                for target_locale in mapping[target_platform]:
                    localized[target_platform][target_locale] = notes[locale]
        plan = {
            "schema_version": 1, "app_name": self.manager.config["app_name"], "version": version,
            "android_version_code": manifest.get("android_version_code"), "ios_build_number": manifest.get("ios_build_number"),
            "platform": platform, "stage": stage, "notes": notes, "localized_notes": localized,
            "metadata_updates": {key: updates[key] if key in platforms else {} for key in updates},
            "defaults": defaults, "input_sha256": input_hash,
            "missing": local_missing, "reviewed": reviewed,
            "submission_missing": submission_missing if stage == "prepare" else [],
            "remote_checks": remote,
        }
        return plan, issues, review_issues

    def review(self, version):
        plan, issues, note_review_issues = self.plan(version)
        blockers = issues + [item for item in note_review_issues if not item["field"].endswith(".store_review")]
        if blockers:
            raise StoreError("Cannot review store inputs:\n" + "\n".join(f"- {item['field']}: {item['action']}" for item in blockers))
        manifest = self.manager.load(version)
        manifest["store_review"] = {"input_sha256": plan["input_sha256"]}
        write_json(self.manager.manifest_path(version), manifest)
        return plan["input_sha256"]

    def prepare(self, version, platform="both", stage="prepare"):
        plan, _, _ = self.plan(version, platform, stage)
        output = safe_path(self.root, "build", "release", version)
        if output.is_symlink():
            raise StoreError("Release output directory cannot be a symlink.")
        output.mkdir(parents=True, exist_ok=True)
        delta = output / "store-metadata"
        if delta.is_symlink():
            raise StoreError("Store metadata output cannot be a symlink.")
        stage_dir = Path(tempfile.mkdtemp(prefix=".store-delta-", dir=output))
        try:
            for target_platform, locales in plan["metadata_updates"].items():
                for locale, fields in locales.items():
                    destination = stage_dir / target_platform / locale
                    destination.mkdir(parents=True, exist_ok=True)
                    for field, text in fields.items():
                        (destination / f"{field}.txt").write_text(text, encoding="utf-8")
            for target_platform, locales in plan["localized_notes"].items():
                for locale, text in locales.items():
                    destination = stage_dir / target_platform / locale
                    destination.mkdir(parents=True, exist_ok=True)
                    if target_platform == "android":
                        (destination / "changelogs").mkdir(exist_ok=True)
                        # Invalid build numbers are reported, never used as paths.
                        code = plan["android_version_code"]
                        if type(code) is int and 1 <= code <= 2_100_000_000:
                            (destination / "changelogs" / f"{code}.txt").write_text(text, encoding="utf-8")
                    else:
                        (destination / "release_notes.txt").write_text(text, encoding="utf-8")
            if delta.exists():
                shutil.rmtree(delta)
            stage_dir.replace(delta)
        finally:
            if stage_dir.exists():
                shutil.rmtree(stage_dir)
        write_json(output / "store-plan.json", plan)
        review_path = safe_path(self.root, "build", "release", version, "review.md")
        if review_path.is_symlink():
            raise StoreError("Store review output cannot be a symlink.")
        review_path.write_text(self.markdown(plan), encoding="utf-8")
        return plan, output

    @staticmethod
    def markdown(plan):
        lines = [f"# {plan['app_name']} {plan['version']} store review", "",
                 f"Platform: {plan['platform']}; stage: {plan['stage']}.",
                 f"Input SHA-256: `{plan['input_sha256']}`.",
                 f"Store inputs reviewed: {'yes' if plan['reviewed'] else 'no'}. No store has been contacted.", "",
                 "## Release notes", ""]
        for locale, text in plan["notes"].items():
            lines += [f"### {locale}", "", text or "(blank)", ""]
        lines += ["## Explicit listing changes", ""]
        changes = 0
        for platform, locales in plan["metadata_updates"].items():
            for locale, fields in locales.items():
                for field, text in fields.items():
                    changes += 1
                    lines += [f"### {platform} / {locale} / {field}", "", text, ""]
        if not changes:
            lines += ["None. Existing listing fields, screenshots, and pricing remain unchanged.", ""]
        lines += ["## Store defaults", "", "```json", json.dumps(plan["defaults"], ensure_ascii=False, sort_keys=True, indent=2), "```", "",
                  "Credential environment-variable names are references only. Values are never read by this tool.", ""]
        for title, key in (("Current blockers", "missing"), ("Required before submission", "submission_missing"), ("Checks deferred to the connected store", "remote_checks")):
            lines += ["## " + title, ""]
            lines += [f"- `{item['field']}`: {item['action']}" for item in plan[key]] or ["None."]
            lines += [""]
        return "\n".join(lines)


def export_translations(manager, version, destination):
    manifest = manager.load(version)
    source = manifest["notes"][manager.source_locale]
    issues = []
    validate_text(source, 500, "notes." + manager.source_locale, issues)
    if issues:
        raise StoreError("Write valid source notes before exporting a translation handoff.")
    handoff = {
        "schema_version": 1, "version": version, "source_locale": manager.source_locale,
        "source_sha256": sha(source),
        "target_locales": [locale for locale in manager.locales if locale != manager.source_locale],
        "notes": manifest["notes"],
    }
    destination = Path(destination)
    if destination.exists():
        raise StoreError(f"Translation handoff already exists: {destination}")
    write_json(destination, handoff)
    return destination


def import_translations(manager, version, source_path):
    manifest = manager.load(version)
    handoff = read_object(Path(source_path))
    targets = apply_translation_drafts(manager, manifest, handoff)
    write_json(manager.manifest_path(version), manifest)
    return targets


def apply_translation_drafts(manager, manifest, handoff):
    """Validate all translations before modifying an in-memory release candidate."""
    version = manifest["version"]
    source = manifest["notes"][manager.source_locale]
    if (handoff.get("schema_version") != 1 or handoff.get("version") != version
            or handoff.get("source_locale") != manager.source_locale or handoff.get("source_sha256") != sha(source)):
        raise StoreError("Translation handoff is stale or belongs to another release/source locale; export a fresh handoff.")
    notes, targets = handoff.get("notes"), handoff.get("target_locales")
    if not isinstance(notes, dict) or not isinstance(targets, list) or any(not isinstance(locale, str) for locale in targets):
        raise StoreError("Translation handoff requires notes and target_locales.")
    if len(targets) != len(set(targets)) or any(locale == manager.source_locale or locale not in manager.locales for locale in targets):
        raise StoreError("Translation handoff contains an unknown, duplicate, or source target locale.")
    if set(notes) - {manager.source_locale} != set(targets):
        raise StoreError("Translation notes must match exactly the listed target locales.")
    if manager.source_locale in notes and notes[manager.source_locale] != source:
        raise StoreError("Translation import cannot change source notes.")
    issues = []
    for locale in targets:
        validate_text(notes[locale], 500, "notes." + locale, issues)
    if issues:
        raise StoreError("Invalid translation drafts:\n" + "\n".join(f"- {item['field']}: {item['action']}" for item in issues))
    for locale in targets:
        manifest["notes"][locale] = notes[locale]
        manifest["reviews"].pop(locale, None)
    manifest.pop("store_review", None)
    return targets


def import_metadata(manager, snapshot_path):
    snapshot = read_object(Path(snapshot_path))
    if snapshot.get("schema_version") != 1 or set(snapshot) != {"schema_version", "metadata"} or not isinstance(snapshot.get("metadata"), dict):
        raise StoreError("Snapshot must contain schema_version: 1 and metadata: {platform: {locale: {field: text}}}.")
    planned, issues = [], []
    for platform, locales in snapshot["metadata"].items():
        if platform not in FIELDS or not isinstance(locales, dict):
            issue(issues, "metadata." + platform, "Use android or ios with a locale object.")
            continue
        allowed = {item for mapping in manager.store_locales.values() for item in mapping[platform]} & STORE_LOCALES[platform]
        for locale, fields in locales.items():
            if locale not in allowed or not isinstance(fields, dict):
                issue(issues, f"metadata.{platform}.{locale}", "Use a configured store locale with a fields object.")
                continue
            for field, text in fields.items():
                reference = f"metadata.{platform}.{locale}.{field}"
                if field not in FIELDS[platform]:
                    issue(issues, reference, "Use a supported listing text field.")
                    continue
                valid = validate_text(text, FIELDS[platform][field], reference, issues,
                                      multiline=field in ("description", "full_description", "promotional_text"), url=field.endswith("_url"),
                                      byte_limit=platform == "ios" and field == "keywords",
                                      minimum=2 if platform == "ios" and field == "name" else 1)
                try:
                    path = safe_path(manager.root, "release", "metadata", platform, locale, field + ".txt")
                    if path.is_symlink() or (path.exists() and not path.is_file()):
                        raise StoreError("Metadata must be an ordinary file.")
                    if path.exists() and path.read_text(encoding="utf-8").rstrip("\r\n") != text:
                        issue(issues, str(path.relative_to(manager.root)), "Existing copy differs; compare and edit it explicitly. Import never overwrites differing text.")
                    elif valid and not path.exists():
                        planned.append((path, text))
                except (StoreError, OSError, UnicodeError):
                    issue(issues, reference, "Use a readable ordinary metadata file inside the project.")
    if issues:
        raise StoreError("Metadata import rejected before writing:\n" + "\n".join(f"- {item['field']}: {item['action']}" for item in issues))
    for path, text in planned:
        path.parent.mkdir(parents=True, exist_ok=True)
        with path.open("x", encoding="utf-8") as stream:
            stream.write(text)
    return len(planned)


def add_commands(commands):
    prepare = commands.add_parser("prepare", help="Preview explicit store changes locally; --reviewed fails on blockers.")
    prepare.add_argument("version")
    prepare.add_argument("--platform", choices=("both", "android", "ios"), default="both")
    prepare.add_argument("--stage", choices=("prepare", "submit", "publish"), default="prepare")
    prepare.add_argument("--reviewed", action="store_true")
    review = commands.add_parser("review-store", help="Record explicit review of notes, selected fields, config, and store defaults.")
    review.add_argument("version")
    export = commands.add_parser("export-translations", help="Write a local JSON handoff; no translation service is called.")
    export.add_argument("version")
    export.add_argument("--output", type=Path, required=True)
    import_drafts = commands.add_parser("import-translations", help="Import drafts; source stays unchanged and imported reviews are cleared.")
    import_drafts.add_argument("version")
    import_drafts.add_argument("file", type=Path)
    metadata = commands.add_parser("import-metadata", help="Import a normalized snapshot without overwriting different existing copy.")
    metadata.add_argument("file", type=Path)


def run_command(manager, arguments):
    if arguments.command == "prepare":
        plan, output = StorePlanner(manager).prepare(arguments.version, arguments.platform, arguments.stage)
        print(f"Prepared {output / 'review.md'} and store-plan.json. No store was contacted.")
        for item in plan["missing"]:
            print(f"MISSING {item['field']}: {item['action']}")
        for item in plan["submission_missing"]:
            print(f"BEFORE SUBMISSION {item['field']}: {item['action']}")
        for item in plan["remote_checks"]:
            print(f"REMOTE CHECK {item['field']}: {item['action']}")
        if not arguments.reviewed:
            print("Preview mode returns success even with blockers; use --reviewed for the release gate.")
        return 1 if arguments.reviewed and plan["missing"] else 0
    if arguments.command == "review-store":
        value = StorePlanner(manager).review(arguments.version)
        print(f"Recorded store input review {value}. Submission declarations and live store state remain separate checks.")
    elif arguments.command == "export-translations":
        print(f"Exported {export_translations(manager, arguments.version, arguments.output)}. Fill target notes, then import and review.")
    elif arguments.command == "import-translations":
        locales = import_translations(manager, arguments.version, arguments.file)
        print(f"Imported drafts for {', '.join(locales)}. Their reviews and the store review were cleared.")
    elif arguments.command == "import-metadata":
        count = import_metadata(manager, arguments.file)
        print(f"Imported {count} new metadata fields. No release selected them for upload.")
    else:
        raise StoreError("Unsupported store command.")
    return 0
