package app.almuflihun.core.quran

import app.almuflihun.core.common.AppError
import app.almuflihun.core.common.AppResult
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.MushafLine
import app.almuflihun.core.model.MushafPage
import app.almuflihun.core.testing.TestDispatcherProvider
import app.almuflihun.core.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Installs the real bundled database on the platform and reads it back (Android under Robolectric, iOS simulator). */
class QuranReaderTest : PlatformTest() {

    private fun reader(storage: QuranStorage = newTestStorage()) =
        SqlQuranReader(QuranDatabaseProvider(storage), TestDispatcherProvider())

    @Test
    fun opening_pages_follow_the_print() = runAppTest {
        val reader = reader()
        val first = (reader.page(1) as AppResult.Success).value
        assertEquals(8, first.lines.size)
        assertEquals(MushafLine.SurahTitle(1, 1), first.lines[0])
        val opening = first.lines.filterIsInstance<MushafLine.Words>().flatMap { it.words }
        assertEquals(AyahKey(1, 1), opening.first().ayah)
        assertEquals(7, opening.count { it.isAyahEnd })

        val second = (reader.page(2) as AppResult.Success).value
        assertEquals(listOf(MushafLine.SurahTitle(1, 2), MushafLine.Basmala(2)), second.lines.take(2))
    }

    @Test
    fun every_page_has_its_lines_and_together_they_hold_every_word_once() = runAppTest {
        val reader = reader()
        var next = 1
        for (number in 1..MushafPage.COUNT) {
            val page = (reader.page(number) as AppResult.Success).value
            assertEquals(if (number <= 2) 8 else 15, page.lines.size, "page $number")
            page.lines.filterIsInstance<MushafLine.Words>().flatMap { it.words }.forEach { word ->
                assertEquals(next++, word.id, "page $number")
            }
        }
        assertEquals(83_669, next)
    }

    @Test
    fun an_ayah_has_its_page_and_ends_with_its_marker() = runAppTest {
        val reader = reader()
        val kursi = (reader.ayah(AyahKey(2, 255)) as AppResult.Success).value
        assertEquals(42, kursi.page)
        assertTrue(kursi.words.last().isAyahEnd)
        assertEquals(1, kursi.words.count { it.isAyahEnd })
        assertEquals((1..kursi.words.size).toList(), kursi.words.map { it.position })
        assertEquals(604, (reader.ayah(AyahKey(114, 6)) as AppResult.Success).value.page)
    }

    @Test
    fun a_missing_ayah_is_not_found() = runAppTest {
        val result = reader().ayah(AyahKey(1, 8))
        assertEquals(AppResult.Failure(AppError.NotFound("1:8")), result)
    }

    @Test
    fun the_copy_is_installed_once_and_replaced_when_the_bundle_changes() = runAppTest {
        val storage = CountingStorage(newTestStorage())
        QuranDatabaseProvider(storage).get()
        QuranDatabaseProvider(storage).get()
        assertEquals(1, storage.installs)
        QuranDatabaseProvider(storage, bundledVersion = { "a newer build" }).get()
        assertEquals(2, storage.installs)
        assertIs<AppResult.Success<*>>(SqlQuranReader(QuranDatabaseProvider(storage), TestDispatcherProvider()).page(3))
    }

    private class CountingStorage(private val delegate: QuranStorage) : QuranStorage by delegate {
        var installs = 0
        override fun install(database: ByteArray, version: String) {
            installs++
            delegate.install(database, version)
        }
    }
}
