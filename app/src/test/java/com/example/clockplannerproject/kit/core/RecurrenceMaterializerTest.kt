package com.example.clockplannerproject.kit.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class RecurrenceMaterializerTest {

    private val monday = LocalDate(2026, 10, 5)
    private val tuesday = LocalDate(2026, 10, 6)
    private val saturday = LocalDate(2026, 10, 10)

    @Test
    fun daily_materializesMissingTomorrowOnly() = runTest {
        val template = Task(
            id = TaskId("daily"),
            title = "Standup",
            colorArgb = 0xFF6750A4,
            blocks = listOf(TimeBlock(9 * 60, 9 * 60 + 15)),
            date = monday,
            recurrence = RecurrenceRule.Daily,
            seriesId = "daily",
        )
        val repo = MemoryRepo(listOf(template))
        val materializer = RecurrenceMaterializer(repo) { TaskId("inst-tue") }
        materializer.ensureVisibleDay(tuesday)
        val items = repo.all()
        assertEquals(1, items.count { it.date == tuesday })
        assertEquals("Standup", items.single { it.date == tuesday }.title)
        assertEquals("daily", items.single { it.date == tuesday }.seriesId)
        assertEquals(RecurrenceRule.None, items.single { it.date == tuesday }.recurrence)
        assertEquals(1, items.count { it.date == monday })
        materializer.ensureVisibleDay(tuesday)
        assertEquals(2, repo.all().size)
    }

    @Test
    fun weekdays_skipsSaturday() = runTest {
        val template = Task(
            id = TaskId("wd"),
            title = "Office",
            colorArgb = 0xFF6750A4,
            date = monday,
            recurrence = RecurrenceRule.Weekdays(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)),
            seriesId = "wd",
        )
        val repo = MemoryRepo(listOf(template))
        RecurrenceMaterializer(repo) { TaskId("nope") }.ensureVisibleDay(saturday)
        assertEquals(1, repo.all().size)
        RecurrenceMaterializer(repo) { TaskId("fri") }.ensureVisibleDay(LocalDate(2026, 10, 9))
        assertEquals(2, repo.all().size)
    }
}

private class MemoryRepo(
    initial: List<Task>,
) : TaskRepository {
    private val items = MutableStateFlow(initial)

    fun all(): List<Task> = items.value

    override fun observeTasks(date: LocalDate): Flow<List<Task>> =
        items.map { list -> list.filter { it.date == date } }

    override fun observeUnscheduled(): Flow<List<Task>> =
        items.map { list -> list.filter { it.date == null } }

    override fun observeAll(): Flow<List<Task>> = items

    override suspend fun upsert(task: Task) {
        items.update { current -> current.filterNot { it.id == task.id } + task }
    }

    override suspend fun delete(taskId: TaskId) {
        items.update { current -> current.filterNot { it.id == taskId } }
    }

    override suspend fun get(taskId: TaskId): Task? = items.value.find { it.id == taskId }

    override suspend fun listRecurringTemplates(): List<Task> =
        items.value.filter { it.recurrence !is RecurrenceRule.None }

    override suspend fun hasSeriesOnDate(seriesId: String, date: LocalDate): Boolean =
        items.value.any { task ->
            task.date == date && (task.seriesId == seriesId || task.id.value == seriesId)
        }
}
