// Compose Multiplatform for a KMP library module: compiler plugin, runtime/foundation/ui, resources,
// preview tooling. Material 3 is NOT added here — only core:designsystem declares it (ADR 0001, ADR 0011).
import app.qurancommunity.buildlogic.libs
import app.qurancommunity.buildlogic.library

plugins {
    // Declared here (not only in qurancommunity.kmp.library) so this script gets the `kotlin { android { … } }` accessors.
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // `api`: a UI module's public composables expose @Composable (runtime), Modifier (ui) and
            // foundation types such as PaddingValues in their signatures — they are part of its ABI.
            api(libs.library("compose-runtime"))
            api(libs.library("compose-foundation"))
            api(libs.library("compose-ui"))
            implementation(libs.library("compose-components-resources"))
            implementation(libs.library("compose-uiToolingPreview"))
        }
        androidMain.dependencies {
            implementation(libs.library("compose-uiTooling"))
        }
    }
}
