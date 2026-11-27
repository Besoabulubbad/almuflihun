package app.almuflihun.core.quran

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// design/data/diyaa-food.json as delivered; blocks are told apart by their "type".

@Serializable
internal data class DiyaaFile(val meta: DiyaaMetaDto, val stations: List<StationDto>, val summary: List<SummaryDto>)

@Serializable
internal data class DiyaaMetaDto(val title: String, val intro: String)

@Serializable
internal data class StationDto(
    val n: Int,
    val title: String,
    val illustration: String,
    val tint: String,
    val blocks: List<BlockDto>,
)

@Serializable
internal data class SummaryDto(val text: String, val illustration: String)

@Serializable
internal data class WordsDto(val key: String, val words: List<Int>)

@Serializable
internal sealed interface BlockDto {
    @Serializable
    @SerialName("verse")
    data class Verse(
        val keys: List<String>,
        val highlight: List<String> = emptyList(),
        @SerialName("highlight_words") val highlightWords: List<WordsDto> = emptyList(),
    ) : BlockDto

    @Serializable
    @SerialName("text")
    data class Text(val level: String, val text: String) : BlockDto

    @Serializable
    @SerialName("lang")
    data class Lang(val level: String, val word: String, val meaning: String) : BlockDto

    @Serializable
    @SerialName("rule")
    data class Rule(val label: String, val text: String) : BlockDto

    @Serializable
    @SerialName("heading")
    data class Heading(val text: String) : BlockDto

    @Serializable
    @SerialName("trait_cards")
    data class TraitCards(val items: List<TraitDto>) : BlockDto

    @Serializable
    @SerialName("contrast")
    data class Contrast(val a: String, val b: String) : BlockDto

    @Serializable
    @SerialName("animal_pairs")
    data class AnimalPairs(val items: List<AnimalDto>) : BlockDto

    @Serializable
    @SerialName("compare")
    data class Compare(val items: List<CompareDto>) : BlockDto

    @Serializable
    @SerialName("bullets")
    data class Bullets(val items: List<String>) : BlockDto

    @Serializable
    @SerialName("count_bars")
    data class CountBars(val unit: String, val items: List<CountDto>) : BlockDto

    @Serializable
    @SerialName("steps")
    data class Steps(val items: List<String>) : BlockDto

    @Serializable
    @SerialName("word_count")
    data class WordCount(val word: String, val text: String, val items: List<PlaceDto>) : BlockDto

    @Serializable
    @SerialName("numbered_list")
    data class NumberedList(val title: String, val items: List<String>) : BlockDto

    @Serializable
    @SerialName("closing_quote")
    data class ClosingQuote(val quote: String, val ref: String) : BlockDto
}

@Serializable
internal data class TraitDto(val word: String, val meaning: String, val refs: String)

@Serializable
internal data class AnimalDto(val name: String, val illustration: String, val label: String)

@Serializable
internal data class CompareDto(
    val about: String,
    val illustration: String? = null,
    val quote: String? = null,
    val ref: String? = null,
    val quotes: List<String> = emptyList(),
)

@Serializable
internal data class CountDto(val name: String, val count: Int, val illustration: String)

@Serializable
internal data class PlaceDto(val about: String, val ref: String)
