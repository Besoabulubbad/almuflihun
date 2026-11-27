package app.almuflihun.core.analytics

/**
 * Every event the app may send: exactly the tracking plan (docs/analytics/tracking-plan.md, ADR 0008). Anything not
 * here cannot be logged, because [Analytics.log] only takes these types. Values are counts, buckets, ids of app
 * content, or short codes; never text a person typed, never verse text, never identity.
 */
sealed class AnalyticsEvent(val name: String, val params: Map<String, Any> = emptyMap()) {

    // Q1
    class ScreenView(screen: String) : AnalyticsEvent("screen_view", mapOf("screen_name" to screen))

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
    class AudioPlay(mode: PlayMode, reciter: String) :
        AnalyticsEvent("audio_play", mapOf("mode" to mode.code, "reciter" to reciter))
    class AudioDownload(surah: Int) : AnalyticsEvent("audio_download", mapOf("surah" to surah))
    class AudioError(kind: String) : AnalyticsEvent("audio_error", mapOf("kind" to kind))

    // Q5
    class DoorOpen(doorId: String) : AnalyticsEvent("door_open", mapOf("door_id" to doorId))
    class TadhkiraOpen(target: Target) : AnalyticsEvent("tadhkira_open", mapOf("target" to target.code))

    // Q6
    class StationOpen(station: Int) : AnalyticsEvent("station_open", mapOf("station" to station))
    class JourneyFinish : AnalyticsEvent("journey_finish")

    // Q7 and Q17
    class Search(kind: SearchKind, results: ResultsBucket, terms: TermsBucket, ops: Boolean, scoped: Boolean) :
        AnalyticsEvent(
            "search",
            mapOf(
                "kind" to kind.code,
                "results_bucket" to results.code,
                "terms" to terms.code,
                "ops" to yesNo(ops),
                "scoped" to yesNo(scoped),
            ),
        )
    class SearchOpenResult(kind: SearchKind) : AnalyticsEvent("search_open_result", mapOf("kind" to kind.code))

    // Q8 and Q18
    class SaveAdd(kind: SaveKind, from: SaveFrom, inOther: Boolean) :
        AnalyticsEvent("save_add", mapOf("kind" to kind.code, "from" to from.code, "in_other" to yesNo(inOther)))
    class SaveRemove(kind: SaveKind) : AnalyticsEvent("save_remove", mapOf("kind" to kind.code))
    class CollectionCreate(count: CountBucket) : AnalyticsEvent("collection_create", mapOf("count" to count.code))
    class CollectionShare(target: ShareTarget) : AnalyticsEvent("collection_share", mapOf("target" to target.code))
    class NoteAdd : AnalyticsEvent("note_add")

    // Q9
    class ComposeStart(type: PostType) : AnalyticsEvent("compose_start", mapOf("type" to type.code))
    class ComposeSubmit(type: PostType) : AnalyticsEvent("compose_submit", mapOf("type" to type.code))
    class ComposeCancel(type: PostType, step: String) :
        AnalyticsEvent("compose_cancel", mapOf("type" to type.code, "step" to step))

    // Q10
    class LiveOpenTiktok(showId: String) : AnalyticsEvent("live_open_tiktok", mapOf("show_id" to showId))
    class VideoPlay(videoId: String) : AnalyticsEvent("video_play", mapOf("video_id" to videoId))
    class RemindToggle(showId: String, on: Boolean) :
        AnalyticsEvent("remind_toggle", mapOf("show_id" to showId, "on" to yesNo(on)))

    // Q11
    class NotificationOpen(category: String) : AnalyticsEvent("notification_open", mapOf("category" to category))
    class NotificationSetting(category: String, on: Boolean) :
        AnalyticsEvent("notification_setting", mapOf("category" to category, "on" to yesNo(on)))

    // Q12
    class ThemeSet(mode: String) : AnalyticsEvent("theme_set", mapOf("mode" to mode))
    class LocationSourceSet(source: LocationSource) :
        AnalyticsEvent("location_source", mapOf("source" to source.code))
    class ReminderSet(which: String, on: Boolean) :
        AnalyticsEvent("reminder_set", mapOf("which" to which, "on" to yesNo(on)))

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

    override fun toString(): String = "$name$params"
}

private fun yesNo(value: Boolean) = if (value) "yes" else "no"

/** Short codes the plan allows, so a value can never be free text. */
enum class Surface(val code: String) { Mushaf("mushaf"), Hizb("hizb") }
enum class MinutesBucket(val code: String) { UnderOne("0-1"), OneToFive("1-5"), FiveToFifteen("5-15"), More("15+") }
enum class PlayMode(val code: String) { Ayah("ayah"), FromHere("from_here"), Hizb("hizb") }
enum class Target(val code: String) { Door("door"), Mushaf("mushaf") }
enum class SearchKind(val code: String) {
    Surah("surah"),
    Jump("jump"),
    Words("words"),
    Root("root"),
    Lemma("lemma"),
    Phrase("phrase"),
    Mixed("mixed"),
}
enum class ResultsBucket(val code: String) { None("0"), Few("1-5"), Some("6-50"), Many("50+") }
enum class TermsBucket(val code: String) { One("1"), Two("2"), ThreeOrMore("3+") }
enum class SaveKind(val code: String) { Ayah("ayah"), Quote("quote") }
enum class SaveFrom(val code: String) {
    AyahSheet("ayah_sheet"),
    Panel("panel"),
    Word("word"),
    SearchResults("search_results"),
}
enum class CountBucket(val code: String) { One("1"), TwoToFive("2-5"), SixOrMore("6+") }
enum class ShareTarget(val code: String) { Text("text"), Community("community") }
enum class PostType(val code: String) {
    Verse("verse"),
    Question("question"),
    Answer("answer"),
    Reflection("reflection"),
    Comment("comment"),
}
enum class LocationSource(val code: String) { Gps("gps"), City("city"), None("none") }
enum class StudyTool(val code: String) {
    Panel("panel"),
    Word("word"),
    RelatedPhrase("related_phrase"),
    RelatedRoot("related_root"),
    Overview("overview"),
}
enum class StudyFrom(val code: String) {
    AyahSheet("ayah_sheet"),
    Mushaf("mushaf"),
    Search("search"),
    Collection("collection"),
    Panel("panel"),
    Index("index"),
}
