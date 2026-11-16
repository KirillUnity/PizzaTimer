package com.example.clockplannerproject.kit.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.taskAppearances
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
        taskAppearances(visibleListTasks(tasks, hideCompleted))
            .sortedBy { it.block?.startMinute ?: Int.MAX_VALUE }
    }
    if (visible.isEmpty()) {
        Text(
            text = stringResource(R.string.day_list_empty_filtered),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = ListFabClearance),
        )
        return
    }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = if (maxWidth >= 600.dp) 2 else 1
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = ListFabClearance,
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(visible, key = { "${it.task.id.value}:${it.blockIndex ?: "untimed"}" }) { row ->
                DayTaskSummaryCard(
                    task = row.task,
                    block = row.block,
                    onClick = { onTaskClick(row.task) },
                )
            }
        }
    }
}

@Composable
internal fun DayTaskSummaryCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    block: TimeBlock? = task.blocks.firstOrNull(),
    emphasized: Boolean = false,
) {
    val colorCd = stringResource(R.string.cd_task_color, task.title)
    val shape = RoundedCornerShape(16.dp)
    val minutes = block?.endMinute?.let { end ->
        TimeMath.durationMinutes(block.startMinute, end)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (emphasized) {
                    MaterialTheme.colorScheme.surfaceContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
            )
            .border(1.dp, PaperOutline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(task.colorArgb.toInt()))
                .semantics { contentDescription = colorCd },
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = when {
                    block == null -> stringResource(R.string.task_untimed)
                    else -> TimeMath.formatRange(block.startMinute, block.endMinute)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (task.normalizedTags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    task.normalizedTags.forEach { tag ->
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
        if (minutes != null) {
            Text(
                text = stringResource(R.string.day_wedge_hours, minutes / 60, minutes % 60),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
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
            tasks = listOf(
                SampleTasks.sportTwoBlocks(LocalDate(2026, 10, 6)).copy(
                    tags = listOf("#Чтение", "#Рутина"),
                ),
            ),
            hideCompleted = false,
            onTaskClick = {},
        )
    }
}

private val ListFabClearance = 96.dp
