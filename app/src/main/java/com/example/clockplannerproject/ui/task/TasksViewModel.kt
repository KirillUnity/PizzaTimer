package com.example.clockplannerproject.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.RecurrenceMaterializer
import com.example.clockplannerproject.kit.core.RecurrenceRule
import com.example.clockplannerproject.kit.core.ReportAttachmentImporter
import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.ReportRepository
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskPalette
import com.example.clockplannerproject.kit.core.TaskReport
import com.example.clockplannerproject.kit.core.TaskDraftValidator
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskInstances
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TaskTags
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.kit.core.planner.AcceptedPlan
import com.example.clockplannerproject.kit.core.planner.PlacementContext
import com.example.clockplannerproject.kit.core.planner.PlanDraft
import com.example.clockplannerproject.kit.core.planner.PlanDraftEdit
import com.example.clockplannerproject.kit.core.planner.PlanOutcome
import com.example.clockplannerproject.kit.core.planner.PlanRequest
import com.example.clockplannerproject.kit.core.planner.ReportOutcome
import com.example.clockplannerproject.kit.core.planner.ReportResult
import com.example.clockplannerproject.kit.core.planner.PlanVerdict
import com.example.clockplannerproject.kit.core.planner.PlacedStep
import com.example.clockplannerproject.kit.core.planner.PlannerPhrases
import com.example.clockplannerproject.kit.core.planner.PlannerPlanStore
import com.example.clockplannerproject.kit.core.planner.PlannerSource
import com.example.clockplannerproject.kit.core.planner.RussianPlannerPhrases
import com.example.clockplannerproject.data.InMemoryPracticeProgressStore
import com.example.clockplannerproject.kit.core.practice.PracticeProgressStore
import com.example.clockplannerproject.kit.core.practice.PracticeSkill
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import java.util.UUID

/**
 * Tasks list and editor. Persistence goes through [TaskRepository]; the Day
 * dial refreshes from the same Room Flow.
 *
 * @since 0.2.0
 */
