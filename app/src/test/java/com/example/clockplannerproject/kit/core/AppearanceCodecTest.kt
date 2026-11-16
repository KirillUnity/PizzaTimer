package com.example.clockplannerproject.kit.core

import com.example.clockplannerproject.kit.core.config.BackgroundConfig
import com.example.clockplannerproject.kit.core.config.BackgroundPresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppearanceCodecTest {

    @Test
    fun unknownModeAndKind_fallBackWithoutThrowing() {
        val decoded = AppearanceCodec.decode(
            viewMode = "SPIRAL",
            accentArgb = null,
            completedArgb = null,
            backgroundKind = "hologram",
            presetName = "nope",
            colorArgb = null,
            userUri = null,
        )
        assertEquals(ViewMode.DIAL, decoded.viewMode)
        assertEquals(BackgroundConfig.None, decoded.background)
        assertEquals(Appearance.DEFAULT_ACCENT, decoded.accentArgb)
    }

    @Test
    fun presetRoundTrip() {
        val saved = Appearance(
            viewMode = ViewMode.PIZZA,
            accentArgb = AppearancePalette.ACCENT_EMERALD,
            completedArgb = AppearancePalette.COMPLETED_COOL,
            background = BackgroundConfig.Preset(BackgroundPresets.NIGHT_INK),
            darkTheme = true,
        )
        val encoded = AppearanceCodec.encode(saved)
        val decoded = AppearanceCodec.decode(
            viewMode = encoded.viewMode,
            accentArgb = encoded.accentArgb,
            completedArgb = encoded.completedArgb,
            backgroundKind = encoded.backgroundKind,
            presetName = encoded.presetName,
            colorArgb = encoded.colorArgb,
            userUri = encoded.userUri,
            darkTheme = encoded.darkTheme,
        )
        assertTrue(decoded.darkTheme)
        assertEquals(saved, decoded)
        assertTrue(decoded.background is BackgroundConfig.Preset)
    }

    @Test
    fun unknownPresetName_isNone() {
        val decoded = AppearanceCodec.decode(
            viewMode = "DIAL",
            accentArgb = 1L,
            completedArgb = 2L,
            backgroundKind = AppearanceCodec.KIND_PRESET,
            presetName = "missing",
            colorArgb = null,
            userUri = null,
        )
        assertEquals(BackgroundConfig.None, decoded.background)
    }
}
