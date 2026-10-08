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
) : UiState

sealed interface ReportsUiIntent : UiIntent {
    data class ChangeDraft(val text: String) : ReportsUiIntent
    data class Edit(val report: TaskReport) : ReportsUiIntent
    data object Save : ReportsUiIntent
    data class Delete(val id: ReportId) : ReportsUiIntent
}

sealed interface ReportsUiEffect : UiEffect {
    data class ShowMessage(val message: String) : ReportsUiEffect
}
