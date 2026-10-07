package com.example.clockplannerproject.kit.compose

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.config.TimeDialConfig
import com.example.clockplannerproject.kit.core.halfAnchorMinute
import com.example.clockplannerproject.kit.core.layout.ReflowMode
import com.example.clockplannerproject.kit.core.layout.RestGaps
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.kit.render.DialDrawState
import com.example.clockplannerproject.kit.render.RendererFactory
import com.example.clockplannerproject.kit.render.TimeDialRenderer
import com.example.clockplannerproject.kit.render.TimeDialRenderer.focusChevronPath
import com.example.clockplannerproject.kit.render.dialTaskTalkBackLabel
import com.example.clockplannerproject.kit.render.hitTestTask
import com.example.clockplannerproject.kit.render.isNearResizeHandle
import com.example.clockplannerproject.kit.render.layoutRing
import com.example.clockplannerproject.kit.render.prepareSectors
import com.example.clockplannerproject.kit.render.visualSliceEndDeg

/**
 * Canvas host. One-finger tap/resize; two-finger twist rotates the scene.
 * CompactRemaining: scene rotation uses the half start as anchor so the
 * now chevron stays at 12 and is not bound to sector times.
 *
 * TalkBack: [accessibilityTasks] are listed as custom actions (civil times).
 * A two-finger twist that leaves the canvas or drops a pointer does not
 * change the stored rotation offset.
 *
 * @since 0.1.0
 */
