# 0018 — Design v7 is adopted as a UI change; decided product behaviour stands

- **Status:** accepted
- **Date:** 2026-11-28
- **Deciders:** Mohammad Abu Lubad (owner), Claude
- **Tags:** design, product, scope
- **Amends:** [0001](0001-design-handoff-and-enforcement.md) (the handoff is now `design/` v7; v5 is archived in `design/archive/v5/`), [0002](0002-first-apk-scope.md) (first-APK scope additions below), [0012](0012-study-engine.md) (entry points to word and ayah study)

## Context and problem statement

The designer delivered v7 («quran-community-design-v7»): four tabs (الذكر، المجتمع، القرآن، الإعدادات), a mini player everywhere, a new design system, widgets, a notification player and notifications. The owner's instruction: **v7 is a UI change; what we decided about the product, its behaviour and its data must not change. Conflicts come to the owner.** v7's own decision log (#37–#66) overrides earlier decisions in several places, so each was checked against the ADRs, the specs and the code.

## What was verified

- **Content data:** `abwab-4.json`, `dhikr-slider.json`, `diyaa-food.json`, `hizb-60.json` and the الضياء source are byte-for-byte the same as v5; the quote placement tests (#85, #87) stand. The illustrations are unchanged.
- **Search claims:** «يا ايها الذين امنوا» 89 ayahs in 20 surahs, «يا أيها الناس» 20, «يا أيها النبي» 13, «الصابرين» 14: all match the shipped text. v7's forgiving search is exactly the normalisation already built (#91).
- **«عباد» = 12 (11 + 1):** does not reproduce. The standalone word is 10 + 1 (الزخرف ١٩ with the small alif), 20 + 1 with prefixes; v7's example counts الفرقان ٦٣ («وَعِبَادُ», prefixed) among the 11. The app computes these counts; a note goes to the designer.
- **Board fonts** (`design/board/fonts`): Amiri Quran and IBM Plex Sans Arabic (OFL), for the board page only; no Mushaf page fonts.

## Decisions (owner, 2026-11-28)

| v7 | Decision |
|---|---|
| All fonts bundled; KFGQPC HAFS for verses outside the Mushaf; skeleton lines instead of a fallback font (v7 #14, #35, DESIGN-SYSTEM fonts) | **ADR 0013/0016 stand:** the exact page fonts are downloaded once; Digital Khatt is bundled for verses outside the Mushaf and as the page fallback while a font downloads. The designer restyles only. |
| Word tap: «استمع للكلمة» and «ابحث بجذرها» (#47); no word or ayah study in the AyahSheet (#46) | **Word tap is off for now.** Tapping anywhere on an ayah selects it and opens the AyahSheet (استمع، من هنا، احفظ، شارك، انسخ، «اسأل عنها في المجتمع», its doors). Word study and ayah study stay built in the engine (#104, #107); their entry points are decided later, together. «استمع للكلمة» stays hidden without a licensed word-audio source. |
| Daily «تلاوة» with «today's portion», a clock-time reminder and amounts ربع / نصف / حزب / جزء (#55, D2) | **As decided before:** the تلاوة is the hizb reading with **its own memory**, «read from where you left», moving **only when the reader reads** (nothing advances by date, no "behind"). Reminders are the two **sun-based** ones, morning «قبل طلوع الشمس» and evening «قبل الغروب», off by default. The unit stays the hizb. |
| One mini-player bar; no player sheet, no «استمع حتى», no sleep timer (#41) | **v7's bar, and play scope and the sleep timer stay** as small options (a long press on the bar or Settings, by design). |
| Search options عبارة / كلمة كاملة / جذر / مطابق (C7) | **Six modes:** default كلمة (forgiving, by letters), and كلمة كاملة، عبارة، جذر، **لفظ** (kept), مطابق (exact, tashkeel compared in a standard mark order). |
| Widgets (#62), notification player and lock screen (#63), moderators' inbox (#56, B5), live audience, topic and details (#45, B3) | **In the first APK** (amends ADR 0002's scope). |
| Name «مجتمع القرآن» and the mark ق (#37, #65) | [ADR 0019](0019-app-name.md). |
| Remote config, more events, A/B tests, zero-result search text (#51, #52, UX-STRATEGY §4) | [ADR 0020](0020-analytics-v7.md): all **except A/B tests**. |

Everything else in v7 (structure, tabs, the mini player, the design system, the screens and copy) is adopted as the UI. Where v7's text still says otherwise on the points above, these decisions win; `docs/design-requests.md` asks the designer to align the files.

## Consequences

- Good: one clear rule for every conflict; nothing built is thrown away; the designer has a precise list.
- Good: the token generator keeps building from `design/archive/v5/tokens.json` until Phase 5 rebuilds the design system on v7's tokens, so nothing breaks in between.
- Bad: the first APK grows (widgets, notification player, moderators' inbox, live details); `docs/plan.md` is updated.
