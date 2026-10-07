package com.example.clockplannerproject.kit.core

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecurrenceRuleTest {

    @Test
    fun daily_occursEveryDay() {
        val rule = RecurrenceRule.Daily
        assertTrue(rule.occursOn(LocalDate(2026, 10, 6)))
        assertTrue(rule.occursOn(LocalDate(2026, 10, 7)))
    }

    @Test
    fun weekdays_skipsUnselectedDay() {
        val weekdays = RecurrenceRule.Weekdays(
            setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
        )
        assertTrue(weekdays.occursOn(LocalDate(2026, 10, 6)))
        assertTrue(weekdays.occursOn(LocalDate(2026, 10, 7)))
        assertFalse(weekdays.occursOn(LocalDate(2026, 10, 10)))
        assertFalse(weekdays.occursOn(LocalDate(2026, 10, 11)))
    }

    @Test
    fun none_neverOccursAsTemplate() {
        assertFalse(RecurrenceRule.None.occursOn(LocalDate(2026, 10, 6)))
    }
}
