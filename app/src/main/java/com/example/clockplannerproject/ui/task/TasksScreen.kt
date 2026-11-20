package com.example.clockplannerproject.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.InputChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockplannerproject.R
import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.RecurrenceRule
import kotlinx.datetime.DayOfWeek
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskDraftError
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskPalette
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.taskAppearances
import com.example.clockplannerproject.kit.core.time.TaskResize
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.kit.core.time.epochMillisToLocalDate
import com.example.clockplannerproject.kit.core.time.toEpochMillisAtStart
import com.example.clockplannerproject.ui.chrome.PaperDateHeader
import com.example.clockplannerproject.ui.stats.ReportCard
import com.example.clockplannerproject.ui.stats.rememberReportImportLaunchers
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.PaperOutline
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel

@Composable
fun TasksRoute(
    modifier: Modifier = Modifier,
    showEditorOverlays: Boolean = true,
    viewModel: TasksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    if (showEditorOverlays) {
        LaunchedEffect(viewModel) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is TasksUiEffect.ShowMessageRes ->
                        snackbarHostState.showSnackbar(context.getString(effect.resId))
                }
            }
        }
    }
    TasksScreen(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        showEditorOverlays = showEditorOverlays,
        modifier = modifier,
    )
}

@Composable
fun TasksDestinationContent(
    modifier: Modifier = Modifier,
    showEditorOverlays: Boolean = true,
) {
    if (LocalInspectionMode.current) {
        TasksScreen(
            state = TasksUiState(
                date = PreviewDate,
                tasks = SampleTasks.typicalDay(PreviewDate),
                isLoading = false,
            ),
            modifier = modifier,
        )
    } else {
        TasksRoute(modifier = modifier, showEditorOverlays = showEditorOverlays)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    state: TasksUiState,
    onIntent: (TasksUiIntent) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    showEditorOverlays: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        ResponsiveTaskGrid(
            tasks = state.visibleTasks,
            showEmpty = !state.isLoading && state.visibleTasks.isEmpty() && state.errorMessageRes == null,
            onEdit = { onIntent(TasksUiIntent.Edit(it)) },
            header = {
                PaperDateHeader(
                    date = state.date,
                    onPrevious = { onIntent(TasksUiIntent.PreviousDay) },
                    onNext = { onIntent(TasksUiIntent.NextDay) },
                    onPickDate = { onIntent(TasksUiIntent.OpenDatePicker) },
                    title = stringResource(R.string.nav_tasks),
                    subtitle = null,
                )
                PlanGoalEntry(onClick = { onIntent(TasksUiIntent.OpenPlan) })
                ProjectControls(
                    projects = state.availableProjects,
                    selected = state.selectedProject,
                    search = state.projectSearch,
                    onSearch = { onIntent(TasksUiIntent.ChangeProjectSearch(it)) },
                    onClearSearch = { onIntent(TasksUiIntent.ClearProjectSearch) },
                    onSelect = { onIntent(TasksUiIntent.FilterByProject(it)) },
                )
                if (state.availableTags.isNotEmpty() || state.selectedTag != null) {
                    TagFilterRow(
                        tags = state.availableTags,
                        selected = state.selectedTag,
                        onSelect = { onIntent(TasksUiIntent.FilterByTag(it)) },
                    )
                }
                state.errorMessageRes?.let { messageRes ->
                    Text(
                        text = stringResource(messageRes),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showEditorOverlays) {
        TaskEditorOverlays(state = state, onIntent = onIntent)
    }
}

@Composable
private fun ResponsiveTaskGrid(
    tasks: List<Task>,
    showEmpty: Boolean,
    onEdit: (Task) -> Unit,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = if (maxWidth >= 600.dp) 2 else 1
        val horizontalPadding = if (maxWidth >= 840.dp) 32.dp else 16.dp
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(top = 8.dp, bottom = TasksListBottomClearance),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                header()
            }
            if (showEmpty) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(R.string.tasks_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding, vertical = 24.dp),
                    )
                }
            } else {
                items(
                    taskAppearances(tasks),
                    key = { "${it.task.id.value}:${it.blockIndex ?: "untimed"}" },
                ) { row ->
                    TaskListRow(
                        task = row.task,
                        block = row.block,
                        onEdit = { onEdit(row.task) },
                        modifier = Modifier.padding(horizontal = horizontalPadding),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskListRow(
    task: Task,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    block: TimeBlock? = task.blocks.firstOrNull(),
) {
    val colorCd = stringResource(R.string.cd_task_color, task.title)
    val statusText = stringResource(statusLabel(task.status))
    val importanceText = stringResource(importanceLabel(task.importance))
    val statusCd = stringResource(R.string.cd_status_chip, statusText)
    val importanceCd = stringResource(R.string.cd_importance_chip, importanceText)
    val whenText = if (block == null) {
        stringResource(R.string.task_row_untimed)
    } else {
        TimeMath.formatRange(block.startMinute, block.endMinute)
    }
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, PaperOutline, shape)
            .clickable(onClick = onEdit)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 36.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                    .background(Color(task.colorArgb.toInt()))
                    .semantics { contentDescription = colorCd },
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics { contentDescription = statusCd },
                )
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = whenText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = importanceText,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.semantics { contentDescription = importanceCd },
                )
            }
        }
        if (task.normalizedTags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                task.normalizedTags.forEach { tag ->
                    val label = hashTag(tag)
                    val tagCd = stringResource(R.string.cd_task_tag, label)
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        softWrap = true,
                        modifier = Modifier.semantics { contentDescription = tagCd },
                    )
                }
            }
        }
    }
}

