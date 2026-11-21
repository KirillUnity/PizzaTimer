package com.example.clockplannerproject.data

import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TimeProvider
import kotlinx.coroutines.flow.first

/**
 * Removes leftover debug sample-day rows. It never inserts tasks.
 *
 * @param taskRepository tasks already stored on this device
 * @param timeProvider unused; kept so existing call sites stay valid
 * @param sampleSeedFlagStore install flag from the old insert path
 * @param purgeFixtures true in the app module so leftover Sleep/Deep work/Lunch/Gym rows go away
 * @since 0.1.0
 */
class SampleDaySeeder(
    private val taskRepository: TaskRepository,
    @Suppress("unused") private val timeProvider: TimeProvider,
    private val sampleSeedFlagStore: SampleSeedFlagStore,
    private val purgeFixtures: Boolean = false,
) {
    /**
     * Deletes [SampleTasks.fixtureIds] when [purgeFixtures] is true.
     * Does not insert a typical day.
     */
    suspend fun seedIfEmpty() {
        if (!purgeFixtures) return
        val leftover = taskRepository.observeAll().first()
            .filter { it.id in SampleTasks.fixtureIds }
        leftover.forEach { taskRepository.delete(it.id) }
        sampleSeedFlagStore.markHandled()
    }
}
