package com.example.clockplannerproject.kit.compose

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskTimer
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.calloutSlice
import com.example.clockplannerproject.kit.core.config.TimeDialConfig
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import kotlinx.datetime.LocalDate
import kotlin.math.cos
import kotlin.math.sin

/**
 * Description cloud near a selected sector. Tap the body to dismiss;
 * Details opens the full card without covering the timer hole first.
 *
 * @since 0.4.0
 */
@Composable
fun SectorCallout(
    task: Task,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenDetails: () -> Unit = {},
) {
    val body = task.description.ifBlank {
        stringResource(R.string.task_description_placeholder)
    }
    val cd = stringResource(R.string.cd_sector_callout, task.title)
    Surface(
        onClick = onDismiss,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
        modifier = modifier.semantics { contentDescription = cd },
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 180.dp)
                .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 4.dp),
        ) {
            Text(text = task.title, style = MaterialTheme.typography.titleSmall)
            Text(text = body, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onOpenDetails) {
                Text(stringResource(R.string.task_details))
            }
        }
    }
}

/**
 * Callout near the sector plus Start/Pause in the hole on canvas modes.
 *
 * @since 0.4.0
 */
@Composable
fun BoxScope.SelectedTaskOverlays(
    task: Task,
    half: DialHalf,
    nowMinute: Int,
    anchorMinute: Float,
    config: TimeDialConfig,
    constraints: Constraints,
    onDismiss: () -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onOpenDetails: () -> Unit = {},
) {
    if (config.viewMode == ViewMode.LIST) return
    val slice = calloutSlice(task, half, nowMinute)
    if (slice != null) {
        val visualMid = TimeMath.visualHalfAngle(
            minuteOfDay = (slice.startMinute + slice.endMinute) / 2f,
            anchorMinute = anchorMinute,
            half = half,
            userRotationOffsetDeg = config.interaction.userRotationOffsetDeg,
        )
        val canvasRad = Math.toRadians(TimeMath.toCanvasAngle(visualMid).toDouble())
        val radiusFrac = when (config.viewMode) {
            ViewMode.PIZZA, ViewMode.PETALS -> 0.5f
            else -> 0.44f
        }
        val density = LocalDensity.current
        val anchorX = (constraints.maxWidth / 2f) *
            (1f + radiusFrac * cos(canvasRad).toFloat())
        val anchorY = (constraints.maxHeight / 2f) *
            (1f + radiusFrac * sin(canvasRad).toFloat())
        val calloutWidth = with(density) { CALLOUT_WIDTH.roundToPx() }
        val calloutHeight = with(density) { CALLOUT_ESTIMATED_HEIGHT.roundToPx() }
        val margin = with(density) { CALLOUT_MARGIN.roundToPx() }
        val offset = boundedCalloutOffset(
            containerWidth = constraints.maxWidth,
            containerHeight = constraints.maxHeight,
            anchorX = anchorX,
            anchorY = anchorY,
            calloutWidth = calloutWidth,
            calloutHeight = calloutHeight,
            margin = margin,
            preferRight = cos(canvasRad) >= 0,
        )
        SectorCallout(
            task = task,
            onDismiss = onDismiss,
            onOpenDetails = onOpenDetails,
            modifier = Modifier
                .widthIn(max = CALLOUT_WIDTH)
                .offset { offset },
        )
    }
    TimerHoleOverlay(
        running = TaskTimer.hasOpenBlock(task),
        canStart = TaskTimer.canStart(task),
        onStart = onStartTimer,
        onPause = onPauseTimer,
        modifier = Modifier.align(Alignment.Center),
    )
}

internal fun boundedCalloutOffset(
    containerWidth: Int,
    containerHeight: Int,
    anchorX: Float,
    anchorY: Float,
    calloutWidth: Int,
    calloutHeight: Int,
    margin: Int,
    preferRight: Boolean,
): IntOffset {
    val preferredX = if (preferRight) {
        anchorX.toInt() + margin
    } else {
        anchorX.toInt() - calloutWidth - margin
    }
    val maxX = (containerWidth - calloutWidth - margin).coerceAtLeast(margin)
    val maxY = (containerHeight - calloutHeight - margin).coerceAtLeast(margin)
    return IntOffset(
        x = preferredX.coerceIn(margin, maxX),
        y = (anchorY.toInt() - calloutHeight / 2).coerceIn(margin, maxY),
    )
}

private val CALLOUT_WIDTH = 168.dp
private val CALLOUT_ESTIMATED_HEIGHT = 112.dp
private val CALLOUT_MARGIN = 8.dp

/**
 * Hole content: LIVE CHRONO when idle, Start/Pause when a sector is selected.
 * Never uses a modal sheet over the hub.
 *
 * @since 0.5.0
 */
@Composable
fun BoxScope.DialHoleContent(
    now: kotlinx.datetime.LocalTime?,
    selectedTask: Task?,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
) {
    if (selectedTask == null) {
        LiveChronoHole(now = now, modifier = Modifier.align(Alignment.Center))
    }
}

@Preview(showBackground = true)
@Composable
private fun SectorCalloutPreview() {
    ClockPlannerProjectTheme {
        SectorCallout(
            task = SampleTasks.typicalDay(LocalDate(2026, 10, 6))[1],
            onDismiss = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
