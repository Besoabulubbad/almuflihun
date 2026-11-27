package app.almuflihun.core.model

/** An ayah's address: surah 1–114 and its ayah number, written `2:255`. */
data class AyahKey(val surah: Int, val ayah: Int) {
    init {
        require(surah in 1..SURAH_COUNT) { "Surah $surah is not 1..$SURAH_COUNT" }
        require(ayah >= 1) { "Ayah $ayah is not positive" }
    }

    override fun toString(): String = "$surah:$ayah"

    companion object {
        const val SURAH_COUNT = 114
    }
}

/**
 * One word of the printed Mushaf as the layout places it. [glyph] is its code in the exact page font (ADR 0013);
 * [text] is the same word as Unicode Uthmani text, for the Digital Khatt fallback, copying and screen readers. The
 * ayah-end marker is a word too, with [isAyahEnd].
 */
data class QuranWord(
    val id: Int,
    val ayah: AyahKey,
    val position: Int,
    val glyph: String,
    val text: String,
    val isAyahEnd: Boolean,
)

/** A line of a Mushaf page, numbered from 1 at the top. */
sealed interface MushafLine {
    val number: Int

    data class SurahTitle(override val number: Int, val surah: Int) : MushafLine
    data class Basmala(override val number: Int) : MushafLine
    data class Words(override val number: Int, val centered: Boolean, val words: List<QuranWord>) : MushafLine
}

/** A page of مصحف المدينة: 15 lines, 8 on pages 1 and 2. */
data class MushafPage(val number: Int, val lines: List<MushafLine>) {
    companion object {
        const val COUNT = 604
    }
}

/**
 * An ayah: its page, its words in order (the last is the ayah-end marker) and the Tanzil simple text, exactly as
 * shipped. [tanzilText] is for matching and search; it is not normalised (the first ayah of most surahs starts with
 * the basmala) and is never shown as is.
 */
data class Ayah(val key: AyahKey, val page: Int, val words: List<QuranWord>, val tanzilText: String)

/**
 * Words [first]..[last] (1-based, inclusive) of an ayah, counted in the Tanzil text without pause marks or the
 * leading basmala. A quote is a placed range: the app shows the shipped words in it, with its full verse (DESIGN §1).
 */
data class WordRange(val key: AyahKey, val first: Int, val last: Int) {
    init {
        require(first in 1..last) { "Bad range $first..$last in $key" }
    }
}

/**
 * The font a Mushaf page is drawn with (ADR 0013, 0016): the exact printed page when its font is on the device,
 * otherwise Digital Khatt, which draws the same words from their Unicode text.
 */
sealed interface PageFont {
    /** The page's own font: a TTF file on Android ([file]), the WOFF bytes on iOS ([bytes]). */
    class Exact(val page: Int, val file: String? = null, val bytes: ByteArray? = null) : PageFont {
        init {
            require(file != null || bytes != null) { "An exact page font needs a file or bytes" }
        }
    }

    /** Digital Khatt, bundled; shown with the quiet note that the exact page is on its way. */
    data object Fallback : PageFont
}

/** What to draw for this word in [font]: its glyph code in the page's own font, or its Unicode text. */
fun QuranWord.drawnWith(font: PageFont): String = if (font is PageFont.Exact) glyph else text

/** Where a surah was revealed (Tanzil metadata). */
enum class RevelationPlace { Meccan, Medinan }

/** A surah: its Arabic name, length, place and order of revelation (Tanzil, CC BY 3.0), and its first page. */
data class Surah(
    val number: Int,
    val name: String,
    val ayahCount: Int,
    val place: RevelationPlace,
    val revelationOrder: Int,
    val firstPage: Int,
)

/**
 * Where an ayah sits in the Mushaf's divisions. [quarter] is 1–4 within the [hizb]. [sajda] says the printed Mushaf
 * marks a prostration here; whether it is obligatory is a ruling, not the text, and is not given (DESIGN §1).
 */
data class AyahPosition(
    val key: AyahKey,
    val page: Int,
    val juz: Int,
    val hizb: Int,
    val quarter: Int,
    val sajda: Boolean,
)

/**
 * A phrase this ayah shares with other ayahs (ADR 0012, S4): at least four consecutive words, found from the text
 * alone. [here] is where it is in this ayah; [elsewhere] every other place it appears, in Mushaf order.
 */
data class SharedPhrase(val here: WordRange, val elsewhere: List<WordRange>) {
    val words: Int get() = here.last - here.first + 1
}
