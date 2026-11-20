package com.example.clockplannerproject.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clockplannerproject.data.DialPhotoSelection
import com.example.clockplannerproject.kit.core.Appearance
import com.example.clockplannerproject.kit.core.AppearanceRepository
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Appearance settings. Values come from [AppearanceRepository] (DataStore).
 *
 * @since 1.0.0
 */
class SettingsViewModel(
    private val appearanceRepository: AppearanceRepository,
    private val photos: DialPhotoSelection,
) : ViewModel() {

    private val _appearance = MutableStateFlow(Appearance.Default)
    val appearance: StateFlow<Appearance> = _appearance.asStateFlow()

    private val _onboardingSeen = MutableStateFlow<Boolean?>(null)
    val onboardingSeen: StateFlow<Boolean?> = _onboardingSeen.asStateFlow()

    init {
        viewModelScope.launch {
            appearanceRepository.observeAppearance().collect { _appearance.value = it }
        }
        viewModelScope.launch {
            appearanceRepository.observeOnboardingSeen().collect { _onboardingSeen.value = it }
        }
    }

    /**
     * @param next full appearance to persist, including view mode and background
     */
    fun save(next: Appearance) {
        val current = _appearance.value
        _appearance.value = next
        viewModelScope.launch(Dispatchers.IO) {
            photos.apply(current, next)
        }
    }

    /**
     * Copies [open]'s stream into the app files directory and stores that path.
     *
     * A failed read leaves the previous background in place.
     *
     * @param open opens the picker stream; closed by the copy
     */
    fun importPhoto(open: () -> InputStream) {
        val current = _appearance.value
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val next = photos.choose(current, open())
                _appearance.value = next
            } catch (error: Exception) {
                Log.w(TAG, "Could not copy dial photo", error)
            }
        }
    }

    /** Persists that the introduction was skipped or finished. */
    fun markOnboardingSeen() {
        _onboardingSeen.value = true
        viewModelScope.launch { appearanceRepository.setOnboardingSeen(true) }
    }

    private companion object {
        const val TAG: String = "SettingsViewModel"
    }
}
