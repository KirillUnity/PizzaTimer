package com.example.clockplannerproject.kit.core.time

import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import kotlin.math.roundToInt

/**
 * Duration resize math. Gesture: one-finger drag of the sector's trailing
 * edge (after a long-press, or when already selected). Pinch is not used —
 * two fingers on a thin ring fight the one-finger scene rotate.
 *
 * Overlaps with neighbors are allowed; this never shifts other tasks.
 *
 * @since 0.2.0
 */
object TaskResize {
    const val SNAP_MINUTES: Int = 5
    const val MIN_DURATION_MINUTES: Int = 5
    val MAX_DURATION_MINUTES: Int = TimeMath.MINUTES_PER_DAY - MIN_DURATION_MINUTES

    /**
     * Rounds [minuteOfDay] to the nearest [step] on a 1440-minute clock.
     */
    fun snapMinute(minuteOfDay: Int, step: Int = SNAP_MINUTES): Int {
        val safeStep = step.coerceAtLeast(1)
        val normalized = minuteOfDay.mod(TimeMath.MINUTES_PER_DAY)
        val snapped = ((normalized + safeStep / 2) / safeStep) * safeStep
        return snapped.mod(TimeMath.MINUTES_PER_DAY)
    }

    /**
     * Minutes gained on a 12-hour face: `360°` = `720` minutes.
     */
    fun minutesFromHalfDegrees(deltaDeg: Float): Float =
        deltaDeg / TimeMath.DEGREES_CIRCLE * TimeMath.MINUTES_PER_HALF.toFloat()

    /**
     * New [Task.endMinute] after an angular drag. Clockwise grows duration.
     * Duration is snapped and clamped so shrinking never wraps overnight.
     *
     * @param deltaMinutes raw minutes from the gesture, before snap
     */
    fun endMinuteAfterDelta(
        startMinute: Int,
        originalEndMinute: Int,
        deltaMinutes: Float,
        snapMinutes: Int = SNAP_MINUTES,
        minDuration: Int = MIN_DURATION_MINUTES,
    ): Int {
        val start = startMinute.mod(TimeMath.MINUTES_PER_DAY)
        val originalDuration = TimeMath.durationMinutes(start, originalEndMinute)
        val proposedDuration = originalDuration + deltaMinutes.roundToInt()
        val snappedDuration = snapDuration(proposedDuration, snapMinutes)
            .coerceIn(minDuration, MAX_DURATION_MINUTES)
        return (start + snappedDuration).mod(TimeMath.MINUTES_PER_DAY)
    }

    /**
     * Rounds a duration (not a clock stamp) to the nearest [step].
     */
    fun snapDuration(minutes: Int, step: Int = SNAP_MINUTES): Int {
        val safeStep = step.coerceAtLeast(1)
        return ((minutes + safeStep / 2) / safeStep) * safeStep
    }

    fun endMinuteAfterAngularDelta(
        task: Task,
        deltaDeg: Float,
        snapMinutes: Int = SNAP_MINUTES,
        minDuration: Int = MIN_DURATION_MINUTES,
    ): Int = endMinuteAfterDelta(
        startMinute = task.startMinute,
        originalEndMinute = task.endMinute,
        deltaMinutes = minutesFromHalfDegrees(deltaDeg),
        snapMinutes = snapMinutes,
        minDuration = minDuration,
    )

    fun overlayEndMinute(
        tasks: List<Task>,
        taskId: TaskId?,
        endMinute: Int?,
        matchingStartMinute: Int? = null,
    ): List<Task> {
        if (taskId == null || endMinute == null) return tasks
        return tasks.map { task ->
            if (task.id == taskId) {
                task.withEndMinute(endMinute, matchingStartMinute)
            } else {
                task
            }
        }
    }
}
