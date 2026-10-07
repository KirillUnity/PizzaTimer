package com.example.clockplannerproject.kit.render

import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.clipTaskToHalf
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
): Float? {
    val slice = clipTaskToHalf(task, half).lastOrNull() ?: return null
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
): Boolean {
    val endDeg = visualSliceEndDeg(
        task = task,
        half = half,
        anchorMinute = anchorMinute,
        userRotationOffsetDeg = userRotationOffsetDeg,
    ) ?: return false
    return abs(TimeMath.signedDeltaDegrees(pointerClockDeg, endDeg)) <= slopDeg
}
