# Architecture decision records

Every significant decision about this codebase, in [MADR](https://adr.github.io/madr/) format: the context, the options actually considered with their trade-offs, the choice, and its consequences. An ADR is immutable once accepted; a change of mind is a new ADR that supersedes it.

**Rule:** code that contradicts an accepted ADR is a defect, whoever wrote it. If the ADR is wrong, fix the ADR first.

Design and content decisions made with the designer live in [`design/DECISIONS.md`](../../design/DECISIONS.md); the ADRs here link to them rather than repeat them.

## How to propose one

1. Copy [`0000-template.md`](0000-template.md) to `NNNN-short-kebab-title.md` with the next number.
2. Status `proposed`. Fill every section; "Options considered" must contain at least two real alternatives with honest negatives.
3. Open a PR titled `docs(adr): propose NNNN <title>`. The discussion lives on the PR.
4. On merge, status becomes `accepted` and the row below is added.

## Index

| # | Title | Status | Date |
|---|---|---|---|
| [0001](0001-design-handoff-and-enforcement.md) | The design handoff is binding, enforced with Sojourn's machinery | accepted | 2026-11-26 |
| [0002](0002-first-apk-scope.md) | The first APK ships every designed area, including Live and Community | accepted | 2026-11-26 |
| [0003](0003-firebase-free-plan.md) | Firebase on the free Spark plan, with no server code | superseded by 0005 | 2026-11-26 |
| [0004](0004-quran-assets-in-repo.md) | Quran rendering assets come from QUL and are committed to the repo | accepted | 2026-11-26 |
| [0005](0005-firebase-blaze-push-notifications.md) | Firebase on Blaze with Cloud Functions for push notifications | accepted | 2026-11-26 |
| [0006](0006-community-users-and-permissions.md) | Community: review by default, trust granted by moderators, roles as data | accepted | 2026-11-26 |
| [0007](0007-recitation-audio-quran-foundation.md) | Recitation audio and timings from the Quran Foundation API, through our backend | accepted | 2026-11-26 |
| [0008](0008-analytics-and-crash-reporting.md) | Opt-in, purpose-limited analytics and crash reporting with Firebase | accepted | 2026-11-26 |
| [0009](0009-app-identity.md) | App identity: `almuflihun`, application ID `app.almuflihun` | accepted | 2026-11-26 |
| [0010](0010-self-updating-apk.md) | The directly distributed APK updates itself | accepted | 2026-11-26 |
| [0011](0011-architecture-from-sojourn.md) | Architecture and engineering setup reused from Sojourn | accepted | 2026-11-26 |
| [0012](0012-study-engine.md) | A Quran study engine: collections, advanced search, word study, related verses | accepted | 2026-11-26 |
| [0013](0013-mushaf-fonts-download.md) | The Mushaf: exact QPC page fonts downloaded once, Digital Khatt bundled as the fallback | accepted | 2026-11-27 |
| [0014](0014-language-ready.md) | Arabic first, built ready for English | accepted | 2026-11-27 |

## How these were decided

Question-and-answer rounds with the owner, one decision at a time, each with options, trade-offs and a recommendation, starting 2026-11-26.
