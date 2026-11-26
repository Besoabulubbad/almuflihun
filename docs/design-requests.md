# Requests to the designer

What engineering still needs from the design. Design v5 (26 Nov 2026, `design/REQUESTS-ANSWERED.md`) answered every request except «أوقات التسبيح»; answered items were removed. When an item is designed, delete it here and note the design version that answers it.

## 1. «أوقات التسبيح» (waiting since v4)

The designer has not received the spec yet. It is [specs/screens.md → Home](specs/screens.md#1-home), section «أوقات التسبيح»: the windows, their verses, how each is computed, and the «من اللغة» honesty rule. Please design the day arc under the moon, each window with its full verse and the «من اللغة» label, and the sky tokens for night, dawn, day and dusk in both themes, with text contrast held on each.

## 2. Small fixes in DESIGN.md

- Five strings still use «·» next to numbers, against rule #22 and §8: «{range} · الصفحات a–b» (Hizb header), «الآية n · سورة» (AyahSheet title), «رد · مع آية» (answer overline), `[المقدّم] · {platform} · {how}` (LiveCard), «n آية · ص n» (surah rows). Replace with «،».

## Engineering notes on v5 (no action needed)

- Roots and lemmas: the source is QUL morphology (ADR 0012), credited and part of the permission request; the mockups' «بيانات مثال» counts will be computed from it.
- Motion tokens (`tokens.json → motion`) are generated into code with the rest of the tokens; detekt's `MotionTokensOnly` enforces them.
