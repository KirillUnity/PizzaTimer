package com.example.clockplannerproject.ui.day.flow

import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DayFlowHoursTest {

    private val date = LocalDate(2026, 11, 16)

    @Test
    fun closedMinutes_skipsOpenSlices() {
        val task = Task(
            id = TaskId("work"),
            title = "Work",
            colorArgb = 1L,
            blocks = listOf(
                TimeBlock(9 * 60, 12 * 60),
                TimeBlock(13 * 60, null),
            ),
            date = date,
        )
        assertEquals(180, DayFlowHours.closedMinutes(listOf(task), 14 * 60))
    }

    @Test
    fun closedMinutes_futureBlockIsZero() {
        val task = Task(
            id = TaskId("gym"),
            title = "Gym",
            colorArgb = 1L,
            blocks = listOf(TimeBlock(18 * 60, 19 * 60 + 30)),
            date = date,
        )
        assertEquals(0, DayFlowHours.closedMinutes(listOf(task), 9 * 60))
    }
}
