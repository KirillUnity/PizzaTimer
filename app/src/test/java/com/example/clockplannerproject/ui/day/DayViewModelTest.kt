package com.example.clockplannerproject.ui.day

import com.example.clockplannerproject.data.InMemoryFlowMemoryStore
import com.example.clockplannerproject.data.InMemoryPracticeProgressStore
import com.example.clockplannerproject.data.SampleDaySeeder
import com.example.clockplannerproject.data.SampleSeedFlagStore
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.FlowMemoryStore
import com.example.clockplannerproject.kit.core.RecurrenceRule
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.practice.PracticeProgressStore
import com.example.clockplannerproject.kit.core.practice.PracticeSkill
import com.example.clockplannerproject.kit.core.PauseMood
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
import org.junit.Assert.assertFalse
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
        assertEquals(13 * 60, viewModel.state.value.overlayTask?.endMinute)
        assertNull(viewModel.state.value.sheetTask)
        viewModel.onIntent(DayUiIntent.CancelResize)
        assertEquals(12 * 60, viewModel.state.value.dialTasks.single().endMinute)
        assertNull(viewModel.state.value.resizingTaskId)
    }

    @Test
    fun selectTask_doesNotOpenSheet() {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        assertEquals(work.id, viewModel.state.value.selectedTask?.id)
        assertEquals(work.id, viewModel.state.value.overlayTask?.id)
        assertNull(viewModel.state.value.sheetTask)
        assertEquals(false, viewModel.state.value.detailsOpen)
    }

    @Test
    fun openTaskDetails_showsSheet_dismissDetailsKeepsSelection() {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.OpenTaskDetails())
        assertEquals(work.id, viewModel.state.value.sheetTask?.id)
        viewModel.onIntent(DayUiIntent.DismissDetails)
        assertEquals(work.id, viewModel.state.value.selectedTask?.id)
        assertNull(viewModel.state.value.sheetTask)
    }

    @Test
    fun openTaskDetails_fromList_selectsAndOpensSheet() {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.OpenTaskDetails(work))
        assertEquals(work.id, viewModel.state.value.selectedTask?.id)
        assertEquals(work.id, viewModel.state.value.sheetTask?.id)
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
    fun selectTask_secondTapDismisses() {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        assertNull(viewModel.state.value.selectedTask)
    }

    @Test
    fun startThenPause_appendsSecondBlock() {
        val viewModel = dayViewModel(listOf(work), LocalTime(12, 0))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.StartTimer)
        val started = viewModel.state.value.tasks.single()
        assertEquals(2, started.blocks.size)
        assertEquals(null, started.blocks.last().endMinute)
        assertEquals(TimeBlock(9 * 60, 12 * 60), started.blocks.first())
        val later = dayViewModel(listOf(started), LocalTime(13, 0))
        later.onIntent(DayUiIntent.SelectTask(started))
        later.onIntent(DayUiIntent.PauseTimer)
        val paused = later.state.value.tasks.single()
        assertEquals(TimeBlock(9 * 60, 12 * 60), paused.blocks.first())
        assertEquals(TimeBlock(12 * 60, 13 * 60), paused.blocks.last())
    }

    @Test
    fun pause_opensSlip_commitKeepsClosedBlocks() {
        val viewModel = dayViewModel(listOf(work), LocalTime(12, 0))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.StartTimer)
        val started = viewModel.state.value.tasks.single()
        val later = dayViewModel(listOf(started), LocalTime(13, 0))
        later.onIntent(DayUiIntent.SelectTask(started))
        later.onIntent(DayUiIntent.PauseTimer)
        assertTrue(later.state.value.pauseSlipOpen)
        val paused = later.state.value.tasks.single()
        later.onIntent(DayUiIntent.ChangePauseDraft("цифры за четверг"))
        later.onIntent(DayUiIntent.SelectPauseMood(PauseMood.Deep))
        later.onIntent(DayUiIntent.CommitPause)
        assertFalse(later.state.value.pauseSlipOpen)
        assertEquals("цифры за четверг", later.state.value.resumeCheckpoint)
        assertEquals(paused.blocks, later.state.value.tasks.single().blocks)
    }

    @Test
    fun resizePreview_closedFirstBlock_doesNotLockLaterBlock() {
        val sport = Task(
            id = TaskId("sport"),
            title = "Sport",
            colorArgb = 0xFFE8B4B8,
            blocks = listOf(TimeBlock(8 * 60, 9 * 60), TimeBlock(10 * 60, 11 * 60)),
            date = date,
        )
        val viewModel = dayViewModel(listOf(sport), LocalTime(9, 30))
        viewModel.onIntent(DayUiIntent.ResizePreview(sport.id, 12 * 60, 8 * 60))
        assertNull(viewModel.state.value.resizingTaskId)
        viewModel.onIntent(DayUiIntent.ResizePreview(sport.id, 12 * 60, 10 * 60))
        assertEquals(sport.id, viewModel.state.value.resizingTaskId)
        assertEquals(12 * 60, viewModel.state.value.overlayTask?.blocks?.last()?.endMinute)
        assertEquals(TimeBlock(8 * 60, 9 * 60), viewModel.state.value.overlayTask?.blocks?.first())
    }

    @Test
    fun midnight_followsOpenToday() {
        val today = date
        val yesterday = LocalDate(2026, 10, 5)
        val next = LocalDate(2026, 10, 7)
        val dayAfter = LocalDate(2026, 10, 8)
        val openBlock = TimeBlock(23 * 60, null)
        val open = work.copy(
            blocks = listOf(openBlock),
            status = TaskStatus.IN_PROGRESS,
        )
        val daily = Task(
            id = TaskId("daily"),
            title = "Daily",
            colorArgb = 0xFF607D8B,
            date = today,
            recurrence = RecurrenceRule.Daily,
        )
        val openRepo = FakeTaskRepository(listOf(open, daily))
        val openClock = FakeTimeProvider(today, LocalTime(23, 59))
        val openDay = DayViewModel(
            taskRepository = openRepo,
            timeProvider = openClock,
            sampleDaySeeder = SampleDaySeeder(openRepo, openClock, MemorySampleSeedFlag()),
        )
        assertEquals(today, openDay.state.value.date)
        assertEquals(DialHalf.PM, openDay.state.value.dialHalf)

        openClock.set(next, LocalTime(0, 0))

        assertEquals(next, openDay.state.value.date)
        assertEquals(DialHalf.AM, openDay.state.value.dialHalf)
        val stored = openRepo.snapshot().single { it.id == open.id }
        assertEquals(today, stored.date)
        assertEquals(listOf(openBlock), stored.blocks)
        val materialized = openDay.state.value.tasks.single()
        assertEquals(next, materialized.date)
        assertEquals(daily.id.value, materialized.seriesId)
        assertTrue(openRepo.snapshot().none { it.date == dayAfter })

        val emptyRepo = FakeTaskRepository()
        val emptyClock = FakeTimeProvider(today, LocalTime(23, 59))
        val seeded = DayViewModel(
            taskRepository = emptyRepo,
            timeProvider = emptyClock,
            sampleDaySeeder = SampleDaySeeder(emptyRepo, emptyClock, MemorySampleSeedFlag()),
        )
        val datesBeforeAdvance = emptyRepo.snapshot().map { it.date }
        emptyClock.set(next, LocalTime(0, 0))
        assertEquals(next, seeded.state.value.date)
        assertEquals(datesBeforeAdvance, emptyRepo.snapshot().map { it.date })
        assertTrue(seeded.state.value.tasks.isEmpty())

        val heldRepo = FakeTaskRepository(listOf(work))
        val heldClock = FakeTimeProvider(today, LocalTime(23, 59))
        val held = DayViewModel(
            taskRepository = heldRepo,
            timeProvider = heldClock,
            sampleDaySeeder = SampleDaySeeder(heldRepo, heldClock, MemorySampleSeedFlag()),
        )
        held.onIntent(DayUiIntent.PreviousDay)
        assertEquals(yesterday, held.state.value.date)
        val halfWhileAway = held.state.value.dialHalf
        heldClock.set(next, LocalTime(0, 0))
        assertEquals(yesterday, held.state.value.date)
        assertEquals(halfWhileAway, held.state.value.dialHalf)

        held.onIntent(DayUiIntent.SelectDate(next))
        assertEquals(next, held.state.value.date)
        heldClock.set(dayAfter, LocalTime(0, 1))
        assertEquals(dayAfter, held.state.value.date)
        assertEquals(DialHalf.AM, held.state.value.dialHalf)
    }

    @Test
    fun commit_survivesNewViewModel_startClearsIt_skipLeavesOther() {
        val memory = InMemoryFlowMemoryStore()
        val other = work.copy(
            id = TaskId("other"),
            title = "Other",
            blocks = listOf(TimeBlock(14 * 60, 15 * 60)),
        )
        val repository = FakeTaskRepository(listOf(work, other))
        val viewModel = dayViewModel(
            initial = emptyList(),
            clock = LocalTime(12, 0),
            flowMemory = memory,
            repository = repository,
        )
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.StartTimer)
        val later = dayViewModel(
            initial = emptyList(),
            clock = LocalTime(13, 0),
            flowMemory = memory,
            repository = repository,
        )
        val started = later.state.value.tasks.first { it.id == work.id }
        later.onIntent(DayUiIntent.SelectTask(started))
        later.onIntent(DayUiIntent.PauseTimer)
        later.onIntent(DayUiIntent.ChangePauseDraft("  цифры за четверг  "))
        later.onIntent(DayUiIntent.SelectPauseMood(PauseMood.Deep))
        later.onIntent(DayUiIntent.CommitPause)
        assertEquals("цифры за четверг", later.state.value.resumeCheckpoint)
        assertEquals(PauseMood.Deep, later.state.value.resumeMood)
        val closed = later.state.value.tasks.first { it.id == work.id }.blocks

        val reloaded = dayViewModel(
            initial = emptyList(),
            clock = LocalTime(13, 30),
            flowMemory = memory,
            repository = repository,
        )
        reloaded.onIntent(DayUiIntent.SelectTask(reloaded.state.value.tasks.first { it.id == work.id }))
        assertEquals("цифры за четверг", reloaded.state.value.resumeCheckpoint)
        assertEquals(PauseMood.Deep, reloaded.state.value.resumeMood)

        reloaded.onIntent(DayUiIntent.ChangePauseDraft("   "))
        reloaded.onIntent(DayUiIntent.SelectPauseMood(PauseMood.Stuck))
        reloaded.onIntent(DayUiIntent.CommitPause)
        assertEquals("цифры за четверг", reloaded.state.value.resumeCheckpoint)
        assertEquals(PauseMood.Deep, reloaded.state.value.resumeMood)

        reloaded.onIntent(DayUiIntent.SelectTask(reloaded.state.value.tasks.first { it.id == other.id }))
        reloaded.onIntent(DayUiIntent.PauseTimer)
        reloaded.onIntent(DayUiIntent.ChangePauseDraft("чужое"))
        reloaded.onIntent(DayUiIntent.SkipPause)
        reloaded.onIntent(DayUiIntent.SelectTask(reloaded.state.value.tasks.first { it.id == work.id }))
        assertEquals("цифры за четверг", reloaded.state.value.resumeCheckpoint)
        assertEquals(PauseMood.Deep, reloaded.state.value.resumeMood)

        val beforeStart = reloaded.state.value.tasks.first { it.id == work.id }.blocks
        assertEquals(closed, beforeStart)
        reloaded.onIntent(DayUiIntent.StartTimer)
        assertNull(reloaded.state.value.resumeCheckpoint)
        assertNull(reloaded.state.value.resumeMood)
        val after = reloaded.state.value.tasks.first { it.id == work.id }
        assertEquals(beforeStart.first(), after.blocks.first())
        assertEquals(beforeStart.size + 1, after.blocks.size)
        assertNull(after.blocks.last().endMinute)
    }

    @Test
    fun dayClose_isVisibleOnThatDate_andHiddenOnTheNext() {
        val memory = InMemoryFlowMemoryStore()
        val repository = FakeTaskRepository(listOf(work))
        val viewModel = dayViewModel(
            initial = emptyList(),
            flowMemory = memory,
            repository = repository,
        )
        viewModel.onIntent(DayUiIntent.ShowDayClose)
        assertTrue(viewModel.state.value.showDayClose)

        val again = dayViewModel(
            initial = emptyList(),
            flowMemory = memory,
            repository = repository,
        )
        assertTrue(again.state.value.showDayClose)
        again.onIntent(DayUiIntent.NextDay)
        assertEquals(LocalDate(2026, 10, 7), again.state.value.date)
        assertFalse(again.state.value.showDayClose)
        again.onIntent(DayUiIntent.PreviousDay)
        assertTrue(again.state.value.showDayClose)
        again.onIntent(DayUiIntent.DismissDayClose)

        val hidden = dayViewModel(
            initial = emptyList(),
            flowMemory = memory,
            repository = repository,
        )
        assertFalse(hidden.state.value.showDayClose)
        hidden.onIntent(DayUiIntent.ShowDayClose)
        assertTrue(hidden.state.value.showDayClose)
        hidden.onIntent(DayUiIntent.SelectTask(hidden.state.value.tasks.single()))
        hidden.onIntent(DayUiIntent.StartTimer)
        assertTrue(hidden.state.value.showDayClose)
        assertEquals(2, hidden.state.value.tasks.single().blocks.size)
    }

    @Test
    fun changingDay_hidesSlip_andKeepsCheckpoint() {
        val memory = InMemoryFlowMemoryStore()
        val repository = FakeTaskRepository(listOf(work))
        val viewModel = dayViewModel(
            initial = emptyList(),
            clock = LocalTime(13, 0),
            flowMemory = memory,
            repository = repository,
        )
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.PauseTimer)
        viewModel.onIntent(DayUiIntent.ChangePauseDraft("цифры за четверг"))
        viewModel.onIntent(DayUiIntent.SelectPauseMood(PauseMood.Calm))
        viewModel.onIntent(DayUiIntent.CommitPause)
        viewModel.onIntent(DayUiIntent.ShowDayClose)
        assertTrue(viewModel.state.value.showDayClose)

        viewModel.onIntent(DayUiIntent.NextDay)
        assertFalse(viewModel.state.value.pauseSlipOpen)
        assertFalse(viewModel.state.value.showDayClose)
        assertNull(viewModel.state.value.resumeCheckpoint)

        viewModel.onIntent(DayUiIntent.PreviousDay)
        viewModel.onIntent(DayUiIntent.SelectTask(viewModel.state.value.tasks.first { it.id == work.id }))
        assertEquals("цифры за четверг", viewModel.state.value.resumeCheckpoint)
        assertEquals(PauseMood.Calm, viewModel.state.value.resumeMood)
        assertTrue(viewModel.state.value.showDayClose)
    }

    @Test
    fun previousDay_shiftsSelectedDate() {
        val viewModel = dayViewModel(listOf(work))
        assertEquals(date, viewModel.state.value.date)
        viewModel.onIntent(DayUiIntent.PreviousDay)
        assertEquals(LocalDate(2026, 10, 5), viewModel.state.value.date)
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

    @Test
    fun practice_dialSelectStartCommitAndDayClose_setFlags_pizzaAndBlankDoNot() = runTest {
        val practice = InMemoryPracticeProgressStore()
        val viewModel = dayViewModel(listOf(work), practice = practice)
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.StartTimer)
        viewModel.onIntent(DayUiIntent.ChangePauseDraft("   "))
        viewModel.onIntent(DayUiIntent.CommitPause)
        var tried = practice.observe().first()
        assertTrue(PracticeSkill.DIAL in tried)
        assertTrue(PracticeSkill.START in tried)
        assertFalse(PracticeSkill.CHECKPOINT in tried)

        viewModel.onIntent(DayUiIntent.ChangePauseDraft("next line"))
        viewModel.onIntent(DayUiIntent.CommitPause)
        viewModel.onIntent(DayUiIntent.ShowDayClose)
        tried = practice.observe().first()
        assertTrue(PracticeSkill.CHECKPOINT in tried)
        assertTrue(PracticeSkill.DAY_CLOSE in tried)
        assertFalse(PracticeSkill.LEFTOVER in tried)

        val pizza = InMemoryPracticeProgressStore()
        val pizzaDay = dayViewModel(listOf(work), practice = pizza)
        pizzaDay.onIntent(DayUiIntent.SetViewMode(ViewMode.PIZZA))
        pizzaDay.onIntent(DayUiIntent.SelectTask(work))
        assertTrue(pizza.observe().first().isEmpty())
    }

    private fun dayViewModel(
        initial: List<Task>,
        clock: LocalTime = LocalTime(9, 30),
        flowMemory: FlowMemoryStore = InMemoryFlowMemoryStore(),
        repository: FakeTaskRepository? = null,
        practice: PracticeProgressStore = InMemoryPracticeProgressStore(),
    ): DayViewModel {
        val time = FakeTimeProvider(date, clock)
        val tasks = repository ?: FakeTaskRepository(initial)
        return DayViewModel(
            taskRepository = tasks,
            timeProvider = time,
            sampleDaySeeder = SampleDaySeeder(tasks, time, MemorySampleSeedFlag()),
            flowMemory = flowMemory,
            practice = practice,
        )
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

    fun snapshot(): List<Task> = items.value
}

private class FakeTimeProvider(
    initialDate: LocalDate,
    time: LocalTime = LocalTime(9, 30),
) : TimeProvider {
    private var date: LocalDate = initialDate
    private val ticks = MutableStateFlow(time)

    fun set(date: LocalDate, time: LocalTime) {
        this.date = date
        ticks.value = time
    }

    override fun now(): LocalTime = ticks.value
    override fun today(): LocalDate = date
    override fun observeTime(): Flow<LocalTime> = ticks
}

private class MemorySampleSeedFlag : SampleSeedFlagStore {
    private var handled = false

    override suspend fun isHandled(): Boolean = handled

    override suspend fun markHandled() {
        handled = true
    }
}
