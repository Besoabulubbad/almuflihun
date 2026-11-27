package app.almuflihun.core.quran

import android.content.Context
import app.almuflihun.core.model.PageFont
import app.almuflihun.core.quran.db.QuranDatabase
import app.almuflihun.core.quran.font.AndroidFontFetcher
import app.almuflihun.core.quran.font.AndroidFontFiles
import app.almuflihun.core.quran.font.AndroidPageFontCache
import app.almuflihun.core.quran.font.FontDownloadScheduler
import app.almuflihun.core.quran.font.FontFetcher
import app.almuflihun.core.quran.font.FontFiles
import app.almuflihun.core.quran.font.FontWorkerFactory
import app.almuflihun.core.quran.font.PageFontProvider
import app.almuflihun.core.quran.font.PageFontStore
import app.almuflihun.core.quran.font.StorePageFontProvider
import app.almuflihun.core.quran.font.WorkManagerFontScheduler
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

/** Keeps the copy in `noBackupFilesDir/quran`: it is rebuilt from the APK, so it never goes into a backup. */
internal class AndroidQuranStorage(
    private val context: Context,
    private val directory: File = File(context.noBackupFilesDir, "quran"),
) : QuranStorage {

    private val database = File(directory, BundledQuranDatabase.FILE_NAME)
    private val marker = File(directory, "installed.version")

    override fun installedVersion(): String? =
        marker.takeIf { it.exists() && database.exists() }?.readText()?.trim()

    override fun install(database: ByteArray, version: String) {
        directory.mkdirs()
        marker.delete()
        val partial = File(directory, "${BundledQuranDatabase.FILE_NAME}.partial")
        partial.writeBytes(database)
        listOf("", "-journal", "-wal", "-shm").forEach { File(directory, BundledQuranDatabase.FILE_NAME + it).delete() }
        check(partial.renameTo(this.database)) { "Could not install the Quran database" }
        marker.writeText(version)
    }

    // An absolute name opens the file where it is instead of in the default databases directory.
    override fun openDriver(): SqlDriver = AndroidSqliteDriver(QuranDatabase.Schema, context, database.absolutePath)
}

internal actual val quranStorageModule: Module = module {
    single<QuranStorage> { AndroidQuranStorage(get()) }
    single<FontFiles> { AndroidFontFiles(get(), MUSHAF_EDITION) }
    single<FontFetcher> { AndroidFontFetcher() }
    single<FontDownloadScheduler> { WorkManagerFontScheduler(get()) }
    single { FontWorkerFactory(get()) }
    single<PageFontProvider> {
        val cache = get<AndroidPageFontCache>()
        StorePageFontProvider(get(), get()) { page ->
            withContext(Dispatchers.IO) { cache.ttf(page) }?.let { PageFont.Exact(page, file = it.path) }
        }
    }
    single { AndroidPageFontCache(get<Context>(), get<PageFontStore>()) }
}
