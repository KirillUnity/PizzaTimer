package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.FlowMemorySnapshot
import com.example.clockplannerproject.kit.core.FlowMemoryStore
import com.example.clockplannerproject.kit.core.PauseCheckpoint
import com.example.clockplannerproject.kit.core.PauseMood
import com.example.clockplannerproject.kit.core.TaskId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

/**
 * Process-local [FlowMemoryStore] for tests and the ViewModel default.
 *
 * @since 1.2.0
 */
class InMemoryFlowMemoryStore(
    initial: FlowMemorySnapshot = FlowMemorySnapshot(),
) : FlowMemoryStore {
    private val state = MutableStateFlow(initial)

    override fun observe(): Flow<FlowMemorySnapshot> = state.asStateFlow()

    override suspend fun saveCheckpoint(taskId: TaskId, text: String, mood: PauseMood) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        state.update { snapshot ->
            snapshot.copy(
                checkpoints = snapshot.checkpoints + (taskId to PauseCheckpoint(trimmed, mood)),
            )
        }
    }

    override suspend fun clearCheckpoint(taskId: TaskId) {
        state.update { snapshot ->
            snapshot.copy(checkpoints = snapshot.checkpoints - taskId)
        }
    }

    override suspend fun markDayClosed(date: LocalDate) {
        state.update { snapshot ->
            snapshot.copy(
                closedDates = snapshot.closedDates + date,
                dismissedDates = snapshot.dismissedDates - date,
            )
        }
    }

    override suspend fun dismissDayClose(date: LocalDate) {
        state.update { snapshot ->
            snapshot.copy(dismissedDates = snapshot.dismissedDates + date)
        }
    }
}
