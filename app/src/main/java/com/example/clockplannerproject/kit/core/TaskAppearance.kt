package com.example.clockplannerproject.kit.core

/**
 * One painted row of [task]. Several civil blocks share one id and appear
 * once each, so a task with two intervals is drawn twice.
 *
 * @property blockIndex index into [Task.blocks], or null when the task is untimed
 * @since 1.1.0
 */
data class TaskAppearance(
    val task: Task,
    val blockIndex: Int?,
) {
    /**
     * The interval this row shows, or null for an untimed task.
     */
    val block: TimeBlock?
        get() = blockIndex?.let { task.blocks.getOrNull(it) }
}

/**
 * Expands each task into one appearance per block. Untimed tasks stay a single row.
 *
 * @since 1.1.0
 */
fun taskAppearances(tasks: List<Task>): List<TaskAppearance> =
    tasks.flatMap { task ->
        if (task.blocks.isEmpty()) {
            listOf(TaskAppearance(task, null))
        } else {
            task.blocks.indices.map { index -> TaskAppearance(task, index) }
        }
    }
