# `:tooling:detekt-rules`

The design's rules (`design/DESIGN.md`) and the Quran rules (`CLAUDE.md`) as detekt rules: a rule without enforcement is a wish. Loaded into every module by `almuflihun.detekt`; scopes in `config/detekt/detekt.yml` under `almuflihun:`.

| Rule | Enforces | Flags | Applies to |
|---|---|---|---|
| `TokensOnly` | DESIGN §0, §4, §5 | `Color(…)`; `.dp` / `.sp` / `.em` on a number literal (`0.dp` allowed) | `feature/*`, `core/ui` |
| `NoAdHocTextStyle` | DESIGN §3 | `TextStyle(…)`, `fontSize =` | `feature/*`, `core/ui` |
| `MotionTokensOnly` | DESIGN §11 | a number literal as `durationMillis` / `delayMillis` or `tween(…)`'s first argument | `feature/*`, `core/ui` |
| `StartEndOnly` | DESIGN §2, §7 | absolute (left/right) APIs, `.Left` / `.Right` / `.Absolute`, `scaleX = -1f` | `feature/*`, `core/ui` |
| `NoTextLiterals` | DESIGN §8, COPY.md | a literal, template or concatenation as the text of `Text`, `BasicText` or any `*Text` | `feature/*`, `core/ui` |
| `InsetsOnlyInOwners` | DESIGN §2 | `WindowInsets` and inset modifiers outside `AppScaffold`, `AppSheet`, `AyahSheet`, `PlayerBar` | `app/*`, `core/*`, `feature/*` |
| `NoQuranTextInCode` | DESIGN §1 | a string literal with Arabic diacritics or Quranic marks: verses come only from the verse data | `app/*`, `core/*`, `feature/*` |

Test source sets are exempt. `core:designsystem` is exempt from the token, text-style, motion and direction rules.

- Tests: `./gradlew :tooling:detekt-rules:test` (a failing and a passing snippet per rule).
- detekt runs the rules on its own Kotlin 2.0 runtime, so this module compiles with `apiVersion = 2.0`.
- After changing a rule, run `./gradlew --stop` before checking it against the repository (detekt caches its classloader per daemon).
