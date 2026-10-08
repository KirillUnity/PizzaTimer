package com.example.clockplannerproject.kit.render

import android.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.config.GeometryConfig
import com.example.clockplannerproject.kit.core.time.TimeMath
import kotlin.math.cos
import kotlin.math.sin

/**
 * Full pie slices from the center. Layout angles match [prepareSectors]
 * (including CompactRemaining). Labels sit on the radius, not the ring tangent.
 *
 * @since 0.3.0
 */
object PizzaRenderer : DialRenderer {

    override val mode: ViewMode = ViewMode.PIZZA

    override fun containsPointer(ring: RingLayout, radius: Float): Boolean =
        radius in 0f..ring.outerRadius

    override fun clockAngleForHit(
        radius: Float,
        canvasDeg: Float,
        userRotationOffsetDeg: Float,
    ): Float =
        if (radius < CENTER_HIT_EPS) {
            userRotationOffsetDeg
        } else {
            TimeMath.fromCanvasAngle(canvasDeg)
        }

    override fun acceptsHitRadius(ring: RingLayout, radius: Float, sweepDeg: Float): Boolean =
        containsPointer(ring, radius)

    override fun prepareLabels(
        sectors: List<PreparedSector>,
        ring: RingLayout,
        geometry: GeometryConfig,
        paint: Paint,
    ): List<PreparedArcLabel> = prepareRadialLabels(sectors, ring, geometry, paint)

    override fun prepareExtras(
        sectors: List<PreparedSector>,
        ring: RingLayout,
        geometry: GeometryConfig,
    ): List<Path> = emptyList()

    override fun DrawScope.render(state: DialDrawState) {
        renderPizzaSlices(state.sectors, state.ring)
        state.sectors.forEach { sector ->
            if (sector.isSelected && sector.sweepDeg > 0f) {
                drawArc(
                    color = state.handleColor,
                    startAngle = sector.canvasStartDeg,
                    sweepAngle = sector.sweepDeg,
                    useCenter = true,
                    topLeft = state.ring.topLeft,
                    size = state.ring.arcSize,
                    style = state.selectionOutline,
                )
            }
        }
        drawCircle(
            color = state.canvasColor,
            radius = state.ring.outerRadius * state.config.geometry.hubRadiusFraction,
            center = state.ring.center,
        )
        TimeDialRenderer.run {
            renderArcLabels(state.labels, state.labelPaint)
            if (state.showNowMarker) {
                renderFocusChevron(state.chevron, state.chevronColor, state.markerAlpha)
            }
            val handleDeg = state.handleDeg
            if (handleDeg != null) {
                renderRadialTick(state.ring, state.handleColor, state.config.geometry, handleDeg)
            }
        }
    }

    private const val CENTER_HIT_EPS: Float = 4f
}

/**
 * Radial labels: glyph center at [GeometryConfig.pizzaLabelRadiusFraction] of
 * [RingLayout.outerRadius], rotation follows the radius (outward). Thin slices
 * skip text using [GeometryConfig.minSweepForLabelDeg].
 *
 * @since 0.3.0
 */
fun prepareRadialLabels(
    sectors: List<PreparedSector>,
    ring: RingLayout,
    geometry: GeometryConfig,
    paint: Paint,
): List<PreparedArcLabel> {
    val metrics = paint.fontMetrics
    val vOffset = -(metrics.ascent + metrics.descent) / 2f
    val labelR = ring.outerRadius * geometry.pizzaLabelRadiusFraction
    return sectors.mapNotNull { sector ->
        if (sector.sweepDeg < geometry.minSweepForLabelDeg) return@mapNotNull null
        val chord = 2f * labelR * sin(Math.toRadians(sector.sweepDeg / 2.0)).toFloat()
        val maxWidth = chord * geometry.labelWidthFractionOfArc
        val text = ellipsizeLabel(sector.title, maxWidth) { paint.measureText(it) }
        if (text.isEmpty()) return@mapNotNull null
        val canvasRad = Math.toRadians(sector.canvasMidDeg.toDouble())
        PreparedArcLabel(
            x = ring.center.x + labelR * cos(canvasRad).toFloat(),
            y = ring.center.y + labelR * sin(canvasRad).toFloat(),
            rotationDeg = tangentLabelRotationDeg(sector.canvasMidDeg),
            text = text,
            vOffset = vOffset,
            colorArgb = sector.labelArgb,
        )
    }
}

fun DrawScope.renderPizzaSlices(
    sectors: List<PreparedSector>,
    ring: RingLayout,
) {
    sectors.forEach { sector ->
        if (sector.sweepDeg <= 0f) return@forEach
        drawArc(
            color = sector.color,
            startAngle = sector.canvasStartDeg,
            sweepAngle = sector.sweepDeg,
            useCenter = true,
            topLeft = ring.topLeft,
            size = ring.arcSize,
        )
    }
}
