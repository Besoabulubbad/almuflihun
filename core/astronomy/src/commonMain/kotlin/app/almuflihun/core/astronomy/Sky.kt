package app.almuflihun.core.astronomy

import app.almuflihun.core.model.Culmination
import app.almuflihun.core.model.HorizonEvent
import app.almuflihun.core.model.LunarNight
import app.almuflihun.core.model.MoonDay
import app.almuflihun.core.model.MoonPhaseName
import app.almuflihun.core.model.MoonState
import app.almuflihun.core.model.Place
import app.almuflihun.core.model.SunDay
import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.EquatorEpoch
import io.github.cosinekitty.astronomy.KM_PER_AU
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Refraction
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.equator
import io.github.cosinekitty.astronomy.geoMoon
import io.github.cosinekitty.astronomy.horizon
import io.github.cosinekitty.astronomy.illumination
import io.github.cosinekitty.astronomy.moonPhase
import io.github.cosinekitty.astronomy.searchAltitude
import io.github.cosinekitty.astronomy.searchHourAngle
import io.github.cosinekitty.astronomy.searchMoonPhase
import io.github.cosinekitty.astronomy.searchRiseSet
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Instant

/**
 * The sky over a place, computed on the device (docs/specs/screens.md §2; ADR 0017). Features reach it through use
 * cases; nothing here knows about screens or the network.
 */
interface Sky {
    fun moon(at: Instant): MoonState
    fun moonDay(place: Place, date: LocalDate, zone: TimeZone): MoonDay
    fun sunDay(place: Place, date: LocalDate, zone: TimeZone): SunDay

    /** «الليلة n» at [at] for [place]; null where the sun does not set (polar summer), so the rule cannot apply. */
    fun lunarNight(place: Place, at: Instant): LunarNight?
}

/** [Sky] on Astronomy Engine. */
class EngineSky : Sky {

    override fun moon(at: Instant): MoonState {
        val time = at.toTime()
        val angle = moonPhase(time)
        return MoonState(
            at = at,
            phaseAngle = angle,
            phase = phaseName(angle),
            litFraction = illumination(Body.Moon, time).phaseFraction,
            distanceKm = geoMoon(time).length() * KM_PER_AU,
        )
    }

    override fun moonDay(place: Place, date: LocalDate, zone: TimeZone): MoonDay {
        val observer = place.observer()
        val start = date.atStartOfDayIn(zone).toTime()
        val end = start.addDays(1.0)
        return MoonDay(
            date = date,
            rise = horizonEvent(Body.Moon, observer, Direction.Rise, start, end),
            set = horizonEvent(Body.Moon, observer, Direction.Set, start, end),
            highest = culmination(Body.Moon, observer, start, end),
        )
    }

    override fun sunDay(place: Place, date: LocalDate, zone: TimeZone): SunDay {
        val observer = place.observer()
        val start = date.atStartOfDayIn(zone).toTime()
        val end = start.addDays(1.0)
        fun twilight(direction: Direction, altitude: Double) =
            searchAltitude(Body.Sun, observer, direction, start, 1.0, altitude)?.takeIf { it.ut < end.ut }?.toInstant()
        return SunDay(
            date = date,
            sunrise = horizonEvent(Body.Sun, observer, Direction.Rise, start, end),
            sunset = horizonEvent(Body.Sun, observer, Direction.Set, start, end),
            noon = culmination(Body.Sun, observer, start, end),
            civilDawn = twilight(Direction.Rise, CIVIL),
            civilDusk = twilight(Direction.Set, CIVIL),
            nauticalDawn = twilight(Direction.Rise, NAUTICAL),
            nauticalDusk = twilight(Direction.Set, NAUTICAL),
            astronomicalDawn = twilight(Direction.Rise, ASTRONOMICAL),
            astronomicalDusk = twilight(Direction.Set, ASTRONOMICAL),
        )
    }

