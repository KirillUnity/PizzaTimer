package com.example.clockplannerproject.kit.core.planner

import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanDraftEditTest {

    private val today = LocalDate(2026, 11, 7)
    private val deadline = today.plus(1, DateTimeUnit.DAY)

    @Test
    fun moveOntoAFullDay_becomesUnplacedWithoutNewEstimates() {
        val meeting = Task(
            id = TaskId("meet"),
            title = "Встреча",
            colorArgb = 1L,
            blocks = listOf(TimeBlock(9 * 60, 19 * 60)),
            date = today,
        )
        val placed = Capacity.place(
            today = today,
            deadline = deadline,
            nowMinute = 8 * 60,
            workStartMinute = 9 * 60,
            workEndMinute = 19 * 60,
            tasks = listOf(meeting),
            steps = listOf(
                StepEstimate("Write", 120, 144, Importance.MEDIUM, "Goal"),
            ),
        )
        val step = placed.steps.single()
        assertFalse(step.unplaced)
        assertEquals(deadline, step.date)
        val draft = PlanDraft(
            draftId = "keep",
            verdict = placed.verdict,
            freeMinutes = placed.freeMinutes,
            usableMinutes = placed.usableMinutes,
            requiredMinutes = placed.requiredMinutes,
            steps = placed.steps,
            narrative = "before",
        )
        val moved = PlanDraftEdit.moveStep(
            draft = draft,
            index = 0,
            date = today,
            context = PlacementContext(
                today = today,
                deadline = deadline,
                nowMinute = 8 * 60,
                workStartMinute = 9 * 60,
                workEndMinute = 19 * 60,
                tasks = listOf(meeting),
            ),
            phrases = RussianPlannerPhrases,
        )
        assertEquals("keep", moved.draftId)
        assertTrue(moved.steps.single().unplaced)
        assertEquals(PlanVerdict.IMPOSSIBLE, moved.verdict)
        assertEquals(120, moved.steps.single().estimateMinutes)
        assertEquals(144, moved.steps.single().bufferedMinutes)
    }

    @Test
    fun withoutStep_keepsDraftIdAndDropsOnlyThatRow() {
        val draft = PlanDraft(
            draftId = "keep",
            verdict = PlanVerdict.FITS,
            freeMinutes = 600,
            usableMinutes = 480,
            requiredMinutes = 288,
            steps = listOf(
                placed("A", 9 * 60, 11 * 60 + 24),
                placed("B", 12 * 60, 15 * 60, estimate = 150, buffered = 180),
            ),
            narrative = "before",
        )
        val edited = PlanDraftEdit.withoutStep(
            draft = draft,
            index = 1,
            context = PlacementContext(
                today = today,
                deadline = today,
                nowMinute = 8 * 60,
                workStartMinute = 9 * 60,
                workEndMinute = 19 * 60,
                tasks = emptyList(),
            ),
            phrases = RussianPlannerPhrases,
        )
        assertEquals("keep", edited.draftId)
        assertEquals(listOf("A"), edited.steps.map { it.title })
        assertEquals(144, edited.requiredMinutes)
        assertFalse(edited.steps.single().unplaced)
        assertEquals(PlanVerdict.FITS, edited.verdict)
    }

    private fun placed(
        title: String,
        start: Int,
        end: Int,
        estimate: Int = 120,
        buffered: Int = 144,
    ) = PlacedStep(
        title = title,
        date = today,
        startMinute = start,
        endMinute = end,
        unplaced = false,
        estimateMinutes = estimate,
        bufferedMinutes = buffered,
        importance = Importance.MEDIUM,
        project = "Goal",
    )
}
