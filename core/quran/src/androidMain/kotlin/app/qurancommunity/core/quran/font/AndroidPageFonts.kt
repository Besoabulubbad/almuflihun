package app.qurancommunity.core.quran.font

import android.content.Context
import java.io.File

/** Page fonts in `noBackupFilesDir/mushaf/<edition>`: rebuilt by download, so never in a backup. */
internal class AndroidFontFiles(private val directory: File) : FontFiles {

    constructor(context: Context, edition: String) : this(File(context.noBackupFilesDir, "mushaf/$edition"))

    override fun read(name: String): ByteArray? = File(directory, name).takeIf { it.isFile }?.readBytes()

    override fun exists(name: String): Boolean = File(directory, name).isFile

    override fun write(name: String, bytes: ByteArray) {
        directory.mkdirs()
        val partial = File(directory, "$name.partial")
        partial.writeBytes(bytes)
        if (!partial.renameTo(File(directory, name))) {
            partial.delete()
            error("Could not store $name")
        }
    }

    override fun delete(name: String) {
        File(directory, name).delete()
    }
}

/**
 * Android draws only TTF/OTF, so a page's WOFF is inflated to a TTF the first time the page is drawn and kept in a
 * small cache of recent pages (ADR 0016). The cache is rebuilt from the stored WOFF if the system clears it.
 */
class AndroidPageFontCache internal constructor(
    private val store: PageFontStore,
    private val directory: File,
    private val keep: Int = DEFAULT_KEEP,
) {
    constructor(context: Context, store: PageFontStore) : this(store, File(context.cacheDir, "mushaf-ttf"))

    /** The page's TTF file, or null when its font is not downloaded (draw the fallback). */
    @Synchronized
    fun ttf(page: Int): File? {
        PageFontStore.requirePage(page)
        val file = File(directory, "p$page.ttf")
        if (file.isFile) {
            file.setLastModified(System.currentTimeMillis())
            return file
        }
        val font = decodeStored(page) ?: return null
        directory.mkdirs()
        val partial = File(directory, "p$page.ttf.partial")
        partial.writeBytes(font.bytes)
        partial.renameTo(file)
        prune()
        return file
    }

    /** The stored page decoded; a file damaged after it was stored is deleted so it downloads again. */
    private fun decodeStored(page: Int): Sfnt? {
        val woff = store.read(page) ?: return null
        return runCatching { Woff.decode(woff) }.getOrElse {
            store.delete(page)
            null
        }
    }

    private fun prune() {
        directory.listFiles { f -> f.name.endsWith(".ttf") }.orEmpty()
            .sortedByDescending { it.lastModified() }
            .drop(keep)
            .forEach { it.delete() }
    }

    private companion object {
        const val DEFAULT_KEEP = 30
    }
}
