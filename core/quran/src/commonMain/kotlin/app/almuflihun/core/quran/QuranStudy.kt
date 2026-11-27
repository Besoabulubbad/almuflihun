package app.almuflihun.core.quran

import app.almuflihun.core.common.AppResult
import app.almuflihun.core.common.DispatcherProvider
import app.almuflihun.core.common.runCatchingApp
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.SharedPhrase
import app.almuflihun.core.model.WordRange
import kotlinx.coroutines.withContext

/** The study engine (ADR 0012): the text explaining itself. Grows with word study and roots in Phase 3b. */
interface QuranStudy {
    /**
     * The phrases this ayah shares with others, longest first, then the rarest: each with every other place it
     * appears. Empty when the ayah shares none.
     */
    suspend fun sharedPhrases(key: AyahKey): AppResult<List<SharedPhrase>>
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
}
