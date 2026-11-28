package app.qurancommunity.core.quran

import app.qurancommunity.core.common.AppResult
import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.model.WordRange
import app.qurancommunity.core.testing.TestDispatcherProvider
import app.qurancommunity.core.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.TimeSource

/**
 * Advanced search on the real text. Every expected number was computed independently from the Tanzil file with the
 * same rules (letters, whole-word phrases, top-to-bottom joins, scopes from Tanzil's metadata).
 */
class AdvancedSearchTest : PlatformTest() {

    private val search by lazy {
        val dispatchers = TestDispatcherProvider()
        val provider = QuranDatabaseProvider(newTestStorage())
        IndexedQuranSearch(provider, SqlQuranReader(provider, dispatchers), dispatchers)
    }

    private suspend fun run(vararg terms: SearchTerm, scope: SearchScope = SearchScope.Quran, limit: Int = 200) =
        (search.advanced(SearchQuery(terms.toList(), scope), limit) as AppResult.Success).value

    /** (ayahs, surahs) */
    private suspend fun count(vararg terms: SearchTerm, scope: SearchScope = SearchScope.Quran): Pair<Int, Int> =
        run(*terms, scope = scope).let { it.totalAyahs to it.groups.size }

    private fun letters(text: String, join: Join = Join.And) = SearchTerm(text, TermMode.Letters, join)
    private fun phrase(text: String, join: Join = Join.And) = SearchTerm(text, TermMode.Phrase, join)

    @Test
    fun letters_find_words_inside_others_and_phrases_need_whole_words() = runAppTest {
        assertEquals(4 to 3, count(letters("الصبر"))) // «بالصبر»
        assertEquals(0 to 0, count(phrase("الصبر")))
        assertEquals(42 to 21, count(letters("رب العالمين")))
        assertEquals(34 to 19, count(phrase("رب العالمين")))
    }

    @Test
    fun terms_join_from_top_to_bottom() = runAppTest {
        assertEquals(2 to 1, count(letters("الصبر"), letters("الصلاة", Join.And)))
        assertEquals(63 to 30, count(letters("الصبر"), letters("الصلاة", Join.Or)))
        assertEquals(2 to 2, count(letters("الصبر"), letters("الصلاة", Join.Not)))
        assertEquals(37 to 23, count(letters("الصبر"), letters("الصلاة", Join.Or), letters("الزكاة", Join.Not)))
    }

    @Test
    fun scopes_narrow_the_search() = runAppTest {
        val jannah = letters("الجنة")
        assertEquals(5 to 1, count(jannah, scope = SearchScope.Surah(2)))
        assertEquals(3 to 3, count(jannah, scope = SearchScope.Juz(30)))
        assertEquals(1 to 1, count(jannah, scope = SearchScope.Hizb(1)))
        assertEquals(40 to 23, count(jannah, scope = SearchScope.Meccan))
        assertEquals(2 to 1, count(jannah, scope = SearchScope.Range(AyahKey(2, 1), AyahKey(2, 100))))
    }

    @Test
    fun results_are_grouped_by_surah_in_mushaf_order_with_matches_placed() = runAppTest {
        val results = run(letters("الصبر"))
        assertEquals(listOf(2, 90, 103), results.groups.map { it.surah })
        assertEquals(listOf(AyahKey(2, 45), AyahKey(2, 153)), results.groups.first().ayahs.map { it.key })
        assertTrue(results.groups.flatMap { it.ayahs }.all { it.matches.isNotEmpty() })
        val fatiha = run(phrase("الحمد لله رب العالمين"), scope = SearchScope.Surah(1)).groups.single().ayahs.single()
        assertEquals(listOf(WordRange(AyahKey(1, 2), 1, 4)), fatiha.matches)
    }

    @Test
    fun a_whole_quran_query_stays_within_the_budget() = runAppTest {
        run(letters("الله")) // builds the index
        // Best of several runs: one pause on a shared CI machine must not fail the budget; the best run is the search.
        val took = List(TIMED_RUNS) {
            val started = TimeSource.Monotonic.markNow()
            run(letters("الله"), phrase("رب العالمين", Join.Or), letters("الصلاة", Join.Not))
            started.elapsedNow()
        }.min()
        println("advanced search, 3 terms over the whole Quran (best of $TIMED_RUNS): $took")
        assertTrue(took.inWholeMilliseconds < BUDGET_MS, "took $took")
    }

    @Test
    fun the_cap_keeps_the_totals_exact() = runAppTest {
        val all = run(letters("رب العالمين"))
        val capped = run(letters("رب العالمين"), limit = 10)
        assertEquals(all.totalAyahs, capped.totalAyahs)
        assertEquals(all.groups.map { it.count }, capped.groups.map { it.count }) // every surah still counted
        assertEquals(10, capped.groups.sumOf { it.ayahs.size })
        assertTrue(capped.truncated)
        assertEquals(false, all.truncated)
        assertEquals(0, run(letters("   ")).totalAyahs)
    }

    private companion object {
        const val BUDGET_MS = 300 // ADR 0012
        const val TIMED_RUNS = 5
    }
}
