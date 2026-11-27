package app.almuflihun.core.data

import app.almuflihun.core.database.UserDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
actual abstract class PlatformTest actual constructor()

// A null name is an in-memory database.
internal actual fun inMemoryUserDatabase(): UserDatabase =
    UserDatabase(AndroidSqliteDriver(UserDatabase.Schema, RuntimeEnvironment.getApplication(), null))
