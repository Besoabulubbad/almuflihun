package app.almuflihun.core.analytics

/**
 * One instance of every event. The `when` in [checkListed] has no `else`, so adding an event without adding it here
 * stops the tests compiling: every event is checked against the rules and the plan.
 */
internal val allEvents: List<AnalyticsEvent> = listOf(
    AnalyticsEvent.ScreenView("HomeKey"),
    AnalyticsEvent.MushafPage(44),
    AnalyticsEvent.ReadingEnd(Surface.Mushaf, 3, MinutesBucket.OneToFive, 46),
    AnalyticsEvent.HizbOpen(2),
    AnalyticsEvent.HizbFinish(2),
    AnalyticsEvent.HizbNext(3),
    AnalyticsEvent.AudioPlay(PlayMode.FromHere, "ali_jaber"),
    AnalyticsEvent.AudioDownload(18),
    AnalyticsEvent.AudioError("stream"),
    AnalyticsEvent.DoorOpen("infaq"),
    AnalyticsEvent.TadhkiraOpen(Target.Mushaf),
    AnalyticsEvent.StationOpen(4),
    AnalyticsEvent.JourneyFinish(),
    AnalyticsEvent.Search(SearchKind.Root, ResultsBucket.Some, TermsBucket.Two, ops = true, scoped = false),
    AnalyticsEvent.SearchOpenResult(SearchKind.Root),
    AnalyticsEvent.SaveAdd(SaveKind.Ayah, SaveFrom.AyahSheet, inOther = false),
    AnalyticsEvent.SaveRemove(SaveKind.Quote),
    AnalyticsEvent.CollectionCreate(CountBucket.TwoToFive),
    AnalyticsEvent.CollectionShare(ShareTarget.Text),
    AnalyticsEvent.NoteAdd(),
    AnalyticsEvent.ComposeStart(PostType.Question),
    AnalyticsEvent.ComposeSubmit(PostType.Question),
    AnalyticsEvent.ComposeCancel(PostType.Answer, "verse"),
    AnalyticsEvent.LiveOpenTiktok("show-1"),
    AnalyticsEvent.VideoPlay("video-1"),
    AnalyticsEvent.RemindToggle("show-1", on = true),
    AnalyticsEvent.NotificationOpen("live"),
    AnalyticsEvent.NotificationSetting("posts.new", on = false),
    AnalyticsEvent.ThemeSet("dark"),
    AnalyticsEvent.LocationSourceSet(LocationSource.City),
    AnalyticsEvent.ReminderSet("morning", on = true),
    AnalyticsEvent.UpdateSeen("1.2.0"),
    AnalyticsEvent.UpdateOpen("1.2.0"),
    AnalyticsEvent.ContentMismatch(),
    AnalyticsEvent.StudyOpen(StudyTool.Word, StudyFrom.Mushaf),
    AnalyticsEvent.StudyFollow(StudyTool.Panel, Target.Door),
)

@Suppress("CyclomaticComplexMethod", "unused")
internal fun checkListed(event: AnalyticsEvent): Unit = when (event) {
    is AnalyticsEvent.ScreenView, is AnalyticsEvent.MushafPage, is AnalyticsEvent.ReadingEnd,
    is AnalyticsEvent.HizbOpen, is AnalyticsEvent.HizbFinish, is AnalyticsEvent.HizbNext,
    is AnalyticsEvent.AudioPlay, is AnalyticsEvent.AudioDownload, is AnalyticsEvent.AudioError,
    is AnalyticsEvent.DoorOpen, is AnalyticsEvent.TadhkiraOpen, is AnalyticsEvent.StationOpen,
    is AnalyticsEvent.JourneyFinish, is AnalyticsEvent.Search, is AnalyticsEvent.SearchOpenResult,
    is AnalyticsEvent.SaveAdd, is AnalyticsEvent.SaveRemove, is AnalyticsEvent.CollectionCreate,
    is AnalyticsEvent.CollectionShare, is AnalyticsEvent.NoteAdd, is AnalyticsEvent.ComposeStart,
    is AnalyticsEvent.ComposeSubmit, is AnalyticsEvent.ComposeCancel, is AnalyticsEvent.LiveOpenTiktok,
    is AnalyticsEvent.VideoPlay, is AnalyticsEvent.RemindToggle, is AnalyticsEvent.NotificationOpen,
    is AnalyticsEvent.NotificationSetting, is AnalyticsEvent.ThemeSet, is AnalyticsEvent.LocationSourceSet,
    is AnalyticsEvent.ReminderSet, is AnalyticsEvent.UpdateSeen, is AnalyticsEvent.UpdateOpen,
    is AnalyticsEvent.ContentMismatch, is AnalyticsEvent.StudyOpen, is AnalyticsEvent.StudyFollow,
    -> Unit
}
