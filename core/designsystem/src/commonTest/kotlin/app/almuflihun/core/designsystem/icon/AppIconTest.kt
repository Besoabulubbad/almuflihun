package app.almuflihun.core.designsystem.icon

import kotlin.test.Test
import kotlin.test.assertEquals

/** Directional icons mirror in right-to-left layouts; nothing else does (DESIGN §2, §7; ADR 0014). */
class AppIconTest {

    @Test
    fun `exactly the directional icons mirror`() {
        val mirrored = AppIcon.entries.filter { it.mirrorInRtl }.toSet()
        assertEquals(
            setOf(
                AppIcon.Back,
                AppIcon.Forward,
                AppIcon.OpenExternal,
                AppIcon.PlayFromHere,
                AppIcon.Previous,
                AppIcon.Next,
                AppIcon.Send,
                AppIcon.SignOut,
            ),
            mirrored,
        )
    }

    @Test
    fun `one icon per meaning and only save and collections share a drawable`() {
        val shared = AppIcon.entries.groupBy { it.resource }.filterValues { it.size > 1 }.values.map { it.toSet() }
        assertEquals(listOf(setOf(AppIcon.Collections, AppIcon.Save)), shared)
    }
}
