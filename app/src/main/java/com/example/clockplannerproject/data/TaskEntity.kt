package com.example.clockplannerproject.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Room row for [com.example.clockplannerproject.kit.core.Task].
 *
 * [dateIso] is `YYYY-MM-DD`, or `null` for backlog tasks. [status] and
 * [importance] are enum names. [project] stores at most one normalized project.
 *
 * Civil minutes live in [TimeBlockEntity], not on this row.
 *
 * @since 0.1.0
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val colorArgb: Long,
    val status: String,
    val dateIso: String?,
    val importance: String,
    val project: String?,
    val recurrenceKind: String = "NONE",
    val weekdaysMask: Int = 0,
    val seriesId: String? = null,
)

/**
 * One civil interval belonging to [taskId].
 *
 * @since 0.3.0
 */
@Entity(
    tableName = "time_blocks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("taskId")],
)
data class TimeBlockEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val startMinute: Int,
    val endMinute: Int?,
    val sortIndex: Int,
)

/**
 * Task plus ordered blocks for Room `@Relation` queries.
 *
 * @since 0.3.0
 */
data class TaskWithBlocks(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId",
    )
    val blocks: List<TimeBlockEntity>,
)
