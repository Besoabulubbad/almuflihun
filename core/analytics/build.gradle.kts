plugins {
    id("qurancommunity.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Analytics.consent is a StateFlow.
            api(libs.kotlinx.coroutines.core)
        }
    }
}
