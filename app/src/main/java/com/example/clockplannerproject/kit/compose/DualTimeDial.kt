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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.config.TimeDialConfig
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import kotlinx.coroutines.launch
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
) {
    val halves = DialHalf.entries
    val nowMinute = currentTime?.let { TimeMath.minuteOfDay(it) }
    val initialPage = if (nowMinute != null && DialHalf.PM.contains(nowMinute)) 1 else 0
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { halves.size })
    var seededNow by remember { mutableStateOf(nowMinute != null) }
    LaunchedEffect(nowMinute != null) {
        if (!seededNow && nowMinute != null) {
            pagerState.scrollToPage(if (DialHalf.PM.contains(nowMinute)) 1 else 0)
            seededNow = true
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
                    modifier = Modifier.size(side),
                )
            }
        }
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

private fun halfLabel(half: DialHalf): Int = when (half) {
    DialHalf.AM -> R.string.dial_am
    DialHalf.PM -> R.string.dial_pm
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
