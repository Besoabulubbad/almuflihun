package app.almuflihun.core.astronomy

import org.koin.core.module.Module
import org.koin.dsl.module

/** Koin module: the [Sky] on Astronomy Engine. */
val astronomyModule: Module = module {
    single<Sky> { EngineSky() }
}
