package com.example.clockplannerproject.kit.core

import com.example.clockplannerproject.kit.core.layout.RestGaps
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlin.math.max
import kotlin.math.min

/**
 * One 12-hour face of the day. AM is `[00:00, 12:00)`, PM is `[12:00, 24:00)`.
 *
 * @since 0.2.0
 */
enum class DialHalf(
    val startMinute: Int,
    val endMinute: Int,
) {
    AM(0, TimeMath.MINUTES_PER_HALF),
    PM(TimeMath.MINUTES_PER_HALF, TimeMath.MINUTES_PER_DAY),
    ;

    fun contains(minuteOfDay: Float): Boolean {
        val normalized = minuteOfDay.mod(TimeMath.MINUTES_PER_DAY.toFloat())
        return normalized >= startMinute && normalized < endMinute
    }
}

/**
 * A piece of [task] that lies on one [DialHalf]. Overnight and noon-spanning
 * work is split; the original [Task] id is unchanged so a tap still opens the same card.
 *
 * @since 0.2.0
 */
data class TaskSlice(
    val task: Task,
    val startMinute: Int,
    val endMinute: Int,
)

/**
 * Clips [task] onto [half]. Overnight ranges become two civil intervals first,
 * then each interval is intersected with the half.
 *
 * @since 0.2.0
 */
fun clipTaskToHalf(task: Task, half: DialHalf): List<TaskSlice> =
    task.blocks.flatMap { block ->
        val civilEnd = block.endMinute ?: return@flatMap emptyList()
        unfoldTaskIntervals(block.startMinute, civilEnd).mapNotNull { (start, end) ->
            val left = max(start, half.startMinute)
            val right = min(end, half.endMinute)
            if (right > left) TaskSlice(task, left, right) else null
        }
    }

/**
 * Slice used for the description callout on [half]: the open timer if any,
 * otherwise the latest civil start on this face (not the first 8–9 when 12–13 exists).
 *
 * @since 0.4.0
 */
fun calloutSlice(task: Task, half: DialHalf, nowMinute: Int): TaskSlice? {
    val slices = clipTaskToHalf(task.resolveOpenBlocks(nowMinute), half)
    if (slices.isEmpty()) return null
    val openStart = task.blocks.find { it.isOpen }?.startMinute
    if (openStart != null) {
        val clippedOpen = max(openStart.mod(TimeMath.MINUTES_PER_DAY), half.startMinute)
        return slices.lastOrNull { it.startMinute == clippedOpen }
            ?: slices.maxByOrNull { it.startMinute }
    }
    return slices.maxByOrNull { it.startMinute }
}

/**
 * Civil slices on [half] that may still be resized. Open timers and closed
 * past blocks are omitted. A closed first interval does not lock later ones.
 *
 * @since 0.4.0
 */
fun resizableSlices(task: Task, half: DialHalf, nowMinute: Int): List<TaskSlice> {
    if (task.blocks.any { it.isOpen }) return emptyList()
    return task.blocks.filterNot { it.isClosed(nowMinute) }.flatMap { block ->
        val civilEnd = block.endMinute ?: return@flatMap emptyList()
        unfoldTaskIntervals(block.startMinute, civilEnd).mapNotNull { (start, end) ->
            val left = max(start, half.startMinute)
            val right = min(end, half.endMinute)
            if (right > left) TaskSlice(task, left, right) else null
        }
    }
}

/**
 * 12 o'clock anchor for [half]: now, or the nearest real task start.
 *
 * @since 0.2.0
 */
fun halfAnchorMinute(nowMinute: Float, half: DialHalf, tasks: List<Task>): Float =
    TimeMath.halfAnchorMinute(
        nowMinute = nowMinute,
        half = half,
        slicesStartEnd = tasks.filterNot(RestGaps::isRest)
            .flatMap { clipTaskToHalf(it, half) }
            .map { it.startMinute to it.endMinute },
    )


/**
 * Civil overnight (`end < start`) becomes two half-open intervals.
 * Exclusive midnight [TimeMath.MINUTES_PER_DAY] stays on the PM face
 * (`22:00–24:00`) and is not re-wrapped as a new overnight task.
 */
internal fun unfoldTaskIntervals(startMinute: Int, endMinute: Int): List<Pair<Int, Int>> {
    val start = startMinute.mod(TimeMath.MINUTES_PER_DAY)
    if (endMinute == TimeMath.MINUTES_PER_DAY && start < TimeMath.MINUTES_PER_DAY) {
        return listOf(start to TimeMath.MINUTES_PER_DAY)
    }
    val end = endMinute.mod(TimeMath.MINUTES_PER_DAY)
    if (start == end) return emptyList()
    return if (end > start) {
        listOf(start to end)
    } else {
        listOf(start to TimeMath.MINUTES_PER_DAY, 0 to end)
    }
}
