package app.qurancommunity.core.quran.font

import app.qurancommunity.core.common.AppResult
import app.qurancommunity.core.model.MushafPage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

/** Where the exact page fonts come from (ADR 0013, 0016): the Quran Foundation CDN, as WOFF. */
object FontSource {
    private const val BASE = "https://verses.quran.foundation/fonts/quran/hafs/v2/woff"

    fun url(page: Int): String = "$BASE/p$page.woff"

    /** The whole Mushaf as WOFF, measured on the 604 files (ADR 0016). */
    const val TOTAL_BYTES = 104L * 1024 * 1024
}

/** One page's download. */
sealed interface FetchResult {
    class Downloaded(val bytes: ByteArray) : FetchResult
    data object NoConnection : FetchResult
    data class Failed(val status: Int?) : FetchResult
}

/** Downloads one page font; one implementation per platform. */
interface FontFetcher {
    suspend fun fetch(page: Int): FetchResult
}

/** The order pages are fetched in (ADR 0013): the reader's page first, then outward, the next page before the last. */
object DownloadOrder {
    fun next(done: Set<Int>, focus: Int): Int? = (1..MushafPage.COUNT)
        .filter { it !in done }
        .minWithOrNull(compareBy<Int> { abs(it - focus) }.thenBy { if (it >= focus) 0 else 1 })
}

/** The connection the device has now. */
enum class Connection { Unmetered, Metered, None }

/** The reader's one-time answer about mobile data (ADR 0013), remembered in Settings. */
enum class MobileDataChoice { NotAsked, Allowed, Declined }

/** Whether downloading may go ahead now. */
sealed interface DownloadGate {
    data object Go : DownloadGate
    data object WaitForNetwork : DownloadGate
    data object WaitForWifi : DownloadGate

    /** Ask once, showing [bytes] still to download. */
    data class AskForMobileData(val bytes: Long) : DownloadGate

    /** Not enough free space: [needed] bytes, [free] available. */
    data class NotEnoughSpace(val needed: Long, val free: Long) : DownloadGate

    data object Complete : DownloadGate
}

/** The rules of ADR 0013 before any byte is fetched: network, mobile data, and free space. */
object DownloadPolicy {
    /** Working space kept free beyond the fonts themselves. */
    const val MARGIN_BYTES = 25L * 1024 * 1024

    fun gate(connection: Connection, choice: MobileDataChoice, freeBytes: Long, pagesLeft: Int): DownloadGate {
        val bytesLeft = FontSource.TOTAL_BYTES * pagesLeft / MushafPage.COUNT
        val needed = bytesLeft + MARGIN_BYTES
        return when {
            pagesLeft == 0 -> DownloadGate.Complete
            freeBytes < needed -> DownloadGate.NotEnoughSpace(needed, freeBytes)
            connection == Connection.None -> DownloadGate.WaitForNetwork
            connection == Connection.Unmetered -> DownloadGate.Go
            choice == MobileDataChoice.Allowed -> DownloadGate.Go
            choice == MobileDataChoice.Declined -> DownloadGate.WaitForWifi
            else -> DownloadGate.AskForMobileData(bytesLeft)
        }
    }
}

/** How far the download is. [failed] pages stay on Digital Khatt until a later run. */
data class FontProgress(val present: Int, val failed: Set<Int> = emptySet(), val running: Boolean = false) {
    val total: Int get() = MushafPage.COUNT
    val complete: Boolean get() = present == total
}

/** How a run ended. */
sealed interface DownloadOutcome {
    data object Complete : DownloadOutcome

    /** The connection dropped; the scheduler runs again when it returns. */
    data object NoConnection : DownloadOutcome

    /** Some pages could not be fetched or checked after every attempt; the rest are done. */
    data class Incomplete(val failed: Set<Int>) : DownloadOutcome
}

/**
 * Fetches the missing page fonts in [DownloadOrder], keeping each only if [PageFontStore] accepts it. A page that
 * fails is tried [ATTEMPTS] times with a growing pause; a connection loss ends the run so the platform scheduler can
 * resume it later (ADR 0016). [focusOn] moves a page the reader opens to the front.
 */
class PageFontDownloader(private val store: PageFontStore, private val fetcher: FontFetcher) {

    private val focus = MutableStateFlow(1)
    private val state = MutableStateFlow(FontProgress(present = 0))

    val progress: StateFlow<FontProgress> = state.asStateFlow()

    fun focusOn(page: Int) {
        PageFontStore.requirePage(page)
        focus.value = page
    }

    suspend fun downloadAll(): DownloadOutcome {
        val done = store.present().toMutableSet()
        val failed = mutableSetOf<Int>()
        state.value = FontProgress(present = done.size, running = true)
        try {
            while (true) {
                val page = DownloadOrder.next(done + failed, focus.value) ?: break
                when (fetchAndKeep(page)) {
                    PageResult.Kept -> done += page
                    PageResult.Failed -> failed += page
                    PageResult.NoConnection -> return DownloadOutcome.NoConnection
                }
                state.update { it.copy(present = done.size, failed = failed.toSet()) }
            }
        } finally {
            state.update { it.copy(running = false) }
        }
        return if (failed.isEmpty()) DownloadOutcome.Complete else DownloadOutcome.Incomplete(failed.toSet())
    }

    private enum class PageResult { Kept, Failed, NoConnection }

    private suspend fun fetchAndKeep(page: Int): PageResult {
        var outcome: PageResult? = null
        var attempt = 0
        while (outcome == null && attempt < ATTEMPTS) {
            if (attempt > 0) delay(PAUSE * attempt)
            outcome = tryOnce(page)
            attempt++
        }
        return outcome ?: PageResult.Failed
    }

    /** One attempt: a final result, or null to try again. */
    private suspend fun tryOnce(page: Int): PageResult? = when (val result = fetcher.fetch(page)) {
        FetchResult.NoConnection -> PageResult.NoConnection
        is FetchResult.Failed -> if (result.status == NOT_FOUND) PageResult.Failed else null
        is FetchResult.Downloaded -> if (store.save(page, result.bytes) is AppResult.Success) PageResult.Kept else null
    }

    private companion object {
        const val ATTEMPTS = 3
        const val NOT_FOUND = 404
        val PAUSE = 2.seconds
    }
}

/**
 * Starts and stops the page-font download in the background (ADR 0016). The caller applies [DownloadPolicy] first
 * (space, and the reader's mobile-data answer) and passes that answer here.
 */
interface FontDownloadScheduler {
    /** Downloads the missing pages: on any connection if [allowMobileData], otherwise only on Wi-Fi. */
    fun start(allowMobileData: Boolean)

    fun stop()
}
