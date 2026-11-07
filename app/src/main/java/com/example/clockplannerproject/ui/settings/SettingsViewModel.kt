package com.example.clockplannerproject.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clockplannerproject.kit.core.Appearance
import com.example.clockplannerproject.kit.core.AppearanceRepository
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
        _appearance.value = next
        viewModelScope.launch { appearanceRepository.save(next) }
    }

    /** Persists that the introduction was skipped or finished. */
    fun markOnboardingSeen() {
        _onboardingSeen.value = true
        viewModelScope.launch { appearanceRepository.setOnboardingSeen(true) }
    }
}
