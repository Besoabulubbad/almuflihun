package app.almuflihun.app.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** Hosts [App] in the iOS shell (`iosApp/`). */
@Suppress("FunctionNaming") // Swift calls it as a factory: MainViewControllerKt.MainViewController()
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
