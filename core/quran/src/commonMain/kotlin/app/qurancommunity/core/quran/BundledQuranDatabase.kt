package app.qurancommunity.core.quran

import app.qurancommunity.core.quran.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * The read-only Quran database built at compile time from `core/quran/sources` (ADR 0015): text, words, glyphs and
 * the page layout. The app copies it to storage once per [readVersion] and opens the copy.
 */
internal object BundledQuranDatabase {

    const val FILE_NAME = "quran.db"

    /** The SHA-256 of the bundled file, written beside it by the build. */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun readVersion(): String = Res.readBytes("files/quran.version").decodeToString().trim()

    @OptIn(ExperimentalResourceApi::class)
    suspend fun readBytes(): ByteArray = Res.readBytes("files/$FILE_NAME")
}
