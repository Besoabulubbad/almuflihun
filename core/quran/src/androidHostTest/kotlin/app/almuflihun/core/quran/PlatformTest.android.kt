package app.almuflihun.core.quran

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.nio.file.Files

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
actual abstract class PlatformTest actual constructor()

internal actual fun newTestStorage(): QuranStorage =
    AndroidQuranStorage(RuntimeEnvironment.getApplication(), Files.createTempDirectory("quran-test").toFile())
