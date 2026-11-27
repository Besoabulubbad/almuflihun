package app.almuflihun.app.shared

import app.almuflihun.core.analytics.Analytics
import app.almuflihun.core.analytics.AnalyticsSink
import app.almuflihun.core.analytics.NoAnalyticsSink
import app.almuflihun.core.common.AppClock
import app.almuflihun.core.common.DefaultDispatcherProvider
import app.almuflihun.core.common.DispatcherProvider
import app.almuflihun.core.common.SystemAppClock
import app.almuflihun.core.data.dataModule
import app.almuflihun.core.database.databaseModule
import app.almuflihun.core.platform.platformModule
import app.almuflihun.core.quran.quranModule
import app.almuflihun.feature.home.homeModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/** What every layer shares: the clock, the dispatchers, and analytics (nothing is sent until Firebase, ADR 0008). */
val appModule: Module = module {
    single<AppClock> { SystemAppClock() }
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<AnalyticsSink> { NoAnalyticsSink }
    single { Analytics(get()) }
}

/** Every Koin module the app starts with. Features join with their first screen; a test verifies the whole graph. */
val appModules: List<Module> = listOf(
    appModule,
    platformModule,
    quranModule,
    databaseModule,
    dataModule,
    homeModule,
)

/**
 * Starts Koin once per process: from `AlmuflihunApplication` on Android (which adds `androidContext`) and from
 * `MainViewController` on iOS.
 */
fun initKoin(platform: KoinAppDeclaration = {}): KoinApplication = startKoin {
    platform()
    modules(appModules)
}
