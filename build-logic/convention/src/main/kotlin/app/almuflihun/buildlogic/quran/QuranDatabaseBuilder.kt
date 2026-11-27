package app.almuflihun.buildlogic.quran

import java.io.File
import java.security.MessageDigest
import java.sql.Connection

/**
 * Builds the app's read-only Quran database from the committed sources (core/quran/sources, ADR 0004 and ADR 0015).
 * It copies values only: the text, glyphs and layout are never edited. Every structural fact the app relies on is
 * checked here, so a broken or mismatched source fails the build instead of a page.
 */
object QuranDatabaseBuilder {

    const val SCHEMA_VERSION = 2
    const val PAGES = 604
    const val AYAHS = 6236
    const val SURAHS = 114
    const val VERSION_FILE = "quran.version"
    private const val AYAH_END = '۝' // ۝ starts every ayah-end marker word
    private const val SHORT_PAGES = 2 // pages 1 and 2 have 8 lines
    private const val SHORT_PAGE_LINES = 8
    private const val PAGE_LINES = 15
    private const val QUARTERS_PER_JUZ = 8

    data class Sources(
        val tanzil: File,
        val layout: File,
        val glyphWords: File,
        val textWords: File,
        val metadata: File,
    )

    private data class Line(
        val page: Int, val line: Int, val kind: String, val centered: Boolean,
        val first: Int?, val last: Int?, val surah: Int?,
    )

    private data class Word(val id: Int, val surah: Int, val ayah: Int, val position: Int, val text: String)

    fun build(sources: Sources, output: File) {
        val lines = readLayout(sources.layout)
        val glyphs = readWords(sources.glyphWords)
        val texts = readWords(sources.textWords)
        val tanzil = readTanzil(sources.tanzil)
        val placement = validate(lines, glyphs, texts, tanzil)
        val metadata = QuranMetadata.read(sources.metadata)
        validateMetadata(metadata, tanzil)

        output.parentFile.mkdirs()
        output.delete()
        connect("jdbc:sqlite:${output.absolutePath}").use { db ->
            db.autoCommit = false
            db.exec(SCHEMA)
            db.insertLines(lines)
            db.insertWords(glyphs, texts, placement)
            db.insertAyahs(tanzil, glyphs, placement)
            db.insertMetadata(metadata)
            db.insertMeta(sources)
            db.commit()
            db.autoCommit = true
            // SQLDelight's drivers create the schema when user_version is 0; 1 tells them the file is ready.
            db.exec("PRAGMA user_version = 1")
            db.exec("VACUUM")
        }
        // The app reinstalls its copy when this changes (any source or schema change changes the file).
        output.resolveSibling(VERSION_FILE).writeText(sha256(output))
    }

    private fun readLayout(file: File): List<Line> = query(file, LAYOUT_QUERY) { rs ->
        Line(
            page = rs.getInt(1), line = rs.getInt(2),
            kind = when (val type = rs.getString(3)) {
                "surah_name" -> "surah"
                "basmallah" -> "basmala"
                "ayah" -> "ayah"
                else -> error("Unknown line type $type")
            },
            centered = rs.getInt(4) == 1,
            first = rs.getString(5)?.takeIf { it.isNotBlank() }?.toInt(),
            last = rs.getString(6)?.takeIf { it.isNotBlank() }?.toInt(),
            surah = rs.getString(7)?.takeIf { it.isNotBlank() }?.toInt(),
        )
    }

    private fun readWords(file: File): List<Word> =
        query(file, "SELECT id, surah, ayah, word, text FROM words ORDER BY id") { rs ->
            Word(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getString(5))
        }

    /** `surah|ayah|text` lines; the copyright block and blank lines are not verses. */
    private fun readTanzil(file: File): Map<Pair<Int, Int>, String> = file.readLines(Charsets.UTF_8)
        .filter { it.isNotBlank() && !it.startsWith("#") }
        .associate { line ->
            val parts = line.split('|', limit = 3)
            require(parts.size == 3) { "Malformed Tanzil line: $line" }
            (parts[0].toInt() to parts[1].toInt()) to parts[2]
        }

