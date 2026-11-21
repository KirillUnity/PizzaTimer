package com.example.clockplannerproject.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.ui.theme.CompletedWarmGray
import com.example.clockplannerproject.ui.theme.EmeraldWork
import com.example.clockplannerproject.ui.theme.OchreCategory
import com.example.clockplannerproject.ui.theme.OnTerracotta
import com.example.clockplannerproject.ui.theme.PaperInk
import com.example.clockplannerproject.ui.theme.PaperOutline
import com.example.clockplannerproject.ui.theme.PaperSurface
import com.example.clockplannerproject.ui.theme.SandRest
import com.example.clockplannerproject.ui.theme.TerracottaNow

private const val FullSweep = 360f
private const val DoneSweep = 78f
private const val FrontSweep = 46f
private const val ClockTop = -90f

/**
 * Static paper figure for one introduction page. Not a live dial.
 *
 * @param page zero-based page, in thesis order
 * @since 1.2.0
 */
@Composable
fun OnboardingFigure(
    page: Int,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(figureDescription(page))
    Surface(
        modifier = modifier.semantics { contentDescription = description },
        shape = RoundedCornerShape(28.dp),
        color = PaperSurface,
        border = BorderStroke(1.dp, PaperOutline),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (page) {
                0 -> QueueFigure()
                1 -> PlaceFigure()
                2 -> StepFigure()
                else -> ThreadFigure()
            }
        }
    }
}

private fun figureDescription(page: Int): Int = when (page) {
    0 -> R.string.onboarding_cd_queue
    1 -> R.string.onboarding_cd_place
    2 -> R.string.onboarding_cd_step
    else -> R.string.onboarding_cd_thread
}

@Composable
private fun QueueFigure() {
    val beats = listOf(
        R.string.onboarding_beat_now to true,
        R.string.onboarding_beat_step to false,
        R.string.onboarding_beat_away to false,
        R.string.onboarding_beat_next to false,
    )
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        beats.forEach { (label, current) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (current) TerracottaNow else SandRest),
                )
                Text(
                    text = stringResource(label),
                    style = if (current) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    color = if (current) PaperInk else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlaceFigure() {
    Canvas(modifier = Modifier.size(200.dp)) {
        val stroke = Stroke(width = size.minDimension * 0.16f, cap = StrokeCap.Butt)
        val inset = stroke.width / 2f
        val arc = Size(size.width - stroke.width, size.height - stroke.width)
        val topLeft = Offset(inset, inset)
        drawArc(SandRest, ClockTop, FullSweep, false, topLeft, arc, style = stroke)
        drawArc(CompletedWarmGray, ClockTop, DoneSweep, false, topLeft, arc, style = stroke)
        drawArc(EmeraldWork, ClockTop + DoneSweep, FrontSweep, false, topLeft, arc, style = stroke)
        drawArc(OchreCategory, ClockTop + DoneSweep + FrontSweep, 34f, false, topLeft, arc, style = stroke)
        drawCircle(PaperInk, radius = size.minDimension * 0.028f)
    }
}

@Composable
private fun StepFigure() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.dial_live_chrono),
            style = MaterialTheme.typography.labelMedium,
            color = TerracottaNow,
        )
        Text(
            text = stringResource(R.string.onboarding_sample_time),
            style = MaterialTheme.typography.headlineMedium,
            color = PaperInk,
        )
        Text(
            text = stringResource(R.string.onboarding_sample_left),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.onboarding_sample_task),
            style = MaterialTheme.typography.titleMedium,
            color = PaperInk,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(TerracottaNow),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = OnTerracotta,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun ThreadFigure() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.onboarding_sample_next_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.onboarding_sample_next),
            style = MaterialTheme.typography.headlineSmall,
            color = PaperInk,
            textAlign = TextAlign.Start,
        )
    }
}
