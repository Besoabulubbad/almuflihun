package app.qurancommunity.core.designsystem

import app.qurancommunity.core.designsystem.sheet.designSystemSnapshots
import app.qurancommunity.core.testing.verifySnapshots
import kotlin.test.Test

class DesignSystemScreenshotsIos {
    @Test
    fun screenshots() = verifySnapshots(designSystemSnapshots)
}
