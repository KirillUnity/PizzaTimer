package com.example.clockplannerproject.kit.core.config

import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.layout.ReflowMode
import com.example.clockplannerproject.kit.core.time.TaskResize

/**
 * Visual configuration for the ring dial. Mutate with [copy].
 *
 * @since 0.1.0
 */
data class TimeDialConfig(
    val colors: ColorConfig = ColorConfig.Default,
    val geometry: GeometryConfig = GeometryConfig.Default,
    val clock: ClockConfig = ClockConfig.Default,
    val interaction: InteractionConfig = InteractionConfig.Default,
    val viewMode: ViewMode = ViewMode.DIAL,
) {
    companion object {
        val Default: TimeDialConfig = TimeDialConfig()
    }
}

/**
 * Packed ARGB longs so the draw loop never parses hex strings.
 *
 * @since 0.1.0
 */
data class ColorConfig(
    val trackArgb: Long = 0x00FFFFFF,
    /** Paper canvas; pizza/petals hub uses this so Start/Pause sits in a hole. */
    val canvasArgb: Long = 0xFFFBF9F5,
    val completedArgb: Long = 0xFF8E8B82,
    val labelArgb: Long = 0xFF1C1B18,
    val nowMarkerArgb: Long = 0xFFE0533C,
    val handleArgb: Long = 0xFF1C1B18,
    val restArgb: Long = 0xFFEADBCE,
    val focusArgb: Long = 0xFFE0533C,
) {
    companion object {
        val Default: ColorConfig = ColorConfig()
    }
}

/**
 * Named layout constants. No magic numbers in renderers.
 *
 * @since 0.1.0
 */
data class GeometryConfig(
    val ringThicknessFraction: Float = RING_THICKNESS_FRACTION,
    val minSweepForLabelDeg: Float = MIN_SWEEP_FOR_LABEL_DEG,
    val labelWidthFractionOfArc: Float = LABEL_WIDTH_FRACTION_OF_ARC,
    val labelFontFractionOfThickness: Float = LABEL_FONT_FRACTION_OF_THICKNESS,
    val outerInsetFraction: Float = OUTER_INSET_FRACTION,
    val markerOverhangFraction: Float = MARKER_OVERHANG_FRACTION,
    val markerStrokeFractionOfThickness: Float = MARKER_STROKE_FRACTION,
    val chevronSpreadDeg: Float = CHEVRON_SPREAD_DEG,
    val chevronLengthFraction: Float = CHEVRON_LENGTH_FRACTION,
    val pizzaLabelRadiusFraction: Float = PIZZA_LABEL_RADIUS_FRACTION,
    val hubRadiusFraction: Float = HUB_RADIUS_FRACTION,
    val petalBaseFraction: Float = PETAL_BASE_FRACTION,
    val petalMinLengthFraction: Float = PETAL_MIN_LENGTH_FRACTION,
    val petalMaxLengthFraction: Float = PETAL_MAX_LENGTH_FRACTION,
) {
    companion object {
        const val RING_THICKNESS_FRACTION: Float = 0.22f
        const val MIN_SWEEP_FOR_LABEL_DEG: Float = 8f
        const val LABEL_WIDTH_FRACTION_OF_ARC: Float = 0.78f
        const val LABEL_FONT_FRACTION_OF_THICKNESS: Float = 0.38f
        const val OUTER_INSET_FRACTION: Float = 0.06f
        const val MARKER_OVERHANG_FRACTION: Float = 0.035f
        const val MARKER_STROKE_FRACTION: Float = 0.1f
        const val CHEVRON_SPREAD_DEG: Float = 10f
        const val CHEVRON_LENGTH_FRACTION: Float = 0.07f
        const val PIZZA_LABEL_RADIUS_FRACTION: Float = 0.55f
        const val HUB_RADIUS_FRACTION: Float = 0.18f
        const val PETAL_BASE_FRACTION: Float = 0.12f
        const val PETAL_MIN_LENGTH_FRACTION: Float = 0.42f
        const val PETAL_MAX_LENGTH_FRACTION: Float = 0.96f
        val Default: GeometryConfig = GeometryConfig()
    }
}

/**
 * Auto-rotation keeps now at 12 o'clock when [snapToNow] is true and
 * [reflowMode] is WallClock. In CompactRemaining the chevron stays at 12
 * as "now" and is not bound to sector times.
 *
 * @since 0.1.0
 */
data class ClockConfig(
    val snapToNow: Boolean = true,
    val showNowMarker: Boolean = true,
    /** Legacy header clock. LIVE CHRONO lives in the ring hole (v0.5). */
    val showCenterTime: Boolean = false,
    val reflowMode: ReflowMode = ReflowMode.WallClock,
) {
    companion object {
        val Default: ClockConfig = ClockConfig()
    }
}

/**
 * Pointer interaction. [userRotationOffsetDeg] is added to auto-now rotation;
 * it does not rewrite [com.example.clockplannerproject.kit.core.Task] times.
 *
 * @param userRotationOffsetDeg clockwise scene offset in degrees
 * @param rotateEnabled two-finger twist around the ring center
 * @param rotateSlopDeg unused for one-finger; kept for two-finger noise
 * @param resizeEnabled long-press or trailing-edge drag changes duration
 * @param resizeSnapMinutes end time snaps to this grid
 * @param resizeMinMinutes shortest allowed task
 * @param resizeHandleDeg angular slop around the trailing edge
 * @since 0.2.0
 */
data class InteractionConfig(
    val userRotationOffsetDeg: Float = 0f,
    val rotateEnabled: Boolean = true,
    val rotateSlopDeg: Float = ROTATE_SLOP_DEG,
    val resizeEnabled: Boolean = true,
    val resizeSnapMinutes: Int = TaskResize.SNAP_MINUTES,
    val resizeMinMinutes: Int = TaskResize.MIN_DURATION_MINUTES,
    val resizeHandleDeg: Float = RESIZE_HANDLE_DEG,
) {
    companion object {
        const val ROTATE_SLOP_DEG: Float = 8f
        const val RESIZE_HANDLE_DEG: Float = 14f
        val Default: InteractionConfig = InteractionConfig()
    }
}
