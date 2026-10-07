package com.example.clockplannerproject.kit.render

import com.example.clockplannerproject.kit.core.time.TimeMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArcLabelTest {

    @Test
    fun ellipsize_keepsFullTextWhenItFits() {
        val result = ellipsizeLabel("Sleep", maxWidth = 50f) { it.length * 10f }
        assertEquals("Sleep", result)
    }

    @Test
    fun ellipsize_appendsDotsWhenTextOverflows() {
        val result = ellipsizeLabel("Deep work", maxWidth = 70f) { it.length * 10f }
        assertTrue(result.endsWith("..."))
        assertTrue(result.length < "Deep work".length + 3)
        assertTrue(result.startsWith("Dee") || result.startsWith("Deep"))
    }

    @Test
    fun ellipsize_emptyWhenWidthIsZero() {
        assertEquals("", ellipsizeLabel("Lunch", maxWidth = 0f) { it.length * 10f })
    }

    @Test
    fun lowerHalf_isSixOClock_notTwelve() {
        assertTrue(canvasAngleIsLowerHalf(90f))
        assertFalse(canvasAngleIsLowerHalf(270f))
        assertTrue(canvasAngleIsLowerHalf(0f))
        assertFalse(canvasAngleIsLowerHalf(270f + 10f))
    }

    @Test
    fun tangentRotation_adds180OnLowerHalf() {
        val unflippedLower = 90f + 90f
        val unflippedUpper = 270f + 90f
        assertEquals(
            180f,
            TimeMath.normalizeDegrees(tangentLabelRotationDeg(90f) - unflippedLower),
            0.01f,
        )
        assertEquals(
            0f,
            TimeMath.normalizeDegrees(tangentLabelRotationDeg(270f) - unflippedUpper),
            0.01f,
        )
    }

    @Test
    fun contrastingLabel_isLightOnDarkSector() {
        assertEquals(0xFFFFFFFFL, contrastingLabelArgb(0xFF2E7D32))
    }

    @Test
    fun contrastingLabel_isDarkOnLightSector() {
        assertEquals(0xFF1C1B1FL, contrastingLabelArgb(0xFFE8E0D0))
    }
}
