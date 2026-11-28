package app.qurancommunity.core.designsystem.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import app.qurancommunity.core.designsystem.resources.Res
import app.qurancommunity.core.designsystem.resources.ic_bell
import app.qurancommunity.core.designsystem.resources.ic_book_open
import app.qurancommunity.core.designsystem.resources.ic_book_open_text
import app.qurancommunity.core.designsystem.resources.ic_bookmark
import app.qurancommunity.core.designsystem.resources.ic_bookmark_check_filled
import app.qurancommunity.core.designsystem.resources.ic_check
import app.qurancommunity.core.designsystem.resources.ic_chevron_down
import app.qurancommunity.core.designsystem.resources.ic_chevron_left
import app.qurancommunity.core.designsystem.resources.ic_chevron_right
import app.qurancommunity.core.designsystem.resources.ic_chevron_up
import app.qurancommunity.core.designsystem.resources.ic_circle_alert
import app.qurancommunity.core.designsystem.resources.ic_circle_check
import app.qurancommunity.core.designsystem.resources.ic_circle_x
import app.qurancommunity.core.designsystem.resources.ic_clock
import app.qurancommunity.core.designsystem.resources.ic_compass
import app.qurancommunity.core.designsystem.resources.ic_copy
import app.qurancommunity.core.designsystem.resources.ic_door_open
import app.qurancommunity.core.designsystem.resources.ic_download
import app.qurancommunity.core.designsystem.resources.ic_ellipsis_vertical
import app.qurancommunity.core.designsystem.resources.ic_external_link
import app.qurancommunity.core.designsystem.resources.ic_flag
import app.qurancommunity.core.designsystem.resources.ic_gauge
import app.qurancommunity.core.designsystem.resources.ic_grip_vertical
import app.qurancommunity.core.designsystem.resources.ic_heart
import app.qurancommunity.core.designsystem.resources.ic_image
import app.qurancommunity.core.designsystem.resources.ic_info
import app.qurancommunity.core.designsystem.resources.ic_lightbulb
import app.qurancommunity.core.designsystem.resources.ic_list
import app.qurancommunity.core.designsystem.resources.ic_list_start
import app.qurancommunity.core.designsystem.resources.ic_log_out
import app.qurancommunity.core.designsystem.resources.ic_map_pin
import app.qurancommunity.core.designsystem.resources.ic_maximize
import app.qurancommunity.core.designsystem.resources.ic_message_circle
import app.qurancommunity.core.designsystem.resources.ic_message_circle_question
import app.qurancommunity.core.designsystem.resources.ic_mic
import app.qurancommunity.core.designsystem.resources.ic_minimize
import app.qurancommunity.core.designsystem.resources.ic_minus
import app.qurancommunity.core.designsystem.resources.ic_monitor
import app.qurancommunity.core.designsystem.resources.ic_moon
import app.qurancommunity.core.designsystem.resources.ic_pause
import app.qurancommunity.core.designsystem.resources.ic_pencil
import app.qurancommunity.core.designsystem.resources.ic_play
import app.qurancommunity.core.designsystem.resources.ic_plus
import app.qurancommunity.core.designsystem.resources.ic_quote
import app.qurancommunity.core.designsystem.resources.ic_radio
import app.qurancommunity.core.designsystem.resources.ic_refresh_cw
import app.qurancommunity.core.designsystem.resources.ic_repeat_1
import app.qurancommunity.core.designsystem.resources.ic_scale
import app.qurancommunity.core.designsystem.resources.ic_search
import app.qurancommunity.core.designsystem.resources.ic_search_x
import app.qurancommunity.core.designsystem.resources.ic_send
import app.qurancommunity.core.designsystem.resources.ic_settings
import app.qurancommunity.core.designsystem.resources.ic_share_2
import app.qurancommunity.core.designsystem.resources.ic_skip_back
import app.qurancommunity.core.designsystem.resources.ic_skip_forward
import app.qurancommunity.core.designsystem.resources.ic_sparkles
import app.qurancommunity.core.designsystem.resources.ic_sun
import app.qurancommunity.core.designsystem.resources.ic_sunrise
import app.qurancommunity.core.designsystem.resources.ic_sunset
import app.qurancommunity.core.designsystem.resources.ic_timer
import app.qurancommunity.core.designsystem.resources.ic_trash_2
import app.qurancommunity.core.designsystem.resources.ic_user_round
import app.qurancommunity.core.designsystem.resources.ic_users_round
import app.qurancommunity.core.designsystem.resources.ic_volume_2
import app.qurancommunity.core.designsystem.resources.ic_wheat
import app.qurancommunity.core.designsystem.resources.ic_wifi_off
import app.qurancommunity.core.designsystem.resources.ic_x
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Every icon the app uses, by meaning (DESIGN §7: Lucide only, one icon per meaning). Directional icons are drawn in
 * their left-to-right form and mirrored in right-to-left layouts, so they stay correct when English arrives (ADR 0014):
 * [Back] is `chevron-left` in English and `chevron-right` in Arabic, as DESIGN §2 requires.
 */
