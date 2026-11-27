package app.almuflihun.core.designsystem.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.almuflihun.core.designsystem.AppTheme
import app.almuflihun.core.designsystem.icon.AppIcon
import app.almuflihun.core.designsystem.icon.Icon
import app.almuflihun.core.designsystem.tokens.Border
import app.almuflihun.core.designsystem.tokens.Radius
import app.almuflihun.core.designsystem.tokens.Size
import app.almuflihun.core.designsystem.tokens.Space
import app.almuflihun.core.testing.SnapshotCase

/** The design system on one page: every colour role, every text role and every icon, in the current variant. */
val designSystemSnapshots = listOf(
    SnapshotCase("tokenSheet_colors") { ColorSheet() },
    SnapshotCase("tokenSheet_text") { TextSheet() },
    SnapshotCase("tokenSheet_icons", fullMatrix = true) { IconSheet() },
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSheet() {
    val p = AppTheme.colors
    val groups = listOf(
        "night" to listOf(
            p.night.bg,
            p.night.surface,
            p.night.surfaceRaised,
            p.night.line,
            p.night.text,
            p.night.accent,
        ),
        "paper" to listOf(p.paper.bg, p.paper.card, p.paper.sunk, p.paper.line, p.paper.text, p.paper.textMuted),
        "accent" to listOf(p.accent.indigo, p.accent.indigoPressed, p.accent.indigoTint, p.accent.indigoSelect),
        "quran" to listOf(p.quran.highlight, p.quran.mushafBg, p.quran.gold, p.quran.brown, p.quran.refChipBg),
        "status" to listOf(p.status.liveBg, p.status.liveText, p.status.danger),
        "diyaa" to listOf(p.diyaa.green, p.diyaa.leaf, p.diyaa.honey, p.diyaa.sea, p.diyaa.pomegranate, p.diyaa.sand),
    )
    Column(Modifier.padding(Space.s16), verticalArrangement = Arrangement.spacedBy(Space.s12)) {
        groups.forEach { (name, colors) ->
            BasicText(name, style = AppTheme.text.label.copy(color = p.paper.text))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.s8)) {
                colors.forEach { Swatch(it) }
            }
        }
    }
}

@Composable
private fun Swatch(color: Color) = Box(
    Modifier
        .size(Size.touch)
        .background(color, RoundedCornerShape(Radius.r8))
        .border(Border.hairline, AppTheme.colors.paper.line, RoundedCornerShape(Radius.r8)),
)

@Composable
private fun TextSheet() {
    val t = AppTheme.text
    val ink = AppTheme.colors.paper.text
    val ui = listOf(
        t.display, t.titleXL, t.titleL, t.titleM, t.titleS, t.subhead, t.bodyL, t.body, t.bodyStrong, t.label,
        t.meta, t.caption, t.captionStrong,
    )
    val quran = listOf(t.quranWord, t.quranHero, t.quranBody, t.quranInline, t.quranSmall)
    Column(Modifier.padding(Space.s16), verticalArrangement = Arrangement.spacedBy(Space.s4)) {
        ui.forEach { BasicText("المفلحون ١٢٣", style = it.copy(color = ink)) }
        quran.forEach { BasicText("ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ", style = it.copy(color = ink)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconSheet() {
    FlowRow(
        Modifier.padding(Space.s16),
        horizontalArrangement = Arrangement.spacedBy(Space.s12),
        verticalArrangement = Arrangement.spacedBy(Space.s12),
    ) {
        AppIcon.entries.forEach {
            Icon(it, contentDescription = null, tint = AppTheme.colors.paper.text, size = Size.iconL)
        }
    }
}
