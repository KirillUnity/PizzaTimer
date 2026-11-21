package com.example.clockplannerproject.ui.stats

import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskReport
import com.example.clockplannerproject.kit.core.mvi.UiEffect
import com.example.clockplannerproject.kit.core.mvi.UiIntent
import com.example.clockplannerproject.kit.core.mvi.UiState

data class ReportsUiState(
    val reports: List<TaskReport> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val draft: String = "",
    val editingId: ReportId? = null,
    val editingTaskId: TaskId? = null,
    /** True while a manual collect is waiting on [com.example.clockplannerproject.kit.core.planner.PlannerSource.report]. */
    val isCollecting: Boolean = false,
    /** Self-report rows for today. Empty day is zeros, not a warning. */
    val dayStats: com.example.clockplannerproject.kit.core.DayStateStats =
        com.example.clockplannerproject.kit.core.DayStateStats.Empty,
) : UiState

sealed interface ReportsUiIntent : UiIntent {
    data class ChangeDraft(val text: String) : ReportsUiIntent
    data class Edit(val report: TaskReport) : ReportsUiIntent
    data object Save : ReportsUiIntent
    data class Delete(val id: ReportId) : ReportsUiIntent
    data class ImportAttachment(val id: ReportId, val sourceUri: String) : ReportsUiIntent
    data class RemoveAttachment(val id: ReportId) : ReportsUiIntent

    /** Collect closed work for the local today. Does not run on a schedule. */
    data object CollectToday : ReportsUiIntent

    /** Collect the seven local days ending today, including today. */
    data object CollectWeek : ReportsUiIntent
}

sealed interface ReportsUiEffect : UiEffect {
    /**
     * Already localized planner copy. Not a repository fallback.
     *
     * @param message phrase from [com.example.clockplannerproject.kit.core.planner.PlannerPhrases]
     */
    data class ShowMessage(val message: String) : ReportsUiEffect

    /**
     * Snackbar copy from resources. The screen resolves [resId] with `stringResource`.
     *
     * @param resId failure string in the default and Russian catalogs
     */
    data class ShowMessageRes(val resId: Int) : ReportsUiEffect
}
