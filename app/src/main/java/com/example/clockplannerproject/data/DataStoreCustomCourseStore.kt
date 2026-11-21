package com.example.clockplannerproject.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.clockplannerproject.kit.core.CustomCourse
import com.example.clockplannerproject.kit.core.CustomCourseStore
import com.example.clockplannerproject.kit.core.CustomCourses
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.customCourseDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "timedial_custom_courses",
)

/**
 * DataStore titles for personal courses. Nothing is uploaded.
 *
 * @since 1.3.0
 */
class DataStoreCustomCourseStore(
    private val dataStore: DataStore<Preferences>,
) : CustomCourseStore {

    override fun observe(): Flow<List<CustomCourse>> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { prefs -> decode(prefs[ROWS].orEmpty()) }

    override suspend fun add(title: String) {
        val current = observe().first()
        val next = CustomCourses.add(current, title)
        dataStore.edit { prefs ->
            prefs[ROWS] = next.joinToString("\n") { "${it.id}\t${it.title}" }
        }
    }

    companion object {
        private val ROWS = stringPreferencesKey("courses")

        fun create(context: Context): DataStoreCustomCourseStore =
            DataStoreCustomCourseStore(context.customCourseDataStore)

        private fun decode(raw: String): List<CustomCourse> =
            raw.lineSequence().mapNotNull { line ->
                val split = line.indexOf('\t')
                if (split <= 0) return@mapNotNull null
                val id = line.substring(0, split)
                val title = line.substring(split + 1).trim()
                if (title.isEmpty()) return@mapNotNull null
                CustomCourse(id = id, title = title)
            }.toList()
    }
}
