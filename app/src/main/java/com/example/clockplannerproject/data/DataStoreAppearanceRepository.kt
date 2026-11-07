package com.example.clockplannerproject.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.clockplannerproject.kit.core.Appearance
import com.example.clockplannerproject.kit.core.AppearanceCodec
import com.example.clockplannerproject.kit.core.AppearanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.appearanceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "timedial_appearance",
)

/**
 * DataStore-backed [AppearanceRepository].
 *
 * Only known keys are read. A wrong type or an unknown enum name falls back
 * to the default instead of crashing. This is preferences, not a Room migration.
 *
 * @since 1.0.0
 */
class DataStoreAppearanceRepository(
    private val dataStore: DataStore<Preferences>,
) : AppearanceRepository {

    override fun observeAppearance(): Flow<Appearance> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error
            }
            .map { prefs -> prefs.toAppearance() }

    override suspend fun save(appearance: Appearance) {
        val encoded = AppearanceCodec.encode(appearance)
        dataStore.edit { prefs ->
            prefs[Keys.VIEW_MODE] = encoded.viewMode
            prefs[Keys.ACCENT] = encoded.accentArgb
            prefs[Keys.COMPLETED] = encoded.completedArgb
            prefs[Keys.BACKGROUND_KIND] = encoded.backgroundKind
            prefs[Keys.PRESET] = encoded.presetName
            prefs[Keys.COLOR] = encoded.colorArgb
            prefs[Keys.USER_URI] = encoded.userUri
        }
    }

    override fun observeOnboardingSeen(): Flow<Boolean> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error
            }
            .map { prefs -> prefs.readBoolean(Keys.ONBOARDING_SEEN) == true }

    override suspend fun setOnboardingSeen(seen: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.ONBOARDING_SEEN] = seen }
    }

    companion object {
        fun create(context: Context): DataStoreAppearanceRepository =
            DataStoreAppearanceRepository(context.appearanceDataStore)
    }
}

private fun Preferences.toAppearance(): Appearance = AppearanceCodec.decode(
    viewMode = readString(Keys.VIEW_MODE),
    accentArgb = readLong(Keys.ACCENT),
    completedArgb = readLong(Keys.COMPLETED),
    backgroundKind = readString(Keys.BACKGROUND_KIND),
    presetName = readString(Keys.PRESET),
    colorArgb = readLong(Keys.COLOR),
    userUri = readString(Keys.USER_URI),
)

private fun Preferences.readString(key: Preferences.Key<String>): String? =
    try {
        this[key]
    } catch (_: ClassCastException) {
        null
    }

private fun Preferences.readLong(key: Preferences.Key<Long>): Long? =
    try {
        this[key]
    } catch (_: ClassCastException) {
        null
    }

private fun Preferences.readBoolean(key: Preferences.Key<Boolean>): Boolean? =
    try {
        this[key]
    } catch (_: ClassCastException) {
        null
    }

private object Keys {
    val VIEW_MODE = stringPreferencesKey("view_mode")
    val ACCENT = longPreferencesKey("accent_argb")
    val COMPLETED = longPreferencesKey("completed_argb")
    val BACKGROUND_KIND = stringPreferencesKey("background_kind")
    val PRESET = stringPreferencesKey("background_preset")
    val COLOR = longPreferencesKey("background_color")
    val USER_URI = stringPreferencesKey("background_user_uri")
    val ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
}
