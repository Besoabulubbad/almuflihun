package app.almuflihun.core.astronomy

import app.almuflihun.core.model.MoonPhaseName
import app.almuflihun.core.model.Place
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.searchRiseSet
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** The sky layer on both platforms, against US Naval Observatory times and the owner's lunar-night rule. */
class SkyTest {

    private val sky = EngineSky()
    private val utc = TimeZone.UTC
    private val amman = Place(31.95, 35.93)

    private fun near(expected: String, actual: Instant?, tolerance: Duration = 1.2.minutes) {
        assertNotNull(actual)
        val diff = (actual - Instant.parse(expected)).absoluteValue
        assertTrue(diff <= tolerance, "$actual is $diff from $expected")
    }

    @Test
    fun phases_at_usno_new_and_full_moons() {
        val full = sky.moon(Instant.parse("2010-01-30T06:18:00Z"))
        assertEquals(MoonPhaseName.Full, full.phase)
        assertTrue(full.litFraction > 0.99)
        val new = sky.moon(Instant.parse("2010-02-14T02:51:00Z"))
        assertEquals(MoonPhaseName.New, new.phase)
        assertTrue(new.litFraction < 0.01)
        assertTrue(new.distanceKm in 356_000.0..407_000.0)
    }

    @Test
    fun sun_and_moon_rise_and_set_match_usno() {
        val sun = sky.sunDay(Place(60.0, 0.0), LocalDate(2019, 3, 26), utc)
        near("2019-03-26T05:45:00Z", sun.sunrise?.time)
        near("2019-03-26T18:28:00Z", sun.sunset?.time)
        // cos A = sin δ / cos φ: six days after the equinox (δ ≈ +2.2°) at 60° N gives ≈ 85°, refraction a little less.
        assertTrue(sun.sunrise!!.azimuth in 83.5..86.0, "sunrise azimuth ${sun.sunrise?.azimuth}")
        assertTrue(sun.civilDawn!! < sun.sunrise!!.time, "civil dawn ${sun.civilDawn} sunrise ${sun.sunrise?.time}")
        assertTrue(sun.civilDusk!! > sun.sunset!!.time, "civil dusk ${sun.civilDusk} sunset ${sun.sunset?.time}")
        // 90 − 60 + δ (≈ +2.2° on 26 March) ≈ 32.2°
        assertTrue(sun.noon!!.altitude in 31.7..32.7, "noon altitude ${sun.noon?.altitude}")

        val moon = sky.moonDay(Place(29.0, -81.0), LocalDate(2019, 1, 1), utc)
        near("2019-01-01T08:12:00Z", moon.rise?.time)
        near("2019-01-01T19:34:00Z", moon.set?.time)
    }

    @Test
    fun a_year_of_lunar_nights_at_amman() {
        var day = LocalDate(2026, 1, 1)
        var previous: Int? = null
        val monthLengths = mutableListOf<Int>()
        repeat(365) {
            val noon = day.atStartOfDayIn(utc) + 9.hours // around local noon in Amman
            val night = assertNotNull(sky.lunarNight(amman, noon))
            if (previous != null) {
                if (night.number == 1) {
                    monthLengths += previous!!
                } else {
                    assertEquals(previous!! + 1, night.number, "on $day")
                }
            }
            previous = night.number
            day = day.plus(DatePeriod(days = 1))
        }
        assertTrue(monthLengths.size >= 11)
        assertTrue(monthLengths.all { it == 29 || it == 30 }, "month lengths $monthLengths")
    }

    @Test
    fun the_night_changes_at_sunset_and_night_one_follows_the_rule() {
        val night = assertNotNull(sky.lunarNight(amman, Instant.parse("2026-06-10T09:00:00Z")))
        val before = assertNotNull(sky.lunarNight(amman, night.began - 5.minutes))
        val after = assertNotNull(sky.lunarNight(amman, night.began + 5.minutes))
        assertEquals(night.number, after.number)
        if (night.number == 1) assertTrue(before.number in 29..30) else assertEquals(night.number - 1, before.number)
        assertTrue(night.conjunction < night.monthBegan && night.monthBegan <= night.began)

        // The rule itself, checked with the engine: night 1's evening sees the moon set after the sun …
        val observer = Observer(amman.latitude, amman.longitude, 0.0)
        val first = Time.fromMillisecondsSince1970(night.monthBegan.toEpochMilliseconds())
        val moonset = searchRiseSet(Body.Moon, observer, Direction.Set, first.addDays(-0.25), 0.5)
        assertTrue(moonset != null && moonset.ut > first.ut)
        // … and the evening before, if it came after the conjunction, did not.
        val conjunction = Time.fromMillisecondsSince1970(night.conjunction.toEpochMilliseconds())
        val previousSunset = searchRiseSet(Body.Sun, observer, Direction.Set, first.addDays(-1.5), 1.4)
        if (previousSunset != null && previousSunset.ut < first.ut - 0.5 && previousSunset.ut > conjunction.ut) {
            val earlier = searchRiseSet(Body.Moon, observer, Direction.Set, previousSunset.addDays(-0.25), 0.5)
            assertTrue(earlier == null || earlier.ut <= previousSunset.ut)
        }
    }

    @Test
    fun no_sunset_no_lunar_night() {
        assertNull(sky.lunarNight(Place(78.22, 15.65), Instant.parse("2026-06-21T12:00:00Z")))
    }
}
