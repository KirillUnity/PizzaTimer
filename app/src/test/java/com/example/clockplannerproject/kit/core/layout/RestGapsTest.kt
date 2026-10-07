package com.example.clockplannerproject.kit.core.layout

import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RestGapsTest {

    private val day = LocalDate(2026, 10, 6)
    private val work = Task(
        id = TaskId("work"),
        title = "Deep work",
        colorArgb = 0xFFA8C5A0,
        blocks = listOf(TimeBlock(9 * 60, 12 * 60)),
        date = day,
    )

    @Test
    fun amWithNineToNoon_fillsBeforeAndNothingAfterOnHalf() {
        val rest = RestGaps.fill(
            tasks = listOf(work),
            half = DialHalf.AM,
            date = day,
            title = "Rest",
            colorArgb = 0xFFD9CBB8,
        )
        assertEquals(1, rest.size)
        assertEquals(0, rest.single().startMinute)
        assertEquals(9 * 60, rest.single().endMinute)
        assertTrue(RestGaps.isRest(rest.single()))
    }

    @Test
    fun emptyHalf_isFullRest() {
        val rest = RestGaps.fill(
            tasks = emptyList(),
            half = DialHalf.PM,
            date = day,
            title = "Rest",
            colorArgb = 0xFFD9CBB8,
        )
        assertEquals(1, rest.size)
        assertEquals(12 * 60, rest.single().startMinute)
        assertEquals(24 * 60, rest.single().endMinute)
    }
}
