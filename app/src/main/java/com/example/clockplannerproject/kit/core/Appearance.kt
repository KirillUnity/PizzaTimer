package com.example.clockplannerproject.kit.core

import androidx.compose.runtime.Immutable
import com.example.clockplannerproject.kit.core.config.BackgroundConfig
import com.example.clockplannerproject.kit.core.config.BackgroundPresets
import kotlinx.coroutines.flow.Flow

/**
 * Saved look of the dial: mode, accent, completed color, underlay, and paper theme.
 *
 * [darkTheme] is the one quiet switch. It does not add a second theme product.
 *
 * @param darkTheme when true, the app uses the night paper scheme
 * @since 1.0.0
 */
@Immutable
data class Appearance(
    val viewMode: ViewMode = ViewMode.DIAL,
    val accentArgb: Long = DEFAULT_ACCENT,
    val completedArgb: Long = DEFAULT_COMPLETED,
    val background: BackgroundConfig = BackgroundConfig.None,
    val darkTheme: Boolean = false,
) {
    companion object {
        const val DEFAULT_ACCENT: Long = 0xFFE0533C
        const val DEFAULT_COMPLETED: Long = 0xFF8E8B82
        val Default: Appearance = Appearance()
    }
}

/**
 * Accent and completed swatches offered in Settings.
 *
 * @since 1.0.0
 */
object AppearancePalette {
    const val ACCENT_TERRACOTTA: Long = Appearance.DEFAULT_ACCENT
    const val ACCENT_EMERALD: Long = 0xFF2E7D52
    const val ACCENT_INK: Long = 0xFF5C4033
    const val COMPLETED_WARM: Long = Appearance.DEFAULT_COMPLETED
    const val COMPLETED_COOL: Long = 0xFF9AA0A6
    const val COMPLETED_INK: Long = 0xFF5C5852
    const val SOLID_SAND: Long = 0xFFEADBCE

    val accents: List<Long> = listOf(ACCENT_TERRACOTTA, ACCENT_EMERALD, ACCENT_INK)
    val completed: List<Long> = listOf(COMPLETED_WARM, COMPLETED_COOL, COMPLETED_INK)
}

/**
 * Preferences for appearance and the onboarding "seen" flag.
 *
 * Unknown keys are ignored. This is not a Room migration.
 *
 * @since 1.0.0
 */
interface AppearanceRepository {
    fun observeAppearance(): Flow<Appearance>

    suspend fun save(appearance: Appearance)

    fun observeOnboardingSeen(): Flow<Boolean>

    suspend fun setOnboardingSeen(seen: Boolean)
}

/**
 * Maps preference values to [Appearance]. Bad or unknown values fall back
 * to defaults and never throw.
 *
 * @since 1.0.0
 */
object AppearanceCodec {
    const val KIND_NONE: String = "none"
    const val KIND_COLOR: String = "color"
    const val KIND_PRESET: String = "preset"
    const val KIND_USER: String = "user"

    /**
     * @param viewMode stored enum name, or null
     * @param backgroundKind [KIND_NONE], [KIND_COLOR], [KIND_PRESET], [KIND_USER], or anything else
     */
    fun decode(
        viewMode: String?,
        accentArgb: Long?,
        completedArgb: Long?,
        backgroundKind: String?,
        presetName: String?,
        colorArgb: Long?,
        userUri: String?,
        darkTheme: Boolean? = null,
    ): Appearance = Appearance(
        viewMode = viewMode.toViewMode(),
        accentArgb = accentArgb ?: Appearance.DEFAULT_ACCENT,
        completedArgb = completedArgb ?: Appearance.DEFAULT_COMPLETED,
        background = background(backgroundKind, presetName, colorArgb, userUri),
        darkTheme = darkTheme == true,
    )

    fun encode(appearance: Appearance): EncodedAppearance {
        val background = appearance.background
        return EncodedAppearance(
            viewMode = appearance.viewMode.name,
            accentArgb = appearance.accentArgb,
            completedArgb = appearance.completedArgb,
            backgroundKind = when (background) {
                BackgroundConfig.None -> KIND_NONE
                is BackgroundConfig.Color -> KIND_COLOR
                is BackgroundConfig.Preset -> KIND_PRESET
                is BackgroundConfig.UserImage -> KIND_USER
            },
            presetName = (background as? BackgroundConfig.Preset)?.name.orEmpty(),
            colorArgb = (background as? BackgroundConfig.Color)?.argb ?: AppearancePalette.SOLID_SAND,
            userUri = (background as? BackgroundConfig.UserImage)?.uri.orEmpty(),
            darkTheme = appearance.darkTheme,
        )
    }

    private fun background(
        kind: String?,
        presetName: String?,
        colorArgb: Long?,
        userUri: String?,
    ): BackgroundConfig = when (kind) {
        KIND_COLOR -> BackgroundConfig.Color(colorArgb ?: AppearancePalette.SOLID_SAND)
        KIND_PRESET -> {
            val name = presetName.orEmpty()
            if (BackgroundPresets.contains(name)) {
                BackgroundConfig.Preset(name)
            } else {
                BackgroundConfig.None
            }
        }
        KIND_USER -> BackgroundConfig.UserImage(userUri.orEmpty())
        else -> BackgroundConfig.None
    }

    private fun String?.toViewMode(): ViewMode =
        ViewMode.entries.firstOrNull { it.name == this } ?: ViewMode.DIAL
}

/**
 * Values written to DataStore for one [Appearance].
 *
 * @since 1.0.0
 */
data class EncodedAppearance(
    val viewMode: String,
    val accentArgb: Long,
    val completedArgb: Long,
    val backgroundKind: String,
    val presetName: String,
    val colorArgb: Long,
    val userUri: String,
    val darkTheme: Boolean = false,
)
