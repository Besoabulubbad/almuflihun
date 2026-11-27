package app.almuflihun.core.domain

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Koin module: the use cases. */
val domainModule: Module = module {
    factoryOf(::GetAyahStudy)
}
