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
 * File copied into app storage and referenced by a [TaskReport].
 *
 * [relativePath] is relative to the app files directory (`report-media/...`).
 * Nothing is uploaded.
 *
 * @param displayName name shown in the report card
 * @param mimeType MIME type captured when the file was copied
 * @since 1.0.0
 */
data class ReportAttachment(
    val relativePath: String,
    val displayName: String,
    val mimeType: String,
) {
    /** True when the copied file should be previewed as a bitmap. */
    val isImage: Boolean get() = mimeType.startsWith("image/")
}

/**
 * Text note on a task, plus an optional local file.
 *
 * @param createdAtEpochMillis UTC millis when the note was first saved
 * @param attachment file under the app files directory, or null
 * @since 0.5.0
 */
data class TaskReport(
    val id: ReportId,
    val taskId: TaskId,
    val createdAtEpochMillis: Long,
    val text: String,
    val attachment: ReportAttachment? = null,
    /** Interval this note belongs to, or null for a note saved before intervals had notes. */
    val blockId: String? = null,
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

/**
 * Copies a user-picked content URI into app storage.
 *
 * Implementations live in `data`. The URI string is not stored; only the
 * resulting [ReportAttachment] path is.
 *
 * @since 1.0.0
 */
interface ReportAttachmentImporter {
    /**
     * @param sourceUri content URI from the system picker
     * @param reportId [ReportId.value] used in the stored file name
     */
    suspend fun import(sourceUri: String, reportId: String): ReportAttachment

    companion object {
        /**
         * Importer used when the graph has not supplied one (JVM tests).
         * Production binds [com.example.clockplannerproject.data.AndroidReportImporter].
         */
        val Unavailable: ReportAttachmentImporter = object : ReportAttachmentImporter {
            override suspend fun import(sourceUri: String, reportId: String): ReportAttachment {
                error("Report attachments are not available")
            }
        }
    }
}

/**
 * Deletes attachment files. Room rows are updated separately.
 *
 * @since 1.0.0
 */
interface ReportFileStore {
    /**
     * @param relativePath path previously stored on [ReportAttachment]
     */
    fun delete(relativePath: String)

    companion object {
        /** No files to remove. Used by JVM tests. */
        val NoOp: ReportFileStore = object : ReportFileStore {
            override fun delete(relativePath: String) = Unit
        }
    }
}
