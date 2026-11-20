package com.example.clockplannerproject.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.Appearance
import com.example.clockplannerproject.kit.core.AppearancePalette
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.config.BackgroundConfig
import com.example.clockplannerproject.kit.compose.userUnderlayReadable
import com.example.clockplannerproject.kit.core.config.BackgroundPresets
import com.example.clockplannerproject.ui.settings.practice.PracticeDestination
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.PaperCanvas
import com.example.clockplannerproject.ui.theme.PaperSecondary
import com.example.clockplannerproject.ui.theme.TerracottaNow
import org.koin.androidx.compose.koinViewModel

/**
 * Settings destination. Persists through [SettingsViewModel].
 *
 * @param onShowOnboarding opens the intro again without clearing other prefs
 * @since 1.0.0
 */
@Composable
fun SettingsDestinationContent(
    onShowOnboarding: () -> Unit,
    onOpenDay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var practiceOpen by rememberSaveable { mutableStateOf(false) }
    if (practiceOpen && !LocalInspectionMode.current) {
        PracticeDestination(
            onBack = { practiceOpen = false },
            onOpenDay = onOpenDay,
            onShowIntroduction = onShowOnboarding,
            modifier = modifier,
        )
        return
    }
    if (LocalInspectionMode.current) {
        SettingsScreen(
            appearance = Appearance.Default,
            onAppearance = {},
            onShowOnboarding = onShowOnboarding,
            onOpenPractice = {},
            onPickPhoto = {},
            modifier = modifier,
        )
    } else {
        val viewModel: SettingsViewModel = koinViewModel()
        val appearance by viewModel.appearance.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val picker = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            viewModel.importPhoto {
                context.contentResolver.openInputStream(uri)
                    ?: error("Could not read photo")
            }
        }
        SettingsScreen(
            appearance = appearance,
            onAppearance = viewModel::save,
            onShowOnboarding = onShowOnboarding,
            onOpenPractice = { practiceOpen = true },
            onPickPhoto = {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            modifier = modifier,
        )
    }
}

/**
 * Paper settings: default view, accent, completed color, dial underlay.
 *
 * «Своё фото» opens the system photo picker. The stored path is an app file.
 * A missing file shows [R.string.background_user_missing].
 *
 * @param onPickPhoto launches the image picker; the caller copies the result
 * @since 1.0.0
 */
