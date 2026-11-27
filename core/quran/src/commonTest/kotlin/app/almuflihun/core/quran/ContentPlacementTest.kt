package app.almuflihun.core.quran

import app.almuflihun.core.common.AppResult
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.quran.resources.Res
import app.almuflihun.core.testing.TestDispatcherProvider
import app.almuflihun.core.testing.runAppTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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

    private data class Quote(val where: String, val key: AyahKey?, val text: String, val range: IntRange?)

    /**
     * Every quote in the content, found by walking the files rather than by listing layouts, so a new layout cannot be
     * skipped: any `quote` or `quotes` with its `key` or `ref`, the door `cards`, and the الضياء `highlight` lists.
     * Quotes without a verse key are returned with a null key.
     */
    private suspend fun quotes(): List<Quote> = listOf("abwab-4.json", "dhikr-slider.json", "diyaa-food.json")
        .flatMap { file -> collect(content(file), file) }

    private fun collect(element: JsonElement, where: String): List<Quote> = when (element) {
        is JsonArray -> element.flatMapIndexed { i, child -> collect(child, "$where[$i]") }
        is JsonObject -> {
            // Only a quote's own key: entries also carry a display `ref` such as 13:21-22, which is not a quote key.
            val key = if ("quote" in element || "quotes" in element) {
                (element["key"] ?: element["ref"])?.let { it as? JsonPrimitive }?.content?.let(::key)
            } else {
                null
            }
            val own = buildList {
                (element["quote"] as? JsonPrimitive)?.let { add(Quote(where, key, it.content, element.range())) }
                (element["quotes"] as? JsonArray)?.forEach { add(Quote(where, key, it.jsonPrimitive.content, null)) }
                (element["cards"] as? JsonArray)?.forEachIndexed { i, card ->
                    val c = card.jsonObject
                    add(Quote("$where/cards[$i]", key(c.string("ref")), c.string("text"), null))
                }
                (element["highlight"] as? JsonArray)?.let { highlights ->
                    val ranges = element["highlight_words"]?.jsonArray.orEmpty().map { it.jsonObject }
                    val first = element["keys"]!!.jsonArray.first().jsonPrimitive.content
                    highlights.forEachIndexed { i, h ->
                        val declared = ranges.getOrNull(i)
                        val k = key(declared?.string("key") ?: first)
                        add(Quote("$where/highlight[$i]", k, h.jsonPrimitive.content, declared?.range()))
                    }
                }
            }
            own + element.entries
                .filterNot { (name, _) -> name in setOf("cards", "highlight", "highlight_words") }
                .flatMap { (name, child) -> collect(child, "$where/$name") }
        }
        else -> emptyList()
    }

    @Test
    fun every_quote_and_highlight_is_placed_in_the_shipped_text() = runAppTest {
        val (keyed, unkeyed) = quotes().partition { it.key != null }
        val failures = keyed.mapNotNull { q ->
            val key = q.key!!
            when (val result = QuotePlacer.place(key, words(key), q.text, q.range)) {
                is QuotePlacer.Result.Mismatch -> "${q.where}: ${result.reason}"
                is QuotePlacer.Result.Placed -> {
                    // Every placed quote can also be highlighted on the Mushaf page.
                    MushafAlignment.mushafPositions(result.range, words(key))
                    null
                }
            }
        }
        assertTrue(failures.isEmpty(), "Quotes not placed:\n" + failures.joinToString("\n"))
        assertEquals(EXPECTED_QUOTES, keyed.size)
        // docs/design-requests.md §4: known quotes without a verse key; the screen must not show them.
        assertEquals(KNOWN_UNKEYED, unkeyed.map { it.where }, "Quotes without a verse key")
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
        const val EXPECTED_QUOTES = 256
        val KNOWN_UNKEYED = List(2) { "diyaa-food.json/stations[8]/blocks[2]/items[0]" } +
            List(2) { "diyaa-food.json/stations[8]/blocks[2]/items[1]" }
        const val EXPECTED_POINTS = 663
    }
}
