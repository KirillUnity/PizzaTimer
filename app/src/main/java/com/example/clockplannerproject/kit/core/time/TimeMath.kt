package com.example.clockplannerproject.kit.core.time

import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.LocalTime

/**
 * Converts civil time to clock-face degrees.
 *
 * `0°` is 12 o'clock, increasing clockwise. Android Canvas `drawArc` uses
 * 3 o'clock as `0°`; convert with [toCanvasAngle] at draw time.
 *
 * The day is two 12-hour faces ([com.example.clockplannerproject.kit.core.DialHalf]):
 * 720 minutes = 360°. Scene rotation: [visualHalfAngle] keeps now at 12 o'clock
 * when now falls on that half.
 *
 * @since 0.1.0
 */
object TimeMath {
    const val MINUTES_PER_DAY: Int = 1440
    const val MINUTES_PER_HALF: Int = 720
    const val DEGREES_CIRCLE: Float = 360f

    /** Canvas degrees are offset so that clock `0°` lands at 12 o'clock. */
    const val CANVAS_ZERO_OFFSET_DEG: Float = -90f

    fun minuteOfDay(time: LocalTime): Float =
        time.hour * 60f + time.minute + time.second / 60f

    /**
     * @param minuteOfDay minutes from local midnight. `1440` wraps to `0`.
     * @return `0f` at midnight / 12 o'clock, `180f` at noon.
     */
    fun minuteToAngle(minuteOfDay: Int): Float = minuteToAngle(minuteOfDay.toFloat())

    fun minuteToAngle(minuteOfDay: Float): Float {
        val normalized = minuteOfDay.mod(MINUTES_PER_DAY.toFloat())
        return normalized / MINUTES_PER_DAY.toFloat() * DEGREES_CIRCLE
    }

    /**
     * Clock-face angle after auto-rotating so [nowMinute] sits at 12 o'clock,
     * plus [userRotationOffsetDeg] from the one-finger rotate gesture.
     *
     * `visualAngle(now, now) == userRotationOffsetDeg`.
     */
    fun visualAngle(
        minuteOfDay: Float,
        nowMinute: Float,
        userRotationOffsetDeg: Float = 0f,
    ): Float = minuteToAngle(minuteOfDay) - minuteToAngle(nowMinute) + userRotationOffsetDeg

    fun visualAngle(
        minuteOfDay: Int,
        nowMinute: Int,
        userRotationOffsetDeg: Float = 0f,
    ): Float = visualAngle(minuteOfDay.toFloat(), nowMinute.toFloat(), userRotationOffsetDeg)

    fun visualClockAngle(
        clockAngleDeg: Float,
        nowMinute: Float,
        userRotationOffsetDeg: Float = 0f,
    ): Float = clockAngleDeg - minuteToAngle(nowMinute) + userRotationOffsetDeg

    /**
     * Sweep of a task in degrees. Overnight (`end < start`) wraps past midnight.
     * Zero-length (`start == end`) is `0f`, not a full day.
     */
    fun durationToSweep(startMinute: Int, endMinute: Int): Float {
        val duration = durationMinutes(startMinute, endMinute)
        return duration / MINUTES_PER_DAY.toFloat() * DEGREES_CIRCLE
    }

    fun durationMinutes(startMinute: Int, endMinute: Int): Int {
        val start = startMinute.mod(MINUTES_PER_DAY)
        val end = endMinute.mod(MINUTES_PER_DAY)
        if (end == start) return 0
        return if (end > start) end - start else MINUTES_PER_DAY - start + end
    }

    fun toCanvasAngle(clockAngleDegrees: Float): Float =
        clockAngleDegrees + CANVAS_ZERO_OFFSET_DEG

    fun fromCanvasAngle(canvasAngleDegrees: Float): Float =
        canvasAngleDegrees - CANVAS_ZERO_OFFSET_DEG

    fun normalizeDegrees(degrees: Float): Float = degrees.mod(DEGREES_CIRCLE)

    /**
     * Signed shortest turn from [fromDeg] to [toDeg] in `(-180, 180]`.
     * Positive is clockwise, matching clock-face degrees.
     *
     * @since 0.2.0
     */
    fun signedDeltaDegrees(fromDeg: Float, toDeg: Float): Float {
        val raw = normalizeDegrees(toDeg - fromDeg)
        return if (raw > 180f) raw - DEGREES_CIRCLE else raw
    }

