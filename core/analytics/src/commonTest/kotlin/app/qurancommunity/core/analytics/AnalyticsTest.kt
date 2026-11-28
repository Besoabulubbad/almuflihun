package app.qurancommunity.core.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnalyticsTest {

    private class RecordingSink : AnalyticsSink {
        val events = mutableListOf<String>()
        val enabled = mutableListOf<Boolean>()
        override fun log(name: String, params: Map<String, Any>) {
            events += name
        }
        override fun setUserProperty(name: String, value: String) {
            events += "property:$name"
        }
        override fun setCollectionEnabled(enabled: Boolean) {
            this.enabled += enabled
        }
    }

    @Test
    fun `nothing is sent before consent or after a denial`() {
        val sink = RecordingSink()
        val analytics = Analytics(sink)
        analytics.log(AnalyticsEvent.MushafPage(1))
        analytics.setUserProperty(UserProperty.Theme, "dark")
        analytics.setConsent(AnalyticsConsent.Denied)
        analytics.log(AnalyticsEvent.MushafPage(2))
        assertEquals(emptyList(), sink.events)
        assertEquals(listOf(false, false), sink.enabled)
    }

    @Test
    fun `events flow only while consent is granted`() {
        val sink = RecordingSink()
        val analytics = Analytics(sink)
        analytics.setConsent(AnalyticsConsent.Granted)
        analytics.log(AnalyticsEvent.HizbOpen(2))
        analytics.setConsent(AnalyticsConsent.Denied)
        analytics.log(AnalyticsEvent.HizbOpen(3))
        assertEquals(listOf("hizb_open"), sink.events)
        assertEquals(listOf(false, true, false), sink.enabled)
    }

    @Test
    fun `names and parameters follow the plan's rules`() {
        val name = Regex("[a-z][a-z0-9_]{0,39}")
        allEvents.forEach { event ->
            assertTrue(name.matches(event.name), "bad event name ${event.name}")
            event.params.forEach { (key, value) ->
                assertTrue(name.matches(key), "bad parameter $key in ${event.name}")
                assertTrue(value is Int || (value is String && value.length <= MAX_VALUE), "bad value $value in $event")
                // Only the unlinked zero-result record may carry the query (tracking plan, rule 3).
                val forbidden = if (event.unlinked) FORBIDDEN - "query" else FORBIDDEN
                assertTrue(key !in forbidden, "${event.name} carries forbidden parameter $key")
            }
        }
    }

    @Test
    fun `every event has a distinct name per type`() {
        val byName = allEvents.groupBy { it.name }.mapValues { (_, events) -> events.map { it::class }.toSet() }
        byName.forEach { (name, types) -> assertEquals(1, types.size, "$name is used by $types") }
    }

    @Test
    fun `an unlinked event never reaches the analytics sink`() {
        val sink = RecordingSink()
        val unlinked = mutableListOf<Pair<String, Map<String, Any>>>()
        val analytics = Analytics(sink, AnalyticsConsent.Granted) { name, params -> unlinked += name to params }
        val zero = assertNotNull(AnalyticsEvent.SearchZero.of("صبر جميل", "صبر جميل", SearchKind.Letters))
        analytics.log(zero)
        analytics.log(AnalyticsEvent.SearchOptionsOpen())
        assertEquals(listOf("search_options_open"), sink.events)
        assertEquals(listOf("search_zero" to mapOf<String, Any>("query" to "صبر جميل", "kind" to "letters")), unlinked)
        assertEquals(listOf("search_zero"), allEvents.filter { it.unlinked }.map { it.name })
    }

    @Test
    fun `nothing unlinked is recorded without consent`() {
        val unlinked = mutableListOf<String>()
        val analytics = Analytics(RecordingSink(), AnalyticsConsent.Denied) { name, _ -> unlinked += name }
        analytics.log(assertNotNull(AnalyticsEvent.SearchZero.of("صبر", "صبر", SearchKind.Phrase)))
        assertEquals(emptyList(), unlinked)
    }

    @Test
    fun `zero-result words are sent only under the plan's conditions`() {
        fun of(raw: String, normalised: String = raw, kind: SearchKind = SearchKind.Letters) =
            AnalyticsEvent.SearchZero.of(raw, normalised, kind)

        assertNull(of("صبر", kind = SearchKind.Root), "root")
        assertNull(of("صبر", kind = SearchKind.Lemma), "lemma")
        assertNull(of("صبر", kind = SearchKind.Exact), "exact")
        assertNull(of("صبر", kind = SearchKind.Mixed), "mixed")
        assertNull(of("رقمي ٠٥٩١٢٣", "رقمي"), "Arabic-Indic digits: nothing is sent, not even the rest")
        assertNull(of("رقمي 0591", "رقمي"), "Latin digits")
        assertNull(of("اسمي ali", "اسمي"), "Latin letters")
        assertNull(of("ب@ريد", "بريد"), "@")
        assertNull(of("ص"), "too short")
        assertNull(of("ص".repeat(51)), "too long")
        assertNull(of("صبر!", "صبر!"), "not letters and spaces")
        assertEquals(
            mapOf<String, Any>("query" to "صبر جميل", "kind" to "whole_word"),
            of("  صَبْرٌ   جَمِيلٌ ", "  صبر   جميل ", SearchKind.WholeWord)?.params,
            "spaces are collapsed; the normalised form is what is sent",
        )
    }

    @Test
    fun `positions and task outcomes are checked`() {
        assertFailsWith<IllegalArgumentException> { AnalyticsEvent.DhikrCardOpen(DhikrCard.Moon, position = 0) }
        assertFailsWith<IllegalArgumentException> { AnalyticsEvent.AyahActionTap(AyahAction.Copy, position = 10) }
        assertFailsWith<IllegalArgumentException> {
            AnalyticsEvent.TaskTime(CoreTask.Tilawa, SecondsBucket.UpToFive, TapsBucket.One, TaskOutcome.Abandoned)
        }
    }

    @Test
    fun `result counts fall in v7's buckets`() {
        assertEquals(
            listOf("0", "1-10", "1-10", "11-100", "11-100", "100+"),
            listOf(0, 1, 10, 11, 100, 101).map { ResultsBucket.of(it).code },
        )
    }

    private companion object {
        const val MAX_VALUE = 40

        /** ADR 0008: no identity, no free text, no exact location. */
        val FORBIDDEN = setOf(
            "query", "text", "word", "root", "lemma", "verse", "verse_key", "ayah_text", "name", "email",
            "user_id", "uid", "lat", "lng", "latitude", "longitude", "note", "collection_name",
        )
    }
}
