package app.qurancommunity.core.analytics

// The short codes the tracking plan allows ("Parameter values"), so a value can never be free text.

enum class ScreenName(val code: String) {
    Dhikr("dhikr"), Moon("moon"), Study("study"), Door("door"), DoorAll("door_all"), Hikma("hikma"), Diyaa("diyaa"),
    Station("station"), CommunityDiscussion("community_discussion"), Post("post"), Profile("profile"), Live("live"),
    LiveDetails("live_details"), Videos("videos"), Video("video"), Moderators("moderators"), Compose("compose"),
    Quran("quran"), Mushaf("mushaf"), Hizb("hizb"), TilawaDone("tilawa_done"), Search("search"),
    Collections("collections"), Collection("collection"), VersePicker("verse_picker"), Settings("settings"),
    TilawaSettings("tilawa_settings"), MyTime("my_time"), Reciter("reciter"), OfflineAudio("offline_audio"),
    Appearance("appearance"), Notifications("notifications"), Location("location"), CityPicker("city_picker"),
    About("about"), WhatWeCollect("what_we_collect"), Feedback("feedback"), Sources("sources"), Update("update"),
    Onboarding("onboarding"), Admin("admin"), AdminInbox("admin_inbox"), WordStudy("word_study"),
    AyahStudy("ayah_study"), SurahOverview("surah_overview"),
}

enum class Surface(val code: String) { Mushaf("mushaf"), Hizb("hizb") }
enum class MinutesBucket(val code: String) { UnderOne("0-1"), OneToFive("1-5"), FiveToFifteen("5-15"), More("15+") }
enum class PlayMode(val code: String) {
    Ayah("ayah"), FromHere("from_here"), Hizb("hizb"), Surah("surah"), Collection("collection"),
}
enum class Reciter(val code: String) { AliJaber("ali_jaber") }
enum class AudioErrorKind(val code: String) {
    Network("network"), Source("source"), Timing("timing"), Storage("storage"), Other("other"),
}
enum class Target(val code: String) { Door("door"), Mushaf("mushaf") }

enum class SearchKind(val code: String) {
    Surah("surah"), Jump("jump"), Letters("letters"), WholeWord("whole_word"), Phrase("phrase"), Root("root"),
    Lemma("lemma"), Exact("exact"), Mixed("mixed"),
}

enum class ResultsBucket(val code: String) {
    None("0"), Few("1-10"), Some("11-100"), Many("100+");

    companion object {
        private const val FEW = 10
        private const val SOME = 100

        fun of(count: Int): ResultsBucket = when {
            count <= 0 -> None
            count <= FEW -> Few
            count <= SOME -> Some
            else -> Many
        }
    }
}

enum class TermsBucket(val code: String) { One("1"), Two("2"), ThreeOrMore("3+") }
enum class SearchFrom(val code: String) { Dhikr("dhikr"), Quran("quran"), Widget("widget"), Link("link") }
enum class RankBucket(val code: String) { First("1"), TwoToFive("2-5"), SixOrMore("6+") }
enum class FormCountBucket(val code: String) { Two("2"), ThreeToFive("3-5"), SixOrMore("6+") }

enum class SaveKind(val code: String) { Ayah("ayah"), Quote("quote") }
enum class SaveFrom(val code: String) {
    AyahSheet("ayah_sheet"), Panel("panel"), Word("word"), SearchResults("search_results"),
}
enum class CountBucket(val code: String) { One("1"), TwoToFive("2-5"), SixOrMore("6+") }
enum class ShareTarget(val code: String) { Text("text"), Community("community") }

enum class PostType(val code: String) {
    Verse("verse"), Question("question"), Answer("answer"), Reflection("reflection"), Comment("comment"),
}
enum class ComposeFrom(val code: String) {
    Fab("fab"), AyahSheet("ayah_sheet"), Live("live"), Collection("collection"), Thread("thread"), Widget("widget"),
}
enum class ComposeStep(val code: String) { Type("type"), Text("text"), Verse("verse"), Send("send") }
enum class LiveFrom(val code: String) {
    Banner("banner"), List("list"), Details("details"), Notification("notification"), Widget("widget"),
}

