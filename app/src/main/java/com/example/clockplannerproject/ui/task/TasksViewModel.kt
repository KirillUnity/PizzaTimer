package com.example.clockplannerproject.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clockplannerproject.kit.core.RecurrenceMaterializer
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskDraftValidator
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskInstances
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
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
) : ViewModel() {

    private val selectedDate = MutableStateFlow(timeProvider.today())

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

    init {
        observeSelectedDate()
    }

    fun onIntent(intent: TasksUiIntent) {
        when (intent) {
            TasksUiIntent.Create -> openCreate()
            is TasksUiIntent.Edit -> _state.update {
                it.copy(
                    editor = TaskEditorState.from(intent.task),
                    timePicker = null,
                    nowMinute = minuteOfNow(),
                )
            }
            TasksUiIntent.Update -> save()
            is TasksUiIntent.Delete -> _state.update { it.copy(pendingDelete = intent.task) }
            TasksUiIntent.ConfirmDelete -> confirmDelete()
            TasksUiIntent.DismissEditor -> _state.update {
                it.copy(editor = null, timePicker = null)
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
            is TasksUiIntent.ChangeTagDraft -> updateEditor { it.copy(tagDraft = intent.value) }
            TasksUiIntent.AddTag -> addTag()
            is TasksUiIntent.RemoveTag -> updateEditor {
                it.copy(tags = it.tags.filterNot { tag -> tag == intent.tag })
            }
            is TasksUiIntent.FilterByTag -> _state.update { it.copy(tagFilter = intent.tag) }
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
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSelectedDate() {
        viewModelScope.launch {
            try {
                selectedDate
                    .flatMapLatest { date ->
                        materializer.ensureVisibleDay(date)
                        taskRepository.observeTasks(date).map { tasks -> date to tasks }
                    }
                    .collect { (date, tasks) ->
                        _state.update { current ->
                            current.copy(
                                date = date,
                                tasks = tasks.sortedBy { it.sortMinute },
                                isLoading = false,
                                errorMessage = null,
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
                        errorMessage = error.message ?: error::class.simpleName,
                    )
                }
                effects.send(
                    TasksUiEffect.ShowMessage(error.message ?: "Could not load tasks"),
                )
            }
        }
    }

    private fun openCreate() {
        val start = minuteOfNow().mod(TimeMath.MINUTES_PER_DAY)
        val end = (start + 60).mod(TimeMath.MINUTES_PER_DAY)
        _state.update {
            it.copy(
                editor = TaskEditorState(
                    blocks = listOf(TimeBlock(start, if (end == start) (start + 60).mod(TimeMath.MINUTES_PER_DAY) else end)),
                ),
                timePicker = null,
                nowMinute = start,
            )
        }
    }

    private fun save() {
        val snapshot = _state.value
        val editor = snapshot.editor ?: return
        val date = snapshot.date ?: return
        val error = TaskDraftValidator.validate(title = editor.title, blocks = editor.blocks)
        if (error != null) {
            _state.update { it.copy(editor = editor.copy(error = error)) }
            return
        }
        viewModelScope.launch {
            try {
                taskRepository.upsert(editor.toTask(date))
                _state.update { it.copy(editor = null, timePicker = null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                effects.send(
                    TasksUiEffect.ShowMessage(error.message ?: "Could not save task"),
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
                    TasksUiEffect.ShowMessage(error.message ?: "Could not delete task"),
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
                tagFilter = null,
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
                tagFilter = null,
            )
        }
    }

    private fun duplicate(task: Task) {
        val copy = TaskInstances.duplicated(
            task,
            TaskId(UUID.randomUUID().toString()),
            _state.value.date ?: task.date,
        )
        viewModelScope.launch {
            try {
                taskRepository.upsert(copy)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                effects.send(
                    TasksUiEffect.ShowMessage(error.message ?: "Could not duplicate task"),
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
                    TasksUiEffect.ShowMessage(error.message ?: "Could not move task"),
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

    private fun addTag() {
        updateEditor { editor ->
            val tag = editor.tagDraft.trim()
            if (tag.isEmpty() || tag in editor.tags) {
                editor.copy(tagDraft = "")
            } else {
                editor.copy(tags = editor.tags + tag, tagDraft = "")
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
}
