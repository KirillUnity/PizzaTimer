package com.example.clockplannerproject.data

import com.example.clockplannerproject.BuildConfig
import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TimeProvider
import kotlinx.coroutines.flow.first

/**
 * Inserts a sample day into Room when the store is empty. No-op in release.
 *
 * @since 0.1.0
 */
class SampleDaySeeder(
    private val taskRepository: TaskRepository,
    private val timeProvider: TimeProvider,
) {
    suspend fun seedIfEmpty() {
        if (!BuildConfig.DEBUG) return
        val date = timeProvider.today()
        if (taskRepository.observeTasks(date).first().isNotEmpty()) return
        SampleTasks.typicalDay(date).forEach { taskRepository.upsert(it) }
    }
}
