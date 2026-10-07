package com.example.clockplannerproject.kit.core

import com.example.clockplannerproject.kit.core.layout.RestGaps

/**
 * How the day is shown. Canvas modes use [com.example.clockplannerproject.kit.render.DialRenderer];
 * [LIST] is Compose, not Canvas.
 *
 * @since 0.3.0
 */
enum class ViewMode {
    DIAL,
    PIZZA,
    PETALS,
    LIST,
}

/**
 * Tasks for [ViewMode.LIST]. [hideCompleted] is a **filter** only: DONE rows
 * drop out, remaining rows keep civil times. There is **no leftover packing
 * and no 360° CompactRemaining reflow** in list mode.
 *
 * @since 0.3.0
 */
fun visibleListTasks(
    tasks: List<Task>,
    hideCompleted: Boolean,
): List<Task> {
    val real = tasks.filterNot(RestGaps::isRest)
    return if (hideCompleted) {
        real.filter { it.status != TaskStatus.DONE }
    } else {
        real
    }
}
