// Generated from tokens.json v7. Do not edit by hand; regenerate from tokens.json.
package app.qurancommunity.design

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Immutable
data class AppColors(
    val bg: Color,
    val surface: Color,
    val surfaceSunk: Color,
    val mushafBg: Color,
    val line: Color,
    val lineStrong: Color,
    val text: Color,
    val textMuted: Color,
    val textFaint: Color,
    val iconQuiet: Color,
    val accent: Color,
    val accentTint: Color,
    val onAccent: Color,
    val onAccentMuted: Color,
    val selection: Color,
    val reflectTint: Color,
    val onReflectTint: Color,
    val noteBg: Color,
    val noteText: Color,
    val danger: Color,
    val live: Color,
    val inverse: Color,
    val onInverse: Color,
    val onInverseMuted: Color,
    val inverseTrack: Color,
    val scrim: Color,
    val formAlt: Color,
    val formAltTint: Color,
    val accentRing: Color,
    val accentSoft: Color,
)

val LightColors = AppColors(
    bg = Color(0xFFF5F2EC),
    surface = Color(0xFFFFFFFF),
    surfaceSunk = Color(0xFFECE8E0),
    mushafBg = Color(0xFFFBF8F1),
    line = Color(0xFFE4DFD5),
    lineStrong = Color(0xFFD9D3C7),
    text = Color(0xFF1D1C1A),
    textMuted = Color(0xFF57534C),
    textFaint = Color(0xFF6B665D),
    iconQuiet = Color(0xFF8A857B),
    accent = Color(0xFF2F4A7A),
    accentTint = Color(0xFFE3E8F2),
    onAccent = Color(0xFFFFFFFF),
    onAccentMuted = Color(0xFFC9D4EA),
    selection = Color(0xFFF3E6BF),
    reflectTint = Color(0xFFF3E6BF),
    onReflectTint = Color(0xFF6B4E0F),
    noteBg = Color(0xFFFFF7E6),
    noteText = Color(0xFF7A5212),
    danger = Color(0xFFA3332B),
    live = Color(0xFFC2410C),
    inverse = Color(0xFF1D1C1A),
    onInverse = Color(0xFFF5F2EC),
    onInverseMuted = Color(0xFFB5B0A6),
    inverseTrack = Color(0xFF4A4740),
    scrim = Color(0x661D1C1A),
    formAlt = Color(0xFF8A4B0F),
    formAltTint = Color(0xFFF6E9D8),
    accentRing = Color(0xFF4A6496),
    accentSoft = Color(0xFF9DB4DE),
)

val DarkColors = AppColors(
    bg = Color(0xFF121417),
    surface = Color(0xFF1C1F24),
    surfaceSunk = Color(0xFF23272D),
    mushafBg = Color(0xFF15161A),
    line = Color(0xFF2A2E35),
    lineStrong = Color(0xFF3A3D44),
    text = Color(0xFFECE8E0),
    textMuted = Color(0xFFA9A49A),
    textFaint = Color(0xFF959086),
    iconQuiet = Color(0xFF7F7A71),
    accent = Color(0xFF9DB4DE),
    accentTint = Color(0xFF24324A),
    onAccent = Color(0xFF121417),
    onAccentMuted = Color(0xFF24324A),
    selection = Color(0xFF4A3D1A),
    reflectTint = Color(0xFF3D3218),
    onReflectTint = Color(0xFFE8CF8F),
    noteBg = Color(0xFF2E2716),
    noteText = Color(0xFFE8CF8F),
    danger = Color(0xFFF08A80),
    live = Color(0xFFFB8A5C),
    inverse = Color(0xFFECE8E0),
    onInverse = Color(0xFF121417),
    onInverseMuted = Color(0xFF57534C),
    inverseTrack = Color(0xFFB5B0A6),
    scrim = Color(0x99000000),
    formAlt = Color(0xFFE8A86B),
    formAltTint = Color(0xFF3A2A18),
    accentRing = Color(0xFF3A4D6E),
    accentSoft = Color(0xFF3A4D6E),
)

/** The Moon page always uses DarkColors plus these. */
object MoonColors { val lit = Color(0xFFF1E9D2); val shadow = Color(0xFF2B2A27) }
object MushafColors { val pageLight = Color(0xFFFBF8F1) }

object Space { val s0 = 0.dp; val s2 = 2.dp; val s4 = 4.dp; val s8 = 8.dp; val s12 = 12.dp; val s16 = 16.dp; val s20 = 20.dp; val s24 = 24.dp; val s32 = 32.dp; val s40 = 40.dp }
object Layout { val screenPadding = 16.dp; val cardPadding = 12.dp; val cardGap = 8.dp; val sectionGap = 16.dp; val rowPaddingV = 12.dp; val rowPaddingH = 12.dp; val sheetPadding = 16.dp }
object Radius { val s = 8.dp; val m = 12.dp; val l = 16.dp; val xl = 24.dp } // pill = CircleShape
object Size {
    val touchMin = 48.dp; val rowMin = 48.dp; val settingsRowMin = 56.dp; val topBar = 56.dp; val searchField = 48.dp
    val button = 44.dp; val chip = 32.dp; val iconS = 16.dp; val iconM = 20.dp; val iconL = 24.dp
    val tabBar = 64.dp; val tabIndicatorW = 56.dp; val tabIndicatorH = 28.dp; val miniPlayer = 48.dp; val miniPlayButton = 32.dp
    val tilawaRing = 56.dp; val fab = 44.dp; val artwork = 56.dp; val widgetRadius = 24.dp; val widgetPadding = 12.dp
    val avatar = 32.dp; val avatarL = 44.dp; val thumbW = 80.dp; val thumbH = 45.dp; val progress = 3.dp
    val switchW = 44.dp; val switchH = 26.dp; val moonRow = 44.dp; val moonHero = 132.dp; val grab = 36.dp; val hairline = 1.dp
}
object Motion { const val fast = 120; const val standard = 200; const val sheet = 280; const val emphasised = 320 }

class AppFonts(val ui: FontFamily, val quranCard: FontFamily)

class AppType(f: AppFonts) {
    private fun ui(size: Int, w: Int, lh: Double) = TextStyle(fontFamily = f.ui, fontSize = size.sp, fontWeight = FontWeight(w), lineHeight = lh.em)
    private fun q(size: Int) = TextStyle(fontFamily = f.quranCard, fontSize = size.sp, lineHeight = 2.15.em)
    val titleL = ui(22, 500, 1.4); val title = ui(18, 500, 1.4); val subhead = ui(15, 500, 1.5)
    val body = ui(15, 400, 1.6); val bodyS = ui(14, 400, 1.6); val label = ui(13, 500, 1.4)
    val meta = ui(12, 400, 1.5); val caption = ui(11, 400, 1.4); val verseNumber = ui(13, 400, 1.4)
    val quranHero = q(20); val quranCard = q(18); val quranAttach = q(17)
}

val LocalAppColors = staticCompositionLocalOf { LightColors }
