package app.almuflihun.core.designsystem

import app.almuflihun.core.designsystem.preview.PreviewVariant
import app.almuflihun.core.designsystem.sheet.designSystemSnapshots
import app.almuflihun.core.testing.ScreenshotSuite
import app.almuflihun.core.testing.SnapshotCase
import org.robolectric.ParameterizedRobolectricTestRunner

class DesignSystemScreenshots(case: SnapshotCase, variant: PreviewVariant) : ScreenshotSuite(case, variant) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}[{1}]")
        fun parameters() = screenshotParameters(designSystemSnapshots)
    }
}
