package com.example.clockplannerproject.ui.day

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clockplannerproject.R
import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.compose.DualTimeDial
import com.example.clockplannerproject.kit.compose.DayTaskSummaryCard
import com.example.clockplannerproject.kit.compose.ListDayView
import com.example.clockplannerproject.kit.compose.TaskBottomSheet
import com.example.clockplannerproject.kit.core.Appearance
import com.example.clockplannerproject.kit.core.BreakSession
import com.example.clockplannerproject.kit.core.BuiltInCourse
import com.example.clockplannerproject.kit.core.BuiltInPractice
import com.example.clockplannerproject.kit.core.HardenKind
import com.example.clockplannerproject.kit.core.TaskTimer
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.PauseMood
import com.example.clockplannerproject.kit.core.taskAppearances
import com.example.clockplannerproject.kit.core.config.TimeDialConfig
import com.example.clockplannerproject.kit.core.layout.ReflowMode
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext
import com.example.clockplannerproject.kit.core.time.SessionDuration
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.kit.core.time.epochMillisToLocalDate
import com.example.clockplannerproject.kit.core.time.toEpochMillisAtStart
import com.example.clockplannerproject.ui.chrome.PaperDateHeader
import com.example.clockplannerproject.ui.day.flow.DayCloseCard
import com.example.clockplannerproject.ui.day.flow.DayFlowColumn
import com.example.clockplannerproject.ui.day.flow.DayFlowHours
import com.example.clockplannerproject.ui.day.flow.PauseSlipSheet
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.EmeraldWork
import com.example.clockplannerproject.ui.theme.OnTerracotta
import com.example.clockplannerproject.ui.theme.PaperSecondary
import com.example.clockplannerproject.ui.theme.SandRest
import com.example.clockplannerproject.ui.theme.TerracottaNow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel

/**
 * Title for the day date strip. Today, yesterday, and tomorrow use catalog words.
 * Any other date keeps the short weekday-and-month form. A missing date stays on
 * the loading string.
 */
@Composable
private fun dayStripTitle(date: LocalDate?, localToday: LocalDate?): String {
    if (date == null) return stringResource(R.string.tasks_date_loading)
    val relative = if (localToday == null) {
        RelativeDay.Other
    } else {
        relativeDay(date, localToday)
    }
    return when (relative) {
        RelativeDay.Today -> stringResource(R.string.day_relative_today)
        RelativeDay.Yesterday -> stringResource(R.string.day_relative_yesterday)
        RelativeDay.Tomorrow -> stringResource(R.string.day_relative_tomorrow)
        RelativeDay.Other -> remember(date) {
            DateTimeFormatter.ofPattern("EEE, d MMM").format(date.toJavaLocalDate())
        }
    }
}

