# Release notes and reusable store text

This tool prepares release files without contacting a store or uploading a build. [Generated notes](GENERATED-NOTES.md) can use the current agent or an explicitly chosen existing generator to draft the source and all translations; no AI provider or credentials are configured by default. Python 3.10 or later is required. The script finds the project from its own location, so commands work from any current directory when the script path is correct.

## Start a release

Choose a three-part version and unused store build numbers. Check the latest numbers accepted by both stores first: this tool validates their format, not their availability. Android version codes must be positive integers no greater than 2,100,000,000; iOS build numbers must be positive integers no greater than 9,999.

Write the source notes once in a UTF-8 text file, then run:

```sh
python3 scripts/release.py init 1.2.3 --android-code 53 --ios-build 8 --notes-file /path/to/notes.txt
```

The numbers are examples, not recommendations for the next release. Omitting `--notes-file` creates blank source notes too. Existing releases are never overwritten by `init`.

Edit `release/releases/1.2.3.json`. Its `notes` object contains every locale configured in `release/config.json`; translations start blank. Add reviewed translations yourself or through a separately authorized translation process. Do not translate app names, URLs, or identifiers accidentally. There is no automatic English fallback for missing translations.

Each locale is limited to 500 Unicode characters, including line breaks, so the same text fits the Google Play release-notes field and can be reused for iOS.

## Review the translations

Read the source and each translation. Only after checking the wording, record that locale's review:

```sh
python3 scripts/release.py review 1.2.3 --locale en-US
python3 scripts/release.py review 1.2.3 --locale es-ES
```

Repeat for every configured locale, including the source locale. After reading
all translations, `review 1.2.3 --all` records that explicit acknowledgement
in one command. It does not evaluate translation quality. The command records hashes of the exact source and translated text. Changing source text invalidates every review; changing one translation invalidates that locale's review. Even whitespace changes require a new review. The tool records your explicit acknowledgement; it cannot judge translation quality or reviewer identity.

```sh
python3 scripts/release.py validate 1.2.3 --reviewed
python3 scripts/release.py export 1.2.3 --reviewed
```

Release workflows should always use `--reviewed`. Without it, `validate` and `export` permit a complete but unapproved draft for local inspection. Blank or oversized text is never accepted.

## Store preparation and translation handoff

[STORE-FLOW.md](STORE-FLOW.md) documents the unified release flow.
`export-translations VERSION --output FILE` creates a reviewable translation
handoff; `import-translations VERSION FILE` imports draft translations without
approving them. Stale source hashes and unknown locales are rejected.

`prepare VERSION` writes `review.md`, `store-plan.json`, and only selected store
fields under `store-metadata`. It reports exact missing inputs. After reviewing
the preview, `review-store VERSION` binds the approved store input to its current
hash; `prepare VERSION --reviewed` enforces it. Use `--stage submit` to see the
additional factual confirmations needed for submission.

## Outputs

The generated directory is `build/release/1.2.3/`:

- `manifest.json`: exact release data, notes, and review hashes.
- `metadata/android/<store-locale>/changelogs/<android-version-code>.txt`: Google Play notes.
- `metadata/ios/<store-locale>/release_notes.txt`: App Store release notes.
- `testflight.json`: `localized_build_info` with each iOS locale's `whats_new`.

Logical note locales map to store locale codes in `release/config.json`. A platform mapping can be a single string or a list of strings. For example, one Spanish note can map to Android `["es-ES", "es-419", "es-US"]` and iOS `["es-ES", "es-MX"]`. This explicitly reuses the same text across those regions: reviewers must check that wording is appropriate for every mapped region. Use separate logical locales when regional wording should differ. Duplicate or unsafe destination mappings are rejected.

Persistent descriptions, titles, keywords, URLs, and other listing text belong under `release/metadata/android/` and `release/metadata/ios/`, using fastlane's metadata layout and filenames. The tool copies these folders into each export and adds the current release notes. This broad export is for build/test artifacts and human inspection. Store
preparation uses a separate selected-field delta; listing copy is never selected
merely because a file exists. The `review-store` hash approves exactly the
selected fields, notes, defaults, and configuration. Screenshots are outside
this workflow. Keep credentials, signing files, and private account information out of metadata folders.

Re-exporting replaces the generated metadata subtree and JSON files so removed metadata cannot linger. Existing AABs, IPAs, build receipts, and other outputs alongside them are preserved. The exported manifest has no timestamps and remains byte-identical when its input is unchanged. Do not hand-edit generated files; edit the release JSON or persistent metadata and export again. Commit source release files and metadata after reviewing them; generated `build/` files should remain ignored.

## Validate the tooling

```sh
python3 -m unittest discover -s scripts/tests -p 'test_release.py' -v
```
