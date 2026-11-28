package app.qurancommunity.core.quran

import app.cash.sqldelight.db.SqlDriver

/** Where the device keeps its copy of the bundled database: app storage that is never backed up. */
internal interface QuranStorage {
    /** The version written by the last complete [install], or null. */
    fun installedVersion(): String?

    /**
     * Replaces the copy with [database], then records [version]. The version is written last, so an interrupted
     * install is redone on the next launch.
     */
    fun install(database: ByteArray, version: String)

    /** Opens the installed copy. */
    fun openDriver(): SqlDriver
}
