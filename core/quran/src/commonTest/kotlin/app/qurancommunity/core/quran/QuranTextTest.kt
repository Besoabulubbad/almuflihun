package app.qurancommunity.core.quran

import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.model.WordRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** The rules on made-up letters, so no Quran text is typed in code (CLAUDE.md). */
class QuranTextTest {

    private val fatha = 'َ'
    private val sukun = 'ْ'
    private val pause = "ۖ"

    @Test
    fun skeleton_drops_marks_and_keeps_letters() {
        assertEquals("ابت", QuranText.skeleton("ا${fatha}ب${sukun}ـت"))
    }

    @Test
    fun words_drop_pause_marks_and_a_leading_basmala_outside_al_fatiha_and_at_tawba() {
        val basmala = listOf("ب", "ت", "ث", "ج")
        val text = "ب ت ث ج د $pause ه"
        assertEquals(listOf("د", "ه"), QuranText.words(AyahKey(2, 1), text, basmala))
        assertEquals(listOf("ب", "ت", "ث", "ج", "د", "ه"), QuranText.words(AyahKey(9, 1), text, basmala))
        assertEquals(listOf("ب", "ت", "ث", "ج", "د", "ه"), QuranText.words(AyahKey(2, 2), text, basmala))
    }

    @Test
    fun a_declared_range_must_match_and_an_undeclared_quote_must_be_unique() {
        val key = AyahKey(3, 4)
        val words = listOf("ا", "ب", "ت", "ب", "ت")
        val marked = "ب" + fatha + " ت"
        assertEquals(QuotePlacer.Result.Placed(WordRange(key, 2, 3)), QuotePlacer.place(key, words, marked, 2..3))
        assertIs<QuotePlacer.Result.Mismatch>(QuotePlacer.place(key, words, "ب ت", 1..2))
        assertIs<QuotePlacer.Result.Mismatch>(QuotePlacer.place(key, words, "ب ت"))
        assertEquals(QuotePlacer.Result.Placed(WordRange(key, 1, 2)), QuotePlacer.place(key, words, "ا ب"))
        assertIs<QuotePlacer.Result.Mismatch>(QuotePlacer.place(key, words, "ا ب", 4..9))
    }

    @Test
    fun a_point_may_be_cut_inside_a_word() {
        val verses = listOf(listOf("با${fatha}لم", "ك"))
        assertEquals(true, QuotePlacer.containsFragment(verses, "الم"))
        assertEquals(false, QuotePlacer.containsFragment(verses, "لك"))
    }
}
