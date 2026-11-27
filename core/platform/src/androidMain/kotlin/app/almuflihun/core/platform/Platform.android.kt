package app.almuflihun.core.platform

import android.content.Context
import android.content.Intent
import android.provider.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

internal class AndroidShareSheet(private val context: Context) : ShareSheet {
    override fun shareText(text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

internal class AndroidMotionSettings(private val context: Context) : MotionSettings {
    override val reduceMotion: Boolean
        get() = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
}

actual val platformModule: Module = module {
    single<ShareSheet> { AndroidShareSheet(get()) }
    single<MotionSettings> { AndroidMotionSettings(get()) }
    single<LocationProvider> { AndroidLocationProvider(get()) }
}