@Composable
fun DayRoute(
    modifier: Modifier = Modifier,
    onAddTask: () -> Unit = {},
    onEditTask: (Task) -> Unit = {},
    onOpenReports: () -> Unit = {},
    viewModel: DayViewModel = koinViewModel(),
    courseViewModel: com.example.clockplannerproject.ui.settings.course.CourseViewModel = koinViewModel(),
) {
    val customCourses by courseViewModel.custom.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarResId by remember { mutableStateOf<Int?>(null) }
    var snackbarNonce by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var offerMinutes by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DayUiEffect.ShowMessageRes -> {
                    snackbarResId = effect.resId
                    snackbarNonce += 1
                }
                is DayUiEffect.OpenEditor -> onEditTask(effect.task)
                DayUiEffect.PlayChime -> {
                    val player = MediaPlayer.create(context, R.raw.session_chime)
                    player?.setOnCompletionListener { it.release() }
                    player?.start()
                }
            }
        }
    }
    val snackbarText = snackbarResId?.let { stringResource(it) }
    LaunchedEffect(snackbarNonce) {
        val text = snackbarText ?: return@LaunchedEffect
        if (snackbarNonce == 0) return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
    }
    val breakTitle = stringResource(R.string.break_title)
    val openBreak = BreakSession.openBreak(state.tasks)
    val workFromBreak = openBreak?.let { wedge ->
        BreakSession.workId(wedge)?.let { id -> state.tasks.find { it.id == id } }
    }
    val shown = state.copy(
        tasks = state.tasks.map { task ->
            if (BreakSession.isBreak(task)) task.copy(title = breakTitle) else task
        },
        selectedTask = state.selectedTask?.let { task ->
            when {
                BreakSession.isBreak(task) -> workFromBreak ?: task.copy(title = breakTitle)
                else -> task
            }
        },
    )
    DayScreen(
        state = shown,
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
        onApplyLook = { viewModel.onIntent(DayUiIntent.SetLook(it)) },
        onStartTimer = {
            if (BreakSession.openBreak(state.tasks) != null) {
                viewModel.onIntent(DayUiIntent.ContinueBreak)
            } else {
                val nowMinute = state.now?.let { TimeMath.minuteOfDay(it).toInt() } ?: 0
                val end = state.selectedTask?.let { SessionDuration.plannedEnd(it, nowMinute) }
                offerMinutes = SessionDuration.suggest(nowMinute, end)
            }
        },
        onPauseTimer = { viewModel.onIntent(DayUiIntent.PauseTimer) },
        onContinueBreak = { viewModel.onIntent(DayUiIntent.ContinueBreak) },
        onFinishBreak = { viewModel.onIntent(DayUiIntent.FinishBreak) },
        onOpenCompletion = { viewModel.onIntent(DayUiIntent.OpenCompletion) },
        onScoreInterval = { viewModel.onIntent(DayUiIntent.OpenIntervalCompletion(it)) },
        onEditCompletion = { viewModel.onIntent(DayUiIntent.EditCompletion(it)) },
        onSubmitCompletion = { viewModel.onIntent(DayUiIntent.SubmitCompletion) },
        onDismissCompletion = { viewModel.onIntent(DayUiIntent.DismissCompletion) },
        onSkipNudge = { viewModel.onIntent(DayUiIntent.SkipNudge) },
        onAcceptNudge = { viewModel.onIntent(DayUiIntent.AcceptNudge(it)) },
        onFinishCourse = { practice, checked ->
            viewModel.onIntent(DayUiIntent.FinishCourse(practice, checked))
        },
        customCourses = customCourses,
        onAddCustomCourse = courseViewModel::addCustom,
        onRefreshCourses = courseViewModel::refresh,
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
        onPauseDraft = { viewModel.onIntent(DayUiIntent.ChangePauseDraft(it)) },
        onPauseMood = { viewModel.onIntent(DayUiIntent.SelectPauseMood(it)) },
        onCommitPause = { viewModel.onIntent(DayUiIntent.CommitPause) },
        onSkipPause = { viewModel.onIntent(DayUiIntent.SkipPause) },
        onShowDayClose = { viewModel.onIntent(DayUiIntent.ShowDayClose) },
        onOpenReports = {
            viewModel.onIntent(DayUiIntent.DismissDayClose)
            onOpenReports()
        },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
    val pendingMinutes = offerMinutes
    if (pendingMinutes != null) {
        DurationSheet(
            minutes = pendingMinutes,
            onMinutes = { offerMinutes = it },
            onConfirm = {
                offerMinutes = null
                viewModel.onIntent(DayUiIntent.StartFor(pendingMinutes))
            },
            onDismiss = { offerMinutes = null },
        )
    }
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
    onApplyLook: (Appearance) -> Unit = {},
    onStartTimer: () -> Unit = {},
    onPauseTimer: () -> Unit = {},
    onContinueBreak: () -> Unit = {},
    onFinishBreak: () -> Unit = {},
    onOpenCompletion: () -> Unit = {},
    onScoreInterval: (Int) -> Unit = {},
    onEditCompletion: (CompletionDraft) -> Unit = {},
    onSubmitCompletion: () -> Unit = {},
    onDismissCompletion: () -> Unit = {},
    onSkipNudge: () -> Unit = {},
    onAcceptNudge: (HardenKind) -> Unit = {},
    onFinishCourse: (BuiltInPractice, Set<String>) -> Unit = { _, _ -> },
    customCourses: List<com.example.clockplannerproject.kit.core.CustomCourse> = emptyList(),
    onAddCustomCourse: (String) -> Unit = {},
    onRefreshCourses: () -> Unit = {},
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
    onPauseDraft: (String) -> Unit = {},
    onPauseMood: (PauseMood) -> Unit = {},
    onCommitPause: () -> Unit = {},
    onSkipPause: () -> Unit = {},
    onShowDayClose: () -> Unit = {},
    onOpenReports: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier,
) {
    val showEmpty = !state.isLoading && state.tasks.isEmpty() && state.errorMessageRes == null
    var menuOpen by remember { mutableStateOf(false) }
    var courseOpen by remember { mutableStateOf(false) }
    val canvasMode = state.viewMode != ViewMode.LIST
    val nowMinute = state.now?.let { TimeMath.minuteOfDay(it).toInt() } ?: 0
    val flowActive = state.pauseSlipOpen ||
        !state.resumeCheckpoint.isNullOrBlank() ||
        state.showDayClose
    val compact = state.hideCompleted && canvasMode
    val dialConfig = remember(
        state.userRotationOffsetDeg,
        compact,
        state.viewMode,
        state.accentArgb,
        state.completedArgb,
        state.background,
    ) {
        TimeDialConfig.Default.copy(
            viewMode = state.viewMode,
            colors = TimeDialConfig.Default.colors.copy(
                nowMarkerArgb = state.accentArgb,
                focusArgb = state.accentArgb,
                handleArgb = state.accentArgb,
                completedArgb = state.completedArgb,
            ),
            background = state.background,
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
        BoxWithConstraints(Modifier.fillMaxSize()) {
        val windowWidth = maxWidth
        val windowHeight = maxHeight
        val shortWindow = windowHeight < ShortWindowHeight
        val dialHeight = maxOf(windowHeight * DialShortFraction, MinDialWindow)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (shortWindow) {
                        Modifier.verticalScroll(rememberScrollState())
                    } else {
                        Modifier
                    },
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val dateLabel = dayStripTitle(state.date, state.localToday)
            val sessionRunning = state.tasks.any { TaskTimer.hasOpenBlock(it) }
            PaperDateHeader(
                date = state.date,
                onPrevious = onPreviousDay,
                onNext = onNextDay,
                onPickDate = onOpenDatePicker,
                title = dateLabel,
                subtitle = null,
                showDate = false,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.cd_day_menu),
                    )
                }
                if (canvasMode) {
                    AmPmRow(
                        selected = state.dialHalf,
                        onSelect = onSetDialHalf,
                    )
                }
            }
            if (!sessionRunning) {
                DayDoneLine(tasks = state.tasks, now = state.now)
            }
            val openBreak = BreakSession.openBreak(state.tasks)
            val breakWorkId = openBreak?.let(BreakSession::workId)
            if (openBreak != null && breakWorkId != null &&
                state.tasks.any { it.id == breakWorkId }
            ) {
                FinishTaskButton(onFinish = onFinishBreak)
            }
            DayChromeRow(
                showHalves = false,
                selectedHalf = state.dialHalf,
                onSelectHalf = onSetDialHalf,
                quiet = sessionRunning,
                resetEnabled = state.hasUserRotation,
                onResetRotation = onResetRotation,
            )
            state.errorMessageRes?.let { messageRes ->
                Text(
                    text = stringResource(messageRes),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
                TextButton(onClick = onRetry, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(stringResource(R.string.day_retry))
                }
            }
            Box(
                modifier = if (shortWindow) {
                    Modifier
                        .fillMaxWidth()
                        .height(dialHeight)
                } else {
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                },
            ) {
                if (showEmpty) {
                    EmptyDayContent(onAddTask = onAddTask)
                } else if (flowActive && canvasMode) {
                    DayFlowColumn(
                        tasks = state.dialTasks,
                        nowMinute = nowMinute,
                        pauseOpen = state.pauseSlipOpen,
                        pauseDraft = state.pauseDraft,
                        pauseMood = state.pauseMood,
                        resumeCheckpoint = state.resumeCheckpoint,
                        resumeMood = state.resumeMood,
                        showDayClose = state.showDayClose,
                        onDraft = onPauseDraft,
                        onMood = onPauseMood,
                        onCommit = onCommitPause,
                        onSkip = onSkipPause,
                        onResume = onStartTimer,
                        onShowDayClose = onShowDayClose,
                        onOpenReports = onOpenReports,
                        dial = { dialModifier ->
                            DialWorkspace(
                                tasks = state.dialTasks,
                                currentTime = state.now,
                                config = dialConfig,
                                selectedTask = state.overlayTask,
                                selectedHalf = state.dialHalf,
                                onTaskClick = onTaskClick,
                                onUserRotationDelta = onRotateBy,
                                onResizePreview = onResizePreview,
                                onResizeCommit = onResizeCommit,
                                onResizeCancel = onResizeCancel,
                                onStartTimer = onStartTimer,
                                onPauseTimer = onPauseTimer,
                                onDismissSelection = onDismissTask,
                                onOpenDetails = onOpenDetails,
                                onHalfChange = onSetDialHalf,
                                minutesLeft = state.sessionMinutesLeft,
                                sessionPaused = BreakSession.openBreak(state.tasks) != null,
                                modifier = dialModifier,
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (flowActive && state.viewMode == ViewMode.LIST) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ListDayView(
                            tasks = state.dialTasks,
                            hideCompleted = state.hideCompleted,
                            onTaskClick = onListTaskClick,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        )
                        if (state.pauseSlipOpen) {
                            PauseSlipSheet(
                                draft = state.pauseDraft,
                                mood = state.pauseMood,
                                onDraft = onPauseDraft,
                                onMood = onPauseMood,
                                onCommit = onCommitPause,
                                onSkip = onSkipPause,
                            )
                        }
                        if (state.showDayClose) {
                            DayCloseCard(
                                closedMinutes = DayFlowHours.closedMinutes(state.dialTasks, nowMinute),
                                checkpoint = state.resumeCheckpoint.orEmpty(),
                                onOpenReports = onOpenReports,
                            )
                        }
                    }
                } else if (state.viewMode == ViewMode.LIST) {
                    ListDayView(
                        tasks = state.dialTasks,
                        hideCompleted = state.hideCompleted,
                        onTaskClick = onListTaskClick,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (state.viewMode == ViewMode.DIAL && windowWidth < 600.dp) {
                    DialWorkspace(
                        tasks = state.dialTasks,
                        currentTime = state.now,
                        config = dialConfig,
                        selectedTask = state.overlayTask,
                        selectedHalf = state.dialHalf,
                        onTaskClick = onTaskClick,
                        onUserRotationDelta = onRotateBy,
                        onResizePreview = onResizePreview,
                        onResizeCommit = onResizeCommit,
                        onResizeCancel = onResizeCancel,
                        onStartTimer = onStartTimer,
                        onPauseTimer = onPauseTimer,
                        onDismissSelection = onDismissTask,
                        onOpenDetails = onOpenDetails,
                        onHalfChange = onSetDialHalf,
                        minutesLeft = state.sessionMinutesLeft,
                        sessionPaused = BreakSession.openBreak(state.tasks) != null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
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
                        minutesLeft = state.sessionMinutesLeft,
                        sessionPaused = BreakSession.openBreak(state.tasks) != null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
        if (menuOpen) {
            val look = Appearance(
                viewMode = state.viewMode,
                accentArgb = state.accentArgb,
                completedArgb = state.completedArgb,
                background = state.background,
            )
            DaySideMenu(
                appearance = look,
                hideCompleted = state.hideCompleted,
                onAppearance = onApplyLook,
                onToggleHideCompleted = onToggleHideCompleted,
                onDismiss = { menuOpen = false },
                showCourseOffer = BuiltInCourse.offer(
                    workOpen = state.tasks.any { task ->
                        !BreakSession.isBreak(task) && TaskTimer.hasOpenBlock(task)
                    },
                    onBreak = BreakSession.openBreak(state.tasks) != null,
                    needsConcentration = BuiltInCourse.needsConcentration(state.selectedTask),
                ),
                onOpenCourse = {
                    menuOpen = false
                    courseOpen = true
                },
            )
        }
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
            onEditScores = onOpenCompletion,
            onScoreInterval = onScoreInterval,
        )
    }
    state.pendingDelete?.let {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.task_delete_day_title)) },
            text = { Text(stringResource(R.string.task_delete_day_body)) },
            confirmButton = {
                Button(
                    onClick = onDismissDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TerracottaNow,
                        contentColor = OnTerracotta,
                    ),
                ) {
                    Text(stringResource(R.string.task_delete_keep))
                }
            },
            dismissButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(stringResource(R.string.task_delete_confirm))
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
    if (state.completion.open) {
        CompletionSheet(
            draft = state.completion,
            onDraft = onEditCompletion,
            onSubmit = onSubmitCompletion,
            onDismiss = onDismissCompletion,
        )
    }
    state.difficultyOffer?.let { offer ->
        DifficultyOfferCard(
            title = offer.title,
            onSkip = onSkipNudge,
            onAccept = onAcceptNudge,
        )
    }
    val workStillOpen = state.tasks.any { task ->
        !BreakSession.isBreak(task) && TaskTimer.hasOpenBlock(task)
    }
    val courseOffered = BuiltInCourse.offer(
        workOpen = workStillOpen,
        onBreak = BreakSession.openBreak(state.tasks) != null,
        needsConcentration = BuiltInCourse.needsConcentration(state.selectedTask),
    )
    if (courseOpen && courseOffered) {
        com.example.clockplannerproject.ui.settings.course.CourseScreen(
            onBack = { courseOpen = false },
            onFinish = onFinishCourse,
            custom = customCourses,
            onAddCustom = onAddCustomCourse,
            onRefresh = onRefreshCourses,
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
    minutesLeft: Int? = null,
    sessionPaused: Boolean = false,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val viewportWidth = maxWidth
        val viewportHeight = maxHeight
        val figureOnly = config.viewMode == ViewMode.PIZZA || config.viewMode == ViewMode.PETALS
        if (figureOnly) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                val side = minOf(viewportWidth, viewportHeight)
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
                    minutesLeft = minutesLeft,
                    sessionPaused = sessionPaused,
                    modifier = Modifier.size(side),
                )
            }
        } else if (viewportWidth < 600.dp) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "dial") {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
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
                            minutesLeft = minutesLeft,
                            sessionPaused = sessionPaused,
                            modifier = Modifier
                                .widthIn(max = CompactDialMax)
                                .fillMaxWidth()
                                .aspectRatio(1f),
                        )
                    }
                }
                items(
                    taskAppearances(tasks),
                    key = { "inventory-${it.task.id.value}:${it.blockIndex ?: "untimed"}" },
                ) { row ->
                    DayTaskSummaryCard(
                        task = row.task,
                        block = row.block,
                        emphasized = selectedTask?.id == row.task.id,
                        onClick = { onTaskClick(row.task) },
                    )
                }
            }
        } else {
            val outerMargin = if (viewportWidth >= 840.dp) 32.dp else 16.dp
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
                        minutesLeft = minutesLeft,
                        sessionPaused = sessionPaused,
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
private fun FinishTaskButton(
    onFinish: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = onFinish,
            modifier = Modifier.height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TerracottaNow,
                contentColor = OnTerracotta,
            ),
        ) {
            Text(stringResource(R.string.break_finish))
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
    minutesLeft: Int? = null,
    sessionPaused: Boolean = false,
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
        minutesLeft = minutesLeft,
        sessionPaused = sessionPaused,
        onDismissSelection = onDismissSelection,
        onOpenDetails = onOpenDetails,
        selectedHalf = selectedHalf,
        onHalfChange = onHalfChange,
        showHalfPills = false,
        modifier = modifier,
    )
}

