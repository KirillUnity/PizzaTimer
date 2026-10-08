package com.example.clockplannerproject.kit.render

import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.config.GeometryConfig
import com.example.clockplannerproject.kit.core.config.TimeDialConfig

/**
 * Canvas strategy for one [ViewMode]. [ViewMode.LIST] is not a renderer.
 *
 * @since 0.3.0
 */
interface DialRenderer {
    val mode: ViewMode

    fun containsPointer(ring: RingLayout, radius: Float): Boolean

    /**
     * Clock-face degrees for a tap. Pizza maps a near-center tap to 12 o'clock
     * ([userRotationOffsetDeg]) because `atan2(0,0)` is undefined.
     */
    fun clockAngleForHit(
        radius: Float,
        canvasDeg: Float,
        userRotationOffsetDeg: Float,
    ): Float

    fun acceptsHitRadius(ring: RingLayout, radius: Float, sweepDeg: Float): Boolean

    fun prepareLabels(
        sectors: List<PreparedSector>,
        ring: RingLayout,
        geometry: GeometryConfig,
        paint: Paint,
    ): List<PreparedArcLabel>

    /**
     * Extra [Path]s built in `drawWithCache`. Must not allocate in [render].
     */
    fun prepareExtras(
        sectors: List<PreparedSector>,
        ring: RingLayout,
        geometry: GeometryConfig,
    ): List<Path>

    fun DrawScope.render(state: DialDrawState)
}

/**
 * Immutable-enough draw payload. Paths and paints are created outside `draw()`.
 *
 * @since 0.3.0
 */
data class DialDrawState(
    val config: TimeDialConfig,
    val ring: RingLayout,
    val sectors: List<PreparedSector>,
    val labels: List<PreparedArcLabel>,
    val extras: List<Path>,
    val labelPaint: android.graphics.Paint,
    val stroke: Stroke,
    val selectedStroke: Stroke,
    val selectionOutline: Stroke,
    val chevron: Path,
    val showNowMarker: Boolean,
    val chevronColor: Color,
    val markerAlpha: Float,
    val handleDeg: Float?,
    val handleColor: Color,
    val canvasColor: Color,
)
