package app.almuflihun

import android.app.Application
import androidx.work.Configuration
import app.almuflihun.app.shared.WorkManagerSetup
import app.almuflihun.app.shared.initKoin
import org.koin.android.ext.koin.androidContext

/**
 * Starts Koin once per process, with the application `Context` the platform adapters need, and gives WorkManager
 * the app's worker factory (WorkManager starts on demand; its automatic start-up is removed in the manifest).
 */
class AlmuflihunApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@AlmuflihunApplication) }
    }

    override val workManagerConfiguration: Configuration
        get() = WorkManagerSetup.configuration()
}
