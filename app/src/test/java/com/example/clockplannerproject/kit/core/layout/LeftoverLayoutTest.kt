package com.example.clockplannerproject.kit.core.layout

import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeftoverLayoutTest {

    private val day = LocalDate(2026, 10, 6)

    private fun timed(id: String, start: Int, end: Int, status: TaskStatus = TaskStatus.TODO) = Task(
        id = TaskId(id),
        title = id,
        colorArgb = 0xFFA8C5A0,
        blocks = listOf(TimeBlock(start, end)),
        status = status,
        date = day,
    )

    private fun untimed(id: String, importance: Importance = Importance.MEDIUM) = Task(
        id = TaskId(id),
        title = id,
        colorArgb = 0xFF90CAF9,
        date = day,
        importance = importance,
    )

    @Test
    fun twoUntimedOnEmptyAm_sweepsSum360() {
        val slices = Reflow.layout(
            listOf(untimed("a"), untimed("b")),
            DialHalf.AM,
            ReflowMode.WallClock,
        )
        assertEquals(2, slices.size)
        val sweep = slices.sumOf { TimeMath.sliceSweepDeg(it.startMinute, it.endMinute).toDouble() }
        assertEquals(360.0, sweep, 0.01)
        assertEquals(TimeMath.MINUTES_PER_HALF, slices.sumOf { it.endMinute - it.startMinute })
    }

    @Test
    fun timedNineToNoon_untimedOnlyInGaps() {
        val slices = Reflow.layout(
            listOf(timed("work", 9 * 60, 12 * 60), untimed("inbox")),
            DialHalf.AM,
            ReflowMode.WallClock,
        )
        val work = slices.filter { it.task.id.value == "work" }
        val leftover = slices.filter { it.task.id.value == "inbox" }
        assertEquals(9 * 60, work.single().startMinute)
        assertEquals(12 * 60, work.single().endMinute)
        assertTrue(leftover.isNotEmpty())
        leftover.forEach { slice ->
            assertTrue(slice.endMinute <= 9 * 60)
            assertTrue(slice.startMinute >= 0)
        }
    }

    @Test
    fun leftoverZero_hidesUntimedOnThatHalf() {
        val full = timed("all", 0, TimeMath.MINUTES_PER_HALF)
        val slices = Reflow.layout(
            listOf(full, untimed("inbox")),
            DialHalf.AM,
            ReflowMode.WallClock,
        )
        assertEquals(1, slices.size)
        assertEquals("all", slices.single().task.id.value)
    }

    @Test
    fun pizzaUsesSameLayoutMinutes() {
        val tasks = listOf(timed("work", 9 * 60, 12 * 60), untimed("inbox"))
        val a = Reflow.layout(tasks, DialHalf.AM, ReflowMode.WallClock)
        val b = Reflow.layout(tasks, DialHalf.AM, ReflowMode.WallClock)
        assertEquals(a.map { Triple(it.task.id, it.startMinute, it.endMinute) },
            b.map { Triple(it.task.id, it.startMinute, it.endMinute) })
    }

    @Test
    fun threeDistinctImportances_are50_30_20() {
        val slices = Reflow.layout(
            listOf(
                untimed("h", Importance.HIGH),
                untimed("m", Importance.MEDIUM),
                untimed("l", Importance.LOW),
            ),
            DialHalf.AM,
            ReflowMode.WallClock,
        )
        val minutes = slices.associate { it.task.id.value to (it.endMinute - it.startMinute) }
        assertEquals(360, minutes.getValue("h"))
        assertEquals(216, minutes.getValue("m"))
        assertEquals(144, minutes.getValue("l"))
        assertEquals(720, minutes.values.sum())
    }

    @Test
    fun equalImportance_equalShares() {
        val slices = Reflow.layout(
            listOf(untimed("a", Importance.HIGH), untimed("b", Importance.HIGH)),
            DialHalf.AM,
            ReflowMode.WallClock,
        )
        val spans = slices.map { it.endMinute - it.startMinute }
        assertEquals(listOf(360, 360), spans)
    }

    @Test
    fun weightsWithTimedNineToNoon() {
        val slices = Reflow.layout(
            listOf(
                timed("work", 9 * 60, 12 * 60),
                untimed("h", Importance.HIGH),
                untimed("m", Importance.MEDIUM),
                untimed("l", Importance.LOW),
            ),
            DialHalf.AM,
            ReflowMode.WallClock,
        )
        val leftover = slices.filter { it.task.isUntimed }
        val minutes = leftover.associate { it.task.id.value to (it.endMinute - it.startMinute) }
        assertEquals(540, minutes.values.sum())
        assertEquals(270, minutes.getValue("h"))
        assertEquals(162, minutes.getValue("m"))
        assertEquals(108, minutes.getValue("l"))
    }

    @Test
    fun compact_dropsDoneThenLeftoverWeights() {
        val slices = Reflow.layout(
            listOf(
                timed("done", 9 * 60, 12 * 60, TaskStatus.DONE),
                untimed("a", Importance.HIGH),
                untimed("b", Importance.HIGH),
            ),
            DialHalf.AM,
            ReflowMode.CompactRemaining,
        )
        assertTrue(slices.none { it.task.status == TaskStatus.DONE })
        val sweep = slices.sumOf { TimeMath.sliceSweepDeg(it.startMinute, it.endMinute).toDouble() }
        assertEquals(360.0, sweep, 0.01)
        assertEquals(listOf(360, 360), slices.map { it.endMinute - it.startMinute })
    }

    @Test
    fun sportTwoBlocks_twoSectorsSameId() {
        val sport = Task(
            id = TaskId("sport"),
            title = "Sport",
            colorArgb = 0xFFE8B4B8,
            blocks = listOf(TimeBlock(8 * 60, 9 * 60), TimeBlock(12 * 60, 13 * 60)),
            date = day,
        )
        val am = Reflow.layout(listOf(sport), DialHalf.AM, ReflowMode.WallClock)
        val pm = Reflow.layout(listOf(sport), DialHalf.PM, ReflowMode.WallClock)
        assertEquals(1, am.size)
        assertEquals(8 * 60, am.single().startMinute)
        assertEquals(9 * 60, am.single().endMinute)
        assertEquals(1, pm.size)
        assertEquals(12 * 60, pm.single().startMinute)
        assertEquals(sport.id, am.single().task.id)
        assertEquals(sport.id, pm.single().task.id)
    }

    @Test
    fun twoBlocksOnSameHalf_twoSectorsSameId() {
        val sport = Task(
            id = TaskId("sport"),
            title = "Sport",
            colorArgb = 0xFFE8B4B8,
            blocks = listOf(TimeBlock(8 * 60, 9 * 60), TimeBlock(10 * 60, 11 * 60)),
            date = day,
        )
        val am = Reflow.layout(listOf(sport), DialHalf.AM, ReflowMode.WallClock)
        assertEquals(2, am.size)
        assertTrue(am.all { it.task.id.value == "sport" })
        assertEquals(8 * 60, am[0].startMinute)
        assertEquals(10 * 60, am[1].startMinute)
    }
}
