package app.almuflihun.core.quran

import org.koin.core.module.Module
import org.koin.dsl.module

/** Where the platform keeps the installed copy. */
internal expect val quranStorageModule: Module

/** Koin module: [QuranReader] over the bundled database. */
val quranModule: Module = module {
    includes(quranStorageModule)
    single { QuranDatabaseProvider(get()) }
    single<QuranReader> { SqlQuranReader(get(), get()) }
}