enum class AppIcon(internal val resource: DrawableResource, internal val mirrorInRtl: Boolean = false) {
    // Navigation and direction
    Back(Res.drawable.ic_chevron_left, mirrorInRtl = true),
    Forward(Res.drawable.ic_chevron_right, mirrorInRtl = true),
    Expand(Res.drawable.ic_chevron_down),
    Collapse(Res.drawable.ic_chevron_up),
    Close(Res.drawable.ic_x),
    More(Res.drawable.ic_ellipsis_vertical),
    Reorder(Res.drawable.ic_grip_vertical),
    FullScreen(Res.drawable.ic_maximize),
    ExitFullScreen(Res.drawable.ic_minimize),
    OpenExternal(Res.drawable.ic_external_link, mirrorInRtl = true),

    // Home and hubs
    Profile(Res.drawable.ic_user_round),
    Settings(Res.drawable.ic_settings),
    Reminders(Res.drawable.ic_bell),
    PortalDhikr(Res.drawable.ic_book_open),
    PortalDiyaa(Res.drawable.ic_wheat),
    PortalFurqan(Res.drawable.ic_scale),
    MushafAndListening(Res.drawable.ic_book_open_text),
    Doors(Res.drawable.ic_door_open),
    Community(Res.drawable.ic_users_round),
    Collections(Res.drawable.ic_bookmark),
    Live(Res.drawable.ic_radio),

    // Mushaf, ayah and study
    Index(Res.drawable.ic_list),
    Search(Res.drawable.ic_search),
    NoResults(Res.drawable.ic_search_x),
    Save(Res.drawable.ic_bookmark),
    Saved(Res.drawable.ic_bookmark_check_filled),
    Share(Res.drawable.ic_share_2),
    Copy(Res.drawable.ic_copy),
    Study(Res.drawable.ic_sparkles),

    // Listening
    Play(Res.drawable.ic_play), // media play never mirrors; only chevrons, arrows and skip do (DESIGN §7)
    Pause(Res.drawable.ic_pause),
    PlayFromHere(Res.drawable.ic_list_start, mirrorInRtl = true),
    Previous(Res.drawable.ic_skip_back, mirrorInRtl = true),
    Next(Res.drawable.ic_skip_forward, mirrorInRtl = true),
    RepeatOne(Res.drawable.ic_repeat_1),
    Reciter(Res.drawable.ic_mic),
    ListenToWord(Res.drawable.ic_volume_2),
    Speed(Res.drawable.ic_gauge),
    SleepTimer(Res.drawable.ic_timer),
    Download(Res.drawable.ic_download),

    // Community
    ShareVerse(Res.drawable.ic_quote),
    Ask(Res.drawable.ic_message_circle_question),
    Reflect(Res.drawable.ic_lightbulb),
    Like(Res.drawable.ic_heart),
    Reply(Res.drawable.ic_message_circle),
    Report(Res.drawable.ic_flag),
    Send(Res.drawable.ic_send, mirrorInRtl = true),
    Photo(Res.drawable.ic_image),

    // Moon and place
    Moonrise(Res.drawable.ic_sunrise),
    Moonset(Res.drawable.ic_sunset),
    Direction(Res.drawable.ic_compass),
    Location(Res.drawable.ic_map_pin),

    // Settings and states
    ThemeLight(Res.drawable.ic_sun),
    ThemeDark(Res.drawable.ic_moon),
    ThemeSystem(Res.drawable.ic_monitor),
    SignOut(Res.drawable.ic_log_out, mirrorInRtl = true),
    Edit(Res.drawable.ic_pencil),
    Delete(Res.drawable.ic_trash_2),
    Add(Res.drawable.ic_plus),
    Remove(Res.drawable.ic_minus),
    Done(Res.drawable.ic_check),
    Success(Res.drawable.ic_circle_check),
    Pending(Res.drawable.ic_clock),
    Warning(Res.drawable.ic_circle_alert),
    Error(Res.drawable.ic_circle_x),
    Info(Res.drawable.ic_info),
    Retry(Res.drawable.ic_refresh_cw),
    Offline(Res.drawable.ic_wifi_off),
}

/**
 * Draws [icon] at [size] in [tint] (DESIGN §7: colour from the text role beside it). An icon-only control must pass an
 * Arabic [contentDescription] (DESIGN §9); a decorative icon passes `null`.
 */
@Composable
fun Icon(icon: AppIcon, contentDescription: String?, tint: Color, size: Dp, modifier: Modifier = Modifier) {
    val mirror = icon.mirrorInRtl && LocalLayoutDirection.current == LayoutDirection.Rtl
    Image(
        painter = painterResource(icon.resource),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier
            .size(size)
            .graphicsLayer { if (mirror) scaleX = -1f }
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            ),
    )
}
