package com.example.clockplannerproject.ui.stats

import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.ReportRepository
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setMain() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun reset() {
        Dispatchers.resetMain()
    }

    @Test
    fun editThenSave_updatesText() {
        val existing = TaskReport(
            id = ReportId("r1"),
            taskId = TaskId("sport"),
            createdAtEpochMillis = 5L,
            text = "Draft",
        )
        val repo = FakeReports(listOf(existing))
        val viewModel = ReportsViewModel(repo)
        viewModel.onIntent(ReportsUiIntent.Edit(existing))
        viewModel.onIntent(ReportsUiIntent.ChangeDraft("Shipped"))
        viewModel.onIntent(ReportsUiIntent.Save)
        assertEquals("Shipped", viewModel.state.value.reports.single().text)
        assertEquals(5L, viewModel.state.value.reports.single().createdAtEpochMillis)
    }
}

private class FakeReports(
    initial: List<TaskReport>,
) : ReportRepository {
    private val items = MutableStateFlow(initial)

    override fun observeReports(taskId: TaskId): Flow<List<TaskReport>> =
        items.map { list -> list.filter { it.taskId == taskId } }

    override fun observeAllReports(): Flow<List<TaskReport>> = items

    override suspend fun upsert(report: TaskReport) {
        items.update { current -> current.filterNot { it.id == report.id } + report }
    }

    override suspend fun delete(id: ReportId) {
        items.update { current -> current.filterNot { it.id == id } }
    }
}
