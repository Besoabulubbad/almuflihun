package app.almuflihun.app.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.almuflihun.core.designsystem.AppTheme
import app.almuflihun.core.designsystem.tokens.Space

/**
 * The root composable for Android and iOS.
 *
 * Shows the app name on the night ground in the real theme until Phase 6 brings the first screen (docs/plan.md).
 */
@Composable
fun App(modifier: Modifier = Modifier) {
    AppTheme {
        Column(
            modifier = modifier.fillMaxSize().background(AppTheme.colors.night.bg),
            verticalArrangement = Arrangement.spacedBy(Space.s8, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicText(
                text = "المفلحون",
                style = AppTheme.text.titleXL.copy(color = AppTheme.colors.night.text),
            )
        }
    }
}
