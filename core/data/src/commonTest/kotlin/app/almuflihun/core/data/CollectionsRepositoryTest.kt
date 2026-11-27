package app.almuflihun.core.data

import app.almuflihun.core.common.AppClock
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.CollectedVerse
import app.almuflihun.core.model.CollectionPlace
import app.almuflihun.core.model.WordRange
import app.almuflihun.core.testing.TestDispatcherProvider
import app.almuflihun.core.testing.runAppTest
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class CollectionsRepositoryTest : PlatformTest() {

    private class FakeClock(var millis: Long = 1_000) : AppClock {
        override val timeZone: TimeZone = TimeZone.UTC
        override fun now(): Instant = Instant.fromEpochMilliseconds(millis)
    }

    private val clock = FakeClock()
    private val repository by lazy {
        LocalCollectionsRepository(inMemoryUserDatabase(), clock, TestDispatcherProvider())
    }
    private val kursi = AyahKey(2, 255)
    private val nur = AyahKey(24, 35)
    private val ikhlas = AyahKey(112, 1)

    @Test
    fun the_default_collection_comes_first_and_is_made_once() = runAppTest {
        val study = repository.create(" دراسة ")
        val saved = repository.ensureDefault()
        assertEquals(saved, repository.ensureDefault())
        val all = repository.collections().first()
        assertEquals(listOf(saved, study), all.map { it.id })
        assertNull(all.first().name) // named by the UI
        assertEquals("دراسة", all.last().name)
    }

    @Test
    fun a_verse_can_be_in_several_collections_once_each() = runAppTest {
        val saved = repository.ensureDefault()
        val study = repository.create("دراسة")
        repository.setMembership(kursi, setOf(saved, study))
        repository.setMembership(kursi, setOf(saved, study)) // again: still once
        assertEquals(setOf(saved, study), repository.placesOf(kursi).first().map { it.collectionId }.toSet())
        assertEquals(listOf(1, 1), repository.collections().first().map { it.verseCount })

        repository.setMembership(kursi, setOf(study)) // unticked in «محفوظاتي»
        assertEquals(setOf(study), repository.placesOf(kursi).first().map { it.collectionId }.toSet())
        assertEquals(listOf(0, 1), repository.collections().first().map { it.verseCount })
    }

    @Test
    fun the_reader_s_order_and_notes_are_kept() = runAppTest {
        val study = repository.create("دراسة")
        val quote = WordRange(nur, 1, 6)
        repository.setMembership(kursi, setOf(study))
        repository.setMembership(nur, setOf(study), quote)
        repository.setMembership(ikhlas, setOf(study))
        repository.move(study, ikhlas, 0)
        repository.setNote(study, nur, "  تأمّل  ")
        assertEquals(
            listOf(
                CollectedVerse(ikhlas, null, null),
                CollectedVerse(kursi, null, null),
                CollectedVerse(nur, quote, "تأمّل"),
            ),
            repository.verses(study).first(),
        )
        repository.setNote(study, nur, " ")
        assertNull(repository.verses(study).first().last().note)
    }

    @Test
    fun removing_and_adding_again_puts_the_verse_last_without_its_old_note() = runAppTest {
        val study = repository.create("دراسة")
        repository.setMembership(kursi, setOf(study))
        repository.setMembership(nur, setOf(study))
        repository.setNote(study, kursi, "ملاحظة")
        repository.setMembership(kursi, emptySet())
        repository.setMembership(kursi, setOf(study))
        assertEquals(
            listOf(CollectedVerse(nur, null, null), CollectedVerse(kursi, null, null)),
            repository.verses(study).first(),
        )
    }

    @Test
    fun the_default_cannot_be_renamed_or_deleted_and_a_deleted_collection_disappears() = runAppTest {
        val saved = repository.ensureDefault()
        val study = repository.create("دراسة")
        repository.setMembership(kursi, setOf(study))
        repository.rename(saved, "غيره")
        repository.delete(saved)
        repository.delete(study)
        val all = repository.collections().first()
        assertEquals(listOf(saved), all.map { it.id })
        assertNull(all.single().name)
        assertEquals(emptySet(), repository.placesOf(kursi).first().map { it.collectionId }.toSet())
    }

    @Test
    fun an_ayah_s_places_show_each_collection_and_its_note() = runAppTest {
        val saved = repository.ensureDefault()
        val study = repository.create("دراسة")
        repository.setMembership(kursi, setOf(study, saved))
        repository.setNote(study, kursi, "تأمّل")
        assertEquals(
            listOf(CollectionPlace(saved, null, true, null), CollectionPlace(study, "دراسة", false, "تأمّل")),
            repository.placesOf(kursi).first(),
        )
        assertEquals(emptyList(), repository.placesOf(nur).first())
    }

    @Test
    fun last_change_follows_the_verses() = runAppTest {
        val study = repository.create("دراسة")
        clock.millis = 5_000
        repository.setMembership(kursi, setOf(study))
        assertEquals(Instant.fromEpochMilliseconds(5_000), repository.collections().first().single().changedAt)
    }
}
