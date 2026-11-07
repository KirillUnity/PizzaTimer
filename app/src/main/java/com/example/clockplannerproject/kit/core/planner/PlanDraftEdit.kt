package com.example.clockplannerproject.kit.core.planner

import androidx.compose.runtime.Immutable
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.LocalDate

/**
 * Calendar slice used to recompute a draft on the phone.
 *
 * [tasks] are the dial tasks already stored. Draft steps are not included;
 * [PlanDraftEdit] adds the other draft rows itself when a step moves.
 *
 * @param today device-local calendar day
 * @param deadline last day that may receive a step
 * @param nowMinute minute that closes an open timer on [today]
 * @param workStartMinute inclusive start of the shared work window
 * @param workEndMinute exclusive end of the shared work window
 * @param tasks instances and recurrence templates visible to capacity
 * @since 1.1.0
 */
@Immutable
data class PlacementContext(
    val today: LocalDate,
    val deadline: LocalDate,
    val nowMinute: Int,
    val workStartMinute: Int,
    val workEndMinute: Int,
    val tasks: List<Task>,
)

/**
 * Local edits of a plan draft.
 *
 * Removing a step or moving it inside the deadline does not call
 * [PlannerSource]. Estimates already on the rows are reused. The verdict
 * follows the same FITS / TIGHT / IMPOSSIBLE rules as [Capacity.place].
 *
 * @since 1.1.0
 */
object PlanDraftEdit {
    /**
     * Drops the step at [index] and places the remaining estimates again.
     *
     * Unplaced rows stay in the list. [PlanDraft.draftId] is kept. An index
     * outside the list returns [draft] unchanged.
     *
     * @param draft draft currently on screen
     * @param index step to remove
     * @param context calendar used for the new placement
     * @param phrases sentences for the recomputed verdict
     * @return draft with one fewer step, or [draft] when [index] is out of range
     * @since 1.1.0
     */
    fun withoutStep(
        draft: PlanDraft,
        index: Int,
        context: PlacementContext,
        phrases: PlannerPhrases,
    ): PlanDraft {
        if (index !in draft.steps.indices) return draft
        val remaining = draft.steps.filterIndexed { stepIndex, _ -> stepIndex != index }
        val placed = Capacity.place(
            today = context.today,
            deadline = context.deadline,
            nowMinute = context.nowMinute,
            workStartMinute = context.workStartMinute,
            workEndMinute = context.workEndMinute,
            tasks = context.tasks,
            steps = remaining.map { it.toEstimate() },
        )
        return draft.copy(
            verdict = placed.verdict,
            freeMinutes = placed.freeMinutes,
            usableMinutes = placed.usableMinutes,
            requiredMinutes = placed.requiredMinutes,
            steps = placed.steps,
            narrative = narrative(placed.verdict, placed.freeMinutes, placed.usableMinutes, placed.requiredMinutes, phrases),
        )
    }

    /**
     * Moves the step at [index] onto [date].
     *
     * The date must lie on [PlacementContext.today] through
     * [PlacementContext.deadline]. Other steps stay where they are. When that
     * day has no single free window of the piece length, the step becomes
     * unplaced and the verdict becomes [PlanVerdict.IMPOSSIBLE].
     *
     * @param draft draft currently on screen
     * @param index step to move
     * @param date day inside the deadline
     * @param context calendar, not including this draft's own rows
     * @param phrases sentences for the recomputed verdict
     * @return updated draft, or [draft] when the index or the day is rejected
     * @since 1.1.0
     */
    fun moveStep(
        draft: PlanDraft,
        index: Int,
        date: LocalDate,
        context: PlacementContext,
        phrases: PlannerPhrases,
    ): PlanDraft {
        val step = draft.steps.getOrNull(index) ?: return draft
        if (date < context.today || date > context.deadline) return draft
        val length = pieceLength(draft, step)
        val occupancy = context.tasks + draft.steps.mapIndexedNotNull { stepIndex, other ->
            if (stepIndex == index) null else other.asBlockingTask(stepIndex)
        }
        val fit = Capacity.earliestFit(
            date = date,
            today = context.today,
            nowMinute = context.nowMinute,
            workStartMinute = context.workStartMinute,
            workEndMinute = context.workEndMinute,
            tasks = occupancy,
            length = length,
        )
        val moved = if (fit == null) {
            step.copy(date = null, startMinute = null, endMinute = null, unplaced = true)
        } else {
            step.copy(
                date = date,
                startMinute = fit.first,
                endMinute = fit.second,
                unplaced = false,
            )
        }
        val steps = draft.steps.mapIndexed { stepIndex, current ->
            if (stepIndex == index) moved else current
        }
        val required = steps.sumOf { it.bufferedMinutes }
        val verdict = judge(steps, draft.freeMinutes, draft.usableMinutes)
        return draft.copy(
            verdict = verdict,
            requiredMinutes = required,
            steps = steps,
            narrative = narrative(verdict, draft.freeMinutes, draft.usableMinutes, required, phrases),
        )
    }
}

private fun narrative(
    verdict: PlanVerdict,
    freeMinutes: Int,
    usableMinutes: Int,
    requiredMinutes: Int,
    phrases: PlannerPhrases,
): String = when (verdict) {
    PlanVerdict.FITS -> phrases.fits(freeMinutes, usableMinutes, requiredMinutes)
    PlanVerdict.TIGHT -> phrases.tight(freeMinutes, usableMinutes, requiredMinutes)
    PlanVerdict.IMPOSSIBLE -> phrases.impossible((requiredMinutes - freeMinutes).coerceAtLeast(0))
}

private fun judge(steps: List<PlacedStep>, freeMinutes: Int, usableMinutes: Int): PlanVerdict {
    val required = steps.sumOf { it.bufferedMinutes }
    if (steps.any { it.unplaced }) return PlanVerdict.IMPOSSIBLE
    return when {
        required <= usableMinutes -> PlanVerdict.FITS
        required <= freeMinutes -> PlanVerdict.TIGHT
        else -> PlanVerdict.IMPOSSIBLE
    }
}

private fun pieceLength(draft: PlanDraft, step: PlacedStep): Int {
    if (!step.unplaced && step.placedMinutes > 0) return step.placedMinutes
    return if (draft.requiredMinutes <= draft.usableMinutes) {
        step.bufferedMinutes
    } else {
        step.estimateMinutes
    }
}

private fun PlacedStep.toEstimate(): StepEstimate = StepEstimate(
    title = title,
    estimateMinutes = estimateMinutes,
    bufferedMinutes = bufferedMinutes,
    importance = importance,
    project = project,
)

private fun PlacedStep.asBlockingTask(index: Int): Task? {
    if (unplaced) return null
    val day = date ?: return null
    val start = startMinute ?: return null
    val end = endMinute ?: return null
    if (end <= start) return null
    return Task(
        id = TaskId("draft-step-$index"),
        title = title,
        colorArgb = 0L,
        blocks = listOf(TimeBlock(start, end)),
        date = day,
        importance = importance,
        project = project,
    )
}
