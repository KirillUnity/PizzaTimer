package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.ReportFileStore
import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.ReportRepository
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskReport
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * [ReportRepository] backed by Room.
 *
 * @since 0.5.0
 */
class RoomReportRepository(
    private val reportDao: ReportDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val fileStore: ReportFileStore = ReportFileStore.NoOp,
) : ReportRepository {
    override fun observeReports(taskId: TaskId): Flow<List<TaskReport>> =
        reportDao.observeByTask(taskId.value)
            .map { rows -> rows.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override fun observeAllReports(): Flow<List<TaskReport>> =
        reportDao.observeAll()
            .map { rows -> rows.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun upsert(report: TaskReport) {
        withContext(ioDispatcher) {
            val previous = reportDao.findById(report.id.value)?.attachmentRelativePath
            reportDao.upsert(report.toEntity())
            val next = report.attachment?.relativePath
            if (previous != null && previous != next) {
                fileStore.delete(previous)
            }
        }
    }

    override suspend fun delete(id: ReportId) {
        withContext(ioDispatcher) {
            val previous = reportDao.findById(id.value)?.attachmentRelativePath
            reportDao.deleteById(id.value)
            if (previous != null) {
                fileStore.delete(previous)
            }
        }
    }
}
