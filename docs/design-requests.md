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
