# Verify stored iOS signing credentials

Run **Verify iOS signing setup** from the repository's default branch and approve
its `release-testing` environment job. The workflow must first be merged onto
that branch. It uses the same four iOS build secrets and credential helper as
the existing release pipeline; no release version or store API key is needed.

The check decrypts/imports the P12 into an ephemeral runner keychain, requires a
valid distribution identity whose certificate appears in the supplied profile,
and validates the profile's expiry, app/team identifiers, distribution type and
Firebase bundle ID. Cleanup runs even after a failed import. A wrong password
fails the import without printing it. The test uploads no artifacts, builds no
application, contacts no app store, and does not enable the release workflows.

GitHub secret names and timestamps only prove a value is stored. A successful
run is the credential-usability check; a later signed app build and device test
are still required. A failed runner start or billing restriction is not a
credential failure. Inspect the workflow summary and failing step before retrying.
