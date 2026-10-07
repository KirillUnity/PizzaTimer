package com.example.clockplannerproject.kit.render

import android.graphics.Paint
import com.example.clockplannerproject.kit.core.config.GeometryConfig
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlin.math.cos
import kotlin.math.sin

private const val ELLIPSIS = "..."

/**
 * Tangent label at a sector midpoint. Built in `drawWithCache`, not in `draw()`.
 *
 * @since 0.2.0
 */
data class PreparedArcLabel(
    val x: Float,
    val y: Float,
    val rotationDeg: Float,
    val text: String,
    val vOffset: Float,
    val colorArgb: Int,
)

/**
 * Canvas `0°` is 3 o'clock. Angles `0°..180°` are the lower half of the dial.
 *
 * @since 0.2.0
 */
fun canvasAngleIsLowerHalf(canvasDeg: Float): Boolean {
    val normalized = TimeMath.normalizeDegrees(canvasDeg)
    return normalized in 0f..180f
}

/**
 * Canvas rotation for a clockwise-tangent label. Lower half adds 180° so
 * glyphs stay upright and keep reading order (no path reverse).
 *
 * @since 0.2.0
 */
fun tangentLabelRotationDeg(canvasMidDeg: Float): Float {
    val tangent = canvasMidDeg + 90f
    return if (canvasAngleIsLowerHalf(canvasMidDeg)) tangent + 180f else tangent
}

/**
 * Dark text on light sectors, light text on dark ones. Parse ARGB outside `draw()`.
 *
 * @since 0.2.0
 */
fun contrastingLabelArgb(sectorArgb: Long): Long {
    val packed = sectorArgb.toInt()
    val r = (packed shr 16) and 0xFF
    val g = (packed shr 8) and 0xFF
    val b = packed and 0xFF
    val luminance = 0.299f * r + 0.587f * g + 0.114f * b
    return if (luminance < 140f) 0xFFFFFFFF else 0xFF1C1B1F
}

/**
 * Fits [text] into [maxWidth], appending `...` when it overflows.
 *
 * @since 0.2.0
 */
fun ellipsizeLabel(
    text: String,
    maxWidth: Float,
    measure: (String) -> Float,
): String {
    if (text.isEmpty() || maxWidth <= 0f) return ""
    if (measure(text) <= maxWidth) return text
    val ellipsisWidth = measure(ELLIPSIS)
    if (ellipsisWidth >= maxWidth) return ELLIPSIS
    var low = 0
    var high = text.length
    var best = 0
    while (low <= high) {
        val mid = (low + high) / 2
        val candidate = text.substring(0, mid)
        val width = measure(candidate) + ellipsisWidth
        if (width <= maxWidth) {
            best = mid
            low = mid + 1
        } else {
            high = mid - 1
        }
    }
    if (best == 0) return ELLIPSIS
    return text.substring(0, best).trimEnd() + ELLIPSIS
}

/**
 * Builds midpoint tangent labels. No [android.graphics.Path] allocation.
 *
 * @since 0.2.0
 */
fun prepareArcLabels(
    sectors: List<PreparedSector>,
    ring: RingLayout,
    geometry: GeometryConfig,
    paint: Paint,
): List<PreparedArcLabel> {
    val metrics = paint.fontMetrics
    val vOffset = -(metrics.ascent + metrics.descent) / 2f
    return sectors.mapNotNull { sector ->
        if (sector.sweepDeg < geometry.minSweepForLabelDeg) return@mapNotNull null
        val sweepRad = Math.toRadians(sector.sweepDeg.toDouble()).toFloat()
        val arcLength = ring.midRadius * sweepRad
        val maxWidth = arcLength * geometry.labelWidthFractionOfArc
        val text = ellipsizeLabel(sector.title, maxWidth) { paint.measureText(it) }
        if (text.isEmpty()) return@mapNotNull null
        val canvasRad = Math.toRadians(sector.canvasMidDeg.toDouble())
        val x = ring.center.x + ring.midRadius * cos(canvasRad).toFloat()
        val y = ring.center.y + ring.midRadius * sin(canvasRad).toFloat()
        PreparedArcLabel(
            x = x,
            y = y,
            rotationDeg = tangentLabelRotationDeg(sector.canvasMidDeg),
            text = text,
            vOffset = vOffset,
            colorArgb = sector.labelArgb,
        )
    }
}
