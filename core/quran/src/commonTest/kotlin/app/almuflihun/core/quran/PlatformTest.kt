package app.almuflihun.core.quran

/** Runs common tests on the platform's real storage and SQLite: Robolectric on Android, the simulator on iOS. */
expect abstract class PlatformTest()

/** A fresh, empty storage location for one test. */
internal expect fun newTestStorage(): QuranStorage
