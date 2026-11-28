package app.qurancommunity.core.model

/** «الضياء»: a short journey through the verses on food (design/data/diyaa-food.json). */
data class Diyaa(val title: String, val intro: String, val stations: List<Station>, val summary: List<SummaryLine>)

/**
 * One station of the journey. [illustration] names a drawing in `design/illustrations`; [tint] is the station's
 * background as the design file gives it (to be mapped to a design token by the screen, never used raw).
 */
data class Station(
    val number: Int,
    val title: String,
    val illustration: String,
    val tint: String,
    val blocks: List<DiyaaBlock>,
)

/** A line of the closing summary, with its drawing. */
data class SummaryLine(val text: String, val illustration: String)

/** The kinds of block a station is built from (DESIGN, «الضياء»). Every quote in them is a placed [WordRange]. */
sealed interface DiyaaBlock {
    /** Full verses, with the placed [highlights] inside them. */
    data class Verses(val keys: List<AyahKey>, val highlights: List<WordRange>) : DiyaaBlock

    /** «تأمّل»: a reflection in the app's words (DESIGN §1, third level). */
    data class Note(val text: String) : DiyaaBlock

    /** «من اللغة»: what a word means in Arabic (DESIGN §1, second level). */
    data class Language(val word: String, val meaning: String) : DiyaaBlock

    data class Rule(val label: String, val text: String) : DiyaaBlock
    data class Heading(val text: String) : DiyaaBlock
    data class Traits(val items: List<Trait>) : DiyaaBlock
    data class Contrast(val first: String, val second: String) : DiyaaBlock
    data class AnimalPairs(val items: List<AnimalPair>) : DiyaaBlock

    /** Two things side by side, each with its placed quotes. */
    data class Compare(val items: List<CompareItem>) : DiyaaBlock

    data class Bullets(val items: List<String>) : DiyaaBlock

    /** How many verses name each thing; counts stay as delivered until the station 6 recount (docs/plan.md). */
    data class CountBars(val unit: String, val items: List<CountBar>) : DiyaaBlock

    data class Steps(val items: List<String>) : DiyaaBlock

    /** Where a word occurs, verse by verse. */
    data class WordCount(val word: String, val text: String, val items: List<WordPlace>) : DiyaaBlock

    data class NumberedList(val title: String, val items: List<String>) : DiyaaBlock
    data class ClosingQuote(val quote: WordRange) : DiyaaBlock
}

data class Trait(val word: String, val meaning: String, val refs: String)
data class AnimalPair(val name: String, val illustration: String, val label: String)
data class CompareItem(val about: String, val illustration: String?, val quotes: List<WordRange>)
data class CountBar(val name: String, val count: Int, val illustration: String)
data class WordPlace(val about: String, val key: AyahKey)
