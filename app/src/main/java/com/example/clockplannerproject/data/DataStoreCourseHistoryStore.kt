package com.example.clockplannerproject.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.clockplannerproject.kit.core.BuiltInPractice
import com.example.clockplannerproject.kit.core.CourseHistory
import com.example.clockplannerproject.kit.core.CourseHistoryStore
import com.example.clockplannerproject.kit.core.PracticePass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.courseDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "timedial_course",
)

/**
 * DataStore history for the built-in course. One line per pass. Nothing is uploaded.
 *
 * @since 1.2.0
 */
class DataStoreCourseHistoryStore(
    private val dataStore: DataStore<Preferences>,
) : CourseHistoryStore {

    override fun observe(): Flow<List<PracticePass>> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { prefs -> decode(prefs[ROWS].orEmpty()) }

    override suspend fun append(pass: PracticePass) {
        val current = observe().first()
        val next = CourseHistory.append(current, pass.practice, pass.atEpochMillis)
        dataStore.edit { prefs ->
            prefs[ROWS] = next.joinToString("\n") { "${it.practice.name}\t${it.atEpochMillis}" }
        }
    }

    companion object {
        private val ROWS = stringPreferencesKey("passes")

        fun create(context: Context): DataStoreCourseHistoryStore =
            DataStoreCourseHistoryStore(context.courseDataStore)

        private fun decode(raw: String): List<PracticePass> =
            raw.lineSequence().mapNotNull { line ->
                val split = line.indexOf('\t')
                if (split <= 0) return@mapNotNull null
                val practice = BuiltInPractice.entries.firstOrNull {
                    it.name == line.substring(0, split)
                } ?: return@mapNotNull null
                val at = line.substring(split + 1).toLongOrNull() ?: return@mapNotNull null
                PracticePass(practice, at)
            }.toList()
    }
}
