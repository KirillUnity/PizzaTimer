package com.example.clockplannerproject.kit.core

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Source of planned tasks. The kit does not know about Room or calendars.
 *
 * @since 0.1.0
 */
interface TaskRepository {
    /**
     * Emits the task list for [date] whenever that day changes.
     */
    fun observeTasks(date: LocalDate): Flow<List<Task>>

    /** Emits tasks in the undated backlog. */
    fun observeUnscheduled(): Flow<List<Task>>

    /** Emits all dated and undated tasks. */
    fun observeAll(): Flow<List<Task>>

    /**
     * Inserts or replaces a task with the same [Task.id].
     */
    suspend fun upsert(task: Task)

    /**
     * Deletes a task by id. No-op if the id is unknown.
     */
    suspend fun delete(taskId: TaskId)

    /**
     * Single row by id, or null.
     *
     * @since 0.4.0
     */
    suspend fun get(taskId: TaskId): Task?

    /**
     * Templates with a non-[RecurrenceRule.None] rule. Used to materialize
     * the visible day only.
     *
     * @since 0.4.0
     */
    suspend fun listRecurringTemplates(): List<Task>

    /**
     * True when this series already has a row on [date] (template or instance).
     *
     * @since 0.4.0
     */
    suspend fun hasSeriesOnDate(seriesId: String, date: LocalDate): Boolean
}
