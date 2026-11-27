# 0015 — The Quran ships as one read-only database, built from the sources at compile time

- **Status:** accepted
- **Date:** 2026-11-27
- **Deciders:** Mohammad Abu Lubad (delegated: "implemented the right way"), Claude
- **Tags:** quran, data, build
- **Amends:** [`docs/architecture/modules.md`](../architecture/modules.md) (`:core:quran` may use Compose Resources and, from the read API, SQLDelight)

## Context and problem statement

`core/quran/sources` holds the Tanzil text and QUL's layout and word databases exactly as downloaded (ADR 0004). The app needs them as one fast, offline store it can query by page, line, ayah and word on Android and iOS, and Phase 3b adds study indexes to the same store (ADR 0012). The sources must stay unmodified, and any mismatch between them must be caught before it reaches a reader.

## Decision drivers

- Never edit the Quran: the shipped values are byte copies of the sources.
- A broken or mismatched source fails the build, not a page in a user's hand.
- One query engine for pages, ayahs, words and, later, search indexes.
- The same file and code path on Android and iOS.

## Options considered

1. **A: a build-time generator writes one SQLite file (`quran.db`) into Compose Resources; the app copies it to storage once per schema version and opens it read-only.**
2. **B: ship the four sources as they are and join them at runtime.** Three databases plus text parsing on every launch; validation only at runtime.
3. **C: parse the sources into in-memory structures at startup.** About 5 MB of text to parse on each launch; no room for the 3b indexes.

## Decision outcome

**Chosen: A.**

- `almuflihun.quran.database` (build-logic) runs `QuranDatabaseBuilder` over `core/quran/sources` and writes `files/quran.db` (tables `line`, `word`, `ayah`, `meta`) into a generated Compose Resources directory of `:core:quran`. Build-time only dependency: `org.xerial:sqlite-jdbc`.
- The builder **checks the sources before writing**: 604 pages with 15 lines (8 on pages 1–2), 114 surah-name lines, 112 basmala lines, every word covered by exactly one layout line in order, the glyph and text word sets identical by id and location, exactly one ayah-end marker per ayah and always last, and the Tanzil verses equal to the layout's 6,236 ayahs. Any failure stops the build.
- Values are **copied, never changed**; a test compares all 83,668 words and glyphs byte for byte with the sources. Normalisation (basmala, pause marks) happens when the app reads.
- `meta` records `schema_version` and the SHA-256 of each source. The app replaces its copy when the version changes.

### Consequences

- Good: the whole Quran is one indexed file; page and ayah lookups are single queries.
- Good: source problems fail CI with a clear message.
- Good: Phase 3b adds its indexes to the same generator.
- Bad: `:core:quran` depends on Compose Resources (resources only, no UI) and, for reading, SQLDelight; the module contract is amended.
- Bad: one more build step (about a second) and about 10 MB in the APK before compression.

## More information

- Builder and tests: `build-logic/convention/src/main/kotlin/app/almuflihun/buildlogic/quran/`
- Sources and checksums: `core/quran/sources/SOURCES.md`
