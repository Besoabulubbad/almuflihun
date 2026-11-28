# مجتمع القرآن: design handoff v7

This package replaces v6 and the v5 package (المفلحون). v7 is the full system: four tabs (الذكر، المجتمع، القرآن، الإعدادات), a mini player everywhere, the Quran engine with spelling-aware search, daily تلاوة, home-screen widgets, the notification player and notifications. It is built to grow and measured after release.

## Read in this order

1. **`DESIGN-SYSTEM.md`**: the strict rules, colours, type, spacing, components and patterns. Everything visual comes from here.
2. **`tokens.json`** and **`code/AppTokens.kt`**: the values. No raw values in screens.
   **`ICONS.md`**: every icon by place. **`brand/`**: the mark ق, app icons, splash, notification icon, store graphics (rules in DESIGN-SYSTEM §11).
3. **`SCREENS.md`**: every screen, by code, with its content and behaviour.
4. **`screens/*.png`**: one image per screen code (A1–F1, widgets and notifications E1–E5). `board/index.html` shows them all with notes.
5. **`UX-STRATEGY.md`**: core tasks and targets, the four uncertain places, and **the plan to keep improving after release**. Analytics, flags and experiment slots in it must be built from day one.
6. **`DECISIONS.md`**: v5 decisions (data, Firebase, fonts, licences, sign-in, analytics) still stand; v6 decisions #37–52 and v7 decisions #53–64 override anything about look and navigation; where v6 and v7 differ, v7 wins. #52 is open for the owner.

## Content data (unchanged from v5)

`data/`:
- `abwab-4.json`: doors, with word ranges.
- `dhikr-slider.json`: تذكرة, 14 items.
- `hizb-60.json`
- `diyaa-food.json` and its source.

`illustrations/` holds the v5 drawings for الضياء; they will be redrawn in the v7 palette.

## Rules that matter most

- Every verse is shown in full. Quotes are highlighted by word ranges, never cut.
- Tanzil simple text is the search base and stays untouched; normalisation runs on a copy.
- KFGQPC text and font for verses outside the Mushaf. QPC page fonts for Mushaf pages. All fonts bundled.
- Sample content is marked «بيانات مثال». Unknown values show «لا نعرف».
- The mini player is the only in-app player; the system notification player mirrors it. The ayah sheet is the same everywhere.
- Four tabs only. New things go inside a tab.

## What is real in the screens

**Real:**
- The verse texts.
- The page 23 layout, hizb 3 (pages 22–31), and hizb 4 starting at 2:203 on page 32.
- Surah ayah counts and start pages.
- The search counts:
  - «يا ايها الذين امنوا»: 89 ayahs in 20 surahs
  - «يا أيها الناس»: 20
  - «يا أيها النبي»: 13
  - «الصابرين»: 14
  - «عباد»: 12 places, 11 written with alif and 1 with the small alif (الزخرف ١٩)
- The door counts and their word ranges.
- The moon phase, calculated on the day of viewing.

**Examples:** names, posts, lives, videos, times and collection counts.
