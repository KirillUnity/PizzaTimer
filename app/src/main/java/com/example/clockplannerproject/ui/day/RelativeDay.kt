package com.example.clockplannerproject.ui.day

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * How the day-strip title names a visible date against local today.
 *
 * @since 0.5.0
 */
enum class RelativeDay {
    /** Visible date is local today. */
    Today,

    /** Visible date is the previous local calendar date. */
    Yesterday,

    /** Visible date is the next local calendar date. */
    Tomorrow,

    /** Any other visible date. The strip keeps the short date. */
    Other,
}

/**
 * Classifies [visible] against [today].
 *
 * Yesterday and tomorrow are exactly one calendar day away. A date two or more
 * days away is [RelativeDay.Other].
 *
 * @param visible date shown on the day screen
 * @param today local calendar date from the clock
 * @since 0.5.0
 */
fun relativeDay(visible: LocalDate, today: LocalDate): RelativeDay = when (visible) {
    today -> RelativeDay.Today
    today.minus(1, DateTimeUnit.DAY) -> RelativeDay.Yesterday
    today.plus(1, DateTimeUnit.DAY) -> RelativeDay.Tomorrow
    else -> RelativeDay.Other
}
