plugins {
    id("qurancommunity.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime) // LocalDate in the sky models
        }
    }
}
