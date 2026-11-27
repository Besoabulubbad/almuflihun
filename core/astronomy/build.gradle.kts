plugins {
    id("almuflihun.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model")) // Place, MoonState, SunDay, LunarNight
            api(libs.kotlinx.datetime) // LocalDate, TimeZone in the API
            api(libs.koin.core) // astronomyModule is public
        }
    }
}

// Astronomy Engine is vendored as upstream code (ADR 0017): kept as close to the original as possible, so our style
// rules do not apply to it. Its three marked [almuflihun] patches are the only changes.
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude("**/io/github/cosinekitty/**")
}
