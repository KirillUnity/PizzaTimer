package com.example.clockplannerproject.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

private val Context.sampleSeedDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "timedial_sample_seed",
)

/**
 * DataStore [SampleSeedFlagStore]. One boolean in `timedial_sample_seed`.
 *
 * @param dataStore preferences file that holds [SampleSeedKeys.HANDLED]
 * @since 0.6.0
 */
class DataStoreSampleSeedFlagStore(
    private val dataStore: DataStore<Preferences>,
) : SampleSeedFlagStore {

    override suspend fun isHandled(): Boolean =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .first()
            .handledFlag()

    override suspend fun markHandled() {
        dataStore.edit { prefs ->
            prefs[SampleSeedKeys.HANDLED] = true
        }
    }

    companion object {
        /**
         * @param context application context that owns the preferences file
         * @return store bound to `timedial_sample_seed`
         * @since 0.6.0
         */
        fun create(context: Context): DataStoreSampleSeedFlagStore =
            DataStoreSampleSeedFlagStore(context.sampleSeedDataStore)
    }
}

private object SampleSeedKeys {
    val HANDLED = booleanPreferencesKey("sample_day_handled")
}

private fun Preferences.handledFlag(): Boolean =
    try {
        this[SampleSeedKeys.HANDLED] == true
    } catch (_: ClassCastException) {
        false
    }
