package com.example.clockplannerproject.ui.day

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockplannerproject.R
import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.compose.DualTimeDial
import com.example.clockplannerproject.kit.compose.DayTaskSummaryCard
import com.example.clockplannerproject.kit.compose.ListDayView
import com.example.clockplannerproject.kit.compose.TaskBottomSheet
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.config.TimeDialConfig
import com.example.clockplannerproject.kit.core.layout.ReflowMode
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.kit.core.time.epochMillisToLocalDate
import com.example.clockplannerproject.kit.core.time.toEpochMillisAtStart
import com.example.clockplannerproject.ui.chrome.PaperDateHeader
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.PaperInk
import com.example.clockplannerproject.ui.theme.TerracottaNow
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
        onListTaskClick = { viewModel.onIntent(DayUiIntent.OpenTaskDetails(it)) },
        onOpenDetails = { viewModel.onIntent(DayUiIntent.OpenTaskDetails()) },
        onDismissTask = { viewModel.onIntent(DayUiIntent.DismissTask) },
        onDismissDetails = { viewModel.onIntent(DayUiIntent.DismissDetails) },
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
        onPreviousDay = { viewModel.onIntent(DayUiIntent.PreviousDay) },
        onNextDay = { viewModel.onIntent(DayUiIntent.NextDay) },
        onOpenDatePicker = { viewModel.onIntent(DayUiIntent.OpenDatePicker) },
        onSelectDate = { viewModel.onIntent(DayUiIntent.SelectDate(it)) },
        onDismissDatePicker = { viewModel.onIntent(DayUiIntent.DismissDatePicker) },
        onSetDialHalf = { viewModel.onIntent(DayUiIntent.SetDialHalf(it)) },
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
    onListTaskClick: (Task) -> Unit = {},
    onOpenDetails: () -> Unit = {},
    onDismissTask: () -> Unit = {},
    onDismissDetails: () -> Unit = {},
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
    onPreviousDay: () -> Unit = {},
    onNextDay: () -> Unit = {},
    onOpenDatePicker: () -> Unit = {},
    onSelectDate: (LocalDate) -> Unit = {},
    onDismissDatePicker: () -> Unit = {},
    onSetDialHalf: (DialHalf) -> Unit = {},
    onDuplicate: () -> Unit = {},
    onRequestMove: () -> Unit = {},
    onConfirmMove: (LocalDate) -> Unit = {},
    onDismissMove: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier,
) {
    val showEmpty = !state.isLoading && state.tasks.isEmpty() && state.errorMessage == null
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
            PaperDateHeader(
                date = state.date,
                onPrevious = onPreviousDay,
                onNext = onNextDay,
                onPickDate = onOpenDatePicker,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val clockText = state.now?.let { TimeMath.formatHm(it) } ?: "—"
                val description = stringResource(R.string.cd_current_time, clockText)
                Text(
                    text = clockText,
                    style = MaterialTheme.typography.titleMedium,
                    color = TerracottaNow,
                    modifier = Modifier.semantics { contentDescription = description },
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        if (state.dialHalf == DialHalf.PM) R.string.dial_pm else R.string.dial_am,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = PaperInk,
                )
                Spacer(Modifier.weight(1f))
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
                FilterChip(
                    selected = state.hideCompleted,
                    onClick = onToggleHideCompleted,
                    label = { Text(stringResource(R.string.day_hide_completed)) },
                    modifier = Modifier.semantics { contentDescription = hideDescription },
                    shape = RoundedCornerShape(8.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (canvasMode) {
                    AmPmRow(
                        selected = state.dialHalf,
                        onSelect = onSetDialHalf,
                    )
                }
                ViewModeRow(
                    selected = state.viewMode,
                    onSelect = onSetViewMode,
                    modifier = Modifier.weight(1f),
                )
            }
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
                        onTaskClick = onListTaskClick,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    ResponsiveDialWorkspace(
                        tasks = state.dialTasks,
                        currentTime = state.now,
                        config = dialConfig,
                        onTaskClick = onTaskClick,
                        onUserRotationDelta = onRotateBy,
                        selectedTask = state.overlayTask,
                        onResizePreview = onResizePreview,
                        onResizeCommit = onResizeCommit,
                        onResizeCancel = onResizeCancel,
                        onStartTimer = onStartTimer,
                        onPauseTimer = onPauseTimer,
                        onDismissSelection = onDismissTask,
                        onOpenDetails = onOpenDetails,
                        selectedHalf = state.dialHalf,
                        onHalfChange = onSetDialHalf,
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
            onDismiss = onDismissDetails,
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
    if (state.showDatePicker && state.date != null) {
        DayMoveDatePicker(
            selectedDate = state.date,
            onConfirm = onSelectDate,
            onDismiss = onDismissDatePicker,
        )
    }
}

@Composable
private fun ResponsiveDialWorkspace(
    tasks: List<Task>,
    currentTime: LocalTime?,
    config: TimeDialConfig,
    selectedTask: Task?,
    selectedHalf: DialHalf,
    onTaskClick: (Task) -> Unit,
    onUserRotationDelta: (Float) -> Unit,
    onResizePreview: (Task, Int) -> Unit,
    onResizeCommit: (Task, Int) -> Unit,
    onResizeCancel: () -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onDismissSelection: () -> Unit,
    onOpenDetails: () -> Unit,
    onHalfChange: (DialHalf) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        if (maxWidth < 600.dp) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "dial") {
                    DialWorkspace(
                        tasks = tasks,
                        currentTime = currentTime,
                        config = config,
                        selectedTask = selectedTask,
                        selectedHalf = selectedHalf,
                        onTaskClick = onTaskClick,
                        onUserRotationDelta = onUserRotationDelta,
                        onResizePreview = onResizePreview,
                        onResizeCommit = onResizeCommit,
                        onResizeCancel = onResizeCancel,
                        onStartTimer = onStartTimer,
                        onPauseTimer = onPauseTimer,
                        onDismissSelection = onDismissSelection,
                        onOpenDetails = onOpenDetails,
                        onHalfChange = onHalfChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                    )
                }
                items(tasks, key = { "inventory-${it.id.value}" }) { task ->
                    DayTaskSummaryCard(task = task, onClick = { onTaskClick(task) })
                }
            }
        } else {
            val outerMargin = if (maxWidth >= 840.dp) 32.dp else 16.dp
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = outerMargin, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    val side = minOf(maxWidth, maxHeight)
                    DialWorkspace(
                        tasks = tasks,
                        currentTime = currentTime,
                        config = config,
                        selectedTask = selectedTask,
                        selectedHalf = selectedHalf,
                        onTaskClick = onTaskClick,
                        onUserRotationDelta = onUserRotationDelta,
                        onResizePreview = onResizePreview,
                        onResizeCommit = onResizeCommit,
                        onResizeCancel = onResizeCancel,
                        onStartTimer = onStartTimer,
                        onPauseTimer = onPauseTimer,
                        onDismissSelection = onDismissSelection,
                        onOpenDetails = onOpenDetails,
                        onHalfChange = onHalfChange,
                        modifier = Modifier.size(side),
                    )
                }
                ListDayView(
                    tasks = tasks,
                    hideCompleted = false,
                    onTaskClick = onTaskClick,
                    modifier = Modifier
                        .weight(0.85f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun DialWorkspace(
    tasks: List<Task>,
    currentTime: LocalTime?,
    config: TimeDialConfig,
    selectedTask: Task?,
    selectedHalf: DialHalf,
    onTaskClick: (Task) -> Unit,
    onUserRotationDelta: (Float) -> Unit,
    onResizePreview: (Task, Int) -> Unit,
    onResizeCommit: (Task, Int) -> Unit,
    onResizeCancel: () -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onDismissSelection: () -> Unit,
    onOpenDetails: () -> Unit,
    onHalfChange: (DialHalf) -> Unit,
    modifier: Modifier = Modifier,
) {
    DualTimeDial(
        tasks = tasks,
        currentTime = currentTime,
        config = config,
        onTaskClick = onTaskClick,
        onUserRotationDelta = onUserRotationDelta,
        selectedTask = selectedTask,
        onResizePreview = onResizePreview,
        onResizeCommit = onResizeCommit,
        onResizeCancel = onResizeCancel,
        onStartTimer = onStartTimer,
        onPauseTimer = onPauseTimer,
        onDismissSelection = onDismissSelection,
        onOpenDetails = onOpenDetails,
        selectedHalf = selectedHalf,
        onHalfChange = onHalfChange,
        showHalfPills = false,
        modifier = modifier,
    )
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

@Preview(
    showBackground = true,
    name = "Compact idle 412",
    widthDp = 412,
    heightDp = 892,
)
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

@Preview(
    showBackground = true,
    name = "Expanded idle dial",
    widthDp = 1000,
    heightDp = 800,
)
@Composable
private fun DayScreenExpandedIdlePreview() {
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

@Preview(
    showBackground = true,
    name = "Compact short selected",
    widthDp = 360,
    heightDp = 640,
)
@Composable
private fun DayScreenCompactShortSelectedPreview() {
    val tasks = SampleTasks.typicalDay(PreviewDate)
    ClockPlannerProjectTheme {
        DayScreen(
            state = DayUiState(
                date = PreviewDate,
                now = LocalTime(10, 42),
                tasks = tasks,
                selectedTask = tasks.first { it.id.value == "work" },
                isLoading = false,
            ),
        )
    }
}

@Preview(
    showBackground = true,
    name = "Medium running",
    widthDp = 700,
    heightDp = 900,
)
@Composable
private fun DayScreenMediumRunningPreview() {
    val sport = SampleTasks.sportTwoBlocks(PreviewDate).copy(
        blocks = listOf(TimeBlock(8 * 60, 9 * 60), TimeBlock(12 * 60, null)),
        status = TaskStatus.IN_PROGRESS,
    )
    ClockPlannerProjectTheme {
        DayScreen(
            state = DayUiState(
                date = PreviewDate,
                now = LocalTime(12, 30),
                tasks = listOf(sport),
                selectedTask = sport,
                dialHalf = DialHalf.PM,
                isLoading = false,
            ),
        )
    }
}

@Preview(
    showBackground = true,
    name = "Expanded list two intervals",
    widthDp = 1000,
    heightDp = 800,
)
@Composable
private fun DayScreenExpandedListPreview() {
    ClockPlannerProjectTheme {
        DayScreen(
            state = DayUiState(
                date = PreviewDate,
                now = PreviewNow,
                tasks = listOf(SampleTasks.sportTwoBlocks(PreviewDate)),
                viewMode = ViewMode.LIST,
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
