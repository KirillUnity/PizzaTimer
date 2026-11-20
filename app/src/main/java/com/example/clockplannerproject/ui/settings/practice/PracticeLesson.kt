package com.example.clockplannerproject.ui.settings.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.practice.PracticeSkill
import com.example.clockplannerproject.ui.theme.OnTerracotta
import com.example.clockplannerproject.ui.theme.PaperCanvas
import com.example.clockplannerproject.ui.theme.TerracottaNow

/**
 * One craft explained with a static diagram. The primary action opens the day.
 *
 * @param onOpenDay navigates to the Day destination
 * @param onBack returns to the hub
 * @since 1.3.0
 */
@Composable
fun PracticeLesson(
    skill: PracticeSkill,
    onOpenDay: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val copy = lessonCopy(skill)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaperCanvas)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PracticeTopBar(title = stringResource(copy.title), onBack = onBack)
        LessonDiagram(skill, modifier = Modifier.fillMaxWidth())
        Text(
            text = stringResource(copy.body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Button(
            onClick = onOpenDay,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TerracottaNow,
                contentColor = OnTerracotta,
            ),
        ) {
            Text(stringResource(copy.primary))
        }
        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(stringResource(R.string.practice_got_it))
        }
    }
}

@Composable
private fun LessonDiagram(skill: PracticeSkill, modifier: Modifier = Modifier) {
    when (skill) {
        PracticeSkill.LEFTOVER -> LeftoverSketch(modifier.padding(horizontal = 16.dp))
        else -> Box(modifier, contentAlignment = Alignment.Center) {
            DialSketch()
        }
    }
}

private data class LessonCopy(
    val title: Int,
    val body: Int,
    val primary: Int,
)

private fun lessonCopy(skill: PracticeSkill): LessonCopy = when (skill) {
    PracticeSkill.DIAL -> LessonCopy(
        R.string.practice_dial_title,
        R.string.practice_dial_body,
        R.string.practice_try_on_day,
    )
    PracticeSkill.LEFTOVER -> LessonCopy(
        R.string.practice_leftover_title,
        R.string.practice_leftover_body,
        R.string.practice_try_on_day,
    )
    PracticeSkill.START -> LessonCopy(
        R.string.practice_start_title,
        R.string.practice_start_body,
        R.string.practice_to_live_day,
    )
    PracticeSkill.CHECKPOINT -> LessonCopy(
        R.string.practice_checkpoint_title,
        R.string.practice_checkpoint_body,
        R.string.practice_to_live_day,
    )
    PracticeSkill.DAY_CLOSE -> LessonCopy(
        R.string.practice_day_close_title,
        R.string.practice_day_close_body,
        R.string.practice_to_live_day,
    )
}
