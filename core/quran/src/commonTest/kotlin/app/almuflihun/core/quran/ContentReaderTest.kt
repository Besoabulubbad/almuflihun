package app.almuflihun.core.quran

import app.almuflihun.core.common.AppError
import app.almuflihun.core.common.AppResult
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.DoorLayout
import app.almuflihun.core.testing.TestDispatcherProvider
import app.almuflihun.core.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** The shipped content read into the app's models, every quote placed, nothing reported. */
class ContentReaderTest : PlatformTest() {

    private val mismatches = mutableListOf<String>()
    private val content by lazy {
        val dispatchers = TestDispatcherProvider()
        BundledContentReader(
            quran = SqlQuranReader(QuranDatabaseProvider(newTestStorage()), dispatchers),
            dispatchers = dispatchers,
            onMismatch = { mismatches += it },
        )
    }

    @Test
    fun doors_are_read_in_order_with_their_layouts() = runAppTest {
        val doors = content.doors().orFail()
        assertEquals(26, doors.size)
        assertEquals(doors.sortedBy { it.order }, doors)
        val visible = doors.filter { it.visible }
        assertEquals(listOf("awamir", "nawahi", "infaq", "dhikr", "shukr", "qawl", "hikma"), visible.map { it.id })
        assertTrue(visible.dropLast(1).all { it.layout is DoorLayout.Evidence })

        val evidence = visible.mapNotNull { it.layout as? DoorLayout.Evidence }
        assertEquals(87, evidence.sumOf { layout -> layout.sections.sumOf { it.items.size } })
        assertEquals(78, doors.sumOf { it.cards.size })
        assertEquals(266, doors.sumOf { it.entries.size })

        val hikma = assertIs<DoorLayout.Passages>(visible.last().layout)
        assertEquals(listOf("isra", "luqman"), hikma.passages.map { it.id })
        val isra = hikma.passages.first()
        assertNotNull(isra.closes)
        assertNotNull(isra.verdict)
        assertEquals(listOf(16, 8), hikma.passages.map { it.steps.size })
        assertEquals("isra", hikma.detailPassage)
        assertEquals(emptyList(), mismatches)
    }

    @Test
    fun the_slider_is_in_order_and_every_slide_comes_from_a_door_entry() = runAppTest {
        val slides = content.slides().orFail()
        assertEquals((1..14).toList(), slides.map { it.order })
        val entries = content.doors().orFail().associate { door ->
            door.id to door.entries.map { it.id }.toSet()
        }
        slides.forEach { slide -> assertTrue(slide.entry in entries.getValue(slide.door), slide.id) }
        assertEquals(emptyList(), mismatches)
    }

    private fun <T> AppResult<T>.orFail(): T = when (this) {
        is AppResult.Success -> value
        is AppResult.Failure -> throw AssertionError(
            "Content failed to load: $error",
            (error as? AppError.Unexpected)?.cause,
        )
    }

    @Test
    fun the_sixty_hizbs_run_from_the_first_ayah_to_the_last() = runAppTest {
        val hizbs = content.hizbs().orFail()
        assertEquals((1..60).toList(), hizbs.map { it.number })
        assertEquals(AyahKey(1, 1), hizbs.first().start)
        assertEquals(AyahKey(114, 6), hizbs.last().end)
        hizbs.forEach { hizb ->
            assertEquals(4, hizb.quarters.size)
            assertEquals(hizb.start, hizb.quarters.first())
            assertEquals((hizb.number + 1) / 2, hizb.juz)
        }
    }
}
