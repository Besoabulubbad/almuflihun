package app.almuflihun.core.quran

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
actual abstract class PlatformTest actual constructor()

internal actual fun newTestStorage(): QuranStorage {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    return AndroidQuranStorage(context, File(context.cacheDir, "quran-test-${UUID.randomUUID()}"))
}
