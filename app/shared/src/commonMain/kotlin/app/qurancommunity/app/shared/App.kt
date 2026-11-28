package app.qurancommunity.app.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.qurancommunity.core.designsystem.AppTheme

/** The root composable for Android and iOS: the theme around the navigation host. */
@Composable
fun App(modifier: Modifier = Modifier) {
    AppTheme {
        AppNavHost(modifier)
    }
}
