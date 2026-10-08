package com.example.clockplannerproject.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.ReportRepository
import com.example.clockplannerproject.kit.core.TaskReport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Reports tab: list every text note. Create/edit also lives on the task card.
 *
 * @since 0.5.0
 */
class ReportsViewModel(
    private val reportRepository: ReportRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsUiState())
    val state: StateFlow<ReportsUiState> = _state.asStateFlow()

    private val effects = Channel<ReportsUiEffect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            try {
                reportRepository.observeAllReports().collect { reports ->
                    _state.update {
                        it.copy(reports = reports, isLoading = false, errorMessage = null)
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
            }
        }
    }

    fun onIntent(intent: ReportsUiIntent) {
        when (intent) {
            is ReportsUiIntent.ChangeDraft -> _state.update { it.copy(draft = intent.text) }
            is ReportsUiIntent.Edit -> _state.update {
                it.copy(
                    editingId = intent.report.id,
                    editingTaskId = intent.report.taskId,
                    draft = intent.report.text,
                )
            }
            ReportsUiIntent.Save -> save()
            is ReportsUiIntent.Delete -> viewModelScope.launch {
                reportRepository.delete(intent.id)
            }
        }
    }

    private fun save() {
        val snapshot = _state.value
        val text = snapshot.draft.trim()
        val taskId = snapshot.editingTaskId ?: return
        if (text.isEmpty()) return
        val report = TaskReport(
            id = snapshot.editingId ?: ReportId(UUID.randomUUID().toString()),
            taskId = taskId,
            createdAtEpochMillis = snapshot.reports.find { it.id == snapshot.editingId }
                ?.createdAtEpochMillis
                ?: System.currentTimeMillis(),
            text = text,
        )
        viewModelScope.launch {
            try {
                reportRepository.upsert(report)
                _state.update { it.copy(draft = "", editingId = null, editingTaskId = null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                effects.send(
                    ReportsUiEffect.ShowMessage(error.message ?: "Could not save note"),
                )
            }
        }
    }
}