    /** Checks the sources agree and returns each word's (page, line). */
    @Suppress("CyclomaticComplexMethod", "LongMethod")
    private fun validate(
        lines: List<Line>,
        glyphs: List<Word>,
        texts: List<Word>,
        tanzil: Map<Pair<Int, Int>, String>,
    ): Map<Int, Pair<Int, Int>> {
        check(lines.maxOf { it.page } == PAGES) { "Layout has ${lines.maxOf { it.page }} pages, expected $PAGES" }
        lines.groupBy { it.page }.forEach { (page, pageLines) ->
            val expected = if (page <= SHORT_PAGES) SHORT_PAGE_LINES else PAGE_LINES
            check(pageLines.map { it.line } == (1..expected).toList()) { "Page $page lines are not 1..$expected" }
        }
        check(lines.count { it.kind == "surah" } == SURAHS) { "Expected $SURAHS surah-name lines" }
        check(lines.filter { it.kind == "surah" }.map { it.surah } == (1..SURAHS).toList()) {
            "Surah-name lines are not 1..$SURAHS in order"
        }
        // Every surah but al-Fatiha (whose basmala is its first ayah) and at-Tawba has a basmala line.
        check(lines.count { it.kind == "basmala" } == SURAHS - 2) { "Expected ${SURAHS - 2} basmala lines" }

        check(glyphs.size == texts.size) { "Glyph and text word counts differ" }
        glyphs.zip(texts).forEach { (g, t) ->
            check(g.id == t.id && g.surah == t.surah && g.ayah == t.ayah && g.position == t.position) {
                "Word ${g.id} differs between the glyph and text sources"
            }
        }
        check(glyphs.map { it.id } == (1..glyphs.size).toList()) { "Word ids are not 1..${glyphs.size}" }

        // The ayah lines cover every word exactly once, in order.
        val placement = HashMap<Int, Pair<Int, Int>>(glyphs.size * 2)
        var next = 1
        lines.filter { it.kind == "ayah" }.forEach { line ->
            val first = checkNotNull(line.first) { "Ayah line ${line.page}:${line.line} has no first word" }
            val last = checkNotNull(line.last) { "Ayah line ${line.page}:${line.line} has no last word" }
            check(first == next && last >= first) { "Line ${line.page}:${line.line} starts at $first, expected $next" }
            (first..last).forEach { placement[it] = line.page to line.line }
            next = last + 1
        }
        check(next == glyphs.size + 1) { "Layout covers words 1..${next - 1} of ${glyphs.size}" }

        // One ayah-end marker per ayah, always its last word.
        val byAyah = texts.groupBy { it.surah to it.ayah }
        check(byAyah.size == AYAHS) { "Words cover ${byAyah.size} ayahs, expected $AYAHS" }
        byAyah.forEach { (key, words) ->
            val markers = words.filter { it.text.firstOrNull() == AYAH_END }
            check(markers.size == 1 && markers.single() == words.maxBy { it.position }) {
                "Ayah ${key.first}:${key.second} must end with exactly one ayah-end marker"
            }
        }
        check(tanzil.keys == byAyah.keys) { "Tanzil verses and layout ayahs differ" }
        return placement
    }

