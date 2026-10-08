package com.example.clockplannerproject.kit.core

import kotlinx.coroutines.flow.Flow

/**
 * Stable identifier for a text report attached to a [Task].
 *
 * @since 0.5.0
 */
@JvmInline
value class ReportId(val value: String)

/**
 * Thin v0.5 report: text only. No files, picker, or cloud.
 *
 * @param createdAtEpochMillis UTC millis when the note was first saved
 * @since 0.5.0
 */
data class TaskReport(
    val id: ReportId,
    val taskId: TaskId,
    val createdAtEpochMillis: Long,
    val text: String,
)

/**
 * Persistence for [TaskReport]. Implementations live in `data`.
 *
 * @since 0.5.0
 */
interface ReportRepository {
    fun observeReports(taskId: TaskId): Flow<List<TaskReport>>

    fun observeAllReports(): Flow<List<TaskReport>>

    suspend fun upsert(report: TaskReport)

    suspend fun delete(id: ReportId)
}
