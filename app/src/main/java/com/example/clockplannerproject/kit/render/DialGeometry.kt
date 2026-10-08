package com.example.clockplannerproject.kit.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.clipTaskToHalf
import com.example.clockplannerproject.kit.core.config.GeometryConfig
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlin.math.min

data class RingLayout(
    val center: Offset,
    val diameter: Float,
    val topLeft: Offset,
    val arcSize: Size,
    val thickness: Float,
    val midRadius: Float,
    val innerRadius: Float,
    val outerRadius: Float,
)

data class PreparedSector(
    val canvasStartDeg: Float,
    val canvasMidDeg: Float,
    val sweepDeg: Float,
    val color: Color,
    val title: String,
    val labelArgb: Int,
    val isSelected: Boolean,
)

fun layoutRing(canvasSize: Size, geometry: GeometryConfig): RingLayout {
    val side = min(canvasSize.width, canvasSize.height)
    val inset = side * geometry.outerInsetFraction
    val diameter = (side - inset * 2f).coerceAtLeast(1f)
    val left = (canvasSize.width - diameter) / 2f
    val top = (canvasSize.height - diameter) / 2f
    val thickness = (diameter * geometry.ringThicknessFraction).coerceAtLeast(1f)
    val radius = diameter / 2f
    return RingLayout(
        center = Offset(left + radius, top + radius),
        diameter = diameter,
        topLeft = Offset(left, top),
        arcSize = Size(diameter, diameter),
        thickness = thickness,
        midRadius = radius - thickness / 2f,
        innerRadius = (radius - thickness).coerceAtLeast(0f),
        outerRadius = radius,
    )
}

/**
 * Minutes a laid-out slice occupies. Exclusive `1440` is the end of PM,
 * not a wrap. Civil overnight (`end < start`) uses [TimeMath.durationMinutes].
 *
 * @since 0.2.0
 */
fun layoutDurationMinutes(startMinute: Int, endMinute: Int): Int =
    if (endMinute >= startMinute) {
        (endMinute - startMinute).coerceAtLeast(0)
    } else {
        TimeMath.durationMinutes(startMinute, endMinute)
    }

/**
 * Paint long sectors first so shorter overlaps sit on top — same rule as hit-test.
 *
 * @since 0.2.0
 */
fun paintOrderedTasks(
    tasks: List<Task>,
    selectedTaskId: TaskId? = null,
): List<Task> {
    val ordered = tasks.sortedWith(
        compareByDescending<Task> {
            layoutDurationMinutes(it.startMinute, it.endMinute)
        }.thenBy { it.id.value },
    )
    if (selectedTaskId == null) return ordered
    val (selected, rest) = ordered.partition { it.id == selectedTaskId }
    return rest + selected
}

/**
 * Layout sectors for one 12-hour face. Overnight work is two arcs via
 * [clipTaskToHalf]. Draw order is [paintOrderedTasks].
 *
 * @since 0.1.0
 */
fun prepareSectors(
    tasks: List<Task>,
    colors: List<Color>,
    half: DialHalf,
    anchorMinute: Float,
    userRotationOffsetDeg: Float,
    selectedTaskId: TaskId? = null,
): List<PreparedSector> {
    val colorByTask = tasks.mapIndexed { index, task ->
        task.id to colors.getOrElse(index) { Color.Gray }
    }.toMap()
    return paintOrderedTasks(tasks, selectedTaskId).flatMap { task ->
        val color = colorByTask[task.id] ?: Color.Gray
        clipTaskToHalf(task, half).map { slice ->
            val sweep = TimeMath.sliceSweepDeg(slice.startMinute, slice.endMinute)
            val visualStart = TimeMath.visualHalfAngle(
                minuteOfDay = slice.startMinute.toFloat(),
                anchorMinute = anchorMinute,
                half = half,
                userRotationOffsetDeg = userRotationOffsetDeg,
            )
            val visualMid = TimeMath.visualHalfAngle(
                minuteOfDay = (slice.startMinute + slice.endMinute) / 2f,
                anchorMinute = anchorMinute,
                half = half,
                userRotationOffsetDeg = userRotationOffsetDeg,
            )
            PreparedSector(
                canvasStartDeg = TimeMath.toCanvasAngle(visualStart),
                canvasMidDeg = TimeMath.toCanvasAngle(visualMid),
                sweepDeg = sweep,
                color = color,
                title = task.title,
                labelArgb = contrastingLabelArgb(
                    color.toArgb().toLong() and 0xFFFFFFFFL,
                ).toInt(),
                isSelected = task.id == selectedTaskId,
            )
        }
    }
}

fun sectorColor(task: Task, completed: Color, fallback: Color): Color =
    if (task.status == TaskStatus.DONE) completed else fallback
