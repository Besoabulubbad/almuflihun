package app.qurancommunity.core.quran

import app.qurancommunity.core.common.AppResult
import app.qurancommunity.core.common.DispatcherProvider
import app.qurancommunity.core.common.runCatchingApp
import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.model.MushafPage
import app.qurancommunity.core.model.Surah
import app.qurancommunity.core.model.WordRange
import kotlinx.coroutines.withContext

/** An ayah that matched, with every match placed as a word range for highlighting. */
data class AyahMatch(val key: AyahKey, val matches: List<WordRange>)

/** [ayahs] in Mushaf order, at most the limit asked for; [total] counts every matching ayah. */
data class SearchResult(val total: Int, val ayahs: List<AyahMatch>)

/** Where a typed jump leads. */
sealed interface Jump {
    data class ToAyah(val key: AyahKey) : Jump
    data class ToPage(val page: Int) : Jump
}

/** How a term is matched (DESIGN «Advanced search»: كلمة / جذر / لفظ / عبارة). */
enum class TermMode {
    /** كلمة: the letters anywhere, so «الصبر» also finds «بالصبر». */
    Letters,

    /** عبارة: these whole words, in this order. */
    Phrase,

    /** جذر: every word from this root («صبر» or «ص ب ر»), whatever its form (QUL morphology). */
    Root,

    /** لفظ: every word of this lemma, as word study names it (QUL morphology). */
    Lemma,
}

/** How a term joins the result of the terms above it: «و», «أو», «ليس». */
enum class Join { And, Or, Not }

/** One line of an advanced search. The first term's [join] is ignored. */
data class SearchTerm(val text: String, val mode: TermMode = TermMode.Letters, val join: Join = Join.And)

/** Where to search (DESIGN: القرآن كله، سورة، جزء، حزب، نطاق، مكية / مدنية). */
sealed interface SearchScope {
    data object Quran : SearchScope
    data class Surah(val number: Int) : SearchScope
    data class Juz(val number: Int) : SearchScope
    data class Hizb(val number: Int) : SearchScope

    /** From [from] to [to], both included. */
    data class Range(val from: AyahKey, val to: AyahKey) : SearchScope
    data object Meccan : SearchScope
    data object Medinan : SearchScope
}

/** Terms read from top to bottom, within a scope. */
data class SearchQuery(val terms: List<SearchTerm>, val scope: SearchScope = SearchScope.Quran)

/** One surah's results: [count] matching ayahs in it, [ayahs] those listed (all, unless the cap was reached). */
data class SurahResults(val surah: Int, val count: Int, val ayahs: List<AyahMatch>)

/**
 * One way the Mushaf writes what was found (owner, 2026-11-27): the same word can be spelled differently in places
 * («إِبْرَٰهِيمَ» / «إِبْرَٰهِۦمَ», «عِبَادِ» / «عِبَٰدَ»), and different words can share the letters searched for.
 * [shape] is the Mushaf's spelling of the word's stem (as first found); [count] the matches written so; [ayahs] those
 * listed, each with only the matches of this shape.
 */
data class ShapeResults(val shape: String, val count: Int, val ayahs: List<AyahMatch>)

/**
 * Results grouped by surah in Mushaf order, with the total on top (DESIGN «Advanced search»), and the same matches
 * grouped by how the Mushaf writes them, most frequent first ([shapes], «حسب الرسم»). At most the limit of ayahs is
 * listed in each view; [truncated] then asks the reader to narrow.
 */
data class GroupedResults(
    val totalAyahs: Int,
    val groups: List<SurahResults>,
    val truncated: Boolean,
    val shapes: List<ShapeResults> = emptyList(),
)

/** Offline search over the shipped text (docs/specs/screens.md §11). */
interface QuranSearch {
    /** Ayahs containing [query] by letters (see [QuranText.searchForm]); a match may start or end inside a word. */
    suspend fun words(query: String, limit: Int = DEFAULT_LIMIT): AppResult<SearchResult>

