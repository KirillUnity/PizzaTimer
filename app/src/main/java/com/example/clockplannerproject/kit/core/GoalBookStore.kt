package com.example.clockplannerproject.kit.core

import kotlinx.coroutines.flow.Flow

/**
 * Device store for [GoalBook]. Not a Room table.
 *
 * @since 1.2.0
 */
interface GoalBookStore {
    fun observe(): Flow<GoalBook>

    suspend fun save(book: GoalBook)
}
