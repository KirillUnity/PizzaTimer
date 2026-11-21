package com.example.clockplannerproject.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.clockplannerproject.kit.core.GoalBook
import com.example.clockplannerproject.kit.core.GoalBookStore
import com.example.clockplannerproject.kit.core.IntermediateGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.goalDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "timedial_goals",
)

/**
 * DataStore-backed [GoalBookStore]. Preferences, not a Room schema change.
 *
 * @since 1.2.0
 */
class DataStoreGoalBookStore(
    private val dataStore: DataStore<Preferences>,
) : GoalBookStore {

    override fun observe(): Flow<GoalBook> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { prefs -> decode(prefs) }

    override suspend fun save(book: GoalBook) {
        dataStore.edit { prefs ->
            prefs[MAIN] = book.mainTitle
            prefs[INTERMEDIATE] = book.intermediate.joinToString("\n") { "${it.id}\t${it.title}" }
            prefs[LINKS] = book.taskLinks.entries.joinToString("\n") { "${it.key}\t${it.value}" }
        }
    }

    companion object {
        private val MAIN = stringPreferencesKey("main")
        private val INTERMEDIATE = stringPreferencesKey("intermediate")
        private val LINKS = stringPreferencesKey("links")

        /**
         * @param context application context that owns the preferences file
         */
        fun create(context: Context): DataStoreGoalBookStore =
            DataStoreGoalBookStore(context.goalDataStore)

        private fun decode(prefs: Preferences): GoalBook {
            val intermediate = prefs[INTERMEDIATE].orEmpty().lineSequence().mapNotNull { line ->
                val split = line.indexOf('\t')
                if (split <= 0) null else IntermediateGoal(
                    id = line.substring(0, split),
                    title = line.substring(split + 1),
                )
            }.toList()
            val links = prefs[LINKS].orEmpty().lineSequence().mapNotNull { line ->
                val split = line.indexOf('\t')
                if (split <= 0) null else line.substring(0, split) to line.substring(split + 1)
            }.toMap()
            return GoalBook(
                mainTitle = prefs[MAIN].orEmpty(),
                intermediate = intermediate,
                taskLinks = links,
            )
        }
    }
}
