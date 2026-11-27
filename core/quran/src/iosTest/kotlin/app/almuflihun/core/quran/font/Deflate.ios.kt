package app.almuflihun.core.quran.font

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.zlib.Z_BEST_COMPRESSION
import platform.zlib.compress2
import platform.zlib.compressBound
import platform.zlib.uLongfVar

@OptIn(ExperimentalForeignApi::class)
internal actual fun deflate(data: ByteArray): ByteArray = memScoped {
    val out = ByteArray(compressBound(data.size.convert()).toInt())
    val length = alloc<uLongfVar>()
    length.value = out.size.convert()
    out.usePinned { dst ->
        data.usePinned { src ->
            compress2(
                dst.addressOf(0).reinterpret(),
                length.ptr,
                src.addressOf(0).reinterpret(),
                data.size.convert(),
                Z_BEST_COMPRESSION,
            )
        }
    }
    out.copyOf(length.value.toInt())
}
