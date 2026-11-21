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
     * One day of a Daily or Weekdays template. Intervals start empty.
     * The series id points at the template; the face is applied later.
     *
     * @param template recurring main task
     * @param date day that owns the new intervals
     * @param newId id for this subtask
     * @since 0.5.0
     */
    fun daySubtask(
        template: Task,
        date: LocalDate,
        newId: TaskId,
    ): Task = template.copy(
        id = newId,
        date = date,
        status = TaskStatus.TODO,
        recurrence = RecurrenceRule.None,
        seriesId = template.seriesId ?: template.id.value,
        blocks = emptyList(),
    )

    /**
     * Materialized occurrence of a template for [date]. The new row is a
     * [daySubtask]: it does not copy the template’s intervals.
     */
    fun materialize(
        template: Task,
        date: LocalDate,
        newId: TaskId,
    ): Task = daySubtask(template, date, newId)
}

/**
 * Shows the template’s name and look on a subtask. Intervals, date, and status stay on the subtask.
 *
 * @param template main Daily or Weekdays task for this series
 * @since 0.5.0
 */
fun Task.withTemplateFace(template: Task): Task = copy(
    title = template.title,
    description = template.description,
    colorArgb = template.colorArgb,
    importance = template.importance,
    project = template.project,
    tags = template.tags,
)

/**
 * Replaces each subtask’s face with its template. Rows that are templates, or have no template, stay as stored.
 *
 * @param catalog tasks that include the series templates; defaults to this list
 * @since 0.5.0
 */
fun List<Task>.showingTemplateFaces(catalog: List<Task> = this): List<Task> {
    val templates = catalog.filter { it.recurrence !is RecurrenceRule.None }
    val bySeries = templates.associateBy { it.seriesId ?: it.id.value }
    return map { task ->
        if (task.recurrence !is RecurrenceRule.None) return@map task
        val series = task.seriesId ?: return@map task
        val template = bySeries[series] ?: return@map task
        if (template.id == task.id) task else task.withTemplateFace(template)
    }
}

/**
 * Moves [task] onto [date]. A Daily or Weekdays template stays on its date and gains one empty
 * subtask when that series has no row on [date]. Any other task keeps its id and intervals.
 *
 * @param newId id used only when a subtask is created
 * @since 0.5.0
 */
suspend fun placeOnDate(
    repository: TaskRepository,
    task: Task,
    date: LocalDate,
    newId: () -> TaskId,
) {
    if (task.recurrence is RecurrenceRule.None) {
        repository.upsert(TaskInstances.moved(task, date))
        return
    }
    if (task.date == date) return
    val series = task.seriesId ?: task.id.value
    if (repository.hasSeriesOnDate(series, date)) return
    repository.upsert(TaskInstances.daySubtask(task, date, newId()))
}
