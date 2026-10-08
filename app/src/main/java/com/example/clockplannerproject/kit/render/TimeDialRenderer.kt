package com.example.clockplannerproject.kit.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.config.GeometryConfig
import com.example.clockplannerproject.kit.core.time.TimeMath
import android.graphics.Paint as AndroidPaint

/**
 * Draws a 12-hour ring. Create [Stroke], [AndroidPaint], and label [android.graphics.Path]
 * in `drawWithCache`, not inside the `onDrawBehind` loop.
 *
 * @since 0.1.0
 */
object TimeDialRenderer : DialRenderer {

    override val mode: ViewMode = ViewMode.DIAL

    override fun containsPointer(ring: RingLayout, radius: Float): Boolean =
        radius in ring.innerRadius..ring.outerRadius

    override fun clockAngleForHit(
        radius: Float,
        canvasDeg: Float,
        userRotationOffsetDeg: Float,
    ): Float = TimeMath.fromCanvasAngle(canvasDeg)

    override fun acceptsHitRadius(ring: RingLayout, radius: Float, sweepDeg: Float): Boolean =
        containsPointer(ring, radius)

    override fun prepareLabels(
        sectors: List<PreparedSector>,
        ring: RingLayout,
        geometry: GeometryConfig,
        paint: AndroidPaint,
    ): List<PreparedArcLabel> = prepareArcLabels(sectors, ring, geometry, paint)

    override fun prepareExtras(
        sectors: List<PreparedSector>,
        ring: RingLayout,
        geometry: GeometryConfig,
    ): List<androidx.compose.ui.graphics.Path> = emptyList()

    override fun DrawScope.render(state: DialDrawState) {
        renderTaskSectors(
            sectors = state.sectors,
            ring = state.ring,
            stroke = state.stroke,
            selectedStroke = state.selectedStroke,
            selectionColor = state.handleColor,
        )
        renderArcLabels(state.labels, state.labelPaint)
        if (state.showNowMarker) {
            renderFocusChevron(state.chevron, state.chevronColor, state.markerAlpha)
        }
        val handleDeg = state.handleDeg
        if (handleDeg != null) {
            renderRadialTick(state.ring, state.handleColor, state.config.geometry, handleDeg)
        }
    }

    fun DrawScope.renderBackground(
        ring: RingLayout,
        trackColor: Color,
        stroke: Stroke,
    ) {
        drawArc(
            color = trackColor,
            startAngle = TimeMath.toCanvasAngle(0f),
            sweepAngle = TimeMath.DEGREES_CIRCLE,
            useCenter = false,
            topLeft = ring.topLeft,
            size = ring.arcSize,
            style = stroke,
        )
    }

    /**
     * Draws [sectors] in list order. [prepareSectors] puts longer sweeps first
     * so shorter overlapping tasks paint on top (matches hit-test).
     */
    fun DrawScope.renderTaskSectors(
        sectors: List<PreparedSector>,
        ring: RingLayout,
        stroke: Stroke,
        selectedStroke: Stroke,
        selectionColor: Color,
    ) {
        sectors.forEach { sector ->
            if (sector.sweepDeg <= 0f) return@forEach
            if (sector.isSelected) {
                drawArc(
                    color = selectionColor,
                    startAngle = sector.canvasStartDeg,
                    sweepAngle = sector.sweepDeg,
                    useCenter = false,
                    topLeft = ring.topLeft,
                    size = ring.arcSize,
                    style = selectedStroke,
                )
            }
            drawArc(
                color = sector.color,
                startAngle = sector.canvasStartDeg,
                sweepAngle = sector.sweepDeg,
                useCenter = false,
                topLeft = ring.topLeft,
                size = ring.arcSize,
                style = stroke,
            )
        }
    }

    /**
     * Draws tangent titles at sector midpoints. [paint] is cached.
     */
    fun DrawScope.renderArcLabels(
        labels: List<PreparedArcLabel>,
        paint: AndroidPaint,
    ) {
        if (labels.isEmpty()) return
        drawContext.canvas.nativeCanvas.let { native ->
            labels.forEach { label ->
                paint.color = label.colorArgb
                native.save()
                native.translate(label.x, label.y)
                native.rotate(label.rotationDeg)
                native.drawText(label.text, 0f, label.vOffset, paint)
                native.restore()
            }
        }
    }

    /**
     * Radial tick at [clockAngleDeg] (0° = 12 o'clock).
     */
    fun DrawScope.renderRadialTick(
        ring: RingLayout,
        color: Color,
        geometry: GeometryConfig,
        clockAngleDeg: Float,
    ) {
        val canvasDeg = TimeMath.toCanvasAngle(clockAngleDeg)
        val rad = Math.toRadians(canvasDeg.toDouble())
        val cos = kotlin.math.cos(rad).toFloat()
        val sin = kotlin.math.sin(rad).toFloat()
        val overhang = ring.outerRadius * geometry.markerOverhangFraction
        val width = (ring.thickness * geometry.markerStrokeFractionOfThickness).coerceAtLeast(3f)
        drawLine(
            color = color,
            start = Offset(
                ring.center.x + ring.innerRadius * cos,
                ring.center.y + ring.innerRadius * sin,
            ),
            end = Offset(
                ring.center.x + (ring.outerRadius + overhang) * cos,
                ring.center.y + (ring.outerRadius + overhang) * sin,
            ),
            strokeWidth = width,
        )
    }

    /**
     * Fills [path] with a chevron at the outer rim pointing inward.
     * Call from `drawWithCache`, not from `draw()`.
     *
     * In CompactRemaining the chevron stays at 12 as "now" and is not bound
     * to reflowed sector times.
     */
    fun focusChevronPath(
        path: androidx.compose.ui.graphics.Path,
        ring: RingLayout,
        geometry: GeometryConfig,
        clockAngleDeg: Float = 0f,
    ): androidx.compose.ui.graphics.Path {
        path.rewind()
        val canvasDeg = TimeMath.toCanvasAngle(clockAngleDeg)
        val rad = Math.toRadians(canvasDeg.toDouble())
        val halfSpread = Math.toRadians(geometry.chevronSpreadDeg.toDouble() / 2.0)
        val tipR = ring.outerRadius - ring.outerRadius * geometry.chevronLengthFraction
        val baseR = ring.outerRadius + ring.outerRadius * geometry.markerOverhangFraction
        val cx = ring.center.x
        val cy = ring.center.y
        path.moveTo(
            cx + tipR * kotlin.math.cos(rad).toFloat(),
            cy + tipR * kotlin.math.sin(rad).toFloat(),
        )
        path.lineTo(
            cx + baseR * kotlin.math.cos(rad - halfSpread).toFloat(),
            cy + baseR * kotlin.math.sin(rad - halfSpread).toFloat(),
        )
        path.lineTo(
            cx + baseR * kotlin.math.cos(rad + halfSpread).toFloat(),
            cy + baseR * kotlin.math.sin(rad + halfSpread).toFloat(),
        )
        path.close()
        return path
    }

    fun DrawScope.renderFocusChevron(
        path: androidx.compose.ui.graphics.Path,
        color: Color,
        alpha: Float = 1f,
    ) {
        drawPath(path, color = color.copy(alpha = (color.alpha * alpha).coerceIn(0f, 1f)))
    }

    /**
     * Focus chevron at 12 o'clock plus [userRotationOffsetDeg].
     */
    fun DrawScope.renderNowMarker(
        path: androidx.compose.ui.graphics.Path,
        color: Color,
        alpha: Float = 1f,
    ) {
        renderFocusChevron(path, color, alpha)
    }
}
