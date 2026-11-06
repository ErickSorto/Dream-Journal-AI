# Validation — 2026-10-20

Passed locally without real credentials or store calls:

- 25 Python tests: notes/review invalidation, export paths, locale mapping,
  temporary credential handling, app/profile validation, cleanup and overwrite refusal.
- 14 Ruby tests / 56 assertions: test-only upload controls, symbol-keyed localized
  TestFlight notes, artifact/config/manifest receipts, mapping export and restoration.
- Locked Ruby 3.4 dependencies installed in a temporary directory. Actual fastlane
  2.241.1 loads the four lanes; every supplied action parameter exists in this version.
- Both modified Gradle scripts configure successfully with Java 17 and offline
  cached dependencies in isolated copies. An additional inspection confirms the
  export task depends on bundleRelease and release has no debug-signing fallback.
- actionlint 1.7.12 reports no workflow findings. YAML gates pass; actual Xcodeproj
  parsing confirms both shared schemes reference the correct application target.

These checks do not certify a signed AAB/IPA or successful upload. No app binaries
were built, no signing credentials were used or generated, and no store was contacted.
The first separately authorized run must verify the selected hosted Xcode, app
source completeness, signing compatibility, actual build numbers and test-track access.
Existing AGP/KMP deprecation warnings remain; application tests and production
behavior are outside these release-tool configuration checks.
