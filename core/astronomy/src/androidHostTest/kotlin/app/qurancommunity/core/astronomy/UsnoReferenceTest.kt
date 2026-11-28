package app.qurancommunity.core.astronomy

import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.nextMoonQuarter
import io.github.cosinekitty.astronomy.searchMoonQuarter
import io.github.cosinekitty.astronomy.searchRiseSet
import java.time.Instant
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The ported engine against the US Naval Observatory: 5,909 Sun and Moon rise and set times at 15 places (tropics to
 * poles, both hemispheres, 1944-2050) and 1,534 moon quarter times since 1800, as published with Astronomy Engine
 * (`generate/riseset`, `generate/moonphase`). USNO gives whole minutes.
 */
class UsnoReferenceTest {

    private fun lines(name: String) = javaClass.classLoader!!.getResource("usno/$name")!!.readText().lines()
        .filter { it.isNotBlank() }

    private fun minutes(a: Time, b: Time) = abs(a.ut - b.ut) * MINUTES_PER_DAY

    @Test
    fun rise_and_set_match_usno() {
        val errors = lines("riseset.txt").map { line ->
            val fields = line.trim().split(Regex("\\s+"))
            val body = fields[0]
            val lon = fields[1]
            val lat = fields[2]
            val utc = fields[3]
            val kind = fields[4]
            val expected = Time.fromMillisecondsSince1970(Instant.parse(utc.replace("Z", ":00Z")).toEpochMilli())
            val found = searchRiseSet(
                body = if (body == "Sun") Body.Sun else Body.Moon,
                observer = Observer(lat.toDouble(), lon.toDouble(), 0.0),
                direction = if (kind == "r") Direction.Rise else Direction.Set,
                startTime = expected.addDays(-1.0 / 24),
                limitDays = 1.0,
            )
            if (found == null) Double.MAX_VALUE else minutes(found, expected)
        }
        val worst = errors.max()
        val within1 = errors.count { it <= 1.0 }
        println("USNO rise/set: ${errors.size} events, within 1 min: $within1, worst ${"%.2f".format(worst)} min")
        assertTrue(worst <= RISE_SET_TOLERANCE_MIN, "worst error $worst min")
        assertTrue(within1 >= errors.size * MIN_SHARE_WITHIN_1, "$within1 of ${errors.size} within 1 min")
    }

    @Test
    fun moon_quarters_match_usno() {
        val reference = lines("moonphases.txt").map { line ->
            val (quarter, utc) = line.trim().split(Regex("\\s+"))
            quarter.toInt() to Time.fromMillisecondsSince1970(Instant.parse(utc).toEpochMilli())
        }
        var mq = searchMoonQuarter(reference.first().second.addDays(-1.0))
        val errors = reference.map { (quarter, time) ->
            while (mq.time.ut < time.ut - 1.0) mq = nextMoonQuarter(mq)
            assertTrue(mq.quarter == quarter, "quarter at $time")
            minutes(mq.time, time).also { mq = nextMoonQuarter(mq) }
        }
        val worst = errors.max()
        println("USNO moon quarters: ${errors.size}, worst ${"%.2f".format(worst)} min")
        assertTrue(worst <= QUARTER_TOLERANCE_MIN, "worst error $worst min")
    }

    private companion object {
        const val MINUTES_PER_DAY = 1440.0

        // USNO rounds to whole minutes. Measured: 5,907 of 5,909 within 1 min, the worst 1.18 min.
        const val RISE_SET_TOLERANCE_MIN = 1.2
        const val MIN_SHARE_WITHIN_1 = 0.999

        // Measured: the worst of 1,534 quarters 1.38 min.
        const val QUARTER_TOLERANCE_MIN = 1.5
    }
}
