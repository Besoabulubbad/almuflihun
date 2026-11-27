plugins {
    id("almuflihun.kmp.library")
    id("almuflihun.compose")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // NavKey, NavBackStack, SavedStateConfiguration and SerializersModule appear in the public API.
            api(libs.navigation3.runtime)
            api(libs.androidx.savedstate)
            api(libs.kotlinx.serialization.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
