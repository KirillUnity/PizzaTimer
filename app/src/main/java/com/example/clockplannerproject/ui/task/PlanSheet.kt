package com.example.clockplannerproject.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.Importance
import com.example.clockplannerproject.kit.core.planner.PlanDraft
import com.example.clockplannerproject.kit.core.planner.PlanVerdict
import com.example.clockplannerproject.kit.core.planner.PlacedStep
import com.example.clockplannerproject.kit.core.time.TimeMath
import com.example.clockplannerproject.kit.core.time.epochMillisToLocalDate
import com.example.clockplannerproject.kit.core.time.toEpochMillisAtStart
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.PaperOutline
import kotlinx.datetime.LocalDate

private const val WINDOW_STEP_MINUTES: Int = 30

/**
 * Entry on the tasks list that opens the goal sheet.
 *
 * @param onClick opens the planner form
 * @param modifier layout modifier
 * @since 1.1.0
 */
@Composable
internal fun PlanGoalEntry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.cd_plan_goal)
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, PaperOutline, shape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.planner_entry_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.planner_entry_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Hosts the goal sheet and its date and time dialogs.
 *
 * Closing the sheet does not write tasks.
 *
 * @param state tasks state that owns the sheet
 * @param onIntent planner and picker intents
 * @since 1.1.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlanSheetHost(
    state: TasksUiState,
    onIntent: (TasksUiIntent) -> Unit,
) {
    val sheet = state.planSheet
    if (sheet != null) {
        val accepting = (sheet as? PlanSheetUi.Draft)?.draft?.accepting == true
        ModalBottomSheet(
            onDismissRequest = { if (!accepting) onIntent(TasksUiIntent.DismissPlan) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            PlanSheet(sheet = sheet, onIntent = onIntent)
        }
    }
    if (state.planDeadlinePicker) {
        val initial = when (val open = state.planSheet) {
            is PlanSheetUi.Form -> open.form.deadline
            is PlanSheetUi.Draft -> open.draft.deadline
            null -> null
        }
        if (initial != null) {
            PlanDateDialog(
                initial = initial,
                onConfirm = { onIntent(TasksUiIntent.SelectPlanDeadline(it)) },
                onDismiss = { onIntent(TasksUiIntent.DismissPlanDeadlinePicker) },
            )
        }
    }
    val stepIndex = state.planStepDateIndex
    val draft = (state.planSheet as? PlanSheetUi.Draft)?.draft
    if (stepIndex != null && draft != null) {
        val step = draft.draft.steps.getOrNull(stepIndex)
        PlanDateDialog(
            initial = step?.date ?: draft.deadline,
            onConfirm = { onIntent(TasksUiIntent.MovePlanStep(stepIndex, it)) },
            onDismiss = { onIntent(TasksUiIntent.DismissPlanStepDate) },
        )
    }
    val edge = state.planWindowEdge
    if (edge != null && state.planSheet != null) {
        val fields = state.planSheet
        val minute = when (fields) {
            is PlanSheetUi.Form -> if (edge == PlanWindowEdge.START) {
                fields.form.workStartMinute
            } else {
                fields.form.workEndMinute
            }
            is PlanSheetUi.Draft -> if (edge == PlanWindowEdge.START) {
                fields.draft.workStartMinute
            } else {
                fields.draft.workEndMinute
            }
        }
        PlanTimeDialog(
            minuteOfDay = minute,
            onConfirm = { picked ->
                val start = when (fields) {
                    is PlanSheetUi.Form -> fields.form.workStartMinute
                    is PlanSheetUi.Draft -> fields.draft.workStartMinute
                }
                val end = when (fields) {
                    is PlanSheetUi.Form -> fields.form.workEndMinute
                    is PlanSheetUi.Draft -> fields.draft.workEndMinute
                }
                if (edge == PlanWindowEdge.START) {
                    onIntent(TasksUiIntent.SetPlanWindow(picked, end))
                } else {
                    onIntent(TasksUiIntent.SetPlanWindow(start, picked))
                }
            },
            onDismiss = { onIntent(TasksUiIntent.DismissPlanWindowTime) },
        )
    }
}

/**
 * Goal form or draft. Dismissal is a separate intent and writes nothing.
 *
 * @param sheet form fields or a ready draft
 * @param onIntent planner actions
 * @param modifier layout modifier
 * @since 1.1.0
 */
@Composable
fun PlanSheet(
    sheet: PlanSheetUi,
    onIntent: (TasksUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = stringResource(R.string.planner_sheet_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(8.dp))
        when (sheet) {
            is PlanSheetUi.Form -> PlanFormBody(form = sheet.form, onIntent = onIntent)
            is PlanSheetUi.Draft -> PlanDraftBody(draft = sheet.draft, onIntent = onIntent)
        }
        Spacer(Modifier.height(16.dp))
        val placeEnabled = (sheet as? PlanSheetUi.Draft)?.draft?.canPlace == true
        val placeDescription = stringResource(R.string.cd_plan_place)
        Button(
            onClick = { onIntent(TasksUiIntent.AcceptPlan) },
            enabled = placeEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = placeDescription },
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(stringResource(R.string.planner_place))
        }
        val accepting = (sheet as? PlanSheetUi.Draft)?.draft?.accepting == true
        TextButton(
            onClick = { onIntent(TasksUiIntent.DismissPlan) },
            enabled = !accepting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(
                    if (sheet is PlanSheetUi.Draft) {
                        R.string.planner_dismiss_draft
                    } else {
                        R.string.planner_dismiss
                    },
                ),
            )
        }
    }
}

