package app.qurancommunity.core.model

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** Where the sky is computed for: the user's approximate location or chosen city (docs/specs/screens.md §2, §12). */
data class Place(val latitude: Double, val longitude: Double, val elevationMeters: Double = 0.0)

/** The eight phases, each 45° of the moon's elongation from the sun, centred on new, quarters and full. */
enum class MoonPhaseName {
    New, WaxingCrescent, FirstQuarter, WaxingGibbous, Full, WaningGibbous, LastQuarter, WaningCrescent,
}

/**
 * The moon at an instant, the same everywhere on Earth: [phaseAngle] is its ecliptic elongation from the sun
 * (0 new, 90 first quarter, 180 full, 270 last quarter), [litFraction] 0..1, [distanceKm] from Earth's centre.
 */
data class MoonState(
    val at: Instant,
    val phaseAngle: Double,
    val phase: MoonPhaseName,
    val litFraction: Double,
    val distanceKm: Double,
)

/** A rise or set: when, and the compass direction (degrees from north, clockwise). */
data class HorizonEvent(val time: Instant, val azimuth: Double)

/** The highest point: when, and how high (degrees above the horizon). */
data class Culmination(val time: Instant, val altitude: Double)

/** The moon over a local day at a place. Any event can be missing: the moon skips a rise or set about once a month. */
data class MoonDay(val date: LocalDate, val rise: HorizonEvent?, val set: HorizonEvent?, val highest: Culmination?)

/**
 * The sun over a local day at a place, with the three twilights' start (morning) and end (evening): civil (−6°),
 * nautical (−12°), astronomical (−18°). Missing where the sun or the twilight does not reach that point (high
 * latitudes).
 */
data class SunDay(
    val date: LocalDate,
    val sunrise: HorizonEvent?,
    val sunset: HorizonEvent?,
    val noon: Culmination?,
    val civilDawn: Instant?,
    val civilDusk: Instant?,
    val nauticalDawn: Instant?,
    val nauticalDusk: Instant?,
    val astronomicalDawn: Instant?,
    val astronomicalDusk: Instant?,
)

/**
 * «الليلة n» (DESIGN DECISIONS #6): night 1 is the first evening after the conjunction on which the moon sets after
 * the sun at the place; each night begins at sunset. [began] is this night's sunset, [monthBegan] night 1's.
 */
data class LunarNight(val number: Int, val began: Instant, val monthBegan: Instant, val conjunction: Instant)
