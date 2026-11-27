plugins {
    id("almuflihun.kmp.library")
    id("almuflihun.compose")
    id("almuflihun.tokens")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Generated Motion.kt exposes CubicBezierEasing; the palette and roles use runtime's @Immutable.
            api(libs.compose.animationCore)
        }
    }
}
