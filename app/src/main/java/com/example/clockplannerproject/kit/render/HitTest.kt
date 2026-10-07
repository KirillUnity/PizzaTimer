package com.example.clockplannerproject.kit.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.clipTaskToHalf
import com.example.clockplannerproject.kit.core.config.GeometryConfig
import com.example.clockplannerproject.kit.core.config.TimeDialConfig
import com.example.clockplannerproject.kit.core.halfAnchorMinute
import com.example.clockplannerproject.kit.core.layout.ReflowMode
import com.example.clockplannerproject.kit.core.layout.RestGaps
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Maps a tap to a real (non-rest) task on a 12-hour ring.
 * Overlaps: shortest [layoutDurationMinutes] wins; ties break by task id.
 *
 * @since 0.1.0
 */
fun hitTestTask(
    offset: Offset,
    tasks: List<Task>,
    @Suppress("UNUSED_PARAMETER") config: TimeDialConfig,
    geometry: GeometryConfig,
    anchorMinute: Float,
    userRotationOffset: Float,
    canvasSize: Size,
    half: DialHalf,
    renderer: DialRenderer = TimeDialRenderer,
): Task? {
    if (canvasSize.width <= 0f || canvasSize.height <= 0f || tasks.isEmpty()) return null
    val ring = layoutRing(canvasSize, geometry)
    val dx = offset.x - ring.center.x
    val dy = offset.y - ring.center.y
    val radius = hypot(dx, dy)
    if (!renderer.containsPointer(ring, radius)) return null
    val canvasDeg = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()
    val visualClockDeg = renderer.clockAngleForHit(radius, canvasDeg, userRotationOffset)
    return tasks
        .filterNot(RestGaps::isRest)
        .mapNotNull { task ->
            val matching = clipTaskToHalf(task, half).filter { slice ->
                val start = TimeMath.visualHalfAngle(
                    minuteOfDay = slice.startMinute.toFloat(),
                    anchorMinute = anchorMinute,
                    half = half,
                    userRotationOffsetDeg = userRotationOffset,
                )
                val sweep = TimeMath.sliceSweepDeg(slice.startMinute, slice.endMinute)
                TimeMath.containsClockAngle(visualClockDeg, start, sweep) &&
                    renderer.acceptsHitRadius(ring, radius, sweep)
            }
            val smallest = matching.minByOrNull {
                layoutDurationMinutes(it.startMinute, it.endMinute)
            } ?: return@mapNotNull null
            task to layoutDurationMinutes(smallest.startMinute, smallest.endMinute)
        }
        .minWithOrNull(
            compareBy<Pair<Task, Int>> { it.second }.thenBy { it.first.id.value },
        )
        ?.first
}

fun hitTestTask(
    offset: Offset,
    tasks: List<Task>,
    config: TimeDialConfig,
    nowMinute: Float,
    canvasSize: Size,
    half: DialHalf,
): Task? {
    val real = tasks.filterNot(RestGaps::isRest)
    val compact = config.clock.reflowMode == ReflowMode.CompactRemaining
    val anchor = if (config.clock.snapToNow && !compact) {
        halfAnchorMinute(nowMinute, half, real)
    } else {
        half.startMinute.toFloat()
    }
    val renderer = RendererFactory.canvasOrNull(config.viewMode) ?: TimeDialRenderer
    return hitTestTask(
        offset = offset,
        tasks = real,
        config = config,
        geometry = config.geometry,
        anchorMinute = anchor,
        userRotationOffset = config.interaction.userRotationOffsetDeg,
        canvasSize = canvasSize,
        half = half,
        renderer = renderer,
    )
}
