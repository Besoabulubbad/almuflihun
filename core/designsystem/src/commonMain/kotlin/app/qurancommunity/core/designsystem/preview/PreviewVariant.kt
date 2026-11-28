package app.qurancommunity.core.designsystem.preview

import androidx.compose.runtime.Immutable
import app.qurancommunity.core.designsystem.ThemeMode
import app.qurancommunity.core.localization.AppLanguage

/**
 * One cell of the screenshot and preview matrix (docs/guides/quality-bar.md): language × theme × font scale × width.
 * Arabic only today; English variants join when English is offered (ADR 0014).
 */
@Immutable
data class PreviewVariant(
    val language: AppLanguage,
    val theme: ThemeMode,
    val fontScale: Float = 1f,
    val widthDp: Int = WIDTH_DESIGN,
) {
    /** Stable file-name fragment, e.g. `ar_dark_f130_w320`. */
    val id: String
        get() = buildString {
            append(language.tag)
            append(if (theme == ThemeMode.Dark) "_dark" else "_light")
            append("_f").append((fontScale * PERCENT).toInt())
            append("_w").append(widthDp)
        }

    /** The [id], so parameterised test names read `tokenSheet[ar_light_f100_w390]`. */
    override fun toString(): String = id

    companion object {
        /** The design width (`size.screenWidth`). */
        const val WIDTH_DESIGN = 390
        const val WIDTH_NARROW = 320

        /** DESIGN §9: layouts must not clip at 130 %. */
        const val FONT_SCALE_LARGE = 1.3f
        private const val PERCENT = 100
    }
}

/** The named sets of variants that tests and previews iterate. */
object PreviewVariants {
    val Light = PreviewVariant(AppLanguage.Arabic, ThemeMode.Light)
    val Dark = PreviewVariant(AppLanguage.Arabic, ThemeMode.Dark)

    /** Every case: light and dark, at 100 % and 130 %, at the design width. */
    val Four: List<PreviewVariant> = listOf(Light, Dark).flatMap { base ->
        listOf(base, base.copy(fontScale = PreviewVariant.FONT_SCALE_LARGE))
    }

    /** A component's or screen's default state also runs at 320 dp: 8 cells. */
    val Matrix: List<PreviewVariant> = Four.flatMap { listOf(it, it.copy(widthDp = PreviewVariant.WIDTH_NARROW)) }
}
