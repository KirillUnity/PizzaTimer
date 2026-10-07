package com.example.clockplannerproject.kit.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme

/**
 * Start / Pause in the ring hole (and pizza center) when a task is selected.
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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (running) {
            FilledTonalButton(
                onClick = onPause,
                modifier = Modifier.semantics { contentDescription = pauseCd },
            ) {
                Text(stringResource(R.string.timer_pause))
            }
        } else {
            FilledTonalButton(
                onClick = onStart,
                enabled = canStart,
                modifier = Modifier.semantics { contentDescription = startCd },
            ) {
                Text(stringResource(R.string.timer_start))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerHoleOverlayPreview() {
    ClockPlannerProjectTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TimerHoleOverlay(running = false, canStart = true, onStart = {}, onPause = {})
            TimerHoleOverlay(running = true, canStart = false, onStart = {}, onPause = {})
        }
    }
}
