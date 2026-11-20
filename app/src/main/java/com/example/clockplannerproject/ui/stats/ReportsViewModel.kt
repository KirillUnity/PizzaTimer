package com.example.clockplannerproject.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.ReportAttachmentImporter
import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.ReportRepository
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskPalette
import com.example.clockplannerproject.kit.core.TaskReport
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.kit.core.planner.PlannerPhrases
import com.example.clockplannerproject.kit.core.planner.PlannerPlanStore
import com.example.clockplannerproject.kit.core.planner.PlannerSource
import com.example.clockplannerproject.kit.core.planner.ReportAnchor
import com.example.clockplannerproject.kit.core.planner.ReportOutcome
import com.example.clockplannerproject.kit.core.planner.ReportPeriod
import com.example.clockplannerproject.kit.core.planner.ReportRequest
import com.example.clockplannerproject.kit.core.planner.RussianPlannerPhrases
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

/**
 * Reports tab: list every text note, and collect a local paragraph into a new note.
 *
 * Collect does not run on a schedule. A second press appends another note.
 * The screen still refuses to save a note that has no [TaskId].
 *
 * @param reportRepository note store
 * @param taskRepository tasks used as the report slice and as the note anchor
 * @param plannerSource on-device mock; it does not keep a diary
 * @param planStore accepted plans read for the comparison paragraph
 * @param timeProvider local today that bounds the period
 * @param phrases localized anchor title; the paragraph itself comes from [plannerSource]
 * @param attachmentImporter copies a picked file into app storage
 * @since 0.5.0
 */
