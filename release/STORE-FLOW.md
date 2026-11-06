# Routine store releases

Use the same release manifest for the build, test upload, and store preparation.
Descriptions already in the stores remain in place. Change them only when the
product or listing changes. The source notes and reviewed translations feed
Google Play release notes, TestFlight “What to Test”, and App Store “What's New”.

This is installed code, not an activated connection. No store calls, credential
configuration, pushes, uploads, review submissions, or public releases were made
while implementing it. Summary and translation drafting can use the current agent or an explicitly chosen existing generator through [GENERATED-NOTES.md](GENERATED-NOTES.md). No provider is configured by default.

For automatic drafting from selected committed changes, start with the generated-notes guide above. It feeds the same release manifest and review steps below.

## What you do each release

1. Choose unused build numbers from the stores and write one source note:

   ```sh
   python3 scripts/release.py init 2.0.0 --android-code 200 --ios-build 200 --notes-file /path/to/notes.txt
   python3 scripts/release.py export-translations 2.0.0 --output /tmp/release-translations.json
   ```

   These numbers are illustrative. The JSON handoff contains the source and
   blank target translations. A human translator or separately authorized tool
   fills it; importing it never approves its wording. No AI API is contacted.
   A reusable instruction for your existing translation tool is: “Fill only the
   target `notes` values in this JSON. Preserve identifiers, source text, hashes,
   app names, and URLs. Use the named regional locales, keep each note under
   500 characters, and return the same JSON structure. Flag wording you cannot
   confidently translate.” Read the result before approving it.

   ```sh
   python3 scripts/release.py import-translations 2.0.0 /tmp/release-translations.json
   python3 scripts/release.py prepare 2.0.0
   ```

2. Read `build/release/2.0.0/review.md`. Check all translations and any selected
   listing changes. After that review, record the acknowledgement:

   ```sh
   python3 scripts/release.py review 2.0.0 --all
   python3 scripts/release.py review-store 2.0.0
   python3 scripts/release.py prepare 2.0.0 --reviewed
   ```

   Notes, selected metadata, locale mappings, versions, or defaults changing
   invalidate the relevant review. These hashes detect edits; they are not an
   identity or authorization system. The preview names each missing field and
   its smallest next step. It makes no store calls.

3. Review and publish the intended source/manifest changes to GitHub only when
   authorized. The manual workflows must be on the default branch to appear in
   Actions. Select the intended release branch/ref. Run **Build and optionally
   upload test release**, choose the version, `both`, and the upload option.
   Test the internal/ TestFlight build before preparing production.

4. Run **Prepare, submit, or release a store version** for each platform, with the same
   version, action `prepare`, and the confirmation requested by the workflow.
   Apple saves the selected store text, attaches the exact processed build, and
   sets manual release. Google validates a temporary edit and discards it; its
   preview is not a saved console draft. See the reason below.

5. When the preview and tests are approved, choose action `submit` for each
   platform. This is a separate consequential operation. Apple sends the exact
   selected version for review with manual release. Google applies the selected
   text and new production release in one commit, which can send it for review.
   Google requires a fresh confirmation that Managed publishing is on and no
   unrelated changes are pending. It may still ask for a specific console
   action; the workflow reports that error and does not retry with looser rules.

6. After approval, authorize public release separately. Apple: action `publish`
   for the exact version. Google: Play Console → Publishing overview → Changes
   ready to publish → Publish changes. Read the displayed changes before clicking.

Routine source input is the version/build numbers and release note, followed by
translation review. Existing descriptions, screenshots, contact details, and
compliance settings are reused. You do not retype them per release. Store-side
requirements or changed product behavior may still require new answers.

## Stages and their effects

| Action | Android | iOS |
| --- | --- | --- |
| Local `prepare` | Files and checks only | Files and checks only |
| `build_test` / `upload_test` | AAB / internal track | IPA / TestFlight internal testing |
| Store `prepare` | Validate temporary edit, then discard | Save selected text, attach exact build, set MANUAL |
| Store `submit` | Commit selected text and new production candidate; Managed publishing must be on | Submit exact version for App Review; release remains MANUAL |
| Store `publish` | Console handoff; no API publication | Request release of exact approved version |

