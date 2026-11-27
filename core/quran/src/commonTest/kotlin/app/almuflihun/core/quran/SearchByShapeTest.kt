package app.almuflihun.core.quran

import app.almuflihun.core.common.AppResult
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.testing.TestDispatcherProvider
import app.almuflihun.core.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * «حسب الرسم» (owner, 2026-11-27): search results grouped by how the Mushaf writes the words they fall on. Counts
 * computed independently from the Tanzil text, the alignment rule and QUL's stems.
 */
class SearchByShapeTest : PlatformTest() {

    private val search by lazy {
        val dispatchers = TestDispatcherProvider()
        val provider = QuranDatabaseProvider(newTestStorage())
        IndexedQuranSearch(provider, SqlQuranReader(provider, dispatchers), dispatchers)
    }

    private suspend fun results(text: String, mode: TermMode = TermMode.Letters) =
        (search.advanced(SearchQuery(listOf(SearchTerm(text, mode))), limit = 10_000) as AppResult.Success).value

    @Test
    fun the_two_spellings_of_ibrahim_are_told_apart() = runAppTest {
        val shapes = results("ابراهيم").shapes
        assertEquals(listOf(54, 15), shapes.map { it.count })
        assertEquals(listOf("إبرهيم", "إبرهم"), shapes.map { QuranText.skeleton(it.shape) })
        // The second spelling is al-Baqara's.
        assertEquals(AyahKey(2, 124), shapes[1].ayahs.first().key)
    }

    @Test
    fun ibad_splits_by_spelling_and_by_word() = runAppTest {
        val shapes = results("عباد").shapes
        assertEquals(listOf(92, 7, 3, 2, 1, 1), shapes.map { it.count })
        val short = shapes.single { QuranText.skeleton(it.shape) == "عبد" }
        assertEquals(listOf(AyahKey(38, 45), AyahKey(43, 19), AyahKey(89, 29)), short.ayahs.map { it.key })
        // al-Furqan 63 («وعباد») is with the usual spelling: the stem leaves the «و» out.
        val usual = shapes.first()
        assertEquals("عباد", QuranText.skeleton(usual.shape))
        assertTrue(usual.ayahs.any { it.key == AyahKey(25, 63) })
    }

    @Test
    fun every_match_is_in_exactly_one_shape() = runAppTest {
        val queries = listOf("الرحمن" to TermMode.Letters, "رب العالمين" to TermMode.Phrase, "صبر" to TermMode.Root)
        queries.forEach { (text, mode) ->
            val result = results(text, mode)
            val matches = result.groups.sumOf { group -> group.ayahs.sumOf { it.matches.size } }
            assertEquals(matches, result.shapes.sumOf { it.count }, text)
        }
        assertEquals(1, results("الرحمن").shapes.size) // one spelling: nothing to tell apart
    }
}
