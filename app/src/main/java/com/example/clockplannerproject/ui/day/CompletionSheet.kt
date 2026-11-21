package com.example.clockplannerproject.ui.day

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.DemandMark
import com.example.clockplannerproject.kit.core.ShortFlowScale
import com.example.clockplannerproject.kit.core.TaskCompletion

/**
 * Finish-task sheet over a dimmed dial. Scores are 1–10 proxies; the short form is 1–5.
 *
 * @param draft current fields; a null score has not been chosen
 * @param onDraft called with the edited draft
 * @param onSubmit asks the view model to save when the proxies are present
 * @param onDismiss closes the sheet without saving
 * @since 1.2.0
 */
@Composable
fun CompletionSheet(
    draft: CompletionDraft,
    onDraft: (CompletionDraft) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
            .padding(16.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(R.string.completion_title), style = MaterialTheme.typography.titleLarge)
                if (draft.titleEditable) {
                    OutlinedTextField(
                        value = draft.title,
                        onValueChange = { onDraft(draft.copy(title = it)) },
                        label = { Text(stringResource(R.string.completion_name)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(draft.title, style = MaterialTheme.typography.titleMedium)
                }
                ScoreRow(
                    label = stringResource(R.string.completion_engagement),
                    value = draft.engagement,
                    onChange = { onDraft(draft.copy(engagement = it)) },
                )
                ScoreRow(
                    label = stringResource(R.string.completion_skill),
                    value = draft.skill,
                    onChange = { onDraft(draft.copy(skill = it)) },
                )
                OutlinedTextField(
                    value = draft.easier,
                    onValueChange = { onDraft(draft.copy(easier = it)) },
                    label = { Text(stringResource(R.string.completion_easier)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                ScoreRow(
                    label = stringResource(R.string.completion_state),
                    value = draft.stateAfter,
                    onChange = { onDraft(draft.copy(stateAfter = it)) },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = draft.mark == DemandMark.CHALLENGE,
                        onClick = { onDraft(draft.copy(mark = DemandMark.CHALLENGE)) },
                        label = { Text(stringResource(R.string.completion_challenge)) },
                    )
                    FilterChip(
                        selected = draft.mark == DemandMark.HINDRANCE,
                        onClick = { onDraft(draft.copy(mark = DemandMark.HINDRANCE)) },
                        label = { Text(stringResource(R.string.completion_hindrance)) },
                    )
                }
                OutlinedTextField(
                    value = draft.thoughts,
                    onValueChange = { onDraft(draft.copy(thoughts = it)) },
                    label = { Text(stringResource(R.string.completion_thoughts)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (draft.mark == DemandMark.HINDRANCE) {
                    OutlinedTextField(
                        value = draft.reframe,
                        onValueChange = { onDraft(draft.copy(reframe = it)) },
                        label = { Text(stringResource(R.string.completion_reframe)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.completion_done), modifier = Modifier.weight(1f))
                    Switch(
                        checked = draft.done,
                        onCheckedChange = { onDraft(draft.copy(done = it)) },
                    )
                }
                if (!draft.done) {
                    OutlinedTextField(
                        value = draft.progress,
                        onValueChange = { onDraft(draft.copy(progress = it)) },
                        label = { Text(stringResource(R.string.completion_progress)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(stringResource(R.string.flow_form_caption))
                ShortFlowScale.DIMENSIONS.forEachIndexed { index, _ ->
                    ScoreRow(
                        label = stringResource(flowDimensionLabel(index)),
                        value = draft.shortFlow.getOrNull(index),
                        range = ShortFlowScale.ANCHORS,
                        onChange = { next ->
                            val updated = draft.shortFlow.toMutableList()
                            while (updated.size < ShortFlowScale.ITEM_COUNT) updated.add(null)
                            updated[index] = next
                            onDraft(draft.copy(shortFlow = updated))
                        },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.completion_dismiss))
                    }
                    Button(onClick = onSubmit) {
                        Text(stringResource(R.string.completion_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(
    label: String,
    value: Int?,
    onChange: (Int) -> Unit,
    range: IntRange = TaskCompletion.SCORE_RANGE,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label, modifier = Modifier.weight(1f))
        IconButton(onClick = {
            val next = ((value ?: (range.first + 1)) - 1).coerceIn(range)
            onChange(next)
        }) {
            Text("−")
        }
        Text(value?.toString() ?: "—")
        IconButton(onClick = {
            val next = ((value ?: (range.first - 1)) + 1).coerceIn(range)
            onChange(next)
        }) {
            Text("+")
        }
    }
}

private fun flowDimensionLabel(index: Int): Int = when (index) {
    0 -> R.string.flow_dim_balance
    1 -> R.string.flow_dim_merge
    2 -> R.string.flow_dim_goals
    3 -> R.string.flow_dim_feedback
    4 -> R.string.flow_dim_focus
    5 -> R.string.flow_dim_control
    6 -> R.string.flow_dim_self
    7 -> R.string.flow_dim_time
    else -> R.string.flow_dim_interest
}
