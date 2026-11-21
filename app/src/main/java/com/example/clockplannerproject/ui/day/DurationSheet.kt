package com.example.clockplannerproject.ui.day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.time.SessionDuration
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme

/**
 * Asks for a session length before the interval opens.
 * The hole buttons stay icon-only; this confirm is not that button.
 *
 * @param minutes current offer, whole minutes
 * @param onMinutes next offer after a 15-minute step
 * @param onConfirm starts the session with [minutes]
 * @param onDismiss leaves the day unchanged
 * @since 1.2.0
 */
@Composable
fun DurationSheet(
    minutes: Int,
    onMinutes: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.duration_title)) },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = { onMinutes(SessionDuration.step(minutes, -1)) }) {
                    Text(stringResource(R.string.cd_duration_less))
                }
                Text(stringResource(R.string.session_minutes_left, minutes))
                TextButton(onClick = { onMinutes(SessionDuration.step(minutes, 1)) }) {
                    Text(stringResource(R.string.cd_duration_more))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.duration_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.duration_dismiss))
            }
        },
    )
}

@Preview
@Composable
private fun DurationSheetPreview() {
    ClockPlannerProjectTheme {
        DurationSheet(minutes = 25, onMinutes = {}, onConfirm = {}, onDismiss = {})
    }
}