    /**
     * Whether [clockAngleDeg] lies on the clockwise arc `[startDeg, startDeg + sweepDeg)`.
     */
    fun containsClockAngle(clockAngleDeg: Float, startDeg: Float, sweepDeg: Float): Boolean {
        if (sweepDeg <= 0f) return false
        val delta = normalizeDegrees(clockAngleDeg - startDeg)
        return delta < sweepDeg
    }

    fun formatMinuteOfDay(minuteOfDay: Int): String {
        val clamped = minuteOfDay.mod(MINUTES_PER_DAY)
        val h = (clamped / 60).toString().padStart(2, '0')
        val m = (clamped % 60).toString().padStart(2, '0')
        return "$h:$m"
    }

    fun formatRange(startMinute: Int, endMinute: Int): String =
        "${formatMinuteOfDay(startMinute)} – ${formatMinuteOfDay(endMinute)}"

    /**
     * Civil labels for one or many [com.example.clockplannerproject.kit.core.TimeBlock]s.
     * Empty list is untimed — callers should use a UI string, not this.
     *
     * @since 0.3.0
     */
    fun formatBlocks(blocks: List<TimeBlock>): String =
        blocks.joinToString(" · ") { formatRange(it.startMinute, it.endMinute) }

    fun midpointClockAngle(startMinute: Int, endMinute: Int): Float {
        val start = minuteToAngle(startMinute)
        val sweep = durationToSweep(startMinute, endMinute)
        return start + sweep / 2f
    }

    /**
     * Angle on a 12-hour face. [minuteOfDay] is clipped into [half].
     * `0°` is 12 o'clock of that half (midnight on AM, noon on PM).
     */
    fun minuteToHalfAngle(minuteOfDay: Float, half: DialHalf): Float {
        val local = (minuteOfDay - half.startMinute).coerceIn(0f, MINUTES_PER_HALF.toFloat())
        return local / MINUTES_PER_HALF.toFloat() * DEGREES_CIRCLE
    }

    fun minuteToHalfAngle(minuteOfDay: Int, half: DialHalf): Float =
        minuteToHalfAngle(minuteOfDay.toFloat(), half)

    fun sliceSweepDeg(startMinute: Int, endMinute: Int): Float {
        val duration = (endMinute - startMinute).coerceAtLeast(0)
        return duration / MINUTES_PER_HALF.toFloat() * DEGREES_CIRCLE
    }

    /**
     * Minute that sits at 12 o'clock on [half].
     *
     * Active half: [nowMinute]. Other half: start of the nearest real task
     * (last by end if now is already past the half, first by start if now is
     * still before it). Empty half: the half's start (midnight or noon).
     *
     * @since 0.2.0
     */
    fun halfAnchorMinute(
        nowMinute: Float,
        half: DialHalf,
        slicesStartEnd: List<Pair<Int, Int>>,
    ): Float {
        if (half.contains(nowMinute)) return nowMinute
        if (slicesStartEnd.isEmpty()) return half.startMinute.toFloat()
        return if (nowMinute >= half.endMinute) {
            slicesStartEnd.maxBy { it.second }.first.toFloat()
        } else {
            slicesStartEnd.minBy { it.first }.first.toFloat()
        }
    }

    /**
     * 12-hour visual angle relative to [anchorMinute] at 12 o'clock,
     * plus [userRotationOffsetDeg].
     */
    fun visualHalfAngle(
        minuteOfDay: Float,
        anchorMinute: Float,
        half: DialHalf,
        userRotationOffsetDeg: Float = 0f,
    ): Float {
        val taskAngle = minuteToHalfAngle(minuteOfDay, half)
        val anchorAngle = minuteToHalfAngle(anchorMinute, half)
        return taskAngle - anchorAngle + userRotationOffsetDeg
    }

    fun midpointHalfAngle(startMinute: Int, endMinute: Int, half: DialHalf): Float {
        val start = minuteToHalfAngle(startMinute, half)
        val sweep = sliceSweepDeg(startMinute, endMinute)
        return start + sweep / 2f
    }

    fun formatHm(time: LocalTime): String {
        val h = time.hour.toString().padStart(2, '0')
        val m = time.minute.toString().padStart(2, '0')
        val s = time.second.toString().padStart(2, '0')
        return "$h:$m:$s"
    }
}
