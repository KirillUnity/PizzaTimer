package com.example.clockplannerproject.data

import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskRepository
import com.example.clockplannerproject.kit.core.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleDaySeederTest {

    private val today = LocalDate(2026, 10, 6)

    @Test
    fun firstEmptyCall_insertsTypicalDayAndSetsFlag() = runTest {
        val repository = RecordingTasks()
        val flag = MemoryFlag()
        seeder(repository, flag).seedIfEmpty()

        val stored = repository.observeAll().first()
        assertEquals(SampleTasks.typicalDay(today).map { it.id }, stored.map { it.id })
        assertTrue(stored.all { it.date == today })
        assertTrue(flag.handled)
    }

    @Test
    fun secondEmptyCall_doesNotInsert() = runTest {
        val repository = RecordingTasks()
        val flag = MemoryFlag()
        val seeder = seeder(repository, flag)
        seeder.seedIfEmpty()
        repository.clear()
        val writesAfterClear = repository.upserts

        seeder.seedIfEmpty()

        assertTrue(repository.observeAll().first().isEmpty())
        assertEquals(writesAfterClear, repository.upserts)
        assertTrue(flag.handled)
    }

    @Test
    fun nonEmptyDatabase_setsFlagAndInsertsNothing() = runTest {
        val yesterday = today.minus(1, DateTimeUnit.DAY)
        val existing = Task(
            id = TaskId("kept"),
            title = "Kept",
            colorArgb = 0xFF112233,
            date = yesterday,
        )
        val repository = RecordingTasks(listOf(existing))
        val flag = MemoryFlag()

        seeder(repository, flag).seedIfEmpty()

        assertEquals(listOf(existing.id), repository.observeAll().first().map { it.id })
        assertEquals(0, repository.upserts)
        assertTrue(flag.handled)
    }

    @Test
    fun releaseBuild_doesNotReadOrWrite() = runTest {
        val repository = RecordingTasks()
        val flag = MemoryFlag()
        SampleDaySeeder(
            taskRepository = repository,
            timeProvider = FixedToday(today),
            sampleSeedFlagStore = flag,
            seedEnabled = false,
        ).seedIfEmpty()

        assertFalse(repository.observedAll)
        assertEquals(0, repository.upserts)
        assertFalse(flag.handled)
        assertFalse(flag.reads)
    }

    private fun seeder(repository: RecordingTasks, flag: MemoryFlag): SampleDaySeeder =
        SampleDaySeeder(
            taskRepository = repository,
            timeProvider = FixedToday(today),
            sampleSeedFlagStore = flag,
            seedEnabled = true,
        )
}

private class MemoryFlag : SampleSeedFlagStore {
    var handled = false
    var reads = false

    override suspend fun isHandled(): Boolean {
        reads = true
        return handled
    }

    override suspend fun markHandled() {
        handled = true
    }
}

private class RecordingTasks(
    initial: List<Task> = emptyList(),
) : TaskRepository {
    private val items = MutableStateFlow(initial)
    var upserts = 0
    var observedAll = false

    fun clear() {
        items.value = emptyList()
    }

    override fun observeTasks(date: LocalDate): Flow<List<Task>> =
        items.map { list -> list.filter { it.date == date } }

    override fun observeUnscheduled(): Flow<List<Task>> =
        items.map { list -> list.filter { it.date == null } }

    override fun observeAll(): Flow<List<Task>> {
        observedAll = true
        return items
    }

    override suspend fun upsert(task: Task) {
        upserts += 1
        items.update { current -> current.filterNot { it.id == task.id } + task }
    }

    override suspend fun delete(taskId: TaskId) {
        items.update { current -> current.filterNot { it.id == taskId } }
    }

    override suspend fun get(taskId: TaskId): Task? = items.value.find { it.id == taskId }

    override suspend fun listRecurringTemplates(): List<Task> = emptyList()

    override suspend fun hasSeriesOnDate(seriesId: String, date: LocalDate): Boolean = false
}

private class FixedToday(private val date: LocalDate) : TimeProvider {
    override fun now(): LocalTime = LocalTime(9, 30)
    override fun today(): LocalDate = date
    override fun observeTime(): Flow<LocalTime> = MutableStateFlow(now())
}