class ReportsViewModel(
    private val reportRepository: ReportRepository,
    private val taskRepository: TaskRepository = IdleTaskRepository,
    private val plannerSource: PlannerSource = IdleReportPlanner,
    private val planStore: PlannerPlanStore = IdleReportPlanStore,
    private val timeProvider: TimeProvider = IdleReportClock,
    private val phrases: PlannerPhrases = RussianPlannerPhrases,
    private val attachmentImporter: ReportAttachmentImporter = ReportAttachmentImporter.Unavailable,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsUiState())
    val state: StateFlow<ReportsUiState> = _state.asStateFlow()

    private val effects = Channel<ReportsUiEffect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            try {
                reportRepository.observeAllReports().collect { reports ->
                    _state.update {
                        it.copy(reports = reports, isLoading = false, errorMessage = null)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: error::class.simpleName,
                    )
                }
            }
        }
    }

    fun onIntent(intent: ReportsUiIntent) {
        when (intent) {
            is ReportsUiIntent.ChangeDraft -> _state.update { it.copy(draft = intent.text) }
            is ReportsUiIntent.Edit -> _state.update {
                it.copy(
                    editingId = intent.report.id,
                    editingTaskId = intent.report.taskId,
                    draft = intent.report.text,
                )
            }
            ReportsUiIntent.Save -> save()
            is ReportsUiIntent.Delete -> delete(intent.id)
            is ReportsUiIntent.ImportAttachment -> importAttachment(intent.id, intent.sourceUri)
            is ReportsUiIntent.RemoveAttachment -> removeAttachment(intent.id)
            ReportsUiIntent.CollectToday -> collect(ReportPeriod.day(timeProvider.today()))
            ReportsUiIntent.CollectWeek -> collect(ReportPeriod.weekEnding(timeProvider.today()))
        }
    }

    private fun collect(period: ReportPeriod.Inclusive) {
        if (_state.value.isCollecting) return
        _state.update { it.copy(isCollecting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val tasks = taskRepository.observeAll().first()
                val plans = planStore.acceptedPlans()
                val slice = tasks.filter { task ->
                    val date = task.date ?: return@filter false
                    date >= period.from && date <= period.to
                }
                val outcome = plannerSource.report(
                    ReportRequest(
                        from = period.from,
                        to = period.to,
                        zone = TimeZone.currentSystemDefault(),
                        tasks = slice,
                        acceptedPlans = plans,
                    ),
                )
                when (outcome) {
                    is ReportOutcome.Rejected -> {
                        _state.update { it.copy(errorMessage = outcome.reason.message) }
                        effects.send(ReportsUiEffect.ShowMessage(outcome.reason.message))
                    }
                    is ReportOutcome.Ready -> {
                        val taskId = anchorTaskId(tasks, plans, period)
                        reportRepository.upsert(
                            TaskReport(
                                id = ReportId(UUID.randomUUID().toString()),
                                taskId = taskId,
                                createdAtEpochMillis = System.currentTimeMillis(),
                                text = outcome.result.text,
                            ),
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                effects.send(ReportsUiEffect.ShowMessageRes(R.string.error_report_collect))
            } finally {
                _state.update { it.copy(isCollecting = false) }
            }
        }
    }

    private suspend fun anchorTaskId(
        tasks: List<Task>,
        plans: List<com.example.clockplannerproject.kit.core.planner.AcceptedPlan>,
        period: ReportPeriod.Inclusive,
    ): TaskId {
        val target = ReportAnchor.choose(
            tasks = tasks,
            plans = plans,
            from = period.from,
            to = period.to,
            fallbackTitle = phrases.reportAnchorTitle(period.to.toString()),
        )
        return when (target) {
            is ReportAnchor.Target.Existing -> target.taskId
            is ReportAnchor.Target.Create -> {
                val task = Task(
                    id = TaskId(UUID.randomUUID().toString()),
                    title = target.title,
                    colorArgb = TaskPalette.colorAt(tasks.size),
                    blocks = emptyList(),
                    status = TaskStatus.TODO,
                    date = null,
                )
                taskRepository.upsert(task)
                task.id
            }
        }
    }

    private fun importAttachment(id: ReportId, sourceUri: String) {
        val report = _state.value.reports.find { it.id == id } ?: return
        viewModelScope.launch {
            try {
                val attachment = attachmentImporter.import(sourceUri, report.id.value)
                reportRepository.upsert(report.copy(attachment = attachment))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                effects.send(ReportsUiEffect.ShowMessageRes(R.string.report_attach_failed))
            }
        }
    }

    private fun removeAttachment(id: ReportId) {
        val report = _state.value.reports.find { it.id == id } ?: return
        viewModelScope.launch {
            reportRepository.upsert(report.copy(attachment = null))
        }
    }

    private fun delete(id: ReportId) {
        viewModelScope.launch {
            reportRepository.delete(id)
            _state.update { state ->
                if (state.editingId == id) {
                    state.copy(draft = "", editingId = null, editingTaskId = null)
                } else {
                    state
                }
            }
        }
    }

    private fun save() {
        val snapshot = _state.value
        val text = snapshot.draft.trim()
        val taskId = snapshot.editingTaskId ?: return
        if (text.isEmpty()) return
        val existing = snapshot.reports.find { it.id == snapshot.editingId }
        val report = TaskReport(
            id = snapshot.editingId ?: ReportId(UUID.randomUUID().toString()),
            taskId = taskId,
            createdAtEpochMillis = existing?.createdAtEpochMillis ?: System.currentTimeMillis(),
            text = text,
            attachment = existing?.attachment,
        )
        viewModelScope.launch {
            try {
                reportRepository.upsert(report)
                _state.update { it.copy(draft = "", editingId = null, editingTaskId = null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                effects.send(ReportsUiEffect.ShowMessageRes(R.string.error_note_save))
            }
        }
    }
}

private object IdleTaskRepository : TaskRepository {
    override fun observeTasks(date: LocalDate): Flow<List<Task>> = flowOf(emptyList())

    override fun observeUnscheduled(): Flow<List<Task>> = flowOf(emptyList())

    override fun observeAll(): Flow<List<Task>> = flowOf(emptyList())

    override suspend fun upsert(task: Task) = Unit

    override suspend fun delete(taskId: TaskId) = Unit

    override suspend fun get(taskId: TaskId): Task? = null

    override suspend fun listRecurringTemplates(): List<Task> = emptyList()

    override suspend fun hasSeriesOnDate(seriesId: String, date: LocalDate): Boolean = false
}

private object IdleReportPlanner : PlannerSource {
    override suspend fun plan(
        request: com.example.clockplannerproject.kit.core.planner.PlanRequest,
    ): com.example.clockplannerproject.kit.core.planner.PlanOutcome =
        com.example.clockplannerproject.kit.core.planner.PlanOutcome.Rejected(
            com.example.clockplannerproject.kit.core.planner.PlanRejection.Unavailable(
                RussianPlannerPhrases.serviceUnavailable(),
            ),
        )

    override suspend fun report(request: ReportRequest): ReportOutcome =
        ReportOutcome.Rejected(
            com.example.clockplannerproject.kit.core.planner.ReportRejection.Unavailable(
                RussianPlannerPhrases.serviceUnavailable(),
            ),
        )
}

private object IdleReportPlanStore : PlannerPlanStore {
    override suspend fun deviceKey(): String = "idle"

    override fun observeAcceptedPlans(): Flow<List<com.example.clockplannerproject.kit.core.planner.AcceptedPlan>> =
        flowOf(emptyList())

    override suspend fun acceptedPlans(): List<com.example.clockplannerproject.kit.core.planner.AcceptedPlan> =
        emptyList()

    override suspend fun saveAcceptedPlan(
        plan: com.example.clockplannerproject.kit.core.planner.AcceptedPlan,
    ) = Unit
}

private object IdleReportClock : TimeProvider {
    override fun now(): LocalTime = LocalTime(0, 0)

    override fun today(): LocalDate = LocalDate(2026, 1, 1)

    override fun observeTime(): Flow<LocalTime> = flowOf(now())
}
