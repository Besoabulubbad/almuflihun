plugins {
    id("almuflihun.kmp.library")
    id("almuflihun.compose")
    id("almuflihun.koin")
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
