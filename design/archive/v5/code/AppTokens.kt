// Generated from tokens.json. Do not edit by hand: change tokens.json and regenerate.
package ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

object Space {
    val s0 = 0.dp
    val s2 = 2.dp
    val s4 = 4.dp
    val s8 = 8.dp
    val s12 = 12.dp
    val s16 = 16.dp
    val s20 = 20.dp
    val s24 = 24.dp
    val s32 = 32.dp
    val s40 = 40.dp
    val s48 = 48.dp
}

object Radius {
    val r4 = 4.dp
    val r8 = 8.dp
    val r12 = 12.dp
    val r16 = 16.dp
    val r24 = 24.dp
    val pill = 999.dp
}

object Size {
    val touch = 44.dp
    val iconL = 24.dp
    val iconM = 20.dp
    val badge = 32.dp
    val dot = 4.dp
    val dotToday = 8.dp
    val progress = 4.dp
    val progressThick = 8.dp
    val button = 48.dp
    val buttonNav = 64.dp
    val chip = 24.dp
    val illustrationS = 40.dp
    val illustrationM = 56.dp
    val illustrationL = 88.dp
    val illustrationTile = 112.dp
    val portalMin = 112.dp
    val tileMin = 112.dp
    val liveCardWidth = 168.dp
    val screenWidth = 390.dp
    val traitMin = 128.dp
    val bar = 24.dp
    val chartMax = 112.dp
    val nameColumn = 64.dp
    val grabWidth = 40.dp
    val playerBar = 64.dp
    val frameOuter = 2.dp
    val frameInner = 1.dp
}

object Border {
    val hairline = 1.dp
    val accentEdge = 4.dp
    val selection = 2.dp
}

/** One palette per theme. Night colours are the same in both. */
class AppPalette(
    val nightBg: Color,
    val nightSurface: Color,
    val nightSurfaceRaised: Color,
    val nightLine: Color,
    val nightLineSoft: Color,
    val nightText: Color,
    val nightTextMuted: Color,
    val nightTextFaint: Color,
    val nightAccent: Color,
    val nightMoon: Color,
    val paperBg: Color,
    val paperCard: Color,
    val paperSunk: Color,
    val paperLine: Color,
    val paperLineDashed: Color,
    val paperText: Color,
    val paperTextBody: Color,
    val paperTextMuted: Color,
    val accentIndigo: Color,
    val accentIndigoPressed: Color,
    val accentIndigoTint: Color,
    val accentIndigoSelect: Color,
    val quranHighlight: Color,
    val quranMushafBg: Color,
    val quranGold: Color,
    val quranBrown: Color,
    val quranRefChipBg: Color,
    val quranCardBorder: Color,
    val quranPlayed: Color,
    val statusLiveBg: Color,
    val statusLiveText: Color,
    val statusDanger: Color,
    val diyaaGreen: Color,
    val diyaaGreenOnDark: Color,
    val diyaaLeaf: Color,
    val diyaaDate: Color,
    val diyaaHoney: Color,
    val diyaaSea: Color,
    val diyaaSeaTint: Color,
    val diyaaPomegranate: Color,
    val diyaaSand: Color,
    val diyaaHeroTop: Color,
    val diyaaHeroMid: Color,
    val diyaaProgressOff: Color,
    val diyaaTintGreen: Color,
    val diyaaTintWheat: Color,
    val diyaaTintSky: Color,
    val diyaaTintRose: Color,
    val diyaaTintHoney: Color,
    val diyaaTintStone: Color,
    val diyaaPairCard: Color,
    val diyaaNeutralCard: Color,
)

