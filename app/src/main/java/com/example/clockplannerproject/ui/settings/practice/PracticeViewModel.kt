package com.example.clockplannerproject.ui.settings.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TimeProvider
import com.example.clockplannerproject.kit.core.mvi.UiState
import com.example.clockplannerproject.kit.core.practice.PracticeProgressStore
import com.example.clockplannerproject.kit.core.practice.PracticeSkill
import com.example.clockplannerproject.kit.core.practice.WeekClosedMinutes
import com.example.clockplannerproject.kit.core.practice.WeekDayMinutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Practice hub state: which crafts are tried, and this week's closed minutes.
 *
 * Lessons are local to the screen. This model does not recompute dial angles.
 *
 * @since 1.3.0
 */
class PracticeViewModel(
    private val practice: PracticeProgressStore,
    private val taskRepository: TaskRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(PracticeUiState())
    val state: StateFlow<PracticeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                practice.observe(),
                taskRepository.observeAll(),
                timeProvider.observeTime()
                    .map { timeProvider.today() }
                    .distinctUntilChanged(),
            ) { tried, tasks, today ->
                PracticeUiState(
                    tried = tried,
                    week = WeekClosedMinutes.columns(today, tasks),
                )
            }.collect { next -> _state.value = next }
        }
    }
}

/**
 * @property tried skills marked on the device
 * @property week Monday through Sunday, including days with no closed minutes
 * @since 1.3.0
 */
data class PracticeUiState(
    val tried: Set<PracticeSkill> = emptySet(),
    val week: List<WeekDayMinutes> = emptyList(),
) : UiState {
    val triedCount: Int get() = tried.size
    val weekClosedMinutes: Int get() = week.sumOf { it.closedMinutes }
}
