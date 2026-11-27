package app.almuflihun.core.quran

import app.almuflihun.core.common.AppResult
import app.almuflihun.core.common.DispatcherProvider
import app.almuflihun.core.common.runCatchingApp
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.MushafPage
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

/** Offline search over the shipped text (docs/specs/screens.md §11). */
interface QuranSearch {
    /** Ayahs containing [query] by letters (see [QuranText.searchForm]); a match may start or end inside a word. */
    suspend fun words(query: String, limit: Int = DEFAULT_LIMIT): AppResult<SearchResult>

    /**
     * A jump typed as `2:255` or as a page word and a number (`صفحة 50`); Arabic-Indic digits are accepted.
     * [pageWords] come from the UI's strings, so the parser carries no copy. Null when the text is not a jump or
     * points nowhere (2:300, page 700).
     */
    suspend fun jump(text: String, pageWords: Set<String>): AppResult<Jump?>

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

    /** One ayah's words in search form, joined by spaces, with where each word starts. */
    private class IndexedAyah(val key: AyahKey, val text: String, val starts: IntArray)

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

    override suspend fun jump(text: String, pageWords: Set<String>): AppResult<Jump?> = runCatchingApp {
        val latin = text.trim().map(::latinDigit).joinToString("")
        AYAH.matchEntire(latin)?.let { m ->
            return@runCatchingApp existingAyah(m.groupValues[1].toInt(), m.groupValues[2].toInt())?.let(Jump::ToAyah)
        }
        val parts = latin.split(WHITESPACE)
        val page = parts.takeIf { it.size == 2 && it[0] in pageWords }?.get(1)?.toIntOrNull()
        page?.takeIf { it in 1..MushafPage.COUNT }?.let(Jump::ToPage)
    }

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
            rows.map { row ->
                val key = AyahKey(row.surah.toInt(), row.ayah.toInt())
                val forms = QuranText.words(key, row.simple, basmala).map(QuranText::searchForm)
                val starts = IntArray(forms.size)
                var offset = 0
                forms.forEachIndexed { i, word ->
                    starts[i] = offset
                    offset += word.length + 1
                }
                IndexedAyah(key, forms.joinToString(" "), starts)
            }
        }.also { index = it }
    }

    private fun latinDigit(c: Char): Char = when (c) {
        in '٠'..'٩' -> '0' + (c - '٠')
        in '۰'..'۹' -> '0' + (c - '۰')
        else -> c
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
        val AYAH = Regex("(\\d{1,3})\\s*[:：]\\s*(\\d{1,3})")
    }
}