val LightPalette = AppPalette(
    nightBg = Color(0xFF141B2D),
    nightSurface = Color(0xFF1D263C),
    nightSurfaceRaised = Color(0xFF222B48),
    nightLine = Color(0xFF33405C),
    nightLineSoft = Color(0xFF4A5675),
    nightText = Color(0xFFEDE9E1),
    nightTextMuted = Color(0xFFB9C3D6),
    nightTextFaint = Color(0xFF8C98B3),
    nightAccent = Color(0xFFA9BEE6),
    nightMoon = Color(0xFFF1ECE1),
    paperBg = Color(0xFFF5F2EC),
    paperCard = Color(0xFFFFFFFF),
    paperSunk = Color(0xFFEDE8DE),
    paperLine = Color(0xFFE2DCD0),
    paperLineDashed = Color(0xFFC8C1B4),
    paperText = Color(0xFF1D1C1A),
    paperTextBody = Color(0xFF34312C),
    paperTextMuted = Color(0xFF57534C),
    accentIndigo = Color(0xFF2F4A7A),
    accentIndigoPressed = Color(0xFF1E3258),
    accentIndigoTint = Color(0xFFEEF1F7),
    accentIndigoSelect = Color(0xFFE6EBF4),
    quranHighlight = Color(0xFFFFEFC2),
    quranMushafBg = Color(0xFFFBF7EE),
    quranGold = Color(0xFFC9A24A),
    quranBrown = Color(0xFF8A5A14),
    quranRefChipBg = Color(0xFFF3E7CC),
    quranCardBorder = Color(0xFFE6D9BC),
    quranPlayed = Color(0xFF8E8A82),
    statusLiveBg = Color(0xFFE0B06A),
    statusLiveText = Color(0xFF2B1D08),
    statusDanger = Color(0xFFA8443A),
    diyaaGreen = Color(0xFF4F6B2E),
    diyaaGreenOnDark = Color(0xFFCFE0A8),
    diyaaLeaf = Color(0xFF7C9A45),
    diyaaDate = Color(0xFF8A5A14),
    diyaaHoney = Color(0xFFD9A63A),
    diyaaSea = Color(0xFF2F6A8A),
    diyaaSeaTint = Color(0xFFDCEBF2),
    diyaaPomegranate = Color(0xFFA8443A),
    diyaaSand = Color(0xFFE8D6B0),
    diyaaHeroTop = Color(0xFFF3DDB0),
    diyaaHeroMid = Color(0xFFF7EAD0),
    diyaaProgressOff = Color(0xFFD8CFBE),
    diyaaTintGreen = Color(0xFFEEF3E2),
    diyaaTintWheat = Color(0xFFF6EEDC),
    diyaaTintSky = Color(0xFFEAF0F5),
    diyaaTintRose = Color(0xFFF7EBE8),
    diyaaTintHoney = Color(0xFFF8EDD0),
    diyaaTintStone = Color(0xFFEDE8DE),
    diyaaPairCard = Color(0xFFF7F3EA),
    diyaaNeutralCard = Color(0xFFF3EFEA),
)

val DarkPalette = AppPalette(
    nightBg = Color(0xFF141B2D),
    nightSurface = Color(0xFF1D263C),
    nightSurfaceRaised = Color(0xFF222B48),
    nightLine = Color(0xFF33405C),
    nightLineSoft = Color(0xFF4A5675),
    nightText = Color(0xFFEDE9E1),
    nightTextMuted = Color(0xFFB9C3D6),
    nightTextFaint = Color(0xFF8C98B3),
    nightAccent = Color(0xFFA9BEE6),
    nightMoon = Color(0xFFF1ECE1),
    paperBg = Color(0xFF0F131C),
    paperCard = Color(0xFF1A2030),
    paperSunk = Color(0xFF232A3A),
    paperLine = Color(0xFF2C3445),
    paperLineDashed = Color(0xFF4A5468),
    paperText = Color(0xFFEDE9E1),
    paperTextBody = Color(0xFFD8D3C9),
    paperTextMuted = Color(0xFFA7A29A),
    accentIndigo = Color(0xFFA9BEE6),
    accentIndigoPressed = Color(0xFFC9D6F0),
    accentIndigoTint = Color(0xFF1E2740),
    accentIndigoSelect = Color(0xFF243150),
    quranHighlight = Color(0xFF4A3D1A),
    quranMushafBg = Color(0xFF16140F),
    quranGold = Color(0xFFC9A24A),
    quranBrown = Color(0xFFD9A85A),
    quranRefChipBg = Color(0xFF2E2715),
    quranCardBorder = Color(0xFF3A3220),
    quranPlayed = Color(0xFF6E6A63),
    statusLiveBg = Color(0xFFE0B06A),
    statusLiveText = Color(0xFF2B1D08),
    statusDanger = Color(0xFFD9705F),
    diyaaGreen = Color(0xFF6E8F3E),
    diyaaGreenOnDark = Color(0xFFCFE0A8),
    diyaaLeaf = Color(0xFF7C9A45),
    diyaaDate = Color(0xFFD9A85A),
    diyaaHoney = Color(0xFFD9A63A),
    diyaaSea = Color(0xFF7FB0C9),
    diyaaSeaTint = Color(0xFF1B2C38),
    diyaaPomegranate = Color(0xFFD9705F),
    diyaaSand = Color(0xFFE8D6B0),
    diyaaHeroTop = Color(0xFF2A2416),
    diyaaHeroMid = Color(0xFF1E1C16),
    diyaaProgressOff = Color(0xFF3A3F4A),
    diyaaTintGreen = Color(0xFF1F2A18),
    diyaaTintWheat = Color(0xFF2A2416),
    diyaaTintSky = Color(0xFF1A2633),
    diyaaTintRose = Color(0xFF2E1E1C),
    diyaaTintHoney = Color(0xFF2E2712),
    diyaaTintStone = Color(0xFF232A3A),
    diyaaPairCard = Color(0xFF232A3A),
    diyaaNeutralCard = Color(0xFF262B36),
)

