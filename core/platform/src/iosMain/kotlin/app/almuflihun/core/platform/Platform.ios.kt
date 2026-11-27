package app.almuflihun.core.platform

import org.koin.core.module.Module
import org.koin.dsl.module
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

internal class IosShareSheet : ShareSheet {
    override fun shareText(text: String) {
        val controller = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?.presentViewController(controller, animated = true, completion = null)
    }
}

internal class IosMotionSettings : MotionSettings {
    override val reduceMotion: Boolean
        get() = UIAccessibilityIsReduceMotionEnabled()
}

actual val platformModule: Module = module {
    single<ShareSheet> { IosShareSheet() }
    single<MotionSettings> { IosMotionSettings() }
}
