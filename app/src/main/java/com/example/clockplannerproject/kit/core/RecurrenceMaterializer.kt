package com.example.clockplannerproject.kit.core

import kotlinx.datetime.LocalDate
import java.util.UUID

/**
 * Creates at most one instance per template for the **visible** [LocalDate].
 * Does not pre-fill tomorrow, next week, or any other horizon.
 *
 * @since 0.4.0
 */
class RecurrenceMaterializer(
    private val repository: TaskRepository,
    private val newId: () -> TaskId = { TaskId(UUID.randomUUID().toString()) },
) {
    /**
     * Inserts missing occurrences for [date]. Templates whose own [Task.date]
     * is already [date] count as the instance for that day.
     */
    suspend fun ensureVisibleDay(date: LocalDate) {
        val templates = repository.listRecurringTemplates()
        for (template in templates) {
            if (!template.recurrence.occursOn(date)) continue
            val series = template.seriesId ?: template.id.value
            if (repository.hasSeriesOnDate(series, date)) continue
            repository.upsert(TaskInstances.materialize(template, date, newId()))
        }
    }
}
