package io.github.cosinekitty.astronomy

internal actual inline fun <T> withPlatformLock(lock: Any, block: () -> T): T = synchronized(lock, block)
