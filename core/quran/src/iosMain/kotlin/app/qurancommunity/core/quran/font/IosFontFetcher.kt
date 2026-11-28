package app.qurancommunity.core.quran.font

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURL
import platform.Foundation.NSURLErrorCannotConnectToHost
import platform.Foundation.NSURLErrorCannotFindHost
import platform.Foundation.NSURLErrorDNSLookupFailed
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorNetworkConnectionLost
import platform.Foundation.NSURLErrorNotConnectedToInternet
import platform.Foundation.NSURLErrorTimedOut
import platform.Foundation.NSURLSession
import platform.Foundation.dataTaskWithURL
import platform.posix.memcpy
import kotlin.coroutines.resume

/** Fetches a page font over HTTPS with NSURLSession; nothing is sent but the request for the file. */
@OptIn(ExperimentalForeignApi::class)
internal class IosFontFetcher : FontFetcher {

    override suspend fun fetch(page: Int): FetchResult = suspendCancellableCoroutine { continuation ->
        val url = checkNotNull(NSURL.URLWithString(FontSource.url(page)))
        val task = NSURLSession.sharedSession.dataTaskWithURL(url) { data, response, error ->
            val status = (response as? NSHTTPURLResponse)?.statusCode?.toInt()
            val result = when {
                error != null && error.domain == NSURLErrorDomain && error.code in CONNECTION_LOSS ->
                    FetchResult.NoConnection
                error != null || status != HTTP_OK || data == null -> FetchResult.Failed(status)
                data.length.toInt() > MAX_BYTES -> FetchResult.Failed(null)
                else -> FetchResult.Downloaded(
                    ByteArray(data.length.toInt()).apply {
                        if (isNotEmpty()) usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
                    },
                )
            }
            continuation.resume(result)
        }
        continuation.invokeOnCancellation { task.cancel() }
        task.resume()
    }

    private companion object {
        const val HTTP_OK = 200
        const val MAX_BYTES = 4 * 1024 * 1024
        val CONNECTION_LOSS = setOf(
            NSURLErrorNotConnectedToInternet,
            NSURLErrorNetworkConnectionLost,
            NSURLErrorTimedOut,
            NSURLErrorCannotFindHost,
            NSURLErrorCannotConnectToHost,
            NSURLErrorDNSLookupFailed,
        )
    }
}
