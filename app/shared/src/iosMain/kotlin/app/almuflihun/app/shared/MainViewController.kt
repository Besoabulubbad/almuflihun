package app.almuflihun.app.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

private var koinStarted = false

/** Hosts [App] in the iOS shell (`iosApp/`), starting Koin the first time. */
@Suppress("FunctionNaming") // Swift calls it as a factory: MainViewControllerKt.MainViewController()
fun MainViewController(): UIViewController {
    if (!koinStarted) {
        initKoin()
        koinStarted = true
    }
    return ComposeUIViewController { App() }
}
