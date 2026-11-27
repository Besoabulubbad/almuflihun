package app.almuflihun.core.quran

import app.almuflihun.core.common.AppError
import app.almuflihun.core.common.AppResult
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.RootUsage
import app.almuflihun.core.model.WordRange
import app.almuflihun.core.testing.TestDispatcherProvider
import app.almuflihun.core.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Word morphology through the study API and search, on the real database (QUL, ADR 0012). */
class MorphologyTest : PlatformTest() {

    private val dispatchers by lazy { TestDispatcherProvider() }
    private val provider by lazy { QuranDatabaseProvider(newTestStorage()) }
    private val study by lazy { SqlQuranStudy(provider, dispatchers) }
    private val search by lazy { IndexedQuranSearch(provider, SqlQuranReader(provider, dispatchers), dispatchers) }

    private fun <T> AppResult<T>.value(): T = (this as AppResult.Success).value

    @Test
    fun a_root_s_usage_matches_published_counts_however_it_is_typed() = runAppTest {
        assertEquals(RootUsage("صبر", 103, 93), study.rootUsage("صبر").value())
        assertEquals(RootUsage("صبر", 103, 93), study.rootUsage("ص ب ر").value())
        assertEquals(339, study.rootUsage("رحم").value().occurrences)
        assertEquals(879, study.rootUsage("أمن").value().occurrences)
        assertEquals(AppResult.Failure(AppError.NotFound("ققق")), study.rootUsage("ققق"))
    }

    @Test
    fun an_ayah_lists_each_word_s_morphology() = runAppTest {
        val words = study.morphology(AyahKey(2, 153)).value()
        assertEquals((1..10).toList(), words.map { it.position })
        assertEquals("صبر", words[4].root)
        assertEquals(null, words[1].root) // «الذين» has no root
        assertEquals("صبر", words[9].root)
    }

    @Test
    fun root_and_lemma_search_find_every_form() = runAppTest {
        suspend fun count(term: SearchTerm, vararg more: SearchTerm) =
            search.advanced(SearchQuery(listOf(term) + more)).value().totalAyahs
        assertEquals(93, count(SearchTerm("صبر", TermMode.Root)))
        assertEquals(313, count(SearchTerm("ر ح م", TermMode.Root)))
        assertEquals(7, count(SearchTerm("صبر", TermMode.Root), SearchTerm("صلو", TermMode.Root, Join.And)))
        assertEquals(69, count(SearchTerm("صبر", TermMode.Lemma)))
        assertEquals(21, count(SearchTerm("صابر", TermMode.Lemma)))
    }

    @Test
    fun a_root_match_is_placed_on_the_content_words() = runAppTest {
        val query = SearchQuery(listOf(SearchTerm("صبر", TermMode.Root)), SearchScope.Surah(2))
        val results = search.advanced(query).value()
        val ayah = results.groups.single().ayahs.first { it.key == AyahKey(2, 153) }
        // «يا أيها» is one Mushaf word, so the Mushaf's 5th and 10th words are the content's 6th and 11th.
        assertEquals(listOf(WordRange(AyahKey(2, 153), 6, 6), WordRange(AyahKey(2, 153), 11, 11)), ayah.matches)
    }
}
