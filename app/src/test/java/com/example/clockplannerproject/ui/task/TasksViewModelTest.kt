package com.example.clockplannerproject.ui.task

import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.ReportRepository
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskDraftError
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskReport
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val date = LocalDate(2026, 10, 6)

    @Before
    fun setMain() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun reset() {
        Dispatchers.resetMain()
    }

    @Test
    fun update_rejectsEmptyTitle() {
        val viewModel = TasksViewModel(FakeTaskRepository(), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.Create)
        viewModel.onIntent(TasksUiIntent.ChangeTitle("  "))
        viewModel.onIntent(TasksUiIntent.Update)

        val editor = viewModel.state.value.editor
        assertEquals(TaskDraftError.EmptyTitle, editor?.error)
        assertTrue(viewModel.state.value.tasks.isEmpty())
    }

    @Test
    fun update_persistsValidTask() {
        val repository = FakeTaskRepository()
        val viewModel = TasksViewModel(repository, FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.Create)
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Focus"))
        viewModel.onIntent(TasksUiIntent.ChangeScheduled(true))
        viewModel.onIntent(TasksUiIntent.AddBlock)
        viewModel.onIntent(TasksUiIntent.ChangeBlockStart(0, 9 * 60))
        viewModel.onIntent(TasksUiIntent.ChangeBlockEnd(0, 10 * 60))
        viewModel.onIntent(TasksUiIntent.Update)

        assertNull(viewModel.state.value.editor)
        assertEquals(1, viewModel.state.value.tasks.size)
        assertEquals("Focus", viewModel.state.value.tasks.single().title)
    }

    @Test
    fun update_persistsOvernightRange() {
        val viewModel = TasksViewModel(FakeTaskRepository(), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.Create)
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Sleep"))
        viewModel.onIntent(TasksUiIntent.ChangeScheduled(true))
        viewModel.onIntent(TasksUiIntent.AddBlock)
        viewModel.onIntent(TasksUiIntent.ChangeBlockStart(0, 22 * 60))
        viewModel.onIntent(TasksUiIntent.ChangeBlockEnd(0, 6 * 60))
        viewModel.onIntent(TasksUiIntent.Update)

        val saved = viewModel.state.value.tasks.single()
        assertEquals(22 * 60, saved.startMinute)
        assertEquals(6 * 60, saved.endMinute)
        assertNull(viewModel.state.value.editor)
    }

    @Test
    fun edit_updatesExistingTitle() {
        val task = Task(
            id = TaskId("keep"),
            title = "Focus",
            colorArgb = 0xFF3949AB,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = date,
        )
        val viewModel = TasksViewModel(FakeTaskRepository(listOf(task)), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.Edit(task))
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Deep work"))
        viewModel.onIntent(TasksUiIntent.Update)

        assertEquals("Deep work", viewModel.state.value.tasks.single().title)
        assertEquals(task.id, viewModel.state.value.tasks.single().id)
        assertNull(viewModel.state.value.editor)
    }

    @Test
    fun confirmDelete_removesTask() {
        val task = Task(
            id = TaskId("keep"),
            title = "Focus",
            colorArgb = 0xFF3949AB,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = date,
        )
        val repository = FakeTaskRepository(listOf(task))
        val viewModel = TasksViewModel(repository, FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.Delete(task))
        viewModel.onIntent(TasksUiIntent.ConfirmDelete)

        assertTrue(viewModel.state.value.tasks.isEmpty())
        assertNull(viewModel.state.value.pendingDelete)
    }

    @Test
    fun selectDate_switchesObservedDay() {
        val other = LocalDate(2026, 10, 8)
        val task = Task(
            id = TaskId("later"),
            title = "Later",
            colorArgb = 0xFF3949AB,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = other,
        )
        val viewModel = TasksViewModel(FakeTaskRepository(listOf(task)), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.SelectDate(other))
        assertEquals(other, viewModel.state.value.date)
        assertEquals("Later", viewModel.state.value.tasks.single().title)
        assertEquals(false, viewModel.state.value.showDatePicker)
    }

    @Test
    fun update_savesUntimedWhenTitleNonEmpty() {
        val viewModel = TasksViewModel(FakeTaskRepository(), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.Create)
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Inbox"))
        viewModel.onIntent(TasksUiIntent.ClearTime)
        viewModel.onIntent(TasksUiIntent.Update)
        val saved = viewModel.state.value.tasks.single()
        assertTrue(saved.isUntimed)
        assertEquals("Inbox", saved.title)
    }

    @Test
    fun update_persistsOneNormalizedProject() {
        val viewModel = TasksViewModel(FakeTaskRepository(), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.Create)
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Focus"))
        viewModel.onIntent(TasksUiIntent.ChangeProject("  Deep Work  "))
        viewModel.onIntent(TasksUiIntent.Update)
        assertEquals("Deep Work", viewModel.state.value.tasks.single().project)
        assertNull(viewModel.state.value.tasks.single().date)
    }

    @Test
    fun duplicate_createsIndependentCopy() {
        val task = Task(
            id = TaskId("keep"),
            title = "Focus",
            colorArgb = 0xFF3949AB,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = date,
            project = "code",
        )
        val viewModel = TasksViewModel(FakeTaskRepository(listOf(task)), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.Duplicate(task))
        assertEquals(2, viewModel.state.value.tasks.size)
        val copy = viewModel.state.value.tasks.single { it.id != task.id }
        viewModel.onIntent(TasksUiIntent.Edit(copy))
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Copy"))
        viewModel.onIntent(TasksUiIntent.Update)
        val titles = viewModel.state.value.tasks.map { it.title }.toSet()
        assertTrue("Focus" in titles)
        assertTrue("Copy" in titles)
    }

    @Test
    fun confirmMove_changesObservedDay() {
        val task = Task(
            id = TaskId("keep"),
            title = "Focus",
            colorArgb = 0xFF3949AB,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = date,
        )
        val dest = LocalDate(2026, 10, 8)
        val viewModel = TasksViewModel(FakeTaskRepository(listOf(task)), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.RequestMove(task))
        viewModel.onIntent(TasksUiIntent.ConfirmMove(dest))
        assertEquals(dest, viewModel.state.value.tasks.single().date)
        viewModel.onIntent(TasksUiIntent.SelectDate(dest))
        assertEquals("Focus", viewModel.state.value.tasks.single().title)
        assertEquals(task.id, viewModel.state.value.tasks.single().id)
    }

    @Test
    fun filterByProject_narrowsList() {
        val work = Task(
            id = TaskId("a"),
            title = "A",
            colorArgb = 0xFF3949AB,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = date,
            project = "Code",
        )
        val gym = Task(
            id = TaskId("b"),
            title = "B",
            colorArgb = 0xFFE8B4B8,
            date = date,
            project = "Sport",
        )
        val viewModel = TasksViewModel(FakeTaskRepository(listOf(work, gym)), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.FilterByProject("sport"))
        assertEquals(listOf("B"), viewModel.state.value.visibleTasks.map { it.title })
    }

    @Test
    fun projectSearch_isCaseInsensitiveSubstring_andSortIsDeterministic() {
        val tasks = listOf(
            Task(TaskId("3"), "Zulu", colorArgb = 1, date = null, project = "Research"),
            Task(TaskId("2"), "Alpha", colorArgb = 1, date = null, project = "research"),
            Task(TaskId("1"), "Other", colorArgb = 1, date = date, project = "Home"),
        )
        val viewModel = TasksViewModel(
            FakeTaskRepository(tasks),
            FakeTimeProvider(date),
            FakeReportRepository(),
        )
        viewModel.onIntent(TasksUiIntent.ChangeProjectSearch("SEA"))

        assertEquals(listOf("research"), viewModel.state.value.availableProjects.map { it.lowercase() })
        assertEquals(listOf("Alpha", "Zulu"), viewModel.state.value.visibleTasks.map { it.title })
    }

    @Test
    fun moveToDiagram_assignsSelectedDate_preservesProject_andClearsBlocks() {
        val backlog = Task(
            id = TaskId("backlog"),
            title = "Plan",
            colorArgb = 1,
            date = null,
            project = "Launch",
        )
        val viewModel = TasksViewModel(
            FakeTaskRepository(listOf(backlog)),
            FakeTimeProvider(date),
            FakeReportRepository(),
        )

        viewModel.onIntent(TasksUiIntent.MoveToDiagram(backlog))

        val moved = viewModel.state.value.tasks.single()
        assertEquals(date, moved.date)
        assertEquals("Launch", moved.project)
        assertTrue(moved.blocks.isEmpty())
    }

    @Test
    fun saveReport_upsertsForEditedTask() {
        val task = Task(
            id = TaskId("keep"),
            title = "Focus",
            colorArgb = 0xFF3949AB,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = date,
        )
        val reports = FakeReportRepository()
        val viewModel = TasksViewModel(
            FakeTaskRepository(listOf(task)),
            FakeTimeProvider(date),
            reports,
        )
        viewModel.onIntent(TasksUiIntent.Edit(task))
        viewModel.onIntent(TasksUiIntent.ChangeReportDraft("Shipped the ring."))
        viewModel.onIntent(TasksUiIntent.SaveReport)
        assertEquals(1, viewModel.state.value.reports.size)
        assertEquals("Shipped the ring.", viewModel.state.value.reports.single().text)
        assertEquals(task.id, viewModel.state.value.reports.single().taskId)
    }
}