    override fun lunarNight(place: Place, at: Instant): LunarNight? = try {
        val observer = place.observer()
        val time = at.toTime()
        var conjunction = previousNewMoon(time)
        var first = firstNight(observer, conjunction)
        if (first.ut > time.ut) {
            // Still in the last nights of the previous month.
            conjunction = previousNewMoon(conjunction.addDays(-1.0))
            first = firstNight(observer, conjunction)
        }
        var number = 1
        var began = first
        var next = sunsetAfter(observer, began)
        while (next.ut <= time.ut) {
            number++
            began = next
            next = sunsetAfter(observer, began)
        }
        LunarNight(number, began.toInstant(), first.toInstant(), conjunction.toInstant())
    } catch (_: NoSunset) {
        null
    }

    /** The rule can't be applied here: no sunset (or no conjunction found) where one is needed. */
    private class NoSunset : Exception()

    private fun sunsetAfter(observer: Observer, time: Time): Time =
        searchRiseSet(Body.Sun, observer, Direction.Set, time.addDays(MINUTE), 2.0) ?: throw NoSunset()

    /** The first sunset after [conjunction] whose evening sees the moon set after the sun. */
    private fun firstNight(observer: Observer, conjunction: Time): Time {
        var sunset = searchRiseSet(Body.Sun, observer, Direction.Set, conjunction, 2.0) ?: throw NoSunset()
        var tried = 1
        while (!moonSetsAfter(observer, sunset)) {
            if (tried++ == EVENINGS_TO_TRY) throw NoSunset()
            sunset = sunsetAfter(observer, sunset)
        }
        return sunset
    }

    private fun moonSetsAfter(observer: Observer, sunset: Time): Boolean {
        val moonset = searchRiseSet(Body.Moon, observer, Direction.Set, sunset.addDays(-QUARTER_DAY), HALF_DAY)
        return moonset != null && moonset.ut > sunset.ut
    }

    private fun previousNewMoon(before: Time): Time {
        var found = searchMoonPhase(0.0, before.addDays(-SYNODIC_SEARCH), SYNODIC_SEARCH) ?: throw NoSunset()
        var next = searchMoonPhase(0.0, found.addDays(1.0), SYNODIC_SEARCH)
        while (next != null && next.ut <= before.ut) {
            found = next
            next = searchMoonPhase(0.0, found.addDays(1.0), SYNODIC_SEARCH)
        }
        return found
    }

    private fun horizonEvent(
        body: Body,
        observer: Observer,
        direction: Direction,
        start: Time,
        end: Time,
    ): HorizonEvent? {
        val time = searchRiseSet(body, observer, direction, start, 1.0)?.takeIf { it.ut < end.ut } ?: return null
        return HorizonEvent(time.toInstant(), position(body, observer, time).azimuth)
    }

    private fun culmination(body: Body, observer: Observer, start: Time, end: Time): Culmination? {
        val info = searchHourAngle(body, observer, 0.0, start)
        return if (info.time.ut < end.ut) Culmination(info.time.toInstant(), info.hor.altitude) else null
    }

    private fun position(body: Body, observer: Observer, time: Time) =
        equator(body, time, observer, EquatorEpoch.OfDate, Aberration.Corrected)
            .let { horizon(time, observer, it.ra, it.dec, Refraction.Normal) }

    private fun phaseName(angle: Double): MoonPhaseName {
        val index = (((angle + HALF_SECTOR) % FULL_TURN) / SECTOR).toInt()
        return MoonPhaseName.entries[index]
    }

    private fun Place.observer() = Observer(latitude, longitude, elevationMeters)

    private fun Instant.toTime() = Time.fromMillisecondsSince1970(toEpochMilliseconds())

    private fun Time.toInstant() = Instant.fromEpochMilliseconds(toMillisecondsSince1970())

    private companion object {
        const val CIVIL = -6.0
        const val NAUTICAL = -12.0
        const val ASTRONOMICAL = -18.0
        const val FULL_TURN = 360.0
        const val SECTOR = 45.0
        const val HALF_SECTOR = 22.5
        const val MINUTE = 1.0 / 1440
        const val QUARTER_DAY = 0.25
        const val HALF_DAY = 0.5
        const val EVENINGS_TO_TRY = 4
        const val SYNODIC_SEARCH = 35.0
    }
}
