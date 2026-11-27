package app.almuflihun.feature.home

import app.almuflihun.core.navigation.AppNavKey
import app.almuflihun.core.ui.UiEffect
import app.almuflihun.core.ui.UiIntent
import app.almuflihun.core.ui.UiState

/** Home's state. A skeleton until Phase 6 brings the moon, «تذكرة» and the portals (docs/specs/screens.md §1). */
data class HomeUiState(val ready: Boolean = true) : UiState

sealed interface HomeIntent : UiIntent {
    data object OpenSettings : HomeIntent
}

sealed interface HomeEffect : UiEffect {
    data class Navigate(val key: AppNavKey) : HomeEffect
}
