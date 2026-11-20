package com.example.clockplannerproject.data

import com.example.clockplannerproject.BuildConfig
import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TimeProvider
import kotlinx.coroutines.flow.first

/**
 * Inserts one debug sample day when this install has never handled it and
 * the task database is completely empty. Release builds do nothing.
 *
 * @param taskRepository tasks already stored on this device
 * @param timeProvider local today used as the sample date
 * @param sampleSeedFlagStore install flag outside the task database
 * @param seedEnabled false in release; tests can force either path
 * @since 0.1.0
 */
class SampleDaySeeder(
    private val taskRepository: TaskRepository,
    private val timeProvider: TimeProvider,
    private val sampleSeedFlagStore: SampleSeedFlagStore,
    private val seedEnabled: Boolean = BuildConfig.DEBUG,
) {
    /**
     * Writes [SampleTasks.typicalDay] for local today at most once per install.
     * When any task already exists, only the install flag is set.
     * Returns immediately when [seedEnabled] is false, without reading or writing.
     */
    suspend fun seedIfEmpty() {
        if (!seedEnabled) return
        if (sampleSeedFlagStore.isHandled()) return
        if (taskRepository.observeAll().first().isNotEmpty()) {
            sampleSeedFlagStore.markHandled()
            return
        }
        val date = timeProvider.today()
        SampleTasks.typicalDay(date).forEach { taskRepository.upsert(it) }
        sampleSeedFlagStore.markHandled()
    }
}
