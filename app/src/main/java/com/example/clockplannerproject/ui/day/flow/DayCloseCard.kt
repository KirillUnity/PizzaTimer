package com.example.clockplannerproject.ui.day.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.clockplannerproject.ui.theme.OnTerracotta
import com.example.clockplannerproject.ui.theme.PaperSurface
import com.example.clockplannerproject.ui.theme.TerracottaNow

/**
 * Card under the dial when the day is closed.
 *
 * Shows closed hours and the last checkpoint. No completion percent and no print number.
 *
 * @param closedMinutes sum of intervals that have already ended
 * @param checkpoint last pause note, or a calm fallback when none was saved
 * @param onOpenReports leaves the day tab for reports
 * @since 1.1.0
 */
@Composable
fun DayCloseCard(
    closedMinutes: Int,
    checkpoint: String,
    onOpenReports: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hours = closedMinutes / 60
    val mins = closedMinutes % 60
    val hoursLabel = if (hours <= 0) {
        stringResource(R.string.flow_closed_minutes, mins)
    } else {
        stringResource(R.string.flow_closed_hours, hours, mins)
    }
    val line = checkpoint.ifBlank { stringResource(R.string.flow_pause_helper) }
    val reportsCd = stringResource(R.string.cd_flow_open_reports)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = PaperSurface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.flow_day_closed),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = hoursLabel,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onOpenReports,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TerracottaNow,
                    contentColor = OnTerracotta,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics { contentDescription = reportsCd },
            ) {
                Text(stringResource(R.string.flow_open_reports))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Day close")
@Composable
private fun DayCloseCardPreview() {
    ClockPlannerProjectTheme {
        DayCloseCard(
            closedMinutes = 6 * 60 + 15,
            checkpoint = "цифры за четверг",
            onOpenReports = {},
        )
    }
}