    /** Tanzil's metadata must agree with the Tanzil text: ayah counts, start indexes, quarters in order. */
    private fun validateMetadata(meta: QuranMetadata, tanzil: Map<Pair<Int, Int>, String>) {
        val counts = tanzil.keys.groupingBy { it.first }.eachCount()
        var start = 0
        meta.surahs.forEachIndexed { i, s ->
            val n = i + 1
            check(s.ayahs == counts[n]) { "quran-data.js: surah $n has ${s.ayahs} ayahs, the text ${counts[n]}" }
            check(s.start == start) { "quran-data.js: surah $n starts at ${s.start}, expected $start" }
            check(s.place == "Meccan" || s.place == "Medinan") { "quran-data.js: surah $n place ${s.place}" }
            start += s.ayahs
        }
        check(meta.surahs.map { it.order }.sorted() == (1..SURAHS).toList()) { "revelation order is not 1..$SURAHS" }
        val order = compareBy<Pair<Int, Int>>({ it.first }, { it.second })
        check(meta.quarterStarts == meta.quarterStarts.sortedWith(order)) { "hizb quarters out of order" }
        check(meta.quarterStarts.first() == (1 to 1)) { "the first quarter does not start at 1:1" }
        check(meta.juzStarts == meta.quarterStarts.filterIndexed { i, _ -> i % QUARTERS_PER_JUZ == 0 }) {
            "juz starts are not every eighth quarter"
        }
        (meta.quarterStarts + meta.sajdas.map { it.surah to it.ayah }).forEach { key ->
            check(key in tanzil) { "quran-data.js names ${key.first}:${key.second}, which is not in the text" }
        }
    }

    private fun Connection.insertMetadata(meta: QuranMetadata) {
        batch("INSERT INTO surah(number, name, ayahs, place, revelation_order) VALUES (?,?,?,?,?)",
            meta.surahs.withIndex()) { st, (i, s) ->
            st.setInt(1, i + 1)
            st.setString(2, s.name)
            st.setInt(3, s.ayahs)
            st.setString(4, s.place.lowercase())
            st.setInt(5, s.order)
        }
        batch("INSERT INTO quarter(number, surah, ayah) VALUES (?,?,?)", meta.quarterStarts.withIndex()) { st, (i, q) ->
            st.setInt(1, i + 1)
            st.setInt(2, q.first)
            st.setInt(3, q.second)
        }
        // Only where the sajdas are, as the printed Mushaf marks them. Tanzil also labels each one "obligatory" or
        // "recommended": a ruling of the schools, not the text, so it is not copied (DESIGN §1).
        batch("INSERT INTO sajda(surah, ayah) VALUES (?,?)", meta.sajdas) { st, s ->
            st.setInt(1, s.surah)
            st.setInt(2, s.ayah)
        }
    }

    private fun Connection.insertLines(lines: List<Line>) = batch(
        "INSERT INTO line(page, line, kind, centered, first_word, last_word, surah) VALUES (?,?,?,?,?,?,?)",
        lines,
    ) { s, l ->
        s.setInt(1, l.page)
        s.setInt(2, l.line)
        s.setString(3, l.kind)
        s.setInt(4, if (l.centered) 1 else 0)
        s.setObject(5, l.first)
        s.setObject(6, l.last)
        s.setObject(7, l.surah)
    }

    private fun Connection.insertWords(glyphs: List<Word>, texts: List<Word>, placement: Map<Int, Pair<Int, Int>>) =
        batch(
            "INSERT INTO word(id, surah, ayah, position, page, line, is_end, glyph, text) VALUES (?,?,?,?,?,?,?,?,?)",
            glyphs.zip(texts),
        ) { s, (g, t) ->
            val (page, line) = placement.getValue(g.id)
            s.setInt(1, g.id)
            s.setInt(2, g.surah)
            s.setInt(3, g.ayah)
            s.setInt(4, g.position)
            s.setInt(5, page)
            s.setInt(6, line)
            s.setInt(7, if (t.text.first() == AYAH_END) 1 else 0)
            s.setString(8, g.text)
            s.setString(9, t.text)
        }

    private fun Connection.insertAyahs(
        tanzil: Map<Pair<Int, Int>, String>,
        words: List<Word>,
        placement: Map<Int, Pair<Int, Int>>,
    ) {
        val firstWord = words.groupBy { it.surah to it.ayah }.mapValues { (_, w) -> w.minBy { it.position }.id }
        batch(
            "INSERT INTO ayah(surah, ayah, page, simple) VALUES (?,?,?,?)",
            tanzil.entries.sortedWith(compareBy({ it.key.first }, { it.key.second })),
        ) { s, (key, text) ->
            s.setInt(1, key.first)
            s.setInt(2, key.second)
            s.setInt(3, placement.getValue(firstWord.getValue(key)).first)
            s.setString(4, text)
        }
    }

