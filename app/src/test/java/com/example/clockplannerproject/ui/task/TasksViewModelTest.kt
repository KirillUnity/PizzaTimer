package com.example.clockplannerproject.ui.task

import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskDraftError
import com.example.clockplannerproject.kit.core.TaskId
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
        val viewModel = TasksViewModel(FakeTaskRepository(), FakeTimeProvider(date))
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
        val viewModel = TasksViewModel(repository, FakeTimeProvider(date))
        viewModel.onIntent(TasksUiIntent.Create)
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Focus"))
        viewModel.onIntent(TasksUiIntent.ChangeBlockStart(0, 9 * 60))
        viewModel.onIntent(TasksUiIntent.ChangeBlockEnd(0, 10 * 60))
        viewModel.onIntent(TasksUiIntent.Update)

        assertNull(viewModel.state.value.editor)
        assertEquals(1, viewModel.state.value.tasks.size)
        assertEquals("Focus", viewModel.state.value.tasks.single().title)
    }

    @Test
    fun update_persistsOvernightRange() {
        val viewModel = TasksViewModel(FakeTaskRepository(), FakeTimeProvider(date))
        viewModel.onIntent(TasksUiIntent.Create)
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Sleep"))
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
        val viewModel = TasksViewModel(FakeTaskRepository(listOf(task)), FakeTimeProvider(date))
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
        val viewModel = TasksViewModel(repository, FakeTimeProvider(date))
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
        val viewModel = TasksViewModel(FakeTaskRepository(listOf(task)), FakeTimeProvider(date))
        viewModel.onIntent(TasksUiIntent.SelectDate(other))
        assertEquals(other, viewModel.state.value.date)
        assertEquals("Later", viewModel.state.value.tasks.single().title)
        assertEquals(false, viewModel.state.value.showDatePicker)
    }

    @Test
    fun update_savesUntimedWhenTitleNonEmpty() {
        val viewModel = TasksViewModel(FakeTaskRepository(), FakeTimeProvider(date))
        viewModel.onIntent(TasksUiIntent.Create)
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Inbox"))
        viewModel.onIntent(TasksUiIntent.ClearTime)
        viewModel.onIntent(TasksUiIntent.Update)
        val saved = viewModel.state.value.tasks.single()
        assertTrue(saved.isUntimed)
        assertEquals("Inbox", saved.title)
    }

    @Test
    fun update_persistsTwoTags() {
        val viewModel = TasksViewModel(FakeTaskRepository(), FakeTimeProvider(date))
        viewModel.onIntent(TasksUiIntent.Create)
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Focus"))
        viewModel.onIntent(TasksUiIntent.ChangeTagDraft("code"))
        viewModel.onIntent(TasksUiIntent.AddTag)
        viewModel.onIntent(TasksUiIntent.ChangeTagDraft("deep"))
        viewModel.onIntent(TasksUiIntent.AddTag)
        viewModel.onIntent(TasksUiIntent.Update)
        assertEquals(listOf("code", "deep"), viewModel.state.value.tasks.single().tags)
    }

    @Test
    fun filterByTag_narrowsList() {
        val work = Task(
            id = TaskId("a"),
            title = "A",
            colorArgb = 0xFF3949AB,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = date,
            tags = listOf("code"),
        )
        val gym = Task(
            id = TaskId("b"),
            title = "B",
            colorArgb = 0xFFE8B4B8,
            date = date,
            tags = listOf("sport"),
        )
        val viewModel = TasksViewModel(FakeTaskRepository(listOf(work, gym)), FakeTimeProvider(date))
        viewModel.onIntent(TasksUiIntent.FilterByTag("sport"))
        assertEquals(listOf("B"), viewModel.state.value.visibleTasks.map { it.title })
    }
}

private class FakeTaskRepository(
    initial: List<Task> = emptyList(),
) : TaskRepository {
    private val items = MutableStateFlow(initial)

    override fun observeTasks(date: LocalDate): Flow<List<Task>> =
        items.map { list -> list.filter { it.date == date } }

    override suspend fun upsert(task: Task) {
        items.update { current -> current.filterNot { it.id == task.id } + task }
    }

    override suspend fun delete(taskId: TaskId) {
        items.update { current -> current.filterNot { it.id == taskId } }
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
