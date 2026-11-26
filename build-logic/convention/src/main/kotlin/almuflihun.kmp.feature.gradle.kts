// A feature module = one area of screens. Library + Compose + Koin + the fixed set of core modules a feature may
// see (docs/architecture/modules.md). Nothing else; Konsist enforces the rest. The screenshot plugin joins in Phase 2.
import app.almuflihun.buildlogic.libs
import app.almuflihun.buildlogic.library

plugins {
    id("almuflihun.kmp.library")
    id("almuflihun.compose")
    id("almuflihun.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:ui"))
            api(project(":core:navigation"))
            api(project(":core:domain"))
            api(libs.library("koin-core"))
            implementation(project(":core:common"))
            implementation(project(":core:localization"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:analytics"))
            implementation(libs.library("koin-compose"))
            implementation(libs.library("koin-composeViewmodel"))
            implementation(libs.library("androidx-lifecycle-viewmodel"))
            implementation(libs.library("androidx-lifecycle-viewmodelCompose"))
            implementation(libs.library("androidx-lifecycle-runtimeCompose"))
            implementation(libs.library("kotlinx-coroutines-core"))
            // core:platform and the like are declared by the features that use them.
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.library("kotlinx-coroutines-test"))
            implementation(libs.library("turbine"))
        }
    }
}
