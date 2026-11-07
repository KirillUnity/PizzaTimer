package com.example.clockplannerproject.kit.core.planner

import com.example.clockplannerproject.kit.core.RecurrenceRule
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Free-window math for the goal planner.
 *
 * No UI and no narrative. Existing blocks are never moved, and a step is
 * never split across windows or days.
 *
 * @since 1.1.0
 */
object Capacity {
    /**
     * `ceil(estimateMinutes * 1.2)` for a non-negative estimate, else 0.
     *
     * @param estimateMinutes raw step length in minutes
     * @return buffered length used as [CapacityResult.requiredMinutes]
     * @since 1.1.0
     */
    fun bufferedMinutesOf(estimateMinutes: Int): Int {
        if (estimateMinutes <= 0) return 0
        val buffered = ((estimateMinutes.toLong() * BUFFER_NUMERATOR) + BUFFER_ROUNDING) /
            BUFFER_DENOMINATOR
        return buffered.toInt()
    }

    /**
     * Occupies the horizon, then places [steps] greedily.
     *
     * Closed blocks on an instance of [today] through [deadline] count, plus
     * template blocks whose recurrence occurs that day when the series has no
     * instance yet. An open block reserves `[start, nowMinute)` on [today]
     * only. Overnight blocks (`end < start`) split at midnight.
     *
     * The longest step takes the earliest single window that can hold it.
     * [PlanVerdict.FITS] places [StepEstimate.bufferedMinutes] when that sum
     * is within [CapacityResult.usableMinutes]. [PlanVerdict.TIGHT] places
     * [StepEstimate.estimateMinutes] when the buffered sum sits strictly
     * between usable and free. Otherwise the verdict is [PlanVerdict.IMPOSSIBLE]
     * and any step without a whole window is [PlacedStep.unplaced].
     *
     * @param today device-local calendar day; open timers apply only here
     * @param deadline last inclusive day that may receive a step
     * @param nowMinute minute of [today] that closes an open timer
     * @param workStartMinute inclusive start of the shared work window
     * @param workEndMinute exclusive end of the shared work window
     * @param tasks instances and templates; this function does not write them
     * @param steps estimates in display order
     * @return verdict and one placed or unplaced row per estimate
     * @since 1.1.0
     */
    fun place(
        today: LocalDate,
        deadline: LocalDate,
        nowMinute: Int,
        workStartMinute: Int,
        workEndMinute: Int,
        tasks: List<Task>,
        steps: List<StepEstimate>,
    ): CapacityResult {
        val normalized = steps.map { step ->
            step.copy(bufferedMinutes = bufferedMinutesOf(step.estimateMinutes))
        }
        val required = normalized.sumOf { it.bufferedMinutes }
        if (deadline < today || !isValidWorkWindow(workStartMinute, workEndMinute)) {
            return CapacityResult(
                verdict = PlanVerdict.IMPOSSIBLE,
                freeMinutes = 0,
                usableMinutes = 0,
                requiredMinutes = required,
                steps = normalized.map { it.toUnplaced() },
            )
        }
        val days = horizon(today, deadline).map { date ->
            val busy = clipToWork(
                intervalsOn(date, today, nowMinute, tasks),
                workStartMinute,
                workEndMinute,
            )
            DayFree(date, freeWindows(workStartMinute, workEndMinute, busy))
        }
        val free = days.sumOf { day -> day.windows.sumOf { it.length } }
        val usable = days.sumOf { day ->
            val dayFree = day.windows.sumOf { it.length }
            (dayFree * USABLE_NUMERATOR) / USABLE_DENOMINATOR
        }
        val bufferedPlacement = placeAll(days, normalized, useBufferedLength = true)
        val estimatePlacement = placeAll(days, normalized, useBufferedLength = false)
        val allBuffered = bufferedPlacement.none { it.unplaced }
        val allEstimate = estimatePlacement.none { it.unplaced }
        val (verdict, placed) = when {
            allBuffered && required <= usable -> PlanVerdict.FITS to bufferedPlacement
            allEstimate && usable < required && required <= free -> PlanVerdict.TIGHT to estimatePlacement
            required <= usable -> PlanVerdict.IMPOSSIBLE to bufferedPlacement
            else -> PlanVerdict.IMPOSSIBLE to estimatePlacement
        }
        return CapacityResult(
            verdict = verdict,
            freeMinutes = free,
            usableMinutes = usable,
            requiredMinutes = required,
            steps = placed,
        )
    }

