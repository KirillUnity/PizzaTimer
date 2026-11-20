package com.example.clockplannerproject.ui.settings.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.practice.WeekDayMinutes
import com.example.clockplannerproject.ui.theme.EmeraldWork
import com.example.clockplannerproject.ui.theme.PaperContainer
import com.example.clockplannerproject.ui.theme.PaperInk
import com.example.clockplannerproject.ui.theme.SandRest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * Seven closed-minute columns. An empty day stays in the row as sand.
 *
 * @param days Monday through Sunday
 * @since 1.3.0
 */
@Composable
fun PracticeWeekStrip(
    days: List<WeekDayMinutes>,
    modifier: Modifier = Modifier,
) {
    val total = days.sumOf { it.closedMinutes }
    val peak = days.maxOfOrNull { it.closedMinutes } ?: 0
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PaperContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.practice_week_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = if (total == 0) {
                stringResource(R.string.flow_closed_none)
            } else {
                stringResource(R.string.practice_week_total, total / 60, total % 60)
            },
            style = MaterialTheme.typography.titleLarge,
            color = PaperInk,
        )
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            days.forEach { day ->
                WeekColumn(day = day, peak = peak, modifier = Modifier.weight(1f))
            }
        }
        Text(
            text = stringResource(R.string.practice_week_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WeekColumn(
    day: WeekDayMinutes,
    peak: Int,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(dowRes(day.date))
    val description = stringResource(R.string.practice_week_day_cd, label, day.closedMinutes)
    val fraction = if (peak <= 0 || day.closedMinutes <= 0) 0f else day.closedMinutes / peak.toFloat()
    Column(
        modifier = modifier.semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SandRest),
            )
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(fraction)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldWork),
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun dowRes(date: LocalDate): Int = when (date.dayOfWeek) {
    DayOfWeek.MONDAY -> R.string.practice_dow_mon
    DayOfWeek.TUESDAY -> R.string.practice_dow_tue
    DayOfWeek.WEDNESDAY -> R.string.practice_dow_wed
    DayOfWeek.THURSDAY -> R.string.practice_dow_thu
    DayOfWeek.FRIDAY -> R.string.practice_dow_fri
    DayOfWeek.SATURDAY -> R.string.practice_dow_sat
    DayOfWeek.SUNDAY -> R.string.practice_dow_sun
}
