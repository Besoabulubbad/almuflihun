package app.qurancommunity.core.domain

import app.qurancommunity.core.common.AppError
import app.qurancommunity.core.common.AppResult
import app.qurancommunity.core.data.CollectionsRepository
import app.qurancommunity.core.model.Ayah
import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.model.AyahPosition
import app.qurancommunity.core.model.CollectedVerse
import app.qurancommunity.core.model.CollectionPlace
import app.qurancommunity.core.model.DoorMention
import app.qurancommunity.core.model.MushafPage
import app.qurancommunity.core.model.RevelationPlace
import app.qurancommunity.core.model.RootUsage
import app.qurancommunity.core.model.SharedPhrase
import app.qurancommunity.core.model.Surah
import app.qurancommunity.core.model.SurahOverview
import app.qurancommunity.core.model.VerseCollection
import app.qurancommunity.core.model.WordMorphology
import app.qurancommunity.core.model.WordRange
import app.qurancommunity.core.model.WordStudy
import app.qurancommunity.core.quran.QuranReader
import app.qurancommunity.core.quran.QuranStudy
import app.qurancommunity.core.testing.runAppTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetAyahStudyTest {

    private val key = AyahKey(2, 255)
    private val baqara = Surah(2, "البقرة", 286, RevelationPlace.Medinan, 87, 2)
    private val position = AyahPosition(key, 42, 3, 5, 1, false)
    private val phrase = SharedPhrase(WordRange(key, 1, 4), listOf(WordRange(AyahKey(3, 2), 1, 4)))

    private class FakeReader(val fail: Boolean = false) : QuranReader {
        override suspend fun page(number: Int): AppResult<MushafPage> = error("unused")
        override suspend fun ayah(key: AyahKey): AppResult<Ayah> = if (fail) {
            AppResult.Failure(AppError.NotFound(key.toString()))
        } else {
            AppResult.Success(Ayah(key, 42, emptyList(), "t"))
        }
        override suspend fun surahs(): AppResult<List<Surah>> =
            AppResult.Success(listOf(Surah(2, "البقرة", 286, RevelationPlace.Medinan, 87, 2)))
        override suspend fun position(key: AyahKey) = AppResult.Success(AyahPosition(key, 42, 3, 5, 1, false))
    }

    private inner class FakeStudy : QuranStudy {
        override suspend fun sharedPhrases(key: AyahKey) = AppResult.Success(listOf(phrase))
        override suspend fun morphology(key: AyahKey) = AppResult.Success(listOf(WordMorphology(1, "اله", null, null)))
        override suspend fun rootUsage(root: String): AppResult<RootUsage> = error("unused")
        override suspend fun wordStudy(key: AyahKey, position: Int): AppResult<WordStudy> = error("unused")
        override suspend fun surahOverview(surah: Int, rootCount: Int): AppResult<SurahOverview> = error("unused")
        override suspend fun doorsQuoting(key: AyahKey) = AppResult.Success(listOf(DoorMention("dhikr", "الذكر", true)))
    }

    private class FakeCollections(val places: MutableStateFlow<List<CollectionPlace>>) : CollectionsRepository {
        override fun collections(): Flow<List<VerseCollection>> = flowOf(emptyList())
        override fun verses(collectionId: String): Flow<List<CollectedVerse>> = flowOf(emptyList())
        override fun placesOf(key: AyahKey): Flow<List<CollectionPlace>> = places
        override suspend fun ensureDefault() = "saved"
        override suspend fun create(name: String) = "new"
        override suspend fun rename(collectionId: String, name: String) = Unit
        override suspend fun delete(collectionId: String) = Unit
        override suspend fun setMembership(key: AyahKey, collectionIds: Set<String>, quote: WordRange?) = Unit
        override suspend fun move(collectionId: String, key: AyahKey, index: Int) = Unit
        override suspend fun setNote(collectionId: String, key: AyahKey, note: String?) = Unit
    }

    @Test
    fun the_panel_brings_every_part_together() = runAppTest {
        val places = MutableStateFlow(listOf(CollectionPlace("saved", null, true, null)))
        val panel = GetAyahStudy(FakeReader(), FakeStudy(), FakeCollections(places))
        val study = (panel(key).first() as AppResult.Success).value
        assertEquals(baqara, study.surah)
        assertEquals(position, study.position)
        assertEquals(listOf(phrase), study.phrases)
        assertEquals("dhikr", study.doors.single().doorId)
        assertEquals("اله", study.words.single().root)
        assertEquals(places.value, study.collections)
    }

    @Test
    fun collections_and_notes_stay_live() = runAppTest {
        val places = MutableStateFlow(emptyList<CollectionPlace>())
        val seen = mutableListOf<List<CollectionPlace>>()
        val job = launch {
            GetAyahStudy(FakeReader(), FakeStudy(), FakeCollections(places))(key).take(2).toList()
                .forEach { seen += (it as AppResult.Success).value.collections }
        }
        testScheduler.advanceUntilIdle()
        places.value = listOf(CollectionPlace("study", "دراسة", false, "تأمّل"))
        job.join()
        assertEquals(listOf(emptyList(), places.value), seen)
    }

    @Test
    fun a_failing_part_fails_the_panel() = runAppTest {
        val panel = GetAyahStudy(FakeReader(fail = true), FakeStudy(), FakeCollections(MutableStateFlow(emptyList())))
        val result = panel(key).first()
        assertIs<AppResult.Failure>(result)
    }
}
