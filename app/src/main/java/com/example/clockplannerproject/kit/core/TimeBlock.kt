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
    /**
     * Stable id for this interval’s scores, goal, and notes.
     * Blank until the interval is saved or started.
     */
    val id: String = "",
    /** Minutes the interval was expected to take, or null when none was set. */
    val plannedMinutes: Int? = null,
) {
    /** True while the timer is running (no civil end yet). */
    val isOpen: Boolean get() = endMinute == null

    /**
     * Minutes this interval actually covered. An open interval runs from its
     * start to [nowMinute].
     *
     * @param nowMinute minute of the local day, used only while [endMinute] is null
     */
    fun actualMinutes(nowMinute: Int): Int {
        val end = endMinute ?: nowMinute.mod(TimeMath.MINUTES_PER_DAY)
        return TimeMath.durationMinutes(startMinute, end)
    }

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

    /**
     * Half-open civil ranges this block occupies. Overnight splits at midnight.
     * A zero-length closed block occupies one minute at start.
     *
     * @param nowMinute used when [endMinute] is null
     * @since 1.2.0
     */
    fun civilRanges(nowMinute: Int = 0): List<IntRange> {
        val start = startMinute.mod(TimeMath.MINUTES_PER_DAY)
        val rawEnd = endMinute
        val endExclusive = when {
            rawEnd == null -> {
                val now = nowMinute.mod(TimeMath.MINUTES_PER_DAY)
                if (now == start) (start + 1).coerceAtMost(TimeMath.MINUTES_PER_DAY) else now
            }
            rawEnd == TimeMath.MINUTES_PER_DAY -> TimeMath.MINUTES_PER_DAY
            else -> rawEnd.mod(TimeMath.MINUTES_PER_DAY)
        }
        if (start == endExclusive) {
            val next = (start + 1).coerceAtMost(TimeMath.MINUTES_PER_DAY)
            return listOf(start until next)
        }
        return if (endExclusive > start) {
            listOf(start until endExclusive)
        } else {
            listOf(start until TimeMath.MINUTES_PER_DAY, 0 until endExclusive)
        }
    }

    /**
     * Whether this block shares any civil minute with [other] on the same day.
     *
     * @since 1.2.0
     */
    fun overlaps(other: TimeBlock, nowMinute: Int = 0): Boolean {
        val mine = civilRanges(nowMinute)
        val theirs = other.civilRanges(nowMinute)
        return mine.any { a ->
            theirs.any { b -> a.first < b.last + 1 && b.first < a.last + 1 }
        }
    }
}
