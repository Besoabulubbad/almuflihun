package app.almuflihun.core.localization

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.LayoutDirection

/**
 * The app's UI languages (ADR 0014). Arabic is the only one offered today; English is planned. Direction, digits and
 * resources follow the language, never a hard-coded value, so adding English is configuration rather than a rewrite.
 * Quran text is always Arabic and right to left, whatever the UI language.
 */
enum class AppLanguage(val tag: String, val direction: LayoutDirection) {
    Arabic("ar", LayoutDirection.Rtl),
    English("en", LayoutDirection.Ltr),
    ;

    companion object {
        /** Languages the user can choose today. English joins when its copy, content and design exist. */
        val offered: List<AppLanguage> = listOf(Arabic)
    }
}

/** The current UI language. Provided at the root of the app; Arabic until English is offered. */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.Arabic }