/**
 * Editor sheet, time picker, delete confirm, and date picker.
 * Hosted at the app root so the center Create FAB works on every tab.
 *
 * @since 0.2.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorOverlays(
    state: TasksUiState,
    onIntent: (TasksUiIntent) -> Unit,
) {
    state.editor?.let { editor ->
        val date = state.date
        if (date != null) {
            TaskEditorSheet(
                editor = editor,
                nowMinute = state.nowMinute,
                reports = state.reports,
                reportDraft = state.reportDraft,
                knownTags = state.availableTags,
                onIntent = onIntent,
            )
        }
    }

    state.pendingDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { onIntent(TasksUiIntent.DismissDelete) },
            title = { Text(stringResource(R.string.task_delete_title)) },
            text = { Text(stringResource(R.string.task_delete_body, task.title)) },
            confirmButton = {
                TextButton(onClick = { onIntent(TasksUiIntent.ConfirmDelete) }) {
                    Text(stringResource(R.string.task_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(TasksUiIntent.DismissDelete) }) {
                    Text(stringResource(R.string.task_delete_cancel))
                }
            },
        )
    }

    if (state.showDatePicker && state.date != null) {
        TaskDatePickerDialog(
            selectedDate = state.date,
            onConfirm = { onIntent(TasksUiIntent.SelectDate(it)) },
            onDismiss = { onIntent(TasksUiIntent.DismissDatePicker) },
        )
    }

    state.pendingMove?.let { moving ->
        TaskDatePickerDialog(
            selectedDate = moving.date ?: state.date ?: return@let,
            onConfirm = { onIntent(TasksUiIntent.ConfirmMove(it)) },
            onDismiss = { onIntent(TasksUiIntent.DismissMove) },
        )
    }

    PlanSheetHost(state = state, onIntent = onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDatePickerDialog(
    selectedDate: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochMillisAtStart(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        onConfirm(epochMillisToLocalDate(millis))
                    } else {
                        onDismiss()
                    }
                },
            ) {
                Text(stringResource(R.string.task_picker_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.task_picker_cancel))
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TaskEditorSheet(
    editor: TaskEditorState,
    nowMinute: Int,
    reports: List<com.example.clockplannerproject.kit.core.TaskReport> = emptyList(),
    reportDraft: String = "",
    knownTags: List<String> = emptyList(),
    onIntent: (TasksUiIntent) -> Unit,
) {
    val launchers = rememberReportImportLaunchers { id, uri ->
        onIntent(TasksUiIntent.ImportReportAttachment(id, uri))
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { onIntent(TasksUiIntent.DismissEditor) },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .navigationBarsPadding(),
        ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.task_editor_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = editor.title,
                onValueChange = { onIntent(TasksUiIntent.ChangeTitle(it)) },
                label = { Text(stringResource(R.string.task_title)) },
                isError = editor.error is TaskDraftError.EmptyTitle,
                supportingText = {
                    if (editor.error is TaskDraftError.EmptyTitle) {
                        Text(stringResource(R.string.task_error_empty_title))
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = editor.description,
                onValueChange = { onIntent(TasksUiIntent.ChangeDescription(it)) },
                label = { Text(stringResource(R.string.task_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.task_color),
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TaskPalette.colors.forEach { color ->
                    val selected = color == editor.colorArgb
                    val swatchCd = stringResource(R.string.cd_palette_swatch)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(color.toInt()))
                            .then(
                                if (selected) {
                                    Modifier.border(
                                        3.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        CircleShape,
                                    )
                                } else {
                                    Modifier
                                },
                            )
                            .clickable { onIntent(TasksUiIntent.ChangeColor(color)) }
                            .semantics { contentDescription = swatchCd },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            ImportanceRow(
                selected = editor.importance,
                onSelect = { onIntent(TasksUiIntent.ChangeImportance(it)) },
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = editor.project,
                onValueChange = { onIntent(TasksUiIntent.ChangeProject(it)) },
                label = { Text(stringResource(R.string.task_project)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            TagEditor(
                tags = editor.tags,
                draft = editor.tagDraft,
                knownTags = knownTags,
                onDraft = { onIntent(TasksUiIntent.ChangeTagDraft(it)) },
                onAdd = { onIntent(TasksUiIntent.AddTag(it)) },
                onRemove = { onIntent(TasksUiIntent.RemoveTag(it)) },
            )
            Spacer(Modifier.height(12.dp))
            FilterChip(
                selected = editor.scheduledDate == null,
                onClick = {
                    onIntent(TasksUiIntent.ChangeScheduled(editor.scheduledDate == null))
                },
                label = { Text(stringResource(R.string.task_backlog_toggle)) },
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.task_status),
                style = MaterialTheme.typography.labelLarge,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TaskStatus.entries.forEach { status ->
                    FilterChip(
                        selected = editor.status == status,
                        onClick = { onIntent(TasksUiIntent.ChangeStatus(status)) },
                        label = { Text(stringResource(statusLabel(status))) },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            if (editor.blocks.isEmpty()) {
                Text(
                    text = stringResource(R.string.task_untimed_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = stringResource(R.string.task_intervals),
                    style = MaterialTheme.typography.labelLarge,
                )
                editor.blocks.forEachIndexed { index, block ->
                    val closed = block.isClosed(nowMinute)
                    val end = block.endMinute
                    IntervalRow(
                        block = block,
                        readOnly = closed,
                        overnight = end != null &&
                            end != TimeMath.MINUTES_PER_DAY &&
                            end.mod(TimeMath.MINUTES_PER_DAY) <
                            block.startMinute.mod(TimeMath.MINUTES_PER_DAY),
                        onEndEarlier = {
                            onIntent(
                                TasksUiIntent.ChangeBlockEnd(
                                    index,
                                    endAfterStep(block, -EndStepMinutes),
                                ),
                            )
                        },
                        onEndLater = {
                            onIntent(
                                TasksUiIntent.ChangeBlockEnd(
                                    index,
                                    endAfterStep(block, EndStepMinutes),
                                ),
                            )
                        },
                    )
                }
            }
            if (editor.error is TaskDraftError.EndEqualsStart) {
                Text(
                    text = stringResource(R.string.task_error_end_equals_start),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            TextButton(
                onClick = { onIntent(TasksUiIntent.AddBlock) },
                modifier = Modifier.height(48.dp),
            ) {
                Text(stringResource(R.string.task_add_interval))
            }
            if (editor.scheduledDate != null) {
                Spacer(Modifier.height(8.dp))
                RecurrenceEditor(
                    rule = editor.recurrence,
                    onChange = { onIntent(TasksUiIntent.ChangeRecurrence(it)) },
                )
            }
            if (!editor.isNew) {
                Spacer(Modifier.height(8.dp))
                EditorAction(
                    label = stringResource(
                        if (editor.scheduledDate == null) {
                            R.string.task_move_to_diagram
                        } else {
                            R.string.task_move
                        },
                    ),
                    onClick = {
                        val task = editor.toTask()
                        if (task.date == null) {
                            onIntent(TasksUiIntent.MoveToDiagram(task))
                        } else {
                            onIntent(TasksUiIntent.RequestMove(task))
                        }
                    },
                )
                EditorAction(
                    label = stringResource(R.string.task_duplicate),
                    onClick = { onIntent(TasksUiIntent.Duplicate(editor.toTask())) },
                )
                EditorAction(
                    label = stringResource(R.string.task_delete),
                    onClick = { onIntent(TasksUiIntent.Delete(editor.toTask())) },
                    destructive = true,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.task_reports),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = reportDraft,
                    onValueChange = { onIntent(TasksUiIntent.ChangeReportDraft(it)) },
                    label = { Text(stringResource(R.string.report_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                TextButton(onClick = { onIntent(TasksUiIntent.SaveReport) }) {
                    Text(stringResource(R.string.report_save))
                }
                reports.forEach { report ->
                    ReportCard(
                        report = report,
                        onClick = { onIntent(TasksUiIntent.EditReport(report)) },
                        onPickImage = { launchers.pickImage(report.id) },
                        onPickFile = { launchers.pickFile(report.id) },
                        onRemoveAttachment = {
                            onIntent(TasksUiIntent.RemoveReportAttachment(report.id))
                        },
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
            Button(
                onClick = { onIntent(TasksUiIntent.Update) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .height(48.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            ) {
                Text(stringResource(R.string.task_save))
            }
        }
    }
}

@Composable
private fun ProjectControls(
    projects: List<String>,
    selected: String?,
    search: String,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSelect: (String?) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = search,
            onValueChange = onSearch,
            label = { Text(stringResource(R.string.task_project_search)) },
            singleLine = true,
            trailingIcon = {
                if (search.isNotEmpty()) {
                    TextButton(
                        onClick = onClearSearch,
                        modifier = Modifier.height(48.dp),
                    ) {
                        Text(stringResource(R.string.task_project_search_clear))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.task_projects_all)) },
            )
            projects.forEach { project ->
                FilterChip(
                    selected = selected.equals(project, ignoreCase = true),
                    onClick = { onSelect(if (selected.equals(project, true)) null else project) },
                    label = { Text(project) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagFilterRow(
    tags: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(R.string.task_tags_all)) },
        )
        tags.forEach { tag ->
            val label = hashTag(tag)
            val selectedTag = selected.equals(tag, ignoreCase = true)
            FilterChip(
                selected = selectedTag,
                onClick = { onSelect(if (selectedTag) null else tag) },
                label = { Text(label, softWrap = true) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagEditor(
    tags: List<String>,
    draft: String,
    knownTags: List<String>,
    onDraft: (String) -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    Text(
        text = stringResource(R.string.task_tags),
        style = MaterialTheme.typography.labelLarge,
    )
    if (tags.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tags.forEach { tag ->
                val removeCd = stringResource(R.string.cd_remove_tag, tag)
                InputChip(
                    selected = true,
                    onClick = { onRemove(tag) },
                    label = { Text(tag) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = removeCd,
                        )
                    },
                )
            }
        }
    }
    val suggestions = knownTags.filter { known ->
        tags.none { it.equals(known, ignoreCase = true) }
    }
    if (suggestions.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestions.forEach { tag ->
                FilterChip(
                    selected = false,
                    onClick = { onAdd(tag) },
                    label = { Text(tag) },
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = onDraft,
            label = { Text(stringResource(R.string.task_tag_add)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAdd(draft) }),
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = { onAdd(draft) },
            enabled = draft.isNotBlank(),
        ) {
            Text(stringResource(R.string.task_tag_confirm))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ImportanceRow(
    selected: Importance,
    onSelect: (Importance) -> Unit,
) {
    Text(
        text = stringResource(R.string.task_importance),
        style = MaterialTheme.typography.labelLarge,
    )
    Spacer(Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Importance.entries.forEach { level ->
            val label = stringResource(importanceLabel(level))
            val description = stringResource(R.string.cd_importance_chip, label)
            FilterChip(
                selected = selected == level,
                onClick = { onSelect(level) },
                label = { Text(label) },
                modifier = Modifier.semantics { contentDescription = description },
            )
        }
    }
}

@Composable
private fun EditorAction(
    label: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Text(
            text = label,
            color = if (destructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * One civil segment. A closed block is text and a lock only.
 * An open block can move its end in [EndStepMinutes] steps.
 */
