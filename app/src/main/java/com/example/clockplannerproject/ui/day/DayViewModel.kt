package com.example.clockplannerproject.ui.day

import com.example.clockplannerproject.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clockplannerproject.data.InMemoryAppearanceRepository
import com.example.clockplannerproject.data.InMemoryFlowMemoryStore
import com.example.clockplannerproject.data.InMemoryPracticeProgressStore
import com.example.clockplannerproject.data.InMemoryCourseHistoryStore
import com.example.clockplannerproject.data.InMemoryGoalBookStore
import com.example.clockplannerproject.data.InMemoryReportRepository
import com.example.clockplannerproject.data.SampleDaySeeder
import com.example.clockplannerproject.kit.core.Appearance
import com.example.clockplannerproject.kit.core.AppearanceRepository
import com.example.clockplannerproject.kit.core.BreakSession
import com.example.clockplannerproject.kit.core.FlowMemorySnapshot
import com.example.clockplannerproject.kit.core.FlowMemoryStore
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.RecurrenceMaterializer
import com.example.clockplannerproject.kit.core.BuiltInCourse
import com.example.clockplannerproject.kit.core.BuiltInPractice
import com.example.clockplannerproject.kit.core.CourseHistoryStore
import com.example.clockplannerproject.kit.core.DemandMark
import com.example.clockplannerproject.kit.core.DifficultyNudge
import com.example.clockplannerproject.kit.core.GoalBook
import com.example.clockplannerproject.kit.core.GoalBookStore
import com.example.clockplannerproject.kit.core.HardenKind
import com.example.clockplannerproject.kit.core.PracticePass
import com.example.clockplannerproject.kit.core.ReportRepository
import com.example.clockplannerproject.kit.core.ShortFlowScale
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskCompletion
import com.example.clockplannerproject.kit.core.TaskCompletionCodec
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskReport
import com.example.clockplannerproject.kit.core.TaskInstances
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.placeOnDate
import com.example.clockplannerproject.kit.core.showingTemplateFaces
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TaskTimer
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.time.SessionDuration
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.kit.core.PauseMood
import com.example.clockplannerproject.kit.core.practice.PracticeProgressStore
import com.example.clockplannerproject.kit.core.practice.PracticeSkill
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Day screen ViewModel. Depends on repository, clock, and debug seeder — no Android Context.
 *
 * While the visible date is still local today, a later [TimeProvider.today] replaces it.
 * A date chosen with the arrows or the calendar stays until the user returns to today.
 *
 * @since 0.1.0
 */
