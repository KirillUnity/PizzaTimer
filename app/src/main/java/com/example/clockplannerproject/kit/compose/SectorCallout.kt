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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.core.BreakSession
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskTimer
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.calloutSlice
import com.example.clockplannerproject.kit.core.config.GeometryConfig
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
    range: String? = null,
    onOpenDetails: () -> Unit = {},
) {
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
                .widthIn(max = CALLOUT_WIDTH)
                .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 4.dp),
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!range.isNullOrBlank()) {
                Text(
                    text = range,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
    val running = TaskTimer.hasOpenBlock(task)
    val slice = calloutSlice(task, half, nowMinute)
    if (slice != null && !(running && config.viewMode == ViewMode.DIAL)) {
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
            avoid = if (config.viewMode == ViewMode.DIAL) {
                dialCalloutKeepOut(constraints, density)
            } else {
                emptyList()
            },
        )
        SectorCallout(
            task = task,
            range = TimeMath.formatRange(slice.startMinute, slice.endMinute),
            onDismiss = onDismiss,
            onOpenDetails = onOpenDetails,
            modifier = Modifier
                .widthIn(max = CALLOUT_WIDTH)
                .offset { offset },
        )
    }
    if (config.viewMode != ViewMode.DIAL) {
        TimerHoleOverlay(
            running = running,
            canStart = TaskTimer.canStart(task),
            onStart = onStartTimer,
            onPause = onPauseTimer,
            modifier = Modifier.align(Alignment.Center),
        )
    }
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
    avoid: List<IntRect> = emptyList(),
): IntOffset {
    val preferredX = if (preferRight) {
        anchorX.toInt() + margin
    } else {
        anchorX.toInt() - calloutWidth - margin
    }
    val maxX = (containerWidth - calloutWidth - margin).coerceAtLeast(margin)
    val maxY = (containerHeight - calloutHeight - margin).coerceAtLeast(margin)
    val preferred = IntOffset(
        x = preferredX.coerceIn(margin, maxX),
        y = (anchorY.toInt() - calloutHeight / 2).coerceIn(margin, maxY),
    )
    if (avoid.isEmpty() || !calloutHits(preferred, calloutWidth, calloutHeight, avoid)) {
        return preferred
    }
    val step = (calloutHeight / 4).coerceAtLeast(8)
    var best = preferred
    var bestDistance = Long.MAX_VALUE
    var y = margin
    while (y <= maxY) {
        var x = margin
        while (x <= maxX) {
            val candidate = IntOffset(x, y)
            if (!calloutHits(candidate, calloutWidth, calloutHeight, avoid)) {
                val dx = (x - preferred.x).toLong()
                val dy = (y - preferred.y).toLong()
                val distance = dx * dx + dy * dy
                if (distance < bestDistance) {
                    bestDistance = distance
                    best = candidate
                }
            }
            x += step
        }
        y += step
    }
    return best
}

private fun calloutHits(
    origin: IntOffset,
    width: Int,
    height: Int,
    avoid: List<IntRect>,
): Boolean {
    val placed = IntRect(origin.x, origin.y, origin.x + width, origin.y + height)
    return avoid.any { it.overlaps(placed) }
}

/**
 * Keeps the description cloud off numerals 12 and 3 and out of the hub.
 */
private fun dialCalloutKeepOut(
    constraints: Constraints,
    density: androidx.compose.ui.unit.Density,
): List<IntRect> {
    val numeral = with(density) { NUMERAL_KEEP_OUT.roundToPx() }
    val gap = with(density) { CALLOUT_MARGIN.roundToPx() }
    val side = minOf(constraints.maxWidth, constraints.maxHeight)
    val holeRadius = (side * DialHoleDiameterFraction / 2f).toInt() + gap
    val cx = constraints.maxWidth / 2
    val cy = constraints.maxHeight / 2
    return listOf(
        IntRect(
            left = cx - numeral,
            top = 0,
            right = cx + numeral,
            bottom = numeral,
        ),
        IntRect(
            left = constraints.maxWidth - numeral,
            top = cy - numeral / 2,
            right = constraints.maxWidth,
            bottom = cy + numeral / 2,
        ),
        IntRect(
            left = cx - holeRadius,
            top = cy - holeRadius,
            right = cx + holeRadius,
            bottom = cy + holeRadius,
        ),
    )
}

