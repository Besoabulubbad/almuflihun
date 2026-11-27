package app.almuflihun.core.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
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
                assertTrue(key !in FORBIDDEN, "${event.name} carries forbidden parameter $key")
            }
        }
    }

    @Test
    fun `every event has a distinct name per type`() {
        val byName = allEvents.groupBy { it.name }.mapValues { (_, events) -> events.map { it::class }.toSet() }
        byName.forEach { (name, types) -> assertEquals(1, types.size, "$name is used by $types") }
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
