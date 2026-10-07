package com.example.clockplannerproject.kit.render

import com.example.clockplannerproject.kit.core.ViewMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RendererFactoryTest {

    @Test
    fun get_dial_returnsTimeDialRenderer() {
        assertSame(TimeDialRenderer, RendererFactory.get(ViewMode.DIAL))
        assertEquals(ViewMode.DIAL, RendererFactory.get(ViewMode.DIAL).mode)
    }

    @Test
    fun get_pizzaAndPetals_areDedicatedRenderers() {
        assertSame(PizzaRenderer, RendererFactory.get(ViewMode.PIZZA))
        assertSame(PetalsRenderer, RendererFactory.get(ViewMode.PETALS))
    }

    @Test
    fun canvasOrNull_list_isNull() {
        assertNull(RendererFactory.canvasOrNull(ViewMode.LIST))
    }

    @Test
    fun get_list_throws() {
        val error = try {
            RendererFactory.get(ViewMode.LIST)
            null
        } catch (thrown: IllegalStateException) {
            thrown
        }
        assertTrue(error is IllegalStateException)
    }
}
