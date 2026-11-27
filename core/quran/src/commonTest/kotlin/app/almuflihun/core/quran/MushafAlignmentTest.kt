package app.almuflihun.core.quran

import app.almuflihun.core.common.AppResult
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.WordRange
import app.almuflihun.core.testing.TestDispatcherProvider
import app.almuflihun.core.testing.runAppTest
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * [MushafAlignment] against the real data: in every ayah the rule gives exactly as many groups as the Mushaf has
 * words, and each group is spelled like its Mushaf word once the two scripts' spelling differences (alef, hamza
 * seats, alef maqsura, ta marbuta) are set aside.
 */
class MushafAlignmentTest : PlatformTest() {

    private val reader by lazy { SqlQuranReader(QuranDatabaseProvider(newTestStorage()), TestDispatcherProvider()) }

    @Test
    fun every_ayah_lines_up_with_the_mushaf_words() = runAppTest {
        val basmala = (reader.ayah(AyahKey(1, 1)) as AppResult.Success).value.tanzilText.split(' ')
        val failures = mutableListOf<String>()
        var joinedWords = 0
        var ayahs = 0
        for (surah in 1..AyahKey.SURAH_COUNT) {
            var number = 1
            while (true) {
                val ayah = (reader.ayah(AyahKey(surah, number)) as? AppResult.Success)?.value ?: break
                ayahs++
                val words = QuranText.words(ayah.key, ayah.tanzilText, basmala)
                val mushaf = ayah.words.filterNot { it.isAyahEnd }.map { QuranText.skeleton(it.text) }
                val groups = MushafAlignment.groups(ayah.key, words)
                if (groups.size != mushaf.size) {
                    failures += "${ayah.key}: ${groups.size} groups for ${mushaf.size} Mushaf words"
                } else {
                    groups.zip(mushaf).forEach { (group, word) ->
                        if (group.count() > 1) joinedWords++
                        val written = group.joinToString("") { QuranText.skeleton(words[it]) }
                        if (distance(written, word) > MAX_SPELLING_DISTANCE) {
                            failures += "${ayah.key}: group $group does not look like its Mushaf word"
                        }
                    }
                }
                number++
            }
        }
        assertTrue(failures.isEmpty(), failures.take(LISTED).joinToString("\n"))
        assertEquals(TOTAL_AYAHS, ayahs)
        assertEquals(JOINED_WORDS, joinedWords)
    }

    @Test
    fun a_range_inside_a_joined_word_covers_the_whole_word() {
        val key = AyahKey(3, 4)
        val words = listOf("قال", "يا", "قوم", "اعبدوا")
        assertEquals(listOf(0..0, 1..2, 3..3), MushafAlignment.groups(key, words))
        assertEquals(2..2, MushafAlignment.mushafPositions(WordRange(key, 3, 3), words))
        assertEquals(1..3, MushafAlignment.mushafPositions(WordRange(key, 1, 4), words))
    }

    /** Letters that the two scripts spell differently (alef forms, hamza and its seats, alef maqsura). */
    private fun comparable(text: String): String = text
        .filterNot { it in "اأإآٱءىئؤ" }
        .replace('ة', 'ه')

    /** Edit distance between the comparable forms, as a share of the longer one. */
    private fun distance(a: String, b: String): Double {
        val x = comparable(a)
        val y = comparable(b)
        var previous = IntArray(y.length + 1) { it }
        for (i in 1..x.length) {
            val current = IntArray(y.length + 1)
            current[0] = i
            for (j in 1..y.length) {
                val substitution = previous[j - 1] + if (x[i - 1] == y[j - 1]) 0 else 1
                current[j] = minOf(previous[j] + 1, current[j - 1] + 1, substitution)
            }
            previous = current
        }
        return previous[y.length].toDouble() / max(1, max(x.length, y.length))
    }

    private companion object {
        const val TOTAL_AYAHS = 6236
        const val JOINED_WORDS = 367 // 366 joined pairs and «يا ابن أم»
        const val MAX_SPELLING_DISTANCE = 0.5
        const val LISTED = 20
    }
}
