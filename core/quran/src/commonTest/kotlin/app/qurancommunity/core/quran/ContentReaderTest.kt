package app.qurancommunity.core.quran

import app.qurancommunity.core.common.AppError
import app.qurancommunity.core.common.AppResult
import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.model.DiyaaBlock
import app.qurancommunity.core.model.DoorLayout
import app.qurancommunity.core.testing.TestDispatcherProvider
import app.qurancommunity.core.testing.runAppTest
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

    @Test
    fun diyaa_has_its_eleven_stations_with_every_block_type() = runAppTest {
        val diyaa = content.diyaa().orFail()
        assertEquals((1..11).toList(), diyaa.stations.map { it.number })
        assertEquals(11, diyaa.summary.size)
        val blocks = diyaa.stations.flatMap { it.blocks }
        assertEquals(BLOCK_KINDS, blocks.map { it::class.simpleName }.toSet())
        assertEquals(30, blocks.count { it is DiyaaBlock.Verses })
        assertEquals(46, blocks.filterIsInstance<DiyaaBlock.Verses>().sumOf { it.highlights.size })
        val compared = blocks.filterIsInstance<DiyaaBlock.Compare>().flatMap { it.items }
        assertEquals(2, compared.sumOf { it.quotes.size }) // the four without a verse key are left out
        // docs/design-requests.md §4: exactly the four quotes without a verse key are reported.
        assertEquals(4, mismatches.size, mismatches.joinToString("\n"))
        assertTrue(mismatches.all { "no verse key" in it })
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

    private companion object {
        val BLOCK_KINDS = setOf(
            "Verses", "Note", "Language", "Rule", "Heading", "Traits", "Contrast", "AnimalPairs", "Compare",
            "Bullets", "CountBars", "Steps", "WordCount", "NumberedList", "ClosingQuote",
        )
    }
}
