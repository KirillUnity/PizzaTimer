package com.example.clockplannerproject.kit.core

import kotlinx.coroutines.flow.Flow

/**
 * On-device history of [PracticePass]. Not a Room table.
 *
 * @since 1.2.0
 */
interface CourseHistoryStore {
    fun observe(): Flow<List<PracticePass>>

    suspend fun append(pass: PracticePass)
}
