package com.example.clockplannerproject.kit.compose

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.clockplannerproject.kit.core.clipTaskToHalf
import com.example.clockplannerproject.kit.core.config.TimeDialConfig
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import kotlinx.datetime.LocalDate
import kotlin.math.cos
import kotlin.math.sin

/**
 * Description cloud near a selected sector. Dismiss with a tap.
 *
 * @since 0.4.0
 */
@Composable
fun SectorCallout(
    task: Task,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val body = task.description.ifBlank {
        stringResource(R.string.task_description_placeholder)
    }
    val cd = stringResource(R.string.cd_sector_callout, task.title)
    Surface(
        onClick = onDismiss,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
        modifier = modifier.semantics { contentDescription = cd },
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 180.dp)
                .padding(12.dp),
        ) {
            Text(text = task.title, style = MaterialTheme.typography.titleSmall)
            Text(text = body, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * Callout near the sector plus Start/Pause in the hole. DIAL and PIZZA
 * show the cloud; the timer overlay is visible in every canvas mode.
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
) {
    val showCloud = config.viewMode == ViewMode.DIAL || config.viewMode == ViewMode.PIZZA
    if (showCloud) {
        val slice = clipTaskToHalf(task.resolveOpenBlocks(nowMinute), half).firstOrNull()
        if (slice != null) {
            val visualMid = TimeMath.visualHalfAngle(
                minuteOfDay = (slice.startMinute + slice.endMinute) / 2f,
                anchorMinute = anchorMinute,
                half = half,
                userRotationOffsetDeg = config.interaction.userRotationOffsetDeg,
            )
            val canvasRad = Math.toRadians(TimeMath.toCanvasAngle(visualMid).toDouble())
            val radiusFrac = if (config.viewMode == ViewMode.PIZZA) 0.42f else 0.36f
            val dx = (constraints.maxWidth / 2f) * (1f + radiusFrac * cos(canvasRad).toFloat())
            val dy = (constraints.maxHeight / 2f) * (1f + radiusFrac * sin(canvasRad).toFloat())
            val density = LocalDensity.current
            SectorCallout(
                task = task,
                onDismiss = onDismiss,
                modifier = Modifier.offset {
                    IntOffset(
                        (dx - 80 * density.density).toInt(),
                        (dy - 40 * density.density).toInt(),
                    )
                },
            )
        }
    }
    TimerHoleOverlay(
        running = TaskTimer.hasOpenBlock(task),
        canStart = TaskTimer.canStart(task),
        onStart = onStartTimer,
        onPause = onPauseTimer,
        modifier = Modifier.align(Alignment.Center),
    )
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
