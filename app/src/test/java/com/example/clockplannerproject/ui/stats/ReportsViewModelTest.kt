package com.example.clockplannerproject.ui.stats

import com.example.clockplannerproject.data.MockPlannerSource
import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.ReportRepository
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskReport
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.kit.core.planner.AcceptedPlan
import com.example.clockplannerproject.kit.core.planner.PlanOutcome
import com.example.clockplannerproject.kit.core.planner.PlanRequest
import com.example.clockplannerproject.kit.core.planner.PlacedStep
import com.example.clockplannerproject.kit.core.planner.PlannerPlanStore
import com.example.clockplannerproject.kit.core.planner.PlannerSource
import com.example.clockplannerproject.kit.core.planner.ReportOutcome
import com.example.clockplannerproject.kit.core.planner.ReportRequest
import com.example.clockplannerproject.kit.core.planner.RussianPlannerPhrases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
import org.junit.Assert.assertTrue
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

    @Test
    fun collectToday_closedBlockAndAcceptedStep_comparesInTheNote() {
        val today = LocalDate(2026, 11, 6)
        val goal = Task(
            id = TaskId("goal"),
            title = "Подготовить отчёт",
            colorArgb = 1L,
            blocks = emptyList(),
            status = TaskStatus.TODO,
            date = null,
        )
        val closed = Task(
            id = TaskId("done"),
            title = "Черновик",
            colorArgb = 2L,
            blocks = listOf(TimeBlock(9 * 60, 11 * 60)),
            status = TaskStatus.DONE,
            date = today,
        )
        val plan = acceptedStep(today, start = 12 * 60, end = 13 * 60)
        val harness = harness(today, tasks = listOf(goal, closed), plans = listOf(plan))
        harness.viewModel.onIntent(ReportsUiIntent.CollectToday)
        val note = harness.viewModel.state.value.reports.single()
        assertTrue(note.text.contains("В плане стояло 60 мин, закрыто 120 мин."))
        assertEquals(goal.id, note.taskId)
        assertEquals(listOf(goal.id, closed.id), harness.tasks.snapshot().map { it.id })
        assertEquals(listOf(TimeBlock(9 * 60, 11 * 60)), harness.tasks.snapshot().single { it.id == closed.id }.blocks)
    }

    @Test
    fun collectToday_emptyDay_writesHonestNoteAndKeepsItEditable() {
        val today = LocalDate(2026, 11, 6)
        val harness = harness(today)
        harness.viewModel.onIntent(ReportsUiIntent.CollectToday)
        val note = harness.viewModel.state.value.reports.single()
        assertTrue(note.text.contains("Закрытых отрезков нет."))
        assertTrue(note.text.contains("Сравнивать не с чем."))
        val anchor = harness.tasks.snapshot().single()
        assertEquals("Отчёт 2026-11-06", anchor.title)
        assertTrue(anchor.blocks.isEmpty())
        assertEquals(null, anchor.date)
        assertEquals(anchor.id, note.taskId)

        harness.viewModel.onIntent(ReportsUiIntent.CollectToday)
        assertEquals(2, harness.viewModel.state.value.reports.size)
        assertEquals(1, harness.tasks.snapshot().size)
        assertEquals(2, harness.viewModel.state.value.reports.map { it.id }.distinct().size)

        val latest = harness.viewModel.state.value.reports.last()
        harness.viewModel.onIntent(ReportsUiIntent.Edit(latest))
        harness.viewModel.onIntent(ReportsUiIntent.ChangeDraft("Поправил текст"))
        harness.viewModel.onIntent(ReportsUiIntent.Save)
        assertEquals("Поправил текст", harness.viewModel.state.value.reports.last().text)
        val saved = harness.viewModel.state.value.reports.last()
        harness.viewModel.onIntent(ReportsUiIntent.Delete(saved.id))
        assertEquals(1, harness.viewModel.state.value.reports.size)
        assertEquals(1, harness.tasks.snapshot().size)
    }

    @Test
    fun collectWeek_countsSevenDaysAndIgnoresAnOpenTimerOutsideThem() {
        val today = LocalDate(2026, 11, 7)
        val insideDay = today.minus(6, DateTimeUnit.DAY)
        val beforeWeek = today.minus(7, DateTimeUnit.DAY)
        val tomorrow = today.plus(1, DateTimeUnit.DAY)
        val inside = Task(
            id = TaskId("inside"),
            title = "Внутри",
            colorArgb = 1L,
            blocks = listOf(TimeBlock(10 * 60, 11 * 60)),
            status = TaskStatus.DONE,
            date = insideDay,
        )
        val outside = Task(
            id = TaskId("outside"),
            title = "Раньше",
            colorArgb = 2L,
            blocks = listOf(TimeBlock(10 * 60, 12 * 60)),
            status = TaskStatus.DONE,
            date = beforeWeek,
        )
        val running = Task(
            id = TaskId("later"),
            title = "Завтра",
            colorArgb = 3L,
            blocks = listOf(TimeBlock(10 * 60, endMinute = null)),
            status = TaskStatus.IN_PROGRESS,
            date = tomorrow,
        )
        val plan = acceptedStep(insideDay, start = 12 * 60, end = 12 * 60 + 45)
        val harness = harness(
            today = today,
            tasks = listOf(inside, outside, running),
            plans = listOf(plan),
        )
        harness.viewModel.onIntent(ReportsUiIntent.CollectWeek)
        val request = harness.requests.last()
        assertEquals(insideDay, request.from)
        assertEquals(today, request.to)
        assertEquals(listOf(inside.id), request.tasks.map { it.id })
        val note = harness.viewModel.state.value.reports.single()
        assertTrue(note.text.contains("В плане стояло 45 мин, закрыто 60 мин."))
    }

    @Test
    fun collectToday_offlineTitle_keepsNotesAndDoesNotDeleteTasks() {
        val today = LocalDate(2026, 11, 6)
        val kept = TaskReport(
            id = ReportId("kept"),
            taskId = TaskId("sport"),
            createdAtEpochMillis = 5L,
            text = "Уже было",
        )
        val offline = Task(
            id = TaskId("off"),
            title = "офлайн встреча",
            colorArgb = 1L,
            blocks = listOf(TimeBlock(10 * 60, 11 * 60)),
            status = TaskStatus.TODO,
            date = today,
        )
        val harness = harness(today, tasks = listOf(offline), reports = listOf(kept))
        val messages = mutableListOf<ReportsUiEffect>()
        val scope = CoroutineScope(dispatcher + SupervisorJob())
        scope.launch { harness.viewModel.effect.collect { messages += it } }
        harness.viewModel.onIntent(ReportsUiIntent.CollectToday)
        scope.cancel()
        assertEquals("Сервис недоступен.", (messages.single() as ReportsUiEffect.ShowMessage).message)
        assertEquals(listOf(kept), harness.viewModel.state.value.reports)
        assertEquals(listOf(offline), harness.tasks.snapshot())
    }

    private fun harness(
        today: LocalDate,
        tasks: List<Task> = emptyList(),
        plans: List<AcceptedPlan> = emptyList(),
        reports: List<TaskReport> = emptyList(),
    ): ReportHarness {
        val taskRepository = FakeTasks(tasks)
        val planStore = MemoryReportPlanStore(plans)
        val clock = FixedReportClock(today)
        val recording = RecordingPlanner(
            MockPlannerSource(
                timeProvider = clock,
                planStore = planStore,
                phrases = RussianPlannerPhrases,
            ),
        )
        val viewModel = ReportsViewModel(
            reportRepository = FakeReports(reports),
            taskRepository = taskRepository,
            plannerSource = recording,
            planStore = planStore,
            timeProvider = clock,
            phrases = RussianPlannerPhrases,
        )
        return ReportHarness(viewModel, taskRepository, recording.requests)
    }

    private fun acceptedStep(date: LocalDate, start: Int, end: Int) = AcceptedPlan(
        id = "p1",
        goal = "Подготовить отчёт",
        deadline = date,
        steps = listOf(
            PlacedStep(
                title = "Черновик",
                date = date,
                startMinute = start,
                endMinute = end,
                unplaced = false,
                estimateMinutes = end - start,
                bufferedMinutes = end - start,
                importance = Importance.MEDIUM,
                project = "Подготовить отчёт",
            ),
        ),
    )
}

