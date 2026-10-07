package com.example.clockplannerproject.kit.render

import androidx.compose.ui.graphics.Color
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class SectorColorTest {

    private val gray = Color(0xFF9E9E9E)
    private val green = Color(0xFF2E7D32)

    @Test
    fun doneTask_usesCompletedGray() {
        val task = sample(TaskStatus.DONE)
        assertEquals(gray, sectorColor(task, gray, green))
    }

    @Test
    fun todoTask_keepsOwnColor() {
        val task = sample(TaskStatus.TODO)
        assertEquals(green, sectorColor(task, gray, green))
    }

    @Test
    fun inProgressTask_keepsOwnColor() {
        val task = sample(TaskStatus.IN_PROGRESS)
        assertEquals(green, sectorColor(task, gray, green))
    }

    private fun sample(status: TaskStatus) = Task(
        id = TaskId("work"),
        title = "Deep work",
        colorArgb = 0xFF2E7D32,
        blocks = listOf(TimeBlock(9 * 60, 12 * 60)),
        status = status,
        date = LocalDate(2026, 10, 6),
    )
}
