package com.example.clockplannerproject.ui.day

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * DIAL / PIZZA / PETALS / LIST. LIST is Compose, not a DialRenderer.
 *
 * @since 0.3.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewModeRow(
    selected: ViewMode,
    onSelect: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = ViewMode.entries
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        modes.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = selected == mode,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                modifier = Modifier.widthIn(min = 76.dp),
            ) {
                Text(stringResource(viewModeLabel(mode)))
            }
        }
    }
}

private fun viewModeLabel(mode: ViewMode): Int = when (mode) {
    ViewMode.DIAL -> R.string.view_mode_dial
    ViewMode.PIZZA -> R.string.view_mode_pizza
    ViewMode.PETALS -> R.string.view_mode_petals
    ViewMode.LIST -> R.string.view_mode_list
}

@Preview(showBackground = true)
@Composable
private fun ViewModeRowPreview() {
    ClockPlannerProjectTheme {
        ViewModeRow(selected = ViewMode.PIZZA, onSelect = {})
    }
}
