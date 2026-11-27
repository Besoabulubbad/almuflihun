package app.almuflihun.core.designsystem.tokens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * WCAG contrast for every text-on-background pair that DESIGN §4 names, in both themes (DESIGN §9: "Contrast meets
 * WCAG AA at the sizes allowed"). Normal text needs 4.5:1. Pairs used only for large or bold text, or for graphics,
 * need 3:1 and are marked so.
 */
class ContrastTest {

    private data class Pair(val name: String, val text: Color, val background: Color, val minimum: Double)

    private fun pairs(p: AppPalette, theme: String) = listOf(
        Pair("$theme paper.text on paper.bg", p.paper.text, p.paper.bg, NORMAL),
        Pair("$theme paper.textBody on paper.card", p.paper.textBody, p.paper.card, NORMAL),
        Pair("$theme paper.textMuted on paper.bg", p.paper.textMuted, p.paper.bg, NORMAL),
        Pair("$theme paper.textMuted on paper.card", p.paper.textMuted, p.paper.card, NORMAL),
        Pair("$theme night.text on night.bg", p.night.text, p.night.bg, NORMAL),
        Pair("$theme night.textMuted on night.bg", p.night.textMuted, p.night.bg, NORMAL),
        Pair("$theme night.textFaint on night.bg (≥14 only)", p.night.textFaint, p.night.bg, NORMAL),
        Pair("$theme night.text on night.surface", p.night.text, p.night.surface, NORMAL),
        Pair("$theme accent.indigo link on paper.bg", p.accent.indigo, p.paper.bg, NORMAL),
        Pair("$theme button text paper.card on accent.indigo", p.paper.card, p.accent.indigo, NORMAL),
        Pair("$theme Quran text paper.text on quran.highlight", p.paper.text, p.quran.highlight, NORMAL),
        Pair("$theme Quran text paper.text on quran.mushafBg", p.paper.text, p.quran.mushafBg, NORMAL),
        Pair("$theme ref chip quran.brown on quran.refChipBg", p.quran.brown, p.quran.refChipBg, NORMAL),
        Pair("$theme live badge status.liveText on status.liveBg", p.status.liveText, p.status.liveBg, NORMAL),
    )

    @Test
    fun `every named pair meets WCAG AA in both themes`() {
        val all = pairs(LightPalette, "light") + pairs(DarkPalette, "dark")
        val report = all.map { it to ratio(it.text, it.background) }
        val failures = report.filter { (pair, value) -> value < pair.minimum }
        assertTrue(
            failures.isEmpty(),
            "Below WCAG AA:\n" + failures.joinToString("\n") { (pair, value) ->
                "  ${pair.name}: ${round(value)}:1 (needs ${pair.minimum}:1)"
            },
        )
    }

    private fun ratio(a: Color, b: Color): Double {
        val la = a.luminance() + 0.05
        val lb = b.luminance() + 0.05
        return maxOf(la, lb) / minOf(la, lb)
    }

    private fun round(value: Double) = (value * 100).toInt() / 100.0

    private companion object {
        const val NORMAL = 4.5
    }
}
