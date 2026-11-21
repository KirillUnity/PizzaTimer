package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.ReportRepository
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [ReportRepository] for previews and JVM tests.
 *
 * @since 1.2.0
 */
class InMemoryReportRepository(
    initial: List<TaskReport> = emptyList(),
) : ReportRepository {
    private val items = MutableStateFlow(initial)

    override fun observeReports(taskId: TaskId): Flow<List<TaskReport>> =
        items.map { list -> list.filter { it.taskId == taskId } }

    override fun observeAllReports(): Flow<List<TaskReport>> = items

    override suspend fun upsert(report: TaskReport) {
        items.value = items.value.filterNot { it.id == report.id } + report
    }

    override suspend fun delete(id: ReportId) {
        items.value = items.value.filterNot { it.id == id }
    }

    /** Current rows, for tests. */
    fun snapshot(): List<TaskReport> = items.value
}
