package com.example.clockplannerproject.ui.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.ReportId
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskReport
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
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
        val context = LocalContext.current
        val launchers = rememberReportImportLaunchers { id, uri ->
            viewModel.onIntent(ReportsUiIntent.ImportAttachment(id, uri))
        }
        LaunchedEffect(viewModel) {
            viewModel.effect.collect { effect ->
                val text = when (effect) {
                    is ReportsUiEffect.ShowMessage -> effect.message
                    is ReportsUiEffect.ShowMessageRes -> context.getString(effect.resId)
                }
                snackbarHostState.showSnackbar(text)
            }
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
@Composable
fun ReportsScreen(
    state: ReportsUiState,
    onIntent: (ReportsUiIntent) -> Unit = {},
    onPickImage: (ReportId) -> Unit = {},
    onPickFile: (ReportId) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.reports_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.reports_cta),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        ReportCollectActions(
            enabled = !state.isCollecting,
            onIntent = onIntent,
        )
        val editingId = state.editingId
        if (editingId != null) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = { onIntent(ReportsUiIntent.ChangeDraft(it)) },
                label = { Text(stringResource(R.string.report_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onIntent(ReportsUiIntent.Save) }) {
                    Text(stringResource(R.string.report_save))
                }
                TextButton(onClick = { onIntent(ReportsUiIntent.Delete(editingId)) }) {
                    Text(stringResource(R.string.report_delete_note))
                }
            }
        }
        if (!state.isLoading && state.reports.isEmpty()) {
            Text(
                text = stringResource(R.string.reports_empty),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(state.reports, key = { it.id.value }) { report ->
                    ReportCard(
                        report = report,
                        onClick = { onIntent(ReportsUiIntent.Edit(report)) },
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
}

@Composable
private fun ReportCollectActions(
    enabled: Boolean,
    onIntent: (ReportsUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(
            onClick = { onIntent(ReportsUiIntent.CollectToday) },
            enabled = enabled,
        ) {
            Text(stringResource(R.string.reports_collect_today))
        }
        TextButton(
            onClick = { onIntent(ReportsUiIntent.CollectWeek) },
            enabled = enabled,
        ) {
            Text(stringResource(R.string.reports_collect_week))
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
                        text = "Ran the morning loop. Felt light.",
                    ),
                ),
            ),
        )
    }
}