    /**
     * Earliest whole free window on [date] that can hold [length] minutes.
     *
     * Occupancy matches [place]: closed blocks, recurrence templates, and an
     * open timer only on [today]. Existing blocks are not moved. A length that
     * does not fit in one window returns null.
     *
     * @param date day to search
     * @param today device-local day; open timers apply only here
     * @param nowMinute minute that closes an open timer on [today]
     * @param workStartMinute inclusive start of the work window
     * @param workEndMinute exclusive end of the work window
     * @param tasks instances and templates already on the dial
     * @param length minutes the piece needs, greater than zero
     * @return inclusive start and exclusive end, or null when no single window fits
     * @since 1.1.0
     */
    fun earliestFit(
        date: LocalDate,
        today: LocalDate,
        nowMinute: Int,
        workStartMinute: Int,
        workEndMinute: Int,
        tasks: List<Task>,
        length: Int,
    ): Pair<Int, Int>? {
        if (length <= 0 || !isValidWorkWindow(workStartMinute, workEndMinute)) return null
        val busy = clipToWork(
            intervalsOn(date, today, nowMinute, tasks),
            workStartMinute,
            workEndMinute,
        )
        val window = freeWindows(workStartMinute, workEndMinute, busy)
            .firstOrNull { it.length >= length }
            ?: return null
        return window.start to window.start + length
    }
}

private const val BUFFER_NUMERATOR: Long = 6L
private const val BUFFER_DENOMINATOR: Long = 5L
private const val BUFFER_ROUNDING: Long = 4L
private const val USABLE_NUMERATOR: Int = 4
private const val USABLE_DENOMINATOR: Int = 5

private data class Span(val start: Int, val end: Int) {
    val length: Int get() = (end - start).coerceAtLeast(0)
}

private data class DayFree(val date: LocalDate, val windows: List<Span>)

private fun isValidWorkWindow(start: Int, end: Int): Boolean =
    start >= 0 && end <= TimeMath.MINUTES_PER_DAY && start < end

private fun horizon(today: LocalDate, deadline: LocalDate): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    var cursor = today
    while (cursor <= deadline) {
        dates += cursor
        cursor = cursor.plus(1, DateTimeUnit.DAY)
    }
    return dates
}

private fun intervalsOn(
    date: LocalDate,
    today: LocalDate,
    nowMinute: Int,
    tasks: List<Task>,
): List<Span> {
    val spans = mutableListOf<Span>()
    for (task in contributingTasks(date, tasks)) {
        for (block in task.blocks) {
            spans += spansForBlock(date, today, nowMinute, block, includeMorningTail = false)
        }
    }
    val previous = date.minus(1, DateTimeUnit.DAY)
    for (task in contributingTasks(previous, tasks)) {
        for (block in task.blocks) {
            spans += spansForBlock(date, today, nowMinute, block, includeMorningTail = true)
        }
    }
    return spans
}

private fun spansForBlock(
    date: LocalDate,
    today: LocalDate,
    nowMinute: Int,
    block: TimeBlock,
    includeMorningTail: Boolean,
): List<Span> {
    val endRaw = block.endMinute
    if (endRaw == null) {
        if (includeMorningTail || date != today) return emptyList()
        val start = block.startMinute.mod(TimeMath.MINUTES_PER_DAY)
        val now = nowMinute.mod(TimeMath.MINUTES_PER_DAY)
        return if (now > start) listOf(Span(start, now)) else emptyList()
    }
    val start = block.startMinute.mod(TimeMath.MINUTES_PER_DAY)
    val end = if (endRaw == TimeMath.MINUTES_PER_DAY) {
        TimeMath.MINUTES_PER_DAY
    } else {
        endRaw.mod(TimeMath.MINUTES_PER_DAY)
    }
    if (end == start) return emptyList()
    if (end > start) {
        return if (includeMorningTail) emptyList() else listOf(Span(start, end))
    }
    return if (includeMorningTail) {
        if (end > 0) listOf(Span(0, end)) else emptyList()
    } else {
        listOf(Span(start, TimeMath.MINUTES_PER_DAY))
    }
}

