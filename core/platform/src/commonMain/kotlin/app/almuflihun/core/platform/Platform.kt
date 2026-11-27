package app.almuflihun.core.platform

import org.koin.core.module.Module

/**
 * Opens the system share sheet with plain text. The app shares a verse as its full text then its reference, nothing
 * else (docs/specs/screens.md §6; DECISIONS #26).
 */
interface ShareSheet {
    fun shareText(text: String)
}

/**
 * Whether the user asked the system for less motion (Android: animations off; iOS: Reduce Motion). DESIGN §11: every
 * slide, transform and shared element becomes a cross-fade; highlights show and hide without fading.
 */
interface MotionSettings {
    val reduceMotion: Boolean
}

/**
 * The platform adapters, for Koin. Haptics and opening links are not here: Compose's `LocalHapticFeedback` and
 * `LocalUriHandler` already work on both platforms (a TikTok link opens the app when installed, else the browser).
 */
expect val platformModule: Module
