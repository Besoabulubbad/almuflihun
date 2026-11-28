package app.qurancommunity.core.data

import app.cash.sqldelight.driver.native.inMemoryDriver
import app.qurancommunity.core.database.UserDatabase

actual abstract class PlatformTest actual constructor()

internal actual fun inMemoryUserDatabase(): UserDatabase = UserDatabase(inMemoryDriver(UserDatabase.Schema))
