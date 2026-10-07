package com.example.clockplannerproject.kit.core

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Clock used by the dial. Replace in tests with a fake.
 *
 * @since 0.1.0
 */
interface TimeProvider {
    /** Current local wall time. */
    fun now(): LocalTime

    /** Current local calendar date. */
    fun today(): LocalDate

    /** Ticks local time so the UI can stay in sync. */
    fun observeTime(): Flow<LocalTime>
}
