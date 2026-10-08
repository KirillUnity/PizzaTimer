package com.example.clockplannerproject.kit.compose

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.config.GeometryConfig
import com.example.clockplannerproject.kit.core.time.TaskResize
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.kit.render.layoutRing
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.atan2

private enum class DialGestureKind { Tap, Resize, Cancel }

/**
 * One-finger tap / long-press resize. Scene rotate is **two-finger twist**
 * ([detectTwoFingerRotate]) so a single drag no longer spins the day.
 *
 * @since 0.2.0
 */
internal suspend fun PointerInputScope.detectDialGestures(
    canvasSize: Size,
    geometry: GeometryConfig,
    resizeEnabled: Boolean,
    resizeSnapMinutes: Int,
    resizeMinMinutes: Int,
    moveSlopDeg: Float,
    hitTest: (Offset) -> Task?,
    selectedTask: Task?,
    isNearHandle: (Offset, Task) -> Boolean,
    onTap: (Offset) -> Unit,
    onResizePreview: (Task, Int) -> Unit,
    onResizeCommit: (Task, Int) -> Unit,
    onResizeCancel: () -> Unit,
    inHitBand: (Offset) -> Boolean,
) {
    if (canvasSize.width <= 0f || canvasSize.height <= 0f) return
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val ring = layoutRing(canvasSize, geometry)
        val start = down.position
        if (!inHitBand(start)) return@awaitEachGesture
        down.consume()
        val hit = hitTest(start)
        val handleTask = selectedTask?.takeIf { resizeEnabled && isNearHandle(start, it) }
        var lastCanvasDeg = canvasDegreesFromCenter(start, ring.center)
        var accumulatedDeg = 0f
        val kind = if (handleTask != null) {
            DialGestureKind.Resize
        } else {
            awaitTapOrLongPress(
                pointerId = down.id,
                moveSlopDeg = moveSlopDeg,
                longPressMs = viewConfiguration.longPressTimeoutMillis.toLong(),
                center = ring.center,
                initialCanvasDeg = lastCanvasDeg,
                onDelta = { delta, last ->
                    accumulatedDeg += delta
                    lastCanvasDeg = last
                },
            )
        }
        when (kind) {
            DialGestureKind.Tap -> onTap(start)
            DialGestureKind.Cancel -> Unit
            DialGestureKind.Resize -> {
                val task = handleTask ?: hit
                if (!resizeEnabled || task == null) {
                    if (hit == null) onTap(start)
                    return@awaitEachGesture
                }
                var deltaDeg = accumulatedDeg
                fun previewEnd(): Int = TaskResize.endMinuteAfterAngularDelta(
                    task = task,
                    deltaDeg = deltaDeg,
                    snapMinutes = resizeSnapMinutes,
                    minDuration = resizeMinMinutes,
                )
                onResizePreview(task, previewEnd())
                val finished = drag(down.id) { change ->
                    change.consume()
                    val next = canvasDegreesFromCenter(change.position, ring.center)
                    deltaDeg += TimeMath.signedDeltaDegrees(lastCanvasDeg, next)
                    lastCanvasDeg = next
                    onResizePreview(task, previewEnd())
                }
                if (!finished) {
                    onResizeCancel()
                    return@awaitEachGesture
                }
                val committed = previewEnd()
                if (committed != task.endMinute) {
                    onResizeCommit(task, committed)
                } else {
                    onResizeCancel()
                }
            }
        }
    }
}

/**
 * Two-finger twist around the ring. Pointers that leave the canvas, drop
 * below two fingers, or cancel **stop applying deltas** and leave the current
 * scene offset unchanged (no jump).
 *
 * @since 0.2.0
 */
internal suspend fun PointerInputScope.detectTwoFingerRotate(
    rotateEnabled: Boolean,
    canvasSize: Size,
    onRotateBy: (Float) -> Unit,
) {
    if (!rotateEnabled) return
    val bounds = if (canvasSize.width > 0f && canvasSize.height > 0f) {
        canvasSize
    } else {
        Size(size.width.toFloat(), size.height.toFloat())
    }
    if (bounds.width <= 0f || bounds.height <= 0f) return
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var lastTwist: Float? = null
        while (true) {
            val event = awaitPointerEvent()
            val pressed = event.changes.filter { it.pressed }.sortedBy { it.id.value }
            if (pressed.isEmpty()) break
            if (pressed.size < 2) {
                lastTwist = null
                continue
            }
            val inside = pressed.all { change ->
                val p = change.position
                p.x in 0f..bounds.width && p.y in 0f..bounds.height
            }
            if (!inside) {
                lastTwist = null
                continue
            }
            pressed.forEach { it.consume() }
            val a = pressed[0].position
            val b = pressed[1].position
            val twist = canvasDegreesFromCenter(b, a)
            val previous = lastTwist
            lastTwist = twist
            if (previous != null) {
                val delta = TimeMath.signedDeltaDegrees(previous, twist)
                if (delta != 0f) onRotateBy(delta)
            }
        }
    }
}

private suspend fun AwaitPointerEventScope.awaitTapOrLongPress(
    pointerId: PointerId,
    moveSlopDeg: Float,
    longPressMs: Long,
    center: Offset,
    initialCanvasDeg: Float,
    onDelta: (delta: Float, last: Float) -> Unit,
): DialGestureKind {
    var last = initialCanvasDeg
    var run = 0f
    val timed = withTimeoutOrNull(longPressMs) {
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == pointerId }
                ?: return@withTimeoutOrNull DialGestureKind.Cancel
            change.consume()
            if (change.changedToUpIgnoreConsumed()) {
                return@withTimeoutOrNull DialGestureKind.Tap
            }
            val next = canvasDegreesFromCenter(change.position, center)
            val delta = TimeMath.signedDeltaDegrees(last, next)
            last = next
            run += delta
            onDelta(delta, last)
            if (abs(run) >= moveSlopDeg) {
                return@withTimeoutOrNull DialGestureKind.Cancel
            }
        }
        @Suppress("UNREACHABLE_CODE")
        DialGestureKind.Cancel
    }
    return timed ?: DialGestureKind.Resize
}

internal fun canvasDegreesFromCenter(point: Offset, center: Offset): Float =
    Math.toDegrees(atan2((point.y - center.y).toDouble(), (point.x - center.x).toDouble()))
        .toFloat()
