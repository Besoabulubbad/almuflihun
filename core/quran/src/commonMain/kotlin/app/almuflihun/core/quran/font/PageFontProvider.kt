package app.almuflihun.core.quran.font

import app.almuflihun.core.model.PageFont
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onSubscription

/** The font each Mushaf page is drawn with, switching from the fallback to the exact page as soon as it arrives. */
interface PageFontProvider {
    /**
     * [PageFont.Exact] if the page's font is on the device; otherwise [PageFont.Fallback] first, then
     * [PageFont.Exact] the moment the page is saved. Asking for a page moves it to the front of the download.
     */
    fun font(page: Int): Flow<PageFont>
}

/** [exact] returns the platform's ready-to-draw font for a stored page, or null when it is not on the device. */
internal class StorePageFontProvider(
    private val store: PageFontStore,
    private val downloader: PageFontDownloader,
    private val exact: suspend (page: Int) -> PageFont.Exact?,
) : PageFontProvider {

    override fun font(page: Int): Flow<PageFont> = flow {
        PageFontStore.requirePage(page)
        downloader.focusOn(page)
        val ready = exact(page)
        if (ready != null) {
            emit(ready)
        } else {
            emit(PageFont.Fallback)
            // Subscribe first, then check again, so a page saved in between is not missed.
            store.saved
                .onSubscription { if (store.has(page)) emit(page) }
                .filter { it == page }
                .first()
            exact(page)?.let { emit(it) }
        }
    }
}
