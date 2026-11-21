package com.example.clockplannerproject.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.DayStateStats
import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskReport
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.OnTerracotta
import com.example.clockplannerproject.ui.theme.PaperCanvas
import com.example.clockplannerproject.ui.theme.PaperInk
import com.example.clockplannerproject.ui.theme.SandRest
import com.example.clockplannerproject.ui.theme.TerracottaNow
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ReportsDestinationContent(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) {
        ReportsScreen(state = ReportsUiState(isLoading = false), modifier = modifier)
    } else {
        val viewModel: ReportsViewModel = koinViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        var snackbarResId by remember { mutableStateOf<Int?>(null) }
        var snackbarLiteral by remember { mutableStateOf<String?>(null) }
        var snackbarNonce by remember { mutableIntStateOf(0) }
        val launchers = rememberReportImportLaunchers { id, uri ->
            viewModel.onIntent(ReportsUiIntent.ImportAttachment(id, uri))
        }
        LaunchedEffect(viewModel) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is ReportsUiEffect.ShowMessage -> {
                        snackbarLiteral = effect.message
                        snackbarResId = null
                        snackbarNonce += 1
                    }
                    is ReportsUiEffect.ShowMessageRes -> {
                        snackbarResId = effect.resId
                        snackbarLiteral = null
                        snackbarNonce += 1
                    }
                }
            }
        }
        val snackbarFromRes = snackbarResId?.let { stringResource(it) }
        val snackbarText = snackbarLiteral ?: snackbarFromRes
        LaunchedEffect(snackbarNonce) {
            val text = snackbarText ?: return@LaunchedEffect
            if (snackbarNonce == 0) return@LaunchedEffect
            snackbarHostState.showSnackbar(text)
        }
        androidx.compose.foundation.layout.Box(modifier) {
            ReportsScreen(
                state = state,
                onIntent = viewModel::onIntent,
                onPickImage = launchers.pickImage,
                onPickFile = launchers.pickFile,
                modifier = Modifier.fillMaxSize(),
            )
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}

/**
 * Paper-chrome list of notes. Each saved note can attach a local photo or file.
 *
 * @since 0.5.0
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportsScreen(
    state: ReportsUiState,
    onIntent: (ReportsUiIntent) -> Unit = {},
    onPickImage: (ReportId) -> Unit = {},
    onPickFile: (ReportId) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PaperCanvas)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
    ) {
        item(key = "header") {
            Text(
                text = stringResource(R.string.reports_eyebrow).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.reports_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            ReportCollectActions(
                enabled = !state.isCollecting,
                onIntent = onIntent,
            )
            DayStatsCard(stats = state.dayStats)
        }
        if (!state.isLoading && state.reports.isEmpty()) {
            item(key = "empty") {
                ReportsEmptyCard()
            }
        } else {
            items(state.reports, key = { it.id.value }) { report ->
                val editing = state.editingId == report.id
                ReportNoteCard(
                    report = report,
                    editing = editing,
                    draft = if (editing) state.draft else report.text,
                    onDraft = { onIntent(ReportsUiIntent.ChangeDraft(it)) },
                    onEdit = { onIntent(ReportsUiIntent.Edit(report)) },
                    onSave = { onIntent(ReportsUiIntent.Save) },
                    onDelete = { onIntent(ReportsUiIntent.Delete(report.id)) },
                    onPickImage = { onPickImage(report.id) },
                    onPickFile = { onPickFile(report.id) },
                    onRemoveAttachment = {
                        onIntent(ReportsUiIntent.RemoveAttachment(report.id))
                    },
                )
            }
        }
    }
}

/**
 * One calm card when the week (or the note list) has nothing closed.
 *
 * @since 1.1.0
 */
@Composable
private fun ReportsEmptyCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = stringResource(R.string.reports_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        )
    }
}

@Composable
private fun DayStatsCard(stats: DayStateStats) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        colors = CardDefaults.cardColors(containerColor = SandRest),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.day_stats_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.day_stats_self_report))
            Text(stringResource(R.string.day_stats_states, stats.states.joinToString()))
            Text(stringResource(R.string.day_stats_marks, stats.hindranceCount, stats.challengeCount))
            Text(stringResource(R.string.day_stats_thoughts))
            Text(stats.thoughts.joinToString())
            Text(stringResource(R.string.day_stats_skill, stats.meanSkill))
            Text(stringResource(R.string.day_stats_easier))
            Text(stats.easier.joinToString())
            Text(stringResource(R.string.day_stats_started, stats.startedCount))
            Text(stringResource(R.string.day_stats_form))
            Text(
                if (stats.formProfile.isEmpty()) {
                    ""
                } else {
                    stats.formProfile.joinToString { String.format(java.util.Locale.US, "%.1f", it) }
                },
            )
        }
    }
}

