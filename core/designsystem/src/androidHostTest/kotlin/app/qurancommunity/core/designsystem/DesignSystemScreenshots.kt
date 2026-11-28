package app.qurancommunity.core.designsystem

import app.qurancommunity.core.designsystem.preview.PreviewVariant
import app.qurancommunity.core.designsystem.sheet.designSystemSnapshots
import app.qurancommunity.core.testing.ScreenshotSuite
import app.qurancommunity.core.testing.SnapshotCase
import org.robolectric.ParameterizedRobolectricTestRunner

class DesignSystemScreenshots(case: SnapshotCase, variant: PreviewVariant) : ScreenshotSuite(case, variant) {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}[{1}]")
        fun parameters() = screenshotParameters(designSystemSnapshots)
    }
}
