package app.almuflihun.core.database

import org.koin.core.module.Module

/** Koin module: the [UserDatabase] on the platform's SQLite driver. */
expect val databaseModule: Module

internal const val USER_DATABASE = "user.db"
