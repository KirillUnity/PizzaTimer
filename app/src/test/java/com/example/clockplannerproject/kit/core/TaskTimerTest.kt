package com.example.clockplannerproject.kit.core

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskTimerTest {

    private val day = LocalDate(2026, 10, 6)
    private val sport = Task(
        id = TaskId("sport"),
        title = "Sport",
        colorArgb = 0xFFE8B4B8,
        blocks = listOf(TimeBlock(8 * 60, 9 * 60)),
        date = day,
    )

    @Test
    fun start_appendsOpenBlock_doesNotMutatePast() {
        val started = TaskTimer.start(sport, 12 * 60)
        assertEquals(TaskStatus.IN_PROGRESS, started.status)
        assertEquals(2, started.blocks.size)
        assertEquals(TimeBlock(8 * 60, 9 * 60), started.blocks.first())
        assertEquals(12 * 60, started.blocks.last().startMinute)
        assertNull(started.blocks.last().endMinute)
        assertTrue(TaskTimer.hasOpenBlock(started))
        assertFalse(TaskTimer.canStart(started))
    }

    @Test
    fun start_ignoredWhenAlreadyOpen() {
        val started = TaskTimer.start(sport, 12 * 60)
        assertEquals(started, TaskTimer.start(started, 12 * 60 + 15))
    }

    @Test
    fun pause_closesOpenBlock_pastUnchanged() {
        val started = TaskTimer.start(sport, 12 * 60)
        val paused = TaskTimer.pause(started, 13 * 60)
        assertEquals(
            listOf(TimeBlock(8 * 60, 9 * 60), TimeBlock(12 * 60, 13 * 60)),
            paused.blocks,
        )
        assertEquals(TimeBlock(8 * 60, 9 * 60), paused.blocks.first())
        assertFalse(TaskTimer.hasOpenBlock(paused))
        assertEquals(TaskStatus.TODO, paused.status)
    }
}
