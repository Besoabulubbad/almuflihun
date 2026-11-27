# Quran source data: origins and checksums

Committed per ADR 0004 (layout and word data) and ADR 0013 (the page fonts are **not** here: they are downloaded at runtime). Every file is shipped exactly as downloaded; normalisation happens when the app reads it. `./gradlew :core:quran:verifyQuranSources` (part of `check`) fails if any file below is missing, changed, or not listed.

| File | What it is | Source | Terms | SHA-256 |
|---|---|---|---|---|
| `quran-simple.txt` | Tanzil Quran Text (Simple, Version 1.1): 6,236 verses as `surah\|ayah\|text`, the base for search and quote matching | [tanzil.net/download](https://tanzil.net/download/), 2026-11-17 | CC BY 3.0, verbatim only, with its copyright block and a link to tanzil.net ([licence](https://tanzil.net/docs/Text_License)) | `b3638f5cf03e2b05e4000efc0e4b656aafb3cf6ed28f8770b6497c5b43c46089` |
| `qpc-v2-15-lines.db` | KFGQPC V2 (1421H) Madani layout: 604 pages, 15 lines, `pages(page_number, line_number, line_type, is_centered, first_word_id, last_word_id, surah_number)` | QUL, [mushaf-layout/10](https://qul.tarteel.ai/resources/mushaf-layout/10), 2026-11-26 | QUL: "no copyright information"; credited; permission request planned (ADR 0004) | `e4df98f35dd3b8927ff096337c8739e0f0b12c8ba622834c345eaa4c3e28dd8c` |
| `qpc-v2.db` | 83,668 words (6,236 ayahs) as QPC V2 page-font glyph codes: `words(id, location, surah, ayah, word, text)` | QUL, the glyph words of the QPC V2 font ([font/249](https://qul.tarteel.ai/resources/font/249)), 2026-11-26 | as above | `4bf9549dfcfd367d4d4b151bd58b51af63b677d1c980cf5e52541c2f981d7e6d` |
| `digital-khatt-v2.db` | The same 83,668 words as Unicode Uthmani text, for the Digital Khatt fallback, cards and screen readers | QUL, Digital Khatt V2 words ([font/247](https://qul.tarteel.ai/resources/font/247)), 2026-11-26 | as above | `071c0f43e30819b8b6427194c2f4c97ff05340da3808092c590967913b0382a6` |

## Checked facts

- QUL's Digital Khatt layout (`digital-khatt-15-lines.db`) has exactly the same `pages` rows as the QPC V2 layout; only its `info` row differs. So one layout serves both fonts and the second file is not committed.
- `qpc-v2.db` and `digital-khatt-v2.db` have the same word ids and locations, row for row.

## Updating

Download the new file, replace it, put its date and new checksum here, and say in the PR what changed. A changed Quran file is reviewed like code.
