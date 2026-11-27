package app.almuflihun.core.quran

import app.almuflihun.core.common.AppResult
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.quran.resources.Res
import app.almuflihun.core.testing.TestDispatcherProvider
import app.almuflihun.core.testing.runAppTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Every quote, card, highlight and point in the shipped content (design/data) is placed in the shipped text
 * (DESIGN §1, docs/plan.md Phase 3). Runs in `check` on Android and iOS, so a quote that does not match fails the
 * build. The numbers pin the content's size, so a quote cannot silently drop out of the check.
 */
@OptIn(ExperimentalResourceApi::class)
class ContentPlacementTest : PlatformTest() {

    // Created inside each test, so its dispatcher shares that test's scheduler.
    private val reader by lazy { SqlQuranReader(QuranDatabaseProvider(newTestStorage()), TestDispatcherProvider()) }
    private val cache = mutableMapOf<AyahKey, List<String>>()
    private var basmala: List<String> = emptyList()

    private suspend fun words(key: AyahKey): List<String> = cache.getOrPut(key) {
        if (basmala.isEmpty()) basmala = (reader.ayah(AyahKey(1, 1)) as AppResult.Success).value.tanzilText.split(' ')
        val ayah = (reader.ayah(key) as AppResult.Success).value
        QuranText.words(key, ayah.tanzilText, basmala)
    }

    private suspend fun content(name: String): JsonObject =
        Json.parseToJsonElement(Res.readBytes("files/content/$name").decodeToString()).jsonObject

    private data class Quote(val where: String, val key: AyahKey, val text: String, val range: IntRange?)

    private suspend fun quotes(): List<Quote> {
        val doors = content("abwab-4.json")["abwab"]!!.jsonArray.map { it.jsonObject }
        val fromDoors = doors.flatMap { door ->
            val id = door.string("id")
            val cards = door["cards"]?.jsonArray.orEmpty().map { card ->
                val c = card.jsonObject
                Quote("door $id card", key(c.string("ref")), c.string("text"), null)
            }
            val items = door["design"]?.let { it as? JsonObject }?.get("sections")?.jsonArray.orEmpty()
                .flatMap { it.jsonObject["items"]?.jsonArray.orEmpty() }
                .map { it.jsonObject }
                .filter { "quote" in it }
                .map { Quote("door $id ${it.string("entry")}", key(it.string("key")), it.string("quote"), it.range()) }
            cards + items
        }
        val slider = content("dhikr-slider.json")["items"]!!.jsonArray.map { it.jsonObject }.map {
            Quote("slider ${it.string("id")}", key(it.string("key")), it.string("quote"), it.range())
        }
        val diyaa = content("diyaa-food.json")["stations"]!!.jsonArray.flatMap { station ->
            val n = station.jsonObject["n"]!!.jsonPrimitive.int
            station.jsonObject["blocks"]!!.jsonArray.map { it.jsonObject }
                .filter { it["type"]?.jsonPrimitive?.content == "verse" }
                .flatMap { block ->
                    val ranges = block["highlight_words"]?.jsonArray.orEmpty().map { it.jsonObject }
                    block["highlight"]?.jsonArray.orEmpty().mapIndexed { i, h ->
                        val declared = ranges.getOrNull(i)
                        val k = declared?.string("key") ?: block["keys"]!!.jsonArray.first().jsonPrimitive.content
                        Quote("الضياء station $n", key(k), h.jsonPrimitive.content, declared?.range())
                    }
                }
        }
        return fromDoors + slider + diyaa
    }

    @Test
    fun every_quote_and_highlight_is_placed_in_the_shipped_text() = runAppTest {
        val all = quotes()
        val failures = all.mapNotNull { q ->
            when (val result = QuotePlacer.place(q.key, words(q.key), q.text, q.range)) {
                is QuotePlacer.Result.Mismatch -> "${q.where}: ${result.reason}"
                is QuotePlacer.Result.Placed -> {
                    // Every placed quote can also be highlighted on the Mushaf page.
                    MushafAlignment.mushafPositions(result.range, words(q.key))
                    null
                }
            }
        }
        assertTrue(failures.isEmpty(), "Quotes not placed:\n" + failures.joinToString("\n"))
        assertEquals(EXPECTED_QUOTES, all.size)
    }

    @Test
    fun every_point_is_a_literal_piece_of_its_verses() = runAppTest {
        var count = 0
        val failures = content("abwab-4.json")["abwab"]!!.jsonArray.flatMap { door ->
            door.jsonObject["entries"]!!.jsonArray.map { it.jsonObject }.flatMap { entry ->
                val verses = entry["verses"]!!.jsonArray.map { words(key(it.jsonPrimitive.content)) }
                entry["points"]?.jsonArray.orEmpty().flatMap { point ->
                    point.jsonObject.string("text").split("، ").mapNotNull { fragment ->
                        count++
                        val found = QuotePlacer.containsFragment(verses, fragment)
                        if (found) null else "${entry.string("id")}: «$fragment»"
                    }
                }
            }
        }
        assertTrue(failures.isEmpty(), "Points not found in their verses:\n" + failures.joinToString("\n"))
        assertEquals(EXPECTED_POINTS, count)
    }

    @Test
    fun the_verse_texts_inside_the_content_match_the_shipped_text() = runAppTest {
        listOf("abwab-4.json", "diyaa-food.json").forEach { file ->
            val verses = content(file)["verses"]!!.jsonObject
            verses.forEach { (k, text) ->
                val key = key(k)
                assertEquals(
                    words(key).map(QuranText::skeleton),
                    text.jsonPrimitive.content.split(' ').map(QuranText::skeleton).filter { it.isNotEmpty() },
                    "$file verse $k",
                )
            }
        }
    }

    private fun JsonObject.string(name: String): String = getValue(name).jsonPrimitive.content

    private fun JsonObject.range(): IntRange? = (get("words") as? JsonArray)?.let { w ->
        w[0].jsonPrimitive.int..w[1].jsonPrimitive.int
    }

    private fun key(text: String): AyahKey = text.split(':').let { AyahKey(it[0].toInt(), it[1].toInt()) }

    private companion object {
        const val EXPECTED_QUOTES = 225
        const val EXPECTED_POINTS = 663
    }
}