class TasksViewModel(
    private val taskRepository: TaskRepository,
    private val timeProvider: TimeProvider,
    private val reportRepository: ReportRepository,
    private val attachmentImporter: ReportAttachmentImporter = ReportAttachmentImporter.Unavailable,
    private val plannerSource: PlannerSource = IdlePlannerSource,
    private val planStore: PlannerPlanStore = IdlePlanStore,
    private val phrases: PlannerPhrases = RussianPlannerPhrases,
    private val practice: PracticeProgressStore = InMemoryPracticeProgressStore(),
) : ViewModel() {

    private val selectedDate = MutableStateFlow(timeProvider.today())
    private val editorTaskId = MutableStateFlow<TaskId?>(null)

    private val _state = MutableStateFlow(
        TasksUiState(
            date = selectedDate.value,
            isLoading = true,
            nowMinute = minuteOfNow(),
        ),
    )
    val state: StateFlow<TasksUiState> = _state.asStateFlow()

    private val effects = Channel<TasksUiEffect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()
    private val materializer = RecurrenceMaterializer(taskRepository)
    private var planGeneration: Int = 0

    init {
        observeSelectedDate()
        observeEditorReports()
    }

    fun onIntent(intent: TasksUiIntent) {
        when (intent) {
            TasksUiIntent.Create -> openCreate()
            TasksUiIntent.CreateForSelectedDay -> openCreate(scheduled = true)
            is TasksUiIntent.Edit -> {
                editorTaskId.value = intent.task.id
                _state.update {
                    it.copy(
                        editor = TaskEditorState.from(intent.task),
                        timePicker = null,
                        nowMinute = minuteOfNow(),
                        reportDraft = "",
                        editingReportId = null,
                    )
                }
            }
            TasksUiIntent.Update -> save()
            is TasksUiIntent.Delete -> _state.update { it.copy(pendingDelete = intent.task) }
            TasksUiIntent.ConfirmDelete -> confirmDelete()
            TasksUiIntent.DismissEditor -> {
                editorTaskId.value = null
                _state.update {
                    it.copy(
                        editor = null,
                        timePicker = null,
                        reports = emptyList(),
                        reportDraft = "",
                        editingReportId = null,
                    )
                }
            }
            TasksUiIntent.DismissDelete -> _state.update { it.copy(pendingDelete = null) }
            TasksUiIntent.PreviousDay -> shiftDate(-1)
            TasksUiIntent.NextDay -> shiftDate(1)
            TasksUiIntent.OpenDatePicker -> _state.update { it.copy(showDatePicker = true) }
            TasksUiIntent.DismissDatePicker -> _state.update { it.copy(showDatePicker = false) }
            is TasksUiIntent.SelectDate -> selectDate(intent.date)
            is TasksUiIntent.ChangeTitle -> updateEditor { it.copy(title = intent.title, error = null) }
            is TasksUiIntent.ChangeDescription -> updateEditor {
                it.copy(description = intent.description, error = null)
            }
            is TasksUiIntent.ChangeColor -> updateEditor { it.copy(colorArgb = intent.colorArgb) }
            is TasksUiIntent.ChangeBlockStart -> updateBlock(intent.index) { block ->
                block.copy(startMinute = intent.minute.mod(TimeMath.MINUTES_PER_DAY))
            }
            is TasksUiIntent.ChangeBlockEnd -> updateBlock(intent.index) { block ->
                val end = intent.minute.mod(TimeMath.MINUTES_PER_DAY)
                block.copy(endMinute = if (end == 0 && block.startMinute != 0) TimeMath.MINUTES_PER_DAY else end)
            }
            TasksUiIntent.AddBlock -> addBlock()
            is TasksUiIntent.RemoveBlock -> removeBlock(intent.index)
            TasksUiIntent.ClearTime -> updateEditor { editor ->
                val now = minuteOfNow()
                editor.copy(blocks = editor.blocks.filter { it.isClosed(now) }, error = null)
            }
            is TasksUiIntent.ChangeStatus -> updateEditor { it.copy(status = intent.status) }
            is TasksUiIntent.ChangeImportance -> updateEditor { it.copy(importance = intent.importance) }
            is TasksUiIntent.ChangeProject -> updateEditor { it.copy(project = intent.value) }
            is TasksUiIntent.ChangeTagDraft -> updateEditor { it.copy(tagDraft = intent.value) }
            is TasksUiIntent.AddTag -> updateEditor { editor ->
                val draft = editor.tagDraft.trim()
                val clearingDraft = intent.value.trim() == draft && draft.isNotEmpty()
                editor.copy(
                    tags = TaskTags.normalize(editor.tags + intent.value),
                    tagDraft = if (clearingDraft) "" else editor.tagDraft,
                )
            }
            is TasksUiIntent.RemoveTag -> updateEditor { editor ->
                editor.copy(
                    tags = editor.tags.filterNot { it.equals(intent.tag, ignoreCase = true) },
                )
            }
            is TasksUiIntent.FilterByProject -> _state.update {
                it.copy(selectedProject = intent.project)
            }
            is TasksUiIntent.FilterByTag -> _state.update {
                it.copy(selectedTag = intent.tag)
            }
            is TasksUiIntent.ChangeProjectSearch -> _state.update {
                it.copy(projectSearch = intent.query)
            }
            TasksUiIntent.ClearProjectSearch -> _state.update { it.copy(projectSearch = "") }
            is TasksUiIntent.ChangeScheduled -> updateEditor { editor ->
                if (intent.scheduled) {
                    editor.copy(scheduledDate = _state.value.date)
                } else {
                    editor.copy(
                        scheduledDate = null,
                        blocks = emptyList(),
                        recurrence = com.example.clockplannerproject.kit.core.RecurrenceRule.None,
                    )
                }
            }
            is TasksUiIntent.MoveToDiagram -> moveToDiagram(intent.task)
            is TasksUiIntent.OpenStartPicker -> _state.update {
                it.copy(timePicker = TimePickerTarget(intent.index, isStart = true), nowMinute = minuteOfNow())
            }
            is TasksUiIntent.OpenEndPicker -> _state.update {
                it.copy(timePicker = TimePickerTarget(intent.index, isStart = false), nowMinute = minuteOfNow())
            }
            TasksUiIntent.DismissTimePicker -> _state.update { it.copy(timePicker = null) }
            is TasksUiIntent.Duplicate -> duplicate(intent.task)
            is TasksUiIntent.RequestMove -> _state.update { it.copy(pendingMove = intent.task) }
            TasksUiIntent.DismissMove -> _state.update { it.copy(pendingMove = null) }
            is TasksUiIntent.ConfirmMove -> confirmMove(intent.date)
            is TasksUiIntent.ChangeRecurrence -> updateEditor { it.copy(recurrence = intent.rule) }
            is TasksUiIntent.ChangeReportDraft -> _state.update { it.copy(reportDraft = intent.text) }
            is TasksUiIntent.EditReport -> _state.update {
                it.copy(editingReportId = intent.report.id, reportDraft = intent.report.text)
            }
            TasksUiIntent.SaveReport -> saveReport()
            is TasksUiIntent.DeleteReport -> viewModelScope.launch {
                reportRepository.delete(intent.id)
            }
            is TasksUiIntent.ImportReportAttachment -> importAttachment(intent.id, intent.sourceUri)
            is TasksUiIntent.RemoveReportAttachment -> removeAttachment(intent.id)
            TasksUiIntent.OpenPlan -> openPlan()
            TasksUiIntent.DismissPlan -> dismissPlan()
            is TasksUiIntent.ChangePlanGoal -> changePlanGoal(intent.goal)
            TasksUiIntent.OpenPlanDeadlinePicker ->
                _state.update { it.copy(planDeadlinePicker = true) }
            TasksUiIntent.DismissPlanDeadlinePicker ->
                _state.update { it.copy(planDeadlinePicker = false) }
            is TasksUiIntent.SelectPlanDeadline -> selectPlanDeadline(intent.date)
            is TasksUiIntent.ShiftPlanWindow -> shiftPlanWindow(intent.deltaMinutes)
            is TasksUiIntent.NudgePlanWindowEdge -> nudgePlanWindowEdge(intent.edge, intent.deltaMinutes)
            is TasksUiIntent.SetPlanWindow -> setPlanWindow(intent.startMinute, intent.endMinute)
            TasksUiIntent.OpenPlanWindowStart ->
                _state.update { it.copy(planWindowEdge = PlanWindowEdge.START) }
            TasksUiIntent.OpenPlanWindowEnd ->
                _state.update { it.copy(planWindowEdge = PlanWindowEdge.END) }
            TasksUiIntent.DismissPlanWindowTime ->
                _state.update { it.copy(planWindowEdge = null) }
            TasksUiIntent.SubmitPlan -> planFields()?.let { requestPlan(it) }
            is TasksUiIntent.RemovePlanStep -> removePlanStep(intent.index)
            is TasksUiIntent.OpenPlanStepDate ->
                _state.update { it.copy(planStepDateIndex = intent.index) }
            TasksUiIntent.DismissPlanStepDate ->
                _state.update { it.copy(planStepDateIndex = null) }
            is TasksUiIntent.MovePlanStep -> movePlanStep(intent.index, intent.date)
            TasksUiIntent.AcceptPlan -> acceptPlan()
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
                effects.send(TasksUiEffect.ShowMessageRes(R.string.report_attach_failed))
            }
        }
    }

    private fun removeAttachment(id: ReportId) {
        val report = _state.value.reports.find { it.id == id } ?: return
        viewModelScope.launch {
            reportRepository.upsert(report.copy(attachment = null))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeEditorReports() {
        viewModelScope.launch {
            editorTaskId.filterNotNull()
                .flatMapLatest { id -> reportRepository.observeReports(id) }
                .collect { reports ->
                    _state.update { it.copy(reports = reports) }
                }
        }
    }

    private fun saveReport() {
        val editor = _state.value.editor ?: return
        val taskId = editor.id ?: return
        val text = _state.value.reportDraft.trim()
        if (text.isEmpty()) return
        val existingId = _state.value.editingReportId
        val existing = _state.value.reports.find { it.id == existingId }
        val created = existing?.createdAtEpochMillis ?: System.currentTimeMillis()
        val report = TaskReport(
            id = existingId ?: ReportId(UUID.randomUUID().toString()),
            taskId = taskId,
            createdAtEpochMillis = created,
            text = text,
            attachment = existing?.attachment,
        )
        viewModelScope.launch {
            try {
                reportRepository.upsert(report)
                _state.update { it.copy(reportDraft = "", editingReportId = null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                effects.send(
                    TasksUiEffect.ShowMessageRes(R.string.error_note_save),
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSelectedDate() {
        viewModelScope.launch {
            try {
                selectedDate.flatMapLatest { date ->
                    materializer.ensureVisibleDay(date)
                    taskRepository.observeAll().map { tasks -> date to tasks }
                }
                    .collect { (date, tasks) ->
                        _state.update { current ->
                            current.copy(
                                date = date,
                                tasks = tasks,
                                isLoading = false,
                                errorMessageRes = null,
                                nowMinute = minuteOfNow(),
                            )
                        }
                    }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessageRes = R.string.error_task_load,
                    )
                }
                effects.send(
                    TasksUiEffect.ShowMessageRes(R.string.error_task_load),
                )
            }
        }
    }

    private fun openCreate(scheduled: Boolean = false) {
        editorTaskId.value = null
        _state.update {
            it.copy(
                editor = TaskEditorState(scheduledDate = if (scheduled) it.date else null),
                timePicker = null,
                nowMinute = minuteOfNow(),
            )
        }
    }

    private fun save() {
        val snapshot = _state.value
        val editor = snapshot.editor ?: return
        val error = TaskDraftValidator.validate(title = editor.title, blocks = editor.blocks)
        if (error != null) {
            _state.update { it.copy(editor = editor.copy(error = error)) }
            return
        }
        viewModelScope.launch {
            try {
                val saved = editor.toTask()
                taskRepository.upsert(saved)
                if (saved.isUntimed) {
                    practice.markTried(PracticeSkill.LEFTOVER)
                }
                _state.update { it.copy(editor = null, timePicker = null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                effects.send(
                    TasksUiEffect.ShowMessageRes(R.string.error_task_save),
                )
            }
        }
    }

    private fun confirmDelete() {
        val task = _state.value.pendingDelete ?: return
        viewModelScope.launch {
            try {
                taskRepository.delete(task.id)
                _state.update { current ->
                    current.copy(
                        pendingDelete = null,
                        editor = current.editor?.takeUnless { it.id == task.id },
                        timePicker = if (current.editor?.id == task.id) null else current.timePicker,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                effects.send(
                    TasksUiEffect.ShowMessageRes(R.string.error_task_delete),
                )
            }
        }
    }

    private fun shiftDate(days: Int) {
        selectedDate.update { it.plus(days, DateTimeUnit.DAY) }
        _state.update {
            it.copy(
                isLoading = true,
                editor = null,
                pendingDelete = null,
                pendingMove = null,
                timePicker = null,
                showDatePicker = false,
                selectedProject = null,
            )
        }
    }

    private fun selectDate(date: LocalDate) {
        selectedDate.value = date
        _state.update {
            it.copy(
                isLoading = true,
                editor = null,
                pendingDelete = null,
                pendingMove = null,
                timePicker = null,
                showDatePicker = false,
                selectedProject = null,
            )
        }
    }

    private fun duplicate(task: Task) {
        val copy = TaskInstances.duplicated(
            task,
            TaskId(UUID.randomUUID().toString()),
            task.date,
        )
        viewModelScope.launch {
            try {
                taskRepository.upsert(copy)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                effects.send(
                    TasksUiEffect.ShowMessageRes(R.string.error_task_duplicate),
                )
            }
        }
    }

    private fun confirmMove(date: LocalDate) {
        val task = _state.value.pendingMove ?: return
        viewModelScope.launch {
            try {
                taskRepository.upsert(TaskInstances.moved(task, date))
                _state.update { it.copy(pendingMove = null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                effects.send(
                    TasksUiEffect.ShowMessageRes(R.string.error_task_move),
                )
            }
        }
    }

    private fun addBlock() {
        val now = minuteOfNow()
        updateEditor { editor ->
            val start = now.mod(TimeMath.MINUTES_PER_DAY)
            val end = (start + 60).mod(TimeMath.MINUTES_PER_DAY)
            editor.copy(
                blocks = editor.blocks + TimeBlock(start, end),
                error = null,
            )
        }
    }

    private fun removeBlock(index: Int) {
        val now = minuteOfNow()
        updateEditor { editor ->
            val block = editor.blocks.getOrNull(index) ?: return@updateEditor editor
            if (block.isClosed(now)) return@updateEditor editor
            editor.copy(blocks = editor.blocks.filterIndexed { i, _ -> i != index }, error = null)
        }
    }

    private fun updateBlock(index: Int, transform: (TimeBlock) -> TimeBlock) {
        val now = minuteOfNow()
        updateEditor { editor ->
            val block = editor.blocks.getOrNull(index) ?: return@updateEditor editor
            if (block.isClosed(now)) return@updateEditor editor
            editor.copy(
                blocks = editor.blocks.mapIndexed { i, current ->
                    if (i == index) transform(current) else current
                },
                error = null,
            )
        }
    }

    private fun moveToDiagram(task: Task) {
        val date = _state.value.date ?: return
        viewModelScope.launch {
            try {
                taskRepository.upsert(
                    task.copy(
                        date = date,
                        blocks = emptyList(),
                        recurrence = com.example.clockplannerproject.kit.core.RecurrenceRule.None,
                    ),
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                effects.send(
                    TasksUiEffect.ShowMessageRes(R.string.error_task_diagram),
                )
            }
        }
    }

    private fun updateEditor(transform: (TaskEditorState) -> TaskEditorState) {
        _state.update { state ->
            val editor = state.editor ?: return@update state
            state.copy(editor = transform(editor), nowMinute = minuteOfNow())
        }
    }

    private fun minuteOfNow(): Int {
        val now = timeProvider.now()
        return now.hour * 60 + now.minute
    }

    private fun openPlan() {
        planGeneration++
        editorTaskId.value = null
        _state.update {
            it.copy(
                editor = null,
                timePicker = null,
                reports = emptyList(),
                reportDraft = "",
                editingReportId = null,
                planSheet = PlanSheetUi.Form(PlanFormState(deadline = timeProvider.today())),
                planDeadlinePicker = false,
                planStepDateIndex = null,
                planWindowEdge = null,
                nowMinute = minuteOfNow(),
            )
        }
    }

    private fun dismissPlan() {
        val accepting = (_state.value.planSheet as? PlanSheetUi.Draft)?.draft?.accepting == true
        if (accepting) return
        planGeneration++
        _state.update {
            it.copy(
                planSheet = null,
                planDeadlinePicker = false,
                planStepDateIndex = null,
                planWindowEdge = null,
            )
        }
    }

    private fun changePlanGoal(goal: String) {
        _state.update { state ->
            val form = (state.planSheet as? PlanSheetUi.Form)?.form ?: return@update state
            state.copy(planSheet = PlanSheetUi.Form(form.copy(goal = goal, message = null)))
        }
    }

    private fun selectPlanDeadline(date: LocalDate) {
        _state.update { it.copy(planDeadlinePicker = false) }
        val fields = planFields() ?: return
        applyPlanFields(fields.copy(deadline = date), replanIfDraft = true)
    }

    private fun shiftPlanWindow(deltaMinutes: Int) {
        val fields = planFields() ?: return
        val length = fields.workEndMinute - fields.workStartMinute
        if (length <= 0) {
            applyPlanFields(fields, replanIfDraft = true)
            return
        }
        val start = (fields.workStartMinute + deltaMinutes)
            .coerceIn(0, TimeMath.MINUTES_PER_DAY - length)
        applyPlanFields(fields.copy(workStartMinute = start, workEndMinute = start + length), replanIfDraft = true)
    }

    private fun nudgePlanWindowEdge(edge: PlanWindowEdge, deltaMinutes: Int) {
        val fields = planFields() ?: return
        val start = if (edge == PlanWindowEdge.START) {
            (fields.workStartMinute + deltaMinutes).coerceIn(0, TimeMath.MINUTES_PER_DAY)
        } else {
            fields.workStartMinute
        }
        val end = if (edge == PlanWindowEdge.END) {
            (fields.workEndMinute + deltaMinutes).coerceIn(0, TimeMath.MINUTES_PER_DAY)
        } else {
            fields.workEndMinute
        }
        applyPlanFields(fields.copy(workStartMinute = start, workEndMinute = end), replanIfDraft = true)
    }

    private fun setPlanWindow(startMinute: Int, endMinute: Int) {
        _state.update { it.copy(planWindowEdge = null) }
        val fields = planFields() ?: return
        val start = startMinute.mod(TimeMath.MINUTES_PER_DAY)
        val end = endMinute.mod(TimeMath.MINUTES_PER_DAY)
        applyPlanFields(fields.copy(workStartMinute = start, workEndMinute = end), replanIfDraft = true)
    }

    private fun applyPlanFields(fields: PlanFields, replanIfDraft: Boolean) {
        val unchanged = planFields()?.sameWindowAndDeadline(fields) == true &&
            fields.goal == planFields()?.goal
        if (unchanged) return
        val onDraft = _state.value.planSheet is PlanSheetUi.Draft
        if (onDraft && replanIfDraft) {
            requestPlan(fields)
        } else {
            showForm(fields, message = null, submitting = false)
        }
    }

    private fun requestPlan(fields: PlanFields) {
        if ((_state.value.planSheet as? PlanSheetUi.Draft)?.draft?.accepting == true) return
        val message = rejectionMessage(fields)
        if (message != null) {
            planGeneration++
            showForm(fields, message = message, submitting = false)
            return
        }
        val generation = ++planGeneration
        showSubmitting(fields)
        viewModelScope.launch {
            val outcome = try {
                plannerSource.plan(
                    PlanRequest(
                        goal = fields.goal,
                        deadline = fields.deadline,
                        zone = TimeZone.currentSystemDefault(),
                        workStartMinute = fields.workStartMinute,
                        workEndMinute = fields.workEndMinute,
                        tasks = plannerSlice(fields.deadline),
                    ),
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                PlanOutcome.Rejected(
                    com.example.clockplannerproject.kit.core.planner.PlanRejection.Unavailable(
                        phrases.serviceUnavailable(),
                    ),
                )
            }
            if (generation != planGeneration) return@launch
            when (outcome) {
                is PlanOutcome.Ready -> showDraft(fields, outcome.draft)
                is PlanOutcome.Rejected -> showForm(fields, outcome.reason.message, submitting = false)
            }
        }
    }

    private fun removePlanStep(index: Int) {
        val sheet = _state.value.planSheet as? PlanSheetUi.Draft ?: return
        if (!sheet.draft.canPlace) return
        val updated = PlanDraftEdit.withoutStep(
            draft = sheet.draft.draft,
            index = index,
            context = placementContext(sheet.draft),
            phrases = phrases,
        )
        _state.update {
            it.copy(
                planSheet = PlanSheetUi.Draft(sheet.draft.copy(draft = updated, message = null)),
            )
        }
    }

    private fun movePlanStep(index: Int, date: LocalDate) {
        _state.update { it.copy(planStepDateIndex = null) }
        val sheet = _state.value.planSheet as? PlanSheetUi.Draft ?: return
        if (!sheet.draft.canPlace) return
        val today = timeProvider.today()
        if (date < today || date > sheet.draft.deadline) {
            _state.update {
                it.copy(
                    planSheet = PlanSheetUi.Draft(
                        sheet.draft.copy(message = phrases.stepDayOutsideDeadline()),
                    ),
                )
            }
            return
        }
        val updated = PlanDraftEdit.moveStep(
            draft = sheet.draft.draft,
            index = index,
            date = date,
            context = placementContext(sheet.draft),
            phrases = phrases,
        )
        _state.update {
            it.copy(planSheet = PlanSheetUi.Draft(sheet.draft.copy(draft = updated, message = null)))
        }
    }

    private fun acceptPlan() {
        val sheet = _state.value.planSheet as? PlanSheetUi.Draft ?: return
        if (!sheet.draft.canPlace) return
        val placed = sheet.draft.draft.steps.filter { step ->
            !step.unplaced && step.date != null && step.startMinute != null && step.endMinute != null
        }
        if (placed.isEmpty()) return
        val generation = ++planGeneration
        _state.update {
            it.copy(planSheet = PlanSheetUi.Draft(sheet.draft.copy(accepting = true)))
        }
        viewModelScope.launch {
            try {
                val existingCount = _state.value.tasks.size
                placed.forEachIndexed { index, step ->
                    taskRepository.upsert(step.toTask(existingCount + index))
                }
                planStore.saveAcceptedPlan(
                    AcceptedPlan(
                        id = sheet.draft.draft.draftId,
                        goal = sheet.draft.goal.trim(),
                        deadline = sheet.draft.deadline,
                        steps = placed,
                    ),
                )
                if (generation != planGeneration) return@launch
                val left = sheet.draft.draft.steps.filter { it.unplaced }
                _state.update { current ->
                    if (left.isEmpty()) {
                        current.copy(
                            planSheet = null,
                            planDeadlinePicker = false,
                            planStepDateIndex = null,
                            planWindowEdge = null,
                        )
                    } else {
                        val required = left.sumOf { it.bufferedMinutes }
                        current.copy(
                            planSheet = PlanSheetUi.Draft(
                                sheet.draft.copy(
                                    draft = sheet.draft.draft.copy(
                                        verdict = PlanVerdict.IMPOSSIBLE,
                                        requiredMinutes = required,
                                        steps = left,
                                        narrative = phrases.impossible(
                                            (required - sheet.draft.draft.freeMinutes).coerceAtLeast(0),
                                        ),
                                    ),
                                    accepting = false,
                                    submitting = false,
                                    message = null,
                                ),
                            ),
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (generation == planGeneration) {
                    _state.update { current ->
                        val open = current.planSheet as? PlanSheetUi.Draft ?: return@update current
                        current.copy(planSheet = PlanSheetUi.Draft(open.draft.copy(accepting = false)))
                    }
                }
                effects.send(
                    TasksUiEffect.ShowMessageRes(R.string.error_plan_place),
                )
            }
        }
    }

    private fun rejectionMessage(fields: PlanFields): String? {
        if (fields.goal.trim().isEmpty()) return phrases.emptyGoal()
        if (fields.deadline < timeProvider.today()) return phrases.deadlinePassed()
        if (invalidWindow(fields.workStartMinute, fields.workEndMinute)) return phrases.invalidWindow()
        return null
    }

    private fun showForm(fields: PlanFields, message: String?, submitting: Boolean) {
        _state.update {
            it.copy(
                planSheet = PlanSheetUi.Form(
                    PlanFormState(
                        goal = fields.goal,
                        deadline = fields.deadline,
                        workStartMinute = fields.workStartMinute,
                        workEndMinute = fields.workEndMinute,
                        message = message,
                        submitting = submitting,
                    ),
                ),
                planStepDateIndex = null,
            )
        }
    }

    private fun showSubmitting(fields: PlanFields) {
        _state.update { state ->
            when (val sheet = state.planSheet) {
                is PlanSheetUi.Draft -> state.copy(
                    planSheet = PlanSheetUi.Draft(
                        sheet.draft.copy(
                            goal = fields.goal,
                            deadline = fields.deadline,
                            workStartMinute = fields.workStartMinute,
                            workEndMinute = fields.workEndMinute,
                            submitting = true,
                            message = null,
                        ),
                    ),
                )
                else -> state.copy(
                    planSheet = PlanSheetUi.Form(
                        PlanFormState(
                            goal = fields.goal,
                            deadline = fields.deadline,
                            workStartMinute = fields.workStartMinute,
                            workEndMinute = fields.workEndMinute,
                            message = null,
                            submitting = true,
                        ),
                    ),
                )
            }
        }
    }

    private fun showDraft(fields: PlanFields, draft: PlanDraft) {
        _state.update {
            it.copy(
                planSheet = PlanSheetUi.Draft(
                    PlanDraftUi(
                        goal = fields.goal.trim(),
                        deadline = fields.deadline,
                        workStartMinute = fields.workStartMinute,
                        workEndMinute = fields.workEndMinute,
                        draft = draft,
                    ),
                ),
            )
        }
    }

    private fun planFields(): PlanFields? = when (val sheet = _state.value.planSheet) {
        is PlanSheetUi.Form -> PlanFields(
            goal = sheet.form.goal,
            deadline = sheet.form.deadline,
            workStartMinute = sheet.form.workStartMinute,
            workEndMinute = sheet.form.workEndMinute,
        )
        is PlanSheetUi.Draft -> PlanFields(
            goal = sheet.draft.goal,
            deadline = sheet.draft.deadline,
            workStartMinute = sheet.draft.workStartMinute,
            workEndMinute = sheet.draft.workEndMinute,
        )
        null -> null
    }

    private fun placementContext(draft: PlanDraftUi): PlacementContext = PlacementContext(
        today = timeProvider.today(),
        deadline = draft.deadline,
        nowMinute = minuteOfNow(),
        workStartMinute = draft.workStartMinute,
        workEndMinute = draft.workEndMinute,
        tasks = plannerSlice(draft.deadline),
    )

    private fun plannerSlice(deadline: LocalDate): List<Task> {
        val today = timeProvider.today()
        return _state.value.tasks.filter { task ->
            task.recurrence !is RecurrenceRule.None ||
                (task.date != null && task.date >= today && task.date <= deadline)
        }
    }

    private fun PlacedStep.toTask(colorIndex: Int): Task {
        val day = checkNotNull(date)
        val start = checkNotNull(startMinute)
        val end = checkNotNull(endMinute)
        return Task(
            id = TaskId(UUID.randomUUID().toString()),
            title = title,
            colorArgb = TaskPalette.colorAt(colorIndex),
            blocks = listOf(TimeBlock(start, end)),
            status = TaskStatus.TODO,
            date = day,
            importance = importance,
            project = project,
        )
    }
}

private data class PlanFields(
    val goal: String,
    val deadline: LocalDate,
    val workStartMinute: Int,
    val workEndMinute: Int,
) {
    fun sameWindowAndDeadline(other: PlanFields): Boolean =
        deadline == other.deadline &&
            workStartMinute == other.workStartMinute &&
            workEndMinute == other.workEndMinute
}

private fun invalidWindow(start: Int, end: Int): Boolean =
    start < 0 || end > TimeMath.MINUTES_PER_DAY || start >= end

private object IdlePlannerSource : PlannerSource {
    override suspend fun plan(request: PlanRequest): PlanOutcome =
        PlanOutcome.Rejected(
            com.example.clockplannerproject.kit.core.planner.PlanRejection.Unavailable(
                RussianPlannerPhrases.serviceUnavailable(),
            ),
        )

    override suspend fun report(
        request: com.example.clockplannerproject.kit.core.planner.ReportRequest,
    ): ReportOutcome = ReportOutcome.Ready(ReportResult("", 0, 0))
}

private object IdlePlanStore : PlannerPlanStore {
    override suspend fun deviceKey(): String = "idle"

    override fun observeAcceptedPlans(): kotlinx.coroutines.flow.Flow<List<AcceptedPlan>> =
        kotlinx.coroutines.flow.flowOf(emptyList())

    override suspend fun acceptedPlans(): List<AcceptedPlan> = emptyList()

    override suspend fun saveAcceptedPlan(plan: AcceptedPlan) = Unit
}
