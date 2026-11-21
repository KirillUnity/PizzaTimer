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

    @Test
    fun labelArc_reversesOnLowerHalf() {
        val upper = sectorLabelArcPath(100f, 100f, 50f, 200f, 80f, reverse = false)
        val lower = sectorLabelArcPath(100f, 100f, 50f, 20f, 80f, reverse = true)
        assertFalse(upper.isEmpty)
        assertFalse(lower.isEmpty)
        assertTrue(canvasAngleIsLowerHalf(90f))
        assertFalse(canvasAngleIsLowerHalf(270f))
    }

    @Test
    fun prepareArcLabels_centersOnArc() {
        val ring = layoutRing(
            androidx.compose.ui.geometry.Size(400f, 400f),
            com.example.clockplannerproject.kit.core.config.GeometryConfig.Default,
        )
        val sector = PreparedSector(
            canvasStartDeg = 0f,
            canvasMidDeg = 90f,
            sweepDeg = 180f,
            color = androidx.compose.ui.graphics.Color(0xFFA8C5A0),
            title = "английский",
            labelArgb = 0xFF1C1B1F.toInt(),
            isSelected = false,
        )
        val paint = android.graphics.Paint().apply { textSize = 24f }
        val labels = prepareArcLabels(
            sectors = listOf(sector),
            ring = ring,
            geometry = com.example.clockplannerproject.kit.core.config.GeometryConfig.Default,
            paint = paint,
            radius = ring.midRadius,
        )
        val label = labels.single()
        assertEquals("английский", label.text)
        assertFalse(label.path.isEmpty)
        val arcLength = ring.midRadius * Math.toRadians(180.0).toFloat()
        val expected = (arcLength - paint.measureText(label.text)) / 2f
        assertEquals(expected, label.hOffset, 0.5f)
    }

    @Test
    fun dialLabels_sitOnStrokeCenterline() {
        val ring = layoutRing(
            androidx.compose.ui.geometry.Size(400f, 400f),
            com.example.clockplannerproject.kit.core.config.GeometryConfig.Default,
        )
        val sector = PreparedSector(
            canvasStartDeg = 0f,
            canvasMidDeg = 90f,
            sweepDeg = 180f,
            color = androidx.compose.ui.graphics.Color(0xFFEADBCE),
            title = "Отдых",
            labelArgb = 0xFF1C1B1F.toInt(),
            isSelected = false,
        )
        val paint = android.graphics.Paint().apply { textSize = 24f }
        val labels = TimeDialRenderer.prepareLabels(
            sectors = listOf(sector),
            ring = ring,
            geometry = com.example.clockplannerproject.kit.core.config.GeometryConfig.Default,
            paint = paint,
        )
        val label = labels.single()
        assertEquals(ring.center.x, label.x, 0.5f)
        assertEquals(ring.center.y + ring.bandCenterRadius, label.y, 0.5f)
        val arcLength = ring.bandCenterRadius * Math.toRadians(180.0).toFloat()
        val expected = (arcLength - paint.measureText(label.text)) / 2f
        assertEquals(expected, label.hOffset, 0.5f)
    }
}
