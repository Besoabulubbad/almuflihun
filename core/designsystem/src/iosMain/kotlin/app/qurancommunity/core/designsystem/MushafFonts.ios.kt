package app.qurancommunity.core.designsystem

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import app.qurancommunity.core.model.PageFont

internal actual fun exactPageFontFamily(font: PageFont.Exact): FontFamily = FontFamily(
    Font(identity = "qpc-v2-p${font.page}", data = checkNotNull(font.bytes) { "iOS draws a page font from bytes" }),
)
