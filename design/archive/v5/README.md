# المفلحون: design handoff for Claude Code

This folder is everything needed to build the screens designed so far. **Read `DECISIONS.md` first, then `REQUESTS-ANSWERED.md`** (design v4: what answers each engineering request), and `COPY.md` (push and in-app copy). it records the answers to the review (server, Quran text, mushaf pages, licences, lunar day, theme). Then read in this order:

1. **`DESIGN.md`:** the strict spec: grid, text roles, colour roles, every component with exact tokens, icons and enforcement rules. Start here.
2. **`tokens.json`:** the closed token set. No size, space, radius, colour or text style may exist outside it.
3. **`code/AppTokens.kt`:** the same tokens for Compose Multiplatform (generated from `tokens.json`). Bind `AppFonts` to the bundled fonts at startup.
4. **`SCREENS.md`:** each screen, its order of sections, its data and its navigation.
5. **`screens/*.png`:** the 33 approved screens (plus `screens/dark/` samples) at 2× (390 pt wide). All mockups were snapped to the type, spacing and radius scales. If a component still differs from DESIGN.md, DESIGN.md is correct.
6. **`mockups/*.dc.html`:** the source mockups (layout reference only).
7. **`illustrations/*.svg`:** the الضياء illustration set (64×64).
8. **`data/`:** the content (see below).

Clickable prototype of the same screens: https://claude.ai/artifact/8KGuBC9oTMC5yUbQkQYpe3

## Data files

| File | What it holds | Used by |
|---|---|---|
| `data/abwab-4.json` | All doors (أبواب).<br>`meta.visible_doors` lists the 7 shown now.<br>`abwab[].design` holds the section layout of each visible door.<br>`verses` maps `"s:a"` to the verse text **after read-time normalisation** (Tanzil simple, basmala split off, ۞ removed). It is a test fixture, not shipped text. | Doors, BabAll, «دليل اليوم», Saved |
| `data/dhikr-slider.json` | The 14 slider items `{id, order, door, entry, key, quote}`. | الذكر slider |
| `data/hizb-60.json` | The 60 hizbs: `{hizb, juz, start, end, quarters[4]}` (Hafs). Generated from the `quran-meta` npm package. | Hizb card, Hizb page |
| `data/diyaa-food.json` | The 11 stations of الضياء. Each is a list of typed `blocks` (see DESIGN.md §6), plus `summary` and `verses`. | الضياء hub and stations |
| `data/food-app-page-2.source.md` | The group's original text for الضياء. | reference only |

### Checks already done
- Every `quote` (87 in the doors, 14 in the slider) and every `highlight` (الضياء) is an **exact substring** of its verse text. Keep a unit test that asserts this. A broken quote must fail the build, not show a cut verse.
- Verse keys use `surah:ayah`. Ranges are lists of consecutive keys.
- **Quran text shipped in the app** (DECISIONS.md #12–13):
  - Tanzil's `quran-simple.txt`, untouched, is the base for matching.
  - The King Fahd Complex Hafs text and the KFGQPC fonts are what the user sees.
  - The QPC page fonts and page data draw the mushaf.
  - All normalisation (basmala split, ۞, pause marks) happens when the file is read.
- **Word ranges.** Every quote and highlight also has `words: [start, end]` (1-based, inclusive) on its verse. That includes all 115 door quotes (87 evidence items + 28 الحكمة quotes). Words are the verse split on spaces, with pause-mark-only tokens (ۖ ۗ ۚ ۛ …) and ۞ removed. All 115 + 14 + الضياء ranges were checked against the text.
- **Moving to the Uthmani text** (DECISIONS.md #3). The app shows the Uthmani Hafs text. Map each range onto it by word position.
  - Tanzil simple and the Uthmani editions sometimes split words differently (for example «يَا أَيُّهَا» = 2 words vs «يَٰٓأَيُّهَا» = 1).
  - So the import step must match the words of the quote, not just the index. Compare a letter skeleton: remove diacritics, unify alif forms, ى/ي and ة/ه, and drop dagger alif and extra alif. Fail the build on any quote it cannot place, so a human can fix that entry.
  - Never cut or show a verse that failed this check.
- **Licences** (DECISIONS.md #5): record the source and licence of the text, page fonts, card font and every reciter in Settings → «مصادر النص والصوت».

## Lunar day (DECISIONS.md #6)
- Night 1 is the first evening after the astronomical new moon (conjunction) on which the moon sets after the sun at the user's location.
- It is computed on the device with the device location, or a chosen city as fallback. The location is never sent anywhere.

## Things the app must not do
- Show any text as Quran unless it comes from the verse data.
- Cut a verse, or show a quote that is not inside its verse.
- Show a Gregorian date on Home.
- Add hadith, tafsir or scholar references anywhere.
