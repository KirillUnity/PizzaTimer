package com.example.clockplannerproject.kit.compose

import androidx.compose.ui.unit.IntRect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SectorCalloutPlacementTest {

    @Test
    fun rightEdge_flipsByClampingInsideCanvas() {
        val offset = boundedCalloutOffset(
            containerWidth = 360,
            containerHeight = 360,
            anchorX = 340f,
            anchorY = 180f,
            calloutWidth = 168,
            calloutHeight = 112,
            margin = 8,
            preferRight = true,
        )

        assertEquals(184, offset.x)
        assertTrue(offset.y in 8..240)
    }

    @Test
    fun topLeft_neverLeavesSafeMargin() {
        val offset = boundedCalloutOffset(
            containerWidth = 320,
            containerHeight = 320,
            anchorX = 4f,
            anchorY = 4f,
            calloutWidth = 168,
            calloutHeight = 112,
            margin = 8,
            preferRight = false,
        )

        assertEquals(8, offset.x)
        assertEquals(8, offset.y)
    }

    @Test
    fun rightNumeral_movesTheCloudOffTheKeepOut() {
        val avoid = listOf(IntRect(320, 150, 360, 210))
        val offset = boundedCalloutOffset(
            containerWidth = 360,
            containerHeight = 360,
            anchorX = 340f,
            anchorY = 180f,
            calloutWidth = 168,
            calloutHeight = 112,
            margin = 8,
            preferRight = true,
            avoid = avoid,
        )
        val placed = IntRect(offset.x, offset.y, offset.x + 168, offset.y + 112)
        assertFalse(placed.overlaps(avoid.first()))
    }
}
