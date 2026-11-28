package app.qurancommunity.core.quran

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// The shapes of design/data, as delivered. Only what the app uses is declared; the rest (meta, notes) is ignored.

@Serializable
internal data class DoorsFile(val abwab: List<DoorDto>)

@Serializable
internal data class DoorDto(
    val id: String,
    val order: Int,
    val title: String,
    val word: String,
    val summary: String,
    val cards: List<CardDto> = emptyList(),
    val entries: List<EntryDto> = emptyList(),
    @SerialName("all_verses") val allVerses: List<String> = emptyList(),
    @SerialName("all_verses_note") val allVersesNote: String? = null,
    val visible: Boolean = false,
    val design: DesignDto? = null,
)

@Serializable
internal data class CardDto(val label: String, val text: String, val ref: String)

@Serializable
internal data class EntryDto(
    val id: String,
    val ref: String,
    val verses: List<String>,
    val title: String,
    val points: List<PointDto> = emptyList(),
    val home: Boolean = false,
)

@Serializable
internal data class PointDto(val label: String, val text: String)

@Serializable
internal data class DesignDto(
    val type: String,
    val sections: List<SectionDto> = emptyList(),
    val passages: List<PassageDto> = emptyList(),
    val mentions: List<String> = emptyList(),
    val detail: DetailDto? = null,
)

@Serializable
internal data class DetailDto(val passage: String? = null)

@Serializable
internal data class SectionDto(val title: String, val sub: String? = null, val items: List<ItemDto> = emptyList())

@Serializable
internal data class ItemDto(
    val entry: String? = null,
    val key: String,
    val quote: String,
    val words: List<Int>,
    val label: String? = null,
    val dua: Boolean = false,
    val visual: String? = null,
)

@Serializable
internal data class QuoteDto(val key: String, val quote: String, val words: List<Int>)

@Serializable
internal data class PassageDto(
    val id: String,
    val entry: String,
    val title: String,
    val opens: QuoteDto,
    val closes: QuoteDto? = null,
    val verdict: QuoteDto? = null,
    val steps: List<StepDto> = emptyList(),
)

@Serializable
internal data class StepDto(
    val kind: String,
    val speaker: String? = null,
    val key: String,
    val quote: String,
    val words: List<Int>,
)

@Serializable
internal data class SliderFile(val items: List<SlideDto>)

@Serializable
internal data class SlideDto(
    val id: String,
    val order: Int,
    val door: String,
    val entry: String,
    val key: String,
    val quote: String,
    val words: List<Int>,
)

@Serializable
internal data class HizbsFile(val hizbs: List<HizbDto>)

@Serializable
internal data class HizbDto(val hizb: Int, val juz: Int, val start: String, val end: String, val quarters: List<String>)
