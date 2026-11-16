package com.example.clockplannerproject.ui.day.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.OnTerracotta
import com.example.clockplannerproject.ui.theme.PaperSurface
import com.example.clockplannerproject.ui.theme.TerracottaNow

/**
 * Checkpoint parked on the sector. Place it on the lower ring, outside the hole.
 *
 * @param checkpoint words saved from the pause slip
 * @param onResume starts the next interval; it does not edit the closed one
 * @since 1.1.0
 */
@Composable
fun ResumeCheckpointChip(
    checkpoint: String,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.flow_resume_chip, checkpoint)
    val resumeCd = stringResource(R.string.cd_flow_resume)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = PaperSurface,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 200.dp),
            )
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TerracottaNow)
                    .clickable(onClick = onResume)
                    .semantics { contentDescription = resumeCd },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = OnTerracotta,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Resume chip")
@Composable
private fun ResumeCheckpointChipPreview() {
    ClockPlannerProjectTheme {
        ResumeCheckpointChip(
            checkpoint = "цифры за четверг",
            onResume = {},
        )
    }
}
