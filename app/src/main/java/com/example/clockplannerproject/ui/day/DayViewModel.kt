package com.example.clockplannerproject.ui.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clockplannerproject.data.SampleDaySeeder
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.RecurrenceMaterializer
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskInstances
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TaskTimer
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Day screen ViewModel. Depends on repository, clock, and debug seeder — no Android Context.
 *
 * @since 0.1.0
 */
class DayViewModel(
    private val taskRepository: TaskRepository,
    private val timeProvider: TimeProvider,
    private val sampleDaySeeder: SampleDaySeeder,
) : ViewModel() {

    private val _state = MutableStateFlow(DayUiState())
    val state: StateFlow<DayUiState> = _state.asStateFlow()

    private val effects = Channel<DayUiEffect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    private val retryTick = MutableStateFlow(0)
    private val selectedDate = MutableStateFlow(timeProvider.today())
    private var halfSeeded = false
    private val materializer = RecurrenceMaterializer(taskRepository)

    init {
        observeDay()
    }

    fun onIntent(intent: DayUiIntent) {
        when (intent) {
            DayUiIntent.Retry -> retryTick.update { it + 1 }
            is DayUiIntent.SelectTask -> _state.update {
                if (it.selectedTask?.id == intent.task.id) {
                    it.copy(selectedTask = null, detailsOpen = false)
                } else {
                    it.copy(selectedTask = intent.task, detailsOpen = false)
                }
            }
            is DayUiIntent.OpenTaskDetails -> _state.update {
                val task = intent.task ?: it.selectedTask
                if (task == null) {
                    it
                } else {
                    it.copy(selectedTask = task, detailsOpen = true)
                }
            }
            DayUiIntent.DismissDetails -> _state.update { it.copy(detailsOpen = false) }
            DayUiIntent.DismissTask -> _state.update {
                it.copy(selectedTask = null, detailsOpen = false, showMovePicker = false)
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
            is DayUiIntent.SetViewMode -> _state.update { it.copy(viewMode = intent.mode) }
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
                selectedDate.value = intent.date
                _state.update { it.copy(showDatePicker = false) }
            }
            is DayUiIntent.SetDialHalf -> _state.update { it.copy(dialHalf = intent.half) }
        }
    }

    private fun shiftDate(days: Int) {
        selectedDate.update { it.plus(days, DateTimeUnit.DAY) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDay() {
        viewModelScope.launch {
            retryTick.collectLatest {
                _state.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    try {
                        sampleDaySeeder.seedIfEmpty()
                    } catch (seedError: Exception) {
                        effects.send(
                            DayUiEffect.ShowMessage(
                                seedError.message ?: "Could not load sample day",
                            ),
                        )
                    }
                    var lastMaterialized: LocalDate? = null
                    combine(
                        timeProvider.observeTime(),
                        selectedDate.filterNotNull(),
                    ) { time, date -> date to time }
                        .distinctUntilChanged()
                        .flatMapLatest { (date, time) ->
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
                                    errorMessage = null,
                                    userRotationOffsetDeg = current.userRotationOffsetDeg,
                                    resizingTaskId = current.resizingTaskId,
                                    resizePreviewEndMinute = current.resizePreviewEndMinute,
                                    resizeBlockStartMinute = current.resizeBlockStartMinute,
                                    hideCompleted = current.hideCompleted,
                                    viewMode = current.viewMode,
                                    showMovePicker = current.showMovePicker,
                                    showDatePicker = current.showDatePicker,
                                    dialHalf = half,
                                )
                            }
                        }
                        .collect { snapshot -> _state.value = snapshot }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: error::class.simpleName,
                        )
                    }
                    effects.send(
                        DayUiEffect.ShowMessage(error.message ?: "Could not load the day"),
                    )
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
                effects.send(
                    DayUiEffect.ShowMessage(error.message ?: "Could not resize task"),
                )
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
                    DayUiEffect.ShowMessage(error.message ?: "Could not update task"),
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
                    DayUiEffect.ShowMessage(error.message ?: "Could not duplicate task"),
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
                    DayUiEffect.ShowMessage(error.message ?: "Could not move task"),
                )
            }
        }
    }

    private fun startTimer() {
        val task = _state.value.selectedTask ?: return
        val now = _state.value.now?.let { TimeMath.minuteOfDay(it).toInt() } ?: return
        if (!TaskTimer.canStart(task)) return
        persistTimer(TaskTimer.start(task, now))
    }

    private fun pauseTimer() {
        val task = _state.value.selectedTask ?: return
        val now = _state.value.now?.let { TimeMath.minuteOfDay(it).toInt() } ?: return
        persistTimer(TaskTimer.pause(task, now))
    }

    private fun persistTimer(updated: Task) {
        viewModelScope.launch {
            try {
                taskRepository.upsert(updated)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                effects.send(
                    DayUiEffect.ShowMessage(error.message ?: "Could not update timer"),
                )
            }
        }
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
                    DayUiEffect.ShowMessage(error.message ?: "Could not delete task"),
                )
            }
        }
    }
}
