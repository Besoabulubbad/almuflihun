# `:tooling:konsist`

Architecture tests over every product Kotlin file (`app/`, `core/`, `feature/`), from `docs/architecture/modules.md` and `CLAUDE.md`: packages match modules; features never import infrastructure, engines or other features; Material only in `core:designsystem`; Firebase only behind `core:firebase`, `core:analytics` and the shells; analytics SDKs only in `core:analytics`; navigation keys only in `core:navigation`; no service locator outside `app:shared`; platform locale APIs only in `core:localization`; feature ViewModels extend `ScreenViewModel` and come with `Route` and `Screen` composables.

Rules pass vacuously on skeletons and fail on the first violating file. Run: `./gradlew :tooling:konsist:test`.