@Composable
private fun IntervalRow(
    block: TimeBlock,
    readOnly: Boolean,
    overnight: Boolean,
    onEndEarlier: () -> Unit,
    onEndLater: () -> Unit,
) {
    val range = TimeMath.formatRange(block.startMinute, block.endMinute)
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (readOnly) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = range,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (readOnly) {
            Text(
                text = stringResource(R.string.task_interval_locked),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onEndEarlier,
                    modifier = Modifier.height(48.dp),
                ) {
                    Text(stringResource(R.string.task_end_earlier))
                }
                TextButton(
                    onClick = onEndLater,
                    modifier = Modifier.height(48.dp),
                ) {
                    Text(stringResource(R.string.task_end_later))
                }
            }
        }
        if (overnight) {
            Text(
                text = stringResource(R.string.task_overnight_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecurrenceEditor(
    rule: RecurrenceRule,
    onChange: (RecurrenceRule) -> Unit,
) {
    Text(
        text = stringResource(R.string.task_recurrence),
        style = MaterialTheme.typography.labelLarge,
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = rule is RecurrenceRule.None,
            onClick = { onChange(RecurrenceRule.None) },
            label = { Text(stringResource(R.string.task_recurrence_none)) },
        )
        FilterChip(
            selected = rule is RecurrenceRule.Daily,
            onClick = { onChange(RecurrenceRule.Daily) },
            label = { Text(stringResource(R.string.task_recurrence_daily)) },
        )
        FilterChip(
            selected = rule is RecurrenceRule.Weekdays,
            onClick = {
                onChange(
                    RecurrenceRule.Weekdays(
                        setOf(
                            DayOfWeek.MONDAY,
                            DayOfWeek.TUESDAY,
                            DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY,
                            DayOfWeek.FRIDAY,
                        ),
                    ),
                )
            },
            label = { Text(stringResource(R.string.task_recurrence_weekdays)) },
        )
    }
    if (rule is RecurrenceRule.Weekdays) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DayOfWeek.entries.forEach { day ->
                val selected = day in rule.days
                val label = java.time.DayOfWeek.valueOf(day.name)
                    .getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault())
                FilterChip(
                    selected = selected,
                    onClick = {
                        val next = if (selected) rule.days - day else rule.days + day
                        onChange(
                            if (next.isEmpty()) RecurrenceRule.None else RecurrenceRule.Weekdays(next),
                        )
                    },
                    label = { Text(label) },
                    modifier = Modifier.semantics { contentDescription = label },
                )
            }
        }
    }
}

