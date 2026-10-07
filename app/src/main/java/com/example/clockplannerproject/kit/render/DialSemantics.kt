package com.example.clockplannerproject.kit.render

import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.time.TimeMath

/**
 * Spoken label for a task on the canvas (title plus civil start–end).
 *
 * @since 0.2.0
 */
fun dialTaskTalkBackLabel(task: Task): String {
    val whenLabel = if (task.isUntimed) {
        task.title
    } else {
        TimeMath.formatBlocks(task.blocks)
    }
    return "${task.title}, $whenLabel"
}
