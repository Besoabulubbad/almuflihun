package app.qurancommunity.core.quran.font

import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.model.PageFont
import app.qurancommunity.core.model.QuranWord
import app.qurancommunity.core.model.drawnWith
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PageFontProviderTest {

    private val woff = TestFonts.woff(TestFonts.tables(listOf(0xFC41)))
    private val store = PageFontStore(MemoryFontFiles()) { setOf(0xFC41) }
    private val fetcher = FakeFontFetcher(woff)
    private val downloader = PageFontDownloader(store, fetcher)
    private val provider = StorePageFontProvider(store, downloader) { page ->
        if (store.has(page)) PageFont.Exact(page, file = "p$page.ttf") else null
    }

    @Test
    fun a_page_on_the_device_is_drawn_exactly_at_once() = runTest {
        store.save(7, woff)
        val fonts = provider.font(7).toList()
        assertEquals(1, fonts.size)
        assertEquals("p7.ttf", assertIs<PageFont.Exact>(fonts.single()).file)
    }

    @Test
    fun a_missing_page_draws_the_fallback_then_switches_when_it_arrives() = runTest {
        val fonts = async { provider.font(12).take(2).toList() }
        yield()
        store.save(11, woff) // another page: no switch
        store.save(12, woff)
        val seen = fonts.await()
        assertEquals(PageFont.Fallback, seen[0])
        assertEquals(12, assertIs<PageFont.Exact>(seen[1]).page)
    }

    @Test
    fun asking_for_a_page_moves_it_to_the_front_of_the_download() = runTest {
        val fonts = async { provider.font(300).take(2).toList() }
        yield()
        downloader.downloadAll()
        assertEquals(300, fetcher.asked.first())
        assertIs<PageFont.Exact>(fonts.await()[1])
    }

    @Test
    fun a_word_is_drawn_from_its_glyph_or_its_text() {
        val word = QuranWord(1, AyahKey(1, 1), 1, glyph = "G", text = "T", isAyahEnd = false)
        assertEquals("G", word.drawnWith(PageFont.Exact(1, file = "p1.ttf")))
        assertEquals("T", word.drawnWith(PageFont.Fallback))
    }
}
