package app.almuflihun.core.model

import kotlin.time.Instant

/**
 * A door (باب): a word of the Quran and the places it occurs (design/data/abwab-4.json). Only [visible] doors are
 * shown for now (DESIGN, «الأبواب»). Every quote in it is already placed in the shipped text.
 */
data class Door(
    val id: String,
    val order: Int,
    val title: String,
    val word: String,
    val summary: String,
    val cards: List<DoorCard>,
    val entries: List<DoorEntry>,
    val allVerses: List<AyahKey>,
    val allVersesNote: String?,
    val visible: Boolean,
    val layout: DoorLayout?,
)

/** A labelled quote on the door's cover. */
data class DoorCard(val label: String, val quote: WordRange)

/**
 * One place in a door: its verses, a title, up to three [points] (fragments of the verse, written without marks) and
 * whether it may be «تذكرة اليوم» on Home. [ref] is the display reference, such as 13:21-22.
 */
data class DoorEntry(
    val id: String,
    val ref: String,
    val verses: List<AyahKey>,
    val title: String,
    val points: List<DoorPoint>,
    val forHome: Boolean,
)

data class DoorPoint(val label: String, val text: String)

/** How a visible door is laid out (DESIGN: «evidence» sections or the «passages» of الحكمة). */
sealed interface DoorLayout {
    data class Evidence(val sections: List<EvidenceSection>) : DoorLayout

    /** [mentions] are entries shown as other places of the word; [detailPassage] opens first. */
    data class Passages(
        val passages: List<Passage>,
        val mentions: List<String>,
        val detailPassage: String?,
    ) : DoorLayout
}

data class EvidenceSection(val title: String, val sub: String?, val items: List<EvidenceItem>)

/**
 * A quote shown large, then its full verse with the quote highlighted. [entry] links it to one of the door's places;
 * null for a supporting quote that is not one of them (three in الإنفاق).
 */
data class EvidenceItem(
    val entry: String?,
    val quote: WordRange,
    val label: String?,
    val isDua: Boolean,
    val visual: String?,
)

/** A run of verses read as one teaching, step by step (the Isra and Luqman passages). */
data class Passage(
    val id: String,
    val entry: String,
    val title: String,
    val opens: WordRange,
    val closes: WordRange?,
    val verdict: WordRange?,
    val steps: List<PassageStep>,
)

/** [kind] is the step's label in the design (لا، افعل، اعلم); [speaker] who speaks, when the text says so. */
data class PassageStep(val kind: String, val speaker: String?, val quote: WordRange)

/** A verse on the «الذكر» slider (design/data/dhikr-slider.json), taken from a door entry. */
data class Slide(val id: String, val order: Int, val door: String, val entry: String, val quote: WordRange)

/** One of the 60 hizbs, with the ayahs where its four quarters start. */
data class Hizb(val number: Int, val juz: Int, val start: AyahKey, val end: AyahKey, val quarters: List<AyahKey>)

/**
 * A collection of verses (ADR 0012, S1). [name] is null for the default «محفوظاتي» ([isDefault]), which the UI names
 * from its strings. [changedAt] is the last change to it or its verses, for «آخر تغيير».
 */
data class VerseCollection(
    val id: String,
    val name: String?,
    val isDefault: Boolean,
    val verseCount: Int,
    val changedAt: Instant,
)

/** A verse in a collection, in the reader's order: the whole verse, or the [quote] saved from a door. */
data class CollectedVerse(val key: AyahKey, val quote: WordRange?, val note: String?)