@Composable
private fun ReportCollectActions(
    enabled: Boolean,
    onIntent: (ReportsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = { onIntent(ReportsUiIntent.CollectToday) },
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TerracottaNow,
                contentColor = OnTerracotta,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Text(stringResource(R.string.reports_collect_today))
        }
        Button(
            onClick = { onIntent(ReportsUiIntent.CollectWeek) },
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SandRest,
                contentColor = PaperInk,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            Text(stringResource(R.string.reports_collect_week))
        }
    }
}

/**
 * One local note: time, text, a photo chip, Edit and Delete.
 *
 * @param editing when true, the draft replaces the saved text
 * @since 1.1.0
 */
@Composable
private fun ReportNoteCard(
    report: TaskReport,
    editing: Boolean,
    draft: String,
    onDraft: (String) -> Unit,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onPickImage: () -> Unit,
    onPickFile: () -> Unit,
    onRemoveAttachment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lines = report.text.lineSequence().toList()
    val title = lines.firstOrNull().orEmpty()
        .ifBlank { stringResource(R.string.report_preview_empty) }
    val body = lines.drop(1).joinToString("\n").trim()
    val cardCd = stringResource(R.string.cd_report_card, title)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardCd },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(TerracottaNow),
            )
            Column(modifier = Modifier.padding(start = 12.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)) {
                Text(
                    text = formatReportTime(report.createdAtEpochMillis),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!editing) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    if (body.isNotEmpty()) {
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = onDraft,
                        label = { Text(stringResource(R.string.report_hint)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        minLines = 3,
                    )
                    TextButton(onClick = onSave) {
                        Text(stringResource(R.string.report_save))
                    }
                }
                ReportPhotoChip(
                    attachment = report.attachment,
                    onPick = onPickImage,
                    onRemove = onRemoveAttachment,
                    modifier = Modifier.padding(top = 12.dp),
                )
                TextButton(onClick = onPickFile) {
                    Text(stringResource(R.string.report_file_chip))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    Button(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                        Text(
                            text = stringResource(R.string.report_action_edit),
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                    Button(
                        onClick = onDelete,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Text(
                            text = stringResource(R.string.report_action_delete),
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ReportCard(
    report: TaskReport,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPickImage: (() -> Unit)? = null,
    onPickFile: (() -> Unit)? = null,
    onRemoveAttachment: (() -> Unit)? = null,
) {
    val whenLabel = formatReportTime(report.createdAtEpochMillis)
    val preview = report.text.lineSequence().firstOrNull().orEmpty()
        .ifBlank { stringResource(R.string.report_preview_empty) }
    val cardCd = stringResource(R.string.cd_report_card, preview)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = cardCd },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = whenLabel, style = MaterialTheme.typography.labelMedium)
            Text(
                text = preview,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (onPickImage != null && onPickFile != null && onRemoveAttachment != null) {
                ReportAttachmentRow(
                    reportId = report.id,
                    attachment = report.attachment,
                    onPickImage = { onPickImage() },
                    onPickFile = { onPickFile() },
                    onRemove = { onRemoveAttachment() },
                )
            }
        }
    }
}

internal fun formatReportTime(epochMillis: Long): String {
    val local = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(local)
}

@Preview(showBackground = true)
@Composable
private fun ReportsScreenPreview() {
    ClockPlannerProjectTheme {
        ReportsScreen(
            state = ReportsUiState(
                isLoading = false,
                reports = listOf(
                    TaskReport(
                        id = ReportId("r1"),
                        taskId = TaskId("sport"),
                        createdAtEpochMillis = 1_728_000_000_000,
                        text = "Архитектура ядра\nВыделили интерфейсы хранилища.",
                        attachment = com.example.clockplannerproject.kit.core.ReportAttachment(
                            relativePath = "preview/board.jpg",
                            displayName = "Фото доски",
                            mimeType = "image/jpeg",
                        ),
                    ),
                ),
            ),
        )
    }
}

@Preview(showBackground = true, name = "Empty week")
@Composable
private fun ReportsEmptyPreview() {
    ClockPlannerProjectTheme {
        ReportsScreen(state = ReportsUiState(isLoading = false))
    }
}
