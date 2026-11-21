package com.example.clockplannerproject.kit.render

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.clockplannerproject.kit.core.config.GeometryConfig
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlin.math.cos
import kotlin.math.sin

private const val ELLIPSIS = "..."

/**
 * Title along a sector arc. [path] is built in `drawWithCache`, not in `draw()`.
 * An empty [path] means a straight tangent at ([x], [y]) with [rotationDeg]
 * (pizza radial helper).
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
    val path: Path = Path(),
    val hOffset: Float = 0f,
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
 * Builds titles on a circular arc of [radius]. Paths are created here, not in `draw()`.
 * The dial passes [RingLayout.bandCenterRadius] so glyphs sit on the painted stroke.
 * Petals pass [RingLayout.midRadius].
 *
 * @param radius distance from the ring center to the label baseline
 * @since 0.2.0
 */
fun prepareArcLabels(
    sectors: List<PreparedSector>,
    ring: RingLayout,
    geometry: GeometryConfig,
    paint: Paint,
    radius: Float,
): List<PreparedArcLabel> {
    val metrics = paint.fontMetrics
    val vOffset = if (metrics == null) {
        0f
    } else {
        -(metrics.ascent + metrics.descent) / 2f
    }
    return sectors.mapNotNull { sector ->
        if (sector.sweepDeg < geometry.minSweepForLabelDeg) return@mapNotNull null
        val sweepRad = Math.toRadians(sector.sweepDeg.toDouble()).toFloat()
        val arcLength = radius * sweepRad
        val maxWidth = arcLength * geometry.labelWidthFractionOfArc
        val text = ellipsizeLabel(sector.title, maxWidth) { paint.measureText(it) }
        if (text.isEmpty()) return@mapNotNull null
        val textWidth = paint.measureText(text)
        val canvasRad = Math.toRadians(sector.canvasMidDeg.toDouble())
        val x = ring.center.x + radius * cos(canvasRad).toFloat()
        val y = ring.center.y + radius * sin(canvasRad).toFloat()
        PreparedArcLabel(
            x = x,
            y = y,
            rotationDeg = tangentLabelRotationDeg(sector.canvasMidDeg),
            text = text,
            vOffset = vOffset,
            colorArgb = sector.labelArgb,
            path = sectorLabelArcPath(
                centerX = ring.center.x,
                centerY = ring.center.y,
                radius = radius,
                canvasStartDeg = sector.canvasStartDeg,
                sweepDeg = sector.sweepDeg,
                reverse = canvasAngleIsLowerHalf(sector.canvasMidDeg),
            ),
            hOffset = ((arcLength - textWidth) / 2f).coerceAtLeast(0f),
        )
    }
}

/**
 * Circular baseline for [drawTextOnPath]. [reverse] walks the arc backward
 * so glyphs on the lower half stay upright.
 *
 * @since 1.2.0
 */
fun sectorLabelArcPath(
    centerX: Float,
    centerY: Float,
    radius: Float,
    canvasStartDeg: Float,
    sweepDeg: Float,
    reverse: Boolean,
): Path {
    val path = Path()
    val oval = RectF(
        centerX - radius,
        centerY - radius,
        centerX + radius,
        centerY + radius,
    )
    if (reverse) {
        path.addArc(oval, canvasStartDeg + sweepDeg, -sweepDeg)
    } else {
        path.addArc(oval, canvasStartDeg, sweepDeg)
    }
    return path
}
