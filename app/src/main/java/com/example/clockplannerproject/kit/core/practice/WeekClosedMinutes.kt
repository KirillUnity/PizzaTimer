package com.example.clockplannerproject.kit.core.practice

import androidx.compose.runtime.Immutable
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Closed minutes for one local date in the week strip.
 *
 * @property date calendar day of the column
 * @property closedMinutes sum of stored closed blocks on [date]. Open timers are omitted.
 * @since 1.3.0
 */
@Immutable
data class WeekDayMinutes(
    val date: LocalDate,
    val closedMinutes: Int,
)

/**
 * Monday–Sunday columns for the local week that contains today.
 *
 * Durations come from stored block ends. This is not dial layout and does
 * not apply leftover 5:3:2 weights.
 *
 * @since 1.3.0
 */
object WeekClosedMinutes {
    /** Columns in a civil week. */
    const val DAYS: Int = 7

    /**
     * @param today local date that must appear in the returned week
     * @param tasks every stored task; only those whose date falls in the week count
     * @return seven columns, Monday first, including days with zero closed minutes
     */
    fun columns(today: LocalDate, tasks: List<Task>): List<WeekDayMinutes> {
        val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        return List(DAYS) { offset ->
            val date = monday.plus(offset, DateTimeUnit.DAY)
            val minutes = tasks
                .asSequence()
                .filter { it.date == date }
                .flatMap { it.blocks.asSequence() }
                .sumOf { closedMinutes(it) }
            WeekDayMinutes(date, minutes)
        }
    }

    /**
     * @param block stored interval
     * @return civil length, or 0 while the timer is still open
     */
    fun closedMinutes(block: TimeBlock): Int {
        val end = block.endMinute ?: return 0
        if (end == TimeMath.MINUTES_PER_DAY) {
            val start = block.startMinute.mod(TimeMath.MINUTES_PER_DAY)
            return TimeMath.MINUTES_PER_DAY - start
        }
        return TimeMath.durationMinutes(block.startMinute, end)
    }
}
