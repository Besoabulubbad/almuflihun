package app.almuflihun.core.quran.font

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Runs [PageFontDownloader] under WorkManager, so the download survives the app being closed and the phone
 * restarting, and waits for the right network and enough storage (ADR 0016). Built by [FontWorkerFactory].
 */
class FontDownloadWorker(
    context: Context,
    params: WorkerParameters,
    private val downloader: PageFontDownloader,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = when (downloader.downloadAll()) {
        DownloadOutcome.Complete -> Result.success()
        // The connection dropped: WorkManager runs it again, with growing pauses, when the network returns.
        DownloadOutcome.NoConnection -> Result.retry()
        // Some pages failed every attempt: try them again later, but not forever (they stay on Digital Khatt).
        is DownloadOutcome.Incomplete -> if (runAttemptCount < MAX_RUNS) Result.retry() else Result.success()
    }

    private companion object {
        const val MAX_RUNS = 5
    }
}

/**
 * Builds [FontDownloadWorker] with its engine (constructor injection, ADR 0011). The app hands it to WorkManager's
 * configuration; other workers fall through to WorkManager's default.
 */
class FontWorkerFactory(private val downloader: PageFontDownloader) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? =
        if (workerClassName == FontDownloadWorker::class.qualifiedName) {
            FontDownloadWorker(appContext, workerParameters, downloader)
        } else {
            null
        }
}

/** Schedules [FontDownloadWorker] as one unique job. */
internal class WorkManagerFontScheduler(private val context: Context) : FontDownloadScheduler {

    override fun start(allowMobileData: Boolean) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (allowMobileData) NetworkType.CONNECTED else NetworkType.UNMETERED)
            .setRequiresStorageNotLow(true)
            .build()
        val request = OneTimeWorkRequestBuilder<FontDownloadWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .addTag(WORK_NAME)
            .build()
        // REPLACE so a changed mobile-data answer takes effect; progress is kept in the store, nothing is lost.
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    override fun stop() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    companion object {
        const val WORK_NAME = "mushaf-page-fonts"
        private const val BACKOFF_SECONDS = 30L
    }
}
