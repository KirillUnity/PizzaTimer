package com.example.clockplannerproject.kit.core.layout

import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.clipTaskToHalf
import kotlinx.datetime.LocalDate
import kotlin.math.max

/**
 * Synthetic "Rest" blocks that fill empty time on a 12-hour face.
 * Not stored in Room. Ids use [ID_PREFIX] so hit-test can ignore them.
 *
 * @since 0.2.0
 */
object RestGaps {
    const val ID_PREFIX: String = "rest:"

    fun isRest(task: Task): Boolean = isRest(task.id)

    fun isRest(id: TaskId): Boolean = id.value.startsWith(ID_PREFIX)

    /**
     * Occupied slices on [half] are merged; remaining intervals become Rest tasks.
     */
    fun fill(
        tasks: List<Task>,
        half: DialHalf,
        date: LocalDate,
        title: String,
        colorArgb: Long,
    ): List<Task> {
        val occupied = mergeOccupied(tasks.filterNot(::isRest), half)
        val gaps = mutableListOf<Pair<Int, Int>>()
        var cursor = half.startMinute
        occupied.forEach { (start, end) ->
            if (start > cursor) gaps += cursor to start
            cursor = max(cursor, end)
        }
        if (cursor < half.endMinute) gaps += cursor to half.endMinute
        return gaps.map { (start, end) ->
            Task(
                id = TaskId("$ID_PREFIX${half.name}-$start-$end"),
                title = title,
                colorArgb = colorArgb,
                blocks = listOf(TimeBlock(start, end)),
                status = TaskStatus.TODO,
                date = date,
            )
        }
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
}
