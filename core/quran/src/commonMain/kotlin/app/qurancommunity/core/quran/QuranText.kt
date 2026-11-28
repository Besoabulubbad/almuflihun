package app.qurancommunity.core.quran

import app.qurancommunity.core.model.AyahKey

/**
 * Read-time normalisation of the Tanzil text (the file itself is never changed, ADR 0004). Content word ranges count
 * these words: the ayah's tokens without pause marks (ۚ ۖ …, ۞) and, in the first ayah of a surah other than al-Fatiha
 * and at-Tawba, without the leading basmala.
 */
object QuranText {

    private const val TATWEEL = 'ـ'
    private const val RUB_EL_HIZB = '۞'
    private val quranicMarks = 'ۖ'..'ۭ'
    private const val BASMALA_WORDS = 4

    /** Al-Fatiha (whose basmala is its first ayah) and at-Tawba (which has none). */
    private val SURAHS_WITHOUT_BASMALA_LINE = setOf(1, 9)

    /** The ayah's words as the content counts them, each exactly as shipped. */
    fun words(key: AyahKey, tanzilText: String, basmala: List<String>): List<String> {
        val tokens = tanzilText.split(' ').filter { token -> token.isNotEmpty() && !token.all(::isMarkOnly) }
        val opensWithBasmala = key.ayah == 1 && key.surah !in SURAHS_WITHOUT_BASMALA_LINE &&
            tokens.size > BASMALA_WORDS &&
            tokens.take(BASMALA_WORDS).map(::skeleton) == basmala.map(::skeleton)
        return if (opensWithBasmala) tokens.drop(BASMALA_WORDS) else tokens
    }

    /**
     * The letters only: vowel and other combining marks, tatweel and Quranic annotation marks removed. Two writings of
     * the same words with different marks (مِنْ / مِن) share a skeleton; different letters never do.
     */
    fun skeleton(text: String): String = buildString {
        text.forEach { c ->
            if (c.category != CharCategory.NON_SPACING_MARK && c != TATWEEL && !isMarkOnly(c)) append(c)
        }
    }

    /**
     * The form search compares (docs/specs/screens.md §11): the [skeleton] with letter forms unified, so a reader
     * typing without hamza or with a plain alef still finds the word: أ إ آ ٱ → ا، ى → ي، ة → ه، ؤ → و، ئ → ي, and a
     * standalone ء dropped. Applied the same way to the text and to the query.
     */
    fun searchForm(text: String): String = buildString {
        skeleton(text).forEach { c ->
            when (c) {
                in ALEF_FORMS -> append('ا')
                'ى', 'ئ' -> append('ي')
                'ة' -> append('ه')
                'ؤ' -> append('و')
                'ء' -> Unit
                else -> append(c)
            }
        }
    }

    private const val ALEF_FORMS = "أإآٱ"

    private fun isMarkOnly(c: Char): Boolean = c in quranicMarks || c == RUB_EL_HIZB
}
