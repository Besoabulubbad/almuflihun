package app.qurancommunity.core.testing

import androidx.compose.runtime.Composable

/**
 * One visual state to screenshot, written once in a module's `commonTest` and rendered by both platform runners:
 * [ScreenshotSuite] on Android (Robolectric) and `verifySnapshots` on iOS (simulator).
 *
 * Images are `<name>_<variant>.png` under `<module>/screenshots/<androidHostTest|iosSimulatorArm64>/`. Every case runs
 * in `PreviewVariants.Four`; [fullMatrix] adds the 320 dp width on Android for a default state.
 */
class SnapshotCase(
    val name: String,
    val fullMatrix: Boolean = false,
    val content: @Composable () -> Unit,
) {
    override fun toString(): String = name
}
