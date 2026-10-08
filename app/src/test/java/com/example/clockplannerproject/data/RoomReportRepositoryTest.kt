package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM stand-in for in-memory Room reports. Schema wipe is the product path
 * (version++ + fallbackToDestructiveMigration, no Migration class).
 */
class RoomReportRepositoryTest {

    @Test
    fun upsert_thenObserveReports_byTaskId() = runTest {
        val dao = InMemoryReportDao()
        val repository = RoomReportRepository(dao, Dispatchers.Unconfined)
        val sport = TaskId("sport")
        val other = TaskId("other")
        val note = TaskReport(
            id = ReportId("r1"),
            taskId = sport,
            createdAtEpochMillis = 1_000L,
            text = "Morning loop",
        )
        repository.upsert(note)
        repository.upsert(
            TaskReport(
                id = ReportId("r2"),
                taskId = other,
                createdAtEpochMillis = 2_000L,
                text = "Unrelated",
            ),
        )

        val forSport = repository.observeReports(sport).first()
        assertEquals(1, forSport.size)
        assertEquals(note, forSport.single())
        assertEquals(2, repository.observeAllReports().first().size)
    }

    @Test
    fun upsert_replacesSameId() = runTest {
        val repository = RoomReportRepository(InMemoryReportDao(), Dispatchers.Unconfined)
        val report = TaskReport(
            id = ReportId("r1"),
            taskId = TaskId("sport"),
            createdAtEpochMillis = 1_000L,
            text = "Draft",
        )
        repository.upsert(report)
        repository.upsert(report.copy(text = "Final"))
        val listed = repository.observeReports(TaskId("sport")).first()
        assertEquals(1, listed.size)
        assertEquals("Final", listed.single().text)
        assertTrue(listed.single().createdAtEpochMillis == 1_000L)
    }
}

private class InMemoryReportDao : ReportDao {
    private val rows = MutableStateFlow<List<TaskReportEntity>>(emptyList())

    override fun observeByTask(taskId: String): Flow<List<TaskReportEntity>> =
        rows.map { list ->
            list.filter { it.taskId == taskId }.sortedByDescending { it.createdAtEpochMillis }
        }

    override fun observeAll(): Flow<List<TaskReportEntity>> =
        rows.map { list -> list.sortedByDescending { it.createdAtEpochMillis } }

    override suspend fun upsert(entity: TaskReportEntity) {
        rows.update { current -> current.filterNot { it.id == entity.id } + entity }
    }

    override suspend fun deleteById(id: String) {
        rows.update { current -> current.filterNot { it.id == id } }
    }
}
