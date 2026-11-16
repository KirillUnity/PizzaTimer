package com.example.clockplannerproject.ui.day

import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DayProgressTest {

    @Test
    fun closedPastCountsDone_futureStaysPlanned_openIsSkipped() {
        val tasks = listOf(
            task("past", TaskStatus.TODO, TimeBlock(8 * 60, 9 * 60)),
            task("later", TaskStatus.TODO, TimeBlock(14 * 60 + 30, 16 * 60 + 30)),
            task("live", TaskStatus.IN_PROGRESS, TimeBlock(15 * 60, null)),
        )
        val (done, planned) = dayWorkMinutes(tasks, nowMinute = 15 * 60 + 40)
        assertEquals(60, done)
        assertEquals(60 + 120, planned)
    }

    private fun task(id: String, status: TaskStatus, block: TimeBlock) = Task(
        id = TaskId(id),
        title = id,
        colorArgb = 0xFF000000,
        blocks = listOf(block),
        status = status,
        date = LocalDate(2026, 11, 8),
    )
}
