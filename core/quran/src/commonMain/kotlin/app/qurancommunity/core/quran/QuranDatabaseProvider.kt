package app.qurancommunity.core.quran

import app.qurancommunity.core.quran.db.QuranDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Opens the Quran database once per process, installing the bundled copy first when the device has none or an older
 * one. Callers run it off the main thread (the first call copies about 6 MB).
 */
internal class QuranDatabaseProvider(
    private val storage: QuranStorage,
    private val bundledVersion: suspend () -> String = BundledQuranDatabase::readVersion,
    private val bundledBytes: suspend () -> ByteArray = BundledQuranDatabase::readBytes,
) {
    private val mutex = Mutex()
    private var database: QuranDatabase? = null

    suspend fun get(): QuranDatabase = mutex.withLock {
        database ?: run {
            val version = bundledVersion()
            if (storage.installedVersion() != version) storage.install(bundledBytes(), version)
            QuranDatabase(storage.openDriver()).also { database = it }
        }
    }
}
