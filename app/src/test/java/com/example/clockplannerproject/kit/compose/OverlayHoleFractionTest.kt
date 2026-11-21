package com.example.clockplannerproject.kit.compose

import com.example.clockplannerproject.kit.core.ViewMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayHoleFractionTest {

    @Test
    fun pizzaHub_isSmallerThanDialHole() {
        val dial = overlayHoleDiameterFraction(ViewMode.DIAL)
        val pizza = overlayHoleDiameterFraction(ViewMode.PIZZA)
        val petals = overlayHoleDiameterFraction(ViewMode.PETALS)
        assertEquals(DialHoleDiameterFraction, dial, 0.0001f)
        assertEquals(pizza, petals, 0.0001f)
        assertTrue(pizza < dial)
        assertTrue(pizza > 0f)
    }
}