The Android production path requires an already published production app;
Managed publishing is unavailable for the first publication. Complete that
one-time release through Play Console with explicit authorization. The Apple
adapter also requires an existing released version so it can safely reuse its
listing; first-ever App Store setup/publication is a separate authorized step.
Apple's first-version rules differ, including the absence of What's New.

An API success is not proof the app is publicly available. For any timeout after
an upload, commit, submission, or release request, inspect the store state before
retrying. The code does not automatically retry these consequential mutations.

## Descriptions and other reusable text

A release begins with `store.metadata_updates: {}`. That means **keep existing
listing text**; only the reviewed release notes are selected. Merely placing a
file under `release/metadata` does not select it for upload. This protects old
drafts and incomplete local snapshots.

To intentionally change a field, edit the persistent text file and select its
store locale and filename stem in the release manifest:

```json
"store": {
  "metadata_updates": {
    "android": {"en-US": ["short_description"]},
    "ios": {"en-US": ["promotional_text"]}
  },
  "checks": {}
}
```

Values then come from `release/metadata/android/en-US/short_description.txt`
and `release/metadata/ios/en-US/promotional_text.txt`. Read the generated preview
before `review-store`. Omitted fields and locales are preserved. App Store
name, subtitle, and privacy URL belong to app information shared across its
platforms; they are not iOS-version-only fields. No screenshots,
prices, countries, phased-release settings, ratings, or privacy questionnaires
are broadly synchronized or deleted.

For an approved existing text snapshot, the local importer accepts a JSON object
with `schema_version: 1` and a `metadata` object containing `android` and/or
`ios`, store locales, and supported field/value pairs. For example:

```json
{"schema_version": 1, "metadata": {"android": {"en-US": {"title": "Approved app name"}}}}
```

The importer validates the entire batch and refuses to replace differing local files:

```sh
python3 scripts/release.py import-metadata /path/to/approved-store-text.json
```

ChorePilot's migrated local descriptions are drafts until reviewed. DreamNorth
has no confirmed local description snapshot. Neither blocks a notes-only release
when complete existing store text is available. No text is invented for a missing
required live field. A new locale or first-ever store listing may need additional
screenshots, contact information, and initial store setup.

## One-time factual defaults

`release/store-defaults.json` keeps verified declarations and review details
reusable. Null means unknown, not false. Fill only facts confirmed by the app
owner. Per-release `store.checks` can override a default when behavior changes.

| Field | Minimal action before submission |
| --- | --- |
| `checks.privacy_current` | Confirm current privacy disclosures include the app and SDK behavior. |
| `checks.age_rating_current` | Confirm current age-rating questionnaire is accurate. |
| `checks.content_rights_current` | Confirm current rights declarations are accurate. |
| `checks.app_access_current` | Confirm reviewer/tester access works with current features. |
| `checks.ads_declaration_current` | Android: confirm the current ads declaration. |
| `checks.export_compliance_current` | iOS: confirm the current export-compliance determination. |
| `android.managed_publishing_confirmed` | Turn on/verify Managed publishing in Play Console; set true only after checking it. |
| `ios.review_details_mode` | Default `reuse`: keep and verify existing App Review contacts and login requirements. |
| `ios.uses_non_exempt_encryption` | Supply the verified answer to Apple's **non-exempt encryption** question only if the selected build has none. Do not infer it from use of HTTPS or OS cryptography. |

If existing Apple review details are complete, leave `review_details_mode` as
`reuse`. If a replacement is needed, use `replace` and provide `review_contact`
(first name, last name, email, international phone), an explicit
`demo_account_required`, and any approved review notes. Demo login fields refer
to environment-variable **names**, default `APP_REVIEW_DEMO_USER` and
`APP_REVIEW_DEMO_PASSWORD`. Never put the actual reviewer credentials in JSON,
metadata files, artifacts, or Git. The workflow supports those two default
environment names; custom names require a separately reviewed workflow change.

