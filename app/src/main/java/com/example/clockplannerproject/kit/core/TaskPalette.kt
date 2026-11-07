package com.example.clockplannerproject.kit.core

/**
 * Limited ARGB palette for task sectors. Values are packed longs, converted
 * to a UI color outside `draw()`.
 *
 * @since 0.2.0
 */
object TaskPalette {
    val colors: List<Long> = listOf(
        0xFFB8A9C9,
        0xFFA8C5A0,
        0xFFE8C4A8,
        0xFFE8B4B8,
        0xFFC5D4E8,
        0xFFD9CBB8,
        0xFFC9B8A8,
        0xFFB5C9C3,
    )

    val defaultColor: Long get() = colors.first()

    /**
     * Palette color for a task written after [used] tasks already exist.
     *
     * Index `0` is [defaultColor]. The sequence wraps after the last swatch.
     * Planner consent uses this instead of a color from the draft: the draft has none.
     *
     * @param used how many tasks already occupy a swatch
     * @return packed ARGB color from [colors]
     * @since 1.1.0
     */
    fun colorAt(used: Int): Long = colors[used.mod(colors.size)]
}
