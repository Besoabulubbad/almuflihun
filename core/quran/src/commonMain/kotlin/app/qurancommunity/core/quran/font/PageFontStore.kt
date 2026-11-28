package app.qurancommunity.core.quran.font

import app.qurancommunity.core.common.AppError
import app.qurancommunity.core.common.AppResult
import app.qurancommunity.core.model.MushafPage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Where one edition's page fonts live on the device: app storage that is never backed up (ADR 0016). */
internal interface FontFiles {
    fun read(name: String): ByteArray?
    fun exists(name: String): Boolean

    /** Writes [bytes] to [name] whole or not at all (a temporary file, then a rename). */
    fun write(name: String, bytes: ByteArray)

    fun delete(name: String)
}

/**
 * The downloaded QPC page fonts (ADR 0013, 0016). A file is kept only if it is an intact WOFF and maps every glyph
 * code its page's words use; anything else is refused, so the Mushaf never draws from a damaged font.
 */
class PageFontStore internal constructor(
    private val files: FontFiles,
    private val requiredGlyphs: suspend (page: Int) -> Set<Int>,
) {
    private val savedPages = MutableSharedFlow<Int>(extraBufferCapacity = SAVED_BUFFER)

    /** Pages as they are saved, so a page on screen can switch to its exact font at once. */
    val saved: SharedFlow<Int> = savedPages.asSharedFlow()

    /** Checks [woff] for [page] and keeps it, or says why it was refused. */
    suspend fun save(page: Int, woff: ByteArray): AppResult<Unit> {
        requirePage(page)
        return try {
            verify(woff, requiredGlyphs(page))
            files.write(name(page), woff)
            savedPages.tryEmit(page)
            AppResult.Success(Unit)
        } catch (e: InvalidFontException) {
            AppResult.Failure(AppError.Validation("page $page: ${e.message}"))
        }
    }

    fun has(page: Int): Boolean = files.exists(name(page))

    /** The page's WOFF bytes (iOS draws them as they are), or null when not downloaded. */
    fun read(page: Int): ByteArray? = files.read(name(page))

    fun delete(page: Int) = files.delete(name(page))

    /** The pages already on the device. */
    fun present(): Set<Int> = (1..MushafPage.COUNT).filterTo(HashSet()) { has(it) }

    internal companion object {
        const val SAVED_BUFFER = 64

        fun name(page: Int) = "p$page.woff"

        fun requirePage(page: Int) = require(page in 1..MushafPage.COUNT) { "Page $page is not 1..${MushafPage.COUNT}" }

        /** Decodes [woff] (every table and checksum) and checks its character map covers [glyphs]. */
        fun verify(woff: ByteArray, glyphs: Set<Int>): Sfnt {
            val font = Woff.decode(woff)
            val cmap = font.tables["cmap"] ?: throw InvalidFontException("no character map")
            val missing = Cmap.missing(cmap, glyphs)
            if (missing.isNotEmpty()) {
                throw InvalidFontException("no glyph for ${missing.size} of the page's codes")
            }
            return font
        }
    }
}
