package app.almuflihun.core.quran

import app.almuflihun.core.common.AppResult
import app.almuflihun.core.common.DispatcherProvider
import app.almuflihun.core.common.runCatchingApp
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.MushafPage
import app.almuflihun.core.model.Surah
import app.almuflihun.core.model.WordRange
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

/** How a term is matched (DESIGN «Advanced search»: كلمة / جذر / لفظ / عبارة). Root and lemma come with morphology. */
enum class TermMode {
    /** كلمة: the letters anywhere, so «الصبر» also finds «بالصبر». */
    Letters,

    /** عبارة: these whole words, in this order. */
    Phrase,
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
 * Results grouped by surah in Mushaf order, with the total on top (DESIGN «Advanced search»). At most the limit of
 * ayahs is listed; [truncated] then asks the reader to narrow.
 */
data class GroupedResults(val totalAyahs: Int, val groups: List<SurahResults>, val truncated: Boolean)

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
    private val provider: QuranDatabaseProvider,
    private val reader: QuranReader,
    private val dispatchers: DispatcherProvider,
) : QuranSearch {

    /** One ayah's words in search form (and joined by spaces, with where each starts), and its divisions. */
    private class IndexedAyah(
        val key: AyahKey,
        val words: List<String>,
        val text: String,
        val starts: IntArray,
        val quarter: Int,
        val meccan: Boolean,
    ) {
        val juz: Int get() = quarter / QUARTERS_PER_JUZ + 1
        val hizb: Int get() = quarter / QUARTERS_PER_HIZB + 1
    }

    private val mutex = Mutex()
    private var index: List<IndexedAyah>? = null

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
                    val found = scoped.mapNotNull { ayah -> ayah.matches(term)?.let { ayah.key to it } }.toMap()
                    result = combine(result, found, if (result == null) Join.Or else term.join)
                }
                group(result.orEmpty(), limit)
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

    private fun IndexedAyah.isIn(scope: SearchScope): Boolean = when (scope) {
        SearchScope.Quran -> true
        is SearchScope.Surah -> key.surah == scope.number
        is SearchScope.Juz -> juz == scope.number
        is SearchScope.Hizb -> hizb == scope.number
        is SearchScope.Range -> !key.isBefore(scope.from) && !scope.to.isBefore(key)
        SearchScope.Meccan -> meccan
        SearchScope.Medinan -> !meccan
    }

    /** This ayah's matches for [term], or null when it has none. */
    private fun IndexedAyah.matches(term: SearchTerm): List<WordRange>? {
        val wanted = term.text.trim().split(WHITESPACE).map(QuranText::searchForm).filter { it.isNotEmpty() }
        if (wanted.isEmpty()) return null
        return when (term.mode) {
            TermMode.Letters -> match(wanted.joinToString(" "))?.matches
            TermMode.Phrase -> (0..words.size - wanted.size)
                .filter { words.subList(it, it + wanted.size) == wanted }
                .map { WordRange(key, it + 1, it + wanted.size) }
                .ifEmpty { null }
        }
    }

    private fun AyahKey.isBefore(other: AyahKey): Boolean =
        surah < other.surah || (surah == other.surah && ayah < other.ayah)

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

    /** Built once, on first search, from the shipped text. */
    private suspend fun index(): List<IndexedAyah> = mutex.withLock {
        index ?: withContext(dispatchers.io) {
            val rows = provider.get().quranQueries.allAyahs().executeAsList()
            val basmala = rows.first().simple.split(' ')
            val queries = provider.get().quranQueries
            val quarters = queries.quarters().executeAsList().map { AyahKey(it.surah.toInt(), it.ayah.toInt()) }
            val meccan = queries.surahs { number, _, _, place, _, _ -> number.toInt() to (place == "meccan") }
                .executeAsList().toMap()
            var quarter = 0
            rows.map { row ->
                val key = AyahKey(row.surah.toInt(), row.ayah.toInt())
                while (quarter + 1 < quarters.size && !key.isBefore(quarters[quarter + 1])) quarter++
                val forms = QuranText.words(key, row.simple, basmala).map(QuranText::searchForm)
                val starts = IntArray(forms.size)
                var offset = 0
                forms.forEachIndexed { i, word ->
                    starts[i] = offset
                    offset += word.length + 1
                }
                IndexedAyah(key, forms, forms.joinToString(" "), starts, quarter, meccan.getValue(key.surah))
            }
        }.also { index = it }
    }

    private fun latinDigit(c: Char): Char = when (c) {
        in '٠'..'٩' -> '0' + (c - '٠')
        in '۰'..'۹' -> '0' + (c - '۰')
        else -> c
    }

    private companion object {
        const val QUARTERS_PER_HIZB = 4
        const val QUARTERS_PER_JUZ = 8
        const val ARTICLE = "ال"
        val WHITESPACE = Regex("\\s+")
        val AYAH = Regex("(\\d{1,3})\\s*[:：]\\s*(\\d{1,3})")
    }
}
