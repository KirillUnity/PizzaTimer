package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskMappersTest {

    @Test
    fun mapper_roundTrip_preservesBlocksTagsImportance() {
        val original = Task(
            id = TaskId("task-2"),
            title = "Gym",
            description = "Legs",
            colorArgb = 0xFFB3261E,
            blocks = listOf(
                TimeBlock(8 * 60, 9 * 60),
                TimeBlock(12 * 60, 13 * 60),
            ),
            status = TaskStatus.IN_PROGRESS,
            date = LocalDate(2026, 10, 6),
            importance = Importance.HIGH,
            tags = listOf("sport", "health"),
        )
        val restored = original.toEntity().toDomain(original.toBlockEntities())
        assertEquals(original, restored)
    }

    @Test
    fun mapper_untimed_emptyBlocks() {
        val original = Task(
            id = TaskId("inbox"),
            title = "Inbox",
            colorArgb = 0xFF80CBC4,
            date = LocalDate(2026, 10, 6),
            importance = Importance.LOW,
        )
        val restored = original.toEntity().toDomain(original.toBlockEntities())
        assertEquals(original, restored)
        assertTrue(restored.isUntimed)
    }
}
