package app.qurancommunity.buildlogic.tokens

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** The generator against the real design/tokens.json, and against broken inputs it must refuse. */
class DesignTokenCodegenTest {

    private val realTokens = File(System.getProperty("qurancommunity.tokensFile")).readText()
    private val files = DesignTokenCodegen.generate(realTokens).associate { it.name to it.content }

    @Test
    fun `generates the four token files`() {
        assertEquals(setOf("Dimensions.kt", "Palette.kt", "TextRoles.kt", "Motion.kt"), files.keys)
    }

    @Test
    fun `dimensions come straight from the token file`() {
        val dims = files.getValue("Dimensions.kt")
        assertTrue("val s16: Dp = 16.dp" in dims, dims)
        assertTrue("val r24: Dp = 24.dp" in dims)
        assertTrue("val touch: Dp = 44.dp" in dims)
        assertTrue("val hairline: Dp = 1.dp" in dims)
    }

    @Test
    fun `both themes produce a palette with the same roles`() {
        val palette = files.getValue("Palette.kt")
        assertTrue("val LightPalette: AppPalette" in palette)
        assertTrue("val DarkPalette: AppPalette" in palette)
        assertTrue("bg = Color(0xFF141B2D)," in palette, "night.bg light") // night looks the same in both modes
        assertTrue("indigo = Color(0xFF2F4A7A)," in palette)
    }

    @Test
    fun `text roles keep font, size, weight and line height`() {
        val text = files.getValue("TextRoles.kt")
        assertTrue("val body: TextRoleSpec = TextRoleSpec(FontRole.Ui, 16f, 400, 1.9f)" in text, text)
        assertTrue("val quranBody: TextRoleSpec = TextRoleSpec(FontRole.Quran, 22f, 400, 2.15f)" in text)
        assertTrue("    quranBody = TextRoles.quranBody.style(ui, quran)," in text, "every role gets a ready style")
    }

    @Test
    fun `motion has durations and easings`() {
        val motion = files.getValue("Motion.kt")
        assertTrue("const val page: Int = 320" in motion, motion)
        assertTrue("val standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)" in motion)
        assertTrue("const val cardPressScale: Float = 0.98f" in motion)
    }

    @Test
    fun `refuses a colour that is not hex`() {
        val broken = realTokens.replace("\"#141B2D\"", "\"navy\"")
        assertFailsWith<IllegalArgumentException> { DesignTokenCodegen.generate(broken) }
    }

    @Test
    fun `refuses a weight the design does not allow`() {
        val broken = realTokens.replaceFirst("\"weight\": 400", "\"weight\": 500")
        assertFailsWith<IllegalArgumentException> { DesignTokenCodegen.generate(broken) }
    }
}
