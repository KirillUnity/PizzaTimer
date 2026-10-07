package com.example.clockplannerproject.kit.core.layout

import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReflowTest {

    private val day = LocalDate(2026, 10, 6)

    private fun task(
        id: String,
        start: Int,
        end: Int,
        status: TaskStatus = TaskStatus.TODO,
    ) = Task(
        id = TaskId(id),
        title = id,
        colorArgb = 0xFFA8C5A0,
        blocks = listOf(TimeBlock(start, end)),
        status = status,
        date = day,
    )

    @Test
    fun compact_twoTodoOneDone_sweepsSumTo360() {
        val tasks = listOf(
            task("a", 9 * 60, 10 * 60),
            task("b", 10 * 60, 11 * 60),
            task("c", 11 * 60, 12 * 60, TaskStatus.DONE),
        )
        val slices = Reflow.layout(tasks, DialHalf.AM, ReflowMode.CompactRemaining)
        assertEquals(2, slices.size)
        val sweep = slices.sumOf { slice ->
            TimeMath.sliceSweepDeg(slice.startMinute, slice.endMinute).toDouble()
        }
        assertEquals(360.0, sweep, 0.01)
        assertTrue(slices.none { it.task.status == TaskStatus.DONE })
    }

    @Test
    fun compact_preservesStartMinuteOrder() {
        val tasks = listOf(
            task("late", 11 * 60, 12 * 60),
            task("early", 8 * 60, 9 * 60),
        )
        val slices = Reflow.layout(tasks, DialHalf.AM, ReflowMode.CompactRemaining)
        assertEquals(listOf("early", "late"), slices.map { it.task.id.value })
        assertEquals(0, slices.first().startMinute)
        assertEquals(TimeMath.MINUTES_PER_HALF, slices.last().endMinute)
    }

    @Test
    fun wallClock_keepsCivilMinutesAndDone() {
        val done = task("c", 11 * 60, 12 * 60, TaskStatus.DONE)
        val slices = Reflow.layout(listOf(done), DialHalf.AM, ReflowMode.WallClock)
        assertEquals(1, slices.size)
        assertEquals(11 * 60, slices.single().startMinute)
        assertEquals(12 * 60, slices.single().endMinute)
    }

    @Test
    fun compact_hidesDoneEntirely() {
        val slices = Reflow.layout(
            listOf(task("c", 9 * 60, 12 * 60, TaskStatus.DONE)),
            DialHalf.AM,
            ReflowMode.CompactRemaining,
        )
        assertTrue(slices.isEmpty())
    }

    @Test
    fun compact_keepsInProgress() {
        val slices = Reflow.layout(
            listOf(task("gym", 10 * 60, 11 * 60, TaskStatus.IN_PROGRESS)),
            DialHalf.AM,
            ReflowMode.CompactRemaining,
        )
        assertEquals(1, slices.size)
        assertEquals(0, slices.single().startMinute)
        assertEquals(TimeMath.MINUTES_PER_HALF, slices.single().endMinute)
    }

    @Test
    fun compact_sharesAreProportionalToDuration() {
        val slices = Reflow.layout(
            listOf(
                task("short", 9 * 60, 10 * 60),
                task("long", 10 * 60, 12 * 60),
            ),
            DialHalf.AM,
            ReflowMode.CompactRemaining,
        )
        assertEquals(2, slices.size)
        val shortSpan = slices[0].endMinute - slices[0].startMinute
        val longSpan = slices[1].endMinute - slices[1].startMinute
        assertEquals(TimeMath.MINUTES_PER_HALF, shortSpan + longSpan)
        assertEquals(2.0, longSpan.toDouble() / shortSpan, 0.01)
    }

    @Test
    fun compact_overnight_fillsEachHalfSeparately() {
        val sleep = task("sleep", 22 * 60, 6 * 60)
        val am = Reflow.layout(listOf(sleep), DialHalf.AM, ReflowMode.CompactRemaining)
        val pm = Reflow.layout(listOf(sleep), DialHalf.PM, ReflowMode.CompactRemaining)
        assertEquals(0, am.single().startMinute)
        assertEquals(TimeMath.MINUTES_PER_HALF, am.single().endMinute)
        assertEquals(DialHalf.PM.startMinute, pm.single().startMinute)
        assertEquals(TimeMath.MINUTES_PER_DAY, pm.single().endMinute)
    }

    @Test
    fun wallClock_openBlock_usesNowViaLayoutApi() {
        val live = Task(
            id = TaskId("live"),
            title = "Live",
            colorArgb = 0xFFE8B4B8,
            blocks = listOf(
                TimeBlock(8 * 60, 9 * 60),
                TimeBlock(12 * 60, endMinute = null),
            ),
            date = day,
        )
        val am = Reflow.layout(listOf(live), DialHalf.AM, ReflowMode.WallClock, nowMinute = 13 * 60)
        val pm = Reflow.layout(listOf(live), DialHalf.PM, ReflowMode.WallClock, nowMinute = 13 * 60)
        assertEquals(8 * 60, am.single().startMinute)
        assertEquals(9 * 60, am.single().endMinute)
        assertEquals(12 * 60, pm.single().startMinute)
        assertEquals(13 * 60, pm.single().endMinute)
    }

    @Test
    fun wallClock_twoSportBlocks_firstImmutable() {
        val sport = Task(
            id = TaskId("sport"),
            title = "Sport",
            colorArgb = 0xFFE8B4B8,
            blocks = listOf(TimeBlock(8 * 60, 9 * 60), TimeBlock(12 * 60, 13 * 60)),
            date = day,
        )
        val am = Reflow.layout(listOf(sport), DialHalf.AM, ReflowMode.WallClock)
        val pm = Reflow.layout(listOf(sport), DialHalf.PM, ReflowMode.WallClock)
        assertEquals(listOf(8 * 60 to 9 * 60), am.map { it.startMinute to it.endMinute })
        assertEquals(listOf(12 * 60 to 13 * 60), pm.map { it.startMinute to it.endMinute })
        assertEquals(sport.id, am.single().task.id)
        assertEquals(sport.id, pm.single().task.id)
    }

    @Test
    fun wallClock_overnight_keepsCivilClips() {
        val sleep = task("sleep", 22 * 60, 6 * 60)
        val am = Reflow.layout(listOf(sleep), DialHalf.AM, ReflowMode.WallClock)
        val pm = Reflow.layout(listOf(sleep), DialHalf.PM, ReflowMode.WallClock)
        assertEquals(0, am.single().startMinute)
        assertEquals(6 * 60, am.single().endMinute)
        assertEquals(22 * 60, pm.single().startMinute)
        assertEquals(24 * 60, pm.single().endMinute)
    }
}
