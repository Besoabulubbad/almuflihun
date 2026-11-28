# Plan

How المفلحون goes from an empty Kotlin Multiplatform skeleton to the first signed APK handed to people. Phases are ordered by dependency. Each has an **exit criterion** that can be seen in the repository (a green workflow, a passing test, a measured number), not a feeling of done.

The order is deliberate: **foundations → Quran and astronomy engines → design system → offline screens → backend → audio → online screens → release**. Everything that works offline comes first, so it can be used and tested early; the networked parts build on a finished base. Two risky questions are answered by **spikes** before the phases that depend on them.

## What this product is

An open-source, non-profit, Arabic-only, Quran-only app for the owner's community: a printed-page Mushaf with recitation, curated doors of verses, a journey through food in the Quran, the moon and the Quranic times of tasbih computed for the user's place, and a moderated community. The design in `design/` is binding (ADR 0001); the quality bar in [guides/quality-bar.md](guides/quality-bar.md) applies to every screen.

## Decisions this plan rests on

All taken on 2026-11-26, one question at a time; each has an ADR. Per-screen decisions are in [specs/screens.md](specs/screens.md).

| Area | Decision | ADR |
|---|---|---|
| Design | `design/` (v7) binding as the UI; decided product behaviour stands | [0001](adr/0001-design-handoff-and-enforcement.md), [0018](adr/0018-design-v7.md) |
| Scope | First APK ships every designed area, including Live, Community, Admin, plus widgets, the notification player, the moderators' inbox and live details (v7) | [0002](adr/0002-first-apk-scope.md), [0018](adr/0018-design-v7.md) |
| Quran assets | QUL page fonts, layout and data committed to the repo, credited | [0004](adr/0004-quran-assets-in-repo.md) |
| Backend | Firebase on Blaze, Cloud Functions, push for live, posts, replies, updates, announcements | [0005](adr/0005-firebase-blaze-push-notifications.md) |
| Community | Review by default, trust granted by moderators, roles as data, points and grades | [0006](adr/0006-community-users-and-permissions.md) |
| Audio | Quran Foundation API through our backend; علي جابر first | [0007](adr/0007-recitation-audio-quran-foundation.md) |
| Analytics | Opt-in, purpose-limited Firebase Analytics and Crashlytics; remote settings and zero-result searches with consent; no A/B tests | [0008](adr/0008-analytics-and-crash-reporting.md), [0020](adr/0020-analytics-v7.md) |
| Identity | Shown as «مجتمع القرآن»; codename `almuflihun`, application ID `app.almuflihun`, MIT licence | [0009](adr/0009-app-identity.md), [0019](adr/0019-app-name.md) |
| Updates | The direct APK updates itself; `play` flavour for later | [0010](adr/0010-self-updating-apk.md) |
| Architecture | Sojourn's setup, adapted | [0011](adr/0011-architecture-from-sojourn.md) |
| Study engine | Collections, advanced search, word study, related verses, study panel, surah overview | [0012](adr/0012-study-engine.md) |

## Phases

### Phase 0: repository and documentation

- `git init` (done), `.gitignore`, the MIT `LICENSE` (done), `THIRD_PARTY.md`.
- **Public GitHub repository `almuflihun`** on the owner's account; branch protection on `main` (PR only, required checks, linear history).
- `README.md`, `CLAUDE.md` (the agent contract), `CONTRIBUTING.md`, PR template with the quality-bar checklist, issue templates, `CODEOWNERS`, `.editorconfig`, `.mcp.json` (Context7, GitHub).
- Docs: architecture overview, modules, tech stack with versions verified on the day, guides (local setup, adding a feature, testing, release), the ADRs, specs, research, the tracking plan.
- `mkdocs.yml` and `docs.yml` publishing the site to GitHub Pages; `claude-review.yml` reviewing every PR against `CLAUDE.md`, the ADRs and the quality bar.

**Exit:** the repository is public, the docs site is live from `main`, and the first PR received an automated review.

### Phase 1: build system and quality gates

- `build-logic` convention plugins (`almuflihun.kmp.library`, `.kmp.feature`, `.compose`, `.android.application` with `direct` and `play` flavours, `.koin`, `.sqldelight`, `.screenshot`, `.detekt`, `.dokka`, `.tokens`).
- Version catalog verified against official sources; Gradle and AGP brought to the newest pair inside Kotlin's documented window.
- detekt with Compose rules and the custom rule set adapted to this design's tokens; Konsist module and naming rules; dependency analysis; Renovate.
- Empty skeletons for every module in ADR 0011, each with a README.
- `pr.yml` (detekt, Konsist, unit tests, screenshots, dependency health, assemble) and `main.yml` (adds the iOS build).

**Exit:** `./gradlew check` is green in CI; a deliberate rule violation on a branch fails the PR; iOS compiles on the macOS runner.

