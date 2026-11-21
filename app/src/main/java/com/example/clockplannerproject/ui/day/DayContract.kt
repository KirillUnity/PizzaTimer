package com.example.clockplannerproject.ui.day

import com.example.clockplannerproject.kit.core.Appearance
import com.example.clockplannerproject.kit.core.BuiltInPractice
import com.example.clockplannerproject.kit.core.DemandMark
import com.example.clockplannerproject.kit.core.HardenKind
import com.example.clockplannerproject.kit.core.DifficultyOffer
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskCompletion
import com.example.clockplannerproject.kit.core.ShortFlowScale
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.config.BackgroundConfig
import com.example.clockplannerproject.kit.core.mvi.UiEffect
import com.example.clockplannerproject.kit.core.mvi.UiIntent
import com.example.clockplannerproject.kit.core.mvi.UiState
import com.example.clockplannerproject.kit.core.time.TaskResize
import com.example.clockplannerproject.kit.core.PauseMood
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
    /** Clock's local today, used to name the date strip. Null before the first tick. */
    val localToday: LocalDate? = null,
    val now: LocalTime? = null,
    val tasks: List<Task> = emptyList(),
    val selectedTask: Task? = null,
    /** When true, [sheetTask] is shown; canvas tap only sets [selectedTask]. */
    val detailsOpen: Boolean = false,
    val pendingDelete: Task? = null,
    val isLoading: Boolean = true,
    /** Load-failure string resource, or null after a successful load. */
    val errorMessageRes: Int? = null,
    val userRotationOffsetDeg: Float = 0f,
    val resizingTaskId: TaskId? = null,
    val resizePreviewEndMinute: Int? = null,
    val resizeBlockStartMinute: Int? = null,
    val hideCompleted: Boolean = false,
    val viewMode: ViewMode = ViewMode.DIAL,
    val accentArgb: Long = Appearance.DEFAULT_ACCENT,
    val completedArgb: Long = Appearance.DEFAULT_COMPLETED,
    val background: BackgroundConfig = BackgroundConfig.None,
    /** Minutes left in the open timed session, or null when no budget is set. */
    val sessionMinutesLeft: Int? = null,
    val showMovePicker: Boolean = false,
    val showDatePicker: Boolean = false,
    val dialHalf: DialHalf = DialHalf.AM,
    /** Pause slip is open. The closed interval is already stored. */
    val pauseSlipOpen: Boolean = false,
    val pauseDraft: String = "",
    val pauseMood: PauseMood = PauseMood.Calm,
    /** Text for the resume chip. Null hides the chip. */
    val resumeCheckpoint: String? = null,
    /** Mood shown on the resume chip. Null when the chip is hidden. */
    val resumeMood: PauseMood? = null,
    /** Day-close card under the dial. */
    val showDayClose: Boolean = false,
    /** Finish-task sheet. Scores are proxies, not a clinical scale. */
    val completion: CompletionDraft = CompletionDraft(),
    /** One hardening offer. Null hides it. Not a flow score. */
    val difficultyOffer: DifficultyOffer? = null,
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
    val overlayTask: Task?
        get() {
            val selected = selectedTask ?: run {
                val id = resizingTaskId ?: return null
                val end = resizePreviewEndMinute ?: return null
                return tasks.find { it.id == id }?.withEndMinute(end, resizeBlockStartMinute)
            }
            val preview = resizePreviewEndMinute
            return if (selected.id == resizingTaskId && preview != null) {
                selected.withEndMinute(preview, resizeBlockStartMinute)
            } else {
                selected
            }
        }
    val sheetTask: Task?
        get() {
            if (!detailsOpen) return null
            return overlayTask
        }
}

sealed interface DayUiIntent : UiIntent {
    data object Retry : DayUiIntent
    data class SelectTask(val task: Task) : DayUiIntent
    /** Full card (sheet). Does not run on canvas tap. */
    data class OpenTaskDetails(val task: Task? = null) : DayUiIntent
    data object DismissDetails : DayUiIntent
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

    /**
     * Writes view, accent, completed color, and underlay.
     * Dark theme on the stored look is kept.
     *
     * @param appearance look chosen in the day panel; [Appearance.darkTheme] is ignored
     */
    data class SetLook(val appearance: Appearance) : DayUiIntent
    data object DuplicateTask : DayUiIntent
    data object RequestMove : DayUiIntent
    data object DismissMove : DayUiIntent
    data class ConfirmMove(val date: LocalDate) : DayUiIntent
    data object StartTimer : DayUiIntent