@Composable
fun DialView(
    tasks: List<Task>,
    sectorColors: List<Color>,
    markerColor: Color,
    config: TimeDialConfig,
    nowMinute: Float,
    half: DialHalf,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
    nowMarkerContentDescription: String = "Current time on the dial",
    onUserRotationDelta: (Float) -> Unit = {},
    selectedTask: Task? = null,
    handleColor: Color = markerColor,
    focusColor: Color = markerColor,
    markerAlpha: Float = 1f,
    onResizePreview: (Task, Int) -> Unit = { _, _ -> },
    onResizeCommit: (Task, Int) -> Unit = { _, _ -> },
    onResizeCancel: () -> Unit = {},
    accessibilityTasks: List<Task> = tasks,
) {
    val geometry = config.geometry
    val clock = config.clock
    val interaction = config.interaction
    val realTasks = remember(tasks) { tasks }
    val compact = clock.reflowMode == ReflowMode.CompactRemaining
    val anchorMinute = if (clock.snapToNow && !compact) {
        halfAnchorMinute(nowMinute, half, realTasks)
    } else {
        half.startMinute.toFloat()
    }
    val talkBackTasks = remember(accessibilityTasks) {
        accessibilityTasks.filterNot(RestGaps::isRest)
    }
    val talkBackActions = remember(talkBackTasks, onTaskClick) {
        talkBackTasks.map { task ->
            val label = dialTaskTalkBackLabel(task)
            CustomAccessibilityAction(label) {
                onTaskClick(task)
                true
            }
        }
    }
    val talkBackSummary = stringResource(
        R.string.cd_dial_summary,
        talkBackTasks.size,
        nowMarkerContentDescription,
    )
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val chevronColor = if (half.contains(nowMinute)) markerColor else focusColor
    val renderer = remember(config.viewMode) {
        RendererFactory.canvasOrNull(config.viewMode) ?: TimeDialRenderer
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
            .pointerInput(tasks, nowMinute, config, half, canvasSize, selectedTask, anchorMinute) {
                detectDialGestures(
                    canvasSize = canvasSize,
                    geometry = geometry,
                    resizeEnabled = interaction.resizeEnabled,
                    resizeSnapMinutes = interaction.resizeSnapMinutes,
                    resizeMinMinutes = interaction.resizeMinMinutes,
                    moveSlopDeg = interaction.rotateSlopDeg,
                    hitTest = { tap ->
                        hitTestTask(
                            offset = tap,
                            tasks = tasks,
                            config = config,
                            nowMinute = nowMinute,
                            canvasSize = canvasSize,
                            half = half,
                        )
                    },
                    selectedTask = selectedTask,
                    isNearHandle = { offset, task ->
                        val now = nowMinute.toInt()
                        val closed = task.blocks.firstOrNull()?.isClosed(now) == true
                        val running = task.blocks.any { it.isOpen }
                        if (closed || running) {
                            false
                        } else {
                            val ring = layoutRing(canvasSize, geometry)
                            val clockDeg = TimeMath.fromCanvasAngle(
                                canvasDegreesFromCenter(offset, ring.center),
                            )
                            isNearResizeHandle(
                                pointerClockDeg = clockDeg,
                                task = task,
                                half = half,
                                anchorMinute = anchorMinute,
                                userRotationOffsetDeg = interaction.userRotationOffsetDeg,
                                slopDeg = interaction.resizeHandleDeg,
                            )
                        }
                    },
                    onTap = { tap ->
                        val hit = hitTestTask(
                            offset = tap,
                            tasks = tasks,
                            config = config,
                            nowMinute = nowMinute,
                            canvasSize = canvasSize,
                            half = half,
                        )
                        if (hit != null) onTaskClick(hit)
                    },
                    onResizePreview = onResizePreview,
                    onResizeCommit = onResizeCommit,
                    onResizeCancel = onResizeCancel,
                    inHitBand = { tap ->
                        val ring = layoutRing(canvasSize, geometry)
                        val dx = tap.x - ring.center.x
                        val dy = tap.y - ring.center.y
                        val radius = kotlin.math.hypot(dx, dy)
                        renderer.containsPointer(ring, radius)
                    },
                )
            }
            .pointerInput(interaction.rotateEnabled, canvasSize) {
                detectTwoFingerRotate(
                    rotateEnabled = interaction.rotateEnabled,
                    canvasSize = canvasSize,
                    onRotateBy = onUserRotationDelta,
                )
            }
            .clearAndSetSemantics {
                contentDescription = talkBackSummary
                customActions = talkBackActions
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val ring = layoutRing(size, geometry)
                    val stroke = Stroke(width = ring.thickness, cap = StrokeCap.Butt)
                    val sectors = prepareSectors(
                        tasks = tasks,
                        colors = sectorColors,
                        half = half,
                        anchorMinute = anchorMinute,
                        userRotationOffsetDeg = interaction.userRotationOffsetDeg,
                        selectedTaskId = selectedTask?.id,
                    )
                    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        textSize = (ring.thickness * geometry.labelFontFractionOfThickness)
                            .coerceAtLeast(10f)
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                        isSubpixelText = true
                    }
                    val labels = renderer.prepareLabels(sectors, ring, geometry, labelPaint)
                    val extras = renderer.prepareExtras(sectors, ring, geometry)
                    val chevron = Path()
                    focusChevronPath(
                        chevron,
                        ring,
                        geometry,
                        interaction.userRotationOffsetDeg,
                    )
                    val handleDeg = selectedTask
                        ?.takeIf { interaction.resizeEnabled && it.blocks.none { block -> block.isOpen } }
                        ?.let { task ->
                        visualSliceEndDeg(
                            task = task,
                            half = half,
                            anchorMinute = anchorMinute,
                            userRotationOffsetDeg = interaction.userRotationOffsetDeg,
                        )
                    }
                    val drawState = DialDrawState(
                        config = config,
                        ring = ring,
                        sectors = sectors,
                        labels = labels,
                        extras = extras,
                        labelPaint = labelPaint,
                        stroke = stroke,
                        chevron = chevron,
                        showNowMarker = clock.showNowMarker,
                        chevronColor = chevronColor,
                        markerAlpha = markerAlpha,
                        handleDeg = handleDeg,
                        handleColor = handleColor,
                    )
                    onDrawBehind {
                        with(renderer) { render(drawState) }
                    }
                },
        )
    }
}
