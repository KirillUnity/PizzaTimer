package com.example.clockplannerproject.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.clockplannerproject.kit.core.FlowMemoryCodec
import com.example.clockplannerproject.kit.core.FlowMemorySnapshot
import com.example.clockplannerproject.kit.core.FlowMemoryStore
import com.example.clockplannerproject.kit.core.PauseCheckpoint
import com.example.clockplannerproject.kit.core.PauseMood
import com.example.clockplannerproject.kit.core.TaskId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import java.io.IOException

private val Context.flowMemoryDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "timedial_flow_memory",
)

/**
 * DataStore-backed [FlowMemoryStore].
 *
 * Checkpoint text, mood, and the day-close flag live here so a process death
 * does not drop them. This is preferences, not a Room migration.
 *
 * @since 1.2.0
 */
class DataStoreFlowMemoryStore(
    private val dataStore: DataStore<Preferences>,
) : FlowMemoryStore {

    override fun observe(): Flow<FlowMemorySnapshot> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { prefs -> prefs.toSnapshot() }

    override suspend fun saveCheckpoint(taskId: TaskId, text: String, mood: PauseMood) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val encoded = FlowMemoryCodec.encodeCheckpoint(
            taskId,
            PauseCheckpoint(trimmed, mood),
        )
        dataStore.edit { prefs ->
            val kept = prefs.readSet(FlowMemoryKeys.CHECKPOINTS)
                .filterNot { FlowMemoryCodec.sameTask(it, taskId) }
            prefs[FlowMemoryKeys.CHECKPOINTS] = (kept + encoded).toSet()
        }
    }

    override suspend fun clearCheckpoint(taskId: TaskId) {
        dataStore.edit { prefs ->
            prefs[FlowMemoryKeys.CHECKPOINTS] = prefs.readSet(FlowMemoryKeys.CHECKPOINTS)
                .filterNot { FlowMemoryCodec.sameTask(it, taskId) }
                .toSet()
        }
    }

    override suspend fun markDayClosed(date: LocalDate) {
        val encoded = FlowMemoryCodec.encodeDate(date)
        dataStore.edit { prefs ->
            prefs[FlowMemoryKeys.CLOSED_DATES] = prefs.readSet(FlowMemoryKeys.CLOSED_DATES) + encoded
            prefs[FlowMemoryKeys.DISMISSED_DATES] = prefs.readSet(FlowMemoryKeys.DISMISSED_DATES) - encoded
        }
    }

    override suspend fun dismissDayClose(date: LocalDate) {
        val encoded = FlowMemoryCodec.encodeDate(date)
        dataStore.edit { prefs ->
            prefs[FlowMemoryKeys.DISMISSED_DATES] = prefs.readSet(FlowMemoryKeys.DISMISSED_DATES) + encoded
        }
    }

    companion object {
        /**
         * @return a store on the `timedial_flow_memory` preferences file
         */
        fun create(context: Context): DataStoreFlowMemoryStore =
            DataStoreFlowMemoryStore(context.flowMemoryDataStore)
    }
}

private fun Preferences.toSnapshot(): FlowMemorySnapshot {
    val checkpoints = readSet(FlowMemoryKeys.CHECKPOINTS)
        .mapNotNull { FlowMemoryCodec.decodeCheckpoint(it) }
        .toMap()
    return FlowMemorySnapshot(
        checkpoints = checkpoints,
        closedDates = readSet(FlowMemoryKeys.CLOSED_DATES).mapNotNull { FlowMemoryCodec.decodeDate(it) }.toSet(),
        dismissedDates = readSet(FlowMemoryKeys.DISMISSED_DATES).mapNotNull { FlowMemoryCodec.decodeDate(it) }.toSet(),
    )
}

private fun Preferences.readSet(key: Preferences.Key<Set<String>>): Set<String> =
    try {
        this[key] ?: emptySet()
    } catch (_: ClassCastException) {
        emptySet()
    }

private object FlowMemoryKeys {
    val CHECKPOINTS = stringSetPreferencesKey("pause_checkpoints")
    val CLOSED_DATES = stringSetPreferencesKey("day_closed_dates")
    val DISMISSED_DATES = stringSetPreferencesKey("day_close_dismissed")
}
