package com.example.clockplannerproject.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.clockplannerproject.kit.core.planner.AcceptedPlan
import com.example.clockplannerproject.kit.core.planner.AcceptedPlanCodec
import com.example.clockplannerproject.kit.core.planner.PlannerPlanStore
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.plannerDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "timedial_planner",
)

/**
 * DataStore [PlannerPlanStore]. The device key is created on first read.
 * Accepted plans stay in preferences so Room's schema does not change.
 *
 * @param dataStore preferences file `timedial_planner`
 * @param newDeviceKey id factory used only when no key is stored yet
 * @since 1.1.0
 */
class DataStorePlannerPlanStore(
    private val dataStore: DataStore<Preferences>,
    private val newDeviceKey: () -> String = { UUID.randomUUID().toString() },
) : PlannerPlanStore {

    override suspend fun deviceKey(): String {
        val existing = dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .first()
            .plannerString(PlannerPreferenceKeys.DEVICE_KEY)
        if (!existing.isNullOrBlank()) return existing
        var stored = newDeviceKey()
        dataStore.edit { prefs ->
            val current = prefs.plannerString(PlannerPreferenceKeys.DEVICE_KEY)
            if (current.isNullOrBlank()) {
                prefs[PlannerPreferenceKeys.DEVICE_KEY] = stored
            } else {
                stored = current
            }
        }
        return stored
    }

    override fun observeAcceptedPlans(): Flow<List<AcceptedPlan>> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { prefs ->
                AcceptedPlanCodec.decode(prefs.plannerString(PlannerPreferenceKeys.ACCEPTED_PLANS).orEmpty())
            }

    override suspend fun acceptedPlans(): List<AcceptedPlan> = observeAcceptedPlans().first()

    override suspend fun saveAcceptedPlan(plan: AcceptedPlan) {
        dataStore.edit { prefs ->
            val current = AcceptedPlanCodec.decode(
                prefs.plannerString(PlannerPreferenceKeys.ACCEPTED_PLANS).orEmpty(),
            )
            prefs[PlannerPreferenceKeys.ACCEPTED_PLANS] = AcceptedPlanCodec.encode(current + plan)
        }
    }

    companion object {
        /**
         * @param context application context that owns the preferences file
         * @return store bound to `timedial_planner`
         * @since 1.1.0
         */
        fun create(context: Context): DataStorePlannerPlanStore =
            DataStorePlannerPlanStore(context.plannerDataStore)
    }
}

private fun Preferences.plannerString(key: Preferences.Key<String>): String? =
    try {
        this[key]
    } catch (_: ClassCastException) {
        null
    }

private object PlannerPreferenceKeys {
    val DEVICE_KEY = stringPreferencesKey("device_key")
    val ACCEPTED_PLANS = stringPreferencesKey("accepted_plans")
}
