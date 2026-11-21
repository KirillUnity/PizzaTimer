package com.example.clockplannerproject.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.clockplannerproject.kit.core.ReportAttachment
import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskReport

/**
 * Room row for [TaskReport]. Optional attachment columns point at a file
 * under the app files directory. Schema changes bump [TimeDialDatabase]
 * version and rely on destructive migration.
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
    val attachmentRelativePath: String? = null,
    val attachmentDisplayName: String? = null,
    val attachmentMimeType: String? = null,
    val blockId: String? = null,
)

fun TaskReportEntity.toDomain(): TaskReport = TaskReport(
    id = ReportId(id),
    taskId = TaskId(taskId),
    createdAtEpochMillis = createdAtEpochMillis,
    text = text,
    attachment = attachmentOrNull(),
    blockId = blockId,
)

fun TaskReport.toEntity(): TaskReportEntity = TaskReportEntity(
    id = id.value,
    taskId = taskId.value,
    createdAtEpochMillis = createdAtEpochMillis,
    text = text,
    attachmentRelativePath = attachment?.relativePath,
    attachmentDisplayName = attachment?.displayName,
    attachmentMimeType = attachment?.mimeType,
    blockId = blockId,
)

private fun TaskReportEntity.attachmentOrNull(): ReportAttachment? {
    val path = attachmentRelativePath ?: return null
    return ReportAttachment(
        relativePath = path,
        displayName = attachmentDisplayName ?: path.substringAfterLast('/'),
        mimeType = attachmentMimeType ?: "application/octet-stream",
    )
}
