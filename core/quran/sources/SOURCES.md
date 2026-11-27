# Quran source data: origins and checksums

Committed per ADR 0004 (layout and word data) and ADR 0013 (the page fonts are **not** here: they are downloaded at runtime). Every file is shipped exactly as downloaded; normalisation happens when the app reads it. `./gradlew :core:quran:verifyQuranSources` (part of `check`) fails if any file below is missing, changed, or not listed.

| File | What it is | Source | Terms | SHA-256 |
|---|---|---|---|---|
| `quran-simple.txt` | Tanzil Quran Text (Simple, Version 1.1): 6,236 verses as `surah\|ayah\|text`, the base for search and quote matching | [tanzil.net/download](https://tanzil.net/download/), 2026-11-17 | CC BY 3.0, verbatim only, with its copyright block and a link to tanzil.net ([licence](https://tanzil.net/docs/Text_License)) | `b3638f5cf03e2b05e4000efc0e4b656aafb3cf6ed28f8770b6497c5b43c46089` |
| `qpc-v2-15-lines.db` | KFGQPC V2 (1421H) Madani layout: 604 pages, 15 lines, `pages(page_number, line_number, line_type, is_centered, first_word_id, last_word_id, surah_number)` | QUL, [mushaf-layout/10](https://qul.tarteel.ai/resources/mushaf-layout/10), 2026-11-26 | QUL: "no copyright information"; credited; permission request planned (ADR 0004) | `e4df98f35dd3b8927ff096337c8739e0f0b12c8ba622834c345eaa4c3e28dd8c` |
| `qpc-v2.db` | 83,668 words (6,236 ayahs) as QPC V2 page-font glyph codes: `words(id, location, surah, ayah, word, text)` | QUL, the glyph words of the QPC V2 font ([font/249](https://qul.tarteel.ai/resources/font/249)), 2026-11-26 | as above | `4bf9549dfcfd367d4d4b151bd58b51af63b677d1c980cf5e52541c2f981d7e6d` |
| `digital-khatt-v2.db` | The same 83,668 words as Unicode Uthmani text, for the Digital Khatt fallback, cards and screen readers | QUL, Digital Khatt V2 words ([font/247](https://qul.tarteel.ai/resources/font/247)), 2026-11-26 | as above | `071c0f43e30819b8b6427194c2f4c97ff05340da3808092c590967913b0382a6` |
| `quran-data.js` | Tanzil Quran Metadata 1.0: per surah its start, ayah count, revelation order, Arabic name and Meccan/Medinan; juz and hizb-quarter starts; sajdas | [tanzil.net/docs/quran_metadata](https://tanzil.net/docs/quran_metadata) (`res/text/metadata/quran-data.js`), 2026-11-27 | CC BY 3.0, verbatim, with attribution | `9e9930c592aaa34b5c7a79ff4e99ffdc782263a266090bfe2c49e4ed4bac4ca4` |
| `word-root.db` | The root of each word: `roots(id, arabic_trilateral, …)` (1,642) and `root_words(root_id, word_location)` (50,298 words) | QUL, [Word root](https://qul.tarteel.ai/resources/morphology/quran-word-roots), 2026-11-27 (owner download) | QUL: "no copyright information"; credited; permission request planned (ADR 0012) | `d2091e8bf69426be0cfae5caff70ed08ca4af691a41d70e7782395372a5b01b5` |
| `word-lemma.db` | The lemma of each word: `lemmas(id, text, text_clean, …)` (4,817) and `lemma_words` (72,510 links) | QUL, [Word lemma](https://qul.tarteel.ai/resources/morphology/75), 2026-11-27 (owner download) | as above | `9a210ac2e724f0009d2b04e8e2f79b66b232689af608c4e1b2c8dceb655d4ccc` |
| `word-stem.db` | The stem of each word: `stems(id, text, text_clean, …)` (12,113) and `stem_words` (77,427 links) | QUL, [Word stem](https://qul.tarteel.ai/resources/morphology/77), 2026-11-27 (owner download) | as above | `90e9fb8ef0d08c5f53220df9c585deb001743797fd46475fb6279d80dd6ccc90` |

## Checked facts

- The morphology files link words by `surah:ayah:word` in the layout's numbering. Every link names a real word; no word has two roots, lemmas or stems. Words without a root (27,134) are particles, pronouns and the like; without a lemma, 4,925 (mostly pronoun forms); without a stem, 5 (2:181:4, 2:282:117, 2:282:124, 8:6:5, 13:37:9). Three lemma links point at ayah-end markers (13:5:26, 2:275:46, 4:176:51), an error in the source; the build drops exactly these three and fails on any other. The `words_count` columns are not used (14 roots disagree with their own links); counts come from the links. Root letters are stored with irregular spaces, removed when read.
- Reference counts for chosen roots match published concordance counts: صبر 103, رحم 339, علم 854, كتب 319, قول 1722, ذكر 292, أمر 248, نهى 56.

- `quran-data.js` agrees with the rest: its 114 ayah counts equal the layout's; its 240 hizb-quarter starts equal `design/data/hizb-60.json`; its juz starts are every eighth quarter. Its **page** starts follow an older print (25 of 604 differ from the QPC V2 layout), so pages always come from `qpc-v2-15-lines.db` and Tanzil's page list is not used.

- QUL's Digital Khatt layout (`digital-khatt-15-lines.db`) has exactly the same `pages` rows as the QPC V2 layout; only its `info` row differs. So one layout serves both fonts and the second file is not committed.
- `qpc-v2.db` and `digital-khatt-v2.db` have the same word ids and locations, row for row.

## Updating

Download the new file, replace it, put its date and new checksum here, and say in the PR what changed. A changed Quran file is reviewed like code.
