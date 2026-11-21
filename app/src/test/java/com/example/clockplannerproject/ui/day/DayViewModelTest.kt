package com.example.clockplannerproject.ui.day

import com.example.clockplannerproject.data.InMemoryAppearanceRepository
import com.example.clockplannerproject.data.InMemoryFlowMemoryStore
import com.example.clockplannerproject.data.InMemoryPracticeProgressStore
import com.example.clockplannerproject.data.SampleDaySeeder
import com.example.clockplannerproject.data.SampleSeedFlagStore
import com.example.clockplannerproject.kit.core.Appearance
import com.example.clockplannerproject.kit.core.AppearancePalette
import com.example.clockplannerproject.data.InMemoryGoalBookStore
import com.example.clockplannerproject.data.InMemoryReportRepository
import com.example.clockplannerproject.kit.core.GoalBook
import com.example.clockplannerproject.kit.core.BreakSession
import com.example.clockplannerproject.kit.core.DemandMark
import com.example.clockplannerproject.kit.core.TaskCompletion
import com.example.clockplannerproject.kit.core.TaskCompletionCodec
import com.example.clockplannerproject.kit.core.TaskReport
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
import com.example.clockplannerproject.kit.core.config.BackgroundConfig
import com.example.clockplannerproject.kit.core.config.BackgroundPresets
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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
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
    fun setLook_persistsTheSameAppearanceSettingsReads() = runTest {
        val appearance = InMemoryAppearanceRepository(
            Appearance.Default.copy(darkTheme = true),
        )
        val viewModel = dayViewModel(listOf(work), appearance = appearance)
        val next = Appearance(
            viewMode = ViewMode.LIST,
            accentArgb = AppearancePalette.ACCENT_EMERALD,
            completedArgb = AppearancePalette.COMPLETED_COOL,
            background = BackgroundConfig.Preset(BackgroundPresets.PARCHMENT),
        )
        viewModel.onIntent(DayUiIntent.SetLook(next))
        assertEquals(ViewMode.LIST, viewModel.state.value.viewMode)
        assertEquals(AppearancePalette.ACCENT_EMERALD, viewModel.state.value.accentArgb)
        assertEquals(BackgroundPresets.PARCHMENT, (viewModel.state.value.background as BackgroundConfig.Preset).name)
        val stored = appearance.observeAppearance().first()
        assertEquals(ViewMode.LIST, stored.viewMode)
        assertEquals(AppearancePalette.ACCENT_EMERALD, stored.accentArgb)
        assertEquals(AppearancePalette.COMPLETED_COOL, stored.completedArgb)
        assertEquals(BackgroundPresets.PARCHMENT, (stored.background as BackgroundConfig.Preset).name)
        assertTrue(stored.darkTheme)
        viewModel.onIntent(DayUiIntent.ToggleHideCompleted)
        assertTrue(viewModel.state.value.hideCompleted)
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
        assertEquals(false, viewModel.state.value.detailsOpen)
    }

    @Test
    fun pause_tapSelectedWork_clearsSelection() {
        val viewModel = dayViewModel(listOf(work), LocalTime(12, 0))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.StartTimer)
        val started = viewModel.state.value.tasks.single { it.id == work.id }
        val later = dayViewModel(listOf(started), LocalTime(13, 0))
        later.onIntent(DayUiIntent.SelectTask(started))
        later.onIntent(DayUiIntent.PauseTimer)
        val pausedWork = later.state.value.tasks.single { it.id == work.id }
        assertEquals(pausedWork.id, later.state.value.selectedTask?.id)
        later.onIntent(DayUiIntent.SelectTask(pausedWork))
        assertNull(later.state.value.selectedTask)
        assertNotNull(BreakSession.openBreak(later.state.value.tasks))
    }

    @Test
    fun openTaskDetails_secondTapClears() {
        val viewModel = dayViewModel(listOf(work))
        viewModel.onIntent(DayUiIntent.OpenTaskDetails(work))
        assertEquals(work.id, viewModel.state.value.sheetTask?.id)
        viewModel.onIntent(DayUiIntent.OpenTaskDetails(work))
        assertNull(viewModel.state.value.selectedTask)
        assertNull(viewModel.state.value.sheetTask)
    }

    @Test
    fun timedStart_closesOnEndMinuteOnce() = runTest {
        val clock = FakeTimeProvider(date, LocalTime(9, 0))
        val viewModel = dayViewModel(listOf(work), sharedClock = clock)
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.StartFor(15))
        val open = viewModel.state.value.tasks.single()
        assertEquals(2, open.blocks.size)
        assertNull(open.blocks.last().endMinute)
        assertEquals(15, viewModel.state.value.sessionMinutesLeft)
        val effects = mutableListOf<DayUiEffect>()
        val job = launch(dispatcher) { viewModel.effect.collect { effects.add(it) } }
        clock.set(date, LocalTime(9, 15))
        val closed = viewModel.state.value.tasks.single()
        assertEquals(TimeBlock(9 * 60, 12 * 60), closed.blocks.first())
        assertEquals(9 * 60, closed.blocks.last().startMinute)
        assertEquals(9 * 60 + 15, closed.blocks.last().endMinute)
        assertEquals(2, closed.blocks.size)
        clock.set(date, LocalTime(9, 20))
        assertEquals(closed.blocks, viewModel.state.value.tasks.single().blocks)
        assertEquals(1, effects.count { it == DayUiEffect.PlayChime })
        job.cancel()
    }

    @Test
    fun playWithoutSelection_createsOneOpenInterval_secondPlayDoesNotAppend() {
        val viewModel = dayViewModel(emptyList(), LocalTime(9, 30))
        val before = viewModel.state.value.tasks
        viewModel.onIntent(DayUiIntent.StartTimer)
        val tasks = viewModel.state.value.tasks
        assertEquals(before.size + 1, tasks.size)
        val created = tasks.single { it.title.isEmpty() }
        assertEquals(1, created.blocks.size)
        assertEquals(9 * 60 + 30, created.blocks.single().startMinute)
        assertNull(created.blocks.single().endMinute)
        assertEquals(created.id, viewModel.state.value.selectedTask?.id)
        before.forEach { previous ->
            assertEquals(previous.blocks, tasks.single { it.id == previous.id }.blocks)
        }
        viewModel.onIntent(DayUiIntent.StartTimer)
        val again = viewModel.state.value.tasks
        assertEquals(tasks.size, again.size)
        assertEquals(1, again.single { it.id == created.id }.blocks.size)

        val open = work.copy(
            blocks = listOf(TimeBlock(8 * 60, null)),
            status = TaskStatus.IN_PROGRESS,
        )
        val running = dayViewModel(listOf(open), LocalTime(9, 30))
        running.onIntent(DayUiIntent.StartTimer)
        assertEquals(1, running.state.value.tasks.size)
        assertEquals(listOf(TimeBlock(8 * 60, null)), running.state.value.tasks.single().blocks)
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
        val paused = later.state.value.tasks.first { it.id == work.id }
        assertEquals(TimeBlock(9 * 60, 12 * 60), paused.blocks.first())
        assertEquals(12 * 60, paused.blocks.last().startMinute)
        assertEquals(13 * 60, paused.blocks.last().endMinute)
        assertTrue(BreakSession.isBreak(later.state.value.tasks.first { it.id != work.id }))
        assertFalse(later.state.value.pauseSlipOpen)
    }

    @Test
    fun pause_startsBreak_andLeavesClosedBlocks() {
        val viewModel = dayViewModel(listOf(work), LocalTime(12, 0))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.StartTimer)
        val started = viewModel.state.value.tasks.single { it.id == work.id }
        val later = dayViewModel(listOf(started), LocalTime(13, 0))
        later.onIntent(DayUiIntent.SelectTask(started))
        later.onIntent(DayUiIntent.PauseTimer)
        assertFalse(later.state.value.pauseSlipOpen)
        val paused = later.state.value.tasks.single { it.id == work.id }
        assertEquals(TimeBlock(9 * 60, 12 * 60), paused.blocks.first())
        assertEquals(12 * 60, paused.blocks.last().startMinute)
        assertEquals(13 * 60, paused.blocks.last().endMinute)
        val breakTask = later.state.value.tasks.single { BreakSession.isBreak(it) }
        assertEquals(13 * 60, breakTask.blocks.single().startMinute)
        assertNull(breakTask.blocks.single().endMinute)
        assertEquals(work.id, later.state.value.selectedTask?.id)
        later.onIntent(DayUiIntent.FinishBreak)
        assertFalse(later.state.value.pauseSlipOpen)
        assertNull(BreakSession.openBreak(later.state.value.tasks))
        val afterFinish = later.state.value.tasks.single { it.id == work.id }
        assertEquals(paused.blocks, afterFinish.blocks)
    }

    @Test
    fun pause_whileBreakOpen_keepsWorkSelected() {
        val viewModel = dayViewModel(listOf(work), LocalTime(12, 0))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.StartTimer)
        val started = viewModel.state.value.tasks.single { it.id == work.id }
        val later = dayViewModel(listOf(started), LocalTime(13, 0))
        later.onIntent(DayUiIntent.SelectTask(started))
        later.onIntent(DayUiIntent.PauseTimer)
        val breakTask = later.state.value.tasks.single { BreakSession.isBreak(it) }
        val pausedWork = later.state.value.tasks.single { it.id == work.id }
        assertEquals(pausedWork.id, later.state.value.selectedTask?.id)
        later.onIntent(DayUiIntent.PauseTimer)
        assertEquals(pausedWork.id, later.state.value.selectedTask?.id)
        assertEquals(pausedWork.blocks, later.state.value.tasks.single { it.id == work.id }.blocks)
        assertEquals(breakTask.blocks, later.state.value.tasks.single { BreakSession.isBreak(it) }.blocks)
        assertNotNull(BreakSession.openBreak(later.state.value.tasks))
    }

    @Test
    fun continue_appendsOneWorkInterval_playDoesNotDouble() {
        val viewModel = dayViewModel(listOf(work), LocalTime(12, 0))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.StartTimer)
        val started = viewModel.state.value.tasks.single { it.id == work.id }
        val later = dayViewModel(listOf(started), LocalTime(13, 0))
        later.onIntent(DayUiIntent.SelectTask(started))
        later.onIntent(DayUiIntent.PauseTimer)
        val closed = later.state.value.tasks.single { it.id == work.id }.blocks
        later.onIntent(DayUiIntent.ContinueBreak)
        val continued = later.state.value.tasks.single { it.id == work.id }
        assertEquals(closed, continued.blocks.dropLast(1))
        assertNull(continued.blocks.last().endMinute)
        assertEquals(13 * 60, continued.blocks.last().startMinute)
        assertNull(BreakSession.openBreak(later.state.value.tasks))
        later.onIntent(DayUiIntent.StartTimer)
        assertEquals(continued.blocks, later.state.value.tasks.single { it.id == work.id }.blocks)
    }

    @Test
    fun completion_emptySave_storesDefaults_andReloadEdits() {
        val reports = InMemoryReportRepository()
        val viewModel = dayViewModel(listOf(work), LocalTime(12, 0), reports = reports)
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.StartTimer)
        val started = viewModel.state.value.tasks.single { it.id == work.id }
        val later = dayViewModel(listOf(started), LocalTime(13, 0), reports = reports)
        later.onIntent(DayUiIntent.SelectTask(started))
        later.onIntent(DayUiIntent.PauseTimer)
        later.onIntent(DayUiIntent.FinishBreak)
        assertTrue(later.state.value.completion.open)
        later.onIntent(DayUiIntent.SubmitCompletion)
        assertFalse(later.state.value.completion.open)
        val stored = TaskCompletionCodec.decode(reports.snapshot().single().text)
        assertEquals(TaskCompletion.DEFAULT_PROXY, stored?.stateAfter)
        assertEquals(TaskCompletion.DEFAULT_PROXY, stored?.engagement)
        assertEquals(TaskCompletion.DEFAULT_PROXY, stored?.skill)
        assertEquals(DemandMark.CHALLENGE, stored?.mark)

        later.onIntent(DayUiIntent.SelectTask(later.state.value.tasks.single { it.id == work.id }))
        later.onIntent(DayUiIntent.OpenCompletion)
        assertTrue(later.state.value.completion.open)
        assertEquals(TaskCompletion.DEFAULT_PROXY, later.state.value.completion.stateAfter)
        later.onIntent(
            DayUiIntent.EditCompletion(
                later.state.value.completion.copy(stateAfter = 8, engagement = 3),
            ),
        )
        later.onIntent(DayUiIntent.SubmitCompletion)
        val edited = TaskCompletionCodec.decode(reports.snapshot().single().text)
        assertEquals(8, edited?.stateAfter)
        assertEquals(3, edited?.engagement)
        assertEquals(1, reports.snapshot().size)
    }

    @Test
    fun completion_scoresEachInterval_andLeavesTaskOpen() {
        val reports = InMemoryReportRepository()
        val viewModel = dayViewModel(listOf(work), LocalTime(9, 0), reports = reports)
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.StartTimer)
        val started = viewModel.state.value.tasks.single { it.id == work.id }
        val paused = dayViewModel(listOf(started), LocalTime(10, 0), reports = reports)
        paused.onIntent(DayUiIntent.SelectTask(started))
        paused.onIntent(DayUiIntent.PauseTimer)
        paused.onIntent(DayUiIntent.ContinueBreak)
        val continued = paused.state.value.tasks.single { it.id == work.id }
        val later = dayViewModel(listOf(continued), LocalTime(11, 0), reports = reports)
        later.onIntent(DayUiIntent.SelectTask(continued))
        later.onIntent(DayUiIntent.PauseTimer)
        later.onIntent(DayUiIntent.FinishBreak)
        val secondId = later.state.value.completion.blockId
        later.onIntent(
            DayUiIntent.EditCompletion(
                later.state.value.completion.copy(
                    stateAfter = 9,
                    engagement = 9,
                    skill = 9,
                    mark = DemandMark.CHALLENGE,
                    done = true,
                ),
            ),
        )
        later.onIntent(DayUiIntent.SubmitCompletion)
        assertEquals(TaskStatus.TODO, later.state.value.tasks.single { it.id == work.id }.status)
        later.onIntent(DayUiIntent.SelectTask(later.state.value.tasks.single { it.id == work.id }))
        later.onIntent(DayUiIntent.OpenIntervalCompletion(0))
        val firstId = later.state.value.completion.blockId
        assertNotEquals(secondId, firstId)
        later.onIntent(
            DayUiIntent.EditCompletion(
                later.state.value.completion.copy(
                    stateAfter = 2,
                    engagement = 2,
                    skill = 2,
                    mark = DemandMark.HINDRANCE,
                ),
            ),
        )
        later.onIntent(DayUiIntent.SubmitCompletion)
        assertEquals(2, reports.snapshot().size)
        val scores = reports.snapshot().associate { report ->
            report.id.value to TaskCompletionCodec.decode(report.text)?.stateAfter
        }
        assertEquals(9, scores[TaskCompletionCodec.reportId(work.id, secondId!!).value])
        assertEquals(2, scores[TaskCompletionCodec.reportId(work.id, firstId!!).value])
        assertEquals(TaskStatus.TODO, later.state.value.tasks.single { it.id == work.id }.status)
    }

    @Test
    fun completion_save_clearsSelection() {
        val reports = InMemoryReportRepository()
        val viewModel = dayViewModel(listOf(work), LocalTime(12, 0), reports = reports)
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.StartTimer)
        val started = viewModel.state.value.tasks.single { it.id == work.id }
        val later = dayViewModel(listOf(started), LocalTime(13, 0), reports = reports)
        later.onIntent(DayUiIntent.SelectTask(started))
        later.onIntent(DayUiIntent.PauseTimer)
        later.onIntent(DayUiIntent.FinishBreak)
        later.onIntent(
            DayUiIntent.EditCompletion(
                later.state.value.completion.copy(
                    engagement = 5,
                    skill = 6,
                    stateAfter = 7,
                    mark = DemandMark.CHALLENGE,
                    done = true,
                ),
            ),
        )
        later.onIntent(DayUiIntent.SubmitCompletion)
        assertFalse(later.state.value.completion.open)
        assertNull(later.state.value.selectedTask)
        assertFalse(later.state.value.detailsOpen)
    }

    @Test
    fun completion_namesTask_andAddsNextWithoutOpenInterval() {
        val reports = InMemoryReportRepository()
        val untitled = work.copy(title = "")
        val viewModel = dayViewModel(listOf(untitled), LocalTime(12, 0), reports = reports)
        viewModel.onIntent(DayUiIntent.SelectTask(untitled))
        viewModel.onIntent(DayUiIntent.StartTimer)
        val started = viewModel.state.value.tasks.single { it.id == untitled.id }
        val later = dayViewModel(listOf(started), LocalTime(13, 0), reports = reports)
        later.onIntent(DayUiIntent.SelectTask(started))
        later.onIntent(DayUiIntent.PauseTimer)
        val closed = later.state.value.tasks.single { it.id == untitled.id }.blocks
        later.onIntent(DayUiIntent.FinishBreak)
        assertTrue(later.state.value.completion.titleEditable)
        later.onIntent(
            DayUiIntent.EditCompletion(
                later.state.value.completion.copy(
                    title = "Черновик",
                    nextTitle = "Следующее",
                    engagement = 4,
                    skill = 7,
                    stateAfter = 6,
                    mark = DemandMark.HINDRANCE,
                    easier = "короче",
                    progress = "ещё абзац",
                ),
            ),
        )
        later.onIntent(DayUiIntent.SubmitCompletion)
        assertFalse(later.state.value.completion.open)
        assertNull(later.state.value.selectedTask)
        val saved = later.state.value.tasks.single { it.id == untitled.id }
        assertEquals("Черновик", saved.title)
        assertEquals(closed, saved.blocks)
        val next = later.state.value.tasks.single { it.title == "Следующее" }
        assertTrue(next.blocks.isEmpty())
        val stored = TaskCompletionCodec.decode(reports.snapshot().single().text)
        assertEquals(DemandMark.HINDRANCE, stored?.mark)
        assertEquals(6, stored?.stateAfter)
        assertFalse(stored?.done ?: true)
    }

    @Test
    fun completion_nextTaskInheritsIntermediate() {
        val untitled = work.copy(title = "Черновик")
        val goals = InMemoryGoalBookStore(
            GoalBook()
                .withMain("Книга")
                .addIntermediate("Глава", id = "g1")
                .link(untitled.id.value, "g1"),
        )
        val viewModel = dayViewModel(listOf(untitled), LocalTime(12, 0), goals = goals)
        viewModel.onIntent(DayUiIntent.SelectTask(untitled))
        viewModel.onIntent(DayUiIntent.StartTimer)
        val started = viewModel.state.value.tasks.single { it.id == untitled.id }
        val later = dayViewModel(listOf(started), LocalTime(13, 0), goals = goals)
        later.onIntent(DayUiIntent.SelectTask(started))
        later.onIntent(DayUiIntent.PauseTimer)
        later.onIntent(DayUiIntent.FinishBreak)
        later.onIntent(
            DayUiIntent.EditCompletion(
                later.state.value.completion.copy(
                    nextTitle = "Следующее",
                    engagement = 4,
                    skill = 7,
                    stateAfter = 6,
                    mark = DemandMark.CHALLENGE,
                ),
            ),
        )
        later.onIntent(DayUiIntent.SubmitCompletion)
        val next = later.state.value.tasks.single { it.title == "Следующее" }
        assertEquals("g1", goals.snapshot().taskLinks[next.id.value])
        assertEquals("g1", goals.snapshot().taskLinks[untitled.id.value])
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
        assertFalse(later.state.value.pauseSlipOpen)
        val paused = later.state.value.tasks.single { it.id == work.id }
        later.onIntent(DayUiIntent.ChangePauseDraft("цифры за четверг"))
        later.onIntent(DayUiIntent.SelectPauseMood(PauseMood.Deep))
        later.onIntent(DayUiIntent.CommitPause)
        assertFalse(later.state.value.pauseSlipOpen)
        assertEquals(paused.blocks, later.state.value.tasks.single { it.id == work.id }.blocks)
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
        assertEquals(today, openDay.state.value.localToday)
        assertEquals(DialHalf.PM, openDay.state.value.dialHalf)

        openClock.set(next, LocalTime(0, 0))

        assertEquals(next, openDay.state.value.date)
        assertEquals(next, openDay.state.value.localToday)
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
        assertEquals(today, held.state.value.localToday)
        val halfWhileAway = held.state.value.dialHalf
        heldClock.set(next, LocalTime(0, 0))
        assertEquals(yesterday, held.state.value.date)
        assertEquals(next, held.state.value.localToday)
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
        val workAfterSkip = reloaded.state.value.tasks.first { it.id == work.id }
        if (reloaded.state.value.selectedTask?.id != work.id) {
            reloaded.onIntent(DayUiIntent.SelectTask(workAfterSkip))
        }
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
    fun deleteWorkWhilePaused_removesBreak() {
        val viewModel = dayViewModel(listOf(work), LocalTime(12, 0))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.StartTimer)
        val started = viewModel.state.value.tasks.single { it.id == work.id }
        val later = dayViewModel(listOf(started), LocalTime(13, 0))
        later.onIntent(DayUiIntent.SelectTask(started))
        later.onIntent(DayUiIntent.PauseTimer)
        assertNotNull(BreakSession.openBreak(later.state.value.tasks))
        later.onIntent(DayUiIntent.RequestDelete)
        later.onIntent(DayUiIntent.ConfirmDelete)
        assertTrue(later.state.value.tasks.none { BreakSession.isBreak(it) })
        assertNull(BreakSession.openBreak(later.state.value.tasks))
        assertTrue(later.state.value.tasks.none { it.id == work.id })
        assertNull(later.state.value.selectedTask)
    }

    @Test
    fun deleteWorkWhilePlaying_leavesNoOpenInterval() {
        val viewModel = dayViewModel(listOf(work), LocalTime(12, 0))
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.StartTimer)
        viewModel.onIntent(DayUiIntent.RequestDelete)
        viewModel.onIntent(DayUiIntent.ConfirmDelete)
        assertTrue(viewModel.state.value.tasks.isEmpty())
        assertNull(BreakSession.openBreak(viewModel.state.value.tasks))
        assertNull(viewModel.state.value.selectedTask)
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
    fun leaveDelete_keepsTaskAndScores() {
        val reports = InMemoryReportRepository(listOf(score(work.id)))
        val viewModel = dayViewModel(listOf(work), reports = reports)
        viewModel.onIntent(DayUiIntent.SelectTask(work))
        viewModel.onIntent(DayUiIntent.RequestDelete)
        viewModel.onIntent(DayUiIntent.DismissDelete)
        assertEquals(work.blocks, viewModel.state.value.tasks.single().blocks)
        assertEquals(1, reports.snapshot().size)
        assertNull(viewModel.state.value.pendingDelete)
    }

    @Test
    fun confirmDelete_removesTaskIntervalsAndScores() {
        val scored = work.copy(
            blocks = listOf(TimeBlock(9 * 60, 10 * 60), TimeBlock(11 * 60, 12 * 60)),
        )
        val neighbor = work.copy(
            id = TaskId("neighbor"),
            title = "Neighbor",
            blocks = listOf(TimeBlock(14 * 60, 15 * 60)),
        )
        val reports = InMemoryReportRepository(listOf(score(scored.id)))
        val viewModel = dayViewModel(listOf(scored, neighbor), reports = reports)
        viewModel.onIntent(DayUiIntent.SelectTask(scored))
        viewModel.onIntent(DayUiIntent.RequestDelete)
        viewModel.onIntent(DayUiIntent.ConfirmDelete)
        assertEquals(listOf(neighbor.id), viewModel.state.value.tasks.map { it.id })
        assertEquals(neighbor.blocks, viewModel.state.value.tasks.single().blocks)
        assertTrue(reports.snapshot().isEmpty())
    }

    @Test
    fun renameTemplate_updatesSubtaskTitleOnDay_keepsInterval() = runTest {
        val template = Task(
            id = TaskId("daily"),
            title = "Standup",
            colorArgb = 0xFF6750A4,
            blocks = listOf(TimeBlock(9 * 60, 9 * 60 + 15)),
            date = LocalDate(2026, 10, 5),
            recurrence = RecurrenceRule.Daily,
            seriesId = "daily",
        )
        val interval = TimeBlock(14 * 60, 15 * 60)
        val subtask = Task(
            id = TaskId("tue"),
            title = "Old",
            colorArgb = 1,
            blocks = listOf(interval),
            date = date,
            seriesId = "daily",
        )
        val repository = FakeTaskRepository(listOf(template, subtask))
        val viewModel = dayViewModel(emptyList(), repository = repository)
        val shown = viewModel.state.value.tasks.single { it.id == subtask.id }
        assertEquals("Standup", shown.title)
        assertEquals(listOf(interval), shown.blocks)
        repository.upsert(template.copy(title = "Renamed"))
        val renamed = viewModel.state.value.tasks.single { it.id == subtask.id }
        assertEquals("Renamed", renamed.title)
        assertEquals(listOf(interval), renamed.blocks)
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
        appearance: InMemoryAppearanceRepository = InMemoryAppearanceRepository(),
        sharedClock: FakeTimeProvider? = null,
        reports: InMemoryReportRepository = InMemoryReportRepository(),
        goals: InMemoryGoalBookStore = InMemoryGoalBookStore(),
    ): DayViewModel {
        val time = sharedClock ?: FakeTimeProvider(date, clock)
        val tasks = repository ?: FakeTaskRepository(initial)
        return DayViewModel(
            taskRepository = tasks,
            timeProvider = time,
            sampleDaySeeder = SampleDaySeeder(tasks, time, MemorySampleSeedFlag()),
            appearanceRepository = appearance,
            flowMemory = flowMemory,
            practice = practice,
            reports = reports,
            goals = goals,
        )
    }

    private fun score(taskId: TaskId): TaskReport {
        val completion = TaskCompletion(
            stateAfter = 5,
            engagement = 5,
            skill = 5,
            easier = "",
            mark = DemandMark.CHALLENGE,
            thoughts = "",
            reframe = "",
            done = true,
            progress = "",
        )
        return TaskReport(
            id = TaskCompletionCodec.reportId(taskId),
            taskId = taskId,
            createdAtEpochMillis = 0L,
            text = TaskCompletionCodec.encode(completion),
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
