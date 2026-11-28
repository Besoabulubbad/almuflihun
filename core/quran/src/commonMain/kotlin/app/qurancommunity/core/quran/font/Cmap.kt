// Byte offsets below are those of the OpenType 'cmap' table formats 4 and 12.
@file:Suppress("MagicNumber")

package app.qurancommunity.core.quran.font

/**
 * Reads a font's character map ('cmap' formats 4 and 12, the ones the QPC fonts use) to tell which code points it
 * draws. Used to check that a page font maps every glyph code its page's words use.
 */
object Cmap {

    private const val FORMAT_4 = 4
    private const val FORMAT_12 = 12
    private const val NO_CHARACTER = 0xFFFF

    /** The code points in [codes] that the font has no glyph for. */
    fun missing(cmap: ByteArray, codes: Set<Int>): Set<Int> {
        val mapped = mapped(cmap)
        return codes.filterNot { it in mapped }.toSet()
    }

    /** Every code point that maps to a real glyph (not glyph 0). */
    fun mapped(cmap: ByteArray): Set<Int> {
        val codes = HashSet<Int>()
        val subtables = cmap.u16(2)
        for (i in 0 until subtables) {
            val offset = cmap.u32(4 + i * 8 + 4).toInt()
            when (cmap.u16(offset)) {
                FORMAT_4 -> readFormat4(cmap, offset, codes)
                FORMAT_12 -> readFormat12(cmap, offset, codes)
            }
        }
        return codes
    }

    private fun readFormat4(t: ByteArray, at: Int, out: MutableSet<Int>) {
        val segments = t.u16(at + 6) / 2
        val ends = at + 14
        val starts = ends + segments * 2 + 2
        val deltas = starts + segments * 2
        val rangeOffsets = deltas + segments * 2
        for (s in 0 until segments) {
            val start = t.u16(starts + s * 2)
            val end = t.u16(ends + s * 2)
            val delta = t.u16(deltas + s * 2)
            val rangeOffset = t.u16(rangeOffsets + s * 2)
            for (code in start..end) {
                val glyph = glyph4(t, rangeOffsets + s * 2, rangeOffset, code - start, code, delta)
                if (code != NO_CHARACTER && glyph != 0) out += code
            }
        }
    }

    /** The glyph for [code] in a format 4 segment: by delta, or through the glyph array when [rangeOffset] is set. */
    private fun glyph4(t: ByteArray, rangeOffsetAt: Int, rangeOffset: Int, index: Int, code: Int, delta: Int): Int {
        if (rangeOffset == 0) return (code + delta) and 0xFFFF
        val raw = t.u16(rangeOffsetAt + rangeOffset + index * 2)
        return if (raw == 0) 0 else (raw + delta) and 0xFFFF
    }

    private fun readFormat12(t: ByteArray, at: Int, out: MutableSet<Int>) {
        val groups = t.u32(at + 12).toInt()
        for (g in 0 until groups) {
            val group = at + 16 + g * 12
            val first = t.u32(group).toInt()
            val last = t.u32(group + 4).toInt()
            val glyph = t.u32(group + 8)
            for (code in first..last) if (glyph + (code - first) != 0L) out += code
        }
    }
}
