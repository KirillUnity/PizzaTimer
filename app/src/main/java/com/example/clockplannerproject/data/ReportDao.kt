package com.example.clockplannerproject.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Persistence for text reports. Call from [Dispatchers.IO] via the repository.
 *
 * @since 0.5.0
 */
@Dao
interface ReportDao {
    @Query("SELECT * FROM task_reports WHERE taskId = :taskId ORDER BY createdAtEpochMillis DESC")
    fun observeByTask(taskId: String): Flow<List<TaskReportEntity>>

    @Query("SELECT * FROM task_reports ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<TaskReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TaskReportEntity)

    @Query("DELETE FROM task_reports WHERE id = :id")
    suspend fun deleteById(id: String)
}
