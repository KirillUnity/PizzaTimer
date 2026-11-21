package com.example.clockplannerproject.kit.core.time

import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionDurationTest {

    private val date = LocalDate(2026, 10, 6)

    @Test
    fun fortyMinutes_offersThirty() {
        assertEquals(30, SessionDuration.suggest(9 * 60 + 20, 10 * 60))
    }

    @Test
    fun noEnd_offersTwentyFive() {
        assertEquals(25, SessionDuration.suggest(9 * 60, null))
    }

    @Test
    fun shortRemainder_offersFifteen() {
        assertEquals(15, SessionDuration.suggest(9 * 60, 9 * 60 + 10))
    }

    @Test
    fun plannedEnd_usesTheBlockThatContainsNow() {
        val task = Task(
            id = TaskId("work"),
            title = "Work",
            colorArgb = 0xFF2E7D32,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = date,
        )
        assertEquals(10 * 60, SessionDuration.plannedEnd(task, 9 * 60 + 20))
        assertNull(SessionDuration.plannedEnd(task.copy(blocks = listOf(TimeBlock(9 * 60, null))), 9 * 60))
    }
}
