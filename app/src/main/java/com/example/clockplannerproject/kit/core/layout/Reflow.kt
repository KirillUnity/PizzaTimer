package com.example.clockplannerproject.kit.core.layout

import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.clipTaskToHalf
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlin.math.max
import kotlin.math.min

/**
 * How remaining work is placed on a 12-hour face.
 *
 * @since 0.2.0
 */
enum class ReflowMode {
    /** Civil minutes. Gaps stay as leftover / Rest. Now sits at 12 o'clock. */
    WallClock,

    /**
     * DONE tasks are omitted first. Timed-only remaining work is scaled so
     * sweeps fill 360°. When untimed tasks remain, timed slices stay civil
     * and leftover (including minutes vacated by DONE) is packed with
     * importance weights — not a list filter.
     *
     * Sector angle in timed-only compact is **share of remaining work**,
     * not wall-clock hours. Labels must still show real start–end. The now
     * chevron stays at 12 as "now" and is not bound to a sector.
     */
    CompactRemaining,
}

/**
 * One painted slice. [startMinute]/[endMinute] are layout minutes on [half];
 * [task] keeps civil blocks for the sheet and labels.
 *
 * @since 0.2.0
 */
data class ReflowSlice(
    val task: Task,
    val startMinute: Int,
    val endMinute: Int,
)

/**
 * Day geometry: civil timed clips, leftover untimed shares, optional compact.
 * DIAL and PIZZA must call this API; UI must not invent leftover percents.
 *
 * @since 0.2.0
 */
object Reflow {
    /**
     * @param tasks domain tasks (Rest is ignored)
     * @param half 12-hour face
     * @param mode wall-clock vs compact remaining
     * @param nowMinute local minute of day; open timer blocks paint start → now
     */
    fun layout(
        tasks: List<Task>,
        half: DialHalf,
        mode: ReflowMode,
        nowMinute: Int = 0,
    ): List<ReflowSlice> {
        val real = tasks.filterNot(RestGaps::isRest).map { it.resolveOpenBlocks(nowMinute) }
        val pool = when (mode) {
            ReflowMode.WallClock -> real
            ReflowMode.CompactRemaining -> real.filter { it.status != TaskStatus.DONE }
        }
        val timed = pool.filterNot { it.isUntimed }
        val untimed = pool.filter { it.isUntimed }
        if (mode == ReflowMode.CompactRemaining && untimed.isEmpty()) {
            return compact(timed, half)
        }
        val timedSlices = timed.flatMap { task ->
            clipTaskToHalf(task, half).map { slice ->
                ReflowSlice(task, slice.startMinute, slice.endMinute)
            }
        }
        val gaps = leftoverGaps(timed, half)
        val leftoverMinutes = gaps.sumOf { (start, end) -> end - start }
        if (leftoverMinutes <= 0 || untimed.isEmpty()) return timedSlices
        val packed = packUntimed(untimed, leftoverMinutes, gaps)
        return timedSlices + packed
    }

    /**
     * Overlay copy used by Canvas: one [TimeBlock] per painted slice so
     * hit-test/draw stay on layout minutes while [ReflowSlice.task] keeps id.
     *
     * @since 0.3.0
     */
    fun toLayoutTask(slice: ReflowSlice): Task =
        slice.task.copy(blocks = listOf(TimeBlock(slice.startMinute, slice.endMinute)))

    private fun leftoverGaps(timed: List<Task>, half: DialHalf): List<Pair<Int, Int>> {
        val occupied = mergeOccupied(timed, half)
        val gaps = mutableListOf<Pair<Int, Int>>()
        var cursor = half.startMinute
        occupied.forEach { (start, end) ->
            if (start > cursor) gaps += cursor to start
            cursor = max(cursor, end)
        }
        if (cursor < half.endMinute) gaps += cursor to half.endMinute
        return gaps
    }

    private fun mergeOccupied(tasks: List<Task>, half: DialHalf): List<Pair<Int, Int>> {
        val raw = tasks.flatMap { clipTaskToHalf(it, half) }
            .map { it.startMinute to it.endMinute }
            .sortedBy { it.first }
        if (raw.isEmpty()) return emptyList()
        val merged = mutableListOf<Pair<Int, Int>>()
        var current = raw.first()
        raw.drop(1).forEach { next ->
            current = if (next.first <= current.second) {
                current.first to max(current.second, next.second)
            } else {
                merged += current
                next
            }
        }
        merged += current
        return merged
    }

    private fun packUntimed(
        untimed: List<Task>,
        leftoverMinutes: Int,
        gaps: List<Pair<Int, Int>>,
    ): List<ReflowSlice> {
        val shares = LeftoverShares.splitMinutes(untimed, leftoverMinutes)
        val out = mutableListOf<ReflowSlice>()
        var gapIndex = 0
        var cursor = gaps.firstOrNull()?.first ?: return emptyList()
        for ((task, minutes) in shares) {
            var remaining = minutes
            while (remaining > 0 && gapIndex < gaps.size) {
                val (gapStart, gapEnd) = gaps[gapIndex]
                if (cursor < gapStart) cursor = gapStart
                val available = gapEnd - cursor
                if (available <= 0) {
                    gapIndex += 1
                    if (gapIndex < gaps.size) cursor = gaps[gapIndex].first
                    continue
                }
                val take = min(remaining, available)
                if (take > 0) {
                    out += ReflowSlice(task, cursor, cursor + take)
                    cursor += take
                    remaining -= take
                }
                if (cursor >= gapEnd) {
                    gapIndex += 1
                    if (gapIndex < gaps.size) cursor = gaps[gapIndex].first
                }
            }
        }
        return out
    }

    private fun compact(tasks: List<Task>, half: DialHalf): List<ReflowSlice> {
        val visible = tasks
            .flatMap { task -> clipTaskToHalf(task, half).map { slice -> task to slice } }
            .sortedBy { it.second.startMinute }
        if (visible.isEmpty()) return emptyList()
        val weights = visible.map { (_, slice) ->
            (slice.endMinute - slice.startMinute).coerceAtLeast(0)
        }
        val totalWeight = weights.sum()
        if (totalWeight <= 0) return emptyList()
        var cursor = half.startMinute
        var remainingMinutes = TimeMath.MINUTES_PER_HALF
        var remainingWeight = totalWeight
        return visible.mapIndexed { index, (task, _) ->
            val weight = weights[index]
            val share = if (index == visible.lastIndex) {
                remainingMinutes
            } else {
                (weight.toLong() * remainingMinutes / remainingWeight)
                    .toInt()
                    .coerceAtLeast(1)
                    .coerceAtMost(remainingMinutes - (visible.size - 1 - index))
            }
            val start = cursor
            val end = start + share
            cursor = end
            remainingMinutes -= share
            remainingWeight -= weight
            ReflowSlice(task, start, end)
        }
    }
}
