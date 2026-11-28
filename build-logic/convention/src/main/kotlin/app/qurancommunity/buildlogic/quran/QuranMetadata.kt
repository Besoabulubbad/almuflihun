package app.qurancommunity.buildlogic.quran

import java.io.File

/** Tanzil's quran-data.js (CC BY 3.0), read as data: only its array literals are parsed, nothing is executed. */
internal class QuranMetadata(
    val surahs: List<Surah>,
    val juzStarts: List<Pair<Int, Int>>,
    val quarterStarts: List<Pair<Int, Int>>,
    val sajdas: List<Sajda>,
) {
    class Surah(val start: Int, val ayahs: Int, val order: Int, val name: String, val place: String)
    class Sajda(val surah: Int, val ayah: Int)

    companion object {
        private const val SURAHS = 114
        private const val JUZ = 30
        private const val QUARTERS = 240
        private val NUMBER = Regex("-?\\d+")
        private val QUOTED = Regex("'([^']*)'|\"([^\"]*)\"")

        fun read(file: File): QuranMetadata {
            val js = file.readText(Charsets.UTF_8)
            val surahs = rows(js, "Sura").take(SURAHS).map { row ->
                val n = NUMBER.findAll(row.substringBefore('\'')).map { it.value.toInt() }.toList()
                val strings = QUOTED.findAll(row).map { it.groupValues[1].ifEmpty { it.groupValues[2] } }.toList()
                Surah(start = n[0], ayahs = n[1], order = n[2], name = strings[0], place = strings.last())
            }
            val juz = rows(js, "Juz").take(JUZ).map(::pair)
            val quarters = rows(js, "HizbQaurter").take(QUARTERS).map(::pair)
            val sajdas = rows(js, "Sajda").map { row ->
                val n = NUMBER.findAll(row).map { it.value.toInt() }.toList()
                Sajda(n[0], n[1])
            }
            check(surahs.size == SURAHS && juz.size == JUZ && quarters.size == QUARTERS) {
                "quran-data.js: ${surahs.size} surahs, ${juz.size} juz, ${quarters.size} quarters"
            }
            return QuranMetadata(surahs, juz, quarters, sajdas)
        }

        /** The data rows of `QuranData.<name> = [ … ];` (the leading empty row and comments skipped). */
        private fun rows(js: String, name: String): List<String> {
            val body = Regex("QuranData\\.$name = \\[(.*?)\\n];", RegexOption.DOT_MATCHES_ALL).find(js)
                ?.groupValues?.get(1) ?: error("quran-data.js has no $name")
            return Regex("\\[([^\\[\\]]*)]").findAll(body.lines().filterNot { it.trim().startsWith("//") }
                .joinToString("\n"))
                .map { it.groupValues[1] }
                .filter { NUMBER.containsMatchIn(it) }
                .toList()
        }

        private fun pair(row: String): Pair<Int, Int> =
            NUMBER.findAll(row).map { it.value.toInt() }.toList().let { it[0] to it[1] }
    }
}
