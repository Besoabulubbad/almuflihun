package app.qurancommunity.core.data

import app.qurancommunity.core.common.AppClock
import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.quran.Join
import app.qurancommunity.core.quran.SearchQuery
import app.qurancommunity.core.quran.SearchScope
import app.qurancommunity.core.quran.SearchTerm
import app.qurancommunity.core.quran.TermMode
import app.qurancommunity.core.testing.TestDispatcherProvider
import app.qurancommunity.core.testing.runAppTest
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class SearchHistoryRepositoryTest : PlatformTest() {

    private class Ticking(var millis: Long = 0) : AppClock {
        override val timeZone: TimeZone = TimeZone.UTC
        override fun now(): Instant = Instant.fromEpochMilliseconds(++millis)
    }

    private val history by lazy {
        LocalSearchHistoryRepository(inMemoryUserDatabase(), Ticking(), TestDispatcherProvider())
    }

    private fun simple(text: String) = SearchQuery(listOf(SearchTerm(text)))

    @Test
    fun every_part_of_a_query_comes_back_as_it_was() = runAppTest {
        val scopes = listOf(
            SearchScope.Quran,
            SearchScope.Surah(2),
            SearchScope.Juz(30),
            SearchScope.Hizb(5),
            SearchScope.Range(AyahKey(2, 1), AyahKey(3, 9)),
            SearchScope.Meccan,
            SearchScope.Medinan,
        )
        val terms = listOf(
            SearchTerm("الصبر"),
            SearchTerm("رب العالمين", TermMode.Phrase, Join.Or),
            SearchTerm("الزكاة", join = Join.Not),
        )
        val queries = scopes.map { scope -> SearchQuery(terms, scope) }
        queries.forEach { history.record(it) }
        assertEquals(queries.reversed(), history.recent().first())
    }

    @Test
    fun the_newest_first_once_each_and_only_the_last_twenty() = runAppTest {
        for (i in 1..25) history.record(simple("كلمة$i"))
        history.record(simple(" كلمة3 ")) // searched again, with spaces: moves to the top
        history.record(simple("   ")) // nothing to remember
        val recent = history.recent().first()
        assertEquals(20, recent.size)
        assertEquals(simple("كلمة3"), recent.first())
        assertEquals(simple("كلمة25"), recent[1])
        history.clear()
        assertEquals(emptyList(), history.recent().first())
    }
}
