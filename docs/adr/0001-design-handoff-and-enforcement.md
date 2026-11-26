# 0001 — The design handoff is binding, enforced with Sojourn's machinery

- **Status:** accepted
- **Date:** 2026-11-26
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** design-system, tooling

## Context and problem statement

The design for المفلحون was made in Claude Design and handed off as `design/` (v3): `DESIGN.md` (a strict spec), `tokens.json` (a closed token set, light and dark), `SCREENS.md`, `DECISIONS.md` (22 recorded decisions), 34 screens with dark samples, mockups, illustrations and the content data. The spec says no style value may exist outside the token set, and no screen may restyle a shared component. Its first version also named a mechanism: a `ui.theme` package, a grep check in CI and the `icons-lucide` library. Sojourn already enforces the same intent with a module, a token generator and detekt rules. We have to choose which mechanism this repo uses.

## Decision drivers

- The design's intent must be enforced by a tool, not by review alone.
- Reuse what is already proven in Sojourn (convention plugins, detekt rule set, screenshot tests).
- One source of truth for tokens; a token change is a JSON diff.

## Options considered

1. **Sojourn's machinery:** `core:designsystem` owns tokens and components; a `build-logic` task generates Kotlin from `design/tokens.json`; custom detekt rules forbid raw `dp`/`sp`, `Color(0x…)` and ad-hoc `TextStyle` outside it; Lucide icons converted to vector drawables.
2. **The original spec mechanism:** a `ui.theme` package, the hand-shipped `AppTokens.kt`, a grep in CI, and `com.composables:icons-lucide`.

## Decision outcome

**Chosen: Option 1**, approved by the owner (`design/DECISIONS.md` #8). `DESIGN.md` §0 now states the intent and leaves the mechanism to the project.

- `design/` is a verbatim copy of the v3 handoff and is the source of truth for every screen. Where a screenshot and `DESIGN.md` disagree, `DESIGN.md` wins; where data and a screenshot disagree, the data wins.
- `design/code/AppTokens.kt` is reference output only; the generator replaces it.
- A value the design does not define is a question to the designer, never an invented token.

### Consequences

- Good: detekt names the rule and the standard it breaks, which a grep cannot.
- Good: the token generator, contrast test and screenshot matrix come from Sojourn ready-made.
- Bad: the detekt rules must be adapted to this design's token names and to two grounds (night and paper) in two themes.
- Neutral: a later handoff replaces `design/` in one commit, and the diff shows what changed.

### Confirmation

- detekt rules in `tooling/detekt-rules` fail the build on raw style values outside `core:designsystem`.
- Konsist forbids Material imports outside `core:designsystem`.
- A unit test asserts every `quote`/`highlight` in `design/data` is a substring of its verse and that its `words` range points at the same words.

## Links

- `design/DESIGN.md` §0, `design/DECISIONS.md` #8
- Sojourn ADRs 0008 (generated tokens) and 0014 (static analysis)
