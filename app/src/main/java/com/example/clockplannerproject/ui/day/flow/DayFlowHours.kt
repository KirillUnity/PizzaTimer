package com.example.clockplannerproject.ui.day.flow

import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.time.TimeMath

/**
 * Closed civil minutes for the day-close card.
 *
 * Open timer slices are skipped. The sum is a duration, never a percent of the day.
 *
 * @since 1.1.0
 */
object DayFlowHours {
    /**
     * @param nowMinute minute of the local day, `0..1439`
     */
    fun closedMinutes(tasks: List<Task>, nowMinute: Int): Int {
        var total = 0
        for (task in tasks) {
            for (block in task.blocks) {
                if (!block.isClosed(nowMinute)) continue
                val end = block.endMinute ?: continue
                total += TimeMath.durationMinutes(block.startMinute, end)
            }
        }
        return total
    }
}
