# Store-flow validation — 2026-10-23

This extends the locally validated build/test-upload pipeline documented in
[VALIDATION.md](VALIDATION.md). It was tested without real credentials, store
requests, signed builds, hosted jobs, pushes, submissions, or publication.

Passed:

- **60 Python tests:** the existing release/build credential tests plus selected
  metadata deltas, translation import/export, stale review hashes, required
  declarations, provider locale allowlists, official text limits, safe URL and
  path handling, no credential-value expansion, and temporary CI API-key guards.
- **65 Ruby tests / 355 assertions:** 15 build/test-lane contracts, 16 Play/store
  guards, 4 pinned-client transport/serialization contracts, 22 Apple state-flow
  contracts, and 8 pinned Apple model serialization contracts. Runtime: Ruby
  3.4.7, fastlane 2.241.1 and the committed dependency lockfile.
- Actual pinned Spaceship transport with fake Faraday returns timeout/500 after
  exactly one attempt and does not print sentinel reviewer credentials. The
  actual Google client serializes `ERROR_IF_IN_REVIEW` and zero commit retries.
- App-specific end-to-end fixtures using the real ChorePilot and DreamNorth
  configurations pass: six/five canonical languages, exact store-locale mappings,
  translation handoff, deterministic previews, submission requirements, reuse
  checks and stale-review invalidation. Fixture notes and confirmations existed
  only in temporary directories; no actual release was approved.
- actionlint 1.7.12 passes both release workflows. Both are manual dispatch only;
  they share a concurrency group and use read-only repository permission.
- Actual `bundle exec fastlane lanes` loads all ten lanes without authentication.
- Independent source review checked pinned API methods, state enums, exact build
  selection, narrow metadata patches, preservation, error redaction, and stage
  separation. Primary sources and limitations are in [API-SUPPORT.md](API-SUPPORT.md).

No test proves that either live store account is ready. The first separately
approved connected preparation must verify account permissions, actual current
versions/builds/locales, metadata completeness, review state, and Play validation.
Provider-side errors are preserved as blockers; response bodies are suppressed
because they can contain reviewer credentials. Inspect the named target in its
store console for an additional provider-required field not known locally.

Initial production publication is outside these update lanes. Google Managed
Publishing cannot protect the first publication; Apple first-version rules and
missing listing baselines need separate setup. Scheduled Apple release dates,
existing production rollouts, unrelated pending changes, and ambiguous versions
stop the workflow for explicit reconciliation.

The prior Gradle and Xcode scheme checks remain historical evidence for the
build configuration; no Kotlin, Gradle, signing, or Xcode project files changed
in this store-flow extension. A signed build or live submission was not claimed.

Apply records in the task workspace contain SHA-256 hashes of every installed
file. The installer checks the inspected originals before writing, refuses
intervening edits, and changes only the listed release-tool files. Application
sources, app-specific configuration, metadata drafts, and unrelated Git changes
are preserved.
