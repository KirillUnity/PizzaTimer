package com.example.clockplannerproject.ui.settings.practice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.example.clockplannerproject.R
import com.example.clockplannerproject.ui.theme.CobaltCategory
import com.example.clockplannerproject.ui.theme.CompletedWarmGray
import com.example.clockplannerproject.ui.theme.EmeraldWork
import com.example.clockplannerproject.ui.theme.OchreCategory
import com.example.clockplannerproject.ui.theme.PaperInk
import com.example.clockplannerproject.ui.theme.SandRest
import com.example.clockplannerproject.ui.theme.TerracottaNow

private const val SketchSweepDegrees = 360f
private const val DoneSweepDegrees = 78f
private const val LiveSweepDegrees = 46f
private const val ClockTopDegrees = -90f
private const val NeedleDegrees = 36f
private val ShareWeights = listOf(5f, 3f, 2f)

/**
 * Static 12-hour ring. Sweeps are fixed diagram constants, not a live dial.
 *
 * @since 1.3.0
 */
@Composable
fun DialSketch(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.practice_cd_dial_sketch)
    Canvas(
        modifier = modifier
            .size(180.dp)
            .semantics { contentDescription = description },
    ) {
        val stroke = Stroke(width = size.minDimension * 0.12f, cap = StrokeCap.Butt)
        val inset = stroke.width / 2f
        val arc = Size(size.width - stroke.width, size.height - stroke.width)
        val topLeft = Offset(inset, inset)
        drawArc(SandRest, ClockTopDegrees, SketchSweepDegrees, false, topLeft, arc, style = stroke)
        drawArc(CompletedWarmGray, ClockTopDegrees, DoneSweepDegrees, false, topLeft, arc, style = stroke)
        drawArc(
            OchreCategory,
            ClockTopDegrees + DoneSweepDegrees,
            LiveSweepDegrees,
            false,
            topLeft,
            arc,
            style = stroke,
        )
        val needle = Stroke(width = size.minDimension * 0.015f, cap = StrokeCap.Round)
        drawArc(
            TerracottaNow,
            ClockTopDegrees + NeedleDegrees,
            1.5f,
            false,
            topLeft,
            arc,
            style = needle,
        )
        drawCircle(PaperInk, radius = size.minDimension * 0.035f)
    }
}

/**
 * Three labeled shares, weights 5, 3, and 2. Not leftover layout.
 *
 * @since 1.3.0
 */
@Composable
fun LeftoverSketch(modifier: Modifier = Modifier) {
    val labels = listOf(
        stringResource(R.string.practice_share_high),
        stringResource(R.string.practice_share_medium),
        stringResource(R.string.practice_share_low),
    )
    val colors = listOf(EmeraldWork, OchreCategory, CobaltCategory)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ShareWeights.forEachIndexed { index, weight ->
            Column(
                modifier = Modifier.weight(weight),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors[index]),
                )
                Text(
                    text = labels[index],
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