@Composable
fun SettingsScreen(
    appearance: Appearance,
    onAppearance: (Appearance) -> Unit,
    onShowOnboarding: () -> Unit,
    onOpenPractice: () -> Unit,
    onPickPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaperCanvas)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        SectionLabel(stringResource(R.string.settings_view_mode))
        SettingsViewModes(
            selected = appearance.viewMode,
            onSelect = { onAppearance(appearance.copy(viewMode = it)) },
        )
        SectionLabel(stringResource(R.string.settings_accent))
        ColorSwatches(
            values = AppearancePalette.accents,
            labels = listOf(
                stringResource(R.string.accent_terracotta),
                stringResource(R.string.accent_emerald),
                stringResource(R.string.accent_ink),
            ),
            selected = appearance.accentArgb,
            descriptionFor = { label -> stringResource(R.string.cd_accent, label) },
            onSelect = { onAppearance(appearance.copy(accentArgb = it)) },
        )
        SectionLabel(stringResource(R.string.settings_completed_color))
        ColorSwatches(
            values = AppearancePalette.completed,
            labels = listOf(
                stringResource(R.string.completed_warm),
                stringResource(R.string.completed_cool),
                stringResource(R.string.completed_ink),
            ),
            selected = appearance.completedArgb,
            descriptionFor = { label -> stringResource(R.string.cd_completed_color, label) },
            onSelect = { onAppearance(appearance.copy(completedArgb = it)) },
        )
        SectionLabel(stringResource(R.string.settings_background))
        BackgroundChips(
            selected = appearance.background,
            onSelect = { onAppearance(appearance.copy(background = it)) },
            onPickPhoto = onPickPhoto,
        )
        val photoPath = (appearance.background as? BackgroundConfig.UserImage)?.uri
        val photoMissing = remember(photoPath) {
            photoPath != null && !userUnderlayReadable(photoPath)
        }
        if (photoMissing) {
            Text(
                text = stringResource(R.string.background_user_missing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        val darkCd = stringResource(R.string.cd_settings_dark)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_dark),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Switch(
                checked = appearance.darkTheme,
                onCheckedChange = { onAppearance(appearance.copy(darkTheme = it)) },
                modifier = Modifier.semantics { contentDescription = darkCd },
            )
        }
        val practiceCd = stringResource(R.string.cd_settings_practice)
        TextButton(
            onClick = onOpenPractice,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .semantics { contentDescription = practiceCd },
        ) {
            Text(
                text = stringResource(R.string.settings_practice),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        val replayCd = stringResource(R.string.cd_show_onboarding)
        TextButton(
            onClick = onShowOnboarding,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .semantics { contentDescription = replayCd },
        ) {
            Text(
                text = stringResource(R.string.settings_show_onboarding),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

/**
 * Default view. The dial card's caption is the 12-hour face, never a 24-hour label.
 *
 * @since 1.1.0
 */
@Composable
private fun SettingsViewModes(
    selected: ViewMode,
    onSelect: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsModeCard(
                label = stringResource(R.string.view_mode_dial),
                caption = stringResource(R.string.settings_dial_caption),
                selected = selected == ViewMode.DIAL,
                onClick = { onSelect(ViewMode.DIAL) },
                modifier = Modifier.weight(1f),
            )
            SettingsModeCard(
                label = stringResource(R.string.view_mode_pizza),
                caption = null,
                selected = selected == ViewMode.PIZZA,
                onClick = { onSelect(ViewMode.PIZZA) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsModeCard(
                label = stringResource(R.string.view_mode_petals),
                caption = null,
                selected = selected == ViewMode.PETALS,
                onClick = { onSelect(ViewMode.PETALS) },
                modifier = Modifier.weight(1f),
            )
            SettingsModeCard(
                label = stringResource(R.string.view_mode_list),
                caption = null,
                selected = selected == ViewMode.LIST,
                onClick = { onSelect(ViewMode.LIST) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SettingsModeCard(
    label: String,
    caption: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.cd_view_mode, label)
    Column(
        modifier = modifier
            .heightIn(min = 72.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) TerracottaNow else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .semantics { contentDescription = description }
            .padding(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (caption != null) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = PaperSecondary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSwatches(
    values: List<Long>,
    labels: List<String>,
    selected: Long,
    descriptionFor: @Composable (String) -> String,
    onSelect: (Long) -> Unit,
) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        values.forEachIndexed { index, argb ->
            val label = labels[index]
            val cd = descriptionFor(label)
            Column(
                modifier = Modifier.widthIn(max = 96.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(if (selected == argb) 40.dp else 36.dp)
                        .clip(CircleShape)
                        .background(Color(argb.toInt()))
                        .border(
                            width = if (selected == argb) 2.dp else 0.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape,
                        )
                        .clickable { onSelect(argb) }
                        .semantics { contentDescription = cd },
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun BackgroundChips(
    selected: BackgroundConfig,
    onSelect: (BackgroundConfig) -> Unit,
    onPickPhoto: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BackgroundChip(
            label = stringResource(R.string.background_none),
            picked = selected is BackgroundConfig.None,
            onClick = { onSelect(BackgroundConfig.None) },
        )
        BackgroundChip(
            label = stringResource(R.string.background_color),
            picked = selected is BackgroundConfig.Color,
            onClick = { onSelect(BackgroundConfig.Color(AppearancePalette.SOLID_SAND)) },
        )
        BackgroundChip(
            label = stringResource(R.string.background_preset_parchment),
            picked = selected is BackgroundConfig.Preset && selected.name == BackgroundPresets.PARCHMENT,
            onClick = { onSelect(BackgroundConfig.Preset(BackgroundPresets.PARCHMENT)) },
        )
        BackgroundChip(
            label = stringResource(R.string.background_preset_night),
            picked = selected is BackgroundConfig.Preset && selected.name == BackgroundPresets.NIGHT_INK,
            onClick = { onSelect(BackgroundConfig.Preset(BackgroundPresets.NIGHT_INK)) },
        )
        BackgroundChip(
            label = stringResource(R.string.background_preset_terracotta),
            picked = selected is BackgroundConfig.Preset &&
                selected.name == BackgroundPresets.TERRACOTTA_WASH,
            onClick = { onSelect(BackgroundConfig.Preset(BackgroundPresets.TERRACOTTA_WASH)) },
        )
        BackgroundChip(
            label = stringResource(R.string.background_user_image),
            picked = selected is BackgroundConfig.UserImage,
            onClick = onPickPhoto,
        )
    }
}

@Composable
private fun BackgroundChip(
    label: String,
    picked: Boolean,
    onClick: () -> Unit,
) {
    val cd = stringResource(R.string.cd_background_option, label)
    FilterChip(
        selected = picked,
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.semantics { contentDescription = cd },
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    ClockPlannerProjectTheme {
        SettingsScreen(
            appearance = Appearance(
                viewMode = ViewMode.PIZZA,
                background = BackgroundConfig.Preset(BackgroundPresets.PARCHMENT),
            ),
            onAppearance = {},
            onShowOnboarding = {},
            onOpenPractice = {},
            onPickPhoto = {},
        )
    }
}
