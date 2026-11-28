package app.qurancommunity.core.quran

import app.qurancommunity.core.common.AppError
import app.qurancommunity.core.common.AppResult
import app.qurancommunity.core.common.DispatcherProvider
import app.qurancommunity.core.common.runCatchingApp
import app.qurancommunity.core.model.AyahKey
import app.qurancommunity.core.model.Door
import app.qurancommunity.core.model.DoorInSurah
import app.qurancommunity.core.model.DoorLayout
import app.qurancommunity.core.model.DoorMention
import app.qurancommunity.core.model.RootCount
import app.qurancommunity.core.model.RootUsage
import app.qurancommunity.core.model.SharedPhrase
import app.qurancommunity.core.model.StudyBasis
import app.qurancommunity.core.model.SurahOverview
import app.qurancommunity.core.model.WordForm
import app.qurancommunity.core.model.WordMorphology
import app.qurancommunity.core.model.WordOccurrence
import app.qurancommunity.core.model.WordRange
import app.qurancommunity.core.model.WordStudy
import kotlinx.coroutines.withContext

/** The study engine (ADR 0012): the text explaining itself. Grows with word study and roots in Phase 3b. */
interface QuranStudy {
    /**
     * The phrases this ayah shares with others, longest first, then the rarest: each with every other place it
     * appears. Empty when the ayah shares none.
     */
    suspend fun sharedPhrases(key: AyahKey): AppResult<List<SharedPhrase>>

    /** Each word's root, lemma and stem, in the Mushaf's word order. */
    suspend fun morphology(key: AyahKey): AppResult<List<WordMorphology>>

    /**
     * How often [root] occurs and in how many ayahs. [root] may be typed with spaces or hamza forms («ص ب ر»,
     * «أمن»); NotFound when no root has these letters.
     */
    suspend fun rootUsage(root: String): AppResult<RootUsage>

    /**
     * Word study for the word at [position] (1-based, in the Mushaf's word list) of [key], as tapped on the page:
     * its morphology and every occurrence of its root (else lemma, else stem). NotFound for no such word.
     */
    suspend fun wordStudy(key: AyahKey, position: Int): AppResult<WordStudy>

    /** Surah overview: facts, span, its [rootCount] most frequent roots, sajdas and the doors that quote it. */
    suspend fun surahOverview(surah: Int, rootCount: Int = TOP_ROOTS): AppResult<SurahOverview>

    /** The doors that include this ayah (entries, cards, quotes), in door order; hidden doors flagged. */
    suspend fun doorsQuoting(key: AyahKey): AppResult<List<DoorMention>>

    companion object {
        const val TOP_ROOTS = 10
    }
}

