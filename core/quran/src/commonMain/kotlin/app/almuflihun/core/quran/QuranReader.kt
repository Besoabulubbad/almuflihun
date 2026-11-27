package app.almuflihun.core.quran

import app.almuflihun.core.common.AppError
import app.almuflihun.core.common.AppResult
import app.almuflihun.core.common.DispatcherProvider
import app.almuflihun.core.common.runCatchingApp
import app.almuflihun.core.model.Ayah
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.MushafLine
import app.almuflihun.core.model.MushafPage
import app.almuflihun.core.model.QuranWord
import kotlinx.coroutines.withContext

/** Reads the Mushaf and its ayahs from the bundled database. Every value is the shipped data, unchanged. */
interface QuranReader {
    /** Page 1–604, line by line, every word placed. */
    suspend fun page(number: Int): AppResult<MushafPage>

    /** One ayah with its page and words. */
    suspend fun ayah(key: AyahKey): AppResult<Ayah>
}

internal class SqlQuranReader(
    private val provider: QuranDatabaseProvider,
    private val dispatchers: DispatcherProvider,
) : QuranReader {

    override suspend fun page(number: Int): AppResult<MushafPage> {
        require(number in 1..MushafPage.COUNT) { "Page $number is not 1..${MushafPage.COUNT}" }
        return withContext(dispatchers.io) {
            runCatchingApp {
                val queries = provider.get().quranQueries
                val lines = queries.pageLines(number.toLong()).executeAsList()
                val first = lines.mapNotNull { it.first_word }.min()
                val last = lines.mapNotNull { it.last_word }.max()
                val words = queries.wordsBetween(first, last, ::toWord).executeAsList().groupBy { it.second }
                MushafPage(
                    number = number,
                    lines = lines.map { line ->
                        val n = line.line.toInt()
                        when (line.kind) {
                            KIND_SURAH -> MushafLine.SurahTitle(n, checkNotNull(line.surah).toInt())
                            KIND_BASMALA -> MushafLine.Basmala(n)
                            else -> MushafLine.Words(n, line.centered == 1L, words.getValue(n).map { it.first })
                        }
                    },
                )
            }
        }
    }

    override suspend fun ayah(key: AyahKey): AppResult<Ayah> = withContext(dispatchers.io) {
        val found = runCatchingApp {
            val queries = provider.get().quranQueries
            queries.ayah(key.surah.toLong(), key.ayah.toLong()).executeAsOneOrNull()?.let { row ->
                val words = queries.ayahWords(key.surah.toLong(), key.ayah.toLong(), ::toWord).executeAsList()
                Ayah(key, row.page.toInt(), words.map { it.first }, row.simple)
            }
        }
        when (found) {
            is AppResult.Success -> found.value?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.NotFound(key.toString()))
            is AppResult.Failure -> found
        }
    }

    /** A word and the page line it sits on. */
    @Suppress("LongParameterList") // the columns of one row, as SQLDelight's mapper passes them
    private fun toWord(
        id: Long,
        surah: Long,
        ayah: Long,
        position: Long,
        line: Long,
        isEnd: Long,
        glyph: String,
        text: String,
    ): Pair<QuranWord, Int> = QuranWord(
        id = id.toInt(),
        ayah = AyahKey(surah.toInt(), ayah.toInt()),
        position = position.toInt(),
        glyph = glyph,
        text = text,
        isAyahEnd = isEnd == 1L,
    ) to line.toInt()

    private companion object {
        const val KIND_SURAH = "surah"
        const val KIND_BASMALA = "basmala"
    }
}
