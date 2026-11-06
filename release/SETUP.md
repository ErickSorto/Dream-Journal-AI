# App release pipeline

This repository uses GitHub Actions and fastlane. Prepare one release manifest,
review each translation, then run **Build and optionally upload test release**.
It builds signed Android AABs and iOS IPAs in parallel. Uploads are optional and
limited to Google Play **internal** and **TestFlight**. The separate
[store preparation flow](STORE-FLOW.md) reuses that same manifest to fill supported
store fields, select the tested build, submit for review, and later request release.
Those stages have independent controls and have not been activated.

The pipeline is installed as source configuration only. No GitHub secrets,
environment protection, store permissions, certificates, API keys, cloud builds,
or store uploads were configured or performed during setup. It remains disabled
until the repository variable `RELEASE_PIPELINE_ENABLED` is explicitly set to
`true`. GitHub-hosted builds can consume billed minutes, especially macOS;
confirm the account's budget before enabling or running them.

## Each release

To have the current agent generate the change summary and all translations, use
[GENERATED-NOTES.md](GENERATED-NOTES.md). It reuses this same release manifest and
review process; no additional service or API key is required for session-agent drafting.

1. Choose a marketing version and **unused** store build numbers after checking
   Play Console and App Store Connect. Local source numbers are not proof of the
   highest uploaded build. Use the same marketing version on both platforms, and
   independent Android/iOS build numbers.
2. Run from the repository root, replacing the illustrative numbers:

   ```sh
   python3 scripts/release.py init 2.0.0 --android-code 200 --ios-build 200 --notes-file /path/to/english-notes.txt
   ```

3. Edit `release/releases/2.0.0.json`. Write English once, supply translations in
   `notes`, and keep each locale at 500 characters or fewer. There is no AI
   provider, translation subscription, or API key integration. A translator or
   an independently authorized translation tool can fill these fields.
4. Read each translation against the source, then record each review:

   ```sh
   python3 scripts/release.py review 2.0.0 --locale en-US
   # Repeat for every configured locale after human review.
   python3 scripts/release.py validate 2.0.0 --reviewed
   python3 scripts/release.py export 2.0.0 --reviewed
   ```

   Changing English invalidates all reviews; changing a translation invalidates
   its review. These hashes are an editorial safeguard, not authentication.
   Use plain text: TestFlight/fastlane can remove emoji and `<` characters.
   See [notes commands](README-notes.md).
5. Review and commit only the intended app changes, pipeline files, metadata,
   and release manifest. Publish the branch only when authorized. The workflow
   must exist on the repository's default branch for GitHub's manual Run
   workflow button to appear. Select the intended release ref.
6. Run the workflow with the version and `both`, `android`, or `ios`. Start with
   `upload=false`; inspect and test the artifacts. For an authorized test-track
   upload, run with `upload=true`. A fresh build is produced by that run, then
   checked against an artifact/manifest SHA-256 receipt before upload.

Both apps use the same commands and workflow design in their separate repos.
Running one app's workflow does not release the other app.

## One-time setup requiring separate authorization

Do not paste private keys or passwords into source files, issues, chat, or shell
history. Reuse the app's existing signing identities. The environment setup below
grants persistent CI access and must be explicitly authorized at the time it is
configured; this document does not perform or authorize that setup.

Create a GitHub environment named `release-testing`. Configure allowed release
branches and required reviewers where supported by the account's plan. Merely
naming the environment in YAML does not protect it. Workflow permissions are
`contents: read`; checkout does not persist a push token. No external
contributor/PR trigger can read these secrets. Authorize anyone able to dispatch
or edit the workflow appropriately.

Use the following environment secrets, scoped separately to each repository.
`*_BASE64` values are encodings of existing files, not newly generated keys.

| Purpose | Secret names |
| --- | --- |
| Android build | `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_STORE_PASSWORD`, `ANDROID_KEYSTORE_KEY_ALIAS`, `ANDROID_KEYSTORE_KEY_PASSWORD`, `GOOGLE_SERVICES_JSON_BASE64` |
| Android test upload | `PLAY_SERVICE_ACCOUNT_JSON_BASE64` |
| iOS build | `IOS_DISTRIBUTION_P12_BASE64`, `IOS_DISTRIBUTION_P12_PASSWORD`, `IOS_PROVISIONING_PROFILE_BASE64`, `GOOGLE_SERVICE_INFO_PLIST_BASE64` |
| iOS test upload | `ASC_KEY_ID`, `ASC_ISSUER_ID`, `ASC_PRIVATE_KEY_BASE64` |

Android must use the upload key already registered to the Play app. Google Play
App Signing keeps the app signing key separately. The existing Google service
account needs access only to the intended app and test releases; production
release/admin permissions are unnecessary for this workflow. New apps need their
initial Play Console setup and first manual upload completed before fastlane
supply can be used.

