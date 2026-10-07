package com.example.clockplannerproject.ui.day

import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.mvi.UiEffect
import com.example.clockplannerproject.kit.core.mvi.UiIntent
import com.example.clockplannerproject.kit.core.mvi.UiState
import com.example.clockplannerproject.kit.core.time.TaskResize
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * @param hideCompleted when true, canvas modes use CompactRemaining reflow
 * (remaining work fills 360°). [ViewMode.LIST] only filters DONE.
 * Civil times on tasks stay unchanged.
 * @param viewMode DIAL / PIZZA / PETALS (Canvas) or LIST (Compose).
 */
data class DayUiState(
    val date: LocalDate? = null,
    val now: LocalTime? = null,
    val tasks: List<Task> = emptyList(),
    val selectedTask: Task? = null,
    val pendingDelete: Task? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val userRotationOffsetDeg: Float = 0f,
    val resizingTaskId: TaskId? = null,
    val resizePreviewEndMinute: Int? = null,
    val resizeBlockStartMinute: Int? = null,
    val hideCompleted: Boolean = false,
    val viewMode: ViewMode = ViewMode.DIAL,
    val showMovePicker: Boolean = false,
) : UiState {
    val taskCount: Int get() = tasks.size
    val hasUserRotation: Boolean get() = kotlin.math.abs(userRotationOffsetDeg) > 0.5f
    val dialTasks: List<Task>
        get() = TaskResize.overlayEndMinute(
            tasks,
            resizingTaskId,
            resizePreviewEndMinute,
            resizeBlockStartMinute,
        )
    val sheetTask: Task?
        get() {
            val selected = selectedTask ?: return null
            val preview = resizePreviewEndMinute
            return if (selected.id == resizingTaskId && preview != null) {
                selected.withEndMinute(preview, resizeBlockStartMinute)
            } else {
                selected
            }
        }
    val handleTask: Task?
        get() {
            sheetTask?.let { return it }
            val id = resizingTaskId ?: return null
            val end = resizePreviewEndMinute ?: return null
            return tasks.find { it.id == id }?.withEndMinute(end, resizeBlockStartMinute)
        }
}

sealed interface DayUiIntent : UiIntent {
    data object Retry : DayUiIntent
    data class SelectTask(val task: Task) : DayUiIntent
    data object DismissTask : DayUiIntent
    data object ToggleComplete : DayUiIntent
    data object EditTask : DayUiIntent
    data object RequestDelete : DayUiIntent
    data object ConfirmDelete : DayUiIntent
    data object DismissDelete : DayUiIntent
    data class RotateBy(val deltaDeg: Float) : DayUiIntent
    data object ResetRotation : DayUiIntent
    data class ResizePreview(
        val taskId: TaskId,
        val endMinute: Int,
        val blockStartMinute: Int? = null,
    ) : DayUiIntent
    data class ResizeTask(
        val taskId: TaskId,
        val newEndMinute: Int,
        val blockStartMinute: Int? = null,
    ) : DayUiIntent
    data object CancelResize : DayUiIntent
    data object ToggleHideCompleted : DayUiIntent
    data class SetViewMode(val mode: ViewMode) : DayUiIntent
    data object DuplicateTask : DayUiIntent
    data object RequestMove : DayUiIntent
    data object DismissMove : DayUiIntent
    data class ConfirmMove(val date: LocalDate) : DayUiIntent
    data object StartTimer : DayUiIntent
    data object PauseTimer : DayUiIntent
}

sealed interface DayUiEffect : UiEffect {
    data class ShowMessage(val message: String) : DayUiEffect
    data class OpenEditor(val task: Task) : DayUiEffect
}
