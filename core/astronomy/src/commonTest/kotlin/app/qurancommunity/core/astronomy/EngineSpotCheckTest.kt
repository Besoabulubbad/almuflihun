package app.qurancommunity.core.astronomy

import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.searchRiseSet
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The ported engine runs the same on Android and iOS: a few US Naval Observatory events from the full reference set
 * (UsnoReferenceTest, JVM), checked on every platform.
 */
class EngineSpotCheckTest {

    private class Event(val body: Body, val lon: Double, val lat: Double, val utc: Time, val direction: Direction)

    private fun time(year: Int, month: Int, day: Int, hour: Int, minute: Int) =
        Time(year, month, day, hour, minute, 0.0)

    private val events = listOf(
        Event(Body.Moon, -81.0, 29.0, time(2019, 1, 1, 8, 12), Direction.Rise),
        Event(Body.Moon, -81.0, 29.0, time(2019, 1, 1, 19, 34), Direction.Set),
        Event(Body.Sun, 0.0, 60.0, time(2019, 3, 26, 18, 28), Direction.Set),
        Event(Body.Sun, 0.0, 60.0, time(2019, 3, 27, 5, 42), Direction.Rise),
        Event(Body.Sun, 45.0, -88.0, time(2022, 3, 17, 20, 13), Direction.Set),
    )

    @Test
    fun usno_events_within_the_reference_tolerance() {
        events.forEach { e ->
            val found = searchRiseSet(e.body, Observer(e.lat, e.lon, 0.0), e.direction, e.utc.addDays(-1.0 / 24), 1.0)
            val minutes = abs(found!!.ut - e.utc.ut) * 1440.0
            assertTrue(minutes <= TOLERANCE_MIN, "${e.body} ${e.direction} at ${e.lat},${e.lon}: $minutes min")
        }
    }

    private companion object {
        const val TOLERANCE_MIN = 1.2
    }
}
