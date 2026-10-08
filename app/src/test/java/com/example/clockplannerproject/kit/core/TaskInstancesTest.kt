package com.example.clockplannerproject.kit.core

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskInstancesTest {

    private val monday = LocalDate(2026, 10, 5)
    private val tuesday = LocalDate(2026, 10, 6)
    private val original = Task(
        id = TaskId("orig"),
        title = "Focus",
        description = "Deep work",
        colorArgb = 0xFFA8C5A0,
        blocks = listOf(TimeBlock(9 * 60, 12 * 60)),
        date = monday,
        project = "code",
        importance = Importance.HIGH,
    )

    @Test
    fun moved_keepsIdBlocksAndProject() {
        val moved = TaskInstances.moved(original, tuesday)
        assertEquals(original.id, moved.id)
        assertEquals(tuesday, moved.date)
        assertEquals(original.blocks, moved.blocks)
        assertEquals(original.project, moved.project)
    }

    @Test
    fun duplicated_newId_copyIsIndependent() {
        val copy = TaskInstances.duplicated(original, TaskId("copy"), tuesday)
        assertNotEquals(original.id, copy.id)
        assertEquals("Focus", copy.title)
        assertEquals(original.blocks, copy.blocks)
        assertEquals(original.project, copy.project)
        assertEquals(tuesday, copy.date)
        assertNull(copy.seriesId)
        val edited = copy.copy(title = "Copy title")
        assertEquals("Focus", original.title)
        assertEquals("Copy title", edited.title)
    }
}
