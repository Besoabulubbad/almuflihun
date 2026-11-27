package app.almuflihun.core.quran.font

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

/** Fetches a page font over HTTPS with the platform's HTTP stack; nothing is sent but the request for the file. */
internal class AndroidFontFetcher : FontFetcher {

    override suspend fun fetch(page: Int): FetchResult = withContext(Dispatchers.IO) {
        val connection = URL(FontSource.url(page)).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            val status = connection.responseCode
            if (status != HttpURLConnection.HTTP_OK) {
                FetchResult.Failed(status)
            } else {
                val bytes = connection.inputStream.use { it.readBytes() }
                if (bytes.size > MAX_BYTES) FetchResult.Failed(null) else FetchResult.Downloaded(bytes)
            }
        } catch (e: IOException) {
            if (e.isConnectionLoss()) FetchResult.NoConnection else FetchResult.Failed(null)
        } finally {
            connection.disconnect()
        }
    }

    /** No route, no name resolution, refused or timed out: the network, not the file. */
    private fun IOException.isConnectionLoss(): Boolean = this is UnknownHostException ||
        this is ConnectException || this is NoRouteToHostException || this is SocketTimeoutException

    private companion object {
        const val TIMEOUT_MS = 30_000
        const val MAX_BYTES = 4 * 1024 * 1024 // a page font is under 1 MB; anything far larger is not one
    }
}
