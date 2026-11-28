plugins {
    id("qurancommunity.kmp.library")
    id("qurancommunity.compose")
    id("qurancommunity.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // platformModule is a Koin Module; LocationProvider returns AppResult.
            api(libs.koin.core)
            api(project(":core:common"))
        }
        androidMain.dependencies {
            // LocationManagerCompat, ContextCompat; the permission launcher.
            implementation(libs.androidx.core)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.activity) // ActivityResultContracts
            implementation(libs.kotlinx.coroutines.core) // suspendCancellableCoroutine
        }
    }
}
