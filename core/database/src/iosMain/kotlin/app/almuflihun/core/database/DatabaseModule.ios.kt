package app.almuflihun.core.database

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

actual val databaseModule: Module = module {
    single { UserDatabase(NativeSqliteDriver(UserDatabase.Schema, USER_DATABASE)) }
}
