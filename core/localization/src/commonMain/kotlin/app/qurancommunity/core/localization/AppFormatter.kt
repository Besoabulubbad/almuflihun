package app.qurancommunity.core.localization

/**
 * Every number, range, list and verse reference shown in the UI goes through here, in the UI [language] (DESIGN §2,
 * §8; ADR 0014). Arabic: Arabic-Indic digits, «٬» thousands, «،» between parts, «سورة البقرة ١٧٢». English (later):
 * Western digits, ",", "Al-Baqarah 172". Surah names come from the content data in the right language; this class only
 * formats.
 */
class AppFormatter(val language: AppLanguage) {

    /** Digits only, no grouping: page numbers, ayah numbers, years. */
    fun digits(value: Long): String = localiseDigits(value.toString())

    fun digits(value: Int): String = digits(value.toLong())

    /** A count or amount with thousands grouping: «١٢٬٣٤٥» / "12,345". */
    fun number(value: Long): String {
        val negative = value < 0
        val grouped = kotlin.math.abs(value).toString().reversed().chunked(GROUP).joinToString(thousands).reversed()
        return (if (negative) "-" else "") + localiseDigits(grouped)
    }

    fun number(value: Int): String = number(value.toLong())

    /** A range with an en dash and no spaces: «١٤٣–١٤٤» / "143–144" (DESIGN §2). */
    fun range(from: Int, to: Int): String = "${digits(from)}$EN_DASH${digits(to)}"

    /** Parts of one meta line: «تلاوة، يوتيوب» / "Recitation, YouTube" (DESIGN §8: never «·» beside numbers). */
    fun parts(vararg parts: String): String = parts.joinToString(listSeparator)

    /** «البقرة ١٧٢» / "Al-Baqarah 172": the short reference (cards, chips). */
    fun reference(surahName: String, ayah: Int): String = "$surahName ${digits(ayah)}"

    /** «البقرة ١٤٣–١٤٤»: a reference to consecutive ayahs. */
    fun reference(surahName: String, fromAyah: Int, toAyah: Int): String =
        if (fromAyah == toAyah) reference(surahName, fromAyah) else "$surahName ${range(fromAyah, toAyah)}"

    /** «سورة البقرة ١٧٢»: the full reference under a verse card. */
    fun fullReference(surahName: String, ayah: Int): String = "$surahPrefix $surahName ${digits(ayah)}".trimStart()

    /** A whole percentage: «٩٥٪» / "95%". */
    fun percent(value: Int): String = when (language) {
        AppLanguage.Arabic -> "${digits(value)}$ARABIC_PERCENT"
        AppLanguage.English -> "${digits(value)}%"
    }

    /**
     * Wraps text of the other direction in Unicode isolates (FSI … PDI), so an inserted value never reorders the
     * sentence around it, e.g. an English name inside Arabic copy.
     */
    fun isolate(text: String): String = "$FSI$text$PDI"

    private val thousands: String
        get() = if (language == AppLanguage.Arabic) ARABIC_THOUSANDS else ","

    private val listSeparator: String
        get() = if (language == AppLanguage.Arabic) "${ARABIC_COMMA} " else ", "

    private val surahPrefix: String
        get() = if (language == AppLanguage.Arabic) ARABIC_SURAH else ""

    private fun localiseDigits(text: String): String = when (language) {
        AppLanguage.Arabic -> buildString(text.length) {
            text.forEach { c -> append(if (c in '0'..'9') ARABIC_INDIC_ZERO + (c - '0') else c) }
        }
        AppLanguage.English -> text
    }

    private companion object {
        const val GROUP = 3
        const val EN_DASH = '–'
        const val ARABIC_INDIC_ZERO = '٠'
        const val ARABIC_THOUSANDS = "٬"
        const val ARABIC_COMMA = '،'
        const val ARABIC_PERCENT = '٪'
        const val ARABIC_SURAH = "سورة"
        const val FSI = '⁨'
        const val PDI = '⁩'
    }
}
