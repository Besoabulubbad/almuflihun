# CLAUDE.md: working in مجتمع القرآن

This file is the contract between the codebase and any AI agent (Claude Code, the PR-review action, or a contributor's assistant). Read it fully before changing anything. Humans: it is also the shortest accurate summary of how this repo works.

## What this is

«مجتمع القرآن» (`qurancommunity`, ADR 0019, ADR 0021) is an open-source, non-profit, **Arabic-only, Quran-only** Kotlin Multiplatform app with one shared Compose Multiplatform UI (Android first; iOS is built, not distributed). It has a printed-page Mushaf with recitation, curated doors of verses, الضياء (food in the Quran), the moon and the Quranic times of tasbih computed on the device, and a moderated community. It is a production app used by real people: the bar is [docs/guides/quality-bar.md](docs/guides/quality-bar.md).

## Before you change anything

1. Read [docs/plan.md](docs/plan.md) and the ADR index [docs/adr/README.md](docs/adr/README.md). Accepted ADRs are binding; if a change contradicts one, stop and propose a superseding ADR.
2. Read the design (v7, ADR 0018): `design/DESIGN-SYSTEM.md` (strict), `design/SCREENS.md`, `design/DECISIONS.md`, `design/UX-STRATEGY.md`, `design/ICONS.md`, `design/COPY.md`, and our per-screen decisions in [docs/specs/screens.md](docs/specs/screens.md). The design is the UI; product decisions in the ADRs win over it (ADR 0018–0020). The design wins over screenshots; data wins over both.
3. For any library, follow its **official documentation for the version pinned** in `gradle/libs.versions.toml`. Use Context7 (`.mcp.json`) for version-exact docs. Do not rely on memory for APIs.

## Non-negotiable rules

- **Quran text only from the verse data.** Never type, paraphrase or truncate a verse. A quote appears only with its full verse. Every quote and highlight is placed by its word range and checked by a build-time test; a broken quote fails the build.
- **The Quran is the only source.** No hadith, tafsir, or people cited as authorities. Three labelled levels: the text, «من اللغة», «تأمّل» (DESIGN §1).
- **Tokens by name only.** No raw `dp`/`sp`, `Color(0x…)`, `TextStyle(` or durations outside `core:designsystem`. A value missing from `design/tokens.json` is a question for the designer, never an invented token.
- **Material stays inside `core:designsystem`.** Screens compose the design system's components and never restyle them.
- **Arabic, right to left.** Start/end, never left/right. Arabic-Indic digits. «،» separates meta parts, never «·». Copy follows `design/COPY.md` and DESIGN §8: no exclamation marks, no emoji, no guilt, no streaks.
- **Every screen models every state** (content, loading, empty, error, offline, no-location, signed-out, each permission) exhaustively.
- **Privacy.** Analytics only through the allow-listed wrapper and only events in [docs/analytics/tracking-plan.md](docs/analytics/tracking-plan.md), after consent (ADR 0008). No personal data in events or logs. Never add an SDK that sends data without an ADR.
- **Security.** Firestore rules and Cloud Functions enforce permissions; the client only shows or hides controls (ADR 0006). No secret in the repo or the APK: the Quran Foundation secret lives only in Firebase Secret Manager (ADR 0007); the signing key only in GitHub Actions secrets.

## Commands

```bash
./gradlew :app:android:assembleDirectDebug            # the APK handed out (flavour `direct`, ADR 0010)
./gradlew :app:android:assemblePlayDebug              # the future Play build (no self-update)
./gradlew detekt                                      # static analysis
./gradlew :app:shared:linkDebugFrameworkIosSimulatorArm64   # iOS framework (needs Xcode)
./gradlew check buildHealth                           # what CI runs on every PR (.github/workflows/pr.yml)
```

Convention plugins live in `build-logic/` (`qurancommunity.*`); versions in `gradle/libs.versions.toml`.

## Conventions

- Kotlin official style; detekt is the single linter (from Phase 1).
- Package root `app.qurancommunity` (ADR 0021). Modules per ADR 0011.
- Conventional Commits (`feat(mushaf): …`); scope = module name without `feature:`/`core:`. Agent-authored commits carry a `Co-Authored-By` trailer.
- Every PR that adds or changes a screen meets the quality bar and states which design screen it implements.

## Documentation duties

- New library, pattern or reversal of a decision → an ADR (`docs/adr/`, copy `0000-template.md`).
- Per-screen behaviour agreed with the owner → `docs/specs/screens.md`.
- Anything the design does not cover → `docs/design-requests.md`, never an invented answer.
- New third-party asset → `THIRD_PARTY.md` and the in-app «مصادر النص والصوت».
- New analytics event → the tracking plan, reviewed by the `analytics-analyst` agent.

## Do not

- Do not reuse code from quran_android (GPL-3.0); behaviour reference only.
- Do not commit secrets, `local.properties`, keystores or service-account files.
- Do not record new screenshot baselines to make CI pass without a reviewed visual change.
