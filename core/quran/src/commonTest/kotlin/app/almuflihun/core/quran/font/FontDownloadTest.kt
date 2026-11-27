package app.almuflihun.core.quran.font

import app.almuflihun.core.model.MushafPage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FontDownloadTest {

    private val woff = TestFonts.woff(TestFonts.tables(listOf(0xFC41)))

    private fun store(files: MemoryFontFiles = MemoryFontFiles()) = PageFontStore(files) { setOf(0xFC41) }

    @Test
    fun the_reader_s_page_comes_first_then_outward_next_page_first() {
        assertEquals(50, DownloadOrder.next(emptySet(), focus = 50))
        assertEquals(51, DownloadOrder.next(setOf(50), focus = 50))
        assertEquals(49, DownloadOrder.next(setOf(50, 51), focus = 50))
        assertEquals(603, DownloadOrder.next(setOf(604), focus = 604))
        assertEquals(null, DownloadOrder.next((1..MushafPage.COUNT).toSet(), focus = 1))
    }

    @Test
    fun the_gate_follows_adr_0013() {
        val plenty = 1L shl 33
        fun gate(c: Connection, choice: MobileDataChoice, free: Long = plenty, left: Int = 10) =
            DownloadPolicy.gate(c, choice, free, left)
        assertIs<DownloadGate.Go>(gate(Connection.Unmetered, MobileDataChoice.NotAsked, left = 604))
        val ask = gate(Connection.Metered, MobileDataChoice.NotAsked, left = 302)
        assertEquals(DownloadGate.AskForMobileData(FontSource.TOTAL_BYTES / 2), ask)
        assertIs<DownloadGate.Go>(gate(Connection.Metered, MobileDataChoice.Allowed))
        assertIs<DownloadGate.WaitForWifi>(gate(Connection.Metered, MobileDataChoice.Declined))
        assertIs<DownloadGate.WaitForNetwork>(gate(Connection.None, MobileDataChoice.Allowed))
        assertIs<DownloadGate.NotEnoughSpace>(gate(Connection.Unmetered, MobileDataChoice.Allowed, 1L shl 20, 604))
        assertIs<DownloadGate.Complete>(gate(Connection.None, MobileDataChoice.NotAsked, 0, 0))
    }

    @Test
    fun a_run_fetches_every_missing_page_from_the_focus() = runTest {
        val files = MemoryFontFiles()
        val store = store(files)
        store.save(2, woff)
        val fetcher = FakeFontFetcher(woff)
        val downloader = PageFontDownloader(store, fetcher)
        downloader.focusOn(300)
        assertEquals(DownloadOutcome.Complete, downloader.downloadAll())
        assertEquals(listOf(300, 301, 299, 302), fetcher.asked.take(4))
        assertEquals(MushafPage.COUNT - 1, fetcher.asked.size) // page 2 was already there
        assertTrue(downloader.progress.value.complete)
    }

    @Test
    fun a_page_the_reader_opens_jumps_ahead() = runTest {
        val fetcher = FakeFontFetcher(woff)
        val downloader = PageFontDownloader(store(), fetcher)
        fetcher.onFetch = { page -> if (page == 3) downloader.focusOn(500) }
        downloader.downloadAll()
        assertEquals(listOf(1, 2, 3, 500, 501, 499), fetcher.asked.take(6))
    }

    @Test
    fun a_damaged_file_is_tried_again_and_a_missing_one_is_left_for_later() = runTest {
        val fetcher = FakeFontFetcher(woff, override = { page, attempt ->
            when {
                page == 10 && attempt == 0 -> FetchResult.Downloaded(woff.copyOf(woff.size - 8)) // damaged once
                page == 20 -> FetchResult.Downloaded(woff.copyOf(100)) // always damaged
                page == 30 -> FetchResult.Failed(404)
                else -> null
            }
        })
        val downloader = PageFontDownloader(store(), fetcher)
        val outcome = downloader.downloadAll()
        assertEquals(DownloadOutcome.Incomplete(setOf(20, 30)), outcome)
        assertEquals(2, fetcher.asked.count { it == 10 })
        assertEquals(3, fetcher.asked.count { it == 20 })
        assertEquals(1, fetcher.asked.count { it == 30 })
        assertEquals(MushafPage.COUNT - 2, downloader.progress.value.present)
    }

    @Test
    fun losing_the_connection_ends_the_run_and_the_next_run_resumes() = runTest {
        var online = true
        val fetcher = FakeFontFetcher(woff, override = { page, _ ->
            if (page == 5) online = false
            if (!online) FetchResult.NoConnection else null
        })
        val store = store()
        val downloader = PageFontDownloader(store, fetcher)
        assertEquals(DownloadOutcome.NoConnection, downloader.downloadAll())
        assertEquals(4, downloader.progress.value.present)
        online = true
        val resumed = FakeFontFetcher(woff)
        assertEquals(DownloadOutcome.Complete, PageFontDownloader(store, resumed).downloadAll())
        assertEquals(5, resumed.asked.first())
    }
}
