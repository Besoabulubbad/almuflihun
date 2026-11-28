package app.qurancommunity.core.quran

import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.model.WordRange

/**
 * Lines up the content's words ([QuranText.words], from the Tanzil simple text) with the printed Mushaf's words (the
 * layout's word list), so a quote placed by its word range can be highlighted on the page.
 *
 * The two texts split words the same way except where the Uthmani script writes two or three words as one: a call
 * («يا» or «ويا», and «ها») joined to the word after it, «يا ابن أم» as one word, and two single places. That is the
 * whole rule; a test checks it against the Mushaf's words in all 6,236 ayahs. The keys below are letter skeletons
 * used for comparison only; nothing here is shown to a reader.
 */
object MushafAlignment {

    private val calls = setOf("يا", "ويا", "ها")
    private val sonOfMother = listOf("يا", "ابن", "أم")

    /** Places where the Mushaf writes two words as one, outside the calls. */
    private val joined = mapOf(
        AyahKey(37, 130) to listOf("إل", "ياسين"),
        AyahKey(72, 16) to listOf("وأن", "لو"),
    )

    /** For each Mushaf word of the ayah, in order, the content words (0-based indices) it is written as. */
    fun groups(key: AyahKey, words: List<String>): List<IntRange> {
        val skeletons = words.map(QuranText::skeleton)
        val result = mutableListOf<IntRange>()
        var i = 0
        while (i < skeletons.size) {
            val size = when {
                skeletons.startsWith(i, sonOfMother) -> sonOfMother.size
                skeletons[i] in calls && i + 1 < skeletons.size -> 2
                joined[key]?.let { skeletons.startsWith(i, it) } == true -> 2
                else -> 1
            }
            result += i until i + size
            i += size
        }
        return result
    }

    /**
     * The Mushaf word positions (1-based, the ayah-end marker not counted) that show [range]. A range that starts or
     * ends inside a joined word covers that whole word.
     */
    fun mushafPositions(range: WordRange, words: List<String>): IntRange {
        val groups = groups(range.key, words)
        val first = groups.indexOfFirst { range.first - 1 in it }
        val last = groups.indexOfFirst { range.last - 1 in it }
        require(first >= 0 && last >= 0) { "Range $range is outside ${range.key}'s ${words.size} words" }
        return first + 1..last + 1
    }

    private fun List<String>.startsWith(index: Int, prefix: List<String>): Boolean =
        index + prefix.size <= size && subList(index, index + prefix.size) == prefix
}