### Spike A: the Mushaf (in parallel with Phase 1)

Render pages 1, 2, 50, 300 and 604 from the committed QPC V2 fonts and QUL layout in Compose: justified and centred lines, surah headers, the basmala, ayah markers, tap to select a word and an ayah, the dark theme. Measure the time to draw a page and the APK size with all 604 fonts.

**Exit:** a written result in `docs/research/` with the numbers. If a budget in the quality bar is missed, the approach is revised before Phase 3.

### Spike B: recitation timings (needs the owner's Quran Foundation credentials)

Through a temporary local call (the secret stays on the owner's machine), fetch علي جابر's chapter files and `segments` for surahs 1 and 2 in pre-live. Compare with QUL 404's timings and the files' durations.

**Exit:** a written result: Quran Foundation segments usable, or the ADR 0007 fallback applies.

### Phase 2: core foundations

- `core:model`, `core:common` (dispatchers, clock, results, logging).
- `core:localization`: Arabic only; Arabic-Indic digits, plurals, bidi isolation, the reference formatter («سورة البقرة ١٧٢», ranges «١٤٣–١٤٤», «،» separators).
- Token generator → `core:designsystem` theme: night and paper grounds, light and dark, the text roles, fonts (IBM Plex Sans Arabic, KFGQPC HAFS Uthmanic Script, the surah-name font), Lucide icons as vectors, a token contrast test.
- `core:ui` (`ScreenState`, the ViewModel base, the scaffold that owns insets), `core:navigation` (Navigation 3 keys and serializers), `core:platform` (location, haptics, share, local notifications, reduced motion, opening other apps), `core:testing`.
- `core:analytics`: the allow-listed wrapper and consent gate (ADR 0008), with its tests.

**Exit:** the theme renders every token in both themes and both grounds in screenshot tests; contrast passes; formatting tests pass; the analytics wrapper drops anything outside the plan.

### Phase 3: the Quran and content engine

- `core:quran`: Tanzil `quran-simple.txt` (unmodified), the King Fahd Complex Hafs text, QPC V2 fonts, QUL layout and words, each with source, date and checksum in `SOURCES.md` (ADR 0004).
- Read-time normalisation (basmala, ۞, pause marks); the letter-skeleton matcher that places every quote and highlight in the shipped text by its word range; the offline search index.
- Content loaded from `design/data`: doors, slider, hizbs, الضياء.
- **Build-time tests:** every verse key exists; every `quote`, `highlight` and `words` range is placed exactly; a failure fails the build.
- **Station 6:** recount palm and grapes from the text with a written definition; publish `docs/content/station-6-counts.md`; set the verified numbers. Done 2026-11-27: the delivered numbers (20, 11, 6, 3, 1) are verified and checked on every build.

**Exit:** all 115 door quotes, 14 slider quotes and every الضياء highlight are placed in the shipped text by test; the station 6 document exists.

### Phase 3b: the study engine (ADR 0012)

- Surah metadata (Tanzil `quran-data`) and QUL morphology (roots, lemmas, stems) committed with `SOURCES.md`, aligned to the layout's words by test.
- Build-time indexes in the bundled database: roots, lemmas, normalised words, phrases (for related verses).
- `core:quran` query API for advanced search (and/or/not, scope, grouping, counts), word study, related verses, surah overview.
- Collections in `core:data` (device + sync), replacing the flat Saved list.

**Exit:** reference counts for chosen roots match; every layout word has morphology or a documented exception; whole-Quran queries within 300 ms.

### Phase 4: astronomy

- `core:astronomy`: the engine (Astronomy Engine or a common-code port, decided by checking its multiplatform support), moon phase, lit fraction, distance, rise and set with azimuths and altitude, sun times and twilight, the lunar-night rule (ADR via `design/DECISIONS.md` #6), the notable moments (crescent night, eclipses visible from the place, closest and farthest full moon, planet meetings, winter and harvest full moons), and the «أوقات التسبيح» windows.
- The offline GeoNames city list with Arabic names (credited).
- Tests against published reference values for several cities, seasons and latitudes.

**Exit:** reference tests pass within stated tolerances (e.g. rise and set within 1 minute).

### Phase 5: design-system components

Rebuilt on **design v7** (ADR 0018): the token generator moves from `design/archive/v5/tokens.json` to v7's `design/tokens.json`, then every component in `design/DESIGN-SYSTEM.md` §6 and §10 (TabBar, MiniPlayer with play scope and sleep timer, AyahSheet, VerseCard, SearchField and the six search types, spelling forms, TilawaCard, WeekStrip, MoonRow, SectionTiles, PostCard, LiveRow and LiveBanner, ModeratorInbox, the الضياء blocks, Admin form parts, banners, prompts, widget layouts), each with KDoc, previews and the screenshot matrix (light/dark × 100%/130%/200% × two widths).

**Exit:** every component's screenshots approved; Konsist confirms no Material outside `core:designsystem`.

### Phase 6: offline screens

The v7 structure (ADR 0018): four tabs, each with its own back stack, and the mini player everywhere. In this order, each a PR that meets the quality bar: the tab shell and mini player; الذكر (تلاوة card with its own memory and the sun-based reminders, the week, the moon row, تذكرة, section tiles); the Moon page; القرآن (search with the six types and spelling forms, continue reading, the index, مجموعاتي and a collection); the Reader (printed pages, pinch to zoom, full screen, the AyahSheet; word tap off for now); ادرس and the door pages, «كل الآيات», الحكمة; الضياء (hub and 11 stations); الإعدادات (local parts, وقتي مع القرآن); first launch (F1). «أوقات التسبيح» lands when designed. Word study and ayah study wait for their entry points.

**Exit:** all offline screens merged; the app is usable without any network.

### Phase 7: Firebase backend

- The owner creates the Firebase project `almuflihun` on Blaze with a budget alert; Google sign-in; the API key restricted to the package and signing fingerprint.
- Firestore schema and **security rules** with emulator tests (users, posts, threads, reviews, reports, points log, roles, trust grants, saved, lives and schedules, videos, feedback, `app/latest`).
- Cloud Functions (TypeScript, tested in the emulator): permission claims, points and grades, review-queue and report pushes, post and reply pushes, scheduled live pushes, announcements with rate limits, account deletion, update push.
- The owner-bootstrap script (sets the owner role once).

**Exit:** rules and functions tests pass in CI; a deploy from CI succeeds; a member cannot publish, edit others' content, set points or set roles (tests).

### Phase 8: audio

- The Quran Foundation secret goes into Firebase Secret Manager (the owner runs one command); the proxy function (allow-list, token cache, rate limits) and Content Sync.
- `core:audio`: the player, ayah and word timing, «استمع للآية», «استمع من هنا», hizb playback, speed, repeat an ayah or a range, background playback with media controls, surah downloads into private storage, credits.

**Exit:** a surah streams and plays offline after download with the playing-word highlight (or the ayah-only fallback from Spike B); the secret never appears in the APK (CI check).

### Phase 9: online screens

Live (TikTok schedules, reminders) and videos (YouTube in-app), Community (compose, verse picker, threads, states, like, save, report, best answer), profiles and grades, Admin (review, reports, users and trust, roles, lives and videos, announcements, feedback), synced Saved, notification settings and the permission pre-prompt, «رأيك».

**Exit:** all online screens merged; Maestro flows for posting, review, report and live reminder pass on CI emulators.

### Phase 10: analytics, self-update and release

- Analytics consent and events per the tracking plan, reviewed by the analytics-analyst agent; Crashlytics.
- The self-update flow (ADR 0010) and `app/latest`; the required-update screen.
- `release.yml`: signed `direct` APK attached to a GitHub Release, `app/latest` written, update push; release-please; the signing key in GitHub secrets plus the owner's offline backup.
- Macrobenchmark and baseline profiles against the quality-bar budgets; TalkBack pass; 130% font sweep.

**Exit:** `v1.0.0` is built and published by the pipeline alone, and the release checklist below is complete.

## Release checklist for the first handout

- [ ] Moderators named and given their roles; the owner account bootstrapped.
- [ ] Permission requests sent to the King Fahd Complex and Tarteel (ADR 0004).
- [ ] Quran Foundation production access approved.
- [ ] Station 6 counts verified from the text.
- [ ] Budget alert set; Firestore rules deployed from `main`.
- [ ] Credits screen lists every source and licence; privacy policy published.
- [ ] Signing key backed up offline by the owner.
- [ ] Every screen meets the quality bar.

## Owner actions

| When | Action |
|---|---|
| Now | Send the designer `docs/design-requests.md` and `docs/specs/screens.md` |
| Phase 0 | Approve creating the public GitHub repository |
| Spike B | Run one local command with the Quran Foundation credentials (the secret never leaves the owner's machine) |
| Phase 7 | Create the Firebase project on Blaze (card on file) and set the budget alert |
| Phase 10 | Name the moderators; keep the signing-key backup |

## Out of scope for the first APK

iOS distribution, الفرقان, «من نحن وكيف نفهم ديننا», «لماذا تقرأ القرآن», محمد أيوب's recitation, «استمع للكلمة» (unless a licensed source is found), the hidden doors, sharing as an image. Planned for an update after the first APK: a «قراءة متصلة» (flowing text) reading mode with adjustable text size, drawn with Digital Khatt (owner, 2026-11-27).

## How to change this plan

Edit this page in a PR. A change that alters a decision in the table above needs a superseding ADR first.
