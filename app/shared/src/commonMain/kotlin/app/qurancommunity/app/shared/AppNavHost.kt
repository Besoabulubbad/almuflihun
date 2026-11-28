package app.qurancommunity.app.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import app.qurancommunity.core.navigation.HomeKey
import app.qurancommunity.core.navigation.Navigator
import app.qurancommunity.core.navigation.StackNavigator
import app.qurancommunity.core.navigation.appSavedStateConfiguration
import app.qurancommunity.feature.home.homeEntries

/**
 * Navigation 3 over one back stack that starts at Home and survives process death. Each entry keeps its own saved
 * state and its own ViewModel store, so a screen's ViewModel lives exactly as long as its entry.
 */
@Composable
internal fun AppNavHost(modifier: Modifier = Modifier) {
    val stack = rememberNavBackStack(appSavedStateConfiguration, HomeKey)
    val navigator = remember(stack) { StackNavigator(stack) }
    NavDisplay(
        backStack = stack,
        onBack = { navigator.back() },
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider { appEntries(navigator) },
    )
}

/** Every feature's entries. Each feature PR adds its `<name>Entries(navigator)` here. */
private fun EntryProviderScope<NavKey>.appEntries(navigator: Navigator) {
    homeEntries(navigator)
}
