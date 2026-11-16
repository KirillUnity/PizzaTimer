package com.example.clockplannerproject.kit.core

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskAppearanceTest {

    private val day = LocalDate(2026, 10, 6)

    @Test
    fun twoBlocks_twoAppearancesSameId() {
        val sport = Task(
            id = TaskId("sport"),
            title = "Sport",
            colorArgb = 0xFFE8B4B8,
            blocks = listOf(TimeBlock(8 * 60, 9 * 60), TimeBlock(14 * 60, 15 * 60)),
            date = day,
        )
        val rows = taskAppearances(listOf(sport))
        assertEquals(2, rows.size)
        assertEquals(listOf(0, 1), rows.map { it.blockIndex })
        assertEquals(sport.id, rows[0].task.id)
        assertEquals(sport.id, rows[1].task.id)
        assertEquals(8 * 60, rows[0].block?.startMinute)
        assertEquals(14 * 60, rows[1].block?.startMinute)
    }

    @Test
    fun untimed_oneAppearance() {
        val task = Task(
            id = TaskId("note"),
            title = "Note",
            colorArgb = 0xFF2563EB,
            date = day,
        )
        val rows = taskAppearances(listOf(task))
        assertEquals(1, rows.size)
        assertNull(rows.single().blockIndex)
    }

    @Test
    fun pointInterval_stillClipsOntoItsHalf() {
        val task = Task(
            id = TaskId("deep"),
            title = "Deep work",
            colorArgb = 0xFF2563EB,
            blocks = listOf(TimeBlock(9 * 60, 12 * 60), TimeBlock(14 * 60 + 18, 14 * 60 + 18)),
            date = day,
        )
        assertEquals(1, clipTaskToHalf(task, DialHalf.AM).size)
        val pm = clipTaskToHalf(task, DialHalf.PM)
        assertEquals(1, pm.size)
        assertEquals(14 * 60 + 18, pm.single().startMinute)
        assertEquals(14 * 60 + 18, pm.single().endMinute)
    }
}
