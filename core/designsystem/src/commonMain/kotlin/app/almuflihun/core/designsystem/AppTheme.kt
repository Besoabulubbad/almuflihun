package app.almuflihun.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.almuflihun.core.designsystem.resources.Res
import app.almuflihun.core.designsystem.resources.digital_khatt_v2
import app.almuflihun.core.designsystem.resources.ibm_plex_sans_arabic_bold
import app.almuflihun.core.designsystem.resources.ibm_plex_sans_arabic_regular
import app.almuflihun.core.designsystem.resources.ibm_plex_sans_arabic_semibold
import app.almuflihun.core.designsystem.tokens.AppPalette
import app.almuflihun.core.designsystem.tokens.AppTextStyles
import app.almuflihun.core.designsystem.tokens.DarkPalette
import app.almuflihun.core.designsystem.tokens.LightPalette
import app.almuflihun.core.designsystem.tokens.appTextStyles
import app.almuflihun.core.localization.AppLanguage
import app.almuflihun.core.localization.LocalAppLanguage
import org.jetbrains.compose.resources.Font

/** The three appearance choices in Settings → المظهر; the default follows the phone (DESIGN, theme rules). */
enum class ThemeMode { Light, Dark, System }

/** The current palette. Provided by [AppTheme]; read through [AppTheme.colors]. */
val LocalAppPalette = staticCompositionLocalOf { LightPalette }

/** The current text roles. Provided by [AppTheme]; read through [AppTheme.text]. */
val LocalAppTextStyles = staticCompositionLocalOf<AppTextStyles> {
    error("No AppTextStyles: wrap the content in AppTheme")
}

/**
 * The app's theme: the palette for [mode], the 19 text roles on the bundled fonts, and the layout direction of
 * [language] (DESIGN §2; Arabic, right to left, today; ADR 0014). Material is not exposed: screens use the
 * design-system components.
 */
@Composable
fun AppTheme(
    mode: ThemeMode = ThemeMode.System,
    language: AppLanguage = LocalAppLanguage.current,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }
    val ui = FontFamily(
        Font(Res.font.ibm_plex_sans_arabic_regular, FontWeight.Normal),
        Font(Res.font.ibm_plex_sans_arabic_semibold, FontWeight.SemiBold),
        Font(Res.font.ibm_plex_sans_arabic_bold, FontWeight.Bold),
    )
    val quran = FontFamily(Font(Res.font.digital_khatt_v2, FontWeight.Normal))
    val styles = remember(ui, quran) { appTextStyles(ui, quran) }
    CompositionLocalProvider(
        LocalAppPalette provides if (dark) DarkPalette else LightPalette,
        LocalAppTextStyles provides styles,
        LocalAppLanguage provides language,
        LocalLayoutDirection provides language.direction,
        content = content,
    )
}

/** Read the current theme values: `AppTheme.colors.paper.bg`, `AppTheme.text.body`. */
object AppTheme {
    val colors: AppPalette
        @Composable @ReadOnlyComposable
        get() = LocalAppPalette.current

    val text: AppTextStyles
        @Composable @ReadOnlyComposable
        get() = LocalAppTextStyles.current
}
