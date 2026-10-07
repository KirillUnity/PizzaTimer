package com.example.clockplannerproject.data

import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import kotlinx.datetime.LocalDate

internal const val TAGS_SEPARATOR: Char = '\u001F'

internal fun TaskEntity.toDomain(blocks: List<TimeBlockEntity>): Task = Task(
    id = TaskId(id),
    title = title,
    description = description,
    colorArgb = colorArgb,
    blocks = blocks.sortedBy { it.sortIndex }.map { it.toDomain() },
    status = TaskStatus.valueOf(status),
    date = LocalDate.parse(dateIso),
    importance = runCatching { Importance.valueOf(importance) }.getOrDefault(Importance.MEDIUM),
    tags = decodeTags(tagsCsv),
)

internal fun TaskWithBlocks.toDomain(): Task = task.toDomain(blocks)

internal fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id.value,
    title = title,
    description = description,
    colorArgb = colorArgb,
    status = status.name,
    dateIso = date.toString(),
    importance = importance.name,
    tagsCsv = encodeTags(tags),
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

internal fun encodeTags(tags: List<String>): String =
    tags.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(TAGS_SEPARATOR.toString())

internal fun decodeTags(csv: String): List<String> =
    if (csv.isEmpty()) emptyList() else csv.split(TAGS_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
