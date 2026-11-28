package app.qurancommunity.feature.home

import app.qurancommunity.core.navigation.SettingsKey
import app.qurancommunity.core.ui.ScreenViewModel

class HomeViewModel : ScreenViewModel<HomeUiState, HomeIntent, HomeEffect>(HomeUiState()) {
    override fun handle(intent: HomeIntent) = when (intent) {
        HomeIntent.OpenSettings -> sendEffect(HomeEffect.Navigate(SettingsKey))
    }
}
