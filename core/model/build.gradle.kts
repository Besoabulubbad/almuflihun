plugins {
    id("almuflihun.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime) // LocalDate in the sky models
        }
    }
}