class DayViewModel(
    private val taskRepository: TaskRepository,
    private val timeProvider: TimeProvider,
    private val sampleDaySeeder: SampleDaySeeder,
    private val appearanceRepository: AppearanceRepository = InMemoryAppearanceRepository(),
    private val flowMemory: FlowMemoryStore = InMemoryFlowMemoryStore(),
    private val practice: PracticeProgressStore = InMemoryPracticeProgressStore(),
    private val reports: ReportRepository = InMemoryReportRepository(),
    private val goals: GoalBookStore = InMemoryGoalBookStore(),
    private val courseHistory: CourseHistoryStore = InMemoryCourseHistoryStore(),
) : ViewModel() {

    private val _state = MutableStateFlow(DayUiState())
    val state: StateFlow<DayUiState> = _state.asStateFlow()

    private val effects = Channel<DayUiEffect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    private val retryTick = MutableStateFlow(0)
    private val selectedDate = MutableStateFlow(timeProvider.today())
    private var followsToday = true
    private var halfSeeded = false
    private val materializer = RecurrenceMaterializer(taskRepository)
    private var latestAppearance = Appearance.Default
    private var appearanceReady = false
    private val budgets = mutableMapOf<TaskId, Int>()
    private val chimed = mutableSetOf<TaskId>()

    init {
        viewModelScope.launch {
            appearanceRepository.observeAppearance().collect { look ->
                latestAppearance = look
                appearanceReady = true
                _state.update {
                    it.copy(
                        viewMode = look.viewMode,
                        accentArgb = look.accentArgb,
                        completedArgb = look.completedArgb,
                        background = look.background,
                    )
                }
            }
        }
        observeDay()
        watchBudgets()
    }

    fun onIntent(intent: DayUiIntent) {
        when (intent) {
            DayUiIntent.Retry -> retryTick.update { it + 1 }
            is DayUiIntent.SelectTask -> {
                val onDial = _state.value.viewMode == ViewMode.DIAL
                _state.update {
                    if (it.selectedTask?.id == intent.task.id) {
                        it.copy(selectedTask = null, detailsOpen = false)
                    } else {
                        it.copy(selectedTask = intent.task, detailsOpen = false)
                    }
                }
                refreshFlowChrome()
                if (onDial) markTried(PracticeSkill.DIAL)
            }
            is DayUiIntent.OpenTaskDetails -> {
                _state.update {
                    val task = intent.task ?: it.selectedTask
                    when {
                        task == null -> it
                        it.selectedTask?.id == task.id && it.detailsOpen -> {
                            it.copy(selectedTask = null, detailsOpen = false)
                        }
                        else -> it.copy(selectedTask = task, detailsOpen = true)
                    }
                }
                refreshFlowChrome()
            }
            DayUiIntent.DismissDetails -> _state.update { it.copy(detailsOpen = false) }
            DayUiIntent.DismissTask -> {
                _state.update {
                    it.copy(selectedTask = null, detailsOpen = false, showMovePicker = false)
                }
                refreshFlowChrome()
            }
            DayUiIntent.ToggleComplete -> toggleComplete()
            DayUiIntent.EditTask -> openEditor()
            DayUiIntent.RequestDelete -> _state.update { it.copy(pendingDelete = it.selectedTask) }
            DayUiIntent.ConfirmDelete -> confirmDelete()
            DayUiIntent.DismissDelete -> _state.update { it.copy(pendingDelete = null) }
            is DayUiIntent.RotateBy -> _state.update {
                it.copy(
                    userRotationOffsetDeg = TimeMath.normalizeDegrees(
                        it.userRotationOffsetDeg + intent.deltaDeg,
                    ),
                )
            }
            DayUiIntent.ResetRotation -> _state.update { it.copy(userRotationOffsetDeg = 0f) }
            is DayUiIntent.ResizePreview -> {
                val task = _state.value.tasks.find { it.id == intent.taskId }
                val nowMinute = _state.value.now?.let { TimeMath.minuteOfDay(it).toInt() } ?: 0
                val closed = task?.blocks?.find { it.startMinute == intent.blockStartMinute }
                    ?.isClosed(nowMinute) == true
                if (!closed) {
                    _state.update {
                        it.copy(
                            resizingTaskId = intent.taskId,
                            resizePreviewEndMinute = intent.endMinute,
                            resizeBlockStartMinute = intent.blockStartMinute,
                        )
                    }
                }
            }
            is DayUiIntent.ResizeTask -> resizeTask(intent)
            DayUiIntent.CancelResize -> clearResizePreview()
            DayUiIntent.ToggleHideCompleted -> _state.update {
                it.copy(hideCompleted = !it.hideCompleted)
            }
            is DayUiIntent.SetViewMode -> applyLook(latestAppearance.copy(viewMode = intent.mode))
            is DayUiIntent.SetLook -> applyLook(
                latestAppearance.copy(
                    viewMode = intent.appearance.viewMode,
                    accentArgb = intent.appearance.accentArgb,
                    completedArgb = intent.appearance.completedArgb,
                    background = intent.appearance.background,
                ),
            )
            DayUiIntent.DuplicateTask -> duplicateSelected()
            DayUiIntent.RequestMove -> _state.update { it.copy(showMovePicker = it.selectedTask != null) }
            DayUiIntent.DismissMove -> _state.update { it.copy(showMovePicker = false) }
            is DayUiIntent.ConfirmMove -> moveSelected(intent.date)
            DayUiIntent.StartTimer -> startTimer(durationMinutes = null)
            is DayUiIntent.StartFor -> startTimer(durationMinutes = intent.minutes)
            DayUiIntent.PauseTimer -> pauseTimer()
            DayUiIntent.ContinueBreak -> continueBreak()
            DayUiIntent.FinishBreak -> finishBreak()
            is DayUiIntent.EditCompletion -> _state.update { it.copy(completion = intent.draft) }
            DayUiIntent.SubmitCompletion -> submitCompletion()
            DayUiIntent.OpenCompletion -> openCompletion(null)
            is DayUiIntent.OpenIntervalCompletion -> openCompletion(intent.blockIndex)
            DayUiIntent.DismissCompletion -> _state.update { it.copy(completion = CompletionDraft()) }
            DayUiIntent.SkipNudge -> skipNudge()
            is DayUiIntent.AcceptNudge -> acceptNudge(intent.kind)
            is DayUiIntent.FinishCourse -> finishCourse(intent.practice, intent.checked)
            DayUiIntent.PreviousDay -> shiftDate(-1)
            DayUiIntent.NextDay -> shiftDate(1)
            DayUiIntent.OpenDatePicker -> _state.update { it.copy(showDatePicker = true) }
            DayUiIntent.DismissDatePicker -> _state.update { it.copy(showDatePicker = false) }
            is DayUiIntent.SelectDate -> {
                selectVisibleDate(intent.date)
                _state.update { it.copy(showDatePicker = false) }
            }
            is DayUiIntent.SetDialHalf -> _state.update { it.copy(dialHalf = intent.half) }
            is DayUiIntent.ChangePauseDraft -> _state.update { it.copy(pauseDraft = intent.text) }
            is DayUiIntent.SelectPauseMood -> _state.update { it.copy(pauseMood = intent.mood) }
            DayUiIntent.CommitPause -> commitPause()
            DayUiIntent.SkipPause -> {
                _state.update { it.copy(pauseSlipOpen = false) }
                refreshFlowChrome()
            }
            DayUiIntent.ShowDayClose -> showDayClose()
            DayUiIntent.DismissDayClose -> dismissDayClose()
        }
    }

    private fun shiftDate(days: Int) {
        selectVisibleDate(selectedDate.value.plus(days, DateTimeUnit.DAY))
    }

    /**
     * Shows [date]. Follows the clock again only when [date] is local today
     * at the moment of the choice.
     */
    private fun selectVisibleDate(date: LocalDate) {
        clearDayFlow()
        followsToday = date == timeProvider.today()
        selectedDate.value = date
    }

    private fun clearDayFlow() {
        _state.update {
            it.copy(
                pauseSlipOpen = false,
                pauseDraft = "",
                pauseMood = PauseMood.Calm,
            )
        }
    }

    /**
     * Shows [next] at once and, once the stored look has loaded, writes it
     * to the same repository Settings uses.
     */
    private fun applyLook(next: Appearance) {
        _state.update {
            it.copy(
                viewMode = next.viewMode,
                accentArgb = next.accentArgb,
                completedArgb = next.completedArgb,
                background = next.background,
            )
        }
        if (appearanceReady) persistAppearance(next)
    }

    private fun persistAppearance(next: Appearance) {
        latestAppearance = next
        viewModelScope.launch {
            appearanceRepository.save(next)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDay() {
        viewModelScope.launch {
            retryTick.collectLatest {
                _state.update { it.copy(isLoading = true, errorMessageRes = null) }
                try {
                    try {
                        sampleDaySeeder.seedIfEmpty()
                    } catch (_: Exception) {
                        effects.send(DayUiEffect.ShowMessageRes(R.string.error_day_sample))
                    }
                    var lastMaterialized: LocalDate? = null
                    combine(
                        timeProvider.observeTime(),
                        selectedDate.filterNotNull(),
                        flowMemory.observe(),
                    ) { time, date, memory ->
                        val today = timeProvider.today()
                        if (followsToday && date != today) {
                            val half = if (DialHalf.PM.contains(TimeMath.minuteOfDay(time))) {
                                DialHalf.PM
                            } else {
                                DialHalf.AM
                            }
                            halfSeeded = true
                            clearDayFlow()
                            _state.update { it.copy(dialHalf = half) }
                            followsToday = true
                            selectedDate.value = today
                            DayTick(today, time, memory)
                        } else {
                            DayTick(date, time, memory)
                        }
                    }
                        .distinctUntilChanged()
                        .flatMapLatest { tick ->
                            val date = tick.date
                            val time = tick.time
                            val memory = tick.memory
                            if (lastMaterialized != date) {
                                materializer.ensureVisibleDay(date)
                                lastMaterialized = date
                            }
                            taskRepository.observeAll().map { all ->
                                val tasks = all
                                    .filter { it.date == date }
                                    .showingTemplateFaces(all)
                                val current = _state.value
                                val selected = current.selectedTask?.id?.let { id ->
                                    tasks.find { it.id == id }
                                }
                                val pending = current.pendingDelete?.id?.let { id ->
                                    tasks.find { it.id == id }
                                }
                                val checkpoint = selected?.let { memory.checkpoint(it.id) }
                                val showChip = checkpoint != null &&
                                    !current.pauseSlipOpen &&
                                    !TaskTimer.hasOpenBlock(selected)
                                val half = if (!halfSeeded) {
                                    halfSeeded = true
                                    if (DialHalf.PM.contains(TimeMath.minuteOfDay(time))) {
                                        DialHalf.PM
                                    } else {
                                        DialHalf.AM
                                    }
                                } else {
                                    current.dialHalf
                                }
                                DayUiState(
                                    date = date,
                                    localToday = timeProvider.today(),
                                    now = time,
                                    tasks = tasks,
                                    selectedTask = selected,
                                    detailsOpen = current.detailsOpen && selected != null,
                                    pendingDelete = pending,
                                    isLoading = false,
                                    errorMessageRes = null,
                                    userRotationOffsetDeg = current.userRotationOffsetDeg,
                                    resizingTaskId = current.resizingTaskId,
                                    resizePreviewEndMinute = current.resizePreviewEndMinute,
                                    resizeBlockStartMinute = current.resizeBlockStartMinute,
                                    hideCompleted = current.hideCompleted,
                                    viewMode = latestAppearance.viewMode,
                                    accentArgb = latestAppearance.accentArgb,
                                    completedArgb = latestAppearance.completedArgb,
                                    background = latestAppearance.background,
                                    sessionMinutesLeft = selected?.id?.let { id ->
                                        budgets[id]?.let { end ->
                                            (end - TimeMath.minuteOfDay(time).toInt()).coerceAtLeast(0)
                                        }
                                    },
                                    showMovePicker = current.showMovePicker,
                                    showDatePicker = current.showDatePicker,
                                    dialHalf = half,
                                    pauseSlipOpen = current.pauseSlipOpen,
                                    pauseDraft = current.pauseDraft,
                                    pauseMood = current.pauseMood,
                                    resumeCheckpoint = if (showChip) checkpoint?.text else null,
                                    resumeMood = if (showChip) checkpoint?.mood else null,
                                    showDayClose = memory.isDayCloseVisible(date) &&
                                        !current.pauseSlipOpen,
                                    completion = current.completion,
                                    difficultyOffer = current.difficultyOffer,
                                )
                            }
                        }
                        .collect { snapshot -> _state.value = snapshot }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessageRes = R.string.error_day_load,
                        )
                    }
                    effects.send(DayUiEffect.ShowMessageRes(R.string.error_day_load))
                }
            }
        }
    }

    private fun clearResizePreview() {
        _state.update {
            it.copy(
                resizingTaskId = null,
                resizePreviewEndMinute = null,
                resizeBlockStartMinute = null,
            )
        }
    }

    private fun resizeTask(intent: DayUiIntent.ResizeTask) {
        val snapshot = _state.value
        val task = snapshot.tasks.find { it.id == intent.taskId } ?: run {
            clearResizePreview()
            return
        }
        val nowMinute = snapshot.now?.let {
            TimeMath.minuteOfDay(it).toInt()
        } ?: 0
        val match = intent.blockStartMinute
        val target = task.blocks.find { it.startMinute == match }
            ?: task.blocks.firstOrNull()
        if (target != null && target.isClosed(nowMinute)) {
            clearResizePreview()
            return
        }
        val updated = task.withEndMinute(intent.newEndMinute, intent.blockStartMinute)
        viewModelScope.launch {
            try {
                taskRepository.upsert(updated)
                _state.update { current ->
                    current.copy(
                        resizingTaskId = null,
                        resizePreviewEndMinute = null,
                        resizeBlockStartMinute = null,
                        selectedTask = current.selectedTask?.takeIf { it.id == updated.id }
                            ?: current.selectedTask,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                clearResizePreview()
                effects.send(DayUiEffect.ShowMessageRes(R.string.error_day_resize))
            }
        }
    }

    private fun toggleComplete() {
        val task = _state.value.selectedTask ?: return
        val nextStatus = if (task.status == TaskStatus.DONE) {
            TaskStatus.TODO
        } else {
            TaskStatus.DONE
        }
        viewModelScope.launch {
            try {
                taskRepository.upsert(task.copy(status = nextStatus))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                effects.send(
                    DayUiEffect.ShowMessageRes(R.string.error_task_update),
                )
            }
        }
    }

    private fun openEditor() {
        val task = _state.value.selectedTask ?: return
        _state.update { it.copy(selectedTask = null, detailsOpen = false) }
        viewModelScope.launch {
            effects.send(DayUiEffect.OpenEditor(task))
        }
    }

    private fun duplicateSelected() {
        val task = _state.value.selectedTask ?: return
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
                    DayUiEffect.ShowMessageRes(R.string.error_task_duplicate),
                )
            }
        }
    }

    private fun moveSelected(date: LocalDate) {
        val task = _state.value.selectedTask ?: return
        viewModelScope.launch {
            try {
                placeOnDate(taskRepository, task, date) {
                    TaskId(UUID.randomUUID().toString())
                }
                _state.update {
                    it.copy(selectedTask = null, detailsOpen = false, showMovePicker = false)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                effects.send(
                    DayUiEffect.ShowMessageRes(R.string.error_task_move),
                )
            }
        }
    }

    private fun startTimer(durationMinutes: Int?) {
        val now = _state.value.now?.let { TimeMath.minuteOfDay(it).toInt() } ?: return
        val selected = _state.value.selectedTask
        val task = if (selected != null) {
            selected
        } else {
            if (_state.value.tasks.any { TaskTimer.hasOpenBlock(it) }) return
            val date = _state.value.date ?: return
            Task(
                id = TaskId(UUID.randomUUID().toString()),
                title = "",
                colorArgb = latestAppearance.accentArgb,
                date = date,
            )
        }
        if (!TaskTimer.canStart(task)) return
        closeOpenBreak(now)
        val started = TaskTimer.start(task, now, plannedMinutes = durationMinutes)
        if (durationMinutes != null) {
            val start = started.blocks.lastOrNull { it.isOpen }?.startMinute ?: now
            budgets[started.id] = SessionDuration.endMinute(start, durationMinutes)
            chimed.remove(started.id)
        }
        _state.update { it.copy(selectedTask = started, pauseSlipOpen = false) }
        viewModelScope.launch {
            if (selected != null) flowMemory.clearCheckpoint(selected.id)
            if (upsertOrReport(started)) {
                practice.markTried(PracticeSkill.START)
            }
        }
    }

    private fun pauseTimer() {
        val snapshot = _state.value
        val openBreak = BreakSession.openBreak(snapshot.tasks)
        if (openBreak != null) {
            val work = BreakSession.workId(openBreak)?.let { id ->
                snapshot.tasks.find { it.id == id }
            }
            _state.update {
                it.copy(
                    selectedTask = work ?: it.selectedTask,
                    pauseSlipOpen = false,
                    showDayClose = false,
                )
            }
            return
        }
        val task = snapshot.selectedTask?.takeUnless { BreakSession.isBreak(it) }
            ?: snapshot.tasks.firstOrNull { !BreakSession.isBreak(it) && TaskTimer.hasOpenBlock(it) }
            ?: return
        if (!TaskTimer.hasOpenBlock(task)) return
        val now = snapshot.now?.let { TimeMath.minuteOfDay(it).toInt() } ?: return
        val date = snapshot.date ?: return
        val paused = TaskTimer.pause(task, now)
        val breakTask = BreakSession.start(paused.copy(date = paused.date ?: date), now)
        _state.update {
            it.copy(
                pauseSlipOpen = false,
                showDayClose = false,
                selectedTask = paused,
            )
        }
        viewModelScope.launch {
            upsertOrReport(paused)
            upsertOrReport(breakTask)
            _state.update { it.copy(selectedTask = paused) }
        }
    }

    private fun continueBreak() {
        val now = _state.value.now?.let { TimeMath.minuteOfDay(it).toInt() } ?: return
        val breakTask = BreakSession.openBreak(_state.value.tasks) ?: return
        val workId = BreakSession.workId(breakTask) ?: return
        val work = _state.value.tasks.find { it.id == workId } ?: return
        if (!TaskTimer.canStart(work)) return
        val closed = TaskTimer.pause(breakTask, now)
        val started = TaskTimer.start(work, now)
        _state.update { it.copy(pauseSlipOpen = false, selectedTask = started) }
        viewModelScope.launch {
            upsertOrReport(closed)
            if (upsertOrReport(started)) {
                practice.markTried(PracticeSkill.START)
            }
        }
    }

    private fun finishBreak() {
        val now = _state.value.now?.let { TimeMath.minuteOfDay(it).toInt() } ?: return
        val breakTask = BreakSession.openBreak(_state.value.tasks) ?: return
        val workId = BreakSession.workId(breakTask)
        val work = workId?.let { id -> _state.value.tasks.find { it.id == id } }
        if (work == null) {
            viewModelScope.launch {
                try {
                    taskRepository.delete(breakTask.id)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    effects.send(DayUiEffect.ShowMessageRes(R.string.error_task_delete))
                }
            }
            return
        }
        val closed = TaskTimer.pause(breakTask, now)
        _state.update {
            it.copy(
                pauseSlipOpen = false,
                selectedTask = work,
                detailsOpen = false,
                completion = CompletionDraft(),
            )
        }
        persistTimer(closed)
        viewModelScope.launch { showCompletion(work, null) }
    }

    private fun openCompletion(blockIndex: Int?) {
        val task = _state.value.selectedTask ?: return
        viewModelScope.launch { showCompletion(task, blockIndex) }
    }

    /**
     * Opens scores for one interval. [blockIndex] null uses the latest interval.
     * The task itself stays at its current status.
     */
    private suspend fun showCompletion(task: Task, blockIndex: Int?) {
        val index = blockIndex ?: task.blocks.indexOfLast { it.endMinute != null }
            .takeIf { it >= 0 }
            ?: task.blocks.lastIndex
        if (index < 0) return
        val (ready, block) = ensureBlockId(task, index)
        val stored = reports.observeReports(ready.id).first()
            .firstOrNull { it.id == TaskCompletionCodec.reportId(ready.id, block.id) }
            ?.let { TaskCompletionCodec.decode(it.text) }
        _state.update {
            it.copy(
                selectedTask = ready,
                detailsOpen = false,
                completion = CompletionDraft.from(ready, block.id, stored),
            )
        }
    }

    private suspend fun ensureBlockId(task: Task, index: Int): Pair<Task, TimeBlock> {
        val block = task.blocks[index]
        if (block.id.isNotBlank()) return task to block
        val assigned = block.copy(id = java.util.UUID.randomUUID().toString())
        val updated = task.copy(
            blocks = task.blocks.mapIndexed { i, current -> if (i == index) assigned else current },
        )
        upsertOrReport(updated)
        return updated to assigned
    }

    private fun submitCompletion() {
        val draft = _state.value.completion
        val taskId = draft.taskId ?: return
        val blockId = draft.blockId ?: return
        val task = _state.value.tasks.find { it.id == taskId } ?: return
        if (draft.titleEditable && draft.title.isBlank()) return
        val completion = TaskCompletion.orNull(
            stateAfter = draft.stateAfter ?: TaskCompletion.DEFAULT_PROXY,
            engagement = draft.engagement ?: TaskCompletion.DEFAULT_PROXY,
            skill = draft.skill ?: TaskCompletion.DEFAULT_PROXY,
            easier = draft.easier,
            mark = draft.mark ?: DemandMark.CHALLENGE,
            thoughts = draft.thoughts,
            reframe = draft.reframe,
            done = draft.done,
            progress = draft.progress,
            shortFlow = ShortFlowScale.profileOrNull(
                draft.shortFlow.mapNotNull { it }.takeIf { it.size == ShortFlowScale.ITEM_COUNT },
            ),
        ) ?: return
        val saved = if (draft.titleEditable) task.copy(title = draft.title.trim()) else task
        val nextName = draft.nextTitle.trim()
        val date = _state.value.date
        _state.update {
            it.copy(
                completion = CompletionDraft(),
                selectedTask = null,
                detailsOpen = false,
            )
        }
        viewModelScope.launch {
            upsertOrReport(saved)
            val reportId = TaskCompletionCodec.reportId(saved.id, blockId)
            val existing = reports.observeReports(saved.id).first()
                .firstOrNull { it.id == reportId }
            reports.upsert(
                TaskReport(
                    id = reportId,
                    taskId = saved.id,
                    createdAtEpochMillis = existing?.createdAtEpochMillis
                        ?: System.currentTimeMillis(),
                    text = TaskCompletionCodec.encode(completion),
                ),
            )
            if (nextName.isNotEmpty() && date != null) {
                val next = Task(
                    id = TaskId(UUID.randomUUID().toString()),
                    title = nextName,
                    colorArgb = latestAppearance.accentArgb,
                    date = date,
                )
                upsertOrReport(next)
                val book = goals.observe().first()
                val intervalKey = GoalBook.intervalKey(saved.id.value, blockId)
                val from = if (book.taskLinks[intervalKey] != null) intervalKey else saved.id.value
                goals.save(book.inherit(from, next.id.value))
            }
            refreshNudge()
        }
    }

    private val skippedNudges = mutableSetOf<String>()

    private fun finishCourse(practice: BuiltInPractice, checked: Set<String>) {
        val lesson = BuiltInCourse.lesson(practice)
        if (!BuiltInCourse.checkpointsMet(lesson, checked)) return
        val workOpen = _state.value.tasks.any { task ->
            !BreakSession.isBreak(task) && TaskTimer.hasOpenBlock(task)
        }
        if (workOpen) return
        viewModelScope.launch {
            courseHistory.append(PracticePass(practice, System.currentTimeMillis()))
        }
    }

    private fun skipNudge() {
        val key = _state.value.difficultyOffer?.key ?: return
        skippedNudges.add(key)
        _state.update { it.copy(difficultyOffer = null) }
    }

    private fun acceptNudge(kind: HardenKind) {
        val offer = _state.value.difficultyOffer ?: return
        val next = _state.value.tasks.lastOrNull { task ->
            task.title == offer.title && task.id.value !in offer.key.split('|')
        } ?: _state.value.tasks.lastOrNull { it.title == offer.title }
        _state.update { it.copy(difficultyOffer = null) }
        if (next == null) return
        val hardened = DifficultyNudge.apply(next, kind)
        persistTimer(hardened)
    }

    private suspend fun refreshNudge() {
        val tasks = taskRepository.observeAll().first()
        val stored = reports.observeAllReports().first()
        val rows = stored.mapNotNull { report ->
            val completion = TaskCompletionCodec.decode(report.text) ?: return@mapNotNull null
            val task = tasks.find { it.id == report.taskId } ?: return@mapNotNull null
            DifficultyNudge.Row(
                taskId = task.id.value,
                title = task.title,
                completion = completion,
                at = report.createdAtEpochMillis,
            )
        }
        val offer = DifficultyNudge.offer(rows, skippedNudges.toSet())
        _state.update { it.copy(difficultyOffer = offer) }
    }

    private fun closeOpenBreak(now: Int) {
        val breakTask = BreakSession.openBreak(_state.value.tasks) ?: return
        persistTimer(TaskTimer.pause(breakTask, now))
    }

    private fun commitPause() {
        val snapshot = _state.value
        val text = snapshot.pauseDraft.trim()
        val taskId = snapshot.selectedTask?.id
        val mood = snapshot.pauseMood
        _state.update {
            it.copy(
                pauseSlipOpen = false,
                pauseDraft = "",
            )
        }
        if (taskId != null && text.isNotEmpty()) {
            viewModelScope.launch {
                flowMemory.saveCheckpoint(taskId, text, mood)
                practice.markTried(PracticeSkill.CHECKPOINT)
            }
        } else {
            refreshFlowChrome()
        }
    }

    /**
     * Reapplies the stored chip and day-close card after a local slip change
     * that does not itself write the store.
     */
    private fun refreshFlowChrome() {
        viewModelScope.launch {
            val memory = flowMemory.observe().first()
            _state.update { current ->
                val selected = current.selectedTask
                val checkpoint = selected?.let { memory.checkpoint(it.id) }
                val showChip = checkpoint != null &&
                    !current.pauseSlipOpen &&
                    !TaskTimer.hasOpenBlock(selected)
                val date = current.date
                current.copy(
                    resumeCheckpoint = if (showChip) checkpoint?.text else null,
                    resumeMood = if (showChip) checkpoint?.mood else null,
                    showDayClose = date != null &&
                        memory.isDayCloseVisible(date) &&
                        !current.pauseSlipOpen,
                )
            }
        }
    }

    private fun showDayClose() {
        val date = _state.value.date ?: return
        _state.update { it.copy(pauseSlipOpen = false) }
        viewModelScope.launch {
            flowMemory.markDayClosed(date)
            practice.markTried(PracticeSkill.DAY_CLOSE)
        }
    }

    private fun dismissDayClose() {
        val date = _state.value.date ?: return
        viewModelScope.launch {
            flowMemory.dismissDayClose(date)
        }
    }

    private fun watchBudgets() {
        viewModelScope.launch {
            state.collect { snapshot ->
                val now = snapshot.now?.let { TimeMath.minuteOfDay(it).toInt() } ?: return@collect
                val due = snapshot.tasks.filter { task ->
                    TaskTimer.hasOpenBlock(task) && budgets[task.id]?.let { now >= it } == true
                }
                due.forEach { task ->
                    val end = budgets.remove(task.id) ?: return@forEach
                    if (chimed.add(task.id)) {
                        effects.trySend(DayUiEffect.PlayChime)
                    }
                    persistTimer(TaskTimer.pause(task, end))
                }
            }
        }
    }

    private fun persistTimer(updated: Task) {
        viewModelScope.launch { upsertOrReport(updated) }
    }

    /**
     * Writes [updated]. A failed write reports the existing timer message
     * and returns false so a practice flag is not set for a start that
     * did not stick.
     */
    private suspend fun upsertOrReport(updated: Task): Boolean {
        try {
            taskRepository.upsert(updated)
            return true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            effects.send(
                DayUiEffect.ShowMessageRes(R.string.error_day_timer),
            )
            return false
        }
    }

    private fun markTried(skill: PracticeSkill) {
        viewModelScope.launch { practice.markTried(skill) }
    }

    private fun confirmDelete() {
        val task = _state.value.pendingDelete ?: return
        viewModelScope.launch {
            try {
                reports.observeReports(task.id).first().forEach { report ->
                    reports.delete(report.id)
                }
                val wedges = if (BreakSession.isBreak(task)) {
                    emptyList()
                } else {
                    BreakSession.wedgesFor(task.id, _state.value.tasks)
                }
                wedges.forEach { wedge -> taskRepository.delete(wedge.id) }
                taskRepository.delete(task.id)
                val gone = buildSet {
                    add(task.id)
                    wedges.forEach { add(it.id) }
                }
                _state.update {
                    it.copy(
                        pendingDelete = null,
                        selectedTask = it.selectedTask?.takeUnless { selected ->
                            selected.id in gone
                        },
                        detailsOpen = it.detailsOpen && it.selectedTask?.id !in gone,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                effects.send(
                    DayUiEffect.ShowMessageRes(R.string.error_task_delete),
                )
            }
        }
    }
}

private data class DayTick(
    val date: LocalDate,
    val time: LocalTime,
    val memory: FlowMemorySnapshot,
)
