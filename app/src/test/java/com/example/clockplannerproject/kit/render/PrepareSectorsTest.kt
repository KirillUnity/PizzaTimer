package com.example.clockplannerproject.kit.render

import androidx.compose.ui.graphics.Color
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrepareSectorsTest {

    private val day = LocalDate(2026, 10, 6)

    private fun task(
        id: String,
        start: Int,
        end: Int,
    ) = Task(
        id = TaskId(id),
        title = id,
        colorArgb = 0xFFA8C5A0,
        blocks = listOf(TimeBlock(start, end)),
        status = TaskStatus.TODO,
        date = day,
    )

    @Test
    fun selectedTask_paintsLast() {
        val long = task("long", 9 * 60, 12 * 60)
        val short = task("short", 9 * 60, 9 * 60 + 30)
        val sectors = prepareSectors(
            tasks = listOf(short, long),
            colors = listOf(Color.Red, Color.Blue),
            half = DialHalf.AM,
            anchorMinute = 9 * 60f,
            userRotationOffsetDeg = 0f,
            selectedTaskId = long.id,
        )
        assertEquals("long", sectors.last().title)
    }

    @Test
    fun overlapping_paintsShorterLast() {
        val long = task("long", 9 * 60, 12 * 60)
        val short = task("short", 9 * 60, 9 * 60 + 30)
        val sectors = prepareSectors(
            tasks = listOf(short, long),
            colors = listOf(Color.Red, Color.Blue),
            half = DialHalf.AM,
            anchorMinute = 9 * 60f,
            userRotationOffsetDeg = 0f,
        )
        assertEquals(2, sectors.size)
        assertTrue(sectors.first().sweepDeg > sectors.last().sweepDeg)
        assertEquals("short", sectors.last().title)
    }

    @Test
    fun overnightPmSlice_hasPositiveSweep() {
        val sleep = task("sleep", 22 * 60, 6 * 60)
        val laidOut = sleep.withRange(22 * 60, 24 * 60)
        val sectors = prepareSectors(
            tasks = listOf(laidOut),
            colors = listOf(Color.Magenta),
            half = DialHalf.PM,
            anchorMinute = 22 * 60f,
            userRotationOffsetDeg = 0f,
        )
        assertEquals(1, sectors.size)
        assertEquals(60f, sectors.single().sweepDeg, 0.01f)
    }

    @Test
    fun overnightAmSlice_hasPositiveSweep() {
        val sleep = task("sleep", 22 * 60, 6 * 60)
        val laidOut = sleep.withRange(0, 6 * 60)
        val sectors = prepareSectors(
            tasks = listOf(laidOut),
            colors = listOf(Color.Magenta),
            half = DialHalf.AM,
            anchorMinute = 0f,
            userRotationOffsetDeg = 0f,
        )
        assertEquals(1, sectors.size)
        assertEquals(180f, sectors.single().sweepDeg, 0.01f)
    }
}
