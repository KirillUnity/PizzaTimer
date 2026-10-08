package com.example.clockplannerproject.ui.task

import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.RecurrenceRule
import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskReport
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
    val project: String = "",
    val scheduledDate: LocalDate? = null,
    val error: TaskDraftError? = null,
    val recurrence: RecurrenceRule = RecurrenceRule.None,
    val seriesId: String? = null,
) {
    val isNew: Boolean get() = id == null

    fun toTask(): Task {
        val resolvedId = id ?: TaskId(UUID.randomUUID().toString())
        val normalizedProject = project.trim().takeIf { it.isNotEmpty() }
        val normalizedRecurrence = if (scheduledDate == null) RecurrenceRule.None else recurrence
        return Task(
            id = resolvedId,
            title = title.trim(),
            description = description.trim(),
            colorArgb = colorArgb,
            blocks = if (scheduledDate == null) emptyList() else blocks,
            status = status,
            date = scheduledDate,
            importance = importance,
            project = normalizedProject,
            recurrence = normalizedRecurrence,
            seriesId = if (normalizedRecurrence is RecurrenceRule.None) {
                seriesId
            } else {
                seriesId ?: resolvedId.value
            },
        )
    }

    companion object {
        fun from(task: Task): TaskEditorState = TaskEditorState(
            id = task.id,
            title = task.title,
            description = task.description,
            colorArgb = task.colorArgb,
            blocks = task.blocks,
            status = task.status,
            importance = task.importance,
            project = task.normalizedProject.orEmpty(),
            scheduledDate = task.date,
            recurrence = task.recurrence,
            seriesId = task.seriesId,
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
    val selectedProject: String? = null,
    val projectSearch: String = "",
    val nowMinute: Int = 0,
    val pendingMove: Task? = null,
    val reports: List<TaskReport> = emptyList(),
    val reportDraft: String = "",
    val editingReportId: ReportId? = null,
) : UiState {
    val visibleTasks: List<Task>
        get() {
            val selected = selectedProject
            val query = projectSearch.trim()
            return tasks.asSequence()
                .filter { task ->
                    selected == null || task.normalizedProject.equals(selected, ignoreCase = true)
                }
                .filter { task ->
                    query.isEmpty() ||
                        task.normalizedProject?.contains(query, ignoreCase = true) == true
                }
                .sortedWith(
                    compareBy<Task>(
                        { it.normalizedProject?.lowercase().orEmpty() },
                        { it.date != null },
                        { it.date },
                        { it.sortMinute },
                        { it.title.lowercase() },
                        { it.id.value },
                    ),
                )
                .toList()
        }

    val availableProjects: List<String>
        get() {
            val query = projectSearch.trim()
            return tasks.mapNotNull(Task::normalizedProject)
                .distinctBy { it.lowercase() }
                .filter { query.isEmpty() || it.contains(query, ignoreCase = true) }
                .sortedWith(String.CASE_INSENSITIVE_ORDER)
        }
}

sealed interface TasksUiIntent : UiIntent {
    data object Create : TasksUiIntent
    data object CreateForSelectedDay : TasksUiIntent
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
    data class ChangeProject(val value: String) : TasksUiIntent
    data class FilterByProject(val project: String?) : TasksUiIntent
    data class ChangeProjectSearch(val query: String) : TasksUiIntent
    data object ClearProjectSearch : TasksUiIntent
    data class ChangeScheduled(val scheduled: Boolean) : TasksUiIntent
    data class MoveToDiagram(val task: Task) : TasksUiIntent
    data class OpenStartPicker(val index: Int) : TasksUiIntent
    data class OpenEndPicker(val index: Int) : TasksUiIntent
    data object DismissTimePicker : TasksUiIntent
    data class Duplicate(val task: Task) : TasksUiIntent
    data class RequestMove(val task: Task) : TasksUiIntent
    data object DismissMove : TasksUiIntent
    data class ConfirmMove(val date: LocalDate) : TasksUiIntent
    data class ChangeRecurrence(val rule: RecurrenceRule) : TasksUiIntent
    data class ChangeReportDraft(val text: String) : TasksUiIntent
    data class EditReport(val report: TaskReport) : TasksUiIntent
    data object SaveReport : TasksUiIntent
    data class DeleteReport(val id: ReportId) : TasksUiIntent
}

sealed interface TasksUiEffect : UiEffect {
    data class ShowMessage(val message: String) : TasksUiEffect
}
