package app.almuflihun.core.localization

import kotlin.test.Test
import kotlin.test.assertEquals

/** The formats DESIGN §2 and §8 name, in Arabic, and their English counterparts (ADR 0014). */
class AppFormatterTest {

    private val ar = AppFormatter(AppLanguage.Arabic)
    private val en = AppFormatter(AppLanguage.English)

    @Test
    fun `digits are Arabic-Indic in Arabic and Western in English`() {
        assertEquals("١٧٢", ar.digits(172))
        assertEquals("٠", ar.digits(0))
        assertEquals("172", en.digits(172))
    }

    @Test
    fun `numbers group thousands`() {
        assertEquals("١٢٬٣٤٥", ar.number(12_345))
        assertEquals("٣٥٦٬٧٣٨", ar.number(356_738))
        assertEquals("999", en.number(999))
        assertEquals("1,000,000", en.number(1_000_000))
        assertEquals("-1,200", en.number(-1_200))
    }

    @Test
    fun `ranges use an en dash with no spaces`() {
        assertEquals("١٤٣–١٤٤", ar.range(143, 144))
        assertEquals("143–144", en.range(143, 144))
    }

    @Test
    fun `parts are separated by the Arabic comma and never a middle dot`() {
        assertEquals("تلاوة، يوتيوب", ar.parts("تلاوة", "يوتيوب"))
        assertEquals("Recitation, YouTube", en.parts("Recitation", "YouTube"))
    }

    @Test
    fun `verse references`() {
        assertEquals("البقرة ١٧٢", ar.reference("البقرة", 172))
        assertEquals("البقرة ١٤٣–١٤٤", ar.reference("البقرة", 143, 144))
        assertEquals("البقرة ١٧٢", ar.reference("البقرة", 172, 172))
        assertEquals("سورة البقرة ١٧٢", ar.fullReference("البقرة", 172))
        assertEquals("Al-Baqarah 172", en.fullReference("Al-Baqarah", 172))
    }

    @Test
    fun `percent uses the Arabic percent sign in Arabic`() {
        assertEquals("٩٥٪", ar.percent(95))
        assertEquals("95%", en.percent(95))
    }

    @Test
    fun `isolate wraps text in first-strong isolates`() {
        assertEquals("⁨Quran Foundation⁩", ar.isolate("Quran Foundation"))
    }
}