private fun contributingTasks(date: LocalDate, tasks: List<Task>): List<Task> {
    val dated = tasks.filter { it.date == date }
    val templates = tasks.filter { template ->
        template.recurrence !is RecurrenceRule.None &&
            template.recurrence.occursOn(date) &&
            !seriesPresent(tasks, template, date)
    }
    return dated + templates
}

private fun seriesPresent(tasks: List<Task>, template: Task, date: LocalDate): Boolean {
    val series = template.seriesId ?: template.id.value
    return tasks.any { task ->
        task.date == date && (task.seriesId == series || task.id.value == series)
    }
}

private fun clipToWork(spans: List<Span>, workStart: Int, workEnd: Int): List<Span> =
    spans.mapNotNull { span ->
        val start = maxOf(span.start, workStart)
        val end = minOf(span.end, workEnd)
        if (end > start) Span(start, end) else null
    }

private fun freeWindows(workStart: Int, workEnd: Int, busy: List<Span>): List<Span> {
    val merged = merge(busy)
    val free = mutableListOf<Span>()
    var cursor = workStart
    for (span in merged) {
        if (span.start > cursor) free += Span(cursor, span.start)
        cursor = maxOf(cursor, span.end)
    }
    if (cursor < workEnd) free += Span(cursor, workEnd)
    return free
}

private fun merge(spans: List<Span>): List<Span> {
    if (spans.isEmpty()) return emptyList()
    val sorted = spans.sortedBy { it.start }
    val merged = mutableListOf(sorted.first())
    for (span in sorted.drop(1)) {
        val last = merged.last()
        if (span.start > last.end) {
            merged += span
        } else if (span.end > last.end) {
            merged[merged.lastIndex] = Span(last.start, span.end)
        }
    }
    return merged
}

private fun placeAll(
    days: List<DayFree>,
    steps: List<StepEstimate>,
    useBufferedLength: Boolean,
): List<PlacedStep> {
    val windows = days.map { day ->
        day.date to day.windows.map { Span(it.start, it.end) }.toMutableList()
    }
    val order = steps.indices.sortedWith(
        compareByDescending<Int> { index ->
            pieceLength(steps[index], useBufferedLength)
        }.thenBy { it },
    )
    val placed = arrayOfNulls<PlacedStep>(steps.size)
    for (index in order) {
        val step = steps[index]
        val length = pieceLength(step, useBufferedLength)
        val slot = earliestSlot(windows, length)
        placed[index] = if (slot == null) {
            step.toUnplaced()
        } else {
            val (dayIndex, windowIndex) = slot
            val day = windows[dayIndex]
            val window = day.second[windowIndex]
            val end = window.start + length
            if (end == window.end) {
                day.second.removeAt(windowIndex)
            } else {
                day.second[windowIndex] = Span(end, window.end)
            }
            step.toPlaced(day.first, window.start, end)
        }
    }
    return List(steps.size) { index -> placed[index]!! }
}

private fun pieceLength(step: StepEstimate, useBufferedLength: Boolean): Int =
    if (useBufferedLength) step.bufferedMinutes else step.estimateMinutes

private fun earliestSlot(
    windows: List<Pair<LocalDate, MutableList<Span>>>,
    length: Int,
): Pair<Int, Int>? {
    if (length <= 0) return null
    for (dayIndex in windows.indices) {
        val dayWindows = windows[dayIndex].second
        for (windowIndex in dayWindows.indices) {
            if (dayWindows[windowIndex].length >= length) return dayIndex to windowIndex
        }
    }
    return null
}

private fun StepEstimate.toUnplaced(): PlacedStep = PlacedStep(
    title = title,
    date = null,
    startMinute = null,
    endMinute = null,
    unplaced = true,
    estimateMinutes = estimateMinutes,
    bufferedMinutes = bufferedMinutes,
    importance = importance,
    project = project,
)

private fun StepEstimate.toPlaced(date: LocalDate, start: Int, end: Int): PlacedStep = PlacedStep(
    title = title,
    date = date,
    startMinute = start,
    endMinute = end,
    unplaced = false,
    estimateMinutes = estimateMinutes,
    bufferedMinutes = bufferedMinutes,
    importance = importance,
    project = project,
)
