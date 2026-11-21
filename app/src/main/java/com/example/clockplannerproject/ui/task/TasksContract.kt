package com.example.clockplannerproject.ui.task

import com.example.clockplannerproject.kit.core.BreakSession
import com.example.clockplannerproject.kit.core.GoalBook
import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.RecurrenceRule
import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskReport
import com.example.clockplannerproject.kit.core.TaskDraftError
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskPalette
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TaskTags
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.mvi.UiEffect
import com.example.clockplannerproject.kit.core.mvi.UiIntent
import com.example.clockplannerproject.kit.core.mvi.UiState
import com.example.clockplannerproject.kit.core.planner.DEFAULT_WORK_END_MINUTE
import com.example.clockplannerproject.kit.core.planner.DEFAULT_WORK_START_MINUTE
import com.example.clockplannerproject.kit.core.planner.PlanDraft
import kotlinx.datetime.LocalDate
import java.util.UUID

/**
 * Where a new-task sheet is in the create flow. Null means the accordion editor (edit).
 *
 * @since 1.3.0
 */
enum class CreateStage {
    CHOOSER,
    NAME,
    WHEN,
    LOOK,
    DETAILS,
}

/**
 * Collapsed rows on the existing-task editor.
 *
 * @since 1.3.0
 */
enum class EditSection {
    NAME,
    WHEN,
    LOOK,
    DETAILS,
    GOALS,
    REPORTS,
    ACTIONS,
}

