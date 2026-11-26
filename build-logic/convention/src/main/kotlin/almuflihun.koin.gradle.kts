// Koin core for modules that contribute a DI module (`val <name>Module = module { … }`, ADR 0004).
// Compose/ViewModel integrations are added by almuflihun.kmp.feature where a UI exists.
import app.almuflihun.buildlogic.libs
import app.almuflihun.buildlogic.library

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.library("koin-core"))
        }
        // koin-test is declared by the modules whose tests verify a graph (checkModules in app:shared).
    }
}
