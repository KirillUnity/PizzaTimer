package com.example.clockplannerproject.ui.day

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.Appearance
import com.example.clockplannerproject.kit.core.AppearancePalette
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.config.BackgroundConfig
import com.example.clockplannerproject.kit.core.config.BackgroundPresets
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.PaperCanvas
import com.example.clockplannerproject.ui.theme.PaperInk
import com.example.clockplannerproject.ui.theme.TerracottaNow

/**
 * Paper side panel for the day look. Writes the same [Appearance] fields as Settings.
 * Hide-completed is a session flag, not a new preference.
 *
 * @param appearance current shared look; dark theme is left to the caller
 * @param hideCompleted when true, canvas views reflow and the list only filters
 * @param onAppearance called with the edited look
 * @param onToggleHideCompleted flips the existing hide-completed flag
 * @param onDismiss closes the panel
 * @param showCourseOffer when true, the short-practice row is shown
 * @param onOpenCourse opens the built-in course and should close this panel
 * @since 1.2.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaySideMenu(
    appearance: Appearance,
    hideCompleted: Boolean,
    onAppearance: (Appearance) -> Unit,
    onToggleHideCompleted: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showCourseOffer: Boolean = false,
    onOpenCourse: () -> Unit = {},
) {
    BoxScrim(onDismiss = onDismiss, modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.86f)
                .align(Alignment.CenterStart),
            shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
            color = PaperCanvas,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 8.dp, top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.day_menu_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = PaperInk,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.day_menu_close),
                        )
                    }
                }
                SectionTitle(stringResource(R.string.day_menu_view))
                ViewMode.entries.forEach { mode ->
                    ViewRow(
                        label = stringResource(viewModeLabel(mode)),
                        selected = appearance.viewMode == mode,
                        onClick = { onAppearance(appearance.copy(viewMode = mode)) },
                    )
                }
                SectionTitle(stringResource(R.string.day_menu_appearance))
                SectionTitle(stringResource(R.string.settings_accent))
                Swatches(
                    values = AppearancePalette.accents,
                    descriptions = listOf(
                        stringResource(R.string.cd_accent, stringResource(R.string.accent_terracotta)),
                        stringResource(R.string.cd_accent, stringResource(R.string.accent_emerald)),
                        stringResource(R.string.cd_accent, stringResource(R.string.accent_ink)),
                    ),
                    selected = appearance.accentArgb,
                    onSelect = { onAppearance(appearance.copy(accentArgb = it)) },
                )
                SectionTitle(stringResource(R.string.settings_completed_color))
                Swatches(
                    values = AppearancePalette.completed,
                    descriptions = listOf(
                        stringResource(
                            R.string.cd_completed_color,
                            stringResource(R.string.completed_warm),
                        ),
                        stringResource(
                            R.string.cd_completed_color,
                            stringResource(R.string.completed_cool),
                        ),
                        stringResource(
                            R.string.cd_completed_color,
                            stringResource(R.string.completed_ink),
                        ),
                    ),
                    selected = appearance.completedArgb,
                    onSelect = { onAppearance(appearance.copy(completedArgb = it)) },
                )
                SectionTitle(stringResource(R.string.settings_background))
                BackgroundChoices(
                    selected = appearance.background,
                    onSelect = { onAppearance(appearance.copy(background = it)) },
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.day_hide_completed),
                        style = MaterialTheme.typography.bodyLarge,
                        color = PaperInk,
                        modifier = Modifier.weight(1f),
                    )
                    val hideDescription = stringResource(R.string.cd_hide_completed)
                    Switch(
                        checked = hideCompleted,
                        onCheckedChange = { onToggleHideCompleted() },
                        modifier = Modifier.semantics { contentDescription = hideDescription },
                    )
                }
                if (showCourseOffer) {
                    TextButton(
                        onClick = onOpenCourse,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.course_offer),
                            style = MaterialTheme.typography.titleMedium,
                            color = TerracottaNow,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScrim(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
                .clickable(onClick = onDismiss),
        )
        content()
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = PaperInk.copy(alpha = 0.7f),
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun ViewRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val description = stringResource(R.string.cd_view_mode, label)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = PaperInk,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = TerracottaNow,
            )
        }
    }
}

@Composable
private fun Swatches(
    values: List<Long>,
    descriptions: List<String>,
    selected: Long,
    onSelect: (Long) -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        for (index in values.indices) {
            val argb = values[index]
            val description = descriptions.getOrElse(index) { "" }
            val ring = if (argb == selected) TerracottaNow else Color.Transparent
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ring)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color(argb.toInt()))
                    .clickable { onSelect(argb) }
                    .semantics { contentDescription = description },
            )
        }
    }
}

@Composable
private fun BackgroundChoices(
    selected: BackgroundConfig,
    onSelect: (BackgroundConfig) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LookChip(stringResource(R.string.background_none), selected is BackgroundConfig.None) {
            onSelect(BackgroundConfig.None)
        }
        LookChip(stringResource(R.string.background_color), selected is BackgroundConfig.Color) {
            onSelect(BackgroundConfig.Color(AppearancePalette.SOLID_SAND))
        }
        LookChip(
            stringResource(R.string.background_preset_parchment),
            selected is BackgroundConfig.Preset && selected.name == BackgroundPresets.PARCHMENT,
        ) { onSelect(BackgroundConfig.Preset(BackgroundPresets.PARCHMENT)) }
        LookChip(
            stringResource(R.string.background_preset_night),
            selected is BackgroundConfig.Preset && selected.name == BackgroundPresets.NIGHT_INK,
        ) { onSelect(BackgroundConfig.Preset(BackgroundPresets.NIGHT_INK)) }
        LookChip(
            stringResource(R.string.background_preset_terracotta),
            selected is BackgroundConfig.Preset && selected.name == BackgroundPresets.TERRACOTTA_WASH,
        ) { onSelect(BackgroundConfig.Preset(BackgroundPresets.TERRACOTTA_WASH)) }
        if (selected is BackgroundConfig.UserImage) {
            LookChip(stringResource(R.string.background_user_image), picked = true) {}
        }
    }
}

@Composable
private fun LookChip(
    label: String,
    picked: Boolean,
    onClick: () -> Unit,
) {
    val description = stringResource(R.string.cd_background_option, label)
    FilterChip(
        selected = picked,
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.semantics { contentDescription = description },
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun DaySideMenuPreview() {
    ClockPlannerProjectTheme {
        DaySideMenu(
            appearance = Appearance(viewMode = ViewMode.DIAL),
            hideCompleted = false,
            onAppearance = {},
            onToggleHideCompleted = {},
            onDismiss = {},
            showCourseOffer = true,
        )
    }
}
