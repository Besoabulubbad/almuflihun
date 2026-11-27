# 0004 — Quran rendering assets come from QUL and are committed to the repo

- **Status:** accepted
- **Date:** 2026-11-26
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** quran, licensing, build
- **Partly superseded by:** [0013](0013-mushaf-fonts-download.md) (page fonts: downloaded from Quran Foundation, never committed)

## Context and problem statement

The mushaf and hizb screens draw the printed Madani pages (604) with the QPC V2 page fonts, and the whole mushaf must work offline from the first launch (`design/DECISIONS.md` #3, #4, #14). That needs the 604 page fonts, a page layout (lines → words), the KFGQPC HAFS font for cards, and word timings for audio. [The research](../research/mushaf-rendering.md) found:

- Quran Foundation's terms allow bundling its fonts with credit, but forbid a prepackaged layout database and count API data in a public repository as redistribution.
- QUL (Tarteel) offers the fonts, the KFGQPC V2 layout (SQLite) and word segments for packaging in apps, but each resource's licence page says it has no copyright information.
- The owner of the fonts and layout, the King Fahd Complex, publishes free downloads but no licence text.

## Decision drivers

- The mushaf works offline from the first launch.
- Simple, reproducible builds for anyone who clones the repo.
- Honest attribution of every source.

## Options considered

1. **QUL files downloaded at build time:** pinned URLs and checksums, bundled into the APK, never committed.
2. **Quran Foundation:** bundled fonts, layout synced from the API on first launch.
3. **QUL files committed to the repository.**

## Decision outcome

**Chosen: Option 3**, the owner's decision.

- The QPC V2 page fonts, the KFGQPC V2 layout database, the KFGQPC HAFS font, the surah-name font and the word-segment files are committed under one assets folder, each with its source URL, download date and checksum in a `SOURCES.md` beside it.
- Tanzil's `quran-simple.txt` is committed unmodified (DECISIONS #13).
- Every source is credited in Settings → «مصادر النص والصوت» and in `THIRD_PARTY.md`.
- Recitation audio is not committed; it is downloaded per surah at runtime (DECISIONS #21). Its source is a separate decision.

### Consequences

- Good: any clone builds offline; CI needs no network for assets.
- Good: one place lists every third-party asset and where it came from.
- Bad: the repo republishes files whose licences are not written down. Mitigated: full attribution; a written permission request to the King Fahd Complex and Tarteel before any store release; the files are removed promptly if an owner asks.
- Bad: tens of MB of binary fonts in git history. Accepted; the files rarely change.

### Confirmation

- A Gradle check verifies each asset against its checksum in `SOURCES.md`.
- The release checklist includes "permission requests sent; credits screen lists every asset".

## Links

- [Research: mushaf rendering](../research/mushaf-rendering.md)
- `design/DECISIONS.md` #3, #4, #5, #12–14, #21
