package com.example.clockplannerproject.kit.render

import com.example.clockplannerproject.kit.core.ViewMode

/**
 * Open/Closed dispatch. New canvas modes register here; do not add a
 * `when (mode)` inside a single draw function.
 *
 * @since 0.3.0
 */
object RendererFactory {
    /**
     * @throws IllegalArgumentException if [mode] is [ViewMode.LIST]
     */
    fun get(mode: ViewMode): DialRenderer = when (mode) {
        ViewMode.DIAL -> TimeDialRenderer
        ViewMode.PIZZA -> PizzaRenderer
        ViewMode.PETALS -> PetalsRenderer
        ViewMode.LIST -> error("LIST is Compose, not a DialRenderer")
    }

    fun canvasOrNull(mode: ViewMode): DialRenderer? =
        if (mode == ViewMode.LIST) null else get(mode)
}
