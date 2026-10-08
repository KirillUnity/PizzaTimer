package com.example.clockplannerproject.kit.compose

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.config.TimeDialConfig
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Two 12-hour rings (AM / PM). Horizontal swipe switches halves.
 * Tasks that cross noon or midnight are split across both faces.
 * One-finger tap or long-press resize; two-finger twist rotates the scene.
 * Swipe in the hole pages AM/PM.
 *
 * @since 0.2.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DualTimeDial(
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    currentTime: LocalTime? = null,
    config: TimeDialConfig = TimeDialConfig.Default,
    onTaskClick: (Task) -> Unit = {},
    onUserRotationDelta: (Float) -> Unit = {},
    selectedTask: Task? = null,
    onResizePreview: (Task, Int) -> Unit = { _, _ -> },
    onResizeCommit: (Task, Int) -> Unit = { _, _ -> },
    onResizeCancel: () -> Unit = {},
    onStartTimer: () -> Unit = {},
    onPauseTimer: () -> Unit = {},
    onDismissSelection: () -> Unit = {},
    onOpenDetails: () -> Unit = {},
    selectedHalf: DialHalf? = null,
    onHalfChange: (DialHalf) -> Unit = {},
    showHalfPills: Boolean = true,
) {
    val halves = DialHalf.entries
    val nowMinute = currentTime?.let { TimeMath.minuteOfDay(it) }
    val initialPage = when {
        selectedHalf == DialHalf.PM -> 1
        selectedHalf == DialHalf.AM -> 0
        nowMinute != null && DialHalf.PM.contains(nowMinute) -> 1
        else -> 0
    }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { halves.size })
    LaunchedEffect(selectedHalf) {
        if (selectedHalf != null) {
            val page = if (selectedHalf == DialHalf.PM) 1 else 0
            if (pagerState.currentPage != page) pagerState.scrollToPage(page)
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onHalfChange(halves[page])
        }
    }
    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            val half = halves[page]
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val side = minOf(maxWidth, maxHeight)
                TimeDial(
                    tasks = tasks,
                    currentTime = currentTime,
                    config = config,
                    half = half,
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
                    modifier = Modifier.size(side),
                )
            }
        }
        if (showHalfPills) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.padding(start = 24.dp, top = 4.dp, end = 24.dp, bottom = 16.dp),
            ) {
                halves.forEachIndexed { index, half ->
                    SegmentedButton(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        shape = SegmentedButtonDefaults.itemShape(index, halves.size),
                    ) {
                        Text(stringResource(halfLabel(half)))
                    }
                }
            }
        }
    }
}

private fun halfLabel(half: DialHalf): Int = when (half) {
    DialHalf.AM -> R.string.dial_am
    DialHalf.PM -> R.string.dial_pm
}

@Preview(showBackground = true, name = "Two Sport blocks one id")
@Composable
private fun DualTimeDialTwoBlocksPreview() {
    ClockPlannerProjectTheme {
        DualTimeDial(
            tasks = listOf(SampleTasks.sportTwoBlocks(LocalDate(2026, 10, 6))),
            currentTime = LocalTime(13, 0, 0),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DualTimeDialPreview() {
    ClockPlannerProjectTheme {
        DualTimeDial(
            tasks = PreviewDialTasks,
            currentTime = LocalTime(9, 30, 0),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
