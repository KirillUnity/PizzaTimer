package com.example.clockplannerproject.ui.day

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class RelativeDayTest {

    private val today = LocalDate(2026, 11, 20)

    @Test
    fun classifiesTodayYesterdayTomorrowAndFurtherDays() {
        assertEquals(RelativeDay.Today, relativeDay(today, today))
        assertEquals(RelativeDay.Yesterday, relativeDay(LocalDate(2026, 11, 19), today))
        assertEquals(RelativeDay.Tomorrow, relativeDay(LocalDate(2026, 11, 21), today))
        assertEquals(RelativeDay.Other, relativeDay(LocalDate(2026, 11, 18), today))
        assertEquals(RelativeDay.Other, relativeDay(LocalDate(2026, 11, 22), today))
    }
}
