package app.qurancommunity.core.testing

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import app.qurancommunity.core.designsystem.preview.AppPreview
import app.qurancommunity.core.designsystem.preview.PreviewVariants
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import io.github.takahirom.roborazzi.captureRoboImage

/**
 * The iOS virtual device: the same 390 × 844 dp as the Android screenshots, at 2× (Android stays 3×). The test binary
 * is unoptimised Kotlin/Native, where an image's cost grows with its pixels: 3× took ~13 s per capture and pushed the
 * CI job past its hour; 2× takes under 2 s and keeps text and icons reviewable.
 */
private const val DEVICE_DENSITY = 2f
private const val DEVICE_WIDTH_DP = 390f
private const val DEVICE_HEIGHT_DP = 844f
private const val SNAPSHOT_TAG = "qurancommunity-snapshot"

/**
 * iOS compares with a 0.5 % pixel tolerance; Android stays exact. Text and icon edges anti-alias slightly differently
 * across simulator devices and runtimes (measured: 0.07–0.15 % of pixels, alpha unchanged, after a new iOS 27.2
 * simulator runtime appeared locally). Roborazzi's iOS documentation uses the same mechanism (its example: 1 %).
 * A changed glyph, colour or layout moves far more pixels than this.
 */
private const val IOS_CHANGE_THRESHOLD = 0.005f

/**
 * The iOS screenshot runner. A module declares one test in `iosTest`:
 *
 * ```
 * class DesignSystemScreenshotsIos {
 *     @Test fun screenshots() = verifySnapshots(designSystemSnapshots)
 * }
 * ```
 *
 * Renders every case in the four shipped variants with Compose's Skiko test runtime on the simulator and records or
 * verifies `<name>_<variant>.png` under `screenshots/iosSimulatorArm64/` (Roborazzi's experimental iOS support; iOS
 * needs an explicit file name per capture). Every case runs even if an earlier one fails, and the failure lists them
 * all.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
fun verifySnapshots(cases: List<SnapshotCase>) {
    val failures = mutableListOf<String>()
    cases.forEach { case ->
        PreviewVariants.Four.forEach { variant ->
            val fileName = "${case.name}_${variant.id}.png"
            try {
                runSkikoComposeUiTest(
                    size = Size(DEVICE_WIDTH_DP * DEVICE_DENSITY, DEVICE_HEIGHT_DP * DEVICE_DENSITY),
                    density = Density(DEVICE_DENSITY),
                ) {
                    setContent {
                        Box(Modifier.testTag(SNAPSHOT_TAG)) {
                            AppPreview(variant) { case.content() }
                        }
                    }
                    onNodeWithTag(SNAPSHOT_TAG).captureRoboImage(
                        this,
                        filePath = fileName,
                        roborazziOptions = RoborazziOptions(
                            compareOptions = RoborazziOptions.CompareOptions(changeThreshold = IOS_CHANGE_THRESHOLD),
                        ),
                    )
                }
            } catch (e: AssertionError) {
                failures += "$fileName: ${e.message}"
            }
        }
    }
    check(failures.isEmpty()) { "${failures.size} iOS screenshot(s) differ:\n" + failures.joinToString("\n") }
}
