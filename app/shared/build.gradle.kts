plugins {
    id("almuflihun.kmp.library")
    id("almuflihun.compose")
    id("almuflihun.koin")
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:designsystem"))
            implementation(project(":core:localization")) // AppTheme reads the UI language (ADR 0014)
            implementation(project(":core:navigation"))
            implementation(project(":core:common"))
            implementation(project(":core:platform"))
            implementation(project(":core:analytics"))
            implementation(project(":core:quran"))
            implementation(project(":core:astronomy"))
            implementation(project(":core:database"))
            implementation(project(":core:data"))
            implementation(project(":core:domain"))
            // Features, as they land.
            implementation(project(":feature:home"))
            // initKoin returns Koin's KoinApplication and takes its KoinAppDeclaration.
            api(libs.koin.core)
            implementation(libs.navigation3.runtime)
            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
            implementation(libs.androidx.savedstate) // the back stack's SavedStateConfiguration
        }
        androidMain.dependencies {
            api(libs.androidx.work.runtime) // WorkManagerSetup returns WorkManager's Configuration
        }
        named("androidHostTest").dependencies {
            implementation(libs.koin.test)
        }
    }
}
