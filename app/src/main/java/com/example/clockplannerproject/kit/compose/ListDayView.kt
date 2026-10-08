package com.example.clockplannerproject.kit.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.kit.core.visibleListTasks
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.PaperOutline
import kotlinx.datetime.LocalDate

/**
 * Text list of the same day as the dial. [hideCompleted] filters DONE;
 * it does not compact remaining work to 360°. Times come from [TimeMath],
 * not leftover shares.
 *
 * @since 0.3.0
 */
@Composable
fun ListDayView(
    tasks: List<Task>,
    hideCompleted: Boolean,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visible = remember(tasks, hideCompleted) {
        visibleListTasks(tasks, hideCompleted).sortedBy { it.sortMinute }
    }
    if (visible.isEmpty()) {
        Text(
            text = stringResource(R.string.day_list_empty_filtered),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
        )
        return
    }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = if (maxWidth >= 600.dp) 2 else 1
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(visible, key = { it.id.value }) { task ->
                DayTaskSummaryCard(task = task, onClick = { onTaskClick(task) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DayTaskSummaryCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorCd = stringResource(R.string.cd_task_color, task.title)
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, PaperOutline, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(task.colorArgb.toInt()))
                .semantics { contentDescription = colorCd },
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = task.title, style = MaterialTheme.typography.titleMedium)
            if (task.isUntimed) {
                Text(
                    text = stringResource(R.string.task_untimed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                task.blocks.forEach { block ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    ) {
                        Text(
                            text = TimeMath.formatRange(block.startMinute, block.endMinute),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        )
                    }
                }
            }
            FlowRow(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(statusLabel(task.status)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                task.normalizedProject?.let { project ->
                    Text(
                        text = project,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

private fun statusLabel(status: TaskStatus): Int = when (status) {
    TaskStatus.TODO -> R.string.task_status_todo
    TaskStatus.IN_PROGRESS -> R.string.task_status_in_progress
    TaskStatus.DONE -> R.string.task_status_done
}

@Preview(showBackground = true, name = "List day")
@Composable
private fun ListDayViewPreview() {
    ClockPlannerProjectTheme {
        ListDayView(
            tasks = PreviewDialTasks,
            hideCompleted = false,
            onTaskClick = {},
        )
    }
}

@Preview(showBackground = true, name = "List two intervals one task")
@Composable
private fun ListDayViewTwoIntervalsPreview() {
    ClockPlannerProjectTheme {
        ListDayView(
            tasks = listOf(SampleTasks.sportTwoBlocks(LocalDate(2026, 10, 6))),
            hideCompleted = false,
            onTaskClick = {},
        )
    }
}
