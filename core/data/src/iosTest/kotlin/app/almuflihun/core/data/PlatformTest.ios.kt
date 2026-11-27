package app.almuflihun.core.data

import app.almuflihun.core.database.UserDatabase
import app.cash.sqldelight.driver.native.inMemoryDriver

actual abstract class PlatformTest actual constructor()

internal actual fun inMemoryUserDatabase(): UserDatabase = UserDatabase(inMemoryDriver(UserDatabase.Schema))
