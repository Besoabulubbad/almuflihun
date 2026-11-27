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
            // The bundled fonts (Font(Res.font.…)).
            implementation(libs.compose.components.resources)
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "app.almuflihun.core.designsystem.resources"
}
