package com.example.clockplannerproject.kit.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.OnTerracotta
import com.example.clockplannerproject.ui.theme.PaperInk
import com.example.clockplannerproject.ui.theme.PaperSecondary
import com.example.clockplannerproject.ui.theme.TerracottaNow
import kotlinx.datetime.LocalTime

/**
 * Pause only while a work interval is open. Idle and break use Play.
 *
 * @since 1.3.0
 */
internal fun holeTimerShowsPause(workOpen: Boolean): Boolean = workOpen

/**
 * Break caption after Pause. Shown even on a compact hole.
 *
 * @since 1.3.0
 */
internal fun holeSessionCaptionRes(paused: Boolean): Int =
    if (paused) R.string.break_title else R.string.dial_this_session

/**
 * Compact Start / Pause in the ring hole (and pizza / petals hub).
 * The control is an icon only. The spoken name stays on the icon.
 *
 * @since 0.4.0
 */
@Composable
fun TimerHoleOverlay(
    running: Boolean,
    canStart: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    sessionPaused: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val startCd = stringResource(R.string.cd_timer_start)
    val pauseCd = stringResource(R.string.cd_timer_pause)
    val colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = TerracottaNow,
        contentColor = OnTerracotta,
        disabledContainerColor = TerracottaNow.copy(alpha = 0.38f),
        disabledContentColor = OnTerracotta,
    )
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (sessionPaused) {
            Text(
                text = stringResource(R.string.break_title),
                style = MaterialTheme.typography.labelSmall,
                color = PaperSecondary,
                maxLines = 1,
                softWrap = false,
            )
        }
        if (holeTimerShowsPause(running)) {
            FilledIconButton(
                onClick = onPause,
                modifier = Modifier.size(56.dp),
                colors = colors,
            ) {
                Icon(
                    imageVector = PauseBars,
                    contentDescription = pauseCd,
                )
            }
        } else {
            FilledIconButton(
                onClick = onStart,
                enabled = canStart,
                modifier = Modifier.size(56.dp),
                colors = colors,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = startCd,
                )
            }
        }
    }
}

/**
 * Idle hole: LIVE CHRONO plus current wall time. Marker color from theme primary
 * (same terracotta as [com.example.clockplannerproject.kit.core.config.ColorConfig.nowMarkerArgb]).
 *
 * @since 0.5.0
 */
@Composable
fun LiveChronoHole(
    now: LocalTime?,
    modifier: Modifier = Modifier,
) {
    val clockText = now?.let { wallClock(it) }
        ?: stringResource(R.string.time_placeholder)
    val description = stringResource(R.string.cd_current_time, clockText)
    Column(
        modifier = modifier.semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.dial_live_chrono),
            style = MaterialTheme.typography.labelSmall,
            color = TerracottaNow,
        )
        Text(
            text = clockText,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Running DIAL hole: minutes left or flow, task title, session caption, and Pause.
 * On pause the caption is the break title and the control is Play.
 *
 * @param holeDiameter inner diameter of the ring, in dp
 * @param paused true when the work interval is closed and a break is open
 * @since 0.5.0
 */
@Composable
fun RunningSessionHole(
    taskTitle: String,
    elapsed: String,
    holeDiameter: Dp,
    onPause: () -> Unit,
    minutesLeft: Int? = null,
    paused: Boolean = false,
    onResume: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val pauseCd = stringResource(R.string.cd_timer_pause)
    val startCd = stringResource(R.string.cd_timer_start)
    val showDetail = holeDiameter >= RoomForSessionDetail
    val button = if (showDetail) SessionPauseButton else CompactPauseButton
    Column(
        modifier = modifier.widthIn(max = holeDiameter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (minutesLeft != null) {
            Text(
                text = stringResource(R.string.session_minutes_left, minutesLeft),
                style = MaterialTheme.typography.labelSmall,
                color = PaperSecondary,
                maxLines = 1,
                softWrap = false,
            )
        } else if (!paused) {
            Text(
                text = stringResource(R.string.dial_in_flow),
                style = MaterialTheme.typography.labelSmall,
                color = TerracottaNow,
                maxLines = 1,
                softWrap = false,
            )
            Text(
                text = elapsed,
                style = MaterialTheme.typography.headlineSmall,
                color = PaperInk,
                maxLines = 1,
                softWrap = false,
            )
        }
        if (showDetail) {
            Text(
                text = taskTitle,
                style = MaterialTheme.typography.titleSmall,
                color = PaperInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (paused || showDetail) {
            Text(
                text = stringResource(holeSessionCaptionRes(paused)),
                style = MaterialTheme.typography.labelSmall,
                color = PaperSecondary,
                maxLines = 1,
                softWrap = false,
            )
        }
        FilledIconButton(
            onClick = if (paused) onResume else onPause,
            modifier = Modifier.size(button),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = TerracottaNow,
                contentColor = OnTerracotta,
            ),
        ) {
            val showPause = holeTimerShowsPause(workOpen = !paused)
            Icon(
                imageVector = if (showPause) PauseBars else Icons.Filled.PlayArrow,
                contentDescription = if (showPause) pauseCd else startCd,
            )
        }
    }
}

/** Wall time for the idle hole, hours and minutes. */
internal fun wallClock(time: LocalTime): String =
    "%02d:%02d".format(time.hour, time.minute)

/**
 * Elapsed civil time of the open block, `HH:MM`.
 *
 * @since 0.5.0
 */
internal fun formatSessionElapsed(startMinute: Int, nowMinute: Int): String {
    val minutes = TimeMath.durationMinutes(startMinute, nowMinute)
    return "%02d:%02d".format(minutes / 60, minutes % 60)
}

private val RoomForSessionDetail = 160.dp
private val SessionPauseButton = 56.dp
private val CompactPauseButton = 48.dp

/** Two bars; [Icons.Filled.Pause] lives in material-icons-extended, which this module does not use. */
private val PauseBars: ImageVector = ImageVector.Builder(
    name = "PauseBars",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.NonZero,
    ) {
        moveTo(6f, 5f)
        horizontalLineToRelative(4f)
        verticalLineToRelative(14f)
        horizontalLineToRelative(-4f)
        close()
        moveTo(14f, 5f)
        horizontalLineToRelative(4f)
        verticalLineToRelative(14f)
        horizontalLineToRelative(-4f)
        close()
    }
}.build()

@Preview(showBackground = true)
@Composable
private fun TimerHoleOverlayPreview() {
    ClockPlannerProjectTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LiveChronoHole(now = LocalTime(10, 42))
            TimerHoleOverlay(running = false, canStart = true, onStart = {}, onPause = {})
            TimerHoleOverlay(running = true, canStart = false, onStart = {}, onPause = {})
        }
    }
}
