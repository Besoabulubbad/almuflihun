package app.almuflihun.app.shared

import android.content.Context
import app.almuflihun.core.analytics.AnalyticsConsent
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test

/** The Koin graph has no holes: every definition's dependencies are declared in [appModules] (Koin `verify()`). */
class AppModulesTest {

    @Test
    fun `every dependency in the graph is declared`() {
        // The application Context is registered by app:android at start-up, not by a module.
        module { includes(appModules) }.verify(
            // Analytics takes its initial consent as a defaulted enum, not a definition.
            extraTypes = listOf(Context::class, AnalyticsConsent::class),
        )
    }
}
