package app.qurancommunity.core.data

import app.qurancommunity.core.common.AppClock
import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.model.HizbPosition
import app.qurancommunity.core.model.MushafPosition
import app.qurancommunity.core.testing.TestDispatcherProvider
import app.qurancommunity.core.testing.runAppTest
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Instant

class ReadingRepositoryTest : PlatformTest() {

    private class Ticking(var millis: Long = 0) : AppClock {
        override val timeZone: TimeZone = TimeZone.UTC
        override fun now(): Instant = Instant.fromEpochMilliseconds(++millis)
    }

    private val repository by lazy {
        LocalReadingRepository(inMemoryUserDatabase(), Ticking(), TestDispatcherProvider())
    }

    @Test
    fun the_mushaf_and_the_hizb_keep_separate_positions() = runAppTest {
        assertNull(repository.mushafPosition().first())
        assertNull(repository.hizbPosition().first())
        repository.saveMushafPosition(MushafPosition(42, AyahKey(2, 255)))
        assertNull(repository.hizbPosition().first()) // opening one never moves the other
        repository.saveHizbPosition(HizbPosition(5, 43, AyahKey(2, 260)))
        assertEquals(MushafPosition(42, AyahKey(2, 255)), repository.mushafPosition().first())
        assertEquals(HizbPosition(5, 43, AyahKey(2, 260)), repository.hizbPosition().first())
        repository.saveMushafPosition(MushafPosition(50, AyahKey(3, 1)))
        assertEquals(MushafPosition(50, AyahKey(3, 1)), repository.mushafPosition().first())
        assertEquals(43, repository.hizbPosition().first()?.page)
    }

    @Test
    fun recent_pages_are_the_last_ten_newest_first() = runAppTest {
        for (page in 1..12) repository.pageOpened(page)
        repository.pageOpened(5) // opened again: moves to the front, not duplicated
        assertEquals(listOf(5, 12, 11, 10, 9, 8, 7, 6, 4, 3), repository.recentPages().first())
    }

    @Test
    fun impossible_places_are_refused() = runAppTest {
        assertFailsWith<IllegalArgumentException> { repository.pageOpened(605) }
        assertFailsWith<IllegalArgumentException> { repository.saveHizbPosition(HizbPosition(61, 1, AyahKey(1, 1))) }
    }
}
