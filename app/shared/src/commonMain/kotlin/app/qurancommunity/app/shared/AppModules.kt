package app.qurancommunity.app.shared

import app.qurancommunity.core.analytics.Analytics
import app.qurancommunity.core.analytics.AnalyticsSink
import app.qurancommunity.core.analytics.NoAnalyticsSink
import app.qurancommunity.core.astronomy.astronomyModule
import app.qurancommunity.core.common.AppClock
import app.qurancommunity.core.common.DefaultDispatcherProvider
import app.qurancommunity.core.common.DispatcherProvider
import app.qurancommunity.core.common.SystemAppClock
import app.qurancommunity.core.data.dataModule
import app.qurancommunity.core.database.databaseModule
import app.qurancommunity.core.domain.domainModule
import app.qurancommunity.core.platform.platformModule
import app.qurancommunity.core.quran.quranModule
import app.qurancommunity.feature.home.homeModule
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
    astronomyModule,
    databaseModule,
    dataModule,
    domainModule,
    homeModule,
)

/**
 * Starts Koin once per process: from `QuranCommunityApplication` on Android (which adds `androidContext`) and from
 * `MainViewController` on iOS.
 */
fun initKoin(platform: KoinAppDeclaration = {}): KoinApplication = startKoin {
    platform()
    modules(appModules)
}
