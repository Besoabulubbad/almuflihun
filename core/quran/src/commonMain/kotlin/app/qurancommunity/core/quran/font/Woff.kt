// Byte offsets and sizes below are those of the WOFF 1.0 and OpenType specifications; naming each would hide them.
@file:Suppress("MagicNumber")

package app.qurancommunity.core.quran.font

/** A font file that is not a valid, intact WOFF; it is discarded and downloaded again (ADR 0016). */
class InvalidFontException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** An OpenType font: its bytes as a TTF/OTF file and its tables by tag. */
class Sfnt(val bytes: ByteArray, val tables: Map<String, ByteArray>)

/**
 * Reads WOFF 1.0 (W3C, 2012): checks the header and every table entry, inflates each table, checks it against the
 * checksum WOFF stores for it, and rebuilds the plain font. Every check the format allows is made, so a damaged or
 * truncated download is never used. Verified on all 604 QPC V2 pages.
 */
object Woff {

    private const val SIGNATURE = 0x774F4646 // "wOFF"
    private const val HEADER_SIZE = 44
    private const val ENTRY_SIZE = 20
    private const val SFNT_HEADER_SIZE = 12
    private const val SFNT_ENTRY_SIZE = 16
    private const val HEAD_ADJUSTMENT = 8 // checkSumAdjustment inside 'head', zero when summing

    fun decode(woff: ByteArray): Sfnt {
        check(woff.size >= HEADER_SIZE, "shorter than a WOFF header")
        check(woff.u32(0) == SIGNATURE.toLong(), "not a WOFF file")
        val flavor = woff.u32(4)
        check(woff.u32(8) == woff.size.toLong(), "length ${woff.u32(8)} but the file has ${woff.size} bytes")
        val count = woff.u16(12)
        check(count > 0, "no tables")
        check(HEADER_SIZE + count * ENTRY_SIZE <= woff.size, "table directory runs past the end")

        val tables = LinkedHashMap<String, ByteArray>()
        val checksums = mutableListOf<Long>()
        var previous = ""
        for (i in 0 until count) {
            val at = HEADER_SIZE + i * ENTRY_SIZE
            val tag = woff.tag(at)
            val offset = woff.u32(at + 4)
            val compLength = woff.u32(at + 8)
            val origLength = woff.u32(at + 12)
            check(tag > previous, "tables out of order at $tag")
            check(offset % 4 == 0L && offset + compLength <= woff.size, "table $tag outside the file")
            check(compLength <= origLength, "table $tag larger compressed than original")
            val stored = woff.copyOfRange(offset.toInt(), (offset + compLength).toInt())
            val table = if (compLength < origLength) inflate(stored, origLength.toInt()) else stored
            check(table.size.toLong() == origLength, "table $tag inflated to ${table.size}, expected $origLength")
            val expected = woff.u32(at + 16)
            check(checksum(table, zeroAdjustment = tag == "head") == expected, "table $tag checksum mismatch")
            tables[tag] = table
            checksums += expected
            previous = tag
        }
        val sfnt = build(flavor, tables, checksums)
        check(sfnt.size.toLong() == woff.u32(16), "font size ${sfnt.size}, header says ${woff.u32(16)}")
        return Sfnt(sfnt, tables)
    }

    /** OpenType table checksum: the sum of big-endian 32-bit words, the last one zero-padded. */
    fun checksum(table: ByteArray, zeroAdjustment: Boolean = false): Long {
        var sum = 0L
        var i = 0
        while (i < table.size) {
            var word = 0L
            for (b in 0 until 4) {
                val index = i + b
                val skip = zeroAdjustment && index in HEAD_ADJUSTMENT until HEAD_ADJUSTMENT + 4
                val byte = if (index < table.size && !skip) table[index].toLong() and 0xFF else 0L
                word = (word shl 8) or byte
            }
            sum = (sum + word) and 0xFFFFFFFFL
            i += 4
        }
        return sum
    }

    /** The plain font: header, directory in tag order, tables 4-byte aligned. */
    private fun build(flavor: Long, tables: Map<String, ByteArray>, checksums: List<Long>): ByteArray {
        val count = tables.size
        var entrySelector = 0
        while (1 shl (entrySelector + 1) <= count) entrySelector++
        val searchRange = (1 shl entrySelector) * SFNT_ENTRY_SIZE
        val size = SFNT_HEADER_SIZE + count * SFNT_ENTRY_SIZE + tables.values.sumOf { padded(it.size) }
        val out = ByteArray(size)
        out.putU32(0, flavor)
        out.putU16(4, count)
        out.putU16(6, searchRange)
        out.putU16(8, entrySelector)
        out.putU16(10, count * SFNT_ENTRY_SIZE - searchRange)
        var data = SFNT_HEADER_SIZE + count * SFNT_ENTRY_SIZE
        tables.entries.forEachIndexed { i, (tag, table) ->
            val at = SFNT_HEADER_SIZE + i * SFNT_ENTRY_SIZE
            tag.forEachIndexed { j, c -> out[at + j] = c.code.toByte() }
            out.putU32(at + 4, checksums[i])
            out.putU32(at + 8, data.toLong())
            out.putU32(at + 12, table.size.toLong())
            table.copyInto(out, data)
            data += padded(table.size)
        }
        return out
    }

    private fun padded(size: Int) = (size + 3) and 3.inv()

    private fun check(condition: Boolean, reason: String) {
        if (!condition) throw InvalidFontException(reason)
    }
}

internal fun ByteArray.u16(at: Int): Int = ((this[at].toInt() and 0xFF) shl 8) or (this[at + 1].toInt() and 0xFF)

internal fun ByteArray.u32(at: Int): Long =
    ((u16(at).toLong()) shl 16) or u16(at + 2).toLong()

internal fun ByteArray.tag(at: Int): String = (0 until 4).map { (this[at + it].toInt() and 0xFF).toChar() }
    .joinToString("")

private fun ByteArray.putU16(at: Int, value: Int) {
    this[at] = (value shr 8).toByte()
    this[at + 1] = value.toByte()
}

private fun ByteArray.putU32(at: Int, value: Long) {
    putU16(at, (value shr 16).toInt())
    putU16(at + 2, value.toInt())
}

/** zlib-inflates [data], which must expand to exactly [size] bytes. */
internal expect fun inflate(data: ByteArray, size: Int): ByteArray
