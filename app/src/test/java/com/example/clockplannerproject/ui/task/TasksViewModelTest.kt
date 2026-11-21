package com.example.clockplannerproject.ui.task

import com.example.clockplannerproject.data.InMemoryPracticeProgressStore
import com.example.clockplannerproject.kit.core.BreakSession
import com.example.clockplannerproject.data.MockPlannerSource
import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.RecurrenceRule
import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.ReportRepository
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskDraftError
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskReport
import com.example.clockplannerproject.kit.core.TaskPalette
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.kit.core.practice.PracticeSkill
import com.example.clockplannerproject.kit.core.planner.AcceptedPlan
import com.example.clockplannerproject.kit.core.planner.PlanOutcome
import com.example.clockplannerproject.kit.core.planner.PlanRequest
import com.example.clockplannerproject.kit.core.planner.PlanVerdict
import com.example.clockplannerproject.kit.core.planner.PlannerPlanStore
import com.example.clockplannerproject.kit.core.planner.PlannerSource
import com.example.clockplannerproject.kit.core.planner.ReportRequest
import com.example.clockplannerproject.kit.core.planner.RussianPlannerPhrases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import kotlinx.coroutines.test.runTest
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
    fun createTodayQuick_savesUntimedOnSelectedDay() {
        val viewModel = TasksViewModel(FakeTaskRepository(), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.CreateForSelectedDay)
        assertEquals(CreateStage.CHOOSER, viewModel.state.value.editor?.createStage)
        viewModel.onIntent(TasksUiIntent.CreateTodayQuick("Новое дело"))

        assertNull(viewModel.state.value.editor)
        val saved = viewModel.state.value.tasks.single()
        assertEquals("Новое дело", saved.title)
        assertEquals(date, saved.date)
        assertTrue(saved.isUntimed)
    }

    @Test
    fun createTodayQuick_usesTypedTitle() {
        val viewModel = TasksViewModel(FakeTaskRepository(), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.CreateForSelectedDay)
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Почта"))
        viewModel.onIntent(TasksUiIntent.CreateTodayQuick("Новое дело"))

        val saved = viewModel.state.value.tasks.single()
        assertEquals("Почта", saved.title)
        assertEquals(date, saved.date)
        assertTrue(saved.isUntimed)
        assertNull(viewModel.state.value.editor)
    }

    @Test
    fun createWizard_skipAll_savesDefaultName() {
        val viewModel = TasksViewModel(FakeTaskRepository(), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.CreateForSelectedDay)
        viewModel.onIntent(TasksUiIntent.StartCreateWizard)
        viewModel.onIntent(TasksUiIntent.CreateSkipStage)
        viewModel.onIntent(TasksUiIntent.CreateSkipStage)
        viewModel.onIntent(TasksUiIntent.CreateSkipStage)
        assertEquals(CreateStage.DETAILS, viewModel.state.value.editor?.createStage)
        viewModel.onIntent(TasksUiIntent.CreateFinishWizard("Новое дело"))

        assertNull(viewModel.state.value.editor)
        val saved = viewModel.state.value.tasks.single()
        assertEquals("Новое дело", saved.title)
        assertEquals(Importance.MEDIUM, saved.importance)
        assertTrue(saved.isUntimed)
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
    fun update_untimedSaveMarksLeftover_timedSaveDoesNot() = runTest {
        val untimed = InMemoryPracticeProgressStore()
        val untimedVm = TasksViewModel(
            FakeTaskRepository(),
            FakeTimeProvider(date),
            FakeReportRepository(),
            practice = untimed,
        )
        untimedVm.onIntent(TasksUiIntent.Create)
        untimedVm.onIntent(TasksUiIntent.ChangeTitle("Read"))
        untimedVm.onIntent(TasksUiIntent.Update)
        assertTrue(PracticeSkill.LEFTOVER in untimed.observe().first())

        val timed = InMemoryPracticeProgressStore()
        val timedVm = TasksViewModel(
            FakeTaskRepository(),
            FakeTimeProvider(date),
            FakeReportRepository(),
            practice = timed,
        )
        timedVm.onIntent(TasksUiIntent.Create)
        timedVm.onIntent(TasksUiIntent.ChangeTitle("Focus"))
        timedVm.onIntent(TasksUiIntent.ChangeScheduled(true))
        timedVm.onIntent(TasksUiIntent.AddBlock)
        timedVm.onIntent(TasksUiIntent.ChangeBlockStart(0, 9 * 60))
        timedVm.onIntent(TasksUiIntent.ChangeBlockEnd(0, 10 * 60))
        timedVm.onIntent(TasksUiIntent.Update)
        assertFalse(PracticeSkill.LEFTOVER in timed.observe().first())
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
    fun update_rejectsOverlappingPeriod() {
        val existing = Task(
            id = TaskId("kept"),
            title = "Focus",
            colorArgb = TaskPalette.defaultColor,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = date,
        )
        val viewModel = TasksViewModel(
            FakeTaskRepository(listOf(existing)),
            FakeTimeProvider(date),
            FakeReportRepository(),
        )
        viewModel.onIntent(TasksUiIntent.CreateForSelectedDay)
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Copy"))
        viewModel.onIntent(TasksUiIntent.AddBlock)
        viewModel.onIntent(TasksUiIntent.ChangeBlockStart(0, 9 * 60))
        viewModel.onIntent(TasksUiIntent.ChangeBlockEnd(0, 10 * 60))
        viewModel.onIntent(TasksUiIntent.Update)
        assertEquals(TaskDraftError.OverlappingPeriod, viewModel.state.value.editor?.error)
        assertEquals(1, viewModel.state.value.tasks.size)
    }

    @Test
    fun visibleTasks_hidesBreakWedges() {
        val work = Task(
            id = TaskId("kept"),
            title = "Focus",
            colorArgb = TaskPalette.defaultColor,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = date,
        )
        val wedge = BreakSession.start(work, 10 * 60)
        val viewModel = TasksViewModel(
            FakeTaskRepository(listOf(work, wedge)),
            FakeTimeProvider(date, LocalTime(10, 10)),
            FakeReportRepository(),
        )
        assertEquals(listOf("Focus"), viewModel.state.value.visibleTasks.map { it.title })
        assertTrue(viewModel.state.value.availableTags.none { it.equals("break", ignoreCase = true) })
    }

    @Test
    fun closedInterval_canBeEditedRemoved_andKeepsExpectedApartFromActual() {
        val task = Task(
            id = TaskId("closed"),
            title = "Focus",
            colorArgb = TaskPalette.defaultColor,
            blocks = listOf(TimeBlock(8 * 60, 9 * 60, id = "b1", plannedMinutes = 30)),
            date = date,
        )
        val viewModel = TasksViewModel(
            FakeTaskRepository(listOf(task)),
            FakeTimeProvider(date, LocalTime(12, 0)),
            FakeReportRepository(),
        )
        viewModel.onIntent(TasksUiIntent.Edit(task))
        viewModel.onIntent(TasksUiIntent.ChangeBlockStart(0, 8 * 60 + 15))
        val edited = viewModel.state.value.editor!!.blocks.single()
        assertEquals(8 * 60 + 15, edited.startMinute)
        assertEquals(30, edited.plannedMinutes)
        assertEquals(45, edited.actualMinutes(12 * 60))
        viewModel.onIntent(TasksUiIntent.ChangePlannedMinutes(0, 60))
        assertEquals(60, viewModel.state.value.editor!!.blocks.single().plannedMinutes)
        viewModel.onIntent(TasksUiIntent.RemoveBlock(0))
        assertTrue(viewModel.state.value.editor!!.blocks.isEmpty())
    }

    @Test
    fun removeBlock_dropsUnclosed_keepsClosed() {
        val viewModel = TasksViewModel(FakeTaskRepository(), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.Create)
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Focus"))
        viewModel.onIntent(TasksUiIntent.ChangeScheduled(true))
        viewModel.onIntent(TasksUiIntent.AddBlock)
        viewModel.onIntent(TasksUiIntent.ChangeBlockStart(0, 8 * 60))
        viewModel.onIntent(TasksUiIntent.ChangeBlockEnd(0, 9 * 60))
        viewModel.onIntent(TasksUiIntent.AddBlock)
        viewModel.onIntent(TasksUiIntent.ChangeBlockStart(1, 18 * 60))
        viewModel.onIntent(TasksUiIntent.ChangeBlockEnd(1, 19 * 60))
        viewModel.onIntent(TasksUiIntent.RemoveBlock(1))
        assertEquals(1, viewModel.state.value.editor?.blocks?.size)
        assertEquals(8 * 60, viewModel.state.value.editor?.blocks?.single()?.startMinute)
    }

    @Test
    fun edit_startsCollapsed_toggleOpensName() {
        val task = Task(
            id = TaskId("keep"),
            title = "Focus",
            colorArgb = 0xFF3949AB,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = date,
        )
        val viewModel = TasksViewModel(FakeTaskRepository(listOf(task)), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.Edit(task))
        assertTrue(viewModel.state.value.editor?.expandedEditSections.isNullOrEmpty())

        viewModel.onIntent(TasksUiIntent.ToggleEditSection(EditSection.NAME))
        assertEquals(setOf(EditSection.NAME), viewModel.state.value.editor?.expandedEditSections)

        viewModel.onIntent(TasksUiIntent.ToggleEditSection(EditSection.NAME))
        assertTrue(viewModel.state.value.editor?.expandedEditSections.isNullOrEmpty())
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
    fun confirmDeleteSelected_removesMarked_keepsOthers() {
        val keep = Task(
            id = TaskId("keep"),
            title = "Keep",
            colorArgb = TaskPalette.defaultColor,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = date,
        )
        val goneA = Task(
            id = TaskId("a"),
            title = "A",
            colorArgb = TaskPalette.defaultColor,
            blocks = listOf(TimeBlock(11 * 60, 12 * 60)),
            date = date,
        )
        val goneB = Task(
            id = TaskId("b"),
            title = "B",
            colorArgb = TaskPalette.defaultColor,
            blocks = listOf(TimeBlock(13 * 60, 14 * 60)),
            date = date,
        )
        val viewModel = TasksViewModel(
            FakeTaskRepository(listOf(keep, goneA, goneB)),
            FakeTimeProvider(date),
            FakeReportRepository(),
        )
        viewModel.onIntent(TasksUiIntent.ToggleTaskSelection(goneA.id))
        viewModel.onIntent(TasksUiIntent.ToggleTaskSelection(goneB.id))
        viewModel.onIntent(TasksUiIntent.RequestDeleteSelected)
        viewModel.onIntent(TasksUiIntent.DismissDelete)
        assertEquals(3, viewModel.state.value.tasks.size)
        assertEquals(setOf(goneA.id, goneB.id), viewModel.state.value.selectedTaskIds)

        viewModel.onIntent(TasksUiIntent.RequestDeleteSelected)
        viewModel.onIntent(TasksUiIntent.ConfirmDelete)
        assertEquals(listOf("Keep"), viewModel.state.value.tasks.map { it.title })
        assertTrue(viewModel.state.value.selectedTaskIds.isEmpty())
        assertFalse(viewModel.state.value.pendingBulkDelete)
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
    fun update_persistsCustomTags_andFilterNarrows() {
        val viewModel = TasksViewModel(FakeTaskRepository(), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.Create)
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Read"))
        viewModel.onIntent(TasksUiIntent.AddTag("  дома  "))
        viewModel.onIntent(TasksUiIntent.ChangeTagDraft("учёба"))
        viewModel.onIntent(TasksUiIntent.AddTag("учёба"))
        viewModel.onIntent(TasksUiIntent.AddTag("дома"))
        viewModel.onIntent(TasksUiIntent.AddTag("   "))
        viewModel.onIntent(TasksUiIntent.Update)
        assertEquals(listOf("дома", "учёба"), viewModel.state.value.tasks.single().tags)

        viewModel.onIntent(TasksUiIntent.Create)
        viewModel.onIntent(TasksUiIntent.ChangeTitle("Other"))
        viewModel.onIntent(TasksUiIntent.Update)
        viewModel.onIntent(TasksUiIntent.FilterByTag("УЧЁБА"))
        assertEquals(listOf("Read"), viewModel.state.value.visibleTasks.map { it.title })
        assertEquals(listOf("дома", "учёба"), viewModel.state.value.availableTags)
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
            blocks = emptyList(),
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
    fun confirmMove_daily_keepsTemplate_andAddsEmptySubtask() {
        val template = Task(
            id = TaskId("daily"),
            title = "Standup",
            colorArgb = 0xFF3949AB,
            blocks = listOf(TimeBlock(9 * 60, 9 * 60 + 15)),
            date = date,
            recurrence = RecurrenceRule.Daily,
            seriesId = "daily",
        )
        val dest = LocalDate(2026, 10, 8)
        val repository = FakeTaskRepository(listOf(template))
        val viewModel = TasksViewModel(repository, FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.RequestMove(template))
        viewModel.onIntent(TasksUiIntent.ConfirmMove(dest))
        val stored = viewModel.state.value.tasks
        val kept = stored.single { it.id == template.id }
        assertEquals(date, kept.date)
        assertEquals(template.blocks, kept.blocks)
        val subtask = stored.single { it.date == dest }
        assertTrue(subtask.blocks.isEmpty())
        assertEquals("daily", subtask.seriesId)
        assertEquals(RecurrenceRule.None, subtask.recurrence)
        viewModel.onIntent(TasksUiIntent.RequestMove(template))
        viewModel.onIntent(TasksUiIntent.ConfirmMove(dest))
        assertEquals(2, viewModel.state.value.tasks.size)
    }

    @Test
    fun dailyEditor_savesIntervalOnlyOnChosenDate() {
        val template = Task(
            id = TaskId("daily"),
            title = "Standup",
            colorArgb = 0xFF3949AB,
            blocks = emptyList(),
            date = date,
            recurrence = RecurrenceRule.Daily,
            seriesId = "daily",
        )
        val other = date.plus(1, DateTimeUnit.DAY)
        val viewModel = TasksViewModel(FakeTaskRepository(listOf(template)), FakeTimeProvider(date), FakeReportRepository())
        viewModel.onIntent(TasksUiIntent.Edit(template))
        assertNull(viewModel.state.value.editor?.intervalDate)
        viewModel.onIntent(TasksUiIntent.AddBlock)
        assertTrue(viewModel.state.value.editor!!.blocks.isEmpty())
        viewModel.onIntent(TasksUiIntent.SelectIntervalDate(date))
        viewModel.onIntent(TasksUiIntent.AddBlock)
        viewModel.onIntent(TasksUiIntent.SelectIntervalDate(other))
        val templateBlocks = viewModel.state.value.tasks.single { it.id == template.id }.blocks
        assertEquals(1, templateBlocks.size)
        assertTrue(viewModel.state.value.editor!!.blocks.isEmpty())
        assertTrue(viewModel.state.value.tasks.none { it.date == other })
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

    @Test
    fun planReport_writesOnlyAfterAccept_andKeepsTheMeeting() = runTest {
        val meeting = meeting()
        val harness = plannerHarness(listOf(meeting))
        val deadline = date.plus(2, DateTimeUnit.DAY)
        val viewModel = harness.viewModel
        viewModel.onIntent(TasksUiIntent.OpenPlan)
        viewModel.onIntent(TasksUiIntent.ChangePlanGoal("Подготовить отчёт"))
        viewModel.onIntent(TasksUiIntent.SelectPlanDeadline(deadline))
        viewModel.onIntent(TasksUiIntent.SubmitPlan)

        assertEquals(listOf(meeting.id), viewModel.state.value.tasks.map { it.id })
        val draft = viewModel.state.value.planSheet as PlanSheetUi.Draft
        assertEquals(PlanVerdict.FITS, draft.draft.draft.verdict)
        assertEquals(1, harness.planner.planCalls)
        assertTrue(harness.store.plans.isEmpty())

        viewModel.onIntent(TasksUiIntent.AcceptPlan)

        assertNull(viewModel.state.value.planSheet)
        val tasks = viewModel.state.value.tasks
        val kept = tasks.single { it.id == meeting.id }
        assertEquals(meeting.blocks, kept.blocks)
        assertEquals(meeting.date, kept.date)
        val added = tasks.filter { it.id != meeting.id }
        assertEquals(3, added.size)
        assertEquals(
            listOf(TaskPalette.colorAt(1), TaskPalette.colorAt(2), TaskPalette.colorAt(3)),
            added.map { it.colorArgb },
        )
        added.forEach { task ->
            assertEquals(TaskStatus.TODO, task.status)
            assertEquals(Importance.MEDIUM, task.importance)
            assertEquals("Подготовить отчёт", task.project)
            val block = task.blocks.single()
            val end = checkNotNull(block.endMinute)
            val day = checkNotNull(task.date)
            assertTrue(end > block.startMinute)
            assertTrue(block.startMinute >= 9 * 60 && end <= 19 * 60)
            assertTrue(day >= date && day <= deadline)
            if (day == date) {
                assertTrue(block.startMinute >= 12 * 60 || end <= 10 * 60)
            }
        }
        val saved = harness.store.plans.single()
        assertEquals("Подготовить отчёт", saved.goal)
        assertEquals(deadline, saved.deadline)
        assertEquals(3, saved.steps.size)
        assertTrue(saved.steps.none { it.unplaced })
    }

    @Test
    fun dozens_impossible_acceptWritesOnlyPlacedSteps() = runTest {
        val meeting = meeting()
        val harness = plannerHarness(listOf(meeting))
        val tomorrow = date.plus(1, DateTimeUnit.DAY)
        val viewModel = harness.viewModel
        viewModel.onIntent(TasksUiIntent.OpenPlan)
        viewModel.onIntent(TasksUiIntent.ChangePlanGoal("десятки часов"))
        viewModel.onIntent(TasksUiIntent.SelectPlanDeadline(tomorrow))
        viewModel.onIntent(TasksUiIntent.SubmitPlan)

        val draft = viewModel.state.value.planSheet as PlanSheetUi.Draft
        assertEquals(PlanVerdict.IMPOSSIBLE, draft.draft.draft.verdict)
        assertEquals(listOf(meeting.id), viewModel.state.value.tasks.map { it.id })
        assertTrue(draft.draft.draft.steps.any { it.unplaced })
        assertTrue(draft.draft.canPlace)

        viewModel.onIntent(TasksUiIntent.AcceptPlan)

        val tasks = viewModel.state.value.tasks
        val kept = tasks.single { it.id == meeting.id }
        assertEquals(listOf(TimeBlock(10 * 60, 12 * 60)), kept.blocks)
        val added = tasks.filter { it.id != meeting.id }
        assertEquals(1, added.size)
        assertEquals(tomorrow, added.single().date)
        assertEquals(9 * 60, added.single().startMinute)
        assertEquals(19 * 60, added.single().endMinute)
        assertEquals(TaskStatus.TODO, added.single().status)
        val left = viewModel.state.value.planSheet as PlanSheetUi.Draft
        assertEquals(3, left.draft.draft.steps.size)
        assertTrue(left.draft.draft.steps.all { it.unplaced })
        assertEquals(1, harness.store.plans.single().steps.size)
    }

    @Test
    fun yesterdayDeadline_staysOnTheFormAndWritesNothing() = runTest {
        val harness = plannerHarness()
        val viewModel = harness.viewModel
        viewModel.onIntent(TasksUiIntent.OpenPlan)
        viewModel.onIntent(TasksUiIntent.ChangePlanGoal("Подготовить отчёт"))
        viewModel.onIntent(TasksUiIntent.SelectPlanDeadline(date.minus(1, DateTimeUnit.DAY)))
        viewModel.onIntent(TasksUiIntent.SubmitPlan)

        val form = viewModel.state.value.planSheet as PlanSheetUi.Form
        assertEquals(RussianPlannerPhrases.deadlinePassed(), form.form.message)
        assertEquals(0, harness.planner.planCalls)
        assertTrue(viewModel.state.value.tasks.isEmpty())
        assertTrue(harness.store.plans.isEmpty())
    }

    @Test
    fun emptyGoal_zeroWindow_andOvernight_doNotCallThePlanner() = runTest {
        val harness = plannerHarness()
        val viewModel = harness.viewModel
        viewModel.onIntent(TasksUiIntent.OpenPlan)
        viewModel.onIntent(TasksUiIntent.SubmitPlan)
        val empty = viewModel.state.value.planSheet as PlanSheetUi.Form
        assertEquals(RussianPlannerPhrases.emptyGoal(), empty.form.message)

        viewModel.onIntent(TasksUiIntent.ChangePlanGoal("Подготовить отчёт"))
        viewModel.onIntent(TasksUiIntent.SetPlanWindow(9 * 60, 9 * 60))
        viewModel.onIntent(TasksUiIntent.SubmitPlan)
        val zero = viewModel.state.value.planSheet as PlanSheetUi.Form
        assertEquals(RussianPlannerPhrases.invalidWindow(), zero.form.message)

        viewModel.onIntent(TasksUiIntent.SetPlanWindow(22 * 60, 6 * 60))
        viewModel.onIntent(TasksUiIntent.SubmitPlan)
        val overnight = viewModel.state.value.planSheet as PlanSheetUi.Form
        assertEquals(RussianPlannerPhrases.invalidWindow(), overnight.form.message)
        assertEquals(0, harness.planner.planCalls)
        assertTrue(viewModel.state.value.tasks.isEmpty())
    }

    @Test
    fun offlineGoal_keepsTheFormAndTheExistingTasks() = runTest {
        val meeting = meeting()
        val harness = plannerHarness(listOf(meeting))
        val viewModel = harness.viewModel
        viewModel.onIntent(TasksUiIntent.OpenPlan)
        viewModel.onIntent(TasksUiIntent.ChangePlanGoal("офлайн Подготовить отчёт"))
        viewModel.onIntent(TasksUiIntent.SubmitPlan)
        viewModel.onIntent(TasksUiIntent.SubmitPlan)

        val form = viewModel.state.value.planSheet as PlanSheetUi.Form
        assertEquals(RussianPlannerPhrases.serviceUnavailable(), form.form.message)
        assertEquals(2, harness.planner.planCalls)
        assertEquals(listOf(meeting), viewModel.state.value.tasks)
        assertTrue(harness.store.plans.isEmpty())
    }

    @Test
    fun removeStep_doesNotCallPlanner_windowChangeDoes() = runTest {
        val meeting = meeting()
        val harness = plannerHarness(listOf(meeting))
        val viewModel = harness.viewModel
        viewModel.onIntent(TasksUiIntent.OpenPlan)
        viewModel.onIntent(TasksUiIntent.ChangePlanGoal("Подготовить отчёт"))
        viewModel.onIntent(TasksUiIntent.SelectPlanDeadline(date.plus(2, DateTimeUnit.DAY)))
        viewModel.onIntent(TasksUiIntent.SubmitPlan)
        val first = viewModel.state.value.planSheet as PlanSheetUi.Draft
        val firstId = first.draft.draft.draftId

        viewModel.onIntent(TasksUiIntent.RemovePlanStep(0))

        val removed = viewModel.state.value.planSheet as PlanSheetUi.Draft
        assertEquals(firstId, removed.draft.draft.draftId)
        assertEquals(2, removed.draft.draft.steps.size)
        assertEquals(1, harness.planner.planCalls)
        assertEquals(listOf(meeting.id), viewModel.state.value.tasks.map { it.id })

        viewModel.onIntent(TasksUiIntent.SetPlanWindow(10 * 60, 18 * 60))

        val replanned = viewModel.state.value.planSheet as PlanSheetUi.Draft
        assertEquals(2, harness.planner.planCalls)
        assertTrue(replanned.draft.draft.draftId != firstId)
        assertEquals(listOf(meeting.id), viewModel.state.value.tasks.map { it.id })
    }

    @Test
    fun dismissDraft_writesNothing() = runTest {
        val meeting = meeting()
        val harness = plannerHarness(listOf(meeting))
        val viewModel = harness.viewModel
        viewModel.onIntent(TasksUiIntent.OpenPlan)
        viewModel.onIntent(TasksUiIntent.ChangePlanGoal("Подготовить отчёт"))
        viewModel.onIntent(TasksUiIntent.SubmitPlan)
        viewModel.onIntent(TasksUiIntent.DismissPlan)

        assertNull(viewModel.state.value.planSheet)
        assertEquals(listOf(meeting), viewModel.state.value.tasks)
        assertTrue(harness.store.plans.isEmpty())
    }

    private fun meeting(): Task = Task(
        id = TaskId("meet"),
        title = "Встреча",
        colorArgb = 0xFF112233,
        blocks = listOf(TimeBlock(10 * 60, 12 * 60)),
        date = date,
    )

    private fun plannerHarness(tasks: List<Task> = emptyList()): PlannerHarness {
        val repository = FakeTaskRepository(tasks)
        val store = MemoryPlanStore()
        var issued = 0
        val clock = FakeTimeProvider(date)
        val counting = CountingPlanner(
            MockPlannerSource(
                timeProvider = clock,
                planStore = store,
                phrases = RussianPlannerPhrases,
                newDraftId = { "draft-${++issued}" },
            ),
        )
        val viewModel = TasksViewModel(
            repository,
            clock,
            FakeReportRepository(),
            plannerSource = counting,
            planStore = store,
            phrases = RussianPlannerPhrases,
        )
        return PlannerHarness(viewModel, store, counting)
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

private class MemoryPlanStore : PlannerPlanStore {
    val plans = mutableListOf<AcceptedPlan>()

    override suspend fun deviceKey(): String = "device-test"

    override fun observeAcceptedPlans(): Flow<List<AcceptedPlan>> = flowOf(plans.toList())

    override suspend fun acceptedPlans(): List<AcceptedPlan> = plans.toList()

    override suspend fun saveAcceptedPlan(plan: AcceptedPlan) {
        plans += plan
    }
}

private class CountingPlanner(
    private val inner: PlannerSource,
) : PlannerSource {
    var planCalls: Int = 0

    override suspend fun plan(request: PlanRequest): PlanOutcome {
        planCalls += 1
        return inner.plan(request)
    }

    override suspend fun report(request: ReportRequest) = inner.report(request)
}

private class PlannerHarness(
    val viewModel: TasksViewModel,
    val store: MemoryPlanStore,
    val planner: CountingPlanner,
)
