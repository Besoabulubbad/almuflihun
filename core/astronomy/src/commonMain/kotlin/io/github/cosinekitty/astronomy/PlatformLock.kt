package io.github.cosinekitty.astronomy

/** [qurancommunity] Replaces the JVM's `synchronized` in the ported engine (ADR 0017). */
internal expect inline fun <T> withPlatformLock(lock: Any, block: () -> T): T
