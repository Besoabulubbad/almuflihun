plugins {
    id("almuflihun.android.application")
}

android {
    namespace = "app.almuflihun"

    defaultConfig {
        // Permanent: never change after the first install (ADR 0009).
        applicationId = "app.almuflihun"
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(project(":app:shared"))
    implementation(libs.androidx.activity) // enableEdgeToEdge
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime) // setContent { App() }
    implementation(libs.compose.ui)
    implementation(libs.koin.android) // androidContext()
    implementation(libs.koin.core) // initKoin's KoinAppDeclaration
    implementation(libs.androidx.work.runtime) // Configuration.Provider
}