private class FakeTaskRepository(
    initial: List<Task> = emptyList(),
) : TaskRepository {
    private val items = MutableStateFlow(initial)

    override fun observeTasks(date: LocalDate): Flow<List<Task>> =
        items.map { list -> list.filter { it.date == date } }

    override fun observeUnscheduled(): Flow<List<Task>> =
        items.map { list -> list.filter { it.date == null } }

    override fun observeAll(): Flow<List<Task>> = items

    override suspend fun upsert(task: Task) {
        items.update { current -> current.filterNot { it.id == task.id } + task }
    }

    override suspend fun delete(taskId: TaskId) {
        items.update { current -> current.filterNot { it.id == taskId } }
    }

    override suspend fun get(taskId: TaskId): Task? = items.value.find { it.id == taskId }

    override suspend fun listRecurringTemplates(): List<Task> =
        items.value.filter { it.recurrence !is com.example.clockplannerproject.kit.core.RecurrenceRule.None }

    override suspend fun hasSeriesOnDate(seriesId: String, date: LocalDate): Boolean =
        items.value.any { task ->
            task.date == date && (task.seriesId == seriesId || task.id.value == seriesId)
        }
}

private class FakeReportRepository : ReportRepository {
    private val items = MutableStateFlow<List<TaskReport>>(emptyList())

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

private class FakeTimeProvider(
    private val date: LocalDate,
    private val time: LocalTime = LocalTime(9, 0),
) : TimeProvider {
    override fun now(): LocalTime = time
    override fun today(): LocalDate = date
    override fun observeTime(): Flow<LocalTime> = flowOf(time)
}
