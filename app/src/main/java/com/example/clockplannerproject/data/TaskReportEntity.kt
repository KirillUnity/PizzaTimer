package com.example.clockplannerproject.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskReport

/**
 * Room row for [TaskReport]. Text only in v0.5.
 *
 * @since 0.5.0
 */
@Entity(
    tableName = "task_reports",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("taskId")],
)
data class TaskReportEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val createdAtEpochMillis: Long,
    val text: String,
)

fun TaskReportEntity.toDomain(): TaskReport = TaskReport(
    id = ReportId(id),
    taskId = TaskId(taskId),
    createdAtEpochMillis = createdAtEpochMillis,
    text = text,
)

fun TaskReport.toEntity(): TaskReportEntity = TaskReportEntity(
    id = id.value,
    taskId = taskId.value,
    createdAtEpochMillis = createdAtEpochMillis,
    text = text,
)
