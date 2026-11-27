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
    fun `the text is copied, never edited`() {
        val line = sources.resolve("quran-simple.txt").readLines().first { it.startsWith("2|255|") }
        assertEquals(line.removePrefix("2|255|"), scalar("SELECT simple FROM ayah WHERE surah = 2 AND ayah = 255"))
        assertEquals("1", scalar("SELECT value FROM meta WHERE key = 'schema_version'"))
        assertEquals("1", scalar("PRAGMA user_version"))
        val digest = java.security.MessageDigest.getInstance("SHA-256").digest(db.readBytes())
        assertEquals(digest.joinToString("") { "%02x".format(it) }, db.resolveSibling("quran.version").readText())
    }
}
