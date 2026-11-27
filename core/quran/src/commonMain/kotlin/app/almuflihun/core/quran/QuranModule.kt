package app.almuflihun.core.quran

import app.almuflihun.core.quran.font.FontFiles
import app.almuflihun.core.quran.font.PageFontDownloader
import app.almuflihun.core.quran.font.PageFontStore
import org.koin.core.module.Module
import org.koin.dsl.module

/** Where the platform keeps the installed copy and the downloaded page fonts. */
internal expect val quranStorageModule: Module

/** Koin module: [QuranReader] over the bundled database and [ContentReader] over the bundled content. */
val quranModule: Module = module {
    includes(quranStorageModule)
    single { QuranDatabaseProvider(get()) }
    single<QuranReader> { SqlQuranReader(get(), get()) }
    single<ContentReader> { BundledContentReader(get(), get()) }
    single { QuranTextIndex(get(), get()) }
    single<QuranSearch> { IndexedQuranSearch(get(), get(), get(), get()) }
    single<QuranStudy> { SqlQuranStudy(get(), get(), get(), get(), get()) }
    single {
        val provider = get<QuranDatabaseProvider>()
        PageFontStore(get<FontFiles>()) { page ->
            provider.get().quranQueries.pageGlyphs(page.toLong()).executeAsList()
                .flatMapTo(HashSet()) { glyph -> glyph.map(Char::code) }
        }
    }
    single { PageFontDownloader(get(), get()) }
}

/** The Mushaf edition whose page fonts are downloaded (ADR 0013): مصحف المدينة ١٤٢١, QPC V2. */
internal const val MUSHAF_EDITION = "qpc-v2"
