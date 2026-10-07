package com.example.clockplannerproject.ui.day

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockplannerproject.R
import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.compose.DualTimeDial
import com.example.clockplannerproject.kit.compose.ListDayView
import com.example.clockplannerproject.kit.compose.TaskBottomSheet
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.config.TimeDialConfig
import com.example.clockplannerproject.kit.core.layout.ReflowMode
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.kit.core.time.epochMillisToLocalDate
import com.example.clockplannerproject.kit.core.time.toEpochMillisAtStart
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun DayRoute(
    modifier: Modifier = Modifier,
    onAddTask: () -> Unit = {},
    onEditTask: (Task) -> Unit = {},
    viewModel: DayViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DayUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is DayUiEffect.OpenEditor -> onEditTask(effect.task)
            }
        }
    }
    DayScreen(
        state = state,
        onRetry = { viewModel.onIntent(DayUiIntent.Retry) },
        onTaskClick = { viewModel.onIntent(DayUiIntent.SelectTask(it)) },
        onDismissTask = { viewModel.onIntent(DayUiIntent.DismissTask) },
        onToggleComplete = { viewModel.onIntent(DayUiIntent.ToggleComplete) },
        onEditTask = { viewModel.onIntent(DayUiIntent.EditTask) },
        onRequestDelete = { viewModel.onIntent(DayUiIntent.RequestDelete) },
        onConfirmDelete = { viewModel.onIntent(DayUiIntent.ConfirmDelete) },
        onDismissDelete = { viewModel.onIntent(DayUiIntent.DismissDelete) },
        onAddTask = onAddTask,
        onRotateBy = { viewModel.onIntent(DayUiIntent.RotateBy(it)) },
        onResetRotation = { viewModel.onIntent(DayUiIntent.ResetRotation) },
        onResizePreview = { task, end ->
            viewModel.onIntent(DayUiIntent.ResizePreview(task.id, end, task.startMinute))
        },
        onResizeCommit = { task, end ->
            viewModel.onIntent(DayUiIntent.ResizeTask(task.id, end, task.startMinute))
        },
        onResizeCancel = { viewModel.onIntent(DayUiIntent.CancelResize) },
        onToggleHideCompleted = { viewModel.onIntent(DayUiIntent.ToggleHideCompleted) },
        onSetViewMode = { viewModel.onIntent(DayUiIntent.SetViewMode(it)) },
        onStartTimer = { viewModel.onIntent(DayUiIntent.StartTimer) },
        onPauseTimer = { viewModel.onIntent(DayUiIntent.PauseTimer) },
        onDuplicate = { viewModel.onIntent(DayUiIntent.DuplicateTask) },
        onRequestMove = { viewModel.onIntent(DayUiIntent.RequestMove) },
        onConfirmMove = { viewModel.onIntent(DayUiIntent.ConfirmMove(it)) },
        onDismissMove = { viewModel.onIntent(DayUiIntent.DismissMove) },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun DayScreen(
    state: DayUiState,
    onRetry: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    onDismissTask: () -> Unit = {},
    onToggleComplete: () -> Unit = {},
    onEditTask: () -> Unit = {},
    onRequestDelete: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onDismissDelete: () -> Unit = {},
    onAddTask: () -> Unit = {},
    onRotateBy: (Float) -> Unit = {},
    onResetRotation: () -> Unit = {},
    onResizePreview: (Task, Int) -> Unit = { _, _ -> },
    onResizeCommit: (Task, Int) -> Unit = { _, _ -> },
    onResizeCancel: () -> Unit = {},
    onToggleHideCompleted: () -> Unit = {},
    onSetViewMode: (ViewMode) -> Unit = {},
    onStartTimer: () -> Unit = {},
    onPauseTimer: () -> Unit = {},
    onDuplicate: () -> Unit = {},
    onRequestMove: () -> Unit = {},
    onConfirmMove: (LocalDate) -> Unit = {},
    onDismissMove: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier,
) {
    val showEmpty = !state.isLoading && state.tasks.isEmpty() && state.errorMessage == null
    val showNowClock = TimeDialConfig.Default.clock.showCenterTime
    val canvasMode = state.viewMode != ViewMode.LIST
    val compact = state.hideCompleted && canvasMode
    val dialConfig = remember(state.userRotationOffsetDeg, compact, state.viewMode) {
        TimeDialConfig.Default.copy(
            viewMode = state.viewMode,
            clock = TimeDialConfig.Default.clock.copy(
                reflowMode = if (compact) {
                    ReflowMode.CompactRemaining
                } else {
                    ReflowMode.WallClock
                },
                snapToNow = !compact,
            ),
            interaction = TimeDialConfig.Default.interaction.copy(
                userRotationOffsetDeg = state.userRotationOffsetDeg,
                resizeEnabled = !compact && canvasMode,
            ),
        )
    }
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showNowClock) {
                DayNowClock(now = state.now)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.day_task_count, state.taskCount),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, top = 2.dp, end = 8.dp, bottom = 4.dp),
                )
                if (state.hasUserRotation) {
                    val resetDescription = stringResource(R.string.cd_reset_rotation)
                    TextButton(
                        onClick = onResetRotation,
                        modifier = Modifier.semantics { contentDescription = resetDescription },
                    ) {
                        Text(stringResource(R.string.day_reset_rotation))
                    }
                }
                val hideDescription = stringResource(R.string.cd_hide_completed)
                Text(
                    text = stringResource(R.string.day_hide_completed),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = state.hideCompleted,
                    onCheckedChange = { onToggleHideCompleted() },
                    modifier = Modifier.semantics { contentDescription = hideDescription },
                )
            }
            ViewModeRow(
                selected = state.viewMode,
                onSelect = onSetViewMode,
            )
            if (state.hideCompleted) {
                Text(
                    text = stringResource(
                        if (state.viewMode == ViewMode.LIST) {
                            R.string.day_list_hide_legend
                        } else {
                            R.string.day_compact_legend
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
                TextButton(onClick = onRetry, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(stringResource(R.string.day_retry))
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (showEmpty) {
                    EmptyDayContent(onAddTask = onAddTask)
                } else if (state.viewMode == ViewMode.LIST) {
                    ListDayView(
                        tasks = state.dialTasks,
                        hideCompleted = state.hideCompleted,
                        onTaskClick = onTaskClick,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    DualTimeDial(
                        tasks = state.dialTasks,
                        currentTime = state.now,
                        config = dialConfig,
                        onTaskClick = onTaskClick,
                        onUserRotationDelta = onRotateBy,
                        selectedTask = state.handleTask,
                        onResizePreview = onResizePreview,
                        onResizeCommit = onResizeCommit,
                        onResizeCancel = onResizeCancel,
                        onStartTimer = onStartTimer,
                        onPauseTimer = onPauseTimer,
                        onDismissSelection = onDismissTask,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
    state.sheetTask?.let { task ->
        TaskBottomSheet(
            task = task,
            onDismiss = onDismissTask,
            onToggleComplete = onToggleComplete,
            onEdit = onEditTask,
            onDelete = onRequestDelete,
            onDuplicate = onDuplicate,
            onMove = onRequestMove,
        )
    }
    state.pendingDelete?.let { task ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.task_delete_title)) },
            text = { Text(stringResource(R.string.task_delete_body, task.title)) },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(stringResource(R.string.task_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text(stringResource(R.string.task_delete_cancel))
                }
            },
        )
    }
    if (state.showMovePicker && state.date != null) {
        DayMoveDatePicker(
            selectedDate = state.date,
            onConfirm = onConfirmMove,
            onDismiss = onDismissMove,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayMoveDatePicker(
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
                    if (millis != null) onConfirm(epochMillisToLocalDate(millis)) else onDismiss()
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

@Composable
private fun DayNowClock(
    now: LocalTime?,
    modifier: Modifier = Modifier,
) {
    val clockText = now?.let { TimeMath.formatHm(it) } ?: "—"
    val description = stringResource(R.string.cd_current_time, clockText)
    Text(
        text = clockText,
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 0.dp)
            .semantics { contentDescription = description },
    )
}

private val PreviewDate = LocalDate(2026, 10, 6)
private val PreviewNow = LocalTime(9, 30, 0)

@Preview(showBackground = true, name = "Empty day")
@Composable
private fun DayScreenEmptyPreview() {
    ClockPlannerProjectTheme {
        DayScreen(
            state = DayUiState(
                date = PreviewDate,
                now = PreviewNow,
                tasks = emptyList(),
                isLoading = false,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Full typical day")
@Composable
private fun DayScreenFullPreview() {
    ClockPlannerProjectTheme {
        DayScreen(
            state = DayUiState(
                date = PreviewDate,
                now = PreviewNow,
                tasks = SampleTasks.typicalDay(PreviewDate),
                isLoading = false,
            ),
        )
    }
}

@Preview(showBackground = true, name = "All tasks DONE")
@Composable
private fun DayScreenAllDonePreview() {
    ClockPlannerProjectTheme {
        DayScreen(
            state = DayUiState(
                date = PreviewDate,
                now = PreviewNow,
                tasks = SampleTasks.allDone(PreviewDate),
                isLoading = false,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Overnight task only")
@Composable
private fun DayScreenOvernightPreview() {
    ClockPlannerProjectTheme {
        DayScreen(
            state = DayUiState(
                date = PreviewDate,
                now = LocalTime(23, 30, 0),
                tasks = SampleTasks.overnightOnly(PreviewDate),
                isLoading = false,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Hide completed compact")
@Composable
private fun DayScreenCompactPreview() {
    ClockPlannerProjectTheme {
        DayScreen(
            state = DayUiState(
                date = PreviewDate,
                now = PreviewNow,
                tasks = SampleTasks.typicalDay(PreviewDate),
                isLoading = false,
                hideCompleted = true,
            ),
        )
    }
}

@Composable
fun DayDestinationContent(
    modifier: Modifier,
    onAddTask: () -> Unit = {},
    onEditTask: (Task) -> Unit = {},
) {
    if (LocalInspectionMode.current) {
        DayScreen(
            state = DayUiState(
                tasks = SampleTasks.typicalDay(PreviewDate),
                now = PreviewNow,
                isLoading = false,
            ),
            modifier = modifier,
        )
    } else {
        DayRoute(modifier = modifier, onAddTask = onAddTask, onEditTask = onEditTask)
    }
}
