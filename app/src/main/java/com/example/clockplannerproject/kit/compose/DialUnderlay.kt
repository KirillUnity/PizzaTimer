package com.example.clockplannerproject.kit.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.config.BackgroundConfig
import com.example.clockplannerproject.kit.core.config.BackgroundPresets
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme

/**
 * Underlay for DIAL and PIZZA. A paper scrim keeps numerals readable.
 * [BackgroundConfig.UserImage] draws the "coming" stub and no photo.
 *
 * @param background current underlay
 * @since 1.0.0
 */
@Composable
fun DialUnderlay(
    background: BackgroundConfig,
    modifier: Modifier = Modifier,
) {
    if (background is BackgroundConfig.None) return
    val scrim = argbToColor(BackgroundConfig.SCRIM_ARGB).copy(alpha = BackgroundConfig.SCRIM_ALPHA)
    Box(modifier = modifier.fillMaxSize()) {
        when (background) {
            BackgroundConfig.None -> Unit
            is BackgroundConfig.Color -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(argbToColor(background.argb)),
            )
            is BackgroundConfig.Preset -> {
                val res = presetDrawable(background.name)
                if (res != null) {
                    Image(
                        painter = painterResource(res),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            is BackgroundConfig.UserImage -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(argbToColor(BackgroundConfig.SCRIM_ARGB)),
            )
        }
        if (background !is BackgroundConfig.UserImage) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scrim),
            )
        }
        if (background is BackgroundConfig.UserImage) {
            Text(
                text = stringResource(R.string.background_user_coming),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

/**
 * @return drawable for a known preset, or null when [name] is not bundled
 */
fun presetDrawable(name: String): Int? = when (name) {
    BackgroundPresets.PARCHMENT -> R.drawable.bg_parchment
    BackgroundPresets.NIGHT_INK -> R.drawable.bg_night_ink
    BackgroundPresets.TERRACOTTA_WASH -> R.drawable.bg_terracotta_wash
    else -> null
}

@Preview(showBackground = true, widthDp = 200, heightDp = 200)
@Composable
private fun DialUnderlayParchmentPreview() {
    ClockPlannerProjectTheme {
        DialUnderlay(background = BackgroundConfig.Preset(BackgroundPresets.PARCHMENT))
    }
}
