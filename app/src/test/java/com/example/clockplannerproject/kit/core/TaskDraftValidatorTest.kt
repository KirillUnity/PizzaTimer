package com.example.clockplannerproject.kit.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskDraftValidatorTest {

    @Test
    fun emptyOrBlankTitle_isError() {
        assertEquals(
            TaskDraftError.EmptyTitle,
            TaskDraftValidator.validate(title = "", startMinute = 9 * 60, endMinute = 10 * 60),
        )
        assertEquals(
            TaskDraftError.EmptyTitle,
            TaskDraftValidator.validate(title = "   ", startMinute = 0, endMinute = 60),
        )
    }

    @Test
    fun endEqualsStart_isError_includingWrappedMidnight() {
        assertEquals(
            TaskDraftError.EndEqualsStart,
            TaskDraftValidator.validate(title = "Focus", startMinute = 9 * 60, endMinute = 9 * 60),
        )
        assertEquals(
            TaskDraftError.EndEqualsStart,
            TaskDraftValidator.validate(title = "Focus", startMinute = 0, endMinute = 1440),
        )
    }

    @Test
    fun overnightRange_isValid() {
        assertNull(
            TaskDraftValidator.validate(
                title = "Sleep",
                startMinute = 22 * 60,
                endMinute = 6 * 60,
            ),
        )
    }

    @Test
    fun sameDayRange_isValid() {
        assertNull(
            TaskDraftValidator.validate(
                title = "Deep work",
                startMinute = 9 * 60,
                endMinute = 12 * 60,
            ),
        )
    }

    @Test
    fun overnightIsNotTreatedAsZeroDuration() {
        val error = TaskDraftValidator.validate("Sleep", 23 * 60, 1)
        assertTrue(error == null)
    }

    @Test
    fun untimedNonEmptyTitle_isValid() {
        assertNull(TaskDraftValidator.validate(title = "Inbox", blocks = emptyList()))
    }
}
