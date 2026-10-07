package com.example.clockplannerproject.data.sample

import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.LocalDate

/**
 * Typical-day fixtures for Previews and debug seeding. Not written in release builds.
 *
 * @since 0.1.0
 */
object SampleTasks {
    fun typicalDay(date: LocalDate): List<Task> = listOf(
        Task(
            id = TaskId("sleep"),
            title = "Sleep",
            description = "Overnight rest",
            colorArgb = 0xFFB8A9C9,
            blocks = listOf(TimeBlock(22 * 60, 6 * 60)),
            status = TaskStatus.TODO,
            date = date,
        ),
        Task(
            id = TaskId("work"),
            title = "Deep work",
            description = "Write the ring renderer.",
            colorArgb = 0xFFA8C5A0,
            blocks = listOf(TimeBlock(9 * 60, 12 * 60)),
            status = TaskStatus.TODO,
            date = date,
            tags = listOf("focus", "code"),
        ),
        Task(
            id = TaskId("lunch"),
            title = "Lunch",
            colorArgb = 0xFFE8C4A8,
            blocks = listOf(TimeBlock(12 * 60, 13 * 60)),
            status = TaskStatus.DONE,
            date = date,
        ),
        Task(
            id = TaskId("gym"),
            title = "Gym",
            description = "Strength session",
            colorArgb = 0xFFE8B4B8,
            blocks = listOf(TimeBlock(18 * 60, 19 * 60 + 30)),
            status = TaskStatus.IN_PROGRESS,
            date = date,
            tags = listOf("sport"),
        ),
    )

    fun allDone(date: LocalDate): List<Task> =
        typicalDay(date).map { it.copy(status = TaskStatus.DONE) }

    fun overnightOnly(date: LocalDate): List<Task> = listOf(
        typicalDay(date).first { it.id.value == "sleep" },
    )

    /** Sport 08:00–09:00 and 12:00–13:00, one TaskId. */
    fun sportTwoBlocks(date: LocalDate): Task = Task(
        id = TaskId("sport"),
        title = "Sport",
        description = "Morning run and noon session",
        colorArgb = 0xFFE8B4B8,
        blocks = listOf(
            TimeBlock(8 * 60, 9 * 60),
            TimeBlock(12 * 60, 13 * 60),
        ),
        date = date,
        tags = listOf("sport"),
    )

    fun untimedPair(date: LocalDate): List<Task> = listOf(
        Task(
            id = TaskId("inbox"),
            title = "Inbox",
            colorArgb = 0xFF80CBC4,
            date = date,
            importance = Importance.HIGH,
            tags = listOf("admin"),
        ),
        Task(
            id = TaskId("read"),
            title = "Read",
            colorArgb = 0xFF90CAF9,
            date = date,
            importance = Importance.HIGH,
            tags = listOf("learn"),
        ),
    )

    fun threeImportances(date: LocalDate): List<Task> = listOf(
        Task(
            id = TaskId("high"),
            title = "High leftover",
            colorArgb = 0xFFE57373,
            date = date,
            importance = Importance.HIGH,
        ),
        Task(
            id = TaskId("medium"),
            title = "Medium leftover",
            colorArgb = 0xFFFFB74D,
            date = date,
            importance = Importance.MEDIUM,
        ),
        Task(
            id = TaskId("low"),
            title = "Low leftover",
            colorArgb = 0xFF81C784,
            date = date,
            importance = Importance.LOW,
        ),
    )

    fun mixedTimedUntimed(date: LocalDate): List<Task> =
        listOf(typicalDay(date).first { it.id.value == "work" }) + untimedPair(date)
}
