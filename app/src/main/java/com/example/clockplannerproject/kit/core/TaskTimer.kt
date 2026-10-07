package com.example.clockplannerproject.kit.core

import com.example.clockplannerproject.kit.core.time.TimeMath

/**
 * Append-only Start / Pause. Closed [TimeBlock]s are never rewritten.
 *
 * @since 0.4.0
 */
object TaskTimer {
    fun hasOpenBlock(task: Task): Boolean = task.blocks.any { it.isOpen }

    fun canStart(task: Task): Boolean = !hasOpenBlock(task)

    /**
     * Appends `[nowMinute, open)` and sets [TaskStatus.IN_PROGRESS].
     * No-op when an open block already exists.
     */
    fun start(task: Task, nowMinute: Int): Task {
        if (!canStart(task)) return task
        val start = nowMinute.mod(TimeMath.MINUTES_PER_DAY)
        return task.copy(
            blocks = task.blocks + TimeBlock(startMinute = start, endMinute = null),
            status = TaskStatus.IN_PROGRESS,
        )
    }

    /**
     * Closes the open block at [nowMinute]. Older closed intervals stay
     * byte-for-byte the same. Status returns to [TaskStatus.TODO] when idle.
     */
    fun pause(task: Task, nowMinute: Int): Task {
        val end = nowMinute.mod(TimeMath.MINUTES_PER_DAY)
        var changed = false
        val blocks = task.blocks.map { block ->
            if (!block.isOpen) {
                block
            } else {
                changed = true
                block.copy(endMinute = if (end == 0 && block.startMinute != 0) {
                    TimeMath.MINUTES_PER_DAY
                } else {
                    end
                })
            }
        }
        if (!changed) return task
        return task.copy(blocks = blocks, status = TaskStatus.TODO)
    }
}
