package com.example.clockplannerproject.kit.core.config

import androidx.compose.runtime.Immutable

/**
 * What sits behind the DIAL and PIZZA faces.
 *
 * [UserImage] holds a path inside the app files directory. The bytes are
 * copied there from the system photo picker. A missing file draws like [None].
 *
 * @since 1.0.0
 */
@Immutable
sealed interface BackgroundConfig {
    /** Paper theme only. */
    data object None : BackgroundConfig

    /**
     * Flat fill.
     *
     * @param argb packed ARGB, already parsed outside the draw loop
     */
    data class Color(val argb: Long) : BackgroundConfig

    /**
     * Bundled drawable addressed by [name].
     *
     * @param name one of [BackgroundPresets]
     */
    data class Preset(val name: String) : BackgroundConfig

    /**
     * User photo copied into the app files directory.
     *
     * @param uri absolute path of that copy, not a content URI
     */
    data class UserImage(val uri: String) : BackgroundConfig

    companion object {
        /** Paper scrim over presets and solid fills so labels stay readable. */
        const val SCRIM_ALPHA: Float = 0.58f

        /** Same paper canvas as [ColorConfig.canvasArgb]. */
        const val SCRIM_ARGB: Long = 0xFFFBF9F5
    }
}

/**
 * Stable names for the three bundled underlays.
 *
 * @since 1.0.0
 */
object BackgroundPresets {
    const val PARCHMENT: String = "parchment"
    const val NIGHT_INK: String = "night_ink"
    const val TERRACOTTA_WASH: String = "terracotta_wash"

    val all: List<String> = listOf(PARCHMENT, NIGHT_INK, TERRACOTTA_WASH)

    fun contains(name: String): Boolean = name in all
}
