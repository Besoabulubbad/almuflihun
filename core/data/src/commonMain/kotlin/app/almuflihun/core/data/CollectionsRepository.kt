package app.almuflihun.core.data

import app.almuflihun.core.common.AppClock
import app.almuflihun.core.common.DispatcherProvider
import app.almuflihun.core.database.UserDatabase
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.CollectedVerse
import app.almuflihun.core.model.CollectionPlace
import app.almuflihun.core.model.VerseCollection
import app.almuflihun.core.model.WordRange
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * The reader's collections (ADR 0012, S1), kept on the device. The default «محفوظاتي» always exists and cannot be
 * renamed or deleted; a verse can be in several collections, once in each.
 */
interface CollectionsRepository {
    /** Every collection, «محفوظاتي» first, then in the order they were made. */
    fun collections(): Flow<List<VerseCollection>>

    /** A collection's verses in the reader's order. */
    fun verses(collectionId: String): Flow<List<CollectedVerse>>

    /**
     * The collections holding [key] with the note in each, «محفوظاتي» first: the ayah study panel, and the add-to
     * sheet's ticked boxes (their ids).
     */
    fun placesOf(key: AyahKey): Flow<List<CollectionPlace>>

    /** Makes the default «محفوظاتي» if it does not exist yet, and returns its id. */
    suspend fun ensureDefault(): String

    suspend fun create(name: String): String
    suspend fun rename(collectionId: String, name: String)
    suspend fun delete(collectionId: String)

    /**
     * The add-to sheet: [key] ends up in exactly [collectionIds]. Newly added, it goes last, with [quote] when a quote
     * was saved; collections it stays in keep its place and note.
     */
    suspend fun setMembership(key: AyahKey, collectionIds: Set<String>, quote: WordRange? = null)

    /** Moves [key] to [index] (0-based) in the collection's order. */
    suspend fun move(collectionId: String, key: AyahKey, index: Int)

    /** The private note; null or blank removes it. */
    suspend fun setNote(collectionId: String, key: AyahKey, note: String?)
}

@OptIn(ExperimentalUuidApi::class)
internal class LocalCollectionsRepository(
    private val database: UserDatabase,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider,
) : CollectionsRepository {

    private val queries get() = database.collectionsQueries

    override fun collections(): Flow<List<VerseCollection>> =
        queries.collections().asFlow().mapToList(dispatchers.io).map { rows ->
            rows.map { row ->
                VerseCollection(
                    id = row.id,
                    name = row.name,
                    isDefault = row.is_default == 1L,
                    verseCount = row.verses.toInt(),
                    changedAt = Instant.fromEpochMilliseconds(maxOf(row.updated_at, row.verses_updated_at ?: 0L)),
                )
            }
        }

    override fun verses(collectionId: String): Flow<List<CollectedVerse>> =
        queries.verses(collectionId).asFlow().mapToList(dispatchers.io).map { rows ->
            rows.map { row ->
                val key = AyahKey(row.surah.toInt(), row.ayah.toInt())
                val first = row.first_word
                val last = row.last_word
                val quote = if (first != null && last != null) WordRange(key, first.toInt(), last.toInt()) else null
                CollectedVerse(key, quote, row.note)
            }
        }

    override fun placesOf(key: AyahKey): Flow<List<CollectionPlace>> =
        queries.placesOf(key.surah.toLong(), key.ayah.toLong()).asFlow().mapToList(dispatchers.io).map { rows ->
            rows.map { CollectionPlace(it.id, it.name, it.is_default == 1L, it.note) }
        }

    override suspend fun ensureDefault(): String = write {
        queries.defaultCollection().executeAsOneOrNull() ?: newCollection(name = null, isDefault = true)
    }

    override suspend fun create(name: String): String = write { newCollection(name.trim(), isDefault = false) }

    override suspend fun rename(collectionId: String, name: String) {
        write { queries.renameCollection(name.trim(), now(), collectionId) }
    }

    override suspend fun delete(collectionId: String) = write {
        if (queries.defaultCollection().executeAsOneOrNull() != collectionId) {
            val at = now()
            queries.deleteCollection(at, collectionId)
            queries.deleteVersesOf(at, collectionId)
        }
    }

    override suspend fun setMembership(key: AyahKey, collectionIds: Set<String>, quote: WordRange?) = write {
        require(quote == null || quote.key == key) { "The quote must be from $key" }
        val at = now()
        val surah = key.surah.toLong()
        val ayah = key.ayah.toLong()
        val current = queries.membership(surah, ayah).executeAsList().toSet()
        (current - collectionIds).forEach { id ->
            queries.removeVerse(at, id, surah, ayah)
            queries.touchCollection(at, id)
        }
        (collectionIds - current).forEach { id ->
            val position = queries.nextVersePosition(id).executeAsOne()
            queries.putVerse(id, surah, ayah, quote?.first?.toLong(), quote?.last?.toLong(), position, at, at)
            queries.touchCollection(at, id)
        }
    }

    override suspend fun move(collectionId: String, key: AyahKey, index: Int) = write {
        val order = queries.verses(collectionId).executeAsList().map { AyahKey(it.surah.toInt(), it.ayah.toInt()) }
        if (key in order) {
            val moved = (order - key).toMutableList().apply { add(index.coerceIn(0, size), key) }
            val at = now()
            moved.forEachIndexed { i, k ->
                queries.setPosition((i + 1).toLong(), at, collectionId, k.surah.toLong(), k.ayah.toLong())
            }
            queries.touchCollection(at, collectionId)
        }
    }

    override suspend fun setNote(collectionId: String, key: AyahKey, note: String?) = write {
        val at = now()
        queries.setNote(note?.trim()?.ifEmpty { null }, at, collectionId, key.surah.toLong(), key.ayah.toLong())
        queries.touchCollection(at, collectionId)
        Unit
    }

    private fun newCollection(name: String?, isDefault: Boolean): String {
        val id = Uuid.random().toString()
        val at = now()
        val position = queries.nextCollectionPosition().executeAsOne()
        queries.insertCollection(id, name, if (isDefault) 1L else 0L, position, at, at)
        return id
    }

    private fun now(): Long = clock.now().toEpochMilliseconds()

    /** Every write is one transaction, off the main thread. */
    private suspend fun <T> write(block: () -> T): T = withContext(dispatchers.io) {
        database.transactionWithResult { block() }
    }
}
