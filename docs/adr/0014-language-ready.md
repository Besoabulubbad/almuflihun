# 0014 — Arabic first, built ready for English

- **Status:** accepted
- **Date:** 2026-11-27
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** localization, architecture
- **Amends:** [0011](0011-architecture-from-sojourn.md) (the "Arabic only, no language switching" row)

## Context and problem statement

The first APK is Arabic only (design DECISIONS #7). The owner said the app will be extended to English at a later stage. Choices made now (a hard-coded right-to-left direction, Arabic-Indic digits written into formatters, copy outside resources, Arabic-only data shapes) would make that a rewrite. Doing it right now costs little.

## Decision drivers

- English must be a configuration and content change, not a code rewrite.
- No cost to the Arabic first release in polish or schedule.
- Quran text must stay Arabic and right to left in any UI language.

## Options considered

1. **Language-ready now:** direction, digits and resources follow an `AppLanguage`; Arabic is the only one offered.
2. **Arabic hard-coded now**, refactor when English comes.

## Decision outcome

**Chosen: Option 1.**

- `AppLanguage` in `core:localization` (Arabic, English); `offered` lists Arabic only until English is ready. The theme takes `LayoutDirection` from it; nothing hard-codes right to left (detekt `StartEndOnly` already forbids left/right).
- **Strings** live in resources with keys, never in code (detekt `NoTextLiterals`); Arabic is the default resource set, English is added as another set.
- **Formatting** (digits, numbers, dates, verse references, separators) goes through `core:localization` formatters that take the language: Arabic-Indic digits and «،» in Arabic, Western digits and "," in English.
- **Quran text** is always Arabic and right to left, in its own text components, whatever the UI language (the verse components set their direction explicitly).
- **Content** (door titles, الضياء text, push copy) is data with an Arabic field today; its shape allows a second language later without changing the code that reads it.
- **Design**: English needs its own pass (Latin UI font, left-to-right layouts, copy). It is a design request when English is scheduled, not now.
- The skeleton of `core:localization` lands now; its formatters arrive with Phase 2's localization work.

### Consequences

- Good: English later is resources, content and a design pass, not a refactor.
- Good: tests can render screens in both directions early to catch layout bugs.
- Bad: a little more indirection today (direction and digits come from a language value).
- Neutral: the screenshot matrix stays Arabic-only until English is offered.

### Confirmation

- No `LayoutDirection.Rtl` literal outside `core:localization` and the Quran text components (code review; a Konsist rule when the components land).
- Formatter tests cover both languages.

## Links

- `design/DECISIONS.md` #7; ADR [0011](0011-architecture-from-sojourn.md)
