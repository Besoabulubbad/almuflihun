package app.qurancommunity.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.qurancommunity.core.navigation.Navigator
import app.qurancommunity.core.ui.CollectEffects
import org.koin.compose.viewmodel.koinViewModel

/** Home with its ViewModel and navigation; [HomeScreen] is the stateless part. */
@Composable
fun HomeRoute(navigator: Navigator, viewModel: HomeViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is HomeEffect.Navigate -> navigator.goTo(effect.key)
        }
    }
    HomeScreen(state = state, onIntent = viewModel::onIntent)
}
