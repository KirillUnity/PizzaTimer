package com.example.clockplannerproject.ui.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
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
        ReportsScreen(
            state = state,
            onIntent = viewModel::onIntent,
            modifier = modifier,
        )
    }
}

/**
 * Paper-chrome list of text reports. No media in v0.5.
 *
 * @since 0.5.0
 */
@Composable
fun ReportsScreen(
    state: ReportsUiState,
    onIntent: (ReportsUiIntent) -> Unit = {},
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
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )
        if (state.editingId != null) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = { onIntent(ReportsUiIntent.ChangeDraft(it)) },
                label = { Text(stringResource(R.string.report_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
            TextButton(onClick = { onIntent(ReportsUiIntent.Save) }) {
                Text(stringResource(R.string.report_save))
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
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(state.reports, key = { it.id.value }) { report ->
                    ReportCard(
                        report = report,
                        onClick = { onIntent(ReportsUiIntent.Edit(report)) },
                    )
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
) {
    val whenLabel = formatReportTime(report.createdAtEpochMillis)
    val preview = report.text.lineSequence().firstOrNull().orEmpty().ifBlank { "…" }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
