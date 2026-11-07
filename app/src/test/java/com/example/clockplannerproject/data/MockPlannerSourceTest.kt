package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.kit.core.planner.AcceptedPlan
import com.example.clockplannerproject.kit.core.planner.Capacity
import com.example.clockplannerproject.kit.core.planner.PlanOutcome
import com.example.clockplannerproject.kit.core.planner.PlanRejection
import com.example.clockplannerproject.kit.core.planner.PlanRequest
import com.example.clockplannerproject.kit.core.planner.PlanVerdict
import com.example.clockplannerproject.kit.core.planner.PlacedStep
import com.example.clockplannerproject.kit.core.planner.PlannerPlanStore
import com.example.clockplannerproject.kit.core.planner.ReportOutcome
import com.example.clockplannerproject.kit.core.planner.ReportRejection
import com.example.clockplannerproject.kit.core.planner.ReportRequest
import com.example.clockplannerproject.kit.core.planner.ReportResult
import com.example.clockplannerproject.kit.core.planner.RussianPlannerPhrases
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockPlannerSourceTest {

    private val today = LocalDate(2026, 11, 6)
    private val zone = TimeZone.UTC

    @Test
    fun offlineGoal_returnsNoSteps() = runTest {
        val planner = planner()
        val lower = planner.plan(request("офлайн Подготовить отчёт"))
        val upper = planner.plan(request("ОФЛАЙН отчёт"))
        assertTrue(lower is PlanOutcome.Rejected)
        assertTrue(upper is PlanOutcome.Rejected)
        assertTrue((lower as PlanOutcome.Rejected).reason is PlanRejection.Unavailable)
        assertEquals("Сервис недоступен.", lower.reason.message)
        assertTrue(planner.store.key.isNotBlank())
    }

    @Test
    fun sameRequest_returnsSameDraftId() = runTest {
        val planner = planner()
        val first = planner.plan(request("Подготовить отчёт")) as PlanOutcome.Ready
        val second = planner.plan(request("  Подготовить отчёт  ")) as PlanOutcome.Ready
        assertEquals(first.draft.draftId, second.draft.draftId)
        assertEquals(first.draft, second.draft)
        assertEquals(1, planner.issuedIds)
        assertEquals(
            listOf(
                "Подготовить отчёт: собрать",
                "Подготовить отчёт: черновик",
                "Подготовить отчёт: вычитка",
            ),
            first.draft.steps.map { it.title },
        )
        assertTrue(first.draft.steps.all { it.project == "Подготовить отчёт" && it.importance == Importance.MEDIUM })
        assertEquals(listOf(120, 150, 120), first.draft.steps.map { it.estimateMinutes })
        assertEquals(listOf(144, 180, 144), first.draft.steps.map { it.bufferedMinutes })
        assertEquals(planner.store.key, planner.plan(request("Подготовить отчёт")).let { planner.store.key })
    }

    @Test
    fun taskOrder_doesNotChangeDraftId() = runTest {
        val planner = planner()
        val firstTask = sampleTask("a")
        val secondTask = sampleTask("b")
        val forward = planner.plan(request("Цель", tasks = listOf(firstTask, secondTask))) as PlanOutcome.Ready
        val backward = planner.plan(request("Цель", tasks = listOf(secondTask, firstTask))) as PlanOutcome.Ready
        val changed = planner.plan(
            request("Цель", tasks = listOf(firstTask.copy(title = "Другое"))),
        ) as PlanOutcome.Ready
        assertEquals(forward.draft.draftId, backward.draft.draftId)
        assertNotEquals(forward.draft.draftId, changed.draft.draftId)
    }

    @Test
    fun blankGoal_pastDeadline_orEmptyWindow_haveNoDraft() = runTest {
        val planner = planner()
        val blank = planner.plan(request("   ")) as PlanOutcome.Rejected
        val past = planner.plan(
            request("Цель", deadline = today.minus(1, DateTimeUnit.DAY)),
        ) as PlanOutcome.Rejected
        val emptyWindow = planner.plan(request("Цель", workStart = 9 * 60, workEnd = 9 * 60)) as PlanOutcome.Rejected
        val reversed = planner.plan(request("Цель", workStart = 19 * 60, workEnd = 9 * 60)) as PlanOutcome.Rejected
        assertTrue(blank.reason is PlanRejection.EmptyGoal)
        assertTrue(past.reason is PlanRejection.DeadlineBeforeToday)
        assertTrue(emptyWindow.reason is PlanRejection.InvalidWindow)
        assertTrue(reversed.reason is PlanRejection.InvalidWindow)
        assertEquals(0, planner.issuedIds)
    }

    @Test
    fun dozens_impossibleNarrativeUsesCapacityShortage() = runTest {
        val planner = planner()
        val ready = planner.plan(request("Нужны десятки часов")) as PlanOutcome.Ready
        val draft = ready.draft
        assertEquals(PlanVerdict.IMPOSSIBLE, draft.verdict)
        assertEquals(4, draft.steps.size)
        assertTrue(draft.steps.all { it.estimateMinutes == 600 && it.project == "Нужны десятки часов" })
        val shortage = (draft.requiredMinutes - draft.freeMinutes).coerceAtLeast(0)
        assertEquals(2880 - 600, shortage)
        assertTrue(draft.narrative.contains(shortage.toString()))
        assertTrue(draft.narrative.contains("укоротить цель"))
        assertTrue(draft.narrative.contains("сдвинуть день"))
        assertTrue(draft.narrative.contains("снять необязательное"))
        assertEquals(Capacity.bufferedMinutesOf(600) * 4, draft.requiredMinutes)
    }

    @Test
    fun report_emptyDay_saysThereIsNothingClosedAndNothingToCompare() = runTest {
        val result = ready(planner().report(report(tasks = emptyList())))
        assertEquals(0, result.doneMinutes)
        assertEquals(0, result.plannedMinutes)
        assertTrue(result.text.contains("Закрытых отрезков нет."))
        assertTrue(result.text.contains("Сравнивать не с чем."))
    }

    @Test
    fun report_comparesClosedBlockWithAcceptedStep() = runTest {
        val closed = sampleTask("done", blocks = listOf(TimeBlock(9 * 60, 11 * 60)))
        val plan = AcceptedPlan(
            id = "p1",
            goal = "Подготовить отчёт",
            deadline = today,
            steps = listOf(
                PlacedStep(
                    title = "Черновик",
                    date = today,
                    startMinute = 12 * 60,
                    endMinute = 13 * 60,
                    unplaced = false,
                    estimateMinutes = 60,
                    bufferedMinutes = 72,
                    importance = Importance.MEDIUM,
                    project = "Подготовить отчёт",
                ),
            ),
        )
        val result = ready(planner().report(report(tasks = listOf(closed), plans = listOf(plan))))
        assertEquals(120, result.doneMinutes)
        assertEquals(60, result.plannedMinutes)
        assertTrue(result.text.contains("В плане стояло 60 мин, закрыто 120 мин."))
    }

    @Test
    fun report_openTimerOnFutureDay_addsNoMinutes() = runTest {
        val tomorrow = today.plus(1, DateTimeUnit.DAY)
        val running = sampleTask(
            id = "later",
            date = tomorrow,
            blocks = listOf(TimeBlock(10 * 60, endMinute = null)),
            status = TaskStatus.IN_PROGRESS,
        )
        val result = ready(
            planner().report(
                ReportRequest(
                    from = tomorrow,
                    to = tomorrow,
                    zone = zone,
                    tasks = listOf(running),
                ),
            ),
        )
        assertEquals(0, result.doneMinutes)
        assertTrue(result.text.contains("Закрытых отрезков нет."))
    }

    @Test
    fun report_doneTaskWithoutBlocks_isNamedAndAddsNoMinutes() = runTest {
        val note = sampleTask("letter", title = "Письмо", blocks = emptyList(), status = TaskStatus.DONE)
        val result = ready(planner().report(report(tasks = listOf(note))))
        assertEquals(0, result.doneMinutes)
        assertTrue(result.text.contains("Закрытых отрезков нет."))
        assertTrue(result.text.contains("Письмо"))
    }

    @Test
    fun report_offlineGoalOrTitle_refusesWithoutAParagraph() = runTest {
        val planner = planner()
        val byGoal = planner.report(
            report(
                tasks = emptyList(),
                plans = listOf(
                    AcceptedPlan(
                        id = "offline",
                        goal = "офлайн Подготовить отчёт",
                        deadline = today,
                        steps = emptyList(),
                    ),
                ),
            ),
        )
        val byTitle = planner.report(
            report(tasks = listOf(sampleTask("x", title = "ОФЛАЙН встреча", blocks = emptyList()))),
        )
        assertTrue(byGoal is ReportOutcome.Rejected)
        assertTrue(byTitle is ReportOutcome.Rejected)
        assertTrue((byGoal as ReportOutcome.Rejected).reason is ReportRejection.Unavailable)
        assertEquals("Сервис недоступен.", byGoal.reason.message)
        assertEquals("Сервис недоступен.", (byTitle as ReportOutcome.Rejected).reason.message)
    }

    private fun ready(outcome: ReportOutcome): ReportResult {
        assertTrue(outcome is ReportOutcome.Ready)
        return (outcome as ReportOutcome.Ready).result
    }

    private fun planner(): Harness {
        val store = MemoryPlanStore()
        var issued = 0
        val source = MockPlannerSource(
            timeProvider = FixedClock(today, minute = 9 * 60),
            planStore = store,
            phrases = RussianPlannerPhrases,
            newDraftId = { "draft-${++issued}" },
        )
        return Harness(source, store) { issued }
    }

    private fun request(
        goal: String,
        deadline: LocalDate = today,
        workStart: Int = 9 * 60,
        workEnd: Int = 19 * 60,
        tasks: List<Task> = emptyList(),
    ) = PlanRequest(
        goal = goal,
        deadline = deadline,
        zone = zone,
        workStartMinute = workStart,
        workEndMinute = workEnd,
        tasks = tasks,
    )

    private fun report(
        tasks: List<Task>,
        plans: List<AcceptedPlan> = emptyList(),
    ) = ReportRequest(
        from = today,
        to = today,
        zone = zone,
        tasks = tasks,
        acceptedPlans = plans,
    )

    private fun sampleTask(
        id: String,
        title: String = id,
        date: LocalDate = today,
        blocks: List<TimeBlock> = listOf(TimeBlock(10 * 60, 11 * 60)),
        status: TaskStatus = TaskStatus.TODO,
    ) = Task(
        id = TaskId(id),
        title = title,
        colorArgb = 1L,
        blocks = blocks,
        status = status,
        date = date,
    )

    private class Harness(
        private val source: MockPlannerSource,
        val store: MemoryPlanStore,
        private val issued: () -> Int,
    ) {
        val issuedIds: Int get() = issued()

        suspend fun plan(request: PlanRequest): PlanOutcome = source.plan(request)

        suspend fun report(request: ReportRequest) = source.report(request)
    }
}

private class FixedClock(
    private val date: LocalDate,
    private val minute: Int,
) : TimeProvider {
    override fun now(): LocalTime = LocalTime(minute / 60, minute % 60)

    override fun today(): LocalDate = date

    override fun observeTime() = flowOf(now())
}

private class MemoryPlanStore : PlannerPlanStore {
    var key: String = ""
    private val plans = MutableStateFlow<List<AcceptedPlan>>(emptyList())

    override suspend fun deviceKey(): String {
        if (key.isEmpty()) key = "device-test"
        return key
    }

    override fun observeAcceptedPlans(): Flow<List<AcceptedPlan>> = plans

    override suspend fun acceptedPlans(): List<AcceptedPlan> = plans.value

    override suspend fun saveAcceptedPlan(plan: AcceptedPlan) {
        plans.value = plans.value + plan
    }
}
