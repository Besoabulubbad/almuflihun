plugins {
    id("almuflihun.kmp.library")
    id("almuflihun.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // SnapshotCase and the runners render through AppPreview.
            api(project(":core:designsystem"))
        }
        androidMain.dependencies {
            // ScreenshotSuite: its subclasses inherit the runner, config and compose rule.
            api(libs.robolectric)
            api(libs.robolectric.annotations)
            api(libs.junit)
            api(libs.compose.uiTestJunit4)
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.core)
            implementation(libs.differ) // RoborazziOptions.CompareOptions (the screenshot tolerance)
            // Registers the test activity for the compose rule under Robolectric; needed at runtime only.
            runtimeOnly(libs.compose.uiTestManifest)
        }
        iosMain.dependencies {
            // Brings Compose's ui-test (runSkikoComposeUiTest) transitively.
            implementation(libs.roborazzi.composeIos)
        }
    }
}
