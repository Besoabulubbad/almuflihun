package app.qurancommunity.core.quran

import app.qurancommunity.core.common.AppResult
import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.model.WordRange
import app.qurancommunity.core.testing.TestDispatcherProvider
import app.qurancommunity.core.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.TimeSource

/**
 * Search over the real text. Queries are what a reader types (plain letters); the expected counts were computed
 * independently from the Tanzil file with the same rules.
 */
class QuranSearchTest : PlatformTest() {

    private val search by lazy {
        val dispatchers = TestDispatcherProvider()
        val provider = QuranDatabaseProvider(newTestStorage())
        IndexedQuranSearch(provider, SqlQuranReader(provider, dispatchers), dispatchers)
    }
    private val pageWords = setOf("صفحة", "page")

    private suspend fun count(query: String): Pair<Int, Int> {
        val result = (search.words(query, limit = Int.MAX_VALUE) as AppResult.Success).value
        return result.total to result.ayahs.sumOf { it.matches.size }
    }

    @Test
    fun words_are_found_by_their_letters() = runAppTest {
        val started = TimeSource.Monotonic.markNow()
        assertEquals(14 to 14, count("الصابرين")) // also والصابرين
        val first = started.elapsedNow()
        assertEquals(69 to 73, count("صبر"))
        assertEquals(42 to 42, count("رب العالمين"))
        assertEquals(23 to 23, count("الحمد لله"))
        val warm = TimeSource.Monotonic.markNow()
        assertEquals(449 to 495, count("شيء"))
        val warmTime = warm.elapsedNow()
        println("search: first (builds the index) $first, warm $warmTime")
        assertTrue(warmTime.inWholeMilliseconds < BUDGET_MS, "warm search took $warmTime")
    }

    @Test
    fun marks_and_letter_forms_do_not_matter() = runAppTest {
        val shadda = 'ّ'
        val kasra = 'ِ'
        assertEquals(count("الصابرين"), count("الصّابِرين".replace("ّ", "$shadda").replace("ِ", "$kasra")))
        assertEquals(count("إله"), count("اله"))
        assertEquals(count("شيء"), count("شي"))
    }

    @Test
    fun a_match_is_placed_on_the_words_it_covers() = runAppTest {
        val result = (search.words("الحمد لله رب") as AppResult.Success).value
        val fatiha = result.ayahs.first()
        assertEquals(AyahKey(1, 2), fatiha.key)
        assertEquals(listOf(WordRange(AyahKey(1, 2), 1, 3)), fatiha.matches)
    }

    @Test
    fun results_are_capped_but_counted() = runAppTest {
        val result = (search.words("شيء") as AppResult.Success).value
        assertEquals(449, result.total)
        assertEquals(QuranSearch.DEFAULT_LIMIT, result.ayahs.size)
        assertEquals(result.ayahs.sortedWith(compareBy({ it.key.surah }, { it.key.ayah })), result.ayahs)
        assertEquals(0, (search.words("   ") as AppResult.Success).value.total)
    }

    @Test
    fun jumps_to_an_ayah_or_a_page() = runAppTest {
        suspend fun jump(text: String) = (search.jump(text, pageWords) as AppResult.Success).value
        assertEquals(Jump.ToAyah(AyahKey(2, 255)), jump("٢:٢٥٥"))
        assertEquals(Jump.ToAyah(AyahKey(2, 255)), jump(" 2 : 255 "))
        assertEquals(Jump.ToPage(50), jump("صفحة ٥٠"))
        assertEquals(Jump.ToPage(604), jump("page 604"))
        assertNull(jump("٢:٣٠٠")) // al-Baqara has 286
        assertNull(jump("١١٥:١"))
        assertNull(jump("صفحة ٧٠٠"))
        assertNull(jump("الصابرين"))
    }

    private companion object {
        const val BUDGET_MS = 300 // docs/guides/quality-bar.md, ADR 0012
    }
}