    /**
     * Opens one interval and closes it after [minutes].
     * The length is not changed again by the timer.
     *
     * @param minutes chosen length, whole minutes
     */
    data class StartFor(val minutes: Int) : DayUiIntent
    data object PauseTimer : DayUiIntent

    /** Closes the open break and appends one work interval on the same task. */
    data object ContinueBreak : DayUiIntent

    /** Closes the open break and opens the completion sheet. */
    data object FinishBreak : DayUiIntent

    /** Replaces the open completion draft. Does not save. */
    data class EditCompletion(val draft: CompletionDraft) : DayUiIntent

    /** Saves the draft, filling missing 1–10 proxies with 5. */
    data object SubmitCompletion : DayUiIntent

    /** Opens the completion sheet for the selected task’s latest interval. */
    data object OpenCompletion : DayUiIntent

    /** Opens the completion sheet for one interval of the selected task. */
    data class OpenIntervalCompletion(val blockIndex: Int) : DayUiIntent

    /** Closes the sheet without writing a completion. */
    data object DismissCompletion : DayUiIntent

    /** Hides the hardening offer for this trio. */
    data object SkipNudge : DayUiIntent

    /** Records one hardening on the next similar task. Scores stay put. */
    data class AcceptNudge(val kind: HardenKind) : DayUiIntent

    /** Appends a course pass when every checkpoint is checked and no work interval is open. */
    data class FinishCourse(val practice: BuiltInPractice, val checked: Set<String>) : DayUiIntent
    data object PreviousDay : DayUiIntent
    data object NextDay : DayUiIntent
    data object OpenDatePicker : DayUiIntent
    data object DismissDatePicker : DayUiIntent
    data class SelectDate(val date: LocalDate) : DayUiIntent
    data class SetDialHalf(val half: DialHalf) : DayUiIntent
    data class ChangePauseDraft(val text: String) : DayUiIntent
    data class SelectPauseMood(val mood: PauseMood) : DayUiIntent
    /** Stores the slip text and closes it. Does not edit closed blocks. */
    data object CommitPause : DayUiIntent
    /** Closes the slip. Does not edit closed blocks. */
    data object SkipPause : DayUiIntent
    data object ShowDayClose : DayUiIntent
    data object DismissDayClose : DayUiIntent
}

sealed interface DayUiEffect : UiEffect {
    /**
     * Snackbar copy from resources. The screen resolves [resId] with `stringResource`.
     *
     * @param resId failure string in the default and Russian catalogs
     */
    data class ShowMessageRes(val resId: Int) : DayUiEffect
    data class OpenEditor(val task: Task) : DayUiEffect

    /** One in-app chime. Not a notification. */
    data object PlayChime : DayUiEffect
}

/**
 * Editable finish-task sheet. Scores stay null until the user sets them.
 * [titleEditable] is true only when the task has no name yet.
 *
 * @since 1.2.0
 */
data class CompletionDraft(
    val open: Boolean = false,
    val taskId: TaskId? = null,
    /** Interval this sheet scores. Null until a block is chosen. */
    val blockId: String? = null,
    val title: String = "",
    val titleEditable: Boolean = false,
    val nextTitle: String = "",
    val stateAfter: Int? = null,
    val engagement: Int? = null,
    val skill: Int? = null,
    val easier: String = "",
    val mark: DemandMark? = null,
    val thoughts: String = "",
    val reframe: String = "",
    val done: Boolean = false,
    val progress: String = "",
    /** Nine short-form answers. A null entry means that item was skipped. */
    val shortFlow: List<Int?> = List(ShortFlowScale.ITEM_COUNT) { null },
) {
    companion object {
        /**
         * Open sheet for [task], filled from [stored] when a record already exists.
         */
        fun from(task: Task, blockId: String, stored: TaskCompletion?): CompletionDraft {
            if (stored == null) {
                return CompletionDraft(
                    open = true,
                    taskId = task.id,
                    blockId = blockId,
                    title = task.title,
                    titleEditable = task.title.isBlank(),
                )
            }
            val flow = stored.shortFlow
            return CompletionDraft(
                open = true,
                taskId = task.id,
                blockId = blockId,
                title = task.title,
                titleEditable = task.title.isBlank(),
                stateAfter = stored.stateAfter,
                engagement = stored.engagement,
                skill = stored.skill,
                easier = stored.easier,
                mark = stored.mark,
                thoughts = stored.thoughts,
                reframe = stored.reframe,
                done = stored.done,
                progress = stored.progress,
                shortFlow = if (flow == null) {
                    List(ShortFlowScale.ITEM_COUNT) { null }
                } else {
                    flow.map { it }
                },
            )
        }
    }
}
