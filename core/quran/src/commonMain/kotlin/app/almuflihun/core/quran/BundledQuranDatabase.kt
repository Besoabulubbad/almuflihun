package app.almuflihun.core.quran

import app.almuflihun.core.quran.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * The read-only Quran database built at compile time from `core/quran/sources` (ADR 0015): text, words, glyphs and
 * the page layout. The app copies it to storage once per [SCHEMA_VERSION] and opens it read-only.
 */
object BundledQuranDatabase {

    /** Matches `QuranDatabaseBuilder.SCHEMA_VERSION` in build-logic. */
    const val SCHEMA_VERSION = 1

    const val FILE_NAME = "quran.db"

    @OptIn(ExperimentalResourceApi::class)
    suspend fun readBytes(): ByteArray = Res.readBytes("files/$FILE_NAME")
}