iOS needs the existing Apple Distribution certificate **including its private
key**, the export password, and a valid App Store provisioning profile matching
the app's bundle ID, team, and entitlements. Review
`release/config.json` against the developer account before use. The CI helper
checks profile identity, expiry, and distribution type and imports it into an
ephemeral keychain; it never requests a new certificate/profile, registers a
device, or enables automatic provisioning. A separate existing App Store
Connect API key with access sufficient to upload builds and edit TestFlight
notes is needed only for upload. Do not replace the current signing identity.

Firebase configurations are currently local/ignored. Supply the correct
existing Android and iOS app configurations; they are materialized only on the
hosted runner and removed in cleanup. The helper refuses to overwrite an
existing file. It cannot be used against a developer's personal keychain.

Choose an installed Xcode compatible with the project's Kotlin/native plugins,
Pods and current Apple upload requirements. Set optional
`RELEASE_XCODE_DEVELOPER_DIR` to its `Contents/Developer` directory on the hosted
macOS image. The workflow logs the selected version. Verify it before the first
release; installed runner images change. Both jobs install the existing project's
Android SDK 37 platform (`platforms;android-37.0`) and Java 17. Ruby 3.4,
fastlane and CocoaPods use the committed `Gemfile.lock`.

After the source, budget, secrets, branch protection, signing, versions and
toolchain have been reviewed and explicitly authorized, set the repository
variable `RELEASE_PIPELINE_ENABLED=true`. No setup command here enables it.

## Test-track behavior and production

Play uploads hardcode `track: internal`. Metadata, screenshots and images are
skipped; localized changelogs are included. `changes_not_sent_for_review=true`
and `rescue_changes_not_sent_for_review=false` disable fastlane’s fallback retry.
Google documents the former for rejection handling, so it is not a universal
no-review guarantee. The commit adapter also uses `ERROR_IF_IN_REVIEW` and
disables commit retries, preventing it from canceling an active review. This lane targets internal testing only. If Play requires
console action for the app, complete it separately after authorization.

TestFlight uploads wait for processing to attach localized “What to Test”
notes. They skip beta review submission and external distribution. An existing
automatic **internal** testing group can receive builds; otherwise assign the
build to internal testers manually in App Store Connect. The lane does not
manage groups or testers. TestFlight processing may time out after upload: check
App Store Connect before retrying the same build number.

Production preparation and submission use the separate opt-in
[store workflow](STORE-FLOW.md). They select the exact tested store build and
apply reviewed notes and explicitly selected listing fields. Submission and
public release remain separate actions. TestFlight notes are separate from the
public App Store “What's New” field; the manifest supplies both formats.

Persistent store descriptions and titles live under `release/metadata/android`
and `release/metadata/ios`. They are reused by the exporter and are not
selected for upload automatically. The store planner exports only explicitly
selected fields into its separate delta directory. Update/retranslate them only
when the listing changes.
Read the app-specific [inventory](INVENTORY.md) before relying on migrated text.

The workflow retains selected build artifacts for seven days. Preserve the
tested artifacts and mapping/dSYM files in an approved location if needed longer.
No broad build directory, keychain, keystore, profile, or API key is uploaded.

## Local validation and build use

Python 3.10+, Java 17, the Android SDK, and Ruby 3.4 are prerequisites.
macOS/Xcode is required for iOS. Install gems with `bundle install` using the
lockfile. The existing app's Gradle/Pods dependencies remain unchanged.

```sh
python3 -m unittest discover -s scripts/tests -p 'test_release*.py'
for test_file in scripts/tests/test_*.rb; do
  bundle exec ruby "$test_file" || exit 1
done
bundle exec fastlane lanes
```

See [fastlane commands](../fastlane/README.md) for local builds using already
installed signing material. Local credential import is intentionally not
automated. Never run a test upload unless it has been explicitly authorized.

## Why this setup

GitHub Actions fits the existing GitHub repositories and ChorePilot CI.
fastlane provides both store formats and build/upload tooling. Codemagic would
introduce another service and signing integration without removing the current
need to review translations, authorize credentials, and test the apps, so it was
not enrolled or configured.

Official references: [Play/supply](https://docs.fastlane.tools/actions/upload_to_play_store/),
[TestFlight/pilot](https://docs.fastlane.tools/actions/upload_to_testflight/),
[metadata/deliver](https://docs.fastlane.tools/actions/upload_to_app_store/),
[GitHub signing](https://docs.github.com/en/actions/how-tos/deploy/deploy-to-third-party-platforms/sign-xcode-applications),
[GitHub environments](https://docs.github.com/en/actions/how-tos/deploy/configure-and-manage-deployments/control-deployments).
