package com.example.clockplannerproject.kit.core

import com.example.clockplannerproject.kit.core.time.TimeMath

/**
 * Field-level errors for the task editor. Overnight ranges are valid.
 * An empty [TimeBlock] list is valid (untimed) when the title is non-empty.
 *
 * @since 0.2.0
 */
sealed interface TaskDraftError {
    data object EmptyTitle : TaskDraftError
    data object EndEqualsStart : TaskDraftError
}

/**
 * Pure validation for create/update. Keep this off the Canvas path.
 *
 * @since 0.2.0
 */
object TaskDraftValidator {
    fun validate(
        title: String,
        blocks: List<TimeBlock>,
    ): TaskDraftError? {
        if (title.isBlank()) return TaskDraftError.EmptyTitle
        for (block in blocks) {
            val start = block.startMinute.mod(TimeMath.MINUTES_PER_DAY)
            val end = if (block.endMinute == TimeMath.MINUTES_PER_DAY) {
                TimeMath.MINUTES_PER_DAY
            } else {
                block.endMinute.mod(TimeMath.MINUTES_PER_DAY)
            }
            if (start == end) return TaskDraftError.EndEqualsStart
        }
        return null
    }

    fun validate(
        title: String,
        startMinute: Int,
        endMinute: Int,
    ): TaskDraftError? = validate(title, listOf(TimeBlock(startMinute, endMinute)))
}