/**
 * Calm line that the plan is calculated on this phone.
 *
 * Shown before the request and again on the draft. It does not depend on the network.
 */
@Composable
private fun OnDevicePlanningNote() {
    Text(
        text = stringResource(R.string.planner_on_device),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PlanFormBody(
    form: PlanFormState,
    onIntent: (TasksUiIntent) -> Unit,
) {
    OnDevicePlanningNote()
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.planner_sheet_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = form.goal,
        onValueChange = { onIntent(TasksUiIntent.ChangePlanGoal(it)) },
        label = { Text(stringResource(R.string.planner_goal_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    PlannerMockHint(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
    )
    Spacer(Modifier.height(12.dp))
    DeadlineButton(date = form.deadline, onIntent = onIntent)
    Spacer(Modifier.height(12.dp))
    WorkWindowEditor(
        start = form.workStartMinute,
        end = form.workEndMinute,
        onIntent = onIntent,
    )
    form.message?.let { message ->
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = { onIntent(TasksUiIntent.SubmitPlan) },
        enabled = !form.submitting,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(
            stringResource(
                if (form.submitting) R.string.planner_waiting else R.string.planner_submit,
            ),
        )
    }
}

@Composable
private fun PlanDraftBody(
    draft: PlanDraftUi,
    onIntent: (TasksUiIntent) -> Unit,
) {
    OnDevicePlanningNote()
    Spacer(Modifier.height(8.dp))
    VerdictBanner(verdict = draft.draft.verdict, narrative = draft.draft.narrative)
    Spacer(Modifier.height(12.dp))
    MinuteStats(
        freeMinutes = draft.draft.freeMinutes,
        usableMinutes = draft.draft.usableMinutes,
        requiredMinutes = draft.draft.requiredMinutes,
    )
    Spacer(Modifier.height(12.dp))
    DeadlineButton(date = draft.deadline, onIntent = onIntent)
    WorkWindowEditor(
        start = draft.workStartMinute,
        end = draft.workEndMinute,
        onIntent = onIntent,
    )
    draft.message?.let { message ->
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    if (draft.submitting) {
        Text(
            text = stringResource(R.string.planner_waiting),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(8.dp))
    draft.draft.steps.forEachIndexed { index, step ->
        PlanStepRow(index = index, step = step, onIntent = onIntent)
        Spacer(Modifier.height(8.dp))
    }
    if (draft.draft.verdict == PlanVerdict.IMPOSSIBLE) {
        Text(
            text = stringResource(R.string.planner_impossible_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DeadlineButton(
    date: LocalDate,
    onIntent: (TasksUiIntent) -> Unit,
) {
    Text(
        text = stringResource(R.string.planner_deadline_label),
        style = MaterialTheme.typography.labelLarge,
    )
    TextButton(onClick = { onIntent(TasksUiIntent.OpenPlanDeadlinePicker) }) {
        Text(text = formatPlanDate(date), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun WorkWindowEditor(
    start: Int,
    end: Int,
    onIntent: (TasksUiIntent) -> Unit,
) {
    Text(
        text = stringResource(R.string.planner_window_label),
        style = MaterialTheme.typography.labelLarge,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onIntent(TasksUiIntent.ShiftPlanWindow(-WINDOW_STEP_MINUTES)) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.planner_window_earlier),
            )
        }
        Text(
            text = TimeMath.formatRange(start, end),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
        IconButton(onClick = { onIntent(TasksUiIntent.ShiftPlanWindow(WINDOW_STEP_MINUTES)) }) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.planner_window_later),
            )
        }
    }
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TextButton(onClick = { onIntent(TasksUiIntent.OpenPlanWindowStart) }) {
            Text(TimeMath.formatMinuteOfDay(start))
        }
        TextButton(
            onClick = {
                onIntent(TasksUiIntent.NudgePlanWindowEdge(PlanWindowEdge.START, WINDOW_STEP_MINUTES))
            },
        ) {
            Text(stringResource(R.string.planner_window_start_later))
        }
        TextButton(
            onClick = {
                onIntent(TasksUiIntent.NudgePlanWindowEdge(PlanWindowEdge.END, -WINDOW_STEP_MINUTES))
            },
        ) {
            Text(stringResource(R.string.planner_window_end_earlier))
        }
        TextButton(onClick = { onIntent(TasksUiIntent.OpenPlanWindowEnd) }) {
            Text(TimeMath.formatMinuteOfDay(end))
        }
    }
    Text(
        text = stringResource(R.string.planner_window_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (start >= end || start < 0 || end > TimeMath.MINUTES_PER_DAY) {
        Text(
            text = stringResource(R.string.planner_error_invalid_window),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun VerdictBanner(
    verdict: PlanVerdict,
    narrative: String,
) {
    val container = when (verdict) {
        PlanVerdict.FITS -> MaterialTheme.colorScheme.tertiaryContainer
        PlanVerdict.TIGHT -> MaterialTheme.colorScheme.secondaryContainer
        PlanVerdict.IMPOSSIBLE -> MaterialTheme.colorScheme.errorContainer
    }
    val onContainer = when (verdict) {
        PlanVerdict.FITS -> MaterialTheme.colorScheme.onTertiaryContainer
        PlanVerdict.TIGHT -> MaterialTheme.colorScheme.onSecondaryContainer
        PlanVerdict.IMPOSSIBLE -> MaterialTheme.colorScheme.onErrorContainer
    }
    val label = stringResource(verdictLabel(verdict))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .padding(16.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.titleLarge, color = onContainer)
        Spacer(Modifier.height(4.dp))
        Text(text = narrative, style = MaterialTheme.typography.bodyMedium, color = onContainer)
    }
}

@Composable
private fun MinuteStats(
    freeMinutes: Int,
    usableMinutes: Int,
    requiredMinutes: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCell(value = freeMinutes, label = stringResource(R.string.planner_stat_free), modifier = Modifier.weight(1f))
        StatCell(value = usableMinutes, label = stringResource(R.string.planner_stat_usable), modifier = Modifier.weight(1f))
        StatCell(value = requiredMinutes, label = stringResource(R.string.planner_stat_required), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCell(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanStepRow(
    index: Int,
    step: PlacedStep,
    onIntent: (TasksUiIntent) -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp),
    ) {
        Text(
            text = step.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextButton(onClick = { onIntent(TasksUiIntent.OpenPlanStepDate(index)) }) {
                Text(
                    text = step.date?.let(::formatPlanDate)
                        ?: stringResource(R.string.planner_step_pick_day),
                )
            }
            TextButton(onClick = { onIntent(TasksUiIntent.RemovePlanStep(index)) }) {
                Text(stringResource(R.string.planner_step_remove))
            }
        }
        val start = step.startMinute
        val end = step.endMinute
        Text(
            text = if (step.unplaced || start == null || end == null) {
                stringResource(R.string.planner_step_unplaced)
            } else {
                TimeMath.formatRange(start, end)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        step.project?.let { project ->
            Text(
                text = stringResource(R.string.planner_step_project, project),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(
                R.string.planner_step_estimate,
                step.estimateMinutes,
                step.bufferedMinutes,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanDateDialog(
    initial: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial.toEpochMillisAtStart(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanTimeDialog(
    minuteOfDay: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val normalized = minuteOfDay.mod(TimeMath.MINUTES_PER_DAY)
    val pickerState = rememberTimePickerState(
        initialHour = normalized / 60,
        initialMinute = normalized % 60,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(pickerState.hour * 60 + pickerState.minute) }) {
                Text(stringResource(R.string.task_picker_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.task_picker_cancel))
            }
        },
        text = { TimePicker(state = pickerState) },
    )
}

private fun verdictLabel(verdict: PlanVerdict): Int = when (verdict) {
    PlanVerdict.FITS -> R.string.planner_verdict_fits
    PlanVerdict.TIGHT -> R.string.planner_verdict_tight
    PlanVerdict.IMPOSSIBLE -> R.string.planner_verdict_impossible
}

private fun formatPlanDate(date: LocalDate): String {
    val javaDate = java.time.LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
    return javaDate.format(java.time.format.DateTimeFormatter.ofPattern("d MMM"))
}

private val PreviewToday = LocalDate(2026, 11, 7)

@Preview(showBackground = true, name = "Plan sheet empty form")
@Composable
private fun PlanSheetFormPreview() {
    ClockPlannerProjectTheme {
        PlanSheet(
            sheet = PlanSheetUi.Form(PlanFormState(deadline = PreviewToday)),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, name = "Plan sheet fits")
@Composable
private fun PlanSheetFitsPreview() {
    ClockPlannerProjectTheme {
        PlanSheet(sheet = previewFitsSheet(), onIntent = {})
    }
}

@Preview(showBackground = true, name = "Plan sheet impossible")
@Composable
private fun PlanSheetImpossiblePreview() {
    ClockPlannerProjectTheme {
        PlanSheet(sheet = previewImpossibleSheet(), onIntent = {})
    }
}

private fun previewFitsSheet(): PlanSheetUi.Draft = PlanSheetUi.Draft(
    PlanDraftUi(
        goal = "Подготовить отчёт",
        deadline = LocalDate(2026, 11, 9),
        workStartMinute = 9 * 60,
        workEndMinute = 19 * 60,
        draft = PlanDraft(
            draftId = "preview-fits",
            verdict = PlanVerdict.FITS,
            freeMinutes = 1680,
            usableMinutes = 1344,
            requiredMinutes = 468,
            narrative = "Помещается: свободно 1680, с запасом 1344, нужно 468.",
            steps = listOf(
                previewStep(
                    title = "Подготовить отчёт: собрать",
                    date = LocalDate(2026, 11, 7),
                    start = 15 * 60,
                    end = 17 * 60 + 24,
                ),
                previewStep(
                    title = "Подготовить отчёт: черновик",
                    date = LocalDate(2026, 11, 7),
                    start = 12 * 60,
                    end = 15 * 60,
                    estimate = 150,
                    buffered = 180,
                ),
            ),
        ),
    ),
)

private fun previewImpossibleSheet(): PlanSheetUi.Draft = PlanSheetUi.Draft(
    PlanDraftUi(
        goal = "десятки часов",
        deadline = LocalDate(2026, 11, 8),
        workStartMinute = 9 * 60,
        workEndMinute = 19 * 60,
        draft = PlanDraft(
            draftId = "preview-impossible",
            verdict = PlanVerdict.IMPOSSIBLE,
            freeMinutes = 1080,
            usableMinutes = 864,
            requiredMinutes = 2880,
            narrative = "Не помещается: не хватает 1800 мин. Можно укоротить цель, сдвинуть день или снять необязательное.",
            steps = listOf(
                PlacedStep(
                    title = "десятки часов: 2",
                    date = null,
                    startMinute = null,
                    endMinute = null,
                    unplaced = true,
                    estimateMinutes = 600,
                    bufferedMinutes = 720,
                    importance = Importance.MEDIUM,
                    project = "десятки часов",
                ),
            ),
        ),
    ),
)

private fun previewStep(
    title: String,
    date: LocalDate,
    start: Int,
    end: Int,
    estimate: Int = 120,
    buffered: Int = 144,
): PlacedStep = PlacedStep(
    title = title,
    date = date,
    startMinute = start,
    endMinute = end,
    unplaced = false,
    estimateMinutes = estimate,
    bufferedMinutes = buffered,
    importance = Importance.MEDIUM,
    project = "Подготовить отчёт",
)
