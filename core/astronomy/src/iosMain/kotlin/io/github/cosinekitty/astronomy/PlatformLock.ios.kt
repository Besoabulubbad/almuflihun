package io.github.cosinekitty.astronomy

import platform.Foundation.NSRecursiveLock

private val engineLock = NSRecursiveLock()

/** One lock for the engine's only cache (Pluto's), which the app never fills. */
internal actual inline fun <T> withPlatformLock(lock: Any, block: () -> T): T {
    engineLock.lock()
    try {
        return block()
    } finally {
        engineLock.unlock()
    }
}
