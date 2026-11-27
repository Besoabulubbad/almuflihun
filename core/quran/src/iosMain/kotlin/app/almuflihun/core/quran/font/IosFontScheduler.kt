package app.almuflihun.core.quran.font

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * On iOS the download runs while the app is open, through the same engine; it resumes on the next launch. A
 * background `URLSession` (downloads with the app closed) needs Swift app-delegate wiring and comes before iOS is
 * distributed (ADR 0010, 0016). [allowMobileData] is honoured by the caller's [DownloadPolicy] gate.
 */
internal class IosFontScheduler(
    private val downloader: PageFontDownloader,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : FontDownloadScheduler {

    private var job: Job? = null

    override fun start(allowMobileData: Boolean) {
        job?.cancel()
        job = scope.launch { downloader.downloadAll() }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }
}
