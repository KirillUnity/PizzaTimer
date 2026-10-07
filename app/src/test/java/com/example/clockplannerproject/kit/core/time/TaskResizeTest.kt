package com.example.clockplannerproject.kit.core.time

import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskResizeTest {

    private val day = LocalDate(2026, 10, 6)
    private val work = Task(
        id = TaskId("work"),
        title = "Deep work",
        colorArgb = 0xFF2E7D32,
        blocks = listOf(TimeBlock(9 * 60, 12 * 60)),
        date = day,
    )

    @Test
    fun snapMinute_roundsToFive() {
        assertEquals(10, TaskResize.snapMinute(12))
        assertEquals(10, TaskResize.snapMinute(8))
        assertEquals(0, TaskResize.snapMinute(1438))
        assertEquals(0, TaskResize.snapMinute(2))
        assertEquals(10, TaskResize.snapDuration(12))
        assertEquals(5, TaskResize.snapDuration(3))
    }

    @Test
    fun endMinuteAfterDelta_snapsAndKeepsMinDuration() {
        val grown = TaskResize.endMinuteAfterDelta(
            startMinute = 9 * 60,
            originalEndMinute = 12 * 60,
            deltaMinutes = 7f,
        )
        assertEquals(12 * 60 + 5, grown)

        val shrunkToMin = TaskResize.endMinuteAfterDelta(
            startMinute = 9 * 60,
            originalEndMinute = 9 * 60 + 5,
            deltaMinutes = -20f,
        )
        assertEquals(9 * 60 + 5, shrunkToMin)
    }

    @Test
    fun endMinuteAfterAngularDelta_clockwiseGrowsOnTwelveHourFace() {
        // 15° on a 12h face = 30 minutes
        val end = TaskResize.endMinuteAfterAngularDelta(work, deltaDeg = 15f)
        assertEquals(12 * 60 + 30, end)
    }

    @Test
    fun overnight_wrapsEndMinute() {
        val end = TaskResize.endMinuteAfterDelta(
            startMinute = 22 * 60,
            originalEndMinute = 2 * 60,
            deltaMinutes = 10f,
        )
        assertEquals(2 * 60 + 10, end)
    }

    @Test
    fun shrinkOvernight_snapsDurationAndDoesNotWrapPastStart() {
        val end = TaskResize.endMinuteAfterDelta(
            startMinute = 22 * 60,
            originalEndMinute = 6 * 60,
            deltaMinutes = -500f,
        )
        assertEquals(22 * 60 + TaskResize.MIN_DURATION_MINUTES, end)
    }

    @Test
    fun growOvernight_keepsCivilEndAfterMidnight() {
        val end = TaskResize.endMinuteAfterDelta(
            startMinute = 22 * 60,
            originalEndMinute = 6 * 60,
            deltaMinutes = 60f,
        )
        assertEquals(7 * 60, end)
    }

    @Test
    fun overlayEndMinute_replacesMatchingTask() {
        val overlay = TaskResize.overlayEndMinute(
            tasks = listOf(work),
            taskId = work.id,
            endMinute = 13 * 60,
        )
        assertEquals(13 * 60, overlay.single().endMinute)
    }
}
