package app.almuflihun.core.quran

import app.almuflihun.core.quran.db.QuranDatabase
import app.almuflihun.core.quran.font.FontDownloadScheduler
import app.almuflihun.core.quran.font.FontFetcher
import app.almuflihun.core.quran.font.FontFiles
import app.almuflihun.core.quran.font.IosFontFetcher
import app.almuflihun.core.quran.font.IosFontFiles
import app.almuflihun.core.quran.font.IosFontScheduler
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

/** Keeps the copy in Application Support/quran, excluded from iCloud and device backups. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class IosQuranStorage(
    private val directory: String = defaultDirectory(),
) : QuranStorage {

    private val files = NSFileManager.defaultManager
    private val database = "$directory/${BundledQuranDatabase.FILE_NAME}"
    private val marker = "$directory/installed.version"

    override fun installedVersion(): String? {
        if (!files.fileExistsAtPath(database) || !files.fileExistsAtPath(marker)) return null
        return NSString.stringWithContentsOfFile(marker, NSUTF8StringEncoding, null)?.trim()
    }

    override fun install(database: ByteArray, version: String) {
        files.createDirectoryAtPath(directory, withIntermediateDirectories = true, attributes = null, error = null)
        files.removeItemAtPath(marker, null)
        listOf("", "-journal", "-wal", "-shm").forEach { files.removeItemAtPath(this.database + it, null) }
        check(database.toNSData().writeToFile(this.database, atomically = true)) {
            "Could not install the Quran database"
        }
        NSURL.fileURLWithPath(directory).setResourceValue(true, NSURLIsExcludedFromBackupKey, null)
        check(version.toNSData().writeToFile(marker, atomically = true)) { "Could not record the Quran version" }
    }

    override fun openDriver(): SqlDriver = NativeSqliteDriver(
        schema = QuranDatabase.Schema,
        name = BundledQuranDatabase.FILE_NAME,
        onConfiguration = { config ->
            config.copy(extendedConfig = DatabaseConfiguration.Extended(basePath = directory))
        },
    )

    private fun String.toNSData(): NSData = encodeToByteArray().toNSData()

    private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }

    companion object {
        fun defaultDirectory(): String {
            val base = NSFileManager.defaultManager.URLForDirectory(
                directory = NSApplicationSupportDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null,
            )
            return checkNotNull(base?.path) { "No Application Support directory" } + "/quran"
        }
    }
}

internal actual val quranStorageModule: Module = module {
    single<QuranStorage> { IosQuranStorage() }
    single<FontFiles> { IosFontFiles.forEdition(MUSHAF_EDITION) }
    single<FontFetcher> { IosFontFetcher() }
    single<FontDownloadScheduler> { IosFontScheduler(get()) }
}
