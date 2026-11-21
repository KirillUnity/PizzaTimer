package com.example.clockplannerproject.ui.day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.HardenKind

/**
 * One offer to harden the next similar task. Skip hides this trio.
 * Agreeing does not award points.
 *
 * @param title shared title of the three completions
 * @since 1.2.0
 */
@Composable
fun DifficultyOfferCard(
    title: String,
    onSkip: () -> Unit,
    onAccept: (HardenKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(R.string.nudge_title, title))
            TextButton(onClick = { onAccept(HardenKind.ALREADY_A_STEP) }) {
                Text(stringResource(R.string.nudge_step))
            }
            TextButton(onClick = { onAccept(HardenKind.EXPLICIT_REQUIREMENT) }) {
                Text(stringResource(R.string.nudge_requirement))
            }
            TextButton(onClick = { onAccept(HardenKind.NARROWER_RESULT) }) {
                Text(stringResource(R.string.nudge_narrower))
            }
            TextButton(onClick = onSkip) {
                Text(stringResource(R.string.nudge_skip))
            }
        }
    }
}
