package app.qurancommunity.core.data

import org.koin.core.module.Module
import org.koin.dsl.module

/** Koin module: the repositories. */
val dataModule: Module = module {
    single<CollectionsRepository> { LocalCollectionsRepository(get(), get(), get()) }
    single<ReadingRepository> { LocalReadingRepository(get(), get(), get()) }
    single<SearchHistoryRepository> { LocalSearchHistoryRepository(get(), get(), get()) }
}
