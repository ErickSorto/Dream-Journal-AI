# Builds and store actions

Use the manually dispatched release workflow after the one-time setup in
`release/SETUP.md`. It builds the reviewed release's AAB and IPA; uploading to
Google Play internal testing and TestFlight is an explicit workflow choice.
Production preparation, submission, and public release are separate opt-in
actions described in [STORE-FLOW.md](../release/STORE-FLOW.md).

## Local commands

Use Ruby 3.4 and run `bundle install` using the pinned `Gemfile`.
Commit the reviewed `Gemfile.lock` with the pipeline before enabling CI. The system
Ruby 2.6 is too old for this bundle; Ruby 3.4 is available on this Mac at
`/usr/local/opt/ruby/bin/ruby`. Both iOS projects already have
`Podfile.lock`; the iOS lane runs `pod install --deployment` and never updates
the pod repositories automatically.

After creating and reviewing a release with `scripts/release.py`:

```sh
bundle exec fastlane android build_test version:1.2.3
bundle exec fastlane ios build_test version:1.2.3
```

The version above is illustrative. Use the actual manifest version and build
numbers greater than those accepted by the respective store. These build lanes
do not upload. They require existing, authorized signing material and the app's
Firebase configuration files.

Only after explicitly authorizing a test upload:

```sh
RELEASE_UPLOAD_CONFIRMED=internal bundle exec fastlane android upload_test version:1.2.3
RELEASE_UPLOAD_CONFIRMED=internal bundle exec fastlane ios upload_test version:1.2.3
```

Never put credentials directly in commands, tracked files, or release notes.
Use the workflow's temporary credential files or a securely supplied local
environment. Signing material is neither created nor registered by these lanes.

| Operation | Required environment |
| --- | --- |
| Android build | `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_STORE_PASSWORD`, `ANDROID_KEYSTORE_KEY_ALIAS`, `ANDROID_KEYSTORE_KEY_PASSWORD` |
| Android upload | `PLAY_SERVICE_ACCOUNT_JSON` (path to authorized service account JSON) |
| iOS build | `IOS_PROFILE_NAME`, `IOS_TEAM_ID`; an installed App Store distribution profile and its certificate/private key |
| iOS upload | `ASC_KEY_ID`, `ASC_ISSUER_ID`, `ASC_KEY_PATH` (path to an existing authorized `.p8` file) |
| Either upload | `RELEASE_UPLOAD_CONFIRMED=internal` |

Both platforms take versions from the same reviewed manifest. Android receives
`RELEASE_VERSION_NAME` and `RELEASE_VERSION_CODE` in its process environment.
iOS receives `MARKETING_VERSION` and `CURRENT_PROJECT_VERSION` for the archive
command; Xcode export is told not to renumber the app.

The Android lane consumes the project's final, verified `SingleArtifact.BUNDLE`
through `exportStoreReleaseBundle`, instead of selecting an arbitrary old AAB.
The iOS lane applies manual signing only to the app target's Release
configuration and restores the project's original bytes afterward. It does not
use automatic provisioning or change Pod targets to the app's provisioning
profile. Do not edit the Xcode project while that build is running.

Outputs are in `build/release/<version>/`: `app.aab`, `app.ipa`, Android's
`mapping.txt` when generated, the reviewed manifest, generated metadata, and
platform build receipts. A receipt binds the artifact hash to the exact
exported manifest and release configuration. If notes, reviews, versions,
locale mappings, or the artifact change, upload stops until that release is rebuilt. The workflow
retains artifacts; store uploads never select a binary by modification time.

## Store behavior

Google Play uploads are fixed to `internal`. Store descriptions, screenshots,
and images are skipped; reviewed localized changelogs are included.
`changes_not_sent_for_review: true` is set and automatic retry without that flag
is disabled. Google documents it for rejection handling, not a universal
no-review guarantee; this lane always targets internal testing. Its commit
adapter adds `ERROR_IF_IN_REVIEW` with zero retries to avoid canceling an active
review or repeating an uncertain commit. Play may still require initial
app setup, a first manual upload, or a separate console action before testers
can install. A console-dependent error is left for review rather than silently
changing publication settings.

TestFlight uploads wait for processing to attach localized "What to Test"
notes, and never submit beta review or distribute to external groups. Configure
an **internal** TestFlight group with automatic distribution in App Store
Connect if you want the processed build to reach internal testers automatically;
otherwise assign the build manually. The lane passes no groups and makes no
membership changes. Processing waits time out after 30 minutes; an upload may
already have succeeded when Apple processing times out, so inspect App Store
Connect before retrying the upload.

Store operations consume the reviewed **delta plan**, never the broad build
export directory. `prepare_store`, `submit_store`, and `release_store` are gated
separately by platform, action, and version. Read [STORE-FLOW.md](../release/STORE-FLOW.md)
for exact effects, required fields, and activation. Android public release
remains a Play Console action. Translation generation is not connected to any
paid or AI service.

## References

- [fastlane Android bundles and output behavior](https://docs.fastlane.tools/actions/gradle/)
- [fastlane Play upload options](https://docs.fastlane.tools/actions/upload_to_play_store/)
- [fastlane TestFlight upload options](https://docs.fastlane.tools/actions/upload_to_testflight/)
- [fastlane archive and export options](https://docs.fastlane.tools/actions/build_app/)
- [target-specific signing settings](https://docs.fastlane.tools/actions/update_code_signing_settings/)
