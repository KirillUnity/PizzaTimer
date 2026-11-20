package com.example.clockplannerproject.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.clockplannerproject.kit.core.practice.PracticeProgressStore
import com.example.clockplannerproject.kit.core.practice.PracticeSkill
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.practiceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "timedial_practice",
)

/**
 * DataStore-backed [PracticeProgressStore].
 *
 * Flags are preferences, not Room columns. A missing key stays untried.
 * [markTried] writes true and never writes false.
 *
 * @since 1.3.0
 */
class DataStorePracticeProgressStore(
    private val dataStore: DataStore<Preferences>,
) : PracticeProgressStore {

    override fun observe(): Flow<Set<PracticeSkill>> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { prefs ->
                buildSet {
                    PracticeSkill.entries.forEach { skill ->
                        if (prefs[key(skill)] == true) add(skill)
                    }
                }
            }

    override suspend fun markTried(skill: PracticeSkill) {
        dataStore.edit { prefs -> prefs[key(skill)] = true }
    }

    companion object {
        /**
         * @param context application context that owns the preferences file
         * @return a store on `timedial_practice`
         */
        fun create(context: Context): DataStorePracticeProgressStore =
            DataStorePracticeProgressStore(context.practiceDataStore)

        private fun key(skill: PracticeSkill) = booleanPreferencesKey("tried_${skill.name}")
    }
}
