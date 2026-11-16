package com.example.clockplannerproject.ui.day

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.OnTerracotta
import com.example.clockplannerproject.ui.theme.PaperContainerLow
import com.example.clockplannerproject.ui.theme.PaperInk
import com.example.clockplannerproject.ui.theme.PaperSecondary
import com.example.clockplannerproject.ui.theme.SandBorder
import com.example.clockplannerproject.ui.theme.TerracottaNow

/**
 * DIAL / PIZZA / PETALS / LIST. Icon plus the full label; chips wrap
 * instead of clipping Pizza or Petals. LIST is Compose, not a DialRenderer.
 *
 * @since 0.3.0
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ViewModeRow(
    selected: ViewMode,
    onSelect: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = ViewMode.entries
    FlowRow(
        modifier = modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        modes.forEach { mode ->
            val label = stringResource(viewModeLabel(mode))
            val description = stringResource(R.string.cd_view_mode, label)
            val isSelected = selected == mode
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) TerracottaNow else MaterialTheme.colorScheme.surfaceContainerLow,
                    )
                    .clickable { onSelect(mode) }
                    .semantics {
                        role = Role.Button
                        contentDescription = description
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = viewModeIcon(mode),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isSelected) OnTerracotta else PaperSecondary,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) OnTerracotta else PaperSecondary,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

/**
 * One-line caption for the active canvas or list mode.
 *
 * @since 1.1.0
 */
@Composable
fun ViewModeNote(
    mode: ViewMode,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(viewModeNote(mode)),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

private fun viewModeLabel(mode: ViewMode): Int = when (mode) {
    ViewMode.DIAL -> R.string.view_mode_dial
    ViewMode.PIZZA -> R.string.view_mode_pizza
    ViewMode.PETALS -> R.string.view_mode_petals
    ViewMode.LIST -> R.string.view_mode_list
}

private fun viewModeNote(mode: ViewMode): Int = when (mode) {
    ViewMode.DIAL -> R.string.day_mode_note_dial
    ViewMode.PIZZA -> R.string.day_mode_note_pizza
    ViewMode.PETALS -> R.string.day_mode_note_petals
    ViewMode.LIST -> R.string.day_mode_note_list
}

private fun viewModeIcon(mode: ViewMode): ImageVector = when (mode) {
    ViewMode.DIAL -> Icons.Filled.DonutLarge
    ViewMode.PIZZA -> Icons.Filled.PieChart
    ViewMode.PETALS -> Icons.Filled.FilterVintage
    ViewMode.LIST -> Icons.AutoMirrored.Filled.ViewList
}

private val ChromeChipHeight = 48.dp

/**
 * Day shell controls: AM/PM, the four view modes, and the full hide-done label.
 * Chips keep a 48dp tap height and wrap onto another row instead of clipping.
 *
 * @param quiet outline-only chips while a session is running
 * @since 0.5.0
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayChromeRow(
    showHalves: Boolean,
    selectedHalf: DialHalf,
    onSelectHalf: (DialHalf) -> Unit,
    selectedMode: ViewMode,
    onSelectMode: (ViewMode) -> Unit,
    hideCompleted: Boolean,
    onToggleHideCompleted: () -> Unit,
    quiet: Boolean,
    resetEnabled: Boolean,
    onResetRotation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (showHalves) {
            DialHalf.entries.forEach { half ->
                val label = stringResource(
                    if (half == DialHalf.AM) R.string.dial_am else R.string.dial_pm,
                )
                val description = stringResource(
                    if (half == DialHalf.AM) R.string.cd_half_am else R.string.cd_half_pm,
                )
                ChromeChip(
                    label = label,
                    isSelected = selectedHalf == half,
                    quiet = quiet,
                    contentDescription = description,
                    onClick = { onSelectHalf(half) },
                )
            }
        }
        ViewMode.entries.forEach { mode ->
            val label = stringResource(viewModeLabel(mode))
            ChromeChip(
                label = label,
                isSelected = selectedMode == mode,
                quiet = quiet,
                icon = viewModeIcon(mode),
                contentDescription = stringResource(R.string.cd_view_mode, label),
                onClick = { onSelectMode(mode) },
            )
        }
        ChromeChip(
            label = stringResource(R.string.day_hide_completed),
            isSelected = hideCompleted,
            quiet = quiet,
            contentDescription = stringResource(R.string.cd_hide_completed),
            onClick = onToggleHideCompleted,
        )
        if (resetEnabled) {
            val resetDescription = stringResource(R.string.cd_reset_rotation)
            IconButton(
                onClick = onResetRotation,
                modifier = Modifier
                    .size(ChromeChipHeight)
                    .semantics { contentDescription = resetDescription },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.RotateLeft,
                    contentDescription = null,
                    tint = PaperSecondary,
                )
            }
        }
    }
}

@Composable
private fun ChromeChip(
    label: String,
    isSelected: Boolean,
    quiet: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    val background = when {
        quiet -> Color.Transparent
        isSelected -> TerracottaNow
        else -> PaperContainerLow
    }
    val content = when {
        quiet && isSelected -> TerracottaNow
        isSelected && !quiet -> OnTerracotta
        else -> PaperInk
    }
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .height(ChromeChipHeight)
            .clip(shape)
            .then(
                if (quiet) {
                    Modifier.border(
                        width = 1.dp,
                        color = if (isSelected) TerracottaNow else SandBorder,
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            )
            .background(background)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                selected = isSelected
                this.contentDescription = contentDescription
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = content,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ViewModeRowPreview() {
    ClockPlannerProjectTheme {
        ViewModeRow(selected = ViewMode.PIZZA, onSelect = {})
    }
}

@Preview(showBackground = true, widthDp = 360, name = "Day chrome wraps")
@Composable
private fun DayChromeRowPreview() {
    ClockPlannerProjectTheme {
        DayChromeRow(
            showHalves = true,
            selectedHalf = DialHalf.AM,
            onSelectHalf = {},
            selectedMode = ViewMode.DIAL,
            onSelectMode = {},
            hideCompleted = false,
            onToggleHideCompleted = {},
            quiet = false,
            resetEnabled = false,
            onResetRotation = {},
        )
    }
}
