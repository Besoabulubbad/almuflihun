package app.almuflihun.core.quran.font

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.memcpy

/** Page fonts in Application Support/mushaf/<edition>, excluded from iCloud and device backups. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class IosFontFiles(private val directory: String) : FontFiles {

    private val files = NSFileManager.defaultManager

    override fun read(name: String): ByteArray? {
        val data = NSData.dataWithContentsOfFile(path(name)) ?: return null
        val bytes = ByteArray(data.length.toInt())
        if (bytes.isNotEmpty()) bytes.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
        return bytes
    }

    override fun exists(name: String): Boolean = files.fileExistsAtPath(path(name))

    override fun write(name: String, bytes: ByteArray) {
        files.createDirectoryAtPath(directory, withIntermediateDirectories = true, attributes = null, error = null)
        NSURL.fileURLWithPath(directory).setResourceValue(true, NSURLIsExcludedFromBackupKey, null)
        val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
        check(data.writeToFile(path(name), atomically = true)) { "Could not store $name" }
    }

    override fun delete(name: String) {
        files.removeItemAtPath(path(name), null)
    }

    private fun path(name: String) = "$directory/$name"

    companion object {
        fun forEdition(edition: String): IosFontFiles {
            val base = NSFileManager.defaultManager.URLForDirectory(
                directory = NSApplicationSupportDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null,
            )
            return IosFontFiles(checkNotNull(base?.path) { "No Application Support directory" } + "/mushaf/$edition")
        }
    }
}
