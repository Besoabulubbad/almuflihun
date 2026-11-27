plugins {
    id("almuflihun.kmp.library")
    id("almuflihun.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // platformModule is a Koin Module in the public API.
            api(libs.koin.core)
        }
    }
}
