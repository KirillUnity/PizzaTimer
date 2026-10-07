package com.example.clockplannerproject.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Persistence for tasks and time blocks. Call from [Dispatchers.IO] via the repository.
 *
 * @since 0.1.0
 */
@Dao
interface TaskDao {
    @Transaction
    @Query("SELECT * FROM tasks WHERE dateIso = :dateIso")
    fun observeByDate(dateIso: String): Flow<List<TaskWithBlocks>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(entity: TaskEntity)

    @Query("DELETE FROM time_blocks WHERE taskId = :taskId")
    suspend fun deleteBlocks(taskId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(entities: List<TimeBlockEntity>)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)
}
