package com.example.clockplannerproject.kit.compose

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.clipTaskToHalf
import com.example.clockplannerproject.kit.core.halfAnchorMinute
import com.example.clockplannerproject.kit.core.config.TimeDialConfig
import com.example.clockplannerproject.kit.core.layout.Reflow
import com.example.clockplannerproject.kit.core.layout.ReflowMode
import com.example.clockplannerproject.kit.core.layout.RestGaps
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.kit.render.sectorColor
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * 12-hour ring for [half]. Wall-clock: now/nearest at 12, Rest in gaps.
 * CompactRemaining: remaining work fills 360°; chevron at 12 is "now"
 * and is not bound to sectors. Labels keep civil start–end.
 *
 * @since 0.1.0
 */
@Composable
fun TimeDial(
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    currentTime: LocalTime? = null,
    config: TimeDialConfig = TimeDialConfig.Default,
    half: DialHalf = DialHalf.AM,
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
) {
    val completed = remember(config.colors.completedArgb) {
        argbToColor(config.colors.completedArgb)
    }
    val marker = remember(config.colors.nowMarkerArgb) {
        argbToColor(config.colors.nowMarkerArgb)
    }
    val handle = remember(config.colors.handleArgb) {
        argbToColor(config.colors.handleArgb)
    }
    val restColor = remember(config.colors.restArgb) {
        argbToColor(config.colors.restArgb)
    }
    val focus = remember(config.colors.focusArgb) {
        argbToColor(config.colors.focusArgb)
    }
    val restTitle = stringResource(R.string.dial_rest)
    val date = tasks.firstOrNull()?.date ?: LocalDate(2026, 1, 1)
    val nowMinute by remember(currentTime) {
        derivedStateOf {
            currentTime?.let { TimeMath.minuteOfDay(it) } ?: 0f
        }
    }
    val compact = config.clock.reflowMode == ReflowMode.CompactRemaining
    val nowMinuteInt = nowMinute.toInt()
    val laidOut = remember(tasks, half, config.clock.reflowMode, nowMinuteInt) {
        Reflow.layout(tasks, half, config.clock.reflowMode, nowMinuteInt).map { slice ->
            val civil = if (slice.task.isUntimed) {
                slice.task.title
            } else {
                val range = TimeMath.formatBlocks(slice.task.blocks)
                if (compact) "${slice.task.title}  $range" else slice.task.title
            }
            Reflow.toLayoutTask(slice).copy(title = civil)
        }
    }
    val withRest = remember(laidOut, half, restTitle, config.colors.restArgb, date, compact) {
        if (compact) {
            laidOut
        } else {
            laidOut + RestGaps.fill(laidOut, half, date, restTitle, config.colors.restArgb)
        }
    }
    val sectorColors = remember(withRest, completed, restColor) {
        withRest.map { task ->
            if (RestGaps.isRest(task)) restColor
            else sectorColor(task, completed, argbToColor(task.colorArgb))
        }
    }
    val pulse = rememberInfiniteTransition(label = "focusChevron")
    val markerAlpha by pulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "focusAlpha",
    )
    val accessibilityTasks = remember(tasks, half, nowMinuteInt) {
        tasks.filter { clipTaskToHalf(it.resolveOpenBlocks(nowMinuteInt), half).isNotEmpty() }
    }
    val anchorMinute = if (config.clock.snapToNow && !compact) {
        halfAnchorMinute(nowMinute, half, withRest)
    } else {
        half.startMinute.toFloat()
    }
    BoxWithConstraints(modifier = modifier) {
        DialView(
            tasks = withRest,
            sectorColors = sectorColors,
            markerColor = marker,
            config = config,
            nowMinute = nowMinute,
            half = half,
            onTaskClick = { layoutTask ->
                val original = tasks.find { it.id == layoutTask.id } ?: layoutTask
                onTaskClick(original)
            },
            modifier = Modifier.fillMaxSize(),
            nowMarkerContentDescription = stringResource(R.string.cd_now_marker),
            onUserRotationDelta = onUserRotationDelta,
            selectedTask = selectedTask,
            handleColor = handle,
            focusColor = focus,
            markerAlpha = markerAlpha,
            onResizePreview = onResizePreview,
            onResizeCommit = onResizeCommit,
            onResizeCancel = onResizeCancel,
            accessibilityTasks = accessibilityTasks,
        )
        DialHoleContent(
            now = currentTime,
            selectedTask = selectedTask,
            onStartTimer = onStartTimer,
            onPauseTimer = onPauseTimer,
        )
        selectedTask?.let { selected ->
            SelectedTaskOverlays(
                task = selected,
                half = half,
                nowMinute = nowMinuteInt,
                anchorMinute = anchorMinute,
                config = config,
                constraints = constraints,
                onDismiss = onDismissSelection,
                onStartTimer = onStartTimer,
                onPauseTimer = onPauseTimer,
                onOpenDetails = onOpenDetails,
            )
        }
    }
}

internal fun argbToColor(argb: Long): Color = Color(argb.toInt())

val PreviewDialTasks: List<Task> = SampleTasks.typicalDay(LocalDate(2026, 10, 6))

@Preview(showBackground = true, name = "Now 09:30 — Deep work at top")
@Composable
private fun TimeDialNowAtTopPreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = PreviewDialTasks,
            currentTime = LocalTime(9, 30, 0),
            half = DialHalf.AM,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "User offset 90° — now at 3 o'clock")
