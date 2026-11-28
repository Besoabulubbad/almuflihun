package app.qurancommunity.core.quran

import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

actual abstract class PlatformTest actual constructor()

internal actual fun newTestStorage(): QuranStorage =
    IosQuranStorage(directory = NSTemporaryDirectory() + "quran-test-" + NSUUID().UUIDString)
