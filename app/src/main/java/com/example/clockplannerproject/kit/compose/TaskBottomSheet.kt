package com.example.clockplannerproject.kit.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.data.sample.SampleTasks
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import kotlinx.datetime.LocalDate

/**
 * Task card over the dial. Complete, edit, and delete; the ring stays visible
 * behind the standard [ModalBottomSheet].
 *
 * @since 0.1.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBottomSheet(
    task: Task,
    onDismiss: () -> Unit,
    onToggleComplete: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        TaskBottomSheetBody(
            task = task,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        TaskBottomSheetActions(
            isDone = task.status == TaskStatus.DONE,
            onToggleComplete = onToggleComplete,
            onEdit = onEdit,
            onDelete = onDelete,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun TaskBottomSheetBody(
    task: Task,
    modifier: Modifier = Modifier,
) {
    val description = task.description.ifBlank {
        stringResource(R.string.task_description_placeholder)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(text = task.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (task.isUntimed) {
                stringResource(R.string.task_untimed)
            } else {
                TimeMath.formatBlocks(task.blocks)
            },
            style = MaterialTheme.typography.bodyLarge,
        )
        if (task.tags.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = task.tags.joinToString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(statusLabel(task.status)),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(16.dp))
        Text(text = description, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TaskBottomSheetActions(
    isDone: Boolean,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (isDone) {
            OutlinedButton(onClick = onToggleComplete, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.task_reopen))
            }
        } else {
            Button(onClick = onToggleComplete, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.task_complete))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onEdit) {
                Text(stringResource(R.string.task_edit))
            }
            TextButton(onClick = onDelete) {
                Text(
                    text = stringResource(R.string.task_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private fun statusLabel(status: TaskStatus): Int = when (status) {
    TaskStatus.TODO -> R.string.task_status_todo
    TaskStatus.IN_PROGRESS -> R.string.task_status_in_progress
    TaskStatus.DONE -> R.string.task_status_done
}

@Preview(showBackground = true)
@Composable
private fun TaskBottomSheetBodyPreview() {
    ClockPlannerProjectTheme {
        Column(Modifier.padding(16.dp)) {
            TaskBottomSheetBody(task = SampleTasks.typicalDay(LocalDate(2026, 10, 6))[1])
            TaskBottomSheetActions(
                isDone = false,
                onToggleComplete = {},
                onEdit = {},
                onDelete = {},
            )
        }
    }
}
