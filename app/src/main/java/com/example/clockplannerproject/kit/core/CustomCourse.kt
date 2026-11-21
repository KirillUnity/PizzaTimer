package com.example.clockplannerproject.kit.core

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * A user-titled course stored on the device. Not a published lesson.
 *
 * @param id stable id
 * @param title trimmed name shown in the list
 * @since 1.3.0
 */
@Immutable
data class CustomCourse(
    val id: String,
    val title: String,
)

/**
 * Local custom-course rows. Blank titles are ignored.
 *
 * @since 1.3.0
 */
object CustomCourses {
    /**
     * Appends [title] when it is not blank.
     */
    fun add(existing: List<CustomCourse>, title: String): List<CustomCourse> {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return existing
        return existing + CustomCourse(id = UUID.randomUUID().toString(), title = trimmed)
    }
}

/**
 * On-device custom course titles.
 *
 * @since 1.3.0
 */
interface CustomCourseStore {
    fun observe(): Flow<List<CustomCourse>>

    suspend fun add(title: String)
}
