# Tech stack

Every library and tool, the version we intend to pin, and where it was checked. **Checked 2026-11-26** against Maven Central and Google Maven metadata (latest stable releases; pre-releases noted). Sojourn's stack was verified on 2026-11-15/16 and is reused where it still holds. Versions are pinned in `gradle/libs.versions.toml` in Phase 1; Renovate keeps them current. Rules: stable releases only unless marked, and every entry is used through its official documentation for that version.

## Language, build, platform

| Tool | Version | Notes |
|---|---|---|
| Kotlin | 2.4.21 | Latest stable; 2.5.0 is at RC2 |
| Gradle / AGP | 9.7.1 / 9.3.1 | The newest pair inside Kotlin 2.4.21's documented compatibility window (as in Sojourn). AGP 9.5.3 is the newest stable but outside that window; re-checked when Kotlin moves |
| JDK toolchain | 21 | JVM target 11 for Android bytecode |
| compileSdk / targetSdk / minSdk | 37 / 37 / 26 | minSdk 26 (Android 8) for `PackageInstaller` session behaviour and adaptive icons; to confirm in Phase 1 |
| Compose Multiplatform | 1.12.2 | 1.13.0 at RC |
| Compose Material 3 (JetBrains) | 1.12.0-alpha03 *(pre-release)* | The only Material 3 line for CMP 1.12; used only inside `core:designsystem` |

## Architecture

| Library | Version |
|---|---|
| Lifecycle ViewModel (JetBrains) | 2.11.0 |
| Navigation 3 UI (JetBrains) | 1.2.0 |
| Koin | 4.2.2 (4.3.0 at beta) |
| kotlinx.coroutines | 1.11.0 |
| kotlinx.serialization | 1.13.0 |
| kotlinx-datetime | 0.8.0 |
| SQLDelight | 2.4.1 |
| Ktor client | 3.6.2 |
| Coil | 3.6.3 |
| Kermit | 2.2.0 |
| multiplatform-settings | 1.3.0 |

## Firebase and Google

| Library | Version | Notes |
|---|---|---|
| GitLive Firebase Kotlin SDK (auth, firestore, messaging, functions, analytics, crashlytics) | 2.8.0 | 3.0.0 at beta. Its supported Firebase Android BoM is checked in Phase 7 |
| Firebase Android BoM | 35.2.0 | Native SDKs under GitLive on Android |
| Google Services / Crashlytics Gradle plugins | 4.6.0 / 3.0.8 | |
| Credential Manager / Google ID | 1.6.0 / 1.2.1 | Google sign-in on Android |
| Cloud Functions | TypeScript, Node LTS | Versions pinned in `firebase/functions/package.json` |

## Quran, audio, astronomy, media

| Item | Version | Notes |
|---|---|---|
| Media3 (ExoPlayer, session) | 1.11.1 | Android playback, background service, media controls; iOS uses AVPlayer behind the same `core:audio` interface |
| Astronomy Engine | source at commit `826e26f` (2025-01-27), vendored as common Kotlin | ADR 0017: the JVM-only artifact is not used; checked against USNO data |
| android-youtube-player | 13.1.0 | Android wrapper of YouTube's official IFrame player, for the videos section |
| QPC V2 fonts, QUL layout, Tanzil text, King Fahd Hafs text | pinned by checksum | `SOURCES.md` beside each asset (ADR 0004) |

## Testing and quality

| Tool | Version |
|---|---|
| kotlin-test, Turbine | 2.4.21, 1.2.1 |
| Roborazzi | 1.79.0 |
| Konsist | 0.18.1 (1.0.0 at beta) |
| detekt + Compose rules | 1.23.8 + 0.6.8 |
| dependency-analysis | 3.9.0 |
| Macrobenchmark / baseline profiles | 1.5.0 |
| Maestro | CLI 2.12.1 |
| Firebase emulator suite | pinned in CI |
| Dokka, MkDocs Material | 2.3.0, 9.8.1 |

## Deliberately not used

| Instead of | We use | Because |
|---|---|---|
| Hilt / Dagger | Koin | multiplatform (Sojourn ADR 0004) |
| Navigation 2 | Navigation 3 | back stack as state (Sojourn ADR 0005) |
| Room | SQLDelight | SQL-first, mature KMP drivers |
| Firebase native SDKs directly in common code | GitLive SDK behind `core:firebase` | Firebase has no official KMP SDK (DECISIONS #15) |
| YouTube Android Player API | IFrame player wrapper | the native API is deprecated |
| quran_android code | our own engine | GPL-3.0 (DECISIONS #5) |
