package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.GoalBook
import com.example.clockplannerproject.kit.core.GoalBookStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [GoalBookStore] for previews and JVM tests.
 *
 * @since 1.2.0
 */
class InMemoryGoalBookStore(
    initial: GoalBook = GoalBook(),
) : GoalBookStore {
    private val book = MutableStateFlow(initial)

    override fun observe(): Flow<GoalBook> = book

    override suspend fun save(book: GoalBook) {
        this.book.value = book
    }

    /** Current book, for tests. */
    fun snapshot(): GoalBook = book.value
}
