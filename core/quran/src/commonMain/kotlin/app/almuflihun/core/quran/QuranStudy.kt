package app.almuflihun.core.quran

import app.almuflihun.core.common.AppError
import app.almuflihun.core.common.AppResult
import app.almuflihun.core.common.DispatcherProvider
import app.almuflihun.core.common.runCatchingApp
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.RootUsage
import app.almuflihun.core.model.SharedPhrase
import app.almuflihun.core.model.WordMorphology
import app.almuflihun.core.model.WordRange
import kotlinx.coroutines.withContext

/** The study engine (ADR 0012): the text explaining itself. Grows with word study and roots in Phase 3b. */
interface QuranStudy {
    /**
     * The phrases this ayah shares with others, longest first, then the rarest: each with every other place it
     * appears. Empty when the ayah shares none.
     */
    suspend fun sharedPhrases(key: AyahKey): AppResult<List<SharedPhrase>>

    /** Each word's root, lemma and stem, in the Mushaf's word order. */
    suspend fun morphology(key: AyahKey): AppResult<List<WordMorphology>>

    /**
     * How often [root] occurs and in how many ayahs. [root] may be typed with spaces or hamza forms («ص ب ر»,
     * «أمن»); NotFound when no root has these letters.
     */
    suspend fun rootUsage(root: String): AppResult<RootUsage>
}

internal class SqlQuranStudy(
    private val provider: QuranDatabaseProvider,
    private val dispatchers: DispatcherProvider,
) : QuranStudy {

    override suspend fun sharedPhrases(key: AyahKey): AppResult<List<SharedPhrase>> = withContext(dispatchers.io) {
        runCatchingApp {
            val queries = provider.get().quranQueries
            queries.phrasesInAyah(key.surah.toLong(), key.ayah.toLong()).executeAsList()
                .distinctBy { it.phrase } // a phrase twice in one ayah: shown once, at its first place
                .map { row ->
                    val here = WordRange(key, row.first_word.toInt(), row.last_word.toInt())
                    val elsewhere = queries.placesOfPhrase(row.phrase).executeAsList()
                        .map { place ->
                            val ayah = AyahKey(place.surah.toInt(), place.ayah.toInt())
                            WordRange(ayah, place.first_word.toInt(), place.last_word.toInt())
                        }
                        .filterNot { it == here }
                    SharedPhrase(here, elsewhere)
                }
        }
    }

    override suspend fun morphology(key: AyahKey): AppResult<List<WordMorphology>> = withContext(dispatchers.io) {
        runCatchingApp {
            provider.get().quranQueries
                .wordMorphology(key.surah.toLong(), key.ayah.toLong()) { position, root, lemma, stem ->
                    WordMorphology(position.toInt(), root, lemma, stem)
                }
                .executeAsList()
        }
    }

    override suspend fun rootUsage(root: String): AppResult<RootUsage> = withContext(dispatchers.io) {
        val queries = provider.get().quranQueries
        val wanted = rootForm(root)
        val letters = runCatchingApp { queries.rootLetters().executeAsList().firstOrNull { rootForm(it) == wanted } }
        when {
            letters is AppResult.Failure -> letters
            (letters as AppResult.Success).value == null -> AppResult.Failure(AppError.NotFound(root))
            else -> runCatchingApp {
                val found = letters.value!!
                val usage = queries.rootUsage(found).executeAsOne()
                RootUsage(found, usage.occurrences.toInt(), usage.ayahs.toInt())
            }
        }
    }
}

/** Root letters for comparison: no spaces, letter forms unified (QuranText.searchForm). */
internal fun rootForm(root: String): String = QuranText.searchForm(root.filterNot(Char::isWhitespace))
