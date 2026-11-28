package app.qurancommunity.core.analytics

/**
 * Every event the app may send: exactly the tracking plan v1 (docs/analytics/tracking-plan.md, ADR 0008, ADR 0020).
 * Anything not here cannot be logged, because [Analytics.log] only takes these types. Values are counts, buckets, ids
 * of app content, or short codes; never text a person typed (except [SearchZero]), never verse text, never identity.
 *
 * [unlinked] events never reach Firebase Analytics, which attaches an app-instance id to everything; they go to the
 * separate [UnlinkedSink] (tracking plan, "Zero-result searches").
 */
sealed class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap(),
    val unlinked: Boolean = false,
) {

    // Q1
    class ScreenView(screen: ScreenName) : AnalyticsEvent("screen_view", mapOf("screen_name" to screen.code))

    // Q2
    class MushafPage(page: Int) : AnalyticsEvent("mushaf_page", mapOf("page" to page))
    class ReadingEnd(surface: Surface, pages: Int, minutes: MinutesBucket, lastPage: Int) : AnalyticsEvent(
        "reading_end",
        mapOf("surface" to surface.code, "pages" to pages, "minutes_bucket" to minutes.code, "last_page" to lastPage),
    )

    // Q3
    class HizbOpen(hizb: Int) : AnalyticsEvent("hizb_open", mapOf("hizb" to hizb))
    class HizbFinish(hizb: Int) : AnalyticsEvent("hizb_finish", mapOf("hizb" to hizb))
    class HizbNext(hizb: Int) : AnalyticsEvent("hizb_next", mapOf("hizb" to hizb))

    // Q4
    class AudioPlay(mode: PlayMode, reciter: Reciter) :
        AnalyticsEvent("audio_play", mapOf("mode" to mode.code, "reciter" to reciter.code))
    class AudioDownload(surah: Int) : AnalyticsEvent("audio_download", mapOf("surah" to surah))
    class AudioError(kind: AudioErrorKind) : AnalyticsEvent("audio_error", mapOf("kind" to kind.code))

    // Q5
    class DoorOpen(doorId: String) : AnalyticsEvent("door_open", mapOf("door_id" to doorId))

    // Q6
    class StationOpen(station: Int) : AnalyticsEvent("station_open", mapOf("station" to station))
    class JourneyFinish : AnalyticsEvent("journey_finish")

    // Q7, Q17
    class Search(
        kind: SearchKind,
        results: ResultsBucket,
        terms: TermsBucket,
        ops: Boolean,
        scoped: Boolean,
        forms: Boolean,
        from: SearchFrom,
    ) : AnalyticsEvent(
        "search",
        mapOf(
            "kind" to kind.code,
            "results_bucket" to results.code,
            "terms" to terms.code,
            "ops" to yesNo(ops),
            "scoped" to yesNo(scoped),
            "forms" to yesNo(forms),
            "from" to from.code,
        ),
    )
    class SearchOpenResult(kind: SearchKind, rank: RankBucket) :
        AnalyticsEvent("search_open_result", mapOf("kind" to kind.code, "rank" to rank.code))
    class SearchOptionsOpen : AnalyticsEvent("search_options_open")

    /**
     * The normalised words of a search that found nothing (ADR 0020 §3). Built only through [of], which applies every
     * condition in the plan; the once-per-day limit is kept by the caller that owns the device's storage.
     */
    class SearchZero private constructor(query: String, kind: SearchKind) :
        AnalyticsEvent("search_zero", mapOf("query" to query, "kind" to kind.code), unlinked = true) {

        companion object {
            private const val MIN_LENGTH = 2
            private const val MAX_LENGTH = 50
            private val allowedKinds = setOf(SearchKind.Letters, SearchKind.WholeWord, SearchKind.Phrase)

            /**
             * [raw] is what the person typed, [normalised] the search's forgiving form of it. Returns null (send
             * nothing) unless the kind may be sent, the raw text has no digit, Latin letter or `@`, and the normalised
             * text is 2–50 Arabic letters and spaces.
             */
            fun of(raw: String, normalised: String, kind: SearchKind): SearchZero? {
                val query = normalised.trim().split(' ').filter { it.isNotEmpty() }.joinToString(" ")
                val allowed = kind in allowedKinds &&
                    raw.none(::mayIdentify) &&
                    query.length in MIN_LENGTH..MAX_LENGTH &&
                    query.all { it == ' ' || it in ARABIC_LETTERS }
                return if (allowed) SearchZero(query, kind) else null
            }

            /** A digit (any script), a Latin letter or `@`: a phone number, email or handle may be in the query. */
            private fun mayIdentify(char: Char): Boolean =
                char.isDigit() || char.lowercaseChar() in 'a'..'z' || char == '@'

            /** ء to ي: the letters left after the search's normalisation. */
            private val ARABIC_LETTERS = 'ء'..'ي'
        }
    }

    // Q8, Q18
    class SaveAdd(kind: SaveKind, from: SaveFrom, inOther: Boolean) :
        AnalyticsEvent("save_add", mapOf("kind" to kind.code, "from" to from.code, "in_other" to yesNo(inOther)))
    class SaveRemove(kind: SaveKind) : AnalyticsEvent("save_remove", mapOf("kind" to kind.code))
    class CollectionCreate(count: CountBucket) : AnalyticsEvent("collection_create", mapOf("count" to count.code))
    class CollectionShare(target: ShareTarget) : AnalyticsEvent("collection_share", mapOf("target" to target.code))
    class NoteAdd : AnalyticsEvent("note_add")

    // Q9
    class ComposeStart(type: PostType, from: ComposeFrom) :
        AnalyticsEvent("compose_start", mapOf("type" to type.code, "from" to from.code))
    class ComposeSubmit(type: PostType) : AnalyticsEvent("compose_submit", mapOf("type" to type.code))
    class ComposeCancel(type: PostType, step: ComposeStep) :
        AnalyticsEvent("compose_cancel", mapOf("type" to type.code, "step" to step.code))

    // Q10
    class LiveOpenTiktok(showId: String, from: LiveFrom) :
        AnalyticsEvent("live_open_tiktok", mapOf("show_id" to showId, "from" to from.code))
    class VideoPlay(videoId: String) : AnalyticsEvent("video_play", mapOf("video_id" to videoId))
    class RemindToggle(showId: String, on: Boolean, from: LiveFrom) :
        AnalyticsEvent("remind_toggle", mapOf("show_id" to showId, "on" to yesNo(on), "from" to from.code))

    // Q11
    class NotificationOpen(category: NotificationCategory) :
        AnalyticsEvent("notification_open", mapOf("category" to category.code))
    class NotificationSetting(category: NotificationCategory, on: Boolean) :
        AnalyticsEvent("notification_setting", mapOf("category" to category.code, "on" to yesNo(on)))

    // Q12
    class ThemeSet(mode: ThemeMode) : AnalyticsEvent("theme_set", mapOf("mode" to mode.code))
    class LocationSourceSet(source: LocationSource) :
        AnalyticsEvent("location_source", mapOf("source" to source.code))
    class ReminderSet(which: Reminder, on: Boolean) :
        AnalyticsEvent("reminder_set", mapOf("which" to which.code, "on" to yesNo(on)))

    // Q13
    class UpdateSeen(version: String) : AnalyticsEvent("update_seen", mapOf("version" to version))
    class UpdateOpen(version: String) : AnalyticsEvent("update_open", mapOf("version" to version))

    // Q15
    class ContentMismatch : AnalyticsEvent("content_mismatch")

    // Q16
    class StudyOpen(tool: StudyTool, from: StudyFrom) :
        AnalyticsEvent("study_open", mapOf("tool" to tool.code, "from" to from.code))
    class StudyFollow(tool: StudyTool, target: Target) :
        AnalyticsEvent("study_follow", mapOf("tool" to tool.code, "target" to target.code))

    // Q19
    class TabOpen(tab: Tab) : AnalyticsEvent("tab_open", mapOf("tab" to tab.code))
    class DhikrCardOpen(card: DhikrCard, position: Int) :
        AnalyticsEvent("dhikr_card_open", mapOf("card" to card.code, "position" to position(position)))

    // Q20
    class AyahSheetOpen(from: AyahSheetFrom) : AnalyticsEvent("ayah_sheet_open", mapOf("from" to from.code))
    class AyahActionTap(action: AyahAction, position: Int) :
        AnalyticsEvent("ayah_action", mapOf("action" to action.code, "position" to position(position)))

    // Q21
    class MediaControl(action: MediaAction, source: MediaSource) :
        AnalyticsEvent("media_control", mapOf("action" to action.code, "source" to source.code))
    class PlayerOptionSet private constructor(option: String, value: String) :
        AnalyticsEvent("player_option", mapOf("option" to option, "value" to value)) {
        constructor(scope: PlayScope) : this("scope", scope.code)
        constructor(sleep: SleepTimer) : this("sleep", sleep.code)
    }

    // Q22
    class WidgetOpen(widget: Widget, link: WidgetLink) :
        AnalyticsEvent("widget_open", mapOf("widget" to widget.code, "link" to link.code))

    // Q23
    class SpellingFormOpen(rank: RankBucket, formCount: FormCountBucket) :
        AnalyticsEvent("spelling_form_open", mapOf("rank" to rank.code, "form_count" to formCount.code))

    // Q24
    class ModeratorMessageSend(kind: ModeratorMessageKind, withVerse: Boolean) :
        AnalyticsEvent("moderator_message_send", mapOf("kind" to kind.code, "with_verse" to yesNo(withVerse)))

    // Q25
    class SignInPrompt(reason: SignInReason) : AnalyticsEvent("sign_in_prompt", mapOf("reason" to reason.code))
    class SignInResultEvent(reason: SignInReason, result: SignInResult) :
        AnalyticsEvent("sign_in_result", mapOf("reason" to reason.code, "result" to result.code))

    // Q26
    class TaskTime(task: CoreTask, seconds: SecondsBucket, taps: TapsBucket, outcome: TaskOutcome) : AnalyticsEvent(
        "task_time",
        mapOf("task" to task.code, "seconds" to seconds.code, "taps" to taps.code, "outcome" to outcome.code),
    ) {
        init {
            require(!(task == CoreTask.Tilawa && outcome == TaskOutcome.Abandoned)) { "a visit is not an intention" }
        }
    }

    // Q27
    class EaseAnswer(task: CoreTask, score: EaseScore) :
        AnalyticsEvent("ease_answer", mapOf("task" to task.code, "score" to score.code))

    override fun toString(): String = "$name$params"
}

private fun yesNo(value: Boolean) = if (value) "yes" else "no"

private const val MAX_POSITION = 9

private fun position(value: Int): Int {
    require(value in 1..MAX_POSITION) { "position $value is outside 1–$MAX_POSITION" }
    return value
}
