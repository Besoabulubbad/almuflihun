package app.almuflihun.buildlogic.quran

import java.io.File
import java.sql.DriverManager
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals

/** Builds the database from the real committed sources and checks what the app relies on. */
class QuranDatabaseBuilderTest {

    private val sources = File(System.getProperty("almuflihun.quranSources"))
    private val db: File by lazy {
        createTempDirectory("quran").toFile().resolve("quran.db").also { out ->
            QuranDatabaseBuilder.build(
                QuranDatabaseBuilder.Sources(
                    tanzil = sources.resolve("quran-simple.txt"),
                    layout = sources.resolve("qpc-v2-15-lines.db"),
                    glyphWords = sources.resolve("qpc-v2.db"),
                    textWords = sources.resolve("digital-khatt-v2.db"),
                    metadata = sources.resolve("quran-data.js"),
                    roots = sources.resolve("word-root.db"),
                    lemmas = sources.resolve("word-lemma.db"),
                    stems = sources.resolve("word-stem.db"),
                ),
                out,
            )
        }
    }

    private fun scalar(sql: String): String = DriverManager.getConnection("jdbc:sqlite:${db.absolutePath}").use { c ->
        c.createStatement().use { s -> s.executeQuery(sql).use { rs -> rs.next(); rs.getString(1) } }
    }

    @Test
    fun `counts match the printed mushaf`() {
        assertEquals("604", scalar("SELECT COUNT(DISTINCT page) FROM line"))
        assertEquals("6236", scalar("SELECT COUNT(*) FROM ayah"))
        assertEquals("6236", scalar("SELECT COUNT(*) FROM word WHERE is_end = 1"))
        assertEquals("77432", scalar("SELECT COUNT(*) FROM word WHERE is_end = 0"))
        assertEquals("114", scalar("SELECT COUNT(*) FROM line WHERE kind = 'surah'"))
    }

    @Test
    fun `known positions are where the print puts them`() {
        assertEquals("1", scalar("SELECT page FROM ayah WHERE surah = 1 AND ayah = 1"))
        assertEquals("2", scalar("SELECT page FROM ayah WHERE surah = 2 AND ayah = 1"))
        assertEquals("42", scalar("SELECT page FROM ayah WHERE surah = 2 AND ayah = 255"))
        assertEquals("604", scalar("SELECT page FROM ayah WHERE surah = 114 AND ayah = 1"))
    }

    @Test
    fun `every word and glyph is copied byte for byte from its source`() {
        val attach = "ATTACH '${sources.resolve("qpc-v2.db").absolutePath}' AS g; " +
            "ATTACH '${sources.resolve("digital-khatt-v2.db").absolutePath}' AS t"
        DriverManager.getConnection("jdbc:sqlite:${db.absolutePath}").use { c ->
            c.createStatement().use { s ->
                attach.split("; ").forEach { s.execute(it) }
                val sql = "SELECT COUNT(*) FROM word w JOIN g.words gw ON gw.id = w.id JOIN t.words tw ON tw.id = w.id " +
                    "WHERE w.glyph = gw.text AND w.text = tw.text AND w.surah = tw.surah AND w.ayah = tw.ayah"
                s.executeQuery(sql).use { rs ->
                    rs.next()
                    assertEquals(83668, rs.getInt(1))
                }
            }
        }
    }

    @Test
    fun `surahs, quarters and sajdas come from Tanzil's metadata`() {
        assertEquals("114", scalar("SELECT COUNT(*) FROM surah"))
        assertEquals("6236", scalar("SELECT SUM(ayahs) FROM surah"))
        assertEquals("286|medinan|87", scalar("SELECT ayahs || '|' || place || '|' || revelation_order FROM surah WHERE number = 2"))
        assertEquals("240", scalar("SELECT COUNT(*) FROM quarter"))
        assertEquals("2:142", scalar("SELECT surah || ':' || ayah FROM quarter WHERE number = 9")) // juz 2
        assertEquals("15", scalar("SELECT COUNT(*) FROM sajda"))
        assertEquals("0", scalar("SELECT COUNT(*) FROM pragma_table_info('sajda') WHERE name = 'kind'"))
    }

    @Test
    fun `repeated phrases are found from the text alone`() {
        assertEquals("2181", scalar("SELECT COUNT(DISTINCT phrase) FROM phrase_place"))
        assertEquals("6459", scalar("SELECT COUNT(*) FROM phrase_place"))
        assertEquals("24", scalar("SELECT MAX(last_word - first_word + 1) FROM phrase_place"))
        assertEquals("0", scalar("SELECT COUNT(*) FROM (SELECT phrase FROM phrase_place GROUP BY phrase HAVING COUNT(DISTINCT surah * 1000 + ayah) < 2)"))
        assertEquals("89", scalar("SELECT MAX(n) FROM (SELECT COUNT(*) n FROM phrase_place GROUP BY phrase)"))
    }

    @Test
    fun `each word carries its root, lemma and stem`() {
        assertEquals("1642", scalar("SELECT COUNT(*) FROM root"))
        assertEquals("50298", scalar("SELECT COUNT(root) FROM word"))
        assertEquals("72507", scalar("SELECT COUNT(lemma) FROM word")) // three marker links dropped
        assertEquals("77427", scalar("SELECT COUNT(stem) FROM word"))
        assertEquals("0", scalar("SELECT COUNT(*) FROM word WHERE is_end = 1 AND (root IS NOT NULL OR lemma IS NOT NULL OR stem IS NOT NULL)"))
        assertEquals("0", scalar("SELECT COUNT(*) FROM root WHERE letters LIKE '% %'"))
    }

    @Test
    fun `chosen roots match published concordance counts`() {
        val counts = mapOf("صبر" to 103, "رحم" to 339, "علم" to 854, "كتب" to 319, "قول" to 1722, "ذكر" to 292, "امر" to 248, "نهي" to 56)
        counts.forEach { (letters, expected) ->
            assertEquals(expected.toString(), scalar("SELECT COUNT(*) FROM word w JOIN root r ON r.id = w.root WHERE r.letters = '$letters'"), letters)
        }
    }

    @Test
    fun `the text is copied, never edited`() {
        val line = sources.resolve("quran-simple.txt").readLines().first { it.startsWith("2|255|") }
        assertEquals(line.removePrefix("2|255|"), scalar("SELECT simple FROM ayah WHERE surah = 2 AND ayah = 255"))
        assertEquals("4", scalar("SELECT value FROM meta WHERE key = 'schema_version'"))
        assertEquals("1", scalar("PRAGMA user_version"))
        val digest = java.security.MessageDigest.getInstance("SHA-256").digest(db.readBytes())
        assertEquals(digest.joinToString("") { "%02x".format(it) }, db.resolveSibling("quran.version").readText())
    }
}
