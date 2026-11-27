package app.almuflihun.buildlogic.quran

import java.io.File
import java.sql.Connection

/**
 * QUL's word morphology (ADR 0012): the root, lemma and stem of each word, linked by `surah:ayah:word` in the layout's
 * numbering. Read as data, checked against the layout, then stored on the words.
 */
internal class Morphology(
    val roots: Map<Int, String>,
    val lemmas: Map<Int, Pair<String, String>>,
    val stems: Map<Int, Pair<String, String>>,
    val wordRoot: Map<String, Int>,
    val wordLemma: Map<String, Int>,
    val wordStem: Map<String, Int>,
) {
    companion object {
        /** Lemma links that point at ayah-end markers in the source: dropped, and any other such link fails. */
        val KNOWN_MARKER_LINKS = setOf("13:5:26", "2:275:46", "4:176:51")

        fun read(rootFile: File, lemmaFile: File, stemFile: File, open: (File) -> Connection): Morphology {
            val roots = open(rootFile).use { db ->
                db.rows("SELECT id, arabic_trilateral FROM roots") { it.getInt(1) to it.getString(2).filterNot(Char::isWhitespace) }
            }.toMap()
            val lemmas = open(lemmaFile).use { db ->
                db.rows("SELECT id, text, text_clean FROM lemmas") { it.getInt(1) to (it.getString(2) to it.getString(3)) }
            }.toMap()
            val stems = open(stemFile).use { db ->
                db.rows("SELECT id, text, text_clean FROM stems") { it.getInt(1) to (it.getString(2) to it.getString(3)) }
            }.toMap()
            return Morphology(
                roots = roots,
                lemmas = lemmas,
                stems = stems,
                wordRoot = links(rootFile, "root_words", "root_id", open),
                wordLemma = links(lemmaFile, "lemma_words", "lemma_id", open),
                wordStem = links(stemFile, "stem_words", "stem_id", open),
            )
        }

        private fun links(file: File, table: String, key: String, open: (File) -> Connection): Map<String, Int> {
            val rows = open(file).use { db -> db.rows("SELECT word_location, $key FROM $table") { it.getString(1) to it.getInt(2) } }
            val duplicated = rows.groupBy { it.first }.filterValues { it.size > 1 }.keys
            check(duplicated.isEmpty()) { "${file.name}: words with two entries: ${duplicated.take(5)}" }
            return rows.toMap()
        }

        private fun <T> Connection.rows(sql: String, map: (java.sql.ResultSet) -> T): List<T> =
            createStatement().use { st -> st.executeQuery(sql).use { rs -> buildList { while (rs.next()) add(map(rs)) } } }
    }

    /**
     * Checks every link names a real word of the layout ([words]: location → is an ayah-end marker) and a real entry,
     * and returns the links to store: the known marker links dropped.
     */
    fun checked(words: Map<String, Boolean>): Morphology {
        fun check(name: String, links: Map<String, Int>, entries: Set<Int>): Map<String, Int> {
            val unknown = links.keys.filter { it !in words }
            check(unknown.isEmpty()) { "$name links to words not in the layout: ${unknown.take(5)}" }
            val missing = links.values.filter { it !in entries }.toSet()
            check(missing.isEmpty()) { "$name links to entries that do not exist: ${missing.take(5)}" }
            val onMarkers = links.keys.filter { words.getValue(it) }.toSet()
            val allowed = if (name == "lemma") KNOWN_MARKER_LINKS else emptySet()
            check(onMarkers == allowed.intersect(onMarkers) && (name != "lemma" || onMarkers == allowed)) {
                "$name links on ayah-end markers: $onMarkers (known: $allowed)"
            }
            return links - onMarkers
        }
        return Morphology(
            roots, lemmas, stems,
            wordRoot = check("root", wordRoot, roots.keys),
            wordLemma = check("lemma", wordLemma, lemmas.keys),
            wordStem = check("stem", wordStem, stems.keys),
        )
    }
}
