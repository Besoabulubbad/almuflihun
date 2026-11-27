package app.almuflihun.core.ui

import app.almuflihun.core.common.AppError
import app.almuflihun.core.common.Loadable
import kotlin.test.Test
import kotlin.test.assertEquals

/** The offline-first rule every data screen follows. */
class ScreenStateTest {

    private fun state(loadable: Loadable<List<Int>>) = ScreenState.from(loadable) { it.isEmpty() }

    @Test
    fun `nothing cached is loading while fetching and offline or error after a failure`() {
        assertEquals(ScreenState.Loading, state(Loadable.Loading))
        assertEquals(ScreenState.Offline(null), state(Loadable(null, error = AppError.Offline)))
        assertEquals(ScreenState.Error(AppError.Remote(503)), state(Loadable(null, error = AppError.Remote(503))))
    }

    @Test
    fun `cached content stays on screen whatever the refresh did`() {
        assertEquals(ScreenState.Content(listOf(1)), state(Loadable(listOf(1), refreshing = true)))
        assertEquals(ScreenState.Offline(listOf(1)), state(Loadable(listOf(1), error = AppError.Offline)))
        assertEquals(ScreenState.Content(listOf(1)), state(Loadable(listOf(1), error = AppError.Remote(503))))
    }

    @Test
    fun `an empty result is empty unless the fetch that should fill it failed`() {
        assertEquals(ScreenState.Empty, state(Loadable(emptyList())))
        assertEquals(ScreenState.Offline(null), state(Loadable(emptyList(), error = AppError.Offline)))
    }
}
