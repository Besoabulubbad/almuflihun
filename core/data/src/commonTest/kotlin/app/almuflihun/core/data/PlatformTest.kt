package app.almuflihun.core.data

import app.almuflihun.core.database.UserDatabase

/** Runs common tests on the platform's real SQLite: Robolectric on Android, the simulator on iOS. */
expect abstract class PlatformTest()

/** A fresh in-memory user database. */
internal expect fun inMemoryUserDatabase(): UserDatabase