    private fun Connection.insertMeta(sources: Sources) = batch(
        "INSERT INTO meta(key, value) VALUES (?,?)",
        listOf(
            "schema_version" to SCHEMA_VERSION.toString(),
            "tanzil_sha256" to sha256(sources.tanzil),
            "layout_sha256" to sha256(sources.layout),
            "glyph_words_sha256" to sha256(sources.glyphWords),
            "text_words_sha256" to sha256(sources.textWords),
            "metadata_sha256" to sha256(sources.metadata),
        ),
    ) { s, (k, v) ->
        s.setString(1, k)
        s.setString(2, v)
    }

    private fun <T> Connection.batch(sql: String, rows: Iterable<T>, bind: (java.sql.PreparedStatement, T) -> Unit) {
        prepareStatement(sql).use { s ->
            rows.forEach { row ->
                bind(s, row)
                s.addBatch()
            }
            s.executeBatch()
        }
    }

    private fun Connection.exec(sql: String) = createStatement().use { st ->
        sql.split(";").map { it.trim() }.filter { it.isNotEmpty() }.forEach { st.execute(it) }
    }

    private fun <T> query(file: File, sql: String, map: (java.sql.ResultSet) -> T): List<T> {
        require(file.exists()) { "Missing source ${file.path}" }
        return connect("jdbc:sqlite:${file.absolutePath}?open_mode=1").use { db ->
            db.createStatement().use { st ->
                st.executeQuery(sql).use { rs -> buildList { while (rs.next()) add(map(rs)) } }
            }
        }
    }

    /**
     * Opens SQLite through the driver class itself: inside Gradle, plugins live in separate class loaders and
     * `DriverManager` does not always see the driver.
     */
    private fun connect(url: String): Connection =
        checkNotNull(org.sqlite.JDBC().connect(url, java.util.Properties())) { "Cannot open $url" }

    private fun sha256(file: File): String =
        MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }

    private const val LAYOUT_QUERY = "SELECT page_number, line_number, line_type, is_centered, first_word_id, " +
        "last_word_id, surah_number FROM pages ORDER BY page_number, line_number"

    /** The bundled schema. Raise [SCHEMA_VERSION] whenever it changes; the app replaces its copy when the version differs. */
    val SCHEMA = """
        CREATE TABLE meta (key TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL) WITHOUT ROWID;
        CREATE TABLE line (
            page INTEGER NOT NULL, line INTEGER NOT NULL, kind TEXT NOT NULL, centered INTEGER NOT NULL,
            first_word INTEGER, last_word INTEGER, surah INTEGER, PRIMARY KEY (page, line)
        ) WITHOUT ROWID;
        CREATE TABLE word (
            id INTEGER NOT NULL PRIMARY KEY, surah INTEGER NOT NULL, ayah INTEGER NOT NULL,
            position INTEGER NOT NULL, page INTEGER NOT NULL, line INTEGER NOT NULL, is_end INTEGER NOT NULL,
            glyph TEXT NOT NULL, text TEXT NOT NULL
        );
        CREATE INDEX word_by_ayah ON word (surah, ayah, position);
        CREATE TABLE ayah (
            surah INTEGER NOT NULL, ayah INTEGER NOT NULL, page INTEGER NOT NULL, simple TEXT NOT NULL,
            PRIMARY KEY (surah, ayah)
        ) WITHOUT ROWID;
        CREATE INDEX ayah_by_page ON ayah (page);
        CREATE TABLE surah (
            number INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL, ayahs INTEGER NOT NULL, place TEXT NOT NULL,
            revelation_order INTEGER NOT NULL
        );
        CREATE TABLE quarter (number INTEGER NOT NULL PRIMARY KEY, surah INTEGER NOT NULL, ayah INTEGER NOT NULL);
        CREATE TABLE sajda (surah INTEGER NOT NULL, ayah INTEGER NOT NULL, PRIMARY KEY (surah, ayah)) WITHOUT ROWID;
    """.trimIndent()
}
