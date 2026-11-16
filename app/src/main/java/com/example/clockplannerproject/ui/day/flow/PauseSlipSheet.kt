package com.example.clockplannerproject.ui.day.flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.OnTerracotta
import com.example.clockplannerproject.ui.theme.PaperSurface
import com.example.clockplannerproject.ui.theme.TerracottaNow

/**
 * Paper slip after Pause. The field and chips are a checkpoint only.
 *
 * The closed interval is already stored. [onCommit] and [onSkip] leave it unchanged.
 *
 * @param draft text the user will see on the resume chip
 * @param mood selected focus chip
 * @param onCommit keeps [draft] as the checkpoint and closes the slip
 * @param onSkip closes the slip and does not replace a previous checkpoint
 * @since 1.1.0
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PauseSlipSheet(
    draft: String,
    mood: PauseMood,
    onDraft: (String) -> Unit,
    onMood: (PauseMood) -> Unit,
    onCommit: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = PaperSurface,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.flow_pause_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            OutlinedTextField(
                value = draft,
                onValueChange = onDraft,
                placeholder = { Text(stringResource(R.string.flow_pause_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MoodChip(PauseMood.Calm, R.string.flow_mood_calm, mood, onMood)
                MoodChip(PauseMood.Deep, R.string.flow_mood_deep, mood, onMood)
                MoodChip(PauseMood.Stuck, R.string.flow_mood_stuck, mood, onMood)
            }
            Text(
                text = stringResource(R.string.flow_pause_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onCommit,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TerracottaNow,
                    contentColor = OnTerracotta,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text(stringResource(R.string.flow_pause_commit))
            }
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.flow_pause_skip))
            }
        }
    }
}

@Composable
private fun MoodChip(
    mood: PauseMood,
    labelRes: Int,
    selected: PauseMood,
    onMood: (PauseMood) -> Unit,
) {
    FilterChip(
        selected = selected == mood,
        onClick = { onMood(mood) },
        label = { Text(stringResource(labelRes)) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TerracottaNow,
            selectedLabelColor = OnTerracotta,
        ),
    )
}

@Preview(showBackground = true, name = "Pause slip")
@Composable
private fun PauseSlipSheetPreview() {
    ClockPlannerProjectTheme {
        PauseSlipSheet(
            draft = "",
            mood = PauseMood.Calm,
            onDraft = {},
            onMood = {},
            onCommit = {},
            onSkip = {},
        )
    }
}
