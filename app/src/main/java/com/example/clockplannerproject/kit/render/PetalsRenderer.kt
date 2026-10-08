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
 * One bezier leaf per sector along its mid-angle. Length scales with sweep
 * (duration). Paths are filled in [prepareExtras] — [render] must not call `Path()`.
 *
 * Hit-test is an **annular wedge**, not the bezier outline: the tap must lie
 * in the sector's clock sweep and between [petalBaseRadius] and
 * [petalTipRadius] (±8% slop). Tight waists of the leaf can miss; wide
 * shoulders can hit slightly outside the paint.
 *
 * @since 0.3.0
 */
object PetalsRenderer : DialRenderer {

    override val mode: ViewMode = ViewMode.PETALS

    override fun containsPointer(ring: RingLayout, radius: Float): Boolean =
        radius in 0f..ring.outerRadius

    override fun clockAngleForHit(
        radius: Float,
        canvasDeg: Float,
        userRotationOffsetDeg: Float,
    ): Float = TimeMath.fromCanvasAngle(canvasDeg)

    override fun acceptsHitRadius(ring: RingLayout, radius: Float, sweepDeg: Float): Boolean {
        val base = petalBaseRadius(ring)
        val tip = petalTipRadius(ring, sweepDeg)
        val slop = ring.outerRadius * HIT_SLOP_FRACTION
        return radius in (base - slop)..(tip + slop)
    }

    override fun prepareLabels(
        sectors: List<PreparedSector>,
        ring: RingLayout,
        geometry: GeometryConfig,
        paint: Paint,
    ): List<PreparedArcLabel> = prepareArcLabels(sectors, ring, geometry, paint)

    override fun prepareExtras(
        sectors: List<PreparedSector>,
        ring: RingLayout,
        geometry: GeometryConfig,
    ): List<Path> = sectors.map { sector ->
        val path = Path()
        fillPetalPath(path, ring, geometry, sector)
        path
    }

    override fun DrawScope.render(state: DialDrawState) {
        state.extras.forEachIndexed { index, path ->
            val sector = state.sectors.getOrNull(index) ?: return@forEachIndexed
            if (sector.sweepDeg <= 0f) return@forEachIndexed
            if (sector.isSelected) {
                drawPath(
                    path = path,
                    color = state.handleColor,
                    style = state.selectionOutline,
                )
            }
            drawPath(path, color = sector.color)
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

    private const val HIT_SLOP_FRACTION: Float = 0.08f
}

fun petalBaseRadius(ring: RingLayout): Float =
    ring.outerRadius * GeometryConfig.PETAL_BASE_FRACTION

fun petalTipRadius(ring: RingLayout, sweepDeg: Float): Float {
    val t = (sweepDeg / 180f).coerceIn(0f, 1f)
    val fraction = GeometryConfig.PETAL_MIN_LENGTH_FRACTION +
        (GeometryConfig.PETAL_MAX_LENGTH_FRACTION - GeometryConfig.PETAL_MIN_LENGTH_FRACTION) * t
    return ring.outerRadius * fraction
}

/**
 * Leaf along [PreparedSector.canvasMidDeg]. Call from `drawWithCache`.
 */
fun fillPetalPath(
    path: Path,
    ring: RingLayout,
    geometry: GeometryConfig,
    sector: PreparedSector,
) {
    path.rewind()
    if (sector.sweepDeg <= 0f) return
    val mid = Math.toRadians(sector.canvasMidDeg.toDouble())
    val baseR = ring.outerRadius * geometry.petalBaseFraction
    val tipR = petalTipRadius(ring, sector.sweepDeg)
    val halfWidth = Math.toRadians((7.0 + sector.sweepDeg * 0.14).coerceIn(7.0, 24.0))
    val cx = ring.center.x
    val cy = ring.center.y
    val cosM = cos(mid).toFloat()
    val sinM = sin(mid).toFloat()
    val waistR = (baseR + tipR) * 0.55f
    val left = mid - halfWidth
    val right = mid + halfWidth
    val baseX = cx + baseR * cosM
    val baseY = cy + baseR * sinM
    val tipX = cx + tipR * cosM
    val tipY = cy + tipR * sinM
    val leftX = cx + waistR * cos(left).toFloat()
    val leftY = cy + waistR * sin(left).toFloat()
    val rightX = cx + waistR * cos(right).toFloat()
    val rightY = cy + waistR * sin(right).toFloat()
    path.moveTo(baseX, baseY)
    path.cubicTo(leftX, leftY, leftX, leftY, tipX, tipY)
    path.cubicTo(rightX, rightY, rightX, rightY, baseX, baseY)
    path.close()
}
