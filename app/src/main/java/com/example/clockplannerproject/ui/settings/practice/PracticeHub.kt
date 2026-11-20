package com.example.clockplannerproject.ui.settings.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.practice.PracticeSkill
import com.example.clockplannerproject.kit.core.practice.WeekDayMinutes
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import com.example.clockplannerproject.ui.theme.EmeraldWork
import com.example.clockplannerproject.ui.theme.OnTerracotta
import com.example.clockplannerproject.ui.theme.PaperCanvas
import com.example.clockplannerproject.ui.theme.PaperSurface
import com.example.clockplannerproject.ui.theme.SandRest
import com.example.clockplannerproject.ui.theme.TerracottaNow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * Practice hub: progress, five skill rows, the week strip, and the intro link.
 *
 * @param onOpenDay leaves Settings for the Day destination
 * @since 1.3.0
 */
@Composable
fun PracticeHub(
    tried: Set<PracticeSkill>,
    week: List<WeekDayMinutes>,
    onOpenLesson: (PracticeSkill) -> Unit,
    onOpenDay: () -> Unit,
    onShowIntroduction: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val triedCount = tried.size
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaperCanvas)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PracticeTopBar(title = stringResource(R.string.practice_title), onBack = onBack)
        Text(
            text = stringResource(R.string.practice_eyebrow),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Text(
            text = stringResource(R.string.practice_progress, triedCount),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        TriedTrack(fraction = triedCount / PracticeSkill.entries.size.toFloat())
        PracticeSkill.entries.forEach { skill ->
            SkillRow(
                skill = skill,
                tried = skill in tried,
                onClick = { onOpenLesson(skill) },
            )
        }
        PracticeWeekStrip(
            days = week,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        if (triedCount == 0) {
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
                Text(stringResource(R.string.practice_to_plan))
            }
        }
        TextButton(
            onClick = onShowIntroduction,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            Text(stringResource(R.string.settings_show_onboarding))
        }
    }
}

@Composable
internal fun PracticeTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.practice_cd_back),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun TriedTrack(fraction: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(SandRest),
    ) {
        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(EmeraldWork),
            )
        }
    }
}

@Composable
private fun SkillRow(
    skill: PracticeSkill,
    tried: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(skillTitle(skill))
    val status = stringResource(if (tried) R.string.practice_tried else R.string.practice_not_yet)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PaperSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (tried) EmeraldWork else SandRest),
            contentAlignment = Alignment.Center,
        ) {
            if (tried) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = OnTerracotta,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { contentDescription = status },
        )
    }
}

internal fun skillTitle(skill: PracticeSkill): Int = when (skill) {
    PracticeSkill.DIAL -> R.string.practice_dial_title
    PracticeSkill.LEFTOVER -> R.string.practice_leftover_title
    PracticeSkill.START -> R.string.practice_start_title
    PracticeSkill.CHECKPOINT -> R.string.practice_checkpoint_title
    PracticeSkill.DAY_CLOSE -> R.string.practice_day_close_title
}

@Preview(showBackground = true, name = "Practice hub 0 of 5")
@Composable
private fun PracticeHubEmptyPreview() {
    ClockPlannerProjectTheme {
        PracticeHub(
            tried = emptySet(),
            week = previewWeek(List(7) { 0 }),
            onOpenLesson = {},
            onOpenDay = {},
            onShowIntroduction = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Practice hub 3 of 5")
@Composable
private fun PracticeHubThreePreview() {
    ClockPlannerProjectTheme {
        PracticeHub(
            tried = setOf(PracticeSkill.DIAL, PracticeSkill.LEFTOVER, PracticeSkill.START),
            week = previewWeek(listOf(40, 0, 25, 80, 15, 30, 55)),
            onOpenLesson = {},
            onOpenDay = {},
            onShowIntroduction = {},
            onBack = {},
        )
    }
}

private fun previewWeek(minutes: List<Int>): List<WeekDayMinutes> {
    val monday = LocalDate(2026, 11, 16)
    return minutes.mapIndexed { index, value ->
        WeekDayMinutes(monday.plus(index, DateTimeUnit.DAY), value)
    }
}
