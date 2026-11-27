package app.almuflihun.core.quran

import app.almuflihun.core.common.AppResult
import app.almuflihun.core.common.DispatcherProvider
import app.almuflihun.core.common.runCatchingApp
import app.almuflihun.core.model.Ayah
import app.almuflihun.core.model.AyahKey
import app.almuflihun.core.model.Door
import app.almuflihun.core.model.DoorCard
import app.almuflihun.core.model.DoorEntry
import app.almuflihun.core.model.DoorLayout
import app.almuflihun.core.model.DoorPoint
import app.almuflihun.core.model.EvidenceItem
import app.almuflihun.core.model.EvidenceSection
import app.almuflihun.core.model.Hizb
import app.almuflihun.core.model.Passage
import app.almuflihun.core.model.PassageStep
import app.almuflihun.core.model.Slide
import app.almuflihun.core.model.WordRange
import app.almuflihun.core.quran.resources.Res
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

/** The curated content (design/data), with every quote placed in the shipped text. */
interface ContentReader {
    /** All doors in their order; only `visible` ones are shown for now. */
    suspend fun doors(): AppResult<List<Door>>

    /** The «الذكر» slider, in order. */
    suspend fun slides(): AppResult<List<Slide>>

    /** The 60 hizbs. */
    suspend fun hizbs(): AppResult<List<Hizb>>
}

/**
 * Reads the bundled content once and places each quote with [QuotePlacer]. A quote that does not place is left out
 * (the screen never shows it) and reported to [onMismatch], for the `content_mismatch` non-fatal (tracking plan Q15).
 * The build already proves the shipped content has none (`ContentPlacementTest`).
 */
internal class BundledContentReader(
    private val quran: QuranReader,
    private val dispatchers: DispatcherProvider,
    private val onMismatch: (String) -> Unit = {},
) : ContentReader {

    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private val words = mutableMapOf<AyahKey, List<String>>()
    private var basmala: List<String>? = null
    private var doors: List<Door>? = null

    override suspend fun doors(): AppResult<List<Door>> = load {
        doors ?: json.decodeFromString<DoorsFile>(read("abwab-4.json")).abwab
            .sortedBy { it.order }
            .map { it.toDoor() }
            .also { doors = it }
    }

    override suspend fun slides(): AppResult<List<Slide>> = load {
        json.decodeFromString<SliderFile>(read("dhikr-slider.json")).items.sortedBy { it.order }.mapNotNull { s ->
            place("slider ${s.id}", s.key, s.quote, s.words)?.let { Slide(s.id, s.order, s.door, s.entry, it) }
        }
    }

    override suspend fun hizbs(): AppResult<List<Hizb>> = load {
        json.decodeFromString<HizbsFile>(read("hizb-60.json")).hizbs.map { h ->
            Hizb(h.hizb, h.juz, key(h.start), key(h.end), h.quarters.map(::key))
        }
    }

    /** One load at a time, so the caches are never written from two coroutines at once. */
    private suspend fun <T> load(block: suspend () -> T): AppResult<T> = withContext(dispatchers.io) {
        mutex.withLock { runCatchingApp { block() } }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun read(file: String): String = Res.readBytes("files/content/$file").decodeToString()

    private suspend fun DoorDto.toDoor(): Door = Door(
        id = id,
        order = order,
        title = title,
        word = word,
        summary = summary,
        cards = cards.mapNotNull { c -> place("door $id card", c.ref, c.text, null)?.let { DoorCard(c.label, it) } },
        entries = entries.map { e ->
            DoorEntry(
                id = e.id,
                ref = e.ref,
                verses = e.verses.map(::key),
                title = e.title,
                points = e.points.map { DoorPoint(it.label, it.text) },
                forHome = e.home,
            )
        },
        allVerses = allVerses.map(::key),
        allVersesNote = allVersesNote,
        visible = visible,
        layout = design?.toLayout(id),
    )

    private suspend fun DesignDto.toLayout(door: String): DoorLayout? = when (type) {
        EVIDENCE -> DoorLayout.Evidence(
            sections.map { s ->
                EvidenceSection(
                    title = s.title,
                    sub = s.sub,
                    items = s.items.mapNotNull { i ->
                        place("door $door ${i.entry}", i.key, i.quote, i.words)?.let {
                            EvidenceItem(i.entry, it, i.label, i.dua, i.visual)
                        }
                    },
                )
            },
        )
        PASSAGES -> DoorLayout.Passages(
            passages = passages.mapNotNull { p -> p.toPassage(door) },
            mentions = mentions,
            detailPassage = detail?.passage,
        )
        else -> {
            onMismatch("door $door: unknown layout $type")
            null
        }
    }

    private suspend fun PassageDto.toPassage(door: String): Passage? {
        val where = "door $door passage $id"
        val opening = place("$where opens", opens.key, opens.quote, opens.words) ?: return null
        return Passage(
            id = id,
            entry = entry,
            title = title,
            opens = opening,
            closes = closes?.let { place("$where closes", it.key, it.quote, it.words) },
            verdict = verdict?.let { place("$where verdict", it.key, it.quote, it.words) },
            steps = steps.mapNotNull { s ->
                place("$where step", s.key, s.quote, s.words)?.let { PassageStep(s.kind, s.speaker, it) }
            },
        )
    }

    /** The quote's placed range, or null (reported) when it does not match the shipped text. */
    private suspend fun place(where: String, key: String, quote: String, range: List<Int>?): WordRange? {
        val ayah = key(key)
        val declared = range?.let { it[0]..it[1] }
        return when (val result = QuotePlacer.place(ayah, wordsOf(ayah), quote, declared)) {
            is QuotePlacer.Result.Placed -> result.range
            is QuotePlacer.Result.Mismatch -> {
                onMismatch("$where: ${result.reason}")
                null
            }
        }
    }

    private suspend fun wordsOf(key: AyahKey): List<String> = words.getOrPut(key) {
        val opening = basmala ?: quran.ayah(AyahKey(1, 1)).text().split(' ').also { basmala = it }
        QuranText.words(key, quran.ayah(key).text(), opening)
    }

    private fun AppResult<Ayah>.text(): String = when (this) {
        is AppResult.Success -> value.tanzilText
        is AppResult.Failure -> error("Ayah unavailable: $error")
    }

    private fun key(text: String): AyahKey = text.split(':').let { AyahKey(it[0].toInt(), it[1].toInt()) }

    private companion object {
        const val EVIDENCE = "evidence"
        const val PASSAGES = "passages"
    }
}
