package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.CourseHistory
import com.example.clockplannerproject.kit.core.CourseHistoryStore
import com.example.clockplannerproject.kit.core.PracticePass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [CourseHistoryStore] for previews and JVM tests.
 *
 * @since 1.2.0
 */
class InMemoryCourseHistoryStore(
    initial: List<PracticePass> = emptyList(),
) : CourseHistoryStore {
    private val rows = MutableStateFlow(initial)

    override fun observe(): Flow<List<PracticePass>> = rows

    override suspend fun append(pass: PracticePass) {
        rows.value = CourseHistory.append(rows.value, pass.practice, pass.atEpochMillis)
    }

    fun snapshot(): List<PracticePass> = rows.value
}
