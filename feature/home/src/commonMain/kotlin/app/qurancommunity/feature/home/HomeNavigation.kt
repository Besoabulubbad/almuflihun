package app.qurancommunity.feature.home

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import app.qurancommunity.core.navigation.HomeKey
import app.qurancommunity.core.navigation.Navigator
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Home's destination. */
fun EntryProviderScope<NavKey>.homeEntries(navigator: Navigator) {
    entry<HomeKey> { HomeRoute(navigator) }
}

/** Koin module: [HomeViewModel]. */
val homeModule: Module = module {
    viewModelOf(::HomeViewModel)
}
