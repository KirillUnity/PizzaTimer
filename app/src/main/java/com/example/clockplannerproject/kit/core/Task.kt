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
 * @property date local calendar day this instance belongs to, or `null` while
 * the task is in the undated backlog.
 * @property colorArgb packed ARGB color, parsed outside the Canvas draw loop.
 * @property importance leftover weight only; ignored for civil blocks.
 * @property project optional single project. Whitespace is normalized by
 * [normalizedProject].
 * @property recurrence template rule; materialized copies use [RecurrenceRule.None].
 * @property seriesId shared id for a template and its day instances.
 * @since 0.1.0
 */
data class Task(
    val id: TaskId,
    val title: String,
    val description: String = "",
    val colorArgb: Long,
    val blocks: List<TimeBlock> = emptyList(),
    val status: TaskStatus = TaskStatus.TODO,
    val date: LocalDate?,
    val importance: Importance = Importance.MEDIUM,
    val project: String? = null,
    val recurrence: RecurrenceRule = RecurrenceRule.None,
    val seriesId: String? = null,
) {
    /** Trimmed project name, or `null` when no project is assigned. */
    val normalizedProject: String?
        get() = project?.trim()?.takeIf { it.isNotEmpty() }

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
     * Replaces open timer ends with [nowMinute] so layout / [clipTaskToHalf]
     * see civil closed intervals. Stored [blocks] on the real task stay open.
     *
     * @since 0.4.0
     */
    fun resolveOpenBlocks(nowMinute: Int): Task {
        if (blocks.none { it.isOpen }) return this
        return copy(blocks = blocks.map { it.resolveForLayout(nowMinute) })
    }

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
    val end = block.endMinute ?: return minute >= start
    if (end == start) return false
    return if (end > start) {
        minute in start until end
    } else {
        minute >= start || minute < end
    }
}
