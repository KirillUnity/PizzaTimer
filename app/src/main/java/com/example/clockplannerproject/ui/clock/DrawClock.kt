package com.example.clockplannerproject.ui.clock

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.clockplannerproject.kit.compose.PreviewDialTasks
import com.example.clockplannerproject.kit.compose.TimeDial
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.config.TimeDialConfig
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import kotlinx.datetime.LocalTime

/**
 * Compatibility wrapper around [TimeDial].
 *
 * @since 0.1.0
 */
@Composable
fun DrawClock(
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    currentTime: LocalTime? = null,
    config: TimeDialConfig = TimeDialConfig.Default,
    onTaskClick: (Task) -> Unit = {},
) {
                TimeDial(
                    tasks = tasks,
                    modifier = modifier,
                    currentTime = currentTime,
                    config = config,
                    half = DialHalf.AM,
                    onTaskClick = onTaskClick,
                )
}

@Preview(showBackground = true, name = "DrawClock preview")
@Composable
private fun DrawClockPreview() {
    ClockPlannerProjectTheme {
        DrawClock(
            tasks = PreviewDialTasks,
            currentTime = LocalTime(9, 30, 0),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
    }
}