@Composable
private fun TimeDialUserOffsetPreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = PreviewDialTasks,
            currentTime = LocalTime(9, 30, 0),
            half = DialHalf.AM,
            config = TimeDialConfig.Default.copy(
                interaction = TimeDialConfig.Default.interaction.copy(
                    userRotationOffsetDeg = 90f,
                ),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Compact remaining — DONE hidden")
@Composable
private fun TimeDialCompactPreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = PreviewDialTasks,
            currentTime = LocalTime(9, 30, 0),
            half = DialHalf.AM,
            config = TimeDialConfig.Default.copy(
                clock = TimeDialConfig.Default.clock.copy(
                    reflowMode = ReflowMode.CompactRemaining,
                    snapToNow = false,
                ),
                interaction = TimeDialConfig.Default.interaction.copy(resizeEnabled = false),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Now 12:10 — Lunch (DONE) at top")
@Composable
private fun TimeDialLunchAtTopPreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = PreviewDialTasks,
            currentTime = LocalTime(12, 10, 0),
            half = DialHalf.PM,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Overnight sleep — AM 01:00")
@Composable
private fun TimeDialOvernightAmPreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = SampleTasks.overnightOnly(LocalDate(2026, 10, 6)),
            currentTime = LocalTime(1, 0, 0),
            half = DialHalf.AM,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Overnight sleep — PM 23:00")
@Composable
private fun TimeDialOvernightPmPreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = SampleTasks.overnightOnly(LocalDate(2026, 10, 6)),
            currentTime = LocalTime(23, 0, 0),
            half = DialHalf.PM,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Pizza AM")
@Composable
private fun TimeDialPizzaPreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = PreviewDialTasks,
            currentTime = LocalTime(9, 30, 0),
            half = DialHalf.AM,
            config = TimeDialConfig.Default.copy(viewMode = ViewMode.PIZZA),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Pizza two Sport sectors")
@Composable
private fun TimeDialPizzaTwoBlocksPreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = listOf(SampleTasks.sportTwoBlocks(LocalDate(2026, 10, 6))),
            currentTime = LocalTime(13, 0, 0),
            half = DialHalf.PM,
            config = TimeDialConfig.Default.copy(viewMode = ViewMode.PIZZA),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Petals two Sport sectors")
@Composable
private fun TimeDialPetalsTwoBlocksPreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = listOf(SampleTasks.sportTwoBlocks(LocalDate(2026, 10, 6))),
            currentTime = LocalTime(13, 0, 0),
            half = DialHalf.PM,
            config = TimeDialConfig.Default.copy(viewMode = ViewMode.PETALS),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Untimed 50/50 leftover AM")
@Composable
private fun TimeDialUntimedPreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = SampleTasks.untimedPair(LocalDate(2026, 10, 6)),
            currentTime = LocalTime(9, 30, 0),
            half = DialHalf.AM,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Timed 9–12 plus untimed leftover")
@Composable
private fun TimeDialMixedPreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = SampleTasks.mixedTimedUntimed(LocalDate(2026, 10, 6)),
            currentTime = LocalTime(9, 30, 0),
            half = DialHalf.AM,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Three importances 50/30/20")
@Composable
private fun TimeDialImportancePreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = SampleTasks.threeImportances(LocalDate(2026, 10, 6)),
            currentTime = LocalTime(9, 30, 0),
            half = DialHalf.AM,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Idle LIVE CHRONO")
@Composable
private fun TimeDialIdlePreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = PreviewDialTasks,
            currentTime = LocalTime(10, 42, 0),
            half = DialHalf.AM,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Selected callout + Start")
@Composable
private fun TimeDialSelectedPreview() {
    val tasks = PreviewDialTasks
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = tasks,
            currentTime = LocalTime(10, 42, 0),
            half = DialHalf.AM,
            selectedTask = tasks.first { it.blocks.isNotEmpty() },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Sport two blocks AM")
@Composable
private fun TimeDialSportBlocksPreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = listOf(SampleTasks.sportTwoBlocks(LocalDate(2026, 10, 6))),
            currentTime = LocalTime(8, 30, 0),
            half = DialHalf.AM,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Sport 8–9 plus live 12→now")
@Composable
private fun TimeDialLiveTimerPreview() {
    val date = LocalDate(2026, 10, 6)
    val sport = SampleTasks.sportTwoBlocks(date).copy(
        blocks = listOf(TimeBlock(8 * 60, 9 * 60), TimeBlock(12 * 60, endMinute = null)),
        status = TaskStatus.IN_PROGRESS,
    )
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = listOf(sport),
            currentTime = LocalTime(12, 30, 0),
            half = DialHalf.PM,
            selectedTask = sport,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Timed only AM")
@Composable
private fun TimeDialTimedOnlyPreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = PreviewDialTasks.filterNot { it.isUntimed },
            currentTime = LocalTime(9, 30, 0),
            half = DialHalf.AM,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Petals AM")
@Composable
private fun TimeDialPetalsPreview() {
    ClockPlannerProjectTheme {
        TimeDial(
            tasks = PreviewDialTasks,
            currentTime = LocalTime(9, 30, 0),
            half = DialHalf.AM,
            config = TimeDialConfig.Default.copy(viewMode = ViewMode.PETALS),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
        )
    }
}
