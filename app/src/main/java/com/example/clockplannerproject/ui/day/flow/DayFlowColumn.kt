package com.example.clockplannerproject.ui.day.flow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.Task

/**
 * Pause slip, resume chip, and day-close card under the mode row.
 *
 * The dial slot is not the hole. The resume chip sits on the lower edge of the ring.
 * While any of these are open, the caller should not draw a task list under the ring.
 *
 * @param dial draws the existing clock; receives the size modifier
 * @since 1.1.0
 */
@Composable
fun DayFlowColumn(
    tasks: List<Task>,
    nowMinute: Int,
    pauseOpen: Boolean,
    pauseDraft: String,
    pauseMood: PauseMood,
    resumeCheckpoint: String?,
    showDayClose: Boolean,
    onDraft: (String) -> Unit,
    onMood: (PauseMood) -> Unit,
    onCommit: () -> Unit,
    onSkip: () -> Unit,
    onResume: () -> Unit,
    onShowDayClose: () -> Unit,
    onOpenReports: () -> Unit,
    dial: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    val compactDial = pauseOpen || showDayClose
    val checkpoint = resumeCheckpoint.orEmpty()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (compactDial) {
                        Modifier.height(220.dp)
                    } else {
                        Modifier.aspectRatio(1f)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            dial(Modifier.fillMaxSize().alpha(if (pauseOpen) 0.55f else 1f))
            if (!pauseOpen && !showDayClose && checkpoint.isNotBlank()) {
                ResumeCheckpointChip(
                    checkpoint = checkpoint,
                    onResume = onResume,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 28.dp, end = 28.dp, bottom = 8.dp),
                )
            }
        }
        if (pauseOpen) {
            PauseSlipSheet(
                draft = pauseDraft,
                mood = pauseMood,
                onDraft = onDraft,
                onMood = onMood,
                onCommit = onCommit,
                onSkip = onSkip,
            )
        }
        if (!pauseOpen && !showDayClose && checkpoint.isNotBlank()) {
            TextButton(
                onClick = onShowDayClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                Text(stringResource(R.string.flow_day_closed))
            }
        }
        if (showDayClose) {
            DayCloseCard(
                closedMinutes = DayFlowHours.closedMinutes(tasks, nowMinute),
                checkpoint = checkpoint,
                onOpenReports = onOpenReports,
            )
        }
    }
}
