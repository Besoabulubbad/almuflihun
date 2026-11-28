package app.qurancommunity.core.analytics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The user's one-time answer to the consent prompt (DESIGN Prompts; ADR 0008). Unknown counts as no. */
enum class AnalyticsConsent { Unknown, Granted, Denied }

/** Where events go: Firebase Analytics from Phase 7; nothing before. */
interface AnalyticsSink {
    fun log(name: String, params: Map<String, Any>)
    fun setUserProperty(name: String, value: String)
    fun setCollectionEnabled(enabled: Boolean)
}

/** Sends nothing. Used until Firebase is set up, and in tests. */
object NoAnalyticsSink : AnalyticsSink {
    override fun log(name: String, params: Map<String, Any>) = Unit
    override fun setUserProperty(name: String, value: String) = Unit
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}

/**
 * Where [AnalyticsEvent.unlinked] events go (tracking plan, "Zero-result searches"): a create-only record with no user
 * or install link, never Firebase Analytics. Set up in Phase 7; nothing before.
 */
fun interface UnlinkedSink {
    fun record(name: String, params: Map<String, Any>)
}

/** Records nothing. Used until the backend is set up, and in tests. */
object NoUnlinkedSink : UnlinkedSink {
    override fun record(name: String, params: Map<String, Any>) = Unit
}

/** The user properties the plan allows: no identity, never which staff role. */
enum class UserProperty(val key: String) {
    Theme("theme"),
    SignedIn("signed_in"),
    Staff("staff"),
    LocationSource("location_source"),
}

/**
 * The only way the app sends analytics (ADR 0008). Nothing leaves before consent is [AnalyticsConsent.Granted]; a
 * denial also asks the SDK to stop collecting on the device. Events are typed, so only the tracking plan can be sent;
 * unlinked ones go only to the [UnlinkedSink].
 */
class Analytics(
    private val sink: AnalyticsSink,
    initialConsent: AnalyticsConsent = AnalyticsConsent.Unknown,
    private val unlinkedSink: UnlinkedSink = NoUnlinkedSink,
) {

    private val consentState = MutableStateFlow(initialConsent)

    /** The current answer, for Settings → الخصوصية. */
    val consent: StateFlow<AnalyticsConsent> = consentState.asStateFlow()

    init {
        sink.setCollectionEnabled(initialConsent == AnalyticsConsent.Granted)
    }

    /** Records the user's answer from the prompt or the Settings switch. */
    fun setConsent(consent: AnalyticsConsent) {
        consentState.value = consent
        sink.setCollectionEnabled(consent == AnalyticsConsent.Granted)
    }

    fun log(event: AnalyticsEvent) {
        if (consentState.value != AnalyticsConsent.Granted) return
        // An unlinked event must never reach the analytics SDK, which ties every event to the app instance.
        if (event.unlinked) unlinkedSink.record(event.name, event.params) else sink.log(event.name, event.params)
    }

    fun setUserProperty(property: UserProperty, value: String) {
        if (consentState.value == AnalyticsConsent.Granted) sink.setUserProperty(property.key, value)
    }
}
