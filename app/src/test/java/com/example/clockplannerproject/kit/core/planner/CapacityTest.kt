package com.example.clockplannerproject.kit.core.planner

import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.RecurrenceRule
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CapacityTest {

    private val friday = LocalDate(2026, 11, 6)
    private val saturday = LocalDate(2026, 11, 7)
    private val monday = LocalDate(2026, 11, 2)

    @Test
    fun bufferedMinutes_roundsUpByOneFifth() {
        assertEquals(144, Capacity.bufferedMinutesOf(120))
        assertEquals(180, Capacity.bufferedMinutesOf(150))
        assertEquals(720, Capacity.bufferedMinutesOf(600))
        assertEquals(2, Capacity.bufferedMinutesOf(1))
        assertEquals(0, Capacity.bufferedMinutesOf(0))
    }

    @Test
    fun meeting_10to12_isNotMoved() {
        val meeting = task(
            id = "meet",
            title = "Встреча",
            date = friday,
            blocks = listOf(TimeBlock(10 * 60, 12 * 60)),
        )
        val before = meeting.blocks
        val result = place(
            tasks = listOf(meeting),
            steps = listOf(estimate("a", 120), estimate("b", 150), estimate("c", 120)),
        )
        assertEquals(before, meeting.blocks)
        assertEquals(PlanVerdict.TIGHT, result.verdict)
        assertEquals(480, result.freeMinutes)
        assertEquals(384, result.usableMinutes)
        assertEquals(468, result.requiredMinutes)
        assertTrue(result.steps.none { it.unplaced })
        result.steps.forEach { step ->
            assertFalse(overlaps(step.startMinute!!, step.endMinute!!, 10 * 60, 12 * 60))
        }
    }

    @Test
    fun openTimer_doesNotOccupyTomorrow() {
        val running = task(
            id = "run",
            date = friday,
            blocks = listOf(TimeBlock(15 * 60, endMinute = null)),
        )
        val result = place(
            deadline = saturday,
            nowMinute = 16 * 60,
            tasks = listOf(running),
            steps = listOf(estimate("deep", 500)),
        )
        assertEquals(PlanVerdict.FITS, result.verdict)
        val step = result.steps.single()
        assertEquals(saturday, step.date)
        assertEquals(9 * 60, step.startMinute)
        assertEquals(19 * 60, step.endMinute)
    }

    @Test
    fun dailyRule_occupiesFutureDayWhenSeriesHasNoInstance() {
        val template = task(
            id = "daily",
            date = monday,
            blocks = listOf(TimeBlock(10 * 60, 12 * 60)),
            recurrence = RecurrenceRule.Daily,
            seriesId = "daily",
        )
        val result = place(
            tasks = listOf(template),
            steps = listOf(estimate("write", 90)),
        )
        val step = result.steps.single()
        assertFalse(step.unplaced)
        assertEquals(12 * 60, step.startMinute)
        assertEquals(12 * 60 + 108, step.endMinute)
    }

    @Test
    fun dailyRule_usesSeriesInstanceInsteadOfTemplate() {
        val template = task(
            id = "daily",
            date = monday,
            blocks = listOf(TimeBlock(10 * 60, 12 * 60)),
            recurrence = RecurrenceRule.Daily,
            seriesId = "daily",
        )
        val instance = task(
            id = "inst",
            date = friday,
            blocks = listOf(TimeBlock(15 * 60, 16 * 60)),
            seriesId = "daily",
        )
        val result = place(
            tasks = listOf(template, instance),
            steps = listOf(estimate("write", 90)),
        )
        assertEquals(9 * 60, result.steps.single().startMinute)
    }

    @Test
    fun fits_shortStepsInsideNineToSeven() {
        val result = place(
            steps = listOf(estimate("a", 60), estimate("b", 60), estimate("c", 60)),
        )
        assertEquals(PlanVerdict.FITS, result.verdict)
        assertEquals(600, result.freeMinutes)
        assertEquals(480, result.usableMinutes)
        assertEquals(216, result.requiredMinutes)
        assertEquals(listOf(9 * 60, 9 * 60 + 72, 9 * 60 + 144), result.steps.map { it.startMinute })
        assertTrue(result.steps.all { it.placedMinutes == 72 && !it.unplaced })
    }

    @Test
    fun tight_whenRequiredSitsBetweenUsableAndFree() {
        val result = place(steps = listOf(estimate("long", 500)))
        assertEquals(PlanVerdict.TIGHT, result.verdict)
        assertEquals(600, result.freeMinutes)
        assertEquals(480, result.usableMinutes)
        assertEquals(600, result.requiredMinutes)
        val step = result.steps.single()
        assertEquals(500, step.placedMinutes)
        assertEquals(9 * 60, step.startMinute)
    }

    @Test
    fun impossible_whenNoSingleWindowFits() {
        val busy = task(
            id = "mid",
            date = friday,
            blocks = listOf(TimeBlock(10 * 60, 11 * 60)),
        )
        val result = place(
            workStartMinute = 9 * 60,
            workEndMinute = 12 * 60,
            tasks = listOf(busy),
            steps = listOf(estimate("gap", 90)),
        )
        assertEquals(PlanVerdict.IMPOSSIBLE, result.verdict)
        assertEquals(120, result.freeMinutes)
        assertEquals(96, result.usableMinutes)
        assertEquals(108, result.requiredMinutes)
        val step = result.steps.single()
        assertTrue(step.unplaced)
        assertNull(step.startMinute)
        assertNull(step.endMinute)
        assertNull(step.date)
    }

    @Test
    fun impossible_whenRequiredExceedsFree() {
        val result = place(
            steps = List(4) { index -> estimate("part$index", 600) },
        )
        assertEquals(PlanVerdict.IMPOSSIBLE, result.verdict)
        assertTrue(result.requiredMinutes > result.freeMinutes)
        assertEquals(600, result.freeMinutes)
        assertEquals(2880, result.requiredMinutes)
        assertEquals(3, result.steps.count { it.unplaced })
        assertEquals(1, result.steps.count { !it.unplaced })
    }

    @Test
    fun overnight_morningTailOccupiesTheNextDay() {
        val asleep = task(
            id = "sleep",
            date = friday,
            blocks = listOf(TimeBlock(22 * 60, 2 * 60)),
        )
        val result = place(
            today = saturday,
            deadline = saturday,
            tasks = listOf(asleep),
            workStartMinute = 0,
            workEndMinute = 4 * 60,
            steps = listOf(estimate("wake", 150)),
        )
        assertEquals(120, result.freeMinutes)
        assertEquals(PlanVerdict.IMPOSSIBLE, result.verdict)
        assertTrue(result.steps.single().unplaced)
    }

    private fun place(
        today: LocalDate = friday,
        deadline: LocalDate = friday,
        nowMinute: Int = 8 * 60,
        workStartMinute: Int = 9 * 60,
        workEndMinute: Int = 19 * 60,
        tasks: List<Task> = emptyList(),
        steps: List<StepEstimate>,
    ): CapacityResult = Capacity.place(
        today = today,
        deadline = deadline,
        nowMinute = nowMinute,
        workStartMinute = workStartMinute,
        workEndMinute = workEndMinute,
        tasks = tasks,
        steps = steps,
    )

    private fun estimate(title: String, minutes: Int) = StepEstimate(
        title = title,
        estimateMinutes = minutes,
        bufferedMinutes = Capacity.bufferedMinutesOf(minutes),
        importance = Importance.MEDIUM,
        project = "goal",
    )

    private fun task(
        id: String,
        title: String = id,
        date: LocalDate?,
        blocks: List<TimeBlock>,
        recurrence: RecurrenceRule = RecurrenceRule.None,
        seriesId: String? = null,
    ) = Task(
        id = TaskId(id),
        title = title,
        colorArgb = 1L,
        blocks = blocks,
        date = date,
        recurrence = recurrence,
        seriesId = seriesId,
    )

    private fun overlaps(start: Int, end: Int, busyStart: Int, busyEnd: Int): Boolean =
        start < busyEnd && end > busyStart
}
