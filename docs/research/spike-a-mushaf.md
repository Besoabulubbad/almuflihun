# Spike A result: rendering the printed Mushaf in Compose

- **Date:** 2026-11-26
- **Code:** `spike/mushaf` (throwaway Android app, never shipped), branch `spike/mushaf-rendering`.
- **Question:** can Compose draw the printed Madani pages from glyph data, with per-word interaction and dark mode, within the quality bar, and at what APK cost?

## Method

Pages 1, 2, 3, 50, 51, 300, 301 and 604, on emulators with Android 17 (API 37) and Android 13 (API 33). Each page is 15 lines (8 on pages 1–2) from the QUL layout; each line is a row of word composables, justified (`SpaceBetween`) or centred per `is_centered`; the font size is fitted so the widest justified line fills the width. Tapping a word selects its ayah. A switch compares two fonts on the same layout.

Data (owner-downloaded from QUL, 2026-11-26):

| File | Content |
|---|---|
| `qpc-v2-15-lines.db` | KFGQPC V2 (1421H) layout: 604 pages, 9,046 lines, `pages(page_number, line_number, line_type, is_centered, first_word_id, last_word_id, surah_number)` |
| `qpc-v2.db` | 83,668 words (77,430 words + 6,236 ayah-end markers + 2), each a private-use glyph code for its page's font |
| `digital-khatt-15-lines.db` | Digital Khatt layout of the same 1421H print |
| `digital-khatt-v2.db` | 83,668 words as Unicode Uthmani text |

**The two layouts are identical** (same line breaks and word IDs; checksums equal), so the fonts are interchangeable on one layout.

## Results

| | QPC V2 page fonts | Digital Khatt V2 ("New Madina") |
|---|---|---|
| Fidelity | Exact printed glyphs per word | Same lines, same words, same ayah markers; justified here by word spacing, where the print stretches letters, so some lines show wider gaps |
| Text | Private-use codes (unreadable for copy, search, screen readers) | Real Unicode Uthmani |
| Fonts | 604 files; 200 MB TTF, ≈130 MB deflated in an APK, ≈94 MB as WOFF2 (which Android cannot load directly). Nothing to strip: the files are almost all outlines (≈189 word glyphs per page) | One OpenType CFF2 font, **510 KB** |
| Licence | KFGQPC; no written licence (ADR 0004 risk) | **SIL Open Font License 1.1** (Amine Anane; Tarteel) |
| Renders on | API 37, API 33 | API 37, API 33 |
| Per-word selection, dark mode | ✅ | ✅ |
| Page data load | font 1–11 ms, layout and words 12–46 ms | same layout and word queries |

Not yet tested: Android 8–12 (API 26–32) for CFF2 support (system images not installed); surah-name lines and the basmala (placeholders in the spike); performance budgets on a real mid-range device.

## Recommendation (first, superseded)

*Superseded by the owner's requirement for the exact printed glyphs; see the follow-up and ADR 0013.* Adopt **Digital Khatt V2** for the Mushaf and Hizb pages, and use the same font and text for every Quran text in the app (cards, slider, study panel), so a verse looks the same everywhere. It removes ~94–130 MB from the APK, replaces an unwritten licence with the OFL, and gives the study engine, copy, search and screen readers the real text. Keep QPC V2 as the documented fallback. Before release: test Android 8–12 (and raise `minSdk` if CFF2 fails there), and refine justification (letter stretching instead of only word spacing) if the owner finds word spacing too loose.

This changes ADR 0004 and design DECISIONS #3, #4, #14 (page fonts) and #12 (card font) and needs the owner's decision and a superseding ADR.

## Follow-up (2026-11-26/27)

- **Marks:** every waqf mark (صلى 1,651; قلى 511; ج 2,083; م 21; ∴ 6; س 7), the 199 ۞ quarter marks and the 15 sajdah marks ۩ are in the text data and drawn by both fonts (pages 5 and 176 checked). QPC V2 also draws the printed overline over the sajdah word; Digital Khatt does not.
- **Tajweed:** the QPC V4 (1441H) font is COLR v0 with 6 palettes; it renders in colour on Android 13 with the V2 word codes, except a few pause marks that need V4's own word data.
- **Compression:** Brotli on the TTFs gives 116 MB, no better than WOFF2 (94 MB). The Quran Foundation CDN serves TTF uncompressed (no gzip/br), so WOFF2 is the download format.
- **quran.com's Android app** ships no pages and asks «Download Required Files?» on first launch (`QuranDataActivity.kt`).
- **Decision:** [ADR 0013](../adr/0013-mushaf-fonts-download.md).
