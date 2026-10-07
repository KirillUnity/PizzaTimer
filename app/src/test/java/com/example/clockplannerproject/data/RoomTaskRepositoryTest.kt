package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM stand-in for in-memory Room: same [TaskDao] contract, no Robolectric SDK jars.
 * Device/emulator: [RoomTaskRepositoryInstrumentedTest].
 */
class RoomTaskRepositoryTest {

    @Test
    fun upsert_thenObserve_emitsTaskForThatDate() = runTest {
        val dao = InMemoryTaskDao()
        val repository = RoomTaskRepository(dao, Dispatchers.Unconfined)
        val date = LocalDate(2026, 10, 6)
        val task = sample(date)

        repository.upsert(task)

        val forDate = repository.observeTasks(date).first()
        assertEquals(1, forDate.size)
        assertEquals(task, forDate.single())

        val otherDay = repository.observeTasks(LocalDate(2026, 10, 7)).first()
        assertTrue(otherDay.isEmpty())
    }

    @Test
    fun upsert_replacesSameId() = runTest {
        val dao = InMemoryTaskDao()
        val repository = RoomTaskRepository(dao, Dispatchers.Unconfined)
        val date = LocalDate(2026, 10, 6)
        val original = sample(date)
        repository.upsert(original)
        repository.upsert(original.copy(title = "Deep work"))
        val forDate = repository.observeTasks(date).first()
        assertEquals(1, forDate.size)
        assertEquals("Deep work", forDate.single().title)
    }

    @Test
    fun delete_removesTask() = runTest {
        val dao = InMemoryTaskDao()
        val repository = RoomTaskRepository(dao, Dispatchers.Unconfined)
        val date = LocalDate(2026, 10, 6)
        val task = sample(date)
        repository.upsert(task)
        repository.delete(task.id)
        assertTrue(repository.observeTasks(date).first().isEmpty())
    }

    @Test
    fun upsert_untimedTask_roundTripsEmptyBlocks() = runTest {
        val dao = InMemoryTaskDao()
        val repository = RoomTaskRepository(dao, Dispatchers.Unconfined)
        val date = LocalDate(2026, 10, 6)
        val task = Task(
            id = TaskId("inbox"),
            title = "Inbox",
            colorArgb = 0xFF80CBC4,
            date = date,
            importance = Importance.HIGH,
            tags = listOf("admin", "mail"),
        )
        repository.upsert(task)
        assertEquals(task, repository.observeTasks(date).first().single())
        assertTrue(repository.observeTasks(date).first().single().isUntimed)
    }

    private fun sample(date: LocalDate) = Task(
        id = TaskId("task-1"),
        title = "Focus",
        description = "Deep work",
        colorArgb = 0xFF6750A4,
        blocks = listOf(TimeBlock(9 * 60, 12 * 60)),
        status = TaskStatus.TODO,
        date = date,
        tags = listOf("work"),
    )
}

private class InMemoryTaskDao : TaskDao {
    private val tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    private val blocks = MutableStateFlow<List<TimeBlockEntity>>(emptyList())

    override fun observeByDate(dateIso: String): Flow<List<TaskWithBlocks>> =
        combine(tasks, blocks) { taskRows, blockRows ->
            taskRows.filter { it.dateIso == dateIso }.map { entity ->
                TaskWithBlocks(
                    task = entity,
                    blocks = blockRows.filter { it.taskId == entity.id },
                )
            }
        }

    override suspend fun upsertTask(entity: TaskEntity) {
        tasks.update { current -> current.filterNot { it.id == entity.id } + entity }
    }

    override suspend fun deleteBlocks(taskId: String) {
        blocks.update { current -> current.filterNot { it.taskId == taskId } }
    }

    override suspend fun insertBlocks(entities: List<TimeBlockEntity>) {
        blocks.update { current -> current + entities }
    }

    override suspend fun deleteById(id: String) {
        tasks.update { current -> current.filterNot { it.id == id } }
        blocks.update { current -> current.filterNot { it.taskId == id } }
    }
}
