package app.qurancommunity.core.quran

import app.qurancommunity.core.common.AppResult
import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.model.WordRange
import app.qurancommunity.core.testing.TestDispatcherProvider
import app.qurancommunity.core.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Repeated phrases on the real database; counts pinned from an independent computation on the Tanzil file. */
class SharedPhrasesTest : PlatformTest() {

    private val dispatchers by lazy { TestDispatcherProvider() }
    private val provider by lazy { QuranDatabaseProvider(newTestStorage()) }
    private val study by lazy { SqlQuranStudy(provider, dispatchers) }

    private suspend fun phrases(surah: Int, ayah: Int) =
        (study.sharedPhrases(AyahKey(surah, ayah)) as AppResult.Success).value

    @Test
    fun an_ayah_lists_its_phrases_longest_first_with_every_other_place() = runAppTest {
        val kursi = phrases(2, 255)
        assertEquals(WordRange(AyahKey(2, 255), 27, 34), kursi.first().here)
        assertEquals(1, kursi.first().elsewhere.size)
        assertEquals(kursi.sortedByDescending { it.words }.map { it.words }, kursi.map { it.words })

        val refrain = phrases(55, 13).single()
        assertEquals(WordRange(AyahKey(55, 13), 1, 4), refrain.here)
        assertEquals(30, refrain.elsewhere.size)
        assertTrue(refrain.elsewhere.all { it.key.surah == 55 })

        assertEquals(emptyList(), phrases(112, 1))
    }

    @Test
    fun every_place_of_a_phrase_holds_the_same_words_as_the_app_reads_them() = runAppTest {
        // The index is built on the JVM at compile time; this proves its normalisation matches the app's.
        val queries = provider.get().quranQueries
        val reader = SqlQuranReader(provider, dispatchers)
        val basmala = (reader.ayah(AyahKey(1, 1)) as AppResult.Success).value.tanzilText.split(' ')
        val words = mutableMapOf<AyahKey, List<String>>()
        suspend fun wordsOf(key: AyahKey) = words.getOrPut(key) {
            val ayah = (reader.ayah(key) as AppResult.Success).value
            QuranText.words(key, ayah.tanzilText, basmala).map(QuranText::searchForm)
        }
        val rows = queries.allPhrasePlaces().executeAsList()
        val mismatched = rows.groupBy { it.phrase }.filter { (_, places) ->
            places.map { p ->
                wordsOf(AyahKey(p.surah.toInt(), p.ayah.toInt())).subList(p.first_word.toInt() - 1, p.last_word.toInt())
            }.toSet().size != 1
        }.keys
        assertEquals(6459, rows.size)
        assertEquals(emptySet(), mismatched)
    }
}
