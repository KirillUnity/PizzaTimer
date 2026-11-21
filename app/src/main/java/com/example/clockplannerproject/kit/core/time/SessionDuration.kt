package com.example.clockplannerproject.kit.core.time

import com.example.clockplannerproject.kit.core.Task

/**
 * Soft session length. Steps are whole minutes. The timer does not
 * change this length by itself.
 *
 * @since 1.2.0
 */
object SessionDuration {
    const val STEP_MINUTES: Int = 15
    const val UNTIMED_MINUTES: Int = 25
    const val MIN_MINUTES: Int = 15
    const val MAX_MINUTES: Int = 12 * 60

    /**
     * Length offered before the interval opens.
     *
     * A planned end floors the remainder to [STEP_MINUTES], and never below
     * [MIN_MINUTES]. No end offers [UNTIMED_MINUTES].
     *
     * @param nowMinute minute of the local day, 0 until 1440
     * @param plannedEndMinute end already on the dial, or null
     */
    fun suggest(nowMinute: Int, plannedEndMinute: Int?): Int {
        if (plannedEndMinute == null) return UNTIMED_MINUTES
        val remaining = plannedEndMinute - nowMinute
        if (remaining <= 0) return MIN_MINUTES
        val stepped = (remaining / STEP_MINUTES) * STEP_MINUTES
        return if (stepped < MIN_MINUTES) MIN_MINUTES else stepped
    }

    /**
     * End minute of the block that contains [nowMinute], or the next block
     * that still has an end. An open block is not a planned end.
     */
    fun plannedEnd(task: Task, nowMinute: Int): Int? {
        val containing = task.blocks.firstOrNull { block ->
            val end = block.endMinute ?: return@firstOrNull false
            block.startMinute <= nowMinute && nowMinute < end
        }
        if (containing != null) return containing.endMinute
        return task.blocks
            .filter { block ->
                val end = block.endMinute
                end != null && block.startMinute > nowMinute
            }
            .minByOrNull { it.startMinute }
            ?.endMinute
    }

    /**
     * Moves [current] by [steps] of [STEP_MINUTES], clamped to the legal range.
     */
    fun step(current: Int, steps: Int): Int =
        (current + steps * STEP_MINUTES).coerceIn(MIN_MINUTES, MAX_MINUTES)

    /**
     * Absolute minute when a session that starts at [startMinute] should close.
     * Does not pass the end of the local day.
     */
    fun endMinute(startMinute: Int, durationMinutes: Int): Int =
        (startMinute + durationMinutes).coerceAtMost(TimeMath.MINUTES_PER_DAY)
}
