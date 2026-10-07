package com.example.clockplannerproject.kit.core

import kotlinx.datetime.LocalDate

/**
 * Stable identifier for a planned task.
 *
 * @since 0.1.0
 */
@JvmInline
value class TaskId(val value: String)

/**
 * Lifecycle of a task on the dial.
 *
 * @since 0.1.0
 */
enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE,
}

/**
 * Domain task for the TimeDial planner.
 *
 * Civil geometry comes from [blocks], never from a single pair of minutes on
 * the task itself. An empty [blocks] list is an **untimed** task (leftover).
 * Several blocks share this [id] and paint as several sectors.
 *
 * @property date local calendar day this instance belongs to.
 * @property colorArgb packed ARGB color, parsed outside the Canvas draw loop.
 * @property importance leftover weight only; ignored for civil blocks.
 * @property tags free-form labels; not a category catalog.
 * @since 0.1.0
 */
data class Task(
    val id: TaskId,
    val title: String,
    val description: String = "",
    val colorArgb: Long,
    val blocks: List<TimeBlock> = emptyList(),
    val status: TaskStatus = TaskStatus.TODO,
    val date: LocalDate,
    val importance: Importance = Importance.MEDIUM,
    val tags: List<String> = emptyList(),
) {
    /** True when the task has no civil intervals. */
    val isUntimed: Boolean get() = blocks.isEmpty()

    /**
     * First block start, or `Int.MAX_VALUE` when untimed (sorts after timed).
     * Not a source of truth for angles.
     */
    val sortMinute: Int get() = blocks.minOfOrNull { it.startMinute } ?: Int.MAX_VALUE

    /**
     * Convenience for single-block overlays and tests. Prefer [blocks].
     */
    val startMinute: Int get() = blocks.firstOrNull()?.startMinute ?: 0

    /**
     * Convenience for single-block overlays and tests. Prefer [blocks].
     */
    val endMinute: Int get() = blocks.lastOrNull()?.endMinute ?: 0

    /**
     * Replaces civil end of a single block, or the block that covers
     * [matchingStartMinute] when several exist.
     *
     * @since 0.3.0
     */
    fun withEndMinute(endMinute: Int, matchingStartMinute: Int? = null): Task {
        if (blocks.isEmpty()) return this
        if (blocks.size == 1) {
            return copy(blocks = listOf(blocks.first().copy(endMinute = endMinute)))
        }
        val match = matchingStartMinute ?: startMinute
        val index = blocks.indexOfFirst { block ->
            block.startMinute == match || blockCoversMinute(block, match)
        }.takeIf { it >= 0 } ?: return this
        return copy(
            blocks = blocks.mapIndexed { i, block ->
                if (i == index) block.copy(endMinute = endMinute) else block
            },
        )
    }

    /**
     * Sets a single civil interval (tests and compact overlays).
     *
     * @since 0.3.0
     */
    fun withRange(startMinute: Int, endMinute: Int): Task =
        copy(blocks = listOf(TimeBlock(startMinute, endMinute)))
}

private fun blockCoversMinute(block: TimeBlock, minute: Int): Boolean {
    val start = block.startMinute
    val end = block.endMinute
    if (end == start) return false
    return if (end > start) {
        minute in start until end
    } else {
        minute >= start || minute < end
    }
}
