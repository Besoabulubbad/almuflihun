package app.almuflihun.core.testing

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import app.almuflihun.core.designsystem.preview.AppPreview
import app.almuflihun.core.designsystem.preview.PreviewVariant
import app.almuflihun.core.designsystem.preview.PreviewVariants
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The Android screenshot runner. A module declares one subclass in `androidHostTest`:
 *
 * ```
 * class DesignSystemScreenshots(case: SnapshotCase, variant: PreviewVariant) : ScreenshotSuite(case, variant) {
 *     companion object {
 *         @JvmStatic @ParameterizedRobolectricTestRunner.Parameters(name = "{0}[{1}]")
 *         fun parameters() = screenshotParameters(designSystemSnapshots)
 *     }
 * }
 * ```
 *
 * Each case × variant is its own test, rendered through `AppPreview` and captured from its node once idle. Robolectric
 * renders on the API 36 runtime with native graphics (Sojourn found the API 37 runtime dropping Compose frames).
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [ScreenshotSuite.SDK], qualifiers = ScreenshotSuite.DEVICE)
abstract class ScreenshotSuite(private val case: SnapshotCase, private val variant: PreviewVariant) {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun screenshot() {
        composeRule.setContent {
            Box(Modifier.testTag(SNAPSHOT_TAG)) {
                AppPreview(variant) { case.content() }
            }
        }
        composeRule.onNodeWithTag(SNAPSHOT_TAG).captureRoboImage(filePath = "${case.name}_${variant.id}.png")
    }

    companion object {
        const val SDK = 36
        const val DEVICE = "w390dp-h844dp-xxhdpi"
        private const val SNAPSHOT_TAG = "almuflihun-snapshot"

        /** Every case in `PreviewVariants.Four`; cases marked `fullMatrix` in all of `PreviewVariants.Matrix`. */
        fun screenshotParameters(cases: List<SnapshotCase>): List<Array<Any>> = cases.flatMap { case ->
            val variants = if (case.fullMatrix) PreviewVariants.Matrix else PreviewVariants.Four
            variants.map { arrayOf<Any>(case, it) }
        }
    }
}
