package com.example.clockplannerproject.kit.core

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun daySubtask_hasNoIntervals_andKeepsSeries() {
        val template = original.copy(recurrence = RecurrenceRule.Daily, seriesId = "orig")
        val subtask = TaskInstances.daySubtask(template, tuesday, TaskId("tue"))
        assertEquals(TaskId("tue"), subtask.id)
        assertEquals(tuesday, subtask.date)
        assertTrue(subtask.blocks.isEmpty())
        assertEquals(RecurrenceRule.None, subtask.recurrence)
        assertEquals("orig", subtask.seriesId)
        assertEquals(template.blocks, template.blocks)
    }

    @Test
    fun withTemplateFace_renamesShownTitle_keepsInterval() {
        val template = original.copy(recurrence = RecurrenceRule.Daily, seriesId = "orig")
        val subtask = TaskInstances.daySubtask(template, tuesday, TaskId("tue"))
            .copy(blocks = listOf(TimeBlock(14 * 60, 15 * 60)), title = "Old")
        val shown = subtask.withTemplateFace(template.copy(title = "Renamed"))
        assertEquals("Renamed", shown.title)
        assertEquals(subtask.blocks, shown.blocks)
        assertEquals(subtask.id, shown.id)
        assertEquals(subtask.date, shown.date)
        assertEquals(subtask.status, shown.status)
    }
}
