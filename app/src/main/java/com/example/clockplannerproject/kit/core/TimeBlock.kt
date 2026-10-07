package com.example.clockplannerproject.kit.core

import com.example.clockplannerproject.kit.core.time.TimeMath

/**
 * Half-open civil interval `[startMinute, endMinute)` in minutes from local
 * midnight. Several blocks on one [Task] paint as several sectors with the
 * same [TaskId].
 *
 * Overnight work uses `endMinute < startMinute` (for example `22:00`–`02:00`).
 * A **null** [endMinute] is an open timer slice: layout paints start → now.
 *
 * @since 0.3.0
 */
data class TimeBlock(
    val startMinute: Int,
    val endMinute: Int? = null,
) {
    /** True while the timer is running (no civil end yet). */
    val isOpen: Boolean get() = endMinute == null

    /**
     * Civil end used for drawing. Open blocks resolve to [nowMinute].
     *
     * @since 0.4.0
     */
    fun resolvedEndMinute(nowMinute: Int): Int {
        val end = endMinute
        if (end != null) return end
        return nowMinute.mod(TimeMath.MINUTES_PER_DAY)
    }

    /**
     * Closed copy for layout / hit-test. Does not mutate stored history.
     *
     * @since 0.4.0
     */
    fun resolveForLayout(nowMinute: Int): TimeBlock =
        if (isOpen) copy(endMinute = resolvedEndMinute(nowMinute)) else this

    /**
     * Whether this interval has already ended relative to [nowMinute].
     * Open timer slices are never closed. Overnight ranges stay editable
     * through the night until [endMinute] the following morning.
     *
     * @since 0.3.0
     */
    fun isClosed(nowMinute: Int): Boolean {
        val endRaw = endMinute ?: return false
        val now = nowMinute.mod(TimeMath.MINUTES_PER_DAY)
        val start = startMinute.mod(TimeMath.MINUTES_PER_DAY)
        val endExclusive = if (endRaw == TimeMath.MINUTES_PER_DAY) {
            TimeMath.MINUTES_PER_DAY
        } else {
            endRaw.mod(TimeMath.MINUTES_PER_DAY)
        }
        if (start == endExclusive) return true
        return if (endExclusive > start) {
            now >= endExclusive
        } else {
            now >= endExclusive && now < start
        }
    }
}
