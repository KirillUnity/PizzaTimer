package com.example.clockplannerproject.kit.core

import com.example.clockplannerproject.kit.core.time.TimeMath

/**
 * Half-open civil interval `[startMinute, endMinute)` in minutes from local
 * midnight. Several blocks on one [Task] paint as several sectors with the
 * same [TaskId].
 *
 * Overnight work uses `endMinute < startMinute` (for example `22:00`–`02:00`).
 *
 * @since 0.3.0
 */
data class TimeBlock(
    val startMinute: Int,
    val endMinute: Int,
) {
    /**
     * Whether this interval has already ended relative to [nowMinute].
     * Overnight ranges stay editable through the night until [endMinute]
     * the following morning.
     *
     * @since 0.3.0
     */
    fun isClosed(nowMinute: Int): Boolean {
        val now = nowMinute.mod(TimeMath.MINUTES_PER_DAY)
        val start = startMinute.mod(TimeMath.MINUTES_PER_DAY)
        val endExclusive = if (endMinute == TimeMath.MINUTES_PER_DAY) {
            TimeMath.MINUTES_PER_DAY
        } else {
            endMinute.mod(TimeMath.MINUTES_PER_DAY)
        }
        if (start == endExclusive) return true
        return if (endExclusive > start) {
            now >= endExclusive
        } else {
            now >= endExclusive && now < start
        }
    }
}
