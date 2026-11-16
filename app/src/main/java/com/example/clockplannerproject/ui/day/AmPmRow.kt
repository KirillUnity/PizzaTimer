package com.example.clockplannerproject.ui.day

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme

/**
 * AM / PM face switcher for the 12-hour rings.
 *
 * @since 0.5.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmPmRow(
    selected: DialHalf,
    onSelect: (DialHalf) -> Unit,
    modifier: Modifier = Modifier,
) {
    val halves = DialHalf.entries
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.padding(start = 4.dp, end = 4.dp),
    ) {
        halves.forEachIndexed { index, half ->
            val label = stringResource(
                if (half == DialHalf.AM) R.string.dial_am else R.string.dial_pm,
            )
            val description = stringResource(
                if (half == DialHalf.AM) R.string.cd_half_am else R.string.cd_half_pm,
            )
            SegmentedButton(
                selected = selected == half,
                onClick = { onSelect(half) },
                shape = SegmentedButtonDefaults.itemShape(index, halves.size),
                modifier = Modifier.semantics { contentDescription = description },
            ) {
                Text(
                    text = label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AmPmRowPreview() {
    ClockPlannerProjectTheme {
        AmPmRow(selected = DialHalf.AM, onSelect = {})
    }
}
