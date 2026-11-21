package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.CustomCourse
import com.example.clockplannerproject.kit.core.CustomCourseStore
import com.example.clockplannerproject.kit.core.CustomCourses
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [CustomCourseStore] for JVM tests.
 *
 * @since 1.3.0
 */
class InMemoryCustomCourseStore(
    initial: List<CustomCourse> = emptyList(),
) : CustomCourseStore {
    private val rows = MutableStateFlow(initial)

    override fun observe(): Flow<List<CustomCourse>> = rows

    override suspend fun add(title: String) {
        rows.value = CustomCourses.add(rows.value, title)
    }

    fun snapshot(): List<CustomCourse> = rows.value
}
