package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.TimeProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * [TimeProvider] using kotlinx-datetime [Clock], not `java.time`.
 *
 * @since 0.1.0
 */
class SystemTimeProvider(
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    private val tickMs: Long = 1_000L,
) : TimeProvider {
    override fun now(): LocalTime = localDateTime().time

    override fun today(): LocalDate = localDateTime().date

    override fun observeTime(): Flow<LocalTime> = flow {
        while (true) {
            emit(now())
            delay(tickMs)
        }
    }

    private fun localDateTime() = clock.now().toLocalDateTime(timeZone)
}