private val ShortWindowHeight = 520.dp
private const val DialShortFraction = 0.72f
private val MinDialWindow = 280.dp
private val CompactDialMax = 340.dp

/**
 * Closed civil minutes already finished, and the scheduled total beside them.
 * Open timer slices are omitted until Pause writes an end.
 */
internal fun dayWorkMinutes(tasks: List<Task>, nowMinute: Int): Pair<Int, Int> {
    var done = 0
    var planned = 0
    for (task in tasks) {
        for (block in task.blocks) {
            val end = block.endMinute ?: continue
            val minutes = TimeMath.durationMinutes(block.startMinute, end)
            planned += minutes
            if (task.status == TaskStatus.DONE || block.isClosed(nowMinute)) {
                done += minutes
            }
        }
    }
    return done to planned
}

@Composable
private fun DayDoneLine(
    tasks: List<Task>,
    now: LocalTime?,
) {
    val nowMinute = now?.let { TimeMath.minuteOfDay(it).toInt() } ?: 0
    val (done, planned) = remember(tasks, nowMinute) { dayWorkMinutes(tasks, nowMinute) }
    val fraction = if (planned <= 0) 0f else (done.toFloat() / planned).coerceIn(0f, 1f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Text(
            text = stringResource(
                R.string.day_done_progress,
                formatWorkDuration(done),
                formatWorkDuration(planned),
            ),
            style = MaterialTheme.typography.labelSmall,
            color = PaperSecondary,
            maxLines = 1,
            softWrap = false,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SandRest),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(EmeraldWork),
            )
        }
    }
}

