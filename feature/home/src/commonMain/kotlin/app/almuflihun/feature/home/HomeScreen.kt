package app.almuflihun.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.almuflihun.core.designsystem.AppTheme
import app.almuflihun.feature.home.resources.Res
import app.almuflihun.feature.home.resources.home_app_name
import org.jetbrains.compose.resources.stringResource

/** Home, stateless. A skeleton on the night ground until Phase 6 builds the designed screen (HomeB). */
@Suppress("UnusedParameter") // the settings button and portals send intents once their screens exist (Phase 6)
@Composable
fun HomeScreen(state: HomeUiState, onIntent: (HomeIntent) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(AppTheme.colors.night.bg),
        contentAlignment = Alignment.Center,
    ) {
        if (state.ready) {
            BasicText(
                text = stringResource(Res.string.home_app_name),
                style = AppTheme.text.titleXL.copy(color = AppTheme.colors.night.text),
            )
        }
    }
}
