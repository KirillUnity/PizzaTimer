package com.example.clockplannerproject.kit.compose

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.clockplannerproject.R
import com.example.clockplannerproject.kit.core.config.BackgroundConfig
import com.example.clockplannerproject.kit.core.config.BackgroundPresets
import com.example.clockplannerproject.ui.theme.ClockPlannerProjectTheme
import java.io.File

/**
 * Longest edge kept when decoding a user photo outside the draw loop.
 */
private const val MAX_UNDERLAY_EDGE: Int = 2048

/**
 * Underlay for DIAL and PIZZA. A paper scrim keeps numerals readable.
 *
 * [BackgroundConfig.UserImage] paints the copied file from remembered state.
 * A missing or undecodable file draws nothing, the same as [BackgroundConfig.None].
 *
 * @param background current underlay
 * @since 1.0.0
 */
@Composable
fun DialUnderlay(
    background: BackgroundConfig,
    modifier: Modifier = Modifier,
) {
    val userPath = (background as? BackgroundConfig.UserImage)?.uri
    val photo = remember(userPath) { userPath?.let { decodeUserUnderlay(it)?.asImageBitmap() } }
    if (background is BackgroundConfig.None) return
    if (background is BackgroundConfig.UserImage && photo == null) return
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
            is BackgroundConfig.UserImage -> {
                val bitmap = photo
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrim),
        )
    }
}

/**
 * @param path absolute path of the copied dial photo
 * @return false when the file is missing, empty, or not a bitmap
 */
fun userUnderlayReadable(path: String): Boolean {
    val file = File(path)
    if (!file.isFile || file.length() == 0L) return false
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    return bounds.outWidth > 0 && bounds.outHeight > 0
}

/**
 * Decodes [path] once per remembered path. Not for the canvas draw loop.
 *
 * @return null when the file cannot be shown
 */
internal fun decodeUserUnderlay(path: String): Bitmap? {
    val file = File(path)
    if (!file.isFile || file.length() == 0L) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    val options = BitmapFactory.Options().apply {
        inSampleSize = underlaySampleSize(bounds.outWidth, bounds.outHeight, MAX_UNDERLAY_EDGE)
    }
    return BitmapFactory.decodeFile(file.absolutePath, options)
}

/**
 * @return power-of-two sample size so the longer edge is at most [maxEdge]
 */
internal fun underlaySampleSize(width: Int, height: Int, maxEdge: Int): Int {
    var sample = 1
    while (width / sample > maxEdge || height / sample > maxEdge) {
        sample *= 2
    }
    return sample
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
