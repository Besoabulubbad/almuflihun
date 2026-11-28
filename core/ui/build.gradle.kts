plugins {
    id("qurancommunity.kmp.library")
    id("qurancommunity.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Public API: ScreenState(AppError, Loadable), ScreenViewModel (ViewModel, Flow).
            api(project(":core:common"))
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}
