package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.RecurrenceRule
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.LocalDate

internal fun TaskEntity.toDomain(blocks: List<TimeBlockEntity>): Task = Task(
    id = TaskId(id),
    title = title,
    description = description,
    colorArgb = colorArgb,
    blocks = blocks.sortedBy { it.sortIndex }.map { it.toDomain() },
    status = TaskStatus.valueOf(status),
    date = dateIso?.let(LocalDate::parse),
    importance = runCatching { Importance.valueOf(importance) }.getOrDefault(Importance.MEDIUM),
    project = project?.trim()?.takeIf { it.isNotEmpty() },
    recurrence = decodeRecurrence(recurrenceKind, weekdaysMask),
    seriesId = seriesId,
)

internal fun TaskWithBlocks.toDomain(): Task = task.toDomain(blocks)

internal fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id.value,
    title = title,
    description = description,
    colorArgb = colorArgb,
    status = status.name,
    dateIso = date?.toString(),
    importance = importance.name,
    project = normalizedProject,
    recurrenceKind = encodeRecurrenceKind(recurrence),
    weekdaysMask = encodeWeekdaysMask(recurrence),
    seriesId = seriesId,
)

internal fun Task.toBlockEntities(): List<TimeBlockEntity> =
    blocks.mapIndexed { index, block ->
        TimeBlockEntity(
            id = "${id.value}-$index",
            taskId = id.value,
            startMinute = block.startMinute,
            endMinute = block.endMinute,
            sortIndex = index,
        )
    }

internal fun TimeBlockEntity.toDomain(): TimeBlock = TimeBlock(
    startMinute = startMinute,
    endMinute = endMinute,
)

internal fun decodeRecurrence(kind: String, mask: Int): RecurrenceRule = when (kind) {
    "DAILY" -> RecurrenceRule.Daily
    "WEEKDAYS" -> {
        val days = RecurrenceRule.daysFromMask(mask)
        if (days.isEmpty()) RecurrenceRule.None else RecurrenceRule.Weekdays(days)
    }
    else -> RecurrenceRule.None
}

internal fun encodeRecurrenceKind(rule: RecurrenceRule): String = when (rule) {
    RecurrenceRule.None -> "NONE"
    RecurrenceRule.Daily -> "DAILY"
    is RecurrenceRule.Weekdays -> "WEEKDAYS"
}

internal fun encodeWeekdaysMask(rule: RecurrenceRule): Int = when (rule) {
    is RecurrenceRule.Weekdays -> RecurrenceRule.maskFor(rule.days)
    else -> 0
}
