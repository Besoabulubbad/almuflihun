package app.almuflihun.app.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * The root composable for Android and iOS.
 *
 * A placeholder until Phase 2 brings the theme and Phase 6 the first screen (docs/plan.md). It exists so the
 * shells build and run end to end from the first commit of Phase 1.
 */
@Composable
fun App(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BasicText(text = "المفلحون")
    }
}
