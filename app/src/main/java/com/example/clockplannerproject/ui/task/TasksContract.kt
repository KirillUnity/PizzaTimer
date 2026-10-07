package com.example.clockplannerproject.ui.task

import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskDraftError
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskPalette
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.mvi.UiEffect
import com.example.clockplannerproject.kit.core.mvi.UiIntent
import com.example.clockplannerproject.kit.core.mvi.UiState
import kotlinx.datetime.LocalDate
import java.util.UUID

data class TaskEditorState(
    val id: TaskId? = null,
    val title: String = "",
    val description: String = "",
    val colorArgb: Long = TaskPalette.defaultColor,
    val blocks: List<TimeBlock> = emptyList(),
    val status: TaskStatus = TaskStatus.TODO,
    val importance: Importance = Importance.MEDIUM,
    val tags: List<String> = emptyList(),
    val tagDraft: String = "",
    val error: TaskDraftError? = null,
) {
    val isNew: Boolean get() = id == null

    fun toTask(date: LocalDate): Task = Task(
        id = id ?: TaskId(UUID.randomUUID().toString()),
        title = title.trim(),
        description = description.trim(),
        colorArgb = colorArgb,
        blocks = blocks,
        status = status,
        date = date,
        importance = importance,
        tags = tags,
    )

    companion object {
        fun from(task: Task): TaskEditorState = TaskEditorState(
            id = task.id,
            title = task.title,
            description = task.description,
            colorArgb = task.colorArgb,
            blocks = task.blocks,
            status = task.status,
            importance = task.importance,
            tags = task.tags,
        )
    }
}

data class TimePickerTarget(
    val blockIndex: Int,
    val isStart: Boolean,
)

data class TasksUiState(
    val date: LocalDate? = null,
    val tasks: List<Task> = emptyList(),
    val editor: TaskEditorState? = null,
    val timePicker: TimePickerTarget? = null,
    val showDatePicker: Boolean = false,
    val pendingDelete: Task? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val tagFilter: String? = null,
    val nowMinute: Int = 0,
) : UiState {
    val visibleTasks: List<Task>
        get() {
            val filter = tagFilter
            val list = if (filter.isNullOrBlank()) tasks else tasks.filter { filter in it.tags }
            return list.sortedBy { it.sortMinute }
        }

    val availableTags: List<String>
        get() = tasks.flatMap { it.tags }.distinct().sorted()
}

sealed interface TasksUiIntent : UiIntent {
    data object Create : TasksUiIntent
    data class Edit(val task: Task) : TasksUiIntent
    data object Update : TasksUiIntent
    data class Delete(val task: Task) : TasksUiIntent
    data object ConfirmDelete : TasksUiIntent
    data object DismissEditor : TasksUiIntent
    data object DismissDelete : TasksUiIntent
    data object PreviousDay : TasksUiIntent
    data object NextDay : TasksUiIntent
    data object OpenDatePicker : TasksUiIntent
    data object DismissDatePicker : TasksUiIntent
    data class SelectDate(val date: LocalDate) : TasksUiIntent
    data class ChangeTitle(val title: String) : TasksUiIntent
    data class ChangeDescription(val description: String) : TasksUiIntent
    data class ChangeColor(val colorArgb: Long) : TasksUiIntent
    data class ChangeBlockStart(val index: Int, val minute: Int) : TasksUiIntent
    data class ChangeBlockEnd(val index: Int, val minute: Int) : TasksUiIntent
    data object AddBlock : TasksUiIntent
    data class RemoveBlock(val index: Int) : TasksUiIntent
    data object ClearTime : TasksUiIntent
    data class ChangeStatus(val status: TaskStatus) : TasksUiIntent
    data class ChangeImportance(val importance: Importance) : TasksUiIntent
    data class ChangeTagDraft(val value: String) : TasksUiIntent
    data object AddTag : TasksUiIntent
    data class RemoveTag(val tag: String) : TasksUiIntent
    data class FilterByTag(val tag: String?) : TasksUiIntent
    data class OpenStartPicker(val index: Int) : TasksUiIntent
    data class OpenEndPicker(val index: Int) : TasksUiIntent
    data object DismissTimePicker : TasksUiIntent
}

sealed interface TasksUiEffect : UiEffect {
    data class ShowMessage(val message: String) : TasksUiEffect
}
