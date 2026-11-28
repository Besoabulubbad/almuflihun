package app.qurancommunity.core.quran.font

import java.util.zip.DataFormatException
import java.util.zip.Inflater

internal actual fun inflate(data: ByteArray, size: Int): ByteArray {
    val inflater = Inflater()
    try {
        inflater.setInput(data)
        val out = ByteArray(size)
        val written = inflater.inflate(out)
        if (written != size || !inflater.finished()) throw InvalidFontException("table did not inflate to $size bytes")
        return out
    } catch (e: DataFormatException) {
        throw InvalidFontException("table is not valid zlib data", e)
    } finally {
        inflater.end()
    }
}
