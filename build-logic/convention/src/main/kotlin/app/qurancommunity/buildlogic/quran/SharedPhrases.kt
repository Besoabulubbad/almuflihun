package app.qurancommunity.buildlogic.quran

/**
 * The Quran's repeated phrases (ADR 0012, S4): runs of at least [MIN_WORDS] consecutive words that appear in two or
 * more ayahs, found from the text alone, with every place each one appears. Words are compared in the same form the
 * app searches with (core:quran QuranText.searchForm); a test in core:quran checks the two agree on every place.
 */
internal object SharedPhrases {

    const val MIN_WORDS = 4

    /** A phrase's place: ayah and its 1-based word range, counted as the content counts words. */
    data class Place(val surah: Int, val ayah: Int, val first: Int, val last: Int)

    /** [words]: each ayah's words in search form, keyed by (surah, ayah), in Mushaf order. */
    fun find(words: Map<Pair<Int, Int>, List<String>>): List<List<Place>> {
        val starts = HashMap<List<String>, MutableSet<Pair<Int, Int>>>()
        words.forEach { (key, ws) ->
            for (i in 0..ws.size - MIN_WORDS) starts.getOrPut(ws.subList(i, i + MIN_WORDS)) { linkedSetOf() } += key
        }
        val phrases = LinkedHashSet<List<String>>()
        starts.values.filter { it.size > 1 }.forEach { keys ->
            val list = keys.toList()
            for (i in list.indices) for (j in i + 1 until list.size) {
                phrases += maximalRuns(words.getValue(list[i]), words.getValue(list[j]))
            }
        }
        return phrases
            .map { phrase -> placesOf(phrase, words, starts.getValue(phrase.subList(0, MIN_WORDS))) }
            .sortedWith(compareByDescending<List<Place>> { it.first().last - it.first().first }.thenBy { it.size }
                .thenBy { it.first().surah }.thenBy { it.first().ayah }.thenBy { it.first().first })
    }

    /** Runs of at least [MIN_WORDS] equal words in [a] and [b] that cannot be extended on either side. */
    private fun maximalRuns(a: List<String>, b: List<String>): Set<List<String>> {
        val runs = HashSet<List<String>>()
        var previous = IntArray(b.size + 1)
        for (i in 1..a.size) {
            val current = IntArray(b.size + 1)
            for (j in 1..b.size) if (a[i - 1] == b[j - 1]) current[j] = previous[j - 1] + 1
            for (j in 1..b.size) {
                val length = current[j]
                val extends = i < a.size && j < b.size && a[i] == b[j]
                if (length >= MIN_WORDS && !extends) runs += a.subList(i - length, i)
            }
            previous = current
        }
        return runs
    }

    private fun placesOf(
        phrase: List<String>,
        words: Map<Pair<Int, Int>, List<String>>,
        candidates: Set<Pair<Int, Int>>,
    ): List<Place> = candidates.sortedWith(compareBy({ it.first }, { it.second })).flatMap { key ->
        val ws = words.getValue(key)
        (0..ws.size - phrase.size).filter { ws.subList(it, it + phrase.size) == phrase }
            .map { Place(key.first, key.second, it + 1, it + phrase.size) }
    }

    /** The app's search form (core:quran QuranText.searchForm), for the JVM. */
    fun searchForm(text: String): String = buildString {
        text.forEach { c ->
            val mark = Character.getType(c) == Character.NON_SPACING_MARK.toInt() || c == 'ـ' ||
                c in 'ۖ'..'ۭ' || c == '۞'
            if (!mark) {
                when (c) {
                    'أ', 'إ', 'آ', 'ٱ' -> append('ا')
                    'ى', 'ئ' -> append('ي')
                    'ة' -> append('ه')
                    'ؤ' -> append('و')
                    'ء' -> Unit
                    else -> append(c)
                }
            }
        }
    }

    /** An ayah's words as the content counts them (core:quran QuranText.words): no pause marks, no basmala prefix. */
    fun contentWords(surah: Int, ayah: Int, tanzil: String, basmala: List<String>): List<String> {
        val tokens = tanzil.split(' ').filter { t -> t.isNotEmpty() && !t.all { it in 'ۖ'..'ۭ' || it == '۞' } }
        val skeleton = { s: String -> s.filterNot { Character.getType(it) == Character.NON_SPACING_MARK.toInt() || it == 'ـ' } }
        val opens = ayah == 1 && surah != 1 && surah != 9 && tokens.size > basmala.size &&
            tokens.take(basmala.size).map(skeleton) == basmala.map(skeleton)
        return if (opens) tokens.drop(basmala.size) else tokens
    }
}