data class TaskEditorState(
    val id: TaskId? = null,
    val title: String = "",
    val description: String = "",
    val colorArgb: Long = TaskPalette.defaultColor,
    val blocks: List<TimeBlock> = emptyList(),
    val status: TaskStatus = TaskStatus.TODO,
    val importance: Importance = Importance.MEDIUM,
    val project: String = "",
    val tags: List<String> = emptyList(),
    val tagDraft: String = "",
    val scheduledDate: LocalDate? = null,
    val error: TaskDraftError? = null,
    val recurrence: RecurrenceRule = RecurrenceRule.None,
    val seriesId: String? = null,
    /**
     * Date whose intervals are open in the editor.
     * Null for a Daily or Weekdays template until the user picks a day.
     */
    val intervalDate: LocalDate? = null,
    val createStage: CreateStage? = null,
    val expandedEditSections: Set<EditSection> = emptySet(),
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
            tags = TaskTags.normalize(tags),
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
            blocks = task.blocks.map { block ->
                if (block.id.isNotBlank()) block else block.copy(id = UUID.randomUUID().toString())
            },
            status = task.status,
            importance = task.importance,
            project = task.normalizedProject.orEmpty(),
            tags = task.normalizedTags,
            scheduledDate = task.date,
            recurrence = task.recurrence,
            seriesId = task.seriesId,
            intervalDate = if (task.recurrence is RecurrenceRule.None) task.date else null,
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
    val showIntervalDatePicker: Boolean = false,
    val pendingDelete: Task? = null,
    val selectedTaskIds: Set<TaskId> = emptySet(),
    val pendingBulkDelete: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessageRes: Int? = null,
    val selectedProject: String? = null,
    val selectedTag: String? = null,
    val projectSearch: String = "",
    val nowMinute: Int = 0,
    val pendingMove: Task? = null,
    val reports: List<TaskReport> = emptyList(),
    /** Interval whose notes and goal are open in the editor. */
    val commentBlockId: String? = null,
    val reportDraft: String = "",
    val editingReportId: ReportId? = null,
    val planSheet: PlanSheetUi? = null,
    val planDeadlinePicker: Boolean = false,
    val planStepDateIndex: Int? = null,
    val planWindowEdge: PlanWindowEdge? = null,
    val goals: GoalBook = GoalBook(),
) : UiState {
    val visibleTasks: List<Task>
        get() {
            val selected = selectedProject
            val tag = selectedTag
            val query = projectSearch.trim()
            return tasks.asSequence()
                .filterNot(BreakSession::isBreak)
                .filter { task ->
                    selected == null || task.normalizedProject.equals(selected, ignoreCase = true)
                }
                .filter { task ->
                    tag == null || task.normalizedTags.any { it.equals(tag, ignoreCase = true) }
                }
                .filter { task ->
                    query.isEmpty() ||
                        task.normalizedProject?.contains(query, ignoreCase = true) == true ||
                        task.normalizedTags.any { it.contains(query, ignoreCase = true) }
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

    /** Distinct user tags on [tasks], narrowed by [projectSearch]. */
    val availableTags: List<String>
        get() {
            val query = projectSearch.trim()
            return tasks.filterNot(BreakSession::isBreak)
                .flatMap(Task::normalizedTags)
                .distinctBy { it.lowercase() }
                .filter { query.isEmpty() || it.contains(query, ignoreCase = true) }
                .sortedWith(String.CASE_INSENSITIVE_ORDER)
        }
}

sealed interface TasksUiIntent : UiIntent {
    data object Create : TasksUiIntent
    data object CreateForSelectedDay : TasksUiIntent
    /** Saves an untimed task on the selected day. [fallbackTitle] is used when the name is blank. */
    data class CreateTodayQuick(val fallbackTitle: String) : TasksUiIntent
    data object StartCreateWizard : TasksUiIntent
    data object CreateNextStage : TasksUiIntent
    data object CreateSkipStage : TasksUiIntent
    data object CreateBackStage : TasksUiIntent
    data class CreateFinishWizard(val fallbackTitle: String) : TasksUiIntent
    data class Edit(val task: Task) : TasksUiIntent
    data object Update : TasksUiIntent
    data class Delete(val task: Task) : TasksUiIntent
    data class ToggleTaskSelection(val taskId: TaskId) : TasksUiIntent
    data object ClearTaskSelection : TasksUiIntent
    data object RequestDeleteSelected : TasksUiIntent
    data object ConfirmDelete : TasksUiIntent
    data object DismissEditor : TasksUiIntent
    data class ToggleEditSection(val section: EditSection) : TasksUiIntent
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
    data class ChangePlannedMinutes(val index: Int, val minutes: Int) : TasksUiIntent
    data object AddBlock : TasksUiIntent
    data class RemoveBlock(val index: Int) : TasksUiIntent
    data object ClearTime : TasksUiIntent
    data class ChangeStatus(val status: TaskStatus) : TasksUiIntent
    data class ChangeImportance(val importance: Importance) : TasksUiIntent
    data class ChangeProject(val value: String) : TasksUiIntent
    data class ChangeTagDraft(val value: String) : TasksUiIntent
    data class AddTag(val value: String) : TasksUiIntent
    data class RemoveTag(val tag: String) : TasksUiIntent
    data class FilterByProject(val project: String?) : TasksUiIntent
    data class FilterByTag(val tag: String?) : TasksUiIntent
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
    data object OpenIntervalDatePicker : TasksUiIntent
    data object DismissIntervalDatePicker : TasksUiIntent
    data class SelectIntervalDate(val date: LocalDate) : TasksUiIntent
    data class ChangeReportDraft(val text: String) : TasksUiIntent
    data class EditReport(val report: TaskReport) : TasksUiIntent
    data object SaveReport : TasksUiIntent
    data class DeleteReport(val id: ReportId) : TasksUiIntent
    data class ImportReportAttachment(val id: ReportId, val sourceUri: String) : TasksUiIntent
    data class RemoveReportAttachment(val id: ReportId) : TasksUiIntent
    data object OpenPlan : TasksUiIntent
    data object DismissPlan : TasksUiIntent
    data class ChangePlanGoal(val goal: String) : TasksUiIntent
    data object OpenPlanDeadlinePicker : TasksUiIntent
    data object DismissPlanDeadlinePicker : TasksUiIntent
    data class SelectPlanDeadline(val date: LocalDate) : TasksUiIntent
    data class ShiftPlanWindow(val deltaMinutes: Int) : TasksUiIntent
    data class NudgePlanWindowEdge(val edge: PlanWindowEdge, val deltaMinutes: Int) : TasksUiIntent
    data class SetPlanWindow(val startMinute: Int, val endMinute: Int) : TasksUiIntent
    data object OpenPlanWindowStart : TasksUiIntent
    data object OpenPlanWindowEnd : TasksUiIntent
    data object DismissPlanWindowTime : TasksUiIntent
    data object SubmitPlan : TasksUiIntent
    data class RemovePlanStep(val index: Int) : TasksUiIntent
    data class OpenPlanStepDate(val index: Int) : TasksUiIntent
    data object DismissPlanStepDate : TasksUiIntent
    data class MovePlanStep(val index: Int, val date: LocalDate) : TasksUiIntent
    data object AcceptPlan : TasksUiIntent

    /** Links the open task to an intermediate goal, or clears it when null. */
    data class LinkIntermediate(val goalId: String?) : TasksUiIntent

    /** Links one interval of the open task to an intermediate goal. */
    data class LinkInterval(val blockId: String, val goalId: String?) : TasksUiIntent

    /** Shows notes for one interval. */
    data class SelectCommentInterval(val blockId: String) : TasksUiIntent
}

/**
 * Which edge of the work window a time picker is editing.
 *
 * @since 1.1.0
 */
enum class PlanWindowEdge {
    START,
    END,
}

/**
 * Goal form before a draft exists.
 *
 * @param goal text the user typed; blank is rejected on submit
 * @param deadline local day, edited on the sheet
 * @param workStartMinute inclusive minute of the work window
 * @param workEndMinute exclusive minute of the work window
 * @param message validation or service text; null when the form is clean
 * @param submitting true while [com.example.clockplannerproject.kit.core.planner.PlannerSource.plan] is running
 * @since 1.1.0
 */
data class PlanFormState(
    val goal: String = "",
    val deadline: LocalDate,
    val workStartMinute: Int = DEFAULT_WORK_START_MINUTE,
    val workEndMinute: Int = DEFAULT_WORK_END_MINUTE,
    val message: String? = null,
    val submitting: Boolean = false,
)

/**
 * Draft shown after a successful plan response.
 *
 * @param goal goal that produced [draft]
 * @param deadline deadline that produced [draft]
 * @param workStartMinute window start used for [draft]
 * @param workEndMinute window end used for [draft]
 * @param draft verdict, minutes, steps, and narrative
 * @param submitting true while a new deadline or window is being planned
 * @param accepting true while placed steps are being written
 * @param message explanation for a rejected step move; null otherwise
 * @since 1.1.0
 */
data class PlanDraftUi(
    val goal: String,
    val deadline: LocalDate,
    val workStartMinute: Int,
    val workEndMinute: Int,
    val draft: PlanDraft,
    val submitting: Boolean = false,
    val accepting: Boolean = false,
    val message: String? = null,
) {
    /** True when «Place on the dial» may write placed steps. */
    val canPlace: Boolean get() = !submitting && !accepting
}

/**
 * Planner sheet. Absent from [TasksUiState.planSheet] when the sheet is closed.
 *
 * @since 1.1.0
 */
sealed interface PlanSheetUi {
    /**
     * @param form goal, deadline, and window
     */
    data class Form(val form: PlanFormState) : PlanSheetUi

    /**
     * @param draft verdict and steps
     */
    data class Draft(val draft: PlanDraftUi) : PlanSheetUi
}

sealed interface TasksUiEffect : UiEffect {
    /**
     * Snackbar copy from resources. The screen resolves [resId] with `stringResource`.
     *
     * @param resId failure string in the default and Russian catalogs
     */
    data class ShowMessageRes(val resId: Int) : TasksUiEffect
}
