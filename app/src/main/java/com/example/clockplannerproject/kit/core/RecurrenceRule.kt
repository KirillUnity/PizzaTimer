package com.example.clockplannerproject.kit.core

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * Repeat rule for a task **template**. Instances materialized for a visible
 * day store [None] and keep [Task.seriesId] pointing at the template.
 *
 * Only the visible calendar day is materialized — never an infinite horizon.
 *
 * @since 0.4.0
 */
sealed class RecurrenceRule {
    /** One-off instance or a materialized copy. */
    data object None : RecurrenceRule()

    /** Every local calendar day. */
    data object Daily : RecurrenceRule()

    /**
     * Selected ISO weekdays. An empty [days] set never occurs.
     *
     * @param days Monday–Sunday flags; unselected days are skipped.
     */
    data class Weekdays(val days: Set<DayOfWeek>) : RecurrenceRule() {
        init {
            require(days.isNotEmpty()) { "Weekdays recurrence needs at least one day" }
        }
    }

    /**
     * Whether an instance should exist on [date]. [None] is never a template.
     */
    fun occursOn(date: LocalDate): Boolean = when (this) {
        None -> false
        Daily -> true
        is Weekdays -> date.dayOfWeek in days
    }

    companion object {
        /** Bit 0 = Monday … bit 6 = Sunday (`DayOfWeek.value` − 1, ISO). */
        fun maskFor(days: Set<DayOfWeek>): Int =
            days.fold(0) { acc, day -> acc or (1 shl (day.value - 1)) }

        fun daysFromMask(mask: Int): Set<DayOfWeek> =
            DayOfWeek.entries.filter { day ->
                mask and (1 shl (day.value - 1)) != 0
            }.toSet()
    }
}
