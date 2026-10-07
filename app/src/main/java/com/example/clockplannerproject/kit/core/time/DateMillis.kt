package com.example.clockplannerproject.kit.core.time

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/**
 * Start of [this] civil day in [timeZone], as epoch milliseconds.
 * Used by the Material DatePicker (not Canvas).
 *
 * @since 0.2.0
 */
fun LocalDate.toEpochMillisAtStart(
    timeZone: TimeZone = TimeZone.UTC,
): Long = atStartOfDayIn(timeZone).toEpochMilliseconds()

/**
 * Civil date of [epochMillis] in [timeZone].
 * DatePicker uses UTC midnight, so the default zone is UTC.
 *
 * @since 0.2.0
 */
fun epochMillisToLocalDate(
    epochMillis: Long,
    timeZone: TimeZone = TimeZone.UTC,
): LocalDate = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone).date
