package app.almuflihun

import android.app.Application
import app.almuflihun.app.shared.initKoin
import org.koin.android.ext.koin.androidContext

/** Starts Koin once per process, with the application `Context` the platform adapters need. */
class AlmuflihunApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@AlmuflihunApplication) }
    }
}
