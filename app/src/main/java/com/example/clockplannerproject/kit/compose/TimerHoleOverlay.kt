package com.example.clockplannerproject.kit.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.OnTerracotta
import com.example.clockplannerproject.ui.theme.TerracottaNow
import kotlinx.datetime.LocalTime

/**
 * Compact Start / Pause in the ring hole (and pizza / petals hub).
 *
 * @since 0.4.0
 */
@Composable
fun TimerHoleOverlay(
    running: Boolean,
    canStart: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
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
        if (running) {
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
            Text(
                text = stringResource(R.string.timer_pause),
                style = MaterialTheme.typography.labelSmall,
                color = TerracottaNow,
            )
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
            Text(
                text = stringResource(R.string.timer_start),
                style = MaterialTheme.typography.labelSmall,
                color = TerracottaNow,
            )
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
    val clockText = now?.let { TimeMath.formatHm(it) } ?: "—"
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
