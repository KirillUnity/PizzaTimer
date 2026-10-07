package com.example.clockplannerproject.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.InputChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockplannerproject.R
import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskDraftError
import com.example.clockplannerproject.kit.core.TaskPalette
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.kit.core.time.epochMillisToLocalDate
import com.example.clockplannerproject.kit.core.time.toEpochMillisAtStart
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun TasksRoute(
    modifier: Modifier = Modifier,
    showEditorOverlays: Boolean = true,
    viewModel: TasksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    if (showEditorOverlays) {
        LaunchedEffect(viewModel) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is TasksUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
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
        Column(modifier = Modifier.fillMaxSize()) {
            DateHeader(
                date = state.date,
                onPrevious = { onIntent(TasksUiIntent.PreviousDay) },
                onNext = { onIntent(TasksUiIntent.NextDay) },
                onPickDate = { onIntent(TasksUiIntent.OpenDatePicker) },
            )
            if (state.availableTags.isNotEmpty()) {
                TagFilterRow(
                    tags = state.availableTags,
                    selected = state.tagFilter,
                    onSelect = { onIntent(TasksUiIntent.FilterByTag(it)) },
                )
            }
            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            }
            if (!state.isLoading && state.visibleTasks.isEmpty() && state.errorMessage == null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.tasks_empty),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.visibleTasks, key = { it.id.value }) { task ->
                        TaskListRow(
                            task = task,
                            onEdit = { onIntent(TasksUiIntent.Edit(task)) },
                            onDelete = { onIntent(TasksUiIntent.Delete(task)) },
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
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
private fun DateHeader(
    date: LocalDate?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPickDate: () -> Unit,
) {
    val label = if (date == null) {
        stringResource(R.string.tasks_date_loading)
    } else {
        remember(date) {
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .format(date.toJavaLocalDate())
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious, enabled = date != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.cd_previous_day),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onPickDate, enabled = date != null) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = stringResource(R.string.cd_pick_date),
                )
            }
        }
        IconButton(onClick = onNext, enabled = date != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.cd_next_day),
            )
        }
    }
}

