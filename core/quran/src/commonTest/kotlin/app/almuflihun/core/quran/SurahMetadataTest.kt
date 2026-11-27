package app.almuflihun.core.quran

import app.almuflihun.core.common.AppError
import app.almuflihun.core.common.AppResult
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.AyahPosition
import app.almuflihun.core.model.RevelationPlace
import app.almuflihun.core.testing.TestDispatcherProvider
import app.almuflihun.core.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Tanzil's metadata through the reader and search, on the real database. */
class SurahMetadataTest : PlatformTest() {

    private val dispatchers by lazy { TestDispatcherProvider() }
    private val provider by lazy { QuranDatabaseProvider(newTestStorage()) }
    private val reader by lazy { SqlQuranReader(provider, dispatchers) }
    private val search by lazy { IndexedQuranSearch(provider, reader, dispatchers) }

    private fun <T> AppResult<T>.value(): T = (this as AppResult.Success).value

    @Test
    fun the_surahs_are_listed_with_their_length_place_order_and_first_page() = runAppTest {
        val surahs = reader.surahs().value()
        assertEquals((1..114).toList(), surahs.map { it.number })
        assertEquals(6236, surahs.sumOf { it.ayahCount })
        val baqara = surahs[1]
        assertEquals(listOf(286, 87, 2), listOf(baqara.ayahCount, baqara.revelationOrder, baqara.firstPage))
        assertEquals(RevelationPlace.Medinan, baqara.place)
        assertEquals(RevelationPlace.Meccan, surahs[0].place)
        assertEquals(1, surahs.first().firstPage)
        assertEquals(604, surahs.last().firstPage)
    }

    @Test
    fun an_ayah_knows_its_page_juz_hizb_quarter_and_sajda() = runAppTest {
        assertEquals(AyahPosition(AyahKey(1, 1), 1, 1, 1, 1, false), reader.position(AyahKey(1, 1)).value())
        assertEquals(AyahPosition(AyahKey(2, 255), 42, 3, 5, 1, false), reader.position(AyahKey(2, 255)).value())
        assertEquals(AyahPosition(AyahKey(114, 6), 604, 30, 60, 4, false), reader.position(AyahKey(114, 6)).value())
        assertEquals(true, reader.position(AyahKey(96, 19)).value().sajda)
        assertEquals(AppResult.Failure(AppError.NotFound("1:8")), reader.position(AyahKey(1, 8)))
    }

    @Test
    fun every_hizb_quarter_in_the_content_agrees_with_the_metadata() = runAppTest {
        val hizbs = BundledContentReader(reader, dispatchers).hizbs().value()
        hizbs.forEach { hizb ->
            hizb.quarters.forEachIndexed { i, key ->
                val position = reader.position(key).value()
                assertEquals(hizb.number to i + 1, position.hizb to position.quarter, "quarter start $key")
                assertEquals(hizb.juz, position.juz, "juz of $key")
            }
        }
    }

    @Test
    fun surahs_are_found_by_name_or_number() = runAppTest {
        assertEquals(listOf(2), search.surahs("البقرة").value().map { it.number })
        assertEquals(listOf(2), search.surahs("بقره").value().map { it.number }) // no article, ة typed as ه
        assertEquals(listOf(3), search.surahs("ال عمران").value().map { it.number })
        assertEquals(listOf(36), search.surahs("٣٦").value().map { it.number })
    }

    @Test
    fun a_jump_can_name_the_surah() = runAppTest {
        suspend fun jump(text: String) = search.jump(text, setOf("صفحة"), setOf("سورة")).value()
        assertEquals(Jump.ToAyah(AyahKey(2, 255)), jump("البقرة ٢٥٥"))
        assertEquals(Jump.ToAyah(AyahKey(2, 255)), jump("سورة البقرة 255"))
        assertEquals(Jump.ToAyah(AyahKey(3, 7)), jump("آل عمران ٧"))
        assertEquals(Jump.ToPage(50), jump("صفحة ٥٠"))
        assertNull(jump("البقرة ٣٠٠"))
        assertNull(jump("الفيل ٦")) // al-Fil has 5
        assertNull(jump("سورة"))
    }
}