/** Inner hole diameter as a fraction of the square dial side. */
internal const val DialHoleDiameterFraction: Float =
    (1f - 2f * GeometryConfig.OUTER_INSET_FRACTION) *
        (1f - 2f * GeometryConfig.RING_THICKNESS_FRACTION)

/**
 * Overlay width as a fraction of the square side. Pizza and petals use the
 * painted hub; DIAL uses the ring hole.
 */
internal fun overlayHoleDiameterFraction(viewMode: ViewMode): Float = when (viewMode) {
    ViewMode.PIZZA, ViewMode.PETALS ->
        (1f - 2f * GeometryConfig.OUTER_INSET_FRACTION) *
            GeometryConfig.HUB_RADIUS_FRACTION
    else -> DialHoleDiameterFraction
}

private val CALLOUT_WIDTH = 180.dp
private val CALLOUT_ESTIMATED_HEIGHT = 96.dp
private val CALLOUT_MARGIN = 8.dp
private val NUMERAL_KEEP_OUT = 40.dp
/** Lifts LIVE CHRONO so the 56dp hub button stays on the pie center. */
private val PizzaChronoLift = 60.dp

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
    viewMode: ViewMode,
    holeDiameter: Dp,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    minutesLeft: Int? = null,
    sessionPaused: Boolean = false,
) {
    val task = selectedTask
    val hubModes = viewMode == ViewMode.PIZZA || viewMode == ViewMode.PETALS
    val workPaused = sessionPaused &&
        task != null &&
        !BreakSession.isBreak(task) &&
        !TaskTimer.hasOpenBlock(task)
    when {
        task == null && hubModes -> {
            TimerHoleOverlay(
                running = false,
                canStart = true,
                onStart = onStartTimer,
                onPause = onPauseTimer,
                sessionPaused = sessionPaused,
                modifier = Modifier.align(Alignment.Center),
            )
            LiveChronoHole(
                now = now,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = -PizzaChronoLift),
            )
        }
        task == null -> Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = holeDiameter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LiveChronoHole(now = now)
            TimerHoleOverlay(
                running = false,
                canStart = true,
                onStart = onStartTimer,
                onPause = onPauseTimer,
                sessionPaused = sessionPaused,
            )
        }
        viewMode == ViewMode.DIAL &&
            (workPaused || (TaskTimer.hasOpenBlock(task) && !BreakSession.isBreak(task))) -> {
            val session = task
            val open = session.blocks.firstOrNull { it.isOpen }
            val nowMinute = now?.let { TimeMath.minuteOfDay(it).toInt() } ?: open?.startMinute ?: 0
            val elapsed = open?.let { formatSessionElapsed(it.startMinute, nowMinute) } ?: "00:00"
            RunningSessionHole(
                taskTitle = session.title,
                elapsed = elapsed,
                holeDiameter = holeDiameter,
                onPause = onPauseTimer,
                minutesLeft = minutesLeft,
                paused = workPaused,
                onResume = onStartTimer,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        else -> TimerHoleOverlay(
            running = holeTimerShowsPause(
                workOpen = task != null &&
                    TaskTimer.hasOpenBlock(task) &&
                    !BreakSession.isBreak(task),
            ),
            canStart = task == null || TaskTimer.canStart(task),
            onStart = onStartTimer,
            onPause = onPauseTimer,
            sessionPaused = sessionPaused,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectorCalloutPreview() {
    ClockPlannerProjectTheme {
        SectorCallout(
            task = SampleTasks.typicalDay(LocalDate(2026, 10, 6))[1],
            range = "14:30 – 16:30",
            onDismiss = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