    /**
     * A jump typed as `2:255`, as a surah's name and an ayah (`البقرة 255`), or as a page word and a number
     * (`صفحة 50`); Arabic-Indic digits are accepted. [pageWords] and [surahWords] (an optional word before the name,
     * `سورة`) come from the UI's strings, so the parser carries no copy. Null when the text is not a jump or points
     * nowhere (2:300, page 700).
     */
    suspend fun jump(text: String, pageWords: Set<String>, surahWords: Set<String> = emptySet()): AppResult<Jump?>

    /**
     * Advanced search (ADR 0012, S2): the terms from top to bottom, each joined to the result so far by «و»
     * (both), «أو» (either) or «ليس» (not), within the scope. Matches of the «و» and «أو» terms are placed for
     * highlighting; «ليس» terms only remove ayahs.
     */
    suspend fun advanced(query: SearchQuery, limit: Int = DEFAULT_LIMIT): AppResult<GroupedResults>

    /** Surahs whose name contains [query] by letters (with or without «ال»), or whose number it is. */
    suspend fun surahs(query: String): AppResult<List<Surah>>

    companion object {
        /** DESIGN «Advanced search»: show the first 200 and ask to narrow. */
        const val DEFAULT_LIMIT = 200
    }
}

internal class IndexedQuranSearch(
    provider: QuranDatabaseProvider,
    private val reader: QuranReader,
    private val dispatchers: DispatcherProvider,
    private val textIndex: QuranTextIndex = QuranTextIndex(provider, dispatchers),
) : QuranSearch {

    override suspend fun words(query: String, limit: Int): AppResult<SearchResult> = withContext(dispatchers.default) {
        runCatchingApp {
            val wanted = query.trim().split(WHITESPACE).map(QuranText::searchForm).filter { it.isNotEmpty() }
                .joinToString(" ")
            if (wanted.isEmpty()) {
                SearchResult(0, emptyList())
            } else {
                val found = index().mapNotNull { ayah -> ayah.match(wanted) }
                SearchResult(found.size, found.take(limit))
            }
        }
    }

    override suspend fun jump(text: String, pageWords: Set<String>, surahWords: Set<String>): AppResult<Jump?> =
        runCatchingApp {
            val latin = text.trim().map(::latinDigit).joinToString("")
            AYAH.matchEntire(latin)?.let { m ->
                return@runCatchingApp existingAyah(m.groupValues[1].toInt(), m.groupValues[2].toInt())
                    ?.let(Jump::ToAyah)
            }
            val parts = latin.split(WHITESPACE).filter { it.isNotEmpty() }
            val number = parts.lastOrNull()?.toIntOrNull()
            val words = parts.dropLast(1).dropWhile { it in surahWords }
            when {
                number == null || words.isEmpty() -> null
                words.size == 1 && words[0] in pageWords ->
                    number.takeIf { it in 1..MushafPage.COUNT }?.let(Jump::ToPage)
                else ->
                    surahNamed(words.joinToString(" "))?.let { existingAyah(it.number, number) }?.let(Jump::ToAyah)
            }
        }

    override suspend fun surahs(query: String): AppResult<List<Surah>> = runCatchingApp {
        val all = (reader.surahs() as? AppResult.Success)?.value ?: error("surahs unavailable")
        val latin = query.trim().map(::latinDigit).joinToString("")
        latin.toIntOrNull()?.let { n -> return@runCatchingApp all.filter { it.number == n } }
        val wanted = nameForm(latin)
        if (wanted.isEmpty()) emptyList() else all.filter { nameForm(it.name).contains(wanted) }
    }

    /** The surah whose whole name is [name], by letters, with or without «ال». */
    private suspend fun surahNamed(name: String): Surah? {
        val wanted = nameForm(name)
        return (reader.surahs() as? AppResult.Success)?.value?.firstOrNull { nameForm(it.name) == wanted }
    }

    /** A surah name for comparison: its search form without spaces and without a leading «ال». */
    private fun nameForm(name: String): String =
        QuranText.searchForm(name).filterNot { it == ' ' }.removePrefix(ARTICLE)

    override suspend fun advanced(query: SearchQuery, limit: Int): AppResult<GroupedResults> =
        withContext(dispatchers.default) {
            runCatchingApp {
                val scoped = index().filter { it.isIn(query.scope) }
                var result: LinkedHashMap<AyahKey, MutableList<WordRange>>? = null
                query.terms.filter { term -> term.text.isNotBlank() }.forEach { term ->
                    val prepared = prepare(term) ?: return@forEach
                    val found = scoped.mapNotNull { ayah -> ayah.matches(prepared)?.let { ayah.key to it } }.toMap()
                    result = combine(result, found, if (result == null) Join.Or else term.join)
                }
                val found = result.orEmpty()
                group(found, limit).copy(shapes = byShape(found, scoped.associateBy { it.key }, limit))
            }
        }

    private fun combine(
        so: LinkedHashMap<AyahKey, MutableList<WordRange>>?,
        found: Map<AyahKey, List<WordRange>>,
        join: Join,
    ): LinkedHashMap<AyahKey, MutableList<WordRange>> {
        val current = so ?: LinkedHashMap()
        return when (join) {
            Join.Not -> LinkedHashMap(current.filterKeys { it !in found })
            Join.And -> LinkedHashMap(
                current.filterKeys { it in found }
                    .mapValues { (key, ranges) -> ranges.apply { addAll(found.getValue(key)) } },
            )
            Join.Or -> {
                found.forEach { (key, ranges) -> current.getOrPut(key) { mutableListOf() }.addAll(ranges) }
                // Keep Mushaf order after a union.
                LinkedHashMap(current.toList().sortedWith(compareBy({ it.first.surah }, { it.first.ayah })).toMap())
            }
        }
    }

    private fun group(result: Map<AyahKey, MutableList<WordRange>>, limit: Int): GroupedResults {
        var listed = 0
        val groups = result.entries.groupBy { it.key.surah }.map { (surah, entries) ->
            val shown = entries.take((limit - listed).coerceAtLeast(0)).map { (key, ranges) ->
                AyahMatch(key, ranges.distinct().sortedBy { it.first })
            }
            listed += shown.size
            SurahResults(surah, entries.size, shown)
        }
        return GroupedResults(result.size, groups, truncated = result.size > limit)
    }

    /**
     * The matches grouped by the Mushaf's spelling of the words they fall on (each Mushaf word's stem; for a match
     * over several words, their shapes in order). Shapes are compared by their letters, so case endings do not split
     * a shape; the label is the first spelling met.
     */
    private fun byShape(
        found: Map<AyahKey, List<WordRange>>,
        ayahs: Map<AyahKey, IndexedAyah>,
        limit: Int,
    ): List<ShapeResults> {
        class Shape(val label: String) {
            var count = 0
            val ayahs = LinkedHashMap<AyahKey, MutableList<WordRange>>()
        }
        val shapes = LinkedHashMap<String, Shape>()
        found.forEach { (key, ranges) ->
            val ayah = ayahs[key] ?: return@forEach
            ranges.distinct().forEach { range ->
                val words = ayah.groups.indices.filter { i ->
                    val group = ayah.groups[i]
                    group.first < range.last && group.last >= range.first - 1
                }
                if (words.isNotEmpty()) {
                    val keyText = words.joinToString(" ") { ayah.shapeKeys[it] }
                    val shape = shapes.getOrPut(keyText) { Shape(words.joinToString(" ") { ayah.shapes[it] }) }
                    shape.count++
                    shape.ayahs.getOrPut(key) { mutableListOf() } += range
                }
            }
        }
        var listed = 0
        return shapes.values.sortedByDescending { it.count }.map { shape ->
            val shown = shape.ayahs.entries.take((limit - listed).coerceAtLeast(0))
                .map { (key, ranges) -> AyahMatch(key, ranges.sortedBy { it.first }) }
            listed += shown.size
            ShapeResults(shape.label, shape.count, shown)
        }
    }

    private fun IndexedAyah.isIn(scope: SearchScope): Boolean = when (scope) {
        SearchScope.Quran -> true
        is SearchScope.Surah -> key.surah == scope.number
        is SearchScope.Juz -> juz == scope.number
        is SearchScope.Hizb -> hizb == scope.number
        is SearchScope.Range -> !key.isBefore(scope.from) && !scope.to.isBefore(key)
        SearchScope.Meccan -> meccan
        SearchScope.Medinan -> !meccan
    }

    /** A term normalised once, before it is compared with every ayah. */
    private class PreparedTerm(val mode: TermMode, val words: List<String>, val joined: String, val value: String)

    private fun prepare(term: SearchTerm): PreparedTerm? {
        val words = term.text.trim().split(WHITESPACE).map(QuranText::searchForm).filter { it.isNotEmpty() }
        if (words.isEmpty()) return null
        val value = when (term.mode) {
            TermMode.Root -> rootForm(term.text)
            else -> words.joinToString("")
        }
        return PreparedTerm(term.mode, words, words.joinToString(" "), value)
    }

    /** This ayah's matches for [term], or null when it has none. */
    private fun IndexedAyah.matches(term: PreparedTerm): List<WordRange>? = when (term.mode) {
        TermMode.Letters -> match(term.joined)?.matches
        TermMode.Phrase -> (0..words.size - term.words.size)
            .filter { words.subList(it, it + term.words.size) == term.words }
            .map { WordRange(key, it + 1, it + term.words.size) }
            .ifEmpty { null }
        TermMode.Root -> mushafMatches(roots, term.value)
        TermMode.Lemma -> mushafMatches(lemmas, term.value)
    }

    /** The content words of each Mushaf word whose value is [wanted]. */
    private fun IndexedAyah.mushafMatches(values: List<String?>, wanted: String): List<WordRange>? =
        values.indices.filter { values[it] == wanted && it < groups.size }
            .map { WordRange(key, groups[it].first + 1, groups[it].last + 1) }
            .ifEmpty { null }

    private suspend fun existingAyah(surah: Int, ayah: Int): AyahKey? {
        if (surah !in 1..AyahKey.SURAH_COUNT || ayah < 1) return null
        val key = AyahKey(surah, ayah)
        return key.takeIf { reader.ayah(it) is AppResult.Success }
    }

    private fun IndexedAyah.match(wanted: String): AyahMatch? {
        val ranges = mutableListOf<WordRange>()
        var from = text.indexOf(wanted)
        while (from >= 0) {
            ranges += WordRange(key, wordAt(from) + 1, wordAt(from + wanted.length - 1) + 1)
            from = text.indexOf(wanted, from + 1)
        }
        return if (ranges.isEmpty()) null else AyahMatch(key, ranges.distinct())
    }

    /** The index of the word containing character [offset]. */
    private fun IndexedAyah.wordAt(offset: Int): Int {
        var low = 0
        var high = starts.size - 1
        while (low < high) {
            val mid = (low + high + 1) / 2
            if (starts[mid] <= offset) low = mid else high = mid - 1
        }
        return low
    }

    private suspend fun index(): List<IndexedAyah> = textIndex.ayahs()

    private fun latinDigit(c: Char): Char = when (c) {
        in '٠'..'٩' -> '0' + (c - '٠')
        in '۰'..'۹' -> '0' + (c - '۰')
        else -> c
    }

    private companion object {
        const val ARTICLE = "ال"
        val WHITESPACE = Regex("\\s+")
        val AYAH = Regex("(\\d{1,3})\\s*[:：]\\s*(\\d{1,3})")
    }
}
