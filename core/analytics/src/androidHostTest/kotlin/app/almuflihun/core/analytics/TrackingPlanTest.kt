package app.almuflihun.core.analytics

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/** Every event and parameter in code is written in docs/analytics/tracking-plan.md (ADR 0008). Runs on the JVM. */
class TrackingPlanTest {

    private val plan = generateSequence(File("").absoluteFile) { it.parentFile }
        .map { File(it, "docs/analytics/tracking-plan.md") }
        .first { it.exists() }
        .readText()

    @Test
    fun `every event and parameter appears in the tracking plan`() {
        val missing = allEvents.flatMap { event ->
            listOf(event.name) + event.params.keys
        }.distinct().filterNot { "`$it`" in plan }
        assertTrue(missing.isEmpty(), "Not in docs/analytics/tracking-plan.md: $missing")
    }

    @Test
    fun `every user property appears in the tracking plan`() {
        val missing = UserProperty.entries.map { it.key }.filterNot { "`$it`" in plan }
        assertTrue(missing.isEmpty(), "User properties not in the plan: $missing")
    }
}
