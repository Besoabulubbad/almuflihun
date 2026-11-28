package app.qurancommunity.core.database

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

actual val databaseModule: Module = module {
    single { UserDatabase(AndroidSqliteDriver(UserDatabase.Schema, get(), USER_DATABASE)) }
}
