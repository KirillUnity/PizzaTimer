package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/**
 * [TaskRepository] backed by Room. DAO calls run on [ioDispatcher].
 *
 * @since 0.1.0
 */
class RoomTaskRepository(
    private val taskDao: TaskDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : TaskRepository {
    override fun observeTasks(date: LocalDate): Flow<List<Task>> =
        taskDao.observeByDate(date.toString())
            .map { rows ->
                rows.map { it.toDomain() }.sortedBy { it.sortMinute }
            }
            .flowOn(ioDispatcher)

    override suspend fun upsert(task: Task) {
        withContext(ioDispatcher) {
            taskDao.upsertTask(task.toEntity())
            taskDao.deleteBlocks(task.id.value)
            val blocks = task.toBlockEntities()
            if (blocks.isNotEmpty()) {
                taskDao.insertBlocks(blocks)
            }
        }
    }

    override suspend fun delete(taskId: TaskId) {
        withContext(ioDispatcher) {
            taskDao.deleteBlocks(taskId.value)
            taskDao.deleteById(taskId.value)
        }
    }

    override suspend fun get(taskId: TaskId): Task? = withContext(ioDispatcher) {
        taskDao.getById(taskId.value)?.toDomain()
    }

    override suspend fun listRecurringTemplates(): List<Task> = withContext(ioDispatcher) {
        taskDao.listRecurring().map { it.toDomain() }
    }

    override suspend fun hasSeriesOnDate(seriesId: String, date: LocalDate): Boolean =
        withContext(ioDispatcher) {
            taskDao.countSeriesOnDate(seriesId, date.toString()) > 0
        }
}
