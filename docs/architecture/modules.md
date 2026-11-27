# Modules

Every Gradle module, what it owns and what it may depend on. Package root `app.almuflihun` (ADR 0009). A module's own `README.md` holds detail that changes often; this page holds the contract. **core-base** = `core:model`, `core:common`.

## Application

| Module | Owns | May depend on |
|---|---|---|
| `:app:android` | `MainActivity`, edge-to-edge, predictive back, app icon, `direct` and `play` flavours | `:app:shared`, `:core:platform`, `:core:update` |
| `:app:shared` | `App()`, Koin start, `NavDisplay`, navigator implementation, iOS `MainViewController()` | everything |
| `iosApp/` | Xcode project | the shared framework |

## Features

All features share one contract: they may depend on `:core:ui`, `:core:designsystem`, `:core:localization`, `:core:navigation`, `:core:domain`, `:core:platform`, `:core:analytics` and core-base; **never** on `:core:data`, `:core:database`, `:core:firebase`, `:core:quran`, `:core:astronomy`, `:core:audio` directly, or on another feature.

| Module | Design screens |
|---|---|
| `:feature:home` | HomeB, HomeStates, Prompts (consent, update banner) |
| `:feature:moon` | Moon, MoonStates |
| `:feature:quran-hub` | Quran (الذكر), QuranStates |
| `:feature:mushaf` | Mushaf, MushafPlay, MushafFull, MushafIndex, MushafSearch, PlayerSheet |
| `:feature:hizb` | Hizb, HizbEnd |
| `:feature:doors` | RemindB, RemindStates, the door pages, BabAll, Hikma, HikmaPassage |
| `:feature:diyaa` | Diyaa, DiyaaS1–S11 |
| `:feature:collections` | Collections (ADR 0012; replaces Saved, SavedStates) |
| `:feature:live` | Live, LiveVideos, VideoPlayer |
| `:feature:community` | Community, CommunityCompose, CommunityThread, CommunityStates, VersePicker |
| `:feature:profile` | Profile, ProfileMine, Grades |
| `:feature:admin` | Admin, AdminReview, AdminUser, AdminRoles, AdminLive, AdminAnnounce, AdminFeedback |
| `:feature:settings` | Settings, CityPicker, WhatWeCollect, Feedback, onboarding |

## Core

| Module | Owns | May depend on |
|---|---|---|
| `:core:model` | Domain types, pure Kotlin | kotlinx-datetime |
| `:core:common` | Dispatchers, clock, results and errors, logging | coroutines, Kermit |
| `:core:localization` | Arabic formatting: digits, plurals, bidi isolation, references, separators | core-base, Compose Resources |
| `:core:designsystem` | Generated tokens, theme (grounds × themes), fonts, icons, every component. The only module that imports Material 3 | `:core:localization`, core-base |
| `:core:ui` | `ScreenState`, ViewModel base, scaffold that owns insets, preview and screenshot helpers | `:core:designsystem`, core-base |
| `:core:navigation` | Keys, serializers, navigator interface, deep-link parsing | Navigation 3, core-base |
| `:core:platform` | Location, haptics, share, local notifications, reduced motion, external apps, package installer | core-base |
| `:core:quran` | Text, normalisation, page layout and glyph data, quote placement, search index, content data loading | core-base, Compose Resources (the bundled `quran.db`), SQLDelight (ADR 0015) |
| `:core:astronomy` | Moon and sun computation, lunar-night rule, notable moments, tasbih windows, city list | core-base |
| `:core:audio` | Player, ayah and word timing, repeat, speed, downloads | `:core:quran`, core-base |
| `:core:database` | SQLDelight schema and migrations | core-base |
| `:core:firebase` | A seam over the GitLive SDK: auth, Firestore, messaging, callable functions | core-base |
| `:core:analytics` | Allow-listed event wrapper, consent gate, Crashlytics | core-base |
| `:core:update` | `app/latest` check, download, verification, installer hand-off, `minSupported` | `:core:platform`, `:core:firebase`, core-base |
| `:core:data` | Repositories (Flow), offline-first | `:core:database`, `:core:firebase`, `:core:quran`, `:core:astronomy`, `:core:audio`, core-base |
| `:core:domain` | Use cases and business rules (permissions shown in UI, points display, «تذكرة» pick, reading time) | `:core:data` interfaces, core-base |
| `:core:testing` | Fixtures, fakes, screenshot runners, coroutine helpers | test-scoped |

## Outside Gradle

| Path | Owns |
|---|---|
| `build-logic/` | Convention plugins `almuflihun.*`, the token generator, resource checks |
| `tooling/detekt-rules`, `tooling/konsist` | The custom detekt rule set; architecture tests |
| `firebase/` | `firestore.rules` with emulator tests; Cloud Functions (TypeScript) |
| `design/` | The binding design handoff, verbatim |
| `docs/` + `mkdocs.yml` | This site |
