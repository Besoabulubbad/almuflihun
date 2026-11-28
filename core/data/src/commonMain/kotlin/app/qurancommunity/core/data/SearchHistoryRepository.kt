package app.qurancommunity.core.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.qurancommunity.core.common.AppClock
import app.qurancommunity.core.common.DispatcherProvider
import app.qurancommunity.core.database.UserDatabase
import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.quran.Join
import app.qurancommunity.core.quran.SearchQuery
import app.qurancommunity.core.quran.SearchScope
import app.qurancommunity.core.quran.SearchTerm
import app.qurancommunity.core.quran.TermMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Recent searches, shown before typing (DESIGN «Advanced search»). Kept on the device only; search terms are never
 * sent anywhere (docs/analytics/tracking-plan.md). The same query searched again moves to the top.
 */
interface SearchHistoryRepository {
    /** The last [KEPT] searches, newest first. */
    fun recent(): Flow<List<SearchQuery>>

    suspend fun record(query: SearchQuery)
    suspend fun clear()

    companion object {
        const val KEPT = 20
    }
}

internal class LocalSearchHistoryRepository(
    private val database: UserDatabase,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider,
) : SearchHistoryRepository {

    private val queries get() = database.searchHistoryQueries
    private val json = Json { ignoreUnknownKeys = true }

    override fun recent(): Flow<List<SearchQuery>> =
        queries.recentSearches(SearchHistoryRepository.KEPT.toLong()).asFlow().mapToList(dispatchers.io)
            .map { rows -> rows.mapNotNull(::decode) }

    /** A stored query, or null if it no longer reads (an older format): it is skipped, never a crash. */
    private fun decode(row: String): SearchQuery? =
        runCatching { json.decodeFromString<QueryDto>(row).toQuery() }.getOrNull()

    override suspend fun record(query: SearchQuery) {
        val terms = query.terms.filter { it.text.isNotBlank() }
        if (terms.isEmpty()) return
        val text = json.encodeToString(QueryDto.from(query.copy(terms = terms.map { it.copy(text = it.text.trim()) })))
        withContext(dispatchers.io) {
            database.transaction {
                queries.recordSearch(text, clock.now().toEpochMilliseconds())
                queries.trimSearches(SearchHistoryRepository.KEPT.toLong())
            }
        }
    }

    override suspend fun clear() {
        withContext(dispatchers.io) { queries.clearSearches() }
    }
}

/** The stored form of a [SearchQuery]; its own type so core:quran's API carries no serialization. */
@Serializable
internal data class QueryDto(val terms: List<TermDto>, val scope: String, val a: List<Int> = emptyList()) {

    @Serializable
    data class TermDto(val text: String, val mode: String, val join: String)

    fun toQuery() = SearchQuery(
        terms = terms.map { SearchTerm(it.text, TermMode.valueOf(it.mode), Join.valueOf(it.join)) },
        scope = when (scope) {
            "surah" -> SearchScope.Surah(a[0])
            "juz" -> SearchScope.Juz(a[0])
            "hizb" -> SearchScope.Hizb(a[0])
            "range" -> SearchScope.Range(AyahKey(a[FROM_SURAH], a[FROM_AYAH]), AyahKey(a[TO_SURAH], a[TO_AYAH]))
            "meccan" -> SearchScope.Meccan
            "medinan" -> SearchScope.Medinan
            else -> SearchScope.Quran
        },
    )

    companion object {
        private const val FROM_SURAH = 0
        private const val FROM_AYAH = 1
        private const val TO_SURAH = 2
        private const val TO_AYAH = 3

        fun from(query: SearchQuery): QueryDto {
            val terms = query.terms.map { TermDto(it.text, it.mode.name, it.join.name) }
            return when (val s = query.scope) {
                SearchScope.Quran -> QueryDto(terms, "quran")
                is SearchScope.Surah -> QueryDto(terms, "surah", listOf(s.number))
                is SearchScope.Juz -> QueryDto(terms, "juz", listOf(s.number))
                is SearchScope.Hizb -> QueryDto(terms, "hizb", listOf(s.number))
                is SearchScope.Range ->
                    QueryDto(terms, "range", listOf(s.from.surah, s.from.ayah, s.to.surah, s.to.ayah))
                SearchScope.Meccan -> QueryDto(terms, "meccan")
                SearchScope.Medinan -> QueryDto(terms, "medinan")
            }
        }
    }
}