enum class NotificationCategory(val code: String) {
    ReminderMorning("reminder_morning"), ReminderEvening("reminder_evening"), LiveReminder("live_reminder"),
    LiveNow("live_now"), Answers("answers"), ModeratorReplies("moderator_replies"), NewPosts("new_posts"),
    Update("update"), Announcement("announcement"), Playback("playback"),
}
enum class ThemeMode(val code: String) { Light("light"), Dark("dark"), System("system") }
enum class LocationSource(val code: String) { Gps("gps"), City("city"), None("none") }
enum class Reminder(val code: String) { Morning("morning"), Evening("evening") }

enum class StudyTool(val code: String) {
    Panel("panel"), Word("word"), RelatedPhrase("related_phrase"), RelatedRoot("related_root"), Overview("overview"),
}
enum class StudyFrom(val code: String) {
    AyahSheet("ayah_sheet"), Mushaf("mushaf"), Search("search"), Collection("collection"), Panel("panel"),
    Index("index"),
}

enum class Tab(val code: String) { Dhikr("dhikr"), Community("community"), Quran("quran"), Settings("settings") }
enum class DhikrCard(val code: String) {
    Search("search"), Tilawa("tilawa"), Week("week"), Moon("moon"), TadhkiraDoor("tadhkira_door"),
    TadhkiraMushaf("tadhkira_mushaf"), TileStudy("tile_study"), TileDiyaa("tile_diyaa"), TileFurqan("tile_furqan"),
}
enum class AyahSheetFrom(val code: String) {
    Mushaf("mushaf"), Hizb("hizb"), Search("search"), Door("door"), Station("station"), Collection("collection"),
    Community("community"), Dhikr("dhikr"),
}
enum class AyahAction(val code: String) {
    Listen("listen"), FromHere("from_here"), Save("save"), Share("share"), Copy("copy"), Ask("ask"), Door("door"),
}
enum class MediaAction(val code: String) {
    Play("play"), Pause("pause"), Next("next"), Previous("previous"), Repeat("repeat"), Speed("speed"), Stop("stop"),
}
enum class MediaSource(val code: String) {
    App("app"), Notification("notification"), LockScreen("lock_screen"), Headset("headset"), Widget("widget"),
}
enum class PlayScope(val code: String) { Surah("surah"), Hizb("hizb"), Quarter("quarter"), Ayah("ayah"), Off("off") }
enum class SleepTimer(val code: String) {
    Fifteen("15"), Thirty("30"), Sixty("60"), EndOfScope("end"), Off("off"),
}
enum class Widget(val code: String) {
    QuickBar("quick_bar"), Tilawa("tilawa"), Moon("moon"), Tadhkira("tadhkira"), Community("community"),
}
enum class WidgetLink(val code: String) {
    Search("search"), Page("page"), Moon("moon"), Ayah("ayah"), Community("community"), Compose("compose"),
}
enum class ModeratorMessageKind(val code: String) { Question("question"), Report("report"), Suggestion("suggestion") }
enum class SignInReason(val code: String) {
    Ask("ask"), Answer("answer"), Send("send"), Moderators("moderators"), Settings("settings"),
}
enum class SignInResult(val code: String) { Done("done"), Cancel("cancel"), Error("error") }

enum class CoreTask(val code: String) {
    Tilawa("tilawa"), FindVerse("find_verse"), OpenAyah("open_ayah"), SaveAyah("save_ayah"),
}
enum class SecondsBucket(val code: String) {
    UpToFive("0-5"), FiveToTen("5-10"), TenToThirty("10-30"), ThirtyToSixty("30-60"), More("60+"),
}
enum class TapsBucket(val code: String) { One("1"), Two("2"), Three("3"), FourToFive("4-5"), SixOrMore("6+") }
enum class TaskOutcome(val code: String) { Done("done"), Abandoned("abandoned") }
enum class EaseScore(val code: String) {
    One("1"), Two("2"), Three("3"), Four("4"), Five("5"), Six("6"), Seven("7"), Skip("skip"),
}
