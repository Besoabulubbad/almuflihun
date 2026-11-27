package app.almuflihun.core.quran.font

import app.almuflihun.core.testing.runAppTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class AndroidPageFontCacheTest {

    private val dir = Files.createTempDirectory("fonts").toFile()
    private val files = AndroidFontFiles(dir.resolve("store"))
    private val store = PageFontStore(files) { setOf(0xFC41) }
    private val woff = TestFonts.woff(TestFonts.tables(listOf(0xFC41)))

    @Test
    fun a_stored_page_becomes_a_ttf_and_the_cache_keeps_the_newest() = runAppTest {
        val cache = AndroidPageFontCache(store, dir.resolve("ttf"), keep = 3)
        assertNull(cache.ttf(1)) // not downloaded: the fallback is drawn
        for (page in 1..5) store.save(page, woff)
        for (page in 1..5) {
            val ttf = cache.ttf(page)!!
            assertContentEquals(Woff.decode(woff).bytes, ttf.readBytes())
            ttf.setLastModified(page * 1_000L)
        }
        assertEquals(3, dir.resolve("ttf").listFiles()!!.count { it.name.endsWith(".ttf") })
    }

    @Test
    fun a_font_damaged_in_storage_is_dropped_so_it_downloads_again() = runAppTest {
        store.save(7, woff)
        dir.resolve("store/p7.woff").writeBytes(woff.copyOf(woff.size - 8))
        assertNull(AndroidPageFontCache(store, dir.resolve("ttf")).ttf(7))
        assertFalse(store.has(7))
    }
}
