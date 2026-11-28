package app.qurancommunity.core.quran.font

import android.content.Context
import androidx.work.Configuration
import androidx.work.ListenableWorker.Result
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FontDownloadWorkerTest {

    private val context: Context = RuntimeEnvironment.getApplication()
    private val woff = TestFonts.woff(TestFonts.tables(listOf(0xFC41)))

    private fun worker(fetcher: FontFetcher, attempt: Int = 0): FontDownloadWorker {
        val store = PageFontStore(MemoryFontFiles()) { setOf(0xFC41) }
        return TestListenableWorkerBuilder<FontDownloadWorker>(context)
            .setWorkerFactory(FontWorkerFactory(PageFontDownloader(store, fetcher)))
            .setRunAttemptCount(attempt)
            .build()
    }

    @Test
    fun a_complete_run_succeeds() = runBlocking {
        assertEquals(Result.success(), worker(FakeFontFetcher(woff)).doWork())
    }

    @Test
    fun a_lost_connection_asks_to_run_again() = runBlocking {
        val offline = FakeFontFetcher(woff, override = { _, _ -> FetchResult.NoConnection })
        assertEquals(Result.retry(), worker(offline).doWork())
    }

    @Test
    fun pages_that_keep_failing_are_retried_a_few_times_then_left() = runBlocking {
        val missing = FakeFontFetcher(woff, override = { page, _ -> if (page == 9) FetchResult.Failed(404) else null })
        assertEquals(Result.retry(), worker(missing, attempt = 1).doWork())
        assertEquals(Result.success(), worker(missing, attempt = 5).doWork())
    }

    @Test
    fun the_job_waits_for_wifi_unless_mobile_data_is_allowed() {
        val config = Configuration.Builder().setExecutor(SynchronousExecutor()).build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        val scheduler = WorkManagerFontScheduler(context)
        val work = WorkManager.getInstance(context)
        fun all() = runBlocking { work.getWorkInfosForUniqueWorkFlow(WorkManagerFontScheduler.WORK_NAME).first() }
        fun active() = all().filter { !it.state.isFinished }

        scheduler.start(allowMobileData = false)
        assertEquals(NetworkType.UNMETERED, active().single().constraints.requiredNetworkType)
        assertEquals(true, active().single().constraints.requiresStorageNotLow())

        scheduler.start(allowMobileData = true) // replaces the job: still one
        assertEquals(NetworkType.CONNECTED, active().single().constraints.requiredNetworkType)

        scheduler.stop()
        assertEquals(emptyList(), active())
        assertEquals(
            WorkInfo.State.CANCELLED,
            all().last().state,
        )
    }
}
