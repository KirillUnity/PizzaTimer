package com.example.clockplannerproject.kit.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.kit.core.visibleListTasks
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme

/**
 * Text list of the same day as the dial. [hideCompleted] filters DONE;
 * it does not compact remaining work to 360°.
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
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(visible, key = { it.id.value }) { task ->
            ListDayRow(task = task, onClick = { onTaskClick(task) })
        }
    }
}

@Composable
private fun ListDayRow(
    task: Task,
    onClick: () -> Unit,
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
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
