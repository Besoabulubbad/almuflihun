package app.almuflihun.feature.home

import app.almuflihun.core.navigation.SettingsKey
import app.almuflihun.core.ui.ScreenViewModel

class HomeViewModel : ScreenViewModel<HomeUiState, HomeIntent, HomeEffect>(HomeUiState()) {
    override fun handle(intent: HomeIntent) = when (intent) {
        HomeIntent.OpenSettings -> sendEffect(HomeEffect.Navigate(SettingsKey))
    }
}
