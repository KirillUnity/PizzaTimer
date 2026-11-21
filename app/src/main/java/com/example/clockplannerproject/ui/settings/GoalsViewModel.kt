package com.example.clockplannerproject.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clockplannerproject.kit.core.GoalBook
import com.example.clockplannerproject.kit.core.GoalBookStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Goal list outside a session. The dial does not read this state.
 *
 * @since 1.2.0
 */
class GoalsViewModel(
    private val store: GoalBookStore,
) : ViewModel() {
    private val _book = MutableStateFlow(GoalBook())
    val book: StateFlow<GoalBook> = _book.asStateFlow()

    init {
        viewModelScope.launch {
            store.observe().collect { _book.value = it }
        }
    }

    /** Replaces the single main goal. */
    fun setMain(title: String) {
        viewModelScope.launch { store.save(_book.value.withMain(title)) }
    }

    /** Adds one intermediate goal. */
    fun addIntermediate(title: String) {
        viewModelScope.launch { store.save(_book.value.addIntermediate(title)) }
    }
}
