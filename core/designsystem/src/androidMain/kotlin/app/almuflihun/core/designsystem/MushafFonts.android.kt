package app.almuflihun.core.designsystem

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import app.almuflihun.core.model.PageFont
import java.io.File

internal actual fun exactPageFontFamily(font: PageFont.Exact): FontFamily =
    FontFamily(Font(File(checkNotNull(font.file) { "Android draws a page font from its TTF file" })))
