package com.example.clockplannerproject.kit.core.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class DateMillisTest {

    @Test
    fun roundTrip_preservesCivilDate() {
        val zone = TimeZone.UTC
        val date = LocalDate(2026, 10, 6)
        val millis = date.toEpochMillisAtStart(zone)
        assertEquals(date, epochMillisToLocalDate(millis, zone))
    }
}
