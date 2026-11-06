# DreamNorth release inventory — 2026-10-20

- Checkout: `/Users/ericksorto/AndroidStudioProjects/Dream-Journal-AI`
- Remote: `https://github.com/ErickSorto/Dream-Journal-AI.git`
- Branch inspected: `chore/implement-categorizer`, HEAD `5815506e34e545383a3bba424a82865d0e05e9e8`
- Pre-existing tracked changes: 186 modified, 407 deleted, plus 285 untracked status entries.
- Android module `:composeApp:shared`, package `org.ballistic.dreamjournalai`; local version `1.3.7` / `111`.
- iOS target `iosApp`, workspace `iosApp/iosApp.xcworkspace`; bundle `org.ballistic.dreamjournalai`. Target-level team `9J5J9J3D9R`; the project-level settings still contain another older team. Confirm the correct account; the lane uses the explicit configured target team.
- Effective target iOS version is `1.4.2` / `92`; `Configuration/Config.xcconfig` contains older `1.4.1` / `91`. Release builds override both via the manifest, without rewriting developer defaults.
- Toolchain: Gradle 9.7.1, AGP 9.4.1, Kotlin 2.4.20, SDK 37, Java 17, CocoaPods 1.16.2; iOS deployment target 16.0.
- No existing GitHub workflow or fastlane setup was found.

## Changes made

Removed Android release's debug-key fallback. Unconfigured ordinary release
builds are now unsigned; the store pipeline refuses to proceed without explicit
existing upload-key credentials. Developer debug signing is unchanged. The
export task consumes the final bundle after the existing manifest/DEX verifier.

Added a separate shared `StoreRelease` scheme referencing the app target and
implicit dependencies. Existing custom development schemes are retained.
CocoaPods integration is retained, including its dummy-framework preparation,
Podfile hooks, and existing release checks.

## Store text provenance

Local historical release notes exist in `output/releases/2026-09-20` and
`output/releases/2026-10-03`. These describe prior releases and were not relabeled
as a new release. No reusable current description/title copies were found.
Populate `release/metadata/android/<locale>` and `release/metadata/ios/<locale>`
once from approved current store text, or authorize a separate read-only store
metadata import later. Do not regenerate descriptions for every binary release.

Five canonical note languages: en-US, pt-BR, es-ES, de-DE, fr-FR. Based on the
local historical store-note files, Spanish is reused for Android es-ES/es-419/es-US
and Apple es-ES/es-MX. Review whether this regional reuse is appropriate; split
the canonical entries if regional translations differ. Live locales were not
queried. No description/translation approval is implied.

Firebase configurations are local/ignored. Existing release artifact files and
prior store receipts were left unchanged.

## Before first use

The release branch must include the intended current app sources and all required
untracked build scripts/resources. The new pipeline cannot reproduce local
uncommitted changes from a remote checkout. No unrelated edits were staged,
committed, reset, or selected for publication during pipeline setup.

Choose new store versions from the consoles. The values below are local evidence,
not a fresh verification of live store state. No app release was initialized or
approved on your behalf. Read [SETUP.md](SETUP.md) for authorization and credentials.

Current Gradle configuration checks pass with the existing legacy AGP/KMP flags.
Gradle warns that the combined Android application/KMP module uses deprecated
compatibility and will need migration before AGP 10; this pipeline does not change
the application architecture or dependency versions.