@Composable
private fun formatWorkDuration(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours > 0 && rest > 0 -> stringResource(R.string.day_duration_hours_minutes, hours, rest)
        hours > 0 -> stringResource(R.string.day_duration_hours, hours)
        else -> stringResource(R.string.day_duration_minutes, rest)
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

@Preview(showBackground = true, name = "Pause slip")
@Composable
private fun DayScreenPausePreview() {
    ClockPlannerProjectTheme {
        DayScreen(
            state = DayUiState(
                date = PreviewDate,
                now = PreviewNow,
                tasks = SampleTasks.typicalDay(PreviewDate),
                isLoading = false,
                selectedTask = SampleTasks.typicalDay(PreviewDate)[1],
                pauseSlipOpen = true,
            ),
        )
    }
}

@Preview(showBackground = true, name = "Resume chip")
@Composable
private fun DayScreenResumePreview() {
    ClockPlannerProjectTheme {
        DayScreen(
            state = DayUiState(
                date = PreviewDate,
                now = PreviewNow,
                tasks = SampleTasks.typicalDay(PreviewDate),
                isLoading = false,
                selectedTask = SampleTasks.typicalDay(PreviewDate)[1],
                resumeCheckpoint = "цифры за четверг",
            ),
        )
    }
}

@Preview(showBackground = true, name = "Day close")
@Composable
private fun DayScreenClosePreview() {
    ClockPlannerProjectTheme {
        DayScreen(
            state = DayUiState(
                date = PreviewDate,
                now = LocalTime(20, 0),
                tasks = SampleTasks.typicalDay(PreviewDate),
                isLoading = false,
                showDayClose = true,
                resumeCheckpoint = "цифры за четверг",
            ),
        )
    }
}

@Composable
fun DayDestinationContent(
    modifier: Modifier,
    onAddTask: () -> Unit = {},
    onEditTask: (Task) -> Unit = {},
    onOpenReports: () -> Unit = {},
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
        DayRoute(
            modifier = modifier,
            onAddTask = onAddTask,
            onEditTask = onEditTask,
            onOpenReports = onOpenReports,
        )
    }
}