private const val EndStepMinutes: Int = 15

/** Clears the tasks FAB so the last card stays fully on screen. */
private val TasksListBottomClearance = 128.dp

private fun hashTag(tag: String): String =
    if (tag.startsWith("#")) tag else "#$tag"

/**
 * New civil end after a ±[EndStepMinutes] step. Closed blocks never call this.
 */
private fun endAfterStep(block: TimeBlock, deltaMinutes: Int): Int {
    val originalEnd = block.endMinute ?: (block.startMinute + EndStepMinutes)
    return TaskResize.endMinuteAfterDelta(
        startMinute = block.startMinute,
        originalEndMinute = originalEnd,
        deltaMinutes = deltaMinutes.toFloat(),
        snapMinutes = EndStepMinutes,
        minDuration = EndStepMinutes,
    )
}

private fun importanceLabel(importance: Importance): Int = when (importance) {
    Importance.HIGH -> R.string.task_importance_high
    Importance.MEDIUM -> R.string.task_importance_medium
    Importance.LOW -> R.string.task_importance_low
}

private fun statusLabel(status: TaskStatus): Int = when (status) {
    TaskStatus.TODO -> R.string.task_status_todo
    TaskStatus.IN_PROGRESS -> R.string.task_status_in_progress
    TaskStatus.DONE -> R.string.task_status_done
}

