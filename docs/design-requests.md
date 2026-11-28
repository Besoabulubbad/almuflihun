# Requests to the designer

What engineering still needs from the design. Design v5 (26 Nov 2026, `design/REQUESTS-ANSWERED.md`) answered every request except «أوقات التسبيح»; answered items were removed. When an item is designed, delete it here and note the design version that answers it.

## 1. «أوقات التسبيح» (waiting since v4)

The designer has not received the spec yet. It is [specs/screens.md → Home](specs/screens.md#1-home), section «أوقات التسبيح»: the windows, their verses, how each is computed, and the «من اللغة» honesty rule. Please design the day arc under the moon, each window with its full verse and the «من اللغة» label, and the sky tokens for night, dawn, day and dusk in both themes, with text contrast held on each.

## 2. Small fixes in DESIGN.md

- Five strings still use «·» next to numbers, against rule #22 and §8: «{range} · الصفحات a–b» (Hizb header), «الآية n · سورة» (AyahSheet title), «رد · مع آية» (answer overline), `[المقدّم] · {platform} · {how}` (LiveCard), «n آية · ص n» (surah rows). Replace with «،».

## Engineering notes on v5 (no action needed)

- Roots and lemmas: the source is QUL morphology (ADR 0012), credited and part of the permission request; the mockups' «بيانات مثال» counts will be computed from it.
- Motion tokens (`tokens.json → motion`) are generated into code with the rest of the tokens; detekt's `MotionTokensOnly` enforces them.

## 3. The Mushaf download and editions (ADR 0013, 2026-11-27)

- **First-launch download** of the exact Mushaf (≈94 MB), right after onboarding: what it is, size, progress, pause, the mobile-data question (shown once with the size), not-enough-space, and failure with retry. The rest of the app stays usable meanwhile.
- **Fallback note** on a Mushaf page drawn with Digital Khatt while its exact font downloads: «يُحمَّل المصحف المطابق للطبعة» (quiet, never blocking).
- **Settings → «شكل المصحف»:** editions (مصحف المدينة ١٤٢١ default; مصحف التجويد الملوّن ١٤٤١ optional download), each with size, status, download, delete. The tajweed edition has light, dark and sepia palettes.
- **Card font change:** Quran text on cards, the slider, the study panel and posts uses **Digital Khatt V2** (bundled, open licence) instead of KFGQPC HAFS Uthmanic Script, so cards never wait for a download.

## 4. Four quotes in الضياء have no verse reference (2026-11-27)

`design/data/diyaa-food.json`, station 9, block `compare`: the two items carry `quotes` without a `ref`, so the app cannot place them or show them with their full verse (DESIGN §1). One of them («من طيبات ما رزقناكم») occurs in four verses (2:57, 2:172, 7:160, 20:81). Please add the verse key to each quote, for example `{"key": "2:61", "quote": "…", "words": [a, b]}`.

Until then the build lists these four as known gaps (`ContentPlacementTest`), and the screen must not show them.

## 5. Search results «حسب الرسم» (owner, 2026-11-27)

Advanced search can group results by how the Mushaf writes the word (docs/specs/screens.md §13): for example «إِبْرَٰهِيمَ» 54 and «إِبْرَٰهِۦمَ» 15. Please design the switch between «حسب السورة» and «حسب الرسم» on SearchAdvanced and the shape group header (the Mushaf spelling in the Quran font, with its count). The engine is ready.

## 6. Align the v7 files with the owner's decisions (ADR 0018, 2026-11-28)

v7 is adopted as the UI. Where its files say otherwise on these points, the owner's decisions win; please update `DESIGN-SYSTEM.md`, `SCREENS.md`, `DECISIONS.md` and the affected screens:

1. **Fonts (ADR 0013/0016 stand):** verses outside the Mushaf use **Digital Khatt** (bundled), not KFGQPC HAFS. Mushaf page fonts are **downloaded once**, not bundled; while a page's font downloads, the page is drawn in Digital Khatt with the quiet note «يُحمَّل المصحف المطابق للطبعة» (not skeleton lines). Replaces v7 #14, #35 and DESIGN-SYSTEM's font section. A small screen for the download (size, Wi-Fi / mobile data, progress) is still needed (request §3).
2. **Word tap is off for now.** C3 is not built. Tapping anywhere on an ayah selects it and opens the AyahSheet as in C2. Word study and ayah study come back later, with their entry points designed together.
3. **«تلاوة» keeps the decided behaviour:** its own memory, «read from where you left», moving only when the reader reads (no daily schedule, no "behind"). The unit is the **hizb** (no ربع / نصف / جزء choice). Reminders are the two **sun-based** ones, morning «قبل طلوع الشمس» and evening «قبل الغروب», off by default, not a clock time. Please adjust A1 (the «٥:٣٠ ص» time), C4 and D2.
4. **Player:** v7's mini bar, plus **play scope** («استمع حتى»: end of the surah, hizb, quarter, or a chosen ayah) and the **sleep timer** as small options (a long press on the bar or a row in Settings, your choice).
5. **Search options:** six types: كلمة (forgiving, default), كلمة كاملة، عبارة، جذر، **لفظ**، مطابق.
6. **Spelling forms example:** «عباد = 12 (11 + 1)» does not reproduce. The standalone word gives 10 + 1 (الزخرف ١٩ with the small alif); with prefixes 20 + 1; v7 lists الفرقان ٦٣ («وَعِبَادُ», prefixed) among the 11. The app computes the real counts; only the sample needs fixing.
7. **Word ranges** in `data/*.json` count words in the **Tanzil simple text** (pause marks and the basmala prefix removed), as the build checks them, not in KFGQPC word order (DESIGN-SYSTEM §highlight). The app maps them onto the Mushaf's words.
8. **Name:** «مجتمع القرآن» is now the app's name (ADR 0019).