@Composable
private fun TaskListRow(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val colorCd = stringResource(R.string.cd_task_color, task.title)
    ListItem(
        headlineContent = { Text(task.title) },
        supportingContent = {
            val schedule = if (task.isUntimed) {
                stringResource(R.string.task_untimed)
            } else {
                TimeMath.formatBlocks(task.blocks)
            }
            val tags = if (task.tags.isEmpty()) "" else " · ${task.tags.joinToString()}"
            Text(
                text = "$schedule · ${stringResource(statusLabel(task.status))}$tags",
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(task.colorArgb.toInt()))
                    .semantics { contentDescription = colorCd },
            )
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.cd_delete_task, task.title),
                )
            }
        },
        modifier = Modifier.clickable(onClick = onEdit),
    )
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
                date = date,
                nowMinute = state.nowMinute,
                onIntent = onIntent,
            )
        }
    }

    val picker = state.timePicker
    val editorForPicker = state.editor
    if (picker != null && editorForPicker != null) {
        val block = editorForPicker.blocks.getOrNull(picker.blockIndex)
        if (block != null) {
            ClockTimePickerDialog(
                minuteOfDay = if (picker.isStart) block.startMinute else block.endMinute,
                title = stringResource(
                    if (picker.isStart) R.string.task_start else R.string.task_end,
                ),
                onConfirm = { picked ->
                    if (picker.isStart) {
                        onIntent(TasksUiIntent.ChangeBlockStart(picker.blockIndex, picked))
                    } else {
                        onIntent(TasksUiIntent.ChangeBlockEnd(picker.blockIndex, picked))
                    }
                    onIntent(TasksUiIntent.DismissTimePicker)
                },
                onDismiss = { onIntent(TasksUiIntent.DismissTimePicker) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditorSheet(
    editor: TaskEditorState,
    date: LocalDate,
    nowMinute: Int,
    onIntent: (TasksUiIntent) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { onIntent(TasksUiIntent.DismissEditor) },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = stringResource(
                    if (editor.isNew) R.string.task_editor_create else R.string.task_editor_edit,
                ),
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
            TagEditorRow(
                tags = editor.tags,
                draft = editor.tagDraft,
                onDraft = { onIntent(TasksUiIntent.ChangeTagDraft(it)) },
                onAdd = { onIntent(TasksUiIntent.AddTag) },
                onRemove = { onIntent(TasksUiIntent.RemoveTag(it)) },
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.task_intervals),
                style = MaterialTheme.typography.labelLarge,
            )
            if (editor.blocks.isEmpty()) {
                Text(
                    text = stringResource(R.string.task_untimed_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            editor.blocks.forEachIndexed { index, block ->
                val closed = block.isClosed(nowMinute)
                IntervalRow(
                    block = block,
                    readOnly = closed,
                    overnight = block.endMinute.mod(TimeMath.MINUTES_PER_DAY) <
                        block.startMinute.mod(TimeMath.MINUTES_PER_DAY) &&
                        block.endMinute != TimeMath.MINUTES_PER_DAY,
                    onStart = { onIntent(TasksUiIntent.OpenStartPicker(index)) },
                    onEnd = { onIntent(TasksUiIntent.OpenEndPicker(index)) },
                    onRemove = { onIntent(TasksUiIntent.RemoveBlock(index)) },
                )
            }
            if (editor.error is TaskDraftError.EndEqualsStart) {
                Text(
                    text = stringResource(R.string.task_error_end_equals_start),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onIntent(TasksUiIntent.AddBlock) }) {
                    Text(stringResource(R.string.task_add_interval))
                }
                if (editor.blocks.isNotEmpty()) {
                    TextButton(onClick = { onIntent(TasksUiIntent.ClearTime) }) {
                        Text(stringResource(R.string.task_clear_time))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.task_status),
                style = MaterialTheme.typography.labelLarge,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (!editor.isNew) {
                    TextButton(
                        onClick = { onIntent(TasksUiIntent.Delete(editor.toTask(date))) },
                    ) {
                        Text(stringResource(R.string.task_delete))
                    }
                }
                TextButton(onClick = { onIntent(TasksUiIntent.Update) }) {
                    Text(stringResource(R.string.task_save))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClockTimePickerDialog(
    minuteOfDay: Int,
    title: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val normalized = minuteOfDay.mod(TimeMath.MINUTES_PER_DAY)
    val pickerState = rememberTimePickerState(
        initialHour = normalized / 60,
        initialMinute = normalized % 60,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pickerState.hour * 60 + pickerState.minute) },
            ) {
                Text(stringResource(R.string.task_picker_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.task_picker_cancel))
            }
        },
    )
}

@Composable
private fun TagFilterRow(
    tags: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(R.string.task_tags_all)) },
        )
        tags.forEach { tag ->
            FilterChip(
                selected = selected == tag,
                onClick = { onSelect(if (selected == tag) null else tag) },
                label = { Text(tag) },
            )
        }
    }
}

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
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Importance.entries.forEach { level ->
            FilterChip(
                selected = selected == level,
                onClick = { onSelect(level) },
                label = { Text(stringResource(importanceLabel(level))) },
            )
        }
    }
}

@Composable
private fun TagEditorRow(
    tags: List<String>,
    draft: String,
    onDraft: (String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
) {
    Text(
        text = stringResource(R.string.task_tags),
        style = MaterialTheme.typography.labelLarge,
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tags.forEach { tag ->
            InputChip(
                selected = false,
                onClick = { onRemove(tag) },
                label = { Text(tag) },
            )
        }
    }
    OutlinedTextField(
        value = draft,
        onValueChange = onDraft,
        label = { Text(stringResource(R.string.task_tag_add)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        trailingIcon = {
            TextButton(onClick = onAdd) {
                Text(stringResource(R.string.task_tag_confirm))
            }
        },
    )
}

@Composable
private fun IntervalRow(
    block: TimeBlock,
    readOnly: Boolean,
    overnight: Boolean,
    onStart: () -> Unit,
    onEnd: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onStart, enabled = !readOnly) {
                Text(
                    text = stringResource(
                        R.string.task_start_value,
                        TimeMath.formatMinuteOfDay(block.startMinute),
                    ),
                )
            }
            TextButton(onClick = onEnd, enabled = !readOnly) {
                Text(
                    text = stringResource(
                        R.string.task_end_value,
                        TimeMath.formatMinuteOfDay(block.endMinute),
                    ),
                )
            }
            if (!readOnly) {
                TextButton(onClick = onRemove) {
                    Text(stringResource(R.string.task_remove_interval))
                }
            } else {
                Text(
                    text = stringResource(R.string.task_interval_locked),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        if (overnight) {
            Text(
                text = stringResource(R.string.task_overnight_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
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

@Preview(showBackground = true)
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
