plugins {
    id("qurancommunity.kmp.library")
    id("qurancommunity.compose")
    id("qurancommunity.tokens")
    id("qurancommunity.screenshot")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model")) // PageFont, in rememberMushafFontFamily's signature
            // The theme takes its layout direction from the UI language (ADR 0014).
            api(project(":core:localization"))
            // Generated Motion.kt exposes CubicBezierEasing; the palette and roles use runtime's @Immutable.
            api(libs.compose.animationCore)
            // The bundled fonts (Font(Res.font.…)).
            implementation(libs.compose.components.resources)
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "app.qurancommunity.core.designsystem.resources"
}
