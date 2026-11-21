package com.example.clockplannerproject.ui.settings.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clockplannerproject.kit.core.BuiltInCourse
import com.example.clockplannerproject.kit.core.BuiltInPractice
import com.example.clockplannerproject.kit.core.CourseHistoryStore
import com.example.clockplannerproject.kit.core.CustomCourse
import com.example.clockplannerproject.kit.core.CustomCourseStore
import com.example.clockplannerproject.kit.core.PracticePass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Built-in course plus local custom titles.
 *
 * @since 1.2.0
 */
class CourseViewModel(
    private val history: CourseHistoryStore,
    private val customStore: CustomCourseStore,
) : ViewModel() {
    val passes: StateFlow<List<PracticePass>> = history.observe().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    private val _custom = MutableStateFlow<List<CustomCourse>>(emptyList())
    val custom: StateFlow<List<CustomCourse>> = _custom.asStateFlow()

    init {
        viewModelScope.launch {
            customStore.observe().collect { _custom.value = it }
        }
    }

    /**
     * Appends one pass when every checkpoint of [practice] is in [checked].
     */
    fun finish(practice: BuiltInPractice, checked: Set<String>) {
        val lesson = BuiltInCourse.lesson(practice)
        if (!BuiltInCourse.checkpointsMet(lesson, checked)) return
        viewModelScope.launch {
            history.append(PracticePass(practice, System.currentTimeMillis()))
        }
    }

    fun addCustom(title: String) {
        viewModelScope.launch { customStore.add(title) }
    }

    fun refresh() {
        viewModelScope.launch {
            _custom.value = customStore.observe().first()
        }
    }
}
