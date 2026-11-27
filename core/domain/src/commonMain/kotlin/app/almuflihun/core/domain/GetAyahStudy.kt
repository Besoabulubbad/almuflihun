package app.almuflihun.core.domain

import app.almuflihun.core.common.AppError
import app.almuflihun.core.common.AppResult
import app.almuflihun.core.data.CollectionsRepository
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.AyahStudy
import app.almuflihun.core.quran.QuranReader
import app.almuflihun.core.quran.QuranStudy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * The ayah study panel (ADR 0012, S5): the Quran parts are read once; the reader's collections and notes stay live,
 * so the panel updates the moment the ayah is added to a collection or a note changes.
 */
class GetAyahStudy(
    private val reader: QuranReader,
    private val study: QuranStudy,
    private val collections: CollectionsRepository,
) {
    operator fun invoke(key: AyahKey): Flow<AppResult<AyahStudy>> = flow {
        when (val fixed = load(key)) {
            is AppResult.Failure -> emit(fixed)
            is AppResult.Success -> emitAll(
                collections.placesOf(key).map { places -> AppResult.Success(fixed.value.copy(collections = places)) },
            )
        }
    }

    private suspend fun load(key: AyahKey): AppResult<AyahStudy> {
        val ayah = reader.ayah(key)
        val surah = reader.surahs()
        val position = reader.position(key)
        val words = study.morphology(key)
        val phrases = study.sharedPhrases(key)
        val doors = study.doorsQuoting(key)
        val failure = listOf(ayah, surah, position, words, phrases, doors)
            .firstNotNullOfOrNull { it as? AppResult.Failure }
        return failure ?: AppResult.Success(
            AyahStudy(
                ayah = (ayah as AppResult.Success).value,
                surah = (surah as AppResult.Success).value.firstOrNull { it.number == key.surah }
                    ?: return AppResult.Failure(AppError.NotFound("surah ${key.surah}")),
                position = (position as AppResult.Success).value,
                words = (words as AppResult.Success).value,
                phrases = (phrases as AppResult.Success).value,
                doors = (doors as AppResult.Success).value,
                collections = emptyList(),
            ),
        )
    }
}
