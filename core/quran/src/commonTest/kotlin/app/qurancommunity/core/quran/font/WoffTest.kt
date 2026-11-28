package app.qurancommunity.core.quran.font

import app.qurancommunity.core.common.AppError
import app.qurancommunity.core.common.AppResult
import app.qurancommunity.core.testing.runAppTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WoffTest {

    private val codes = listOf(0x627, 0xFC41, 0xFE70, 0x1F600)
    private val tables = TestFonts.tables(codes)

    @Test
    fun a_woff_decodes_to_its_tables_and_a_font_that_reads_back() {
        val font = Woff.decode(TestFonts.woff(tables))
        assertEquals(tables.keys, font.tables.keys)
        tables.forEach { (tag, table) -> assertContentEquals(table, font.tables.getValue(tag), tag) }
        assertEquals(0x00010000L, font.bytes.u32(0))
        assertEquals(tables.size, font.bytes.u16(4))
        assertEquals("cmap", font.bytes.tag(12))
    }

    @Test
    fun damage_of_any_kind_is_refused() {
        val good = TestFonts.woff(tables)
        val glyfData = good.u32(44 + 20 + 4).toInt() // second entry is 'glyf', stored compressed
        val refusals = listOf(
            good.copyOf(good.size - 4), // truncated
            TestFonts.woff(tables) { it[0] = 0 }, // not WOFF
            TestFonts.woff(tables) { it[glyfData + 10] = (it[glyfData + 10] + 1).toByte() }, // bytes changed
            TestFonts.woff(tables) { it[44 + 20 + 16] = (it[44 + 20 + 16] + 1).toByte() }, // checksum changed
            TestFonts.woff(tables) { b -> "zzzz".forEachIndexed { j, c -> b[44 + j] = c.code.toByte() } }, // order
        )
        refusals.forEachIndexed { i, bytes -> assertFailsWith<InvalidFontException>("case $i") { Woff.decode(bytes) } }
    }

    @Test
    fun the_character_map_reads_both_formats() {
        val cmap = tables.getValue("cmap")
        assertEquals(codes.toSet(), Cmap.mapped(cmap))
        assertEquals(setOf(0x628), Cmap.missing(cmap, setOf(0x627, 0x628)))
    }

    @Test
    fun the_store_keeps_only_fonts_that_draw_their_page() = runAppTest {
        val files = MemoryFontFiles()
        val store = PageFontStore(files) { page -> if (page == 2) setOf(0x628) else setOf(0xFC41) }
        val woff = TestFonts.woff(tables)

        assertEquals(AppResult.Success(Unit), store.save(1, woff))
        assertTrue(store.has(1))
        assertContentEquals(woff, store.read(1))

        val missingGlyph = store.save(2, woff) // the font lacks a code page 2 uses
        assertIs<AppError.Validation>((missingGlyph as AppResult.Failure).error)
        assertFalse(store.has(2))

        assertIs<AppResult.Failure>(store.save(3, woff.copyOf(woff.size - 8)))
        assertEquals(setOf(1), store.present())
        store.delete(1)
        assertEquals(emptySet(), store.present())
    }
}
