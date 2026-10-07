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

    /**
     * Inserts or replaces a task with the same [Task.id].
     */
    suspend fun upsert(task: Task)

    /**
     * Deletes a task by id. No-op if the id is unknown.
     */
    suspend fun delete(taskId: TaskId)
}
