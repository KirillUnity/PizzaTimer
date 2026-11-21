package com.example.clockplannerproject.kit.core

import com.example.clockplannerproject.kit.core.time.TimeMath

/**
 * A pause drawn as its own task: one open block from the minute of the pause.
 * The work task id sits in [Task.description]. No Room column is added.
 *
 * @since 1.2.0
 */
object BreakSession {
    /** Tag that marks a break wedge. Not a user-created label. */
    const val TAG: String = "break"

    /** Sand fill, packed ARGB. Matches the paper sand swatch. */
    const val SAND_ARGB: Long = 0xFFEADBCE

    /**
     * @return true when [task] is a break wedge
     */
    fun isBreak(task: Task): Boolean =
        task.tags.any { it.equals(TAG, ignoreCase = true) }

    /**
     * @return the work task this break belongs to, or null
     */
    fun workId(task: Task): TaskId? {
        if (!isBreak(task)) return null
        val id = task.description.trim()
        if (id.isEmpty()) return null
        return TaskId(id)
    }

    /**
     * @return the open break on [tasks], or null
     */
    fun openBreak(tasks: List<Task>): Task? =
        tasks.firstOrNull { isBreak(it) && TaskTimer.hasOpenBlock(it) }

    /**
     * Break wedges whose stored work id is [taskId].
     *
     * @param taskId work task the wedges belong to
     * @param tasks day list that may include sand wedges
     */
    fun wedgesFor(taskId: TaskId, tasks: List<Task>): List<Task> =
        tasks.filter { isBreak(it) && workId(it) == taskId }

    /**
     * A new break starting at [nowMinute] for [work].
     *
     * @param work task that was just paused
     * @param nowMinute minute of the local day
     */
    fun start(work: Task, nowMinute: Int): Task {
        val date = work.date
        return Task(
            id = TaskId(java.util.UUID.randomUUID().toString()),
            title = "",
            description = work.id.value,
            colorArgb = SAND_ARGB,
            blocks = listOf(
                TimeBlock(
                    startMinute = nowMinute.mod(TimeMath.MINUTES_PER_DAY),
                    endMinute = null,
                    id = java.util.UUID.randomUUID().toString(),
                ),
            ),
            status = TaskStatus.IN_PROGRESS,
            date = date,
            tags = listOf(TAG),
        )
    }
}
