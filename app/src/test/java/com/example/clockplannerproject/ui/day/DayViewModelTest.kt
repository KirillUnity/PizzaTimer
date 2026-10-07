package com.example.clockplannerproject.ui.day

import com.example.clockplannerproject.data.SampleDaySeeder
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.kit.core.ViewMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
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
class DayViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val date = LocalDate(2026, 10, 6)
    private val work = Task(
        id = TaskId("work"),
        title = "Deep work",
        colorArgb = 0xFF2E7D32,
        blocks = listOf(TimeBlock(9 * 60, 12 * 60)),
        date = date,
    )

    @Before
    fun setMain() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun reset() {
        Dispatchers.resetMain()
    }

    @Test
    fun toggleComplete_marksDoneAndKeepsSelection() {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.ToggleComplete)

        val updated = viewModel.state.value.tasks.single()
        assertEquals(TaskStatus.DONE, updated.status)
        assertEquals(TaskStatus.DONE, viewModel.state.value.selectedTask?.status)
    }

    @Test
    fun toggleComplete_reopensDoneTask() {
        val done = work.copy(status = TaskStatus.DONE)
        val viewModel = dayViewModel(listOf(done))
        viewModel.onIntent(DayUiIntent.SelectTask(done))
        viewModel.onIntent(DayUiIntent.ToggleComplete)
        assertEquals(TaskStatus.TODO, viewModel.state.value.tasks.single().status)
    }

    @Test
    fun rotateBy_addsOffset_resetClearsIt() {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.RotateBy(90f))
        assertEquals(90f, viewModel.state.value.userRotationOffsetDeg, 0.01f)
        viewModel.onIntent(DayUiIntent.ResetRotation)
        assertEquals(0f, viewModel.state.value.userRotationOffsetDeg, 0.01f)
    }

    @Test
    fun resizeTask_updatesEndMinuteInRepository() {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.ResizeTask(work.id, 13 * 60))
        assertEquals(13 * 60, viewModel.state.value.tasks.single().endMinute)
        assertNull(viewModel.state.value.resizingTaskId)
    }

    @Test
    fun toggleHideCompleted_survivesTaskEmission() {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.ToggleHideCompleted)
        assertEquals(true, viewModel.state.value.hideCompleted)
        viewModel.onIntent(DayUiIntent.ResizeTask(work.id, 13 * 60))
        assertEquals(true, viewModel.state.value.hideCompleted)
        viewModel.onIntent(DayUiIntent.ToggleHideCompleted)
        assertEquals(false, viewModel.state.value.hideCompleted)
    }

    @Test
    fun rotateBy_wrapsPast360() {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.RotateBy(350f))
        viewModel.onIntent(DayUiIntent.RotateBy(20f))
        assertEquals(10f, viewModel.state.value.userRotationOffsetDeg, 0.01f)
    }

    @Test
    fun resizePreview_thenCancel_restoresCivilEnd() {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.ResizePreview(work.id, 13 * 60))
        assertEquals(13 * 60, viewModel.state.value.dialTasks.single().endMinute)
        assertEquals(13 * 60, viewModel.state.value.sheetTask?.endMinute)
        viewModel.onIntent(DayUiIntent.CancelResize)
        assertEquals(12 * 60, viewModel.state.value.dialTasks.single().endMinute)
        assertNull(viewModel.state.value.resizingTaskId)
    }

    @Test
    fun dismissTask_clearsSheet() {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.DismissTask)
        assertNull(viewModel.state.value.selectedTask)
        assertNull(viewModel.state.value.sheetTask)
    }

    @Test
    fun editTask_opensEditorAndClearsSheet() = runTest {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        val effects = mutableListOf<DayUiEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }
        viewModel.onIntent(DayUiIntent.EditTask)
        job.cancel()
        assertNull(viewModel.state.value.selectedTask)
        assertTrue(effects.any { it is DayUiEffect.OpenEditor && it.task.id == work.id })
    }

    @Test
    fun setViewMode_survivesTaskEmission() {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.SetViewMode(ViewMode.PIZZA))
        assertEquals(ViewMode.PIZZA, viewModel.state.value.viewMode)
        viewModel.onIntent(DayUiIntent.ResizeTask(work.id, 13 * 60))
        assertEquals(ViewMode.PIZZA, viewModel.state.value.viewMode)
    }

    @Test
    fun confirmDelete_removesTaskAndSheet() {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.RequestDelete)
        viewModel.onIntent(DayUiIntent.ConfirmDelete)
        assertEquals(true, viewModel.state.value.tasks.isEmpty())
        assertNull(viewModel.state.value.selectedTask)
        assertNull(viewModel.state.value.pendingDelete)
    }

    private fun dayViewModel(initial: List<Task>): DayViewModel {
        val time = FakeTimeProvider(date)
        val repository = FakeTaskRepository(initial)
        return DayViewModel(
            taskRepository = repository,
            timeProvider = time,
            sampleDaySeeder = SampleDaySeeder(repository, time),
        )
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
    private val time: LocalTime = LocalTime(9, 30),
) : TimeProvider {
    private val ticks = MutableStateFlow(time)
    override fun now(): LocalTime = ticks.value
    override fun today(): LocalDate = date
    override fun observeTime(): Flow<LocalTime> = ticks
}
