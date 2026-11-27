package app.almuflihun.core.data

import app.almuflihun.core.common.AppClock
import app.almuflihun.core.common.DispatcherProvider
import app.almuflihun.core.database.UserDatabase
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.HizbPosition
import app.almuflihun.core.model.MushafPage
import app.almuflihun.core.model.MushafPosition
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * The reader's places (docs/specs/screens.md §4): the Mushaf and the Hizb each remember their own position, and
 * opening one never moves the other. Save a position when the reader reads (a page turned, an ayah played), never on
 * a jump, so «عُد إلى قراءتك» can lead back.
 */
interface ReadingRepository {
    fun mushafPosition(): Flow<MushafPosition?>
    fun hizbPosition(): Flow<HizbPosition?>
    suspend fun saveMushafPosition(position: MushafPosition)
    suspend fun saveHizbPosition(position: HizbPosition)

    /** «آخر الصفحات»: the last [RECENT_PAGES] pages opened, newest first. */
    fun recentPages(): Flow<List<Int>>
    suspend fun pageOpened(page: Int)

    companion object {
        const val RECENT_PAGES = 10
    }
}

internal class LocalReadingRepository(
    private val database: UserDatabase,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider,
) : ReadingRepository {

    private val queries get() = database.readingQueries

    override fun mushafPosition(): Flow<MushafPosition?> =
        queries.position(MUSHAF).asFlow().mapToOneOrNull(dispatchers.io).map { row ->
            row?.let { MushafPosition(it.page.toInt(), AyahKey(it.surah.toInt(), it.ayah.toInt())) }
        }

    override fun hizbPosition(): Flow<HizbPosition?> =
        queries.position(HIZB).asFlow().mapToOneOrNull(dispatchers.io).map { row ->
            row?.hizb?.let { hizb ->
                HizbPosition(hizb.toInt(), row.page.toInt(), AyahKey(row.surah.toInt(), row.ayah.toInt()))
            }
        }

    override suspend fun saveMushafPosition(position: MushafPosition) {
        requirePage(position.page)
        write { save(MUSHAF, null, position.page, position.ayah) }
    }

    override suspend fun saveHizbPosition(position: HizbPosition) {
        requirePage(position.page)
        require(position.hizb in 1..HIZBS) { "Hizb ${position.hizb} is not 1..$HIZBS" }
        write { save(HIZB, position.hizb.toLong(), position.page, position.ayah) }
    }

    override fun recentPages(): Flow<List<Int>> =
        queries.recentPages(ReadingRepository.RECENT_PAGES.toLong()).asFlow().mapToList(dispatchers.io)
            .map { pages -> pages.map { it.toInt() } }

    override suspend fun pageOpened(page: Int) {
        requirePage(page)
        write {
            queries.openPage(page.toLong(), now())
            queries.trimRecentPages(ReadingRepository.RECENT_PAGES.toLong())
        }
    }

    private fun save(surface: String, hizb: Long?, page: Int, ayah: AyahKey) =
        queries.savePosition(surface, hizb, page.toLong(), ayah.surah.toLong(), ayah.ayah.toLong(), now())

    private fun requirePage(page: Int) = require(page in 1..MushafPage.COUNT) { "Page $page is not 1..604" }

    private fun now(): Long = clock.now().toEpochMilliseconds()

    private suspend fun write(block: () -> Unit) = withContext(dispatchers.io) {
        database.transaction { block() }
    }

    private companion object {
        const val MUSHAF = "mushaf"
        const val HIZB = "hizb"
        const val HIZBS = 60
    }
}
