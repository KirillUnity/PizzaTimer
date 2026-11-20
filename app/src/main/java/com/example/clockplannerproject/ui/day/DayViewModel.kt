package com.example.clockplannerproject.ui.day

import com.example.clockplannerproject.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clockplannerproject.data.InMemoryAppearanceRepository
import com.example.clockplannerproject.data.InMemoryFlowMemoryStore
import com.example.clockplannerproject.data.InMemoryPracticeProgressStore
import com.example.clockplannerproject.data.SampleDaySeeder
import com.example.clockplannerproject.kit.core.Appearance
import com.example.clockplannerproject.kit.core.AppearanceRepository
import com.example.clockplannerproject.kit.core.FlowMemorySnapshot
import com.example.clockplannerproject.kit.core.FlowMemoryStore
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.RecurrenceMaterializer
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskInstances
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TaskTimer
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.kit.core.ViewMode
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
                    if (task == null) {
                        it
                    } else {
                        it.copy(selectedTask = task, detailsOpen = true)
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
            is DayUiIntent.SetViewMode -> {
                _state.update { it.copy(viewMode = intent.mode) }
                if (appearanceReady) {
                    persistAppearance(latestAppearance.copy(viewMode = intent.mode))
                }
            }
            DayUiIntent.DuplicateTask -> duplicateSelected()
            DayUiIntent.RequestMove -> _state.update { it.copy(showMovePicker = it.selectedTask != null) }
            DayUiIntent.DismissMove -> _state.update { it.copy(showMovePicker = false) }
            is DayUiIntent.ConfirmMove -> moveSelected(intent.date)
            DayUiIntent.StartTimer -> startTimer()
            DayUiIntent.PauseTimer -> pauseTimer()
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
                            taskRepository.observeTasks(date).map { tasks ->
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
                taskRepository.upsert(TaskInstances.moved(task, date))
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

    private fun startTimer() {
        val task = _state.value.selectedTask ?: return
        val now = _state.value.now?.let { TimeMath.minuteOfDay(it).toInt() } ?: return
        if (!TaskTimer.canStart(task)) return
        _state.update { it.copy(pauseSlipOpen = false) }
        val started = TaskTimer.start(task, now)
        viewModelScope.launch {
            flowMemory.clearCheckpoint(task.id)
            if (upsertOrReport(started)) {
                practice.markTried(PracticeSkill.START)
            }
        }
    }

    private fun pauseTimer() {
        val task = _state.value.selectedTask ?: return
        val now = _state.value.now?.let { TimeMath.minuteOfDay(it).toInt() } ?: return
        _state.update {
            it.copy(
                pauseSlipOpen = true,
                showDayClose = false,
                pauseDraft = "",
                pauseMood = PauseMood.Calm,
            )
        }
        persistTimer(TaskTimer.pause(task, now))
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
                taskRepository.delete(task.id)
                _state.update {
                    it.copy(
                        pendingDelete = null,
                        selectedTask = it.selectedTask?.takeUnless { selected ->
                            selected.id == task.id
                        },
                        detailsOpen = it.detailsOpen && it.selectedTask?.id != task.id,
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
