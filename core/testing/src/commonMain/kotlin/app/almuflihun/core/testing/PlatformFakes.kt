package app.almuflihun.core.testing

import app.almuflihun.core.platform.MotionSettings
import app.almuflihun.core.platform.ShareSheet

/** Records what was shared, for ViewModel and screen tests. */
class FakeShareSheet : ShareSheet {
    val shared = mutableListOf<String>()
    override fun shareText(text: String) {
        shared += text
    }
}

/** A fixed reduce-motion answer for tests and screenshots. */
class FakeMotionSettings(override val reduceMotion: Boolean = false) : MotionSettings
