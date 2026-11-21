package com.example.clockplannerproject.ui.settings.course

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.BuiltInCourse
import com.example.clockplannerproject.kit.core.BuiltInPractice
import com.example.clockplannerproject.kit.core.CourseLesson
import com.example.clockplannerproject.kit.core.CustomCourse
import com.example.clockplannerproject.ui.theme.OnTerracotta
import com.example.clockplannerproject.ui.theme.TerracottaNow

/**
 * Built-in practices plus local custom titles. Not a fifth tab.
 *
 * @param onFinish appends a pass when the checkpoints are met
 * @since 1.2.0
 */
@Composable
fun CourseScreen(
    onBack: () -> Unit,
    onFinish: (BuiltInPractice, Set<String>) -> Unit,
    custom: List<CustomCourse> = emptyList(),
    onAddCustom: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf<BuiltInPractice?>(null) }
    var customOpen by remember { mutableStateOf<CustomCourse?>(null) }
    var adding by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                when {
                    open != null -> open = null
                    customOpen != null -> customOpen = null
                    else -> onBack()
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.course_back))
            }
            Text(stringResource(R.string.course_title), style = MaterialTheme.typography.titleLarge)
        }
        val selected = open
        val customSelected = customOpen
        when {
            selected != null -> LessonBody(lesson = BuiltInCourse.lesson(selected), onFinish = onFinish)
            customSelected != null -> Text(customSelected.title, style = MaterialTheme.typography.titleMedium)
            else -> {
                BuiltInCourse.lessons.forEach { lesson ->
                    TextButton(onClick = { open = lesson.practice }, modifier = Modifier.fillMaxWidth()) {
                        Text(lessonTitle(lesson))
                    }
                }
                custom.forEach { course ->
                    TextButton(onClick = { customOpen = course }, modifier = Modifier.fillMaxWidth()) {
                        Text(course.title)
                    }
                }
                Spacer(Modifier.height(16.dp))
                CourseListButton(
                    label = stringResource(R.string.course_add),
                    onClick = { adding = true },
                )
                CourseListButton(
                    label = stringResource(R.string.course_refresh),
                    onClick = onRefresh,
                )
            }
        }
    }
    if (adding) {
        AlertDialog(
            onDismissRequest = { adding = false },
            title = { Text(stringResource(R.string.course_add)) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text(stringResource(R.string.course_add_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAddCustom(draft)
                        draft = ""
                        adding = false
                    },
                ) {
                    Text(stringResource(R.string.course_add_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { adding = false }) {
                    Text(stringResource(R.string.course_add_cancel))
                }
            },
        )
    }
}

@Composable
private fun CourseListButton(
    label: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TerracottaNow,
            contentColor = OnTerracotta,
        ),
    ) {
        Text(label)
    }
}

@Composable
private fun LessonBody(
    lesson: CourseLesson,
    onFinish: (BuiltInPractice, Set<String>) -> Unit,
) {
    var checked by remember { mutableStateOf(setOf<String>()) }
    Text(lessonTitle(lesson), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(R.string.course_minutes, lesson.minMinutes, lesson.maxMinutes))
    Text(lesson.citation)
    lesson.stepIds.forEach { step ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = step in checked,
                onCheckedChange = { on ->
                    checked = if (on) checked + step else checked - step
                },
            )
            Text(stringResource(stepLabel(step)))
        }
    }
    Button(
        onClick = { onFinish(lesson.practice, checked) },
        enabled = BuiltInCourse.checkpointsMet(lesson, checked),
    ) {
        Text(stringResource(R.string.course_finish))
    }
}

@Composable
private fun lessonTitle(lesson: CourseLesson): String = stringResource(
    when (lesson.practice) {
        BuiltInPractice.HEARTFULNESS -> R.string.course_heartfulness
        BuiltInPractice.YOGA_PAUSE -> R.string.course_yoga
        BuiltInPractice.MICRO_QIGONG -> R.string.course_qigong
    },
)

private fun stepLabel(step: String): Int = when (step) {
    "scan" -> R.string.course_step_scan
    "release" -> R.string.course_step_release
    "settle" -> R.string.course_step_settle
    "breath" -> R.string.course_step_breath
    "stretch" -> R.string.course_step_stretch
    "notice" -> R.string.course_step_notice
    "stand" -> R.string.course_step_stand
    "shift" -> R.string.course_step_shift
    else -> R.string.course_step_rest
}
