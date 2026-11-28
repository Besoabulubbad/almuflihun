package app.qurancommunity.core.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.qurancommunity.core.database.UserDatabase
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
actual abstract class PlatformTest actual constructor()

// A null name is an in-memory database.
internal actual fun inMemoryUserDatabase(): UserDatabase = UserDatabase(
    AndroidSqliteDriver(UserDatabase.Schema, InstrumentationRegistry.getInstrumentation().targetContext, null),
)
