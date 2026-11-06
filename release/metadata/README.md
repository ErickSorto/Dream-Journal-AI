# Reusable store metadata

Keep approved listing text here once; update only when the listing changes.
Android locale folders use `title.txt`, `short_description.txt`,
`full_description.txt`. iOS folders use `name.txt`, `subtitle.txt`,
`description.txt`, `keywords.txt`, `promotional_text.txt`, `privacy_url.txt`,
`support_url.txt`, and `marketing_url.txt`.

The notes exporter adds Android version-code changelogs and iOS release_notes
under `build/release/<version>/metadata`. Test uploads only send release notes;
they never modify store descriptions, titles, screenshots or privacy URLs.
Review inventory/provenance in `../INVENTORY.md` before production use.
