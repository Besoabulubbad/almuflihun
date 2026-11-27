package app.almuflihun.core.quran

import app.almuflihun.core.common.AppError
import app.almuflihun.core.common.AppResult
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.StudyBasis
import app.almuflihun.core.model.WordOccurrence
import app.almuflihun.core.model.WordRange
import app.almuflihun.core.testing.TestDispatcherProvider
import app.almuflihun.core.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Word study on the real database (ADR 0012, S3). Counts match the morphology's own (see MorphologyTest). */
class WordStudyTest : PlatformTest() {

    private val study by lazy {
        val dispatchers = TestDispatcherProvider()
        SqlQuranStudy(QuranDatabaseProvider(newTestStorage()), dispatchers)
    }

    private suspend fun studyOf(surah: Int, ayah: Int, position: Int) =
        (study.wordStudy(AyahKey(surah, ayah), position) as AppResult.Success).value

    @Test
    fun a_word_with_a_root_gathers_every_word_of_that_root_by_form() = runAppTest {
        val sabr = studyOf(2, 153, 5) // «بالصبر»
        assertEquals(StudyBasis.Root, sabr.basis)
        assertEquals("صبر", sabr.root)
        assertEquals(103, sabr.occurrences.size)
        assertEquals(93, sabr.ayahCount)
        // By lemma, so the verb صَبَرَ (58) and the noun صَبْر (15) are separate forms.
        assertEquals(listOf(58, 20, 15, 4, 3, 1, 1, 1), sabr.forms.map { it.occurrences.size })
        assertEquals("صبر", QuranText.skeleton(sabr.forms.first().lemma!!))
        val mushafOrder = compareBy<WordOccurrence>({ it.range.key.surah }, { it.range.key.ayah })
        assertEquals(sabr.occurrences.sortedWith(mushafOrder), sabr.occurrences)
        // The tapped word itself, placed on the content words («يا أيها» is one Mushaf word).
        assertTrue(sabr.occurrences.any { it.range == WordRange(AyahKey(2, 153), 6, 6) && it.position == 5 })
    }

    @Test
    fun a_word_without_a_root_falls_back_to_its_lemma_then_its_stem() = runAppTest {
        val alladhina = studyOf(2, 153, 2) // «الذين»
        assertEquals(StudyBasis.Lemma, alladhina.basis)
        assertEquals(null, alladhina.root)
        assertEquals(1464, alladhina.occurrences.size)
        assertEquals(1243, alladhina.ayahCount)

        val bare = studyOf(2, 181, 4) // one of the three words with no root, lemma or stem
        assertEquals(StudyBasis.Word, bare.basis)
        assertEquals(1, bare.occurrences.size)
    }

    @Test
    fun only_real_words_can_be_studied() = runAppTest {
        val marker = study.wordStudy(AyahKey(2, 153), 11) // the ayah-end marker
        assertEquals(AppResult.Failure(AppError.NotFound("2:153:11")), marker)
        assertEquals(AppResult.Failure(AppError.NotFound("1:1:99")), study.wordStudy(AyahKey(1, 1), 99))
    }
}
