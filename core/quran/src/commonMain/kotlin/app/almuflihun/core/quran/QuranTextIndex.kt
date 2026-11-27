package app.almuflihun.core.quran

import app.almuflihun.core.common.DispatcherProvider
import app.almuflihun.core.model.AyahKey
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * One ayah prepared for search and study: its content words in search form (also joined, with where each starts),
 * its divisions, and per Mushaf word its root, lemma and the content words it is written as ([groups]).
 */
internal data class IndexedAyah(
    val key: AyahKey,
    val words: List<String>,
    val text: String,
    val starts: IntArray,
    val quarter: Int,
    val meccan: Boolean,
    val roots: List<String?>,
    val lemmas: List<String?>,
    val groups: List<IntRange>,
) {
    val juz: Int get() = quarter / QUARTERS_PER_JUZ + 1
    val hizb: Int get() = quarter / QUARTERS_PER_HIZB + 1

    private companion object {
        const val QUARTERS_PER_HIZB = 4
        const val QUARTERS_PER_JUZ = 8
    }
}

/** The whole text prepared once, in memory, and shared by search and study. */
internal class QuranTextIndex(
    private val provider: QuranDatabaseProvider,
    private val dispatchers: DispatcherProvider,
) {
    private val mutex = Mutex()
    private var ayahs: List<IndexedAyah>? = null
    private var byKey: Map<AyahKey, IndexedAyah>? = null

    /** Every ayah in Mushaf order; built on first use. */
    suspend fun ayahs(): List<IndexedAyah> = mutex.withLock { ayahs ?: build().also { ayahs = it } }

    suspend fun ayah(key: AyahKey): IndexedAyah? {
        val all = ayahs()
        return mutex.withLock { (byKey ?: all.associateBy { it.key }.also { byKey = it })[key] }
    }

    private suspend fun build(): List<IndexedAyah> = withContext(dispatchers.io) {
        val queries = provider.get().quranQueries
        val rows = queries.allAyahs().executeAsList()
        val basmala = rows.first().simple.split(' ')
        val quarters = queries.quarters().executeAsList().map { AyahKey(it.surah.toInt(), it.ayah.toInt()) }
        val meccan = queries.surahs { number, _, _, place, _, _ -> number.toInt() to (place == "meccan") }
            .executeAsList().toMap()
        val morphology = queries.allWordMorphology().executeAsList()
            .groupBy { AyahKey(it.surah.toInt(), it.ayah.toInt()) }
        var quarter = 0
        rows.map { row ->
            val key = AyahKey(row.surah.toInt(), row.ayah.toInt())
            while (quarter + 1 < quarters.size && !key.isBefore(quarters[quarter + 1])) quarter++
            val tokens = QuranText.words(key, row.simple, basmala)
            val forms = tokens.map(QuranText::searchForm)
            val words = morphology[key].orEmpty()
            val starts = IntArray(forms.size)
            var offset = 0
            forms.forEachIndexed { i, word ->
                starts[i] = offset
                offset += word.length + 1
            }
            IndexedAyah(
                key = key,
                words = forms,
                text = forms.joinToString(" "),
                starts = starts,
                quarter = quarter,
                meccan = meccan.getValue(key.surah),
                roots = words.map { it.letters?.let(::rootForm) },
                lemmas = words.map { it.clean?.let(QuranText::searchForm) },
                groups = MushafAlignment.groups(key, tokens),
            )
        }
    }
}

internal fun AyahKey.isBefore(other: AyahKey): Boolean =
    surah < other.surah || (surah == other.surah && ayah < other.ayah)