private val PreviewDate = LocalDate(2026, 10, 6)

@Preview(showBackground = true, name = "Tasks compact short", widthDp = 360, heightDp = 640)
@Composable
private fun TasksScreenPreview() {
    ClockPlannerProjectTheme {
        TasksScreen(
            state = TasksUiState(
                date = PreviewDate,
                tasks = SampleTasks.typicalDay(PreviewDate),
                isLoading = false,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Tasks medium grid", widthDp = 700, heightDp = 900)
@Composable
private fun TasksScreenMediumPreview() {
    ClockPlannerProjectTheme {
        TasksScreen(
            state = TasksUiState(
                date = PreviewDate,
                tasks = SampleTasks.typicalDay(PreviewDate) +
                    SampleTasks.sportTwoBlocks(PreviewDate),
                isLoading = false,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TasksScreenEmptyPreview() {
    ClockPlannerProjectTheme {
        TasksScreen(
            state = TasksUiState(
                date = PreviewDate,
                isLoading = false,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Tasks inventory", widthDp = 390, heightDp = 844)
@Composable
private fun TasksInventoryPreview() {
    ClockPlannerProjectTheme {
        TasksScreen(
            state = TasksUiState(
                date = PreviewDate,
                tasks = listOf(
                    SampleTasks.sportTwoBlocks(PreviewDate),
                    Task(
                        id = TaskId("design"),
                        title = "Полировка интерфейса",
                        colorArgb = 0xFF2E7D52,
                        date = PreviewDate,
                        importance = Importance.HIGH,
                        tags = listOf("Дизайн"),
                    ),
                ),
                isLoading = false,
                nowMinute = 10 * 60,
            ),
            showEditorOverlays = false,
        )
    }
}

@Preview(showBackground = true, name = "Timed editor", widthDp = 390, heightDp = 844)
@Composable
private fun TimedEditorPreview() {
    ClockPlannerProjectTheme {
        TasksScreen(
            state = TasksUiState(
                date = PreviewDate,
                tasks = listOf(SampleTasks.sportTwoBlocks(PreviewDate)),
                editor = TaskEditorState.from(SampleTasks.sportTwoBlocks(PreviewDate)),
                isLoading = false,
                nowMinute = 10 * 60,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Untimed editor", widthDp = 390, heightDp = 844)
@Composable
private fun UntimedEditorPreview() {
    ClockPlannerProjectTheme {
        TasksScreen(
            state = TasksUiState(
                date = PreviewDate,
                editor = TaskEditorState(scheduledDate = PreviewDate),
                isLoading = false,
                nowMinute = 10 * 60,
            ),
        )
    }
}
