package app.qurancommunity.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.qurancommunity.core.designsystem.resources.Res
import app.qurancommunity.core.designsystem.resources.digital_khatt_v2
import app.qurancommunity.core.model.PageFont
import org.jetbrains.compose.resources.Font

/**
 * The font family a Mushaf page is drawn with (ADR 0013, 0016): the page's own QPC font when it is on the device,
 * Digital Khatt otherwise. Pair it with `QuranWord.drawnWith(font)`, which picks the glyph code or the Unicode text.
 */
@Composable
fun rememberMushafFontFamily(font: PageFont): FontFamily {
    val fallback = FontFamily(Font(Res.font.digital_khatt_v2, FontWeight.Normal))
    return when (font) {
        PageFont.Fallback -> fallback
        is PageFont.Exact -> remember(font.page, font.file, font.bytes) { exactPageFontFamily(font) }
    }
}

/** The page's own font as a family; platform-specific because Android draws files and iOS draws bytes. */
internal expect fun exactPageFontFamily(font: PageFont.Exact): FontFamily
