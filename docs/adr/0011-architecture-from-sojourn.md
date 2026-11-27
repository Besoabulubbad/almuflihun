# 0011 — Architecture and engineering setup reused from Sojourn

- **Status:** accepted
- **Date:** 2026-11-26
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** architecture, build, testing, ci
- **Amended by:** [0014](0014-language-ready.md) (language-ready, English later)

## Context and problem statement

The owner built Sojourn (`~/AndroidStudioProjects/TheFlexGlobal`), a public KMP showcase whose architecture, build logic, quality gates, CI and docs are proven. المفلحون is a larger product (Quran rendering, astronomy, audio, Firebase-backed community and admin), Arabic-only, Android-first. We decide whether to reuse Sojourn's setup or start lighter.

## Options considered

1. **Adopt Sojourn's setup**, adapted to this product.
2. **A lighter setup:** one module per layer, no iOS screenshot tests.

## Decision outcome

**Chosen: Option 1** (owner). Each item follows the Sojourn ADR named in brackets unless noted.

| Area | Decision |
|---|---|
| Platform | KMP, one shared Compose Multiplatform UI for Android and iOS; iOS is built and tested, not distributed (Sojourn 0001) |
| Modules | `core/*` + `feature/*` + thin `app/*`, `build-logic` convention plugins (`almuflihun.*`) (Sojourn 0003). Features: `home`, `moon`, `quran-hub`, `mushaf`, `hizb`, `doors`, `diyaa`, `saved`, `live`, `community`, `profile`, `admin`, `settings`. New core modules: `astronomy`, `quran` (text, layout, search, verse rules), `audio`, `firebase` (seam over GitLive), `analytics` (allow-listed wrapper, ADR 0008), `update` (ADR 0010) |
| DI | Koin with graph verification (Sojourn 0004) |
| Navigation | Navigation 3, keys in `core:navigation`, entry providers per feature (Sojourn 0005) |
| Presentation | MVI: one ViewModel per screen, `Route` + stateless `Screen`, `ScreenState<T>` (Sojourn 0006) |
| Data | SQLDelight on the device is the source of truth for everything local; Firestore only for Community, Live, Admin, synced Saved, `app/latest` (Sojourn 0007, adapted) |
| Design system | Tokens generated from `design/tokens.json`, two grounds × two themes; components built once in `core:designsystem` (ADR 0001, Sojourn 0008) |
| Language | Arabic only. The `AppLocale` machinery stays for direction, Arabic-Indic digits, plurals and bidi isolation, without language switching (Sojourn 0009, adapted) |
| Tests | Unit, Konsist, Roborazzi screenshots on Android and iOS (light/dark × 100%/130% font × two widths), Compose UI tests, Maestro end-to-end, plus Firebase emulator tests for rules and functions (Sojourn 0010, extended) |
| CI/CD | GitHub Actions: PR, main (with iOS), release (direct and play flavours, ADR 0010), docs, functions; Conventional Commits; release-please (Sojourn 0011) |
| Docs | MkDocs Material + Dokka on GitHub Pages; ADRs (Sojourn 0012) |
| AI | `CLAUDE.md` contract, Context7 and GitHub MCP, automated Claude PR review, the analytics-analyst agent (Sojourn 0013) |
| Hygiene | detekt with custom rules, Konsist, dependency-analysis, Renovate (Sojourn 0014) |

### Consequences

- Good: proven foundations; Phase 1 is mostly adaptation, not invention.
- Good: rules are enforced by tools from the first module.
- Bad: more modules and plugins than a small app needs at first; justified by Community and Admin size.
- Neutral: versions are re-verified for this project in `docs/tech-stack.md`, not copied blindly.

## Links

- Sojourn ADRs 0001, 0003–0014 (`~/AndroidStudioProjects/TheFlexGlobal/docs/adr/`)
- ADRs [0001](0001-design-handoff-and-enforcement.md), [0005](0005-firebase-blaze-push-notifications.md), [0008](0008-analytics-and-crash-reporting.md), [0010](0010-self-updating-apk.md)
