package app.almuflihun.core.designsystem.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import app.almuflihun.core.designsystem.AppTheme

/**
 * Renders [content] exactly as one [PreviewVariant]: theme and direction from [AppTheme], the font scale through
 * `LocalDensity`, the width as a hard constraint, on the paper ground. Used by every preview and by the screenshot
 * runners, so previews and baselines are the same pixels.
 */
@Composable
fun AppPreview(variant: PreviewVariant, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, variant.fontScale)) {
        AppTheme(mode = variant.theme, language = variant.language) {
            Box(Modifier.width(variant.widthDp.dp).background(AppTheme.colors.paper.bg)) {
                content()
            }
        }
    }
}
