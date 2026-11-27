package app.almuflihun.core.quran

import app.almuflihun.core.common.AppError
import app.almuflihun.core.common.AppResult
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.RootCount
import app.almuflihun.core.model.SurahOverview
import app.almuflihun.core.testing.TestDispatcherProvider
import app.almuflihun.core.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Surah overview (ADR 0012, S6) on the real database and content; numbers from independent SQL and JSON reads. */
class SurahOverviewTest : PlatformTest() {

    private val study by lazy { SqlQuranStudy(QuranDatabaseProvider(newTestStorage()), TestDispatcherProvider()) }

    private suspend fun overview(surah: Int): SurahOverview = (study.surahOverview(surah) as AppResult.Success).value

    @Test
    fun al_baqara() = runAppTest {
        val baqara = overview(2)
        assertEquals(2..49, baqara.pages)
        assertEquals(1..3, baqara.juz)
        assertEquals(6117, baqara.wordCount)
        assertEquals(
            listOf(RootCount("اله", 289), RootCount("قول", 139), RootCount("علم", 102), RootCount("امن", 84)),
            baqara.topRoots.take(4),
        )
        assertEquals(10, baqara.topRoots.size)
        assertEquals(emptyList(), baqara.sajdas)
        assertEquals(21, baqara.doors.size)
        assertEquals(7, baqara.doors.count { it.visible })
        assertEquals(12, baqara.doors.single { it.doorId == "infaq" }.ayahs.size)
    }

    @Test
    fun ar_rahman_shows_its_refrain_in_its_roots() = runAppTest {
        val rahman = overview(55)
        assertEquals(listOf("ربب", "ايي", "كذب", "الو"), rahman.topRoots.take(4).map { it.root })
        assertEquals(emptyList(), rahman.doors)
    }

    @Test
    fun small_surahs_sajdas_and_passages() = runAppTest {
        val ikhlas = overview(112)
        assertEquals(604..604, ikhlas.pages)
        assertEquals(30..30, ikhlas.juz)
        assertEquals(15, ikhlas.wordCount)
        assertEquals(listOf(AyahKey(22, 18), AyahKey(22, 77)), overview(22).sajdas)
        val isra = overview(17).doors.associate { it.doorId to it.ayahs.size }
        assertEquals(18, isra["hikma"])
        assertEquals(4, isra["qawl"])
        assertEquals(AppResult.Failure(AppError.NotFound("surah 115")), study.surahOverview(115))
    }

    @Test
    fun the_doors_that_include_an_ayah() = runAppTest {
        val doors = (study.doorsQuoting(AyahKey(16, 90)) as AppResult.Success).value
        assertEquals(listOf("awamir", "nawahi"), doors.filter { it.visible }.map { it.doorId }.take(2))
        assertEquals(emptyList(), (study.doorsQuoting(AyahKey(112, 1)) as AppResult.Success).value)
    }
}
