package com.example.clockplannerproject.kit.render

import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.resizableSlices
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlin.math.abs

/**
 * Visual clock degrees of the trailing edge of [task] on [half], or `null`
 * if the task does not appear on this face.
 *
 * @since 0.2.0
 */
fun visualSliceEndDeg(
    task: Task,
    half: DialHalf,
    anchorMinute: Float,
    userRotationOffsetDeg: Float,
    nowMinute: Int = 0,
): Float? {
    val slice = resizableSlices(task, half, nowMinute).lastOrNull() ?: return null
    val start = TimeMath.visualHalfAngle(
        minuteOfDay = slice.startMinute.toFloat(),
        anchorMinute = anchorMinute,
        half = half,
        userRotationOffsetDeg = userRotationOffsetDeg,
    )
    return start + TimeMath.sliceSweepDeg(slice.startMinute, slice.endMinute)
}

/**
 * Whether [pointerClockDeg] is close enough to the sector end to start a resize
 * without waiting for a long-press.
 *
 * @since 0.2.0
 */
fun isNearResizeHandle(
    pointerClockDeg: Float,
    task: Task,
    half: DialHalf,
    anchorMinute: Float,
    userRotationOffsetDeg: Float,
    slopDeg: Float,
    nowMinute: Int = 0,
): Boolean {
    if (task.blocks.any { it.isOpen }) return false
    val slices = resizableSlices(task, half, nowMinute)
    if (slices.isEmpty()) return false
    return slices.any { slice ->
        val start = TimeMath.visualHalfAngle(
            minuteOfDay = slice.startMinute.toFloat(),
            anchorMinute = anchorMinute,
            half = half,
            userRotationOffsetDeg = userRotationOffsetDeg,
        )
        val endDeg = start + TimeMath.sliceSweepDeg(slice.startMinute, slice.endMinute)
        abs(TimeMath.signedDeltaDegrees(pointerClockDeg, endDeg)) <= slopDeg
    }
}