val LocalAppPalette = staticCompositionLocalOf { LightPalette }

/** Bind to the bundled fonts at startup (see DESIGN.md §3). */
object AppFonts {
    lateinit var ui: FontFamily
    lateinit var quran: FontFamily
}

object AppText {
    val display: TextStyle get() = TextStyle(fontFamily = AppFonts.ui, fontSize = 56.sp, fontWeight = FontWeight.Bold, lineHeight = 1.3.em)
    val titleXL: TextStyle get() = TextStyle(fontFamily = AppFonts.ui, fontSize = 36.sp, fontWeight = FontWeight.Bold, lineHeight = 1.3.em)
    val titleL: TextStyle get() = TextStyle(fontFamily = AppFonts.ui, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 1.3.em)
    val titleM: TextStyle get() = TextStyle(fontFamily = AppFonts.ui, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 1.3.em)
    val titleS: TextStyle get() = TextStyle(fontFamily = AppFonts.ui, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 1.6.em)
    val subhead: TextStyle get() = TextStyle(fontFamily = AppFonts.ui, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 1.6.em)
    val bodyL: TextStyle get() = TextStyle(fontFamily = AppFonts.ui, fontSize = 18.sp, fontWeight = FontWeight.Normal, lineHeight = 1.9.em)
    val body: TextStyle get() = TextStyle(fontFamily = AppFonts.ui, fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 1.9.em)
    val bodyStrong: TextStyle get() = TextStyle(fontFamily = AppFonts.ui, fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 1.9.em)
    val label: TextStyle get() = TextStyle(fontFamily = AppFonts.ui, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 1.6.em)
    val meta: TextStyle get() = TextStyle(fontFamily = AppFonts.ui, fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 1.6.em)
    val caption: TextStyle get() = TextStyle(fontFamily = AppFonts.ui, fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 1.6.em)
    val captionStrong: TextStyle get() = TextStyle(fontFamily = AppFonts.ui, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 1.6.em)
    val quranWord: TextStyle get() = TextStyle(fontFamily = AppFonts.quran, fontSize = 30.sp, fontWeight = FontWeight.Normal, lineHeight = 1.8.em)
    val quranHero: TextStyle get() = TextStyle(fontFamily = AppFonts.quran, fontSize = 26.sp, fontWeight = FontWeight.Normal, lineHeight = 2.15.em)
    val quranBody: TextStyle get() = TextStyle(fontFamily = AppFonts.quran, fontSize = 22.sp, fontWeight = FontWeight.Normal, lineHeight = 2.15.em)
    val quranInline: TextStyle get() = TextStyle(fontFamily = AppFonts.quran, fontSize = 18.sp, fontWeight = FontWeight.Normal, lineHeight = 1.8.em)
    val quranSmall: TextStyle get() = TextStyle(fontFamily = AppFonts.quran, fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 1.8.em)
}

/** Durations in ms and easings. See DESIGN.md §11. */
object Motion {
    const val instant = 0
    const val fast = 120
    const val standard = 240
    const val page = 320
    const val emphasised = 400
    const val sheetExit = 200
    const val highlightHold = 1600
    const val stagger = 40
    const val skeletonDelay = 300
    const val hizbEndDelay = 600
    val standardEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val decelerateEasing = CubicBezierEasing(0f, 0f, 0f, 1f)
    val accelerateEasing = CubicBezierEasing(0.3f, 0f, 1f, 1f)
    val emphasisedEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    const val pressScale = 0.98f
}
