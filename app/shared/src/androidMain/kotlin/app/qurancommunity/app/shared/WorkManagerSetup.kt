package app.qurancommunity.app.shared

import androidx.work.Configuration
import app.qurancommunity.core.quran.font.FontWorkerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * WorkManager's configuration with the app's worker factory, so workers get their dependencies through their
 * constructors. The app shell is the one place that looks things up in Koin (ADR 0011).
 */
object WorkManagerSetup : KoinComponent {
    fun configuration(): Configuration = Configuration.Builder()
        .setWorkerFactory(get<FontWorkerFactory>())
        .build()
}
