package app.qurancommunity.core.quran

import app.qurancommunity.core.common.AppResult
import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.model.DiyaaBlock
import app.qurancommunity.core.testing.TestDispatcherProvider
import app.qurancommunity.core.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * الضياء station 6 recounted from the shipped text by the definition in docs/content/station-6-counts.md: a verse
 * counts for a tree when one of its words is the tree's name, with only و ف, ب ل ك and ال attached and an optional
 * tanween alef or ة. The names are letter skeletons used for matching; nothing here is shown.
 */
class StationSixCountsTest : PlatformTest() {

    private val dispatchers by lazy { TestDispatcherProvider() }
    private val quran by lazy { SqlQuranReader(QuranDatabaseProvider(newTestStorage()), dispatchers) }

    private val names = mapOf(
        "النخل" to listOf("نخل", "نخلة", "نخيل"),
        "الأعناب" to listOf("عنب", "أعناب"),
        "الزيتون" to listOf("زيتون", "زيتونة"),
        "الرمان" to listOf("رمان"),
        "التين" to listOf("تين"),
    )

    private fun matcher(bases: List<String>) =
        Regex("[وف]?[بلك]?(ال|ل)?(" + bases.joinToString("|") { Regex.escape(it) } + ")[اة]?")

    @Test
    fun the_counts_shown_are_the_counts_in_the_text() = runAppTest {
        val basmala = (quran.ayah(AyahKey(1, 1)) as AppResult.Success).value.tanzilText.split(' ')
        val matchers = names.mapValues { (_, bases) -> matcher(bases) }
        val verses = names.keys.associateWith { mutableListOf<String>() }
        for (surah in 1..AyahKey.SURAH_COUNT) {
            var number = 1
            while (true) {
                val ayah = (quran.ayah(AyahKey(surah, number)) as? AppResult.Success)?.value ?: break
                val words = QuranText.words(ayah.key, ayah.tanzilText, basmala).map(QuranText::skeleton)
                matchers.forEach { (tree, regex) ->
                    if (words.any { regex.matches(it) }) verses.getValue(tree) += ayah.key.toString()
                }
                number++
            }
        }
        assertEquals(EXPECTED, verses, "docs/content/station-6-counts.md")

        val content = BundledContentReader(quran, dispatchers)
        val bars = (content.diyaa() as AppResult.Success).value.stations.first { it.number == 6 }.blocks
            .filterIsInstance<DiyaaBlock.CountBars>().single().items
        assertEquals(verses.mapValues { it.value.size }, bars.associate { it.name to it.count })
    }

    private companion object {
        val EXPECTED = mapOf(
            "النخل" to listOf(
                "2:266", "6:99", "6:141", "13:4", "16:11", "16:67", "17:91", "18:32", "19:23", "19:25",
                "20:71", "23:19", "26:148", "36:34", "50:10", "54:20", "55:11", "55:68", "69:7", "80:29",
            ),
            "الأعناب" to listOf(
                "2:266", "6:99", "13:4", "16:11", "16:67", "17:91", "18:32", "23:19", "36:34", "78:32", "80:28",
            ),
            "الزيتون" to listOf("6:99", "6:141", "16:11", "24:35", "80:29", "95:1"),
            "الرمان" to listOf("6:99", "6:141", "55:68"),
            "التين" to listOf("95:1"),
        )
    }
}