internal class SqlQuranStudy(
    private val provider: QuranDatabaseProvider,
    private val dispatchers: DispatcherProvider,
    private val index: QuranTextIndex = QuranTextIndex(provider, dispatchers),
    private val reader: QuranReader = SqlQuranReader(provider, dispatchers),
    private val content: ContentReader = BundledContentReader(reader, dispatchers),
) : QuranStudy {

    override suspend fun sharedPhrases(key: AyahKey): AppResult<List<SharedPhrase>> = withContext(dispatchers.io) {
        runCatchingApp {
            val queries = provider.get().quranQueries
            queries.phrasesInAyah(key.surah.toLong(), key.ayah.toLong()).executeAsList()
                .distinctBy { it.phrase } // a phrase twice in one ayah: shown once, at its first place
                .map { row ->
                    val here = WordRange(key, row.first_word.toInt(), row.last_word.toInt())
                    val elsewhere = queries.placesOfPhrase(row.phrase).executeAsList()
                        .map { place ->
                            val ayah = AyahKey(place.surah.toInt(), place.ayah.toInt())
                            WordRange(ayah, place.first_word.toInt(), place.last_word.toInt())
                        }
                        .filterNot { it == here }
                    SharedPhrase(here, elsewhere)
                }
        }
    }

    override suspend fun morphology(key: AyahKey): AppResult<List<WordMorphology>> = withContext(dispatchers.io) {
        runCatchingApp {
            provider.get().quranQueries
                .wordMorphology(key.surah.toLong(), key.ayah.toLong()) { position, root, lemma, stem ->
                    WordMorphology(position.toInt(), root, lemma, stem)
                }
                .executeAsList()
        }
    }

    override suspend fun wordStudy(key: AyahKey, position: Int): AppResult<WordStudy> = withContext(dispatchers.io) {
        val queries = provider.get().quranQueries
        val info = runCatchingApp {
            queries.wordInfo(key.surah.toLong(), key.ayah.toLong(), position.toLong()).executeAsOneOrNull()
        }
        when {
            info is AppResult.Failure -> info
            (info as AppResult.Success).value == null -> AppResult.Failure(AppError.NotFound("$key:$position"))
            else -> runCatchingApp {
                val word = info.value!!
                val (basis, rows) = when {
                    word.root != null -> StudyBasis.Root to queries.wordsOfRoot(word.root).executeAsList()
                        .map { Row(it.surah, it.ayah, it.position, it.text) }
                    word.lemma != null -> StudyBasis.Lemma to queries.wordsOfLemma(word.lemma).executeAsList()
                        .map { Row(it.surah, it.ayah, it.position, it.text) }
                    word.stem != null -> StudyBasis.Stem to queries.wordsOfStem(word.stem).executeAsList()
                        .map { Row(it.surah, it.ayah, it.position, it.text) }
                    else -> {
                        val self = Row(key.surah.toLong(), key.ayah.toLong(), position.toLong(), word.lemma_text)
                        StudyBasis.Word to listOf(self)
                    }
                }
                val occurrences = rows.mapNotNull { it.toOccurrence() }
                WordStudy(
                    key = key,
                    position = position,
                    text = word.text,
                    root = word.letters,
                    lemma = word.lemma_text,
                    stem = word.stem_text,
                    basis = basis,
                    occurrences = occurrences,
                    forms = occurrences.groupBy { it.lemma }
                        .map { (lemma, places) -> WordForm(lemma, places) }
                        .sortedByDescending { it.occurrences.size },
                )
            }
        }
    }

    override suspend fun surahOverview(surah: Int, rootCount: Int): AppResult<SurahOverview> = try {
        val info = reader.surahs().valueOrStop().firstOrNull { it.number == surah }
            ?: throw Stop(AppError.NotFound("surah $surah"))
        val firstJuz = reader.position(AyahKey(surah, 1)).valueOrStop().juz
        val lastJuz = reader.position(AyahKey(surah, info.ayahCount)).valueOrStop().juz
        val doors = content.doors().valueOrStop()
        withContext(dispatchers.io) {
            runCatchingApp {
                val queries = provider.get().quranQueries
                val span = queries.surahSpan(surah.toLong()).executeAsOne()
                SurahOverview(
                    surah = info,
                    pages = span.first_page!!.toInt()..span.last_page!!.toInt(),
                    juz = firstJuz..lastJuz,
                    wordCount = span.words!!.toInt(),
                    topRoots = queries.surahRoots(surah.toLong(), rootCount.toLong()).executeAsList()
                        .map { RootCount(it.letters, it.occurrences.toInt()) },
                    sajdas = queries.sajdasIn(surah.toLong()).executeAsList().map { AyahKey(surah, it.toInt()) },
                    doors = doors.mapNotNull { door -> door.inSurah(surah) },
                )
            }
        }
    } catch (stop: Stop) {
        AppResult.Failure(stop.error)
    }

    /** Carries a failed step's error out unchanged. */
    private class Stop(val error: AppError) : Exception()

    private fun <T> AppResult<T>.valueOrStop(): T = when (this) {
        is AppResult.Success -> value
        is AppResult.Failure -> throw Stop(error)
    }

    override suspend fun doorsQuoting(key: AyahKey): AppResult<List<DoorMention>> = try {
        val doors = content.doors().valueOrStop()
        AppResult.Success(doors.filter { key in it.ayahKeys() }.map { DoorMention(it.id, it.title, it.visible) })
    } catch (stop: Stop) {
        AppResult.Failure(stop.error)
    }

    /** The door's ayahs from [surah], in order. */
    private fun Door.inSurah(surah: Int): DoorInSurah? {
        val keys = ayahKeys().filter { it.surah == surah }.sortedBy { it.ayah }
        return if (keys.isEmpty()) null else DoorInSurah(id, title, visible, keys)
    }

    /** Every ayah a door includes: its entries' verses, its cards and every quote in its layout. */
    private fun Door.ayahKeys(): Set<AyahKey> {
        return buildSet {
            entries.forEach { entry -> addAll(entry.verses) }
            cards.forEach { add(it.quote.key) }
            when (val design = layout) {
                is DoorLayout.Evidence -> design.sections.forEach { section ->
                    section.items.forEach { add(it.quote.key) }
                }
                is DoorLayout.Passages -> design.passages.forEach { p ->
                    add(p.opens.key)
                    p.closes?.let { add(it.key) }
                    p.verdict?.let { add(it.key) }
                    p.steps.forEach { add(it.quote.key) }
                }
                null -> Unit
            }
        }
    }

    private class Row(val surah: Long, val ayah: Long, val position: Long, val lemma: String?)

    /** Placed on the content words the Mushaf word is written as (MushafAlignment). */
    private suspend fun Row.toOccurrence(): WordOccurrence? {
        val key = AyahKey(surah.toInt(), ayah.toInt())
        val group = index.ayah(key)?.groups?.getOrNull(position.toInt() - 1) ?: return null
        return WordOccurrence(WordRange(key, group.first + 1, group.last + 1), position.toInt(), lemma)
    }

    override suspend fun rootUsage(root: String): AppResult<RootUsage> = withContext(dispatchers.io) {
        val queries = provider.get().quranQueries
        val wanted = rootForm(root)
        val letters = runCatchingApp { queries.rootLetters().executeAsList().firstOrNull { rootForm(it) == wanted } }
        when {
            letters is AppResult.Failure -> letters
            (letters as AppResult.Success).value == null -> AppResult.Failure(AppError.NotFound(root))
            else -> runCatchingApp {
                val found = letters.value!!
                val usage = queries.rootUsage(found).executeAsOne()
                RootUsage(found, usage.occurrences.toInt(), usage.ayahs.toInt())
            }
        }
    }
}

/** Root letters for comparison: no spaces, letter forms unified (QuranText.searchForm). */
internal fun rootForm(root: String): String = QuranText.searchForm(root.filterNot(Char::isWhitespace))
