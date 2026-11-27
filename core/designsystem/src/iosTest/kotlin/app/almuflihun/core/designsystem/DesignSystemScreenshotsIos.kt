package app.almuflihun.core.designsystem

import app.almuflihun.core.designsystem.sheet.designSystemSnapshots
import app.almuflihun.core.testing.verifySnapshots
import kotlin.test.Test

class DesignSystemScreenshotsIos {
    @Test
    fun screenshots() = verifySnapshots(designSystemSnapshots)
}
