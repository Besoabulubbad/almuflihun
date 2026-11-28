package app.qurancommunity.core.quran

import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.model.WordRange

/**
 * Places quotes from the content (doors, slider, الضياء) in the shipped text (DESIGN §1: a quote appears only with its
 * full verse, placed by its word range). The quote's own string is only checked, never shown: the app shows the
 * shipped words in the range.
 */
object QuotePlacer {

    sealed interface Result {
        data class Placed(val range: WordRange) : Result
        data class Mismatch(val reason: String) : Result
    }

    /**
     * Checks [quote] against [ayahWords]. With a declared [range], the words there must have the quote's skeleton;
     * without one, the quote must occur exactly once and that occurrence is returned.
     */
    fun place(key: AyahKey, ayahWords: List<String>, quote: String, range: IntRange? = null): Result {
        val wanted = words(quote)
        val have = ayahWords.map(QuranText::skeleton)
        return when {
            wanted.isEmpty() -> Result.Mismatch("$key: empty quote")
            range != null -> checkRange(key, have, wanted, range)
            else -> find(key, have, wanted)
        }
    }

    private fun checkRange(key: AyahKey, have: List<String>, wanted: List<String>, range: IntRange): Result {
        if (range.first < 1 || range.last > have.size) {
            return Result.Mismatch("$key: range $range outside the ayah's ${have.size} words")
        }
        val found = have.subList(range.first - 1, range.last)
        return if (found == wanted) {
            Result.Placed(WordRange(key, range.first, range.last))
        } else {
            Result.Mismatch("$key: words $range are ${shown(found)}, quote is ${shown(wanted)}")
        }
    }

    private fun find(key: AyahKey, have: List<String>, wanted: List<String>): Result {
        val starts = (0..have.size - wanted.size).filter { have.subList(it, it + wanted.size) == wanted }
        return when (starts.size) {
            1 -> Result.Placed(WordRange(key, starts.single() + 1, starts.single() + wanted.size))
            0 -> Result.Mismatch("$key: quote ${shown(wanted)} not found")
            else -> Result.Mismatch("$key: quote ${shown(wanted)} occurs ${starts.size} times; add its range")
        }
    }

    /**
     * A «points» fragment (DESIGN: a literal piece of the verse, written without marks). It may be cut inside a word
     * (العدل from بالعدل), so it is checked by letters within the verses' text.
     */
    fun containsFragment(versesWords: List<List<String>>, fragment: String): Boolean {
        val wanted = words(fragment).joinToString(" ")
        // Joined across the entry's verses: a fragment may run from one verse into the next.
        val text = versesWords.flatten().joinToString(" ", transform = QuranText::skeleton)
        return wanted.isNotEmpty() && text.contains(wanted)
    }

    private fun shown(words: List<String>): String = words.joinToString(" ", prefix = "«", postfix = "»")

    private fun words(text: String): List<String> =
        text.split(' ').map(QuranText::skeleton).filter { it.isNotEmpty() }
}
