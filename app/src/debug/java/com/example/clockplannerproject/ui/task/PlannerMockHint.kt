package com.example.clockplannerproject.ui.task

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.clockplannerproject.R

/**
 * Short tester hint for the on-device mock words, compiled into debug builds only.
 *
 * @param modifier layout modifier
 * @since 1.1.0
 */
@Composable
internal fun PlannerMockHint(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.planner_debug_triggers),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