Persistent checks reduce repetitive answers but do not establish store readiness.
The live calls verify the exact app/version/build, current editable/review state,
and required existing metadata. Store validation may reveal account agreements,
new declarations, missing artwork, or other requirements that local files cannot
verify. Such failures remain specific blockers; nothing fills them with guesses.

## Activation requiring separate approval

The build setup remains in [SETUP.md](SETUP.md). Store actions have a separate
`RELEASE_STORE_PIPELINE_ENABLED=true` switch and separate GitHub environments:
`release-store`, `release-submission`, and `release-production`. Configure allowed
branches and required reviewers where supported **before** enabling the switch.
Naming an environment in YAML alone does not create an approval requirement.

After explicit action-time approval, connect existing app-scoped store credentials
with the minimum required rights in the appropriate environments. Android
submission needs production-release rights beyond the existing internal-testing
scope. Apple needs the rights to edit app metadata, submit versions, and release;
verify the actual key role and app access. Do not broaden a test credential
silently. The store-only workflow needs no keystore, provisioning profile, P12,
Firebase configuration, or signing export. It operates on already uploaded builds.

Store secrets are `PLAY_SERVICE_ACCOUNT_JSON_BASE64` for Android and
`ASC_KEY_ID`, `ASC_PRIVATE_KEY_BASE64`, and `ASC_ISSUER_ID` for an Apple team key.
For an authorized individual Apple key, leave `ASC_ISSUER_ID` empty. The store
lane supports this; the existing build/test-upload lane still requires a team
issuer and must be reviewed separately before switching its key type. Reviewer credentials, when needed for explicit replacement, are
separately approved secrets. No credentials have been created or configured.

The workflow requires the literal confirmation `<platform>:<action>:<version>`,
for example `ios:submit:2.0.0`. Google submission also requires the literal
`managed-publishing-on-no-unrelated-changes`. This is a human attestation, not an
API check of Managed publishing. The API cannot inspect all unrelated pending
console changes. Do not dispatch submission while other store work is pending.

## Verified API boundaries

Google's commit endpoint does not document a universal “save metadata but never
submit it for review” switch. `changesNotSentForReview` is described for rejection
handling. The new commit behavior can cancel/resubmit an existing review unless
`ERROR_IF_IN_REVIEW` is explicitly requested. Therefore Google preparation uses
an edit that is validated and discarded; only the separately authorized submit
stage commits. Managed publishing separates review from availability for the new
production candidate, but does not cover every type of store change. We do not
edit an already-live release's notes or complete an existing staged rollout.
Creating an edit can invalidate another open edit for the same Google user.
Both release workflows share a concurrency group; also avoid simultaneous
Console/API editing with that account. The prepare call is an ephemeral API
mutation, with no committed listing/release changes. If cleanup fails, inspect
the reported edit rather than assuming it was discarded.
[Google commit API](https://developers.google.com/android-publisher/api-ref/rest/v3/edits/commit),
[Managed publishing and exceptions](https://support.google.com/googleplay/android-developer/answer/9859654?hl=en).

Apple preparation uses focused API patches to preserve unrelated settings. It
explicitly selects a processed, valid build and verifies manual release. Existing
scheduled-release dates must be reconciled in App Store Connect before this
manual flow can proceed. Missing listing fields may be reused from the same
locale of the already released version; blank or unreviewed translations are
never invented. A
separate release request requires Pending Developer Release.
[Apple version fields](https://developer.apple.com/help/app-store-connect/reference/app-information/platform-version-information/),
[Apple manual release API](https://developer.apple.com/documentation/appstoreconnectapi/post-v1-appstoreversionreleaserequests).

Some compliance fields have APIs; for example, Google exposes a Data Safety CSV
endpoint and Apple exposes age-rating declarations. This implementation preserves
those answers rather than inventing them or performing a bulk replacement.
Privacy-policy URLs are not privacy disclosures. User-approved changes to these
answers require a separate, reviewed integration or console update.
[Google Data Safety API](https://developers.google.com/android-publisher/api-ref/rest/v3/applications/dataSafety),
[Apple age rating](https://developer.apple.com/documentation/appstoreconnectapi/age-ratings).
