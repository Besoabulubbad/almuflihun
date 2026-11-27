package app.almuflihun.core.quran

import org.koin.core.module.Module
import org.koin.dsl.module

/** Where the platform keeps the installed copy. */
internal expect val quranStorageModule: Module

/** Koin module: [QuranReader] over the bundled database and [ContentReader] over the bundled content. */
val quranModule: Module = module {
    includes(quranStorageModule)
    single { QuranDatabaseProvider(get()) }
    single<QuranReader> { SqlQuranReader(get(), get()) }
    single<ContentReader> { BundledContentReader(get(), get()) }
    single<QuranSearch> { IndexedQuranSearch(get(), get(), get()) }
}
