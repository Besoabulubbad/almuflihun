package app.almuflihun.core.quran.font

/** zlib-deflates [data] (test-only, to build WOFF files). */
internal expect fun deflate(data: ByteArray): ByteArray

/** Fake font files in memory. */
internal class MemoryFontFiles : FontFiles {
    val files = mutableMapOf<String, ByteArray>()
    override fun read(name: String) = files[name]?.copyOf()
    override fun exists(name: String) = name in files
    override fun write(name: String, bytes: ByteArray) {
        files[name] = bytes.copyOf()
    }
    override fun delete(name: String) {
        files.remove(name)
    }
}

/**
 * Builds small, valid fonts and their WOFF files in code, so no third-party font is needed in tests: a 'cmap' with a
 * format 4 and a format 12 subtable mapping [codes] to glyphs 1.., a 'head' with a non-zero checkSumAdjustment, a
 * large compressible 'glyf' and a tiny 'name' (stored uncompressed).
 */
internal object TestFonts {

    // In tag order, as WOFF requires.
    fun tables(codes: List<Int>): Map<String, ByteArray> = linkedMapOf(
        "cmap" to cmap(codes),
        "glyf" to ByteArray(GLYF_SIZE) { (it % 7).toByte() },
        "head" to ByteArray(HEAD_SIZE) { if (it in 8..11) 0x5A else it.toByte() },
        "name" to byteArrayOf(0, 0, 0, 1, 0, 6),
    )

    fun woff(tables: Map<String, ByteArray>, corrupt: (ByteArray) -> Unit = {}): ByteArray {
        val entries = tables.entries.toList()
        val stored = entries.map { (_, t) -> deflate(t).let { z -> if (z.size < t.size) z else t } }
        var offset = 44 + entries.size * 20
        val offsets = stored.map { s -> offset.also { offset += (s.size + 3) and 3.inv() } }
        val out = ByteArray(offset)
        put32(out, 0, 0x774F4646)
        put32(out, 4, 0x00010000)
        put32(out, 8, out.size.toLong())
        put16(out, 12, entries.size)
        put32(out, 16, (12 + entries.size * 16 + tables.values.sumOf { (it.size + 3) and 3.inv() }).toLong())
        entries.forEachIndexed { i, (tag, table) ->
            val at = 44 + i * 20
            tag.forEachIndexed { j, c -> out[at + j] = c.code.toByte() }
            put32(out, at + 4, offsets[i].toLong())
            put32(out, at + 8, stored[i].size.toLong())
            put32(out, at + 12, table.size.toLong())
            put32(out, at + 16, Woff.checksum(table, zeroAdjustment = tag == "head"))
            stored[i].copyInto(out, offsets[i])
        }
        corrupt(out)
        return out
    }

    /** cmap with format 4 (codes ≤ U+FFFF) and format 12 (all codes), each mapping code i to glyph i + 1. */
    private fun cmap(codes: List<Int>): ByteArray {
        val bmp = codes.filter { it <= 0xFFFF }.sorted()
        val f4 = format4(bmp)
        val f12 = format12(codes.sorted())
        val out = ByteArray(4 + 2 * 8 + f4.size + f12.size)
        put16(out, 2, 2)
        put16(out, 4, 3)
        put16(out, 6, 1)
        put32(out, 8, 20)
        put16(out, 12, 3)
        put16(out, 14, 10)
        put32(out, 16, (20 + f4.size).toLong())
        f4.copyInto(out, 20)
        f12.copyInto(out, 20 + f4.size)
        return out
    }

    private fun format4(codes: List<Int>): ByteArray {
        val segments = codes.size + 1 // one segment per code, then the 0xFFFF terminator
        val out = ByteArray(16 + segments * 8)
        put16(out, 0, 4)
        put16(out, 2, out.size)
        put16(out, 6, segments * 2)
        val ends = 14
        val starts = ends + segments * 2 + 2
        val deltas = starts + segments * 2
        codes.forEachIndexed { i, code ->
            put16(out, ends + i * 2, code)
            put16(out, starts + i * 2, code)
            put16(out, deltas + i * 2, (i + 1 - code) and 0xFFFF)
        }
        put16(out, ends + codes.size * 2, 0xFFFF)
        put16(out, starts + codes.size * 2, 0xFFFF)
        put16(out, deltas + codes.size * 2, 1)
        return out
    }

    private fun format12(codes: List<Int>): ByteArray {
        val out = ByteArray(16 + codes.size * 12)
        put16(out, 0, 12)
        put32(out, 4, out.size.toLong())
        put32(out, 12, codes.size.toLong())
        codes.forEachIndexed { i, code ->
            put32(out, 16 + i * 12, code.toLong())
            put32(out, 20 + i * 12, code.toLong())
            put32(out, 24 + i * 12, (i + 1).toLong())
        }
        return out
    }

    private fun put16(b: ByteArray, at: Int, v: Int) {
        b[at] = (v shr 8).toByte()
        b[at + 1] = v.toByte()
    }

    private fun put32(b: ByteArray, at: Int, v: Long) {
        put16(b, at, (v shr 16).toInt())
        put16(b, at + 2, v.toInt())
    }

    private const val GLYF_SIZE = 4000
    private const val HEAD_SIZE = 54
}

/** Serves every page, with optional overrides; records the order pages were asked for. */
internal class FakeFontFetcher(
    private val good: ByteArray,
    val asked: MutableList<Int> = mutableListOf(),
    val override: (page: Int, attempt: Int) -> FetchResult? = { _, _ -> null },
) : FontFetcher {
    private val attempts = mutableMapOf<Int, Int>()
    var onFetch: (Int) -> Unit = {}
    override suspend fun fetch(page: Int): FetchResult {
        asked += page
        onFetch(page)
        val attempt = attempts.getOrElse(page) { 0 }.also { attempts[page] = it + 1 }
        return override(page, attempt) ?: FetchResult.Downloaded(good)
    }
}
