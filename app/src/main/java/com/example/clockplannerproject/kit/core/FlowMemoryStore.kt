package com.example.clockplannerproject.kit.core

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Pause note and mood for one task.
 *
 * @param text trimmed note, never blank
 * @param mood focus chosen on the pause slip
 * @since 1.2.0
 */
data class PauseCheckpoint(
    val text: String,
    val mood: PauseMood,
)

/**
 * Device snapshot of pause notes and which local dates show the day-close card.
 *
 * A date shows the card when it is in [closedDates] and not in [dismissedDates].
 *
 * @since 1.2.0
 */
data class FlowMemorySnapshot(
    val checkpoints: Map<TaskId, PauseCheckpoint> = emptyMap(),
    val closedDates: Set<LocalDate> = emptySet(),
    val dismissedDates: Set<LocalDate> = emptySet(),
) {
    /**
     * @return the saved note for [taskId], or null when none is stored
     */
    fun checkpoint(taskId: TaskId): PauseCheckpoint? = checkpoints[taskId]

    /**
     * @return true when [date] was closed and the card has not been dismissed
     */
    fun isDayCloseVisible(date: LocalDate): Boolean =
        date in closedDates && date !in dismissedDates
}

/**
 * Pause notes and day-close flags that survive a new process.
 *
 * A blank [saveCheckpoint] text is ignored so an empty slip cannot erase a note.
 * Preferences, not a Room table.
 *
 * @since 1.2.0
 */
interface FlowMemoryStore {
    /** @return the latest snapshot, including an empty one when nothing is stored */
    fun observe(): Flow<FlowMemorySnapshot>

    /**
     * Stores [text] and [mood] for [taskId].
     * Whitespace-only [text] leaves the previous note in place.
     */
    suspend fun saveCheckpoint(taskId: TaskId, text: String, mood: PauseMood)

    /** Removes the note for [taskId]. Other tasks stay. */
    suspend fun clearCheckpoint(taskId: TaskId)

    /**
     * Remembers that the user closed [date] and clears a previous dismissal
     * of that same date.
     */
    suspend fun markDayClosed(date: LocalDate)

    /** Hides the day-close card for [date] until [markDayClosed] runs again. */
    suspend fun dismissDayClose(date: LocalDate)
}
