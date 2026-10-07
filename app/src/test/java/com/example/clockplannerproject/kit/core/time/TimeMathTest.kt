package com.example.clockplannerproject.kit.core.time

import com.example.clockplannerproject.kit.core.DialHalf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeMathTest {

    @Test
    fun minuteToAngle_midnight_isZero() {
        assertEquals(0f, TimeMath.minuteToAngle(0), 0.01f)
    }

    @Test
    fun minuteToAngle_noon_is180() {
        assertEquals(180f, TimeMath.minuteToAngle(720), 0.01f)
    }

    @Test
    fun minuteToAngle_1440_wrapsToZero() {
        assertEquals(0f, TimeMath.minuteToAngle(1440), 0.01f)
        assertEquals(TimeMath.minuteToAngle(0), TimeMath.minuteToAngle(1440), 0.01f)
    }

    @Test
    fun durationToSweep_threeHours_is45() {
        assertEquals(45f, TimeMath.durationToSweep(9 * 60, 12 * 60), 0.01f)
    }

    @Test
    fun durationToSweep_overnight_wrapsMidnight() {
        // 22:00 → 02:00 = 4 hours = 60°
        assertEquals(60f, TimeMath.durationToSweep(22 * 60, 2 * 60), 0.01f)
    }

    @Test
    fun durationToSweep_zeroLength_isZero() {
        assertEquals(0f, TimeMath.durationToSweep(100, 100), 0.01f)
    }

    @Test
    fun visualAngle_atNow_isZero() {
        assertEquals(0f, TimeMath.visualAngle(0, 0), 0.01f)
        assertEquals(0f, TimeMath.visualAngle(720, 720), 0.01f)
        assertEquals(0f, TimeMath.visualAngle(22 * 60, 22 * 60), 0.01f)
        assertEquals(0f, TimeMath.visualAngle(90.5f, 90.5f), 0.01f)
    }

    @Test
    fun visualAngle_noonWhenNowIsMidnight_is180() {
        assertEquals(180f, TimeMath.visualAngle(720, 0), 0.01f)
    }

    @Test
    fun visualAngle_addsUserOffsetWithoutChangingNowIdentity() {
        assertEquals(15f, TimeMath.visualAngle(100, 100, userRotationOffsetDeg = 15f), 0.01f)
    }

    @Test
    fun minuteOfDay_includesSeconds() {
        val time = kotlinx.datetime.LocalTime(1, 2, 30)
        assertEquals(62.5f, TimeMath.minuteOfDay(time), 0.01f)
    }

    @Test
    fun fromCanvasAngle_invertsToCanvasAngle() {
        assertEquals(0f, TimeMath.fromCanvasAngle(TimeMath.toCanvasAngle(0f)), 0.01f)
        assertEquals(90f, TimeMath.fromCanvasAngle(TimeMath.toCanvasAngle(90f)), 0.01f)
    }

    @Test
    fun containsClockAngle_overnightWrapsPastZero() {
        assertTrue(TimeMath.containsClockAngle(0f, startDeg = 330f, sweepDeg = 120f))
        assertTrue(TimeMath.containsClockAngle(45f, startDeg = 330f, sweepDeg = 120f))
        assertFalse(TimeMath.containsClockAngle(90f, startDeg = 330f, sweepDeg = 120f))
        assertFalse(TimeMath.containsClockAngle(180f, startDeg = 330f, sweepDeg = 120f))
    }

    @Test
    fun formatRange_overnight() {
        assertEquals("22:00 – 06:00", TimeMath.formatRange(22 * 60, 6 * 60))
    }

    @Test
    fun minuteToHalfAngle_amNine_is270() {
        assertEquals(270f, TimeMath.minuteToHalfAngle(9 * 60, DialHalf.AM), 0.01f)
    }

    @Test
    fun minuteToHalfAngle_pmOne_is30() {
        assertEquals(30f, TimeMath.minuteToHalfAngle(13 * 60, DialHalf.PM), 0.01f)
    }

    @Test
    fun sliceSweep_threeHoursOnTwelveHourFace_is90() {
        assertEquals(90f, TimeMath.sliceSweepDeg(9 * 60, 12 * 60), 0.01f)
    }

    @Test
    fun visualHalfAngle_usesAnchorNotWallClockWhenOtherHalf() {
        assertEquals(
            0f,
            TimeMath.visualHalfAngle(
                minuteOfDay = 9 * 60f,
                anchorMinute = 9 * 60f,
                half = DialHalf.AM,
            ),
            0.01f,
        )
    }

    @Test
    fun signedDeltaDegrees_shortestClockwise() {
        assertEquals(90f, TimeMath.signedDeltaDegrees(0f, 90f), 0.01f)
        assertEquals(-90f, TimeMath.signedDeltaDegrees(0f, 270f), 0.01f)
        assertEquals(20f, TimeMath.signedDeltaDegrees(350f, 10f), 0.01f)
    }

    @Test
    fun visualHalfAngle_addsUserOffset() {
        assertEquals(
            90f,
            TimeMath.visualHalfAngle(
                minuteOfDay = 9 * 60f,
                anchorMinute = 9 * 60f,
                half = DialHalf.AM,
                userRotationOffsetDeg = 90f,
            ),
            0.01f,
        )
    }

    @Test
    fun halfAnchor_nowOnHalf_isNow() {
        assertEquals(
            15 * 60f + 28f,
            TimeMath.halfAnchorMinute(15 * 60f + 28f, DialHalf.PM, listOf(18 * 60 to 19 * 60)),
            0.01f,
        )
    }

    @Test
    fun halfAnchor_amAfterNoon_isLastTaskStart() {
        val slices = listOf(0 to 6 * 60, 9 * 60 to 12 * 60)
        assertEquals(
            9 * 60f,
            TimeMath.halfAnchorMinute(15 * 60f + 28f, DialHalf.AM, slices),
            0.01f,
        )
    }
}
