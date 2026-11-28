package app.qurancommunity.core.quran.font

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.zlib.Z_OK
import platform.zlib.uLongfVar
import platform.zlib.uncompress

@OptIn(ExperimentalForeignApi::class)
internal actual fun inflate(data: ByteArray, size: Int): ByteArray {
    if (data.isEmpty()) throw InvalidFontException("empty table data")
    val out = ByteArray(size)
    memScoped {
        val length = alloc<uLongfVar>()
        length.value = size.convert()
        val status = out.usePinned { dst ->
            data.usePinned { src ->
                uncompress(
                    dst.addressOf(0).reinterpret(),
                    length.ptr,
                    src.addressOf(0).reinterpret(),
                    data.size.convert(),
                )
            }
        }
        if (status != Z_OK || length.value.toInt() != size) {
            throw InvalidFontException("table did not inflate to $size bytes (zlib $status)")
        }
    }
    return out
}
