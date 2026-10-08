package com.example.clockplannerproject.kit.core

import kotlinx.datetime.LocalDate

/**
 * Move / duplicate helpers. These do **not** generate a recurrence series.
 *
 * @since 0.4.0
 */
object TaskInstances {
    /**
     * Same [Task.id], new calendar [date]. Project and civil blocks are kept.
     */
    fun moved(task: Task, date: LocalDate): Task = task.copy(date = date)

    /**
     * Independent copy: new [newId], same fields and blocks. Copied blocks are
     * inert data — they do not share identity with the original timer.
     *
     * @param date target day; defaults to the source instance date.
     */
    fun duplicated(
        task: Task,
        newId: TaskId,
        date: LocalDate? = task.date,
    ): Task = task.copy(
        id = newId,
        date = date,
        seriesId = null,
        recurrence = RecurrenceRule.None,
        blocks = task.blocks.map { it.copy() },
    )

    /**
     * Materialized occurrence of a template for [date]. Planned closed blocks
     * are copied as civil data; open timer slices are dropped so they do not
     * “come alive” on another day.
     */
    fun materialize(
        template: Task,
        date: LocalDate,
        newId: TaskId,
    ): Task = template.copy(
        id = newId,
        date = date,
        status = TaskStatus.TODO,
        recurrence = RecurrenceRule.None,
        seriesId = template.seriesId ?: template.id.value,
        blocks = template.blocks.mapNotNull { block ->
            val end = block.endMinute ?: return@mapNotNull null
            TimeBlock(block.startMinute, end)
        },
    )
}