private class ReportHarness(
    val viewModel: ReportsViewModel,
    val tasks: FakeTasks,
    val requests: List<ReportRequest>,
)

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

private class FakeTasks(
    initial: List<Task>,
) : TaskRepository {
    private val items = MutableStateFlow(initial)

    fun snapshot(): List<Task> = items.value

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

    override suspend fun listRecurringTemplates(): List<Task> = emptyList()

    override suspend fun hasSeriesOnDate(seriesId: String, date: LocalDate): Boolean = false
}

private class MemoryReportPlanStore(
    initial: List<AcceptedPlan>,
) : PlannerPlanStore {
    private val plans = MutableStateFlow(initial)

    override suspend fun deviceKey(): String = "device-test"

    override fun observeAcceptedPlans(): Flow<List<AcceptedPlan>> = plans

    override suspend fun acceptedPlans(): List<AcceptedPlan> = plans.value

    override suspend fun saveAcceptedPlan(plan: AcceptedPlan) {
        plans.update { it + plan }
    }
}

private class FixedReportClock(
    private val date: LocalDate,
) : TimeProvider {
    override fun now(): LocalTime = LocalTime(9, 0)

    override fun today(): LocalDate = date

    override fun observeTime(): Flow<LocalTime> = flowOf(now())
}

private class RecordingPlanner(
    private val inner: PlannerSource,
) : PlannerSource {
    val requests = mutableListOf<ReportRequest>()

    override suspend fun plan(request: PlanRequest): PlanOutcome = inner.plan(request)

    override suspend fun report(request: ReportRequest): ReportOutcome {
        requests += request
        return inner.report(request)
    }
}
