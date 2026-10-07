package com.example.clockplannerproject.kit.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.example.clockplannerproject.kit.compose.PreviewDialTasks
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TaskStatus
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.kit.core.config.GeometryConfig
import com.example.clockplannerproject.kit.core.config.TimeDialConfig
import com.example.clockplannerproject.kit.core.halfAnchorMinute
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HitTestTaskTest {

    private val canvas = Size(200f, 200f)
    private val config = TimeDialConfig.Default
    private val geometry = GeometryConfig.Default
    private val day = LocalDate(2026, 10, 6)

    private fun hit(
        offset: Offset,
        tasks: List<Task> = PreviewDialTasks,
        nowMinute: Float,
        half: DialHalf,
        userRotationOffset: Float = 0f,
    ): Task? {
        val anchor = halfAnchorMinute(nowMinute, half, tasks)
        return hitTestTask(
            offset = offset,
            tasks = tasks,
            config = config,
            geometry = geometry,
            anchorMinute = anchor + 0f,
            userRotationOffset = userRotationOffset,
            canvasSize = canvas,
            half = half,
        )
    }

    @Test
    fun center_isMiss() {
        assertNull(hit(Offset(100f, 100f), nowMinute = 9 * 60f, half = DialHalf.AM))
    }

    @Test
    fun pointOnRingAtTwelve_hitsTaskStartingAtNow() {
        val ring = layoutRing(canvas, geometry)
        val tap = Offset(ring.center.x, ring.center.y - ring.midRadius)
        val work = PreviewDialTasks.first { it.id.value == "work" }
        assertEquals(work.id, hit(tap, nowMinute = 9 * 60f, half = DialHalf.AM)?.id)
    }

    @Test
    fun outsideRing_isMiss() {
        assertNull(hit(Offset(100f, 1f), nowMinute = 9 * 60f, half = DialHalf.AM))
    }

    @Test
    fun overlappingSectors_smallestSweepWins() {
        val long = Task(
            id = TaskId("long"),
            title = "Long",
            colorArgb = 0xFF0000FF,
            blocks = listOf(TimeBlock(9 * 60, 12 * 60)),
            status = TaskStatus.TODO,
            date = day,
        )
        val short = Task(
            id = TaskId("short"),
            title = "Short",
            colorArgb = 0xFFFF0000,
            blocks = listOf(TimeBlock(9 * 60, 9 * 60 + 30)),
            status = TaskStatus.TODO,
            date = day,
        )
        val ring = layoutRing(canvas, geometry)
        val tap = Offset(ring.center.x, ring.center.y - ring.midRadius)
        assertEquals(
            short.id,
            hit(tap, listOf(long, short), nowMinute = 9 * 60f, half = DialHalf.AM)?.id,
        )
    }

    @Test
    fun overnightTask_atNowHitsSleep() {
        val ring = layoutRing(canvas, geometry)
        val tap = Offset(ring.center.x, ring.center.y - ring.midRadius)
        val sleep = PreviewDialTasks.first { it.id.value == "sleep" }
        assertEquals(sleep.id, hit(tap, nowMinute = 23 * 60f, half = DialHalf.PM)?.id)
    }

    @Test
    fun overnightTask_onAmFace_hitsMorningSlice() {
        val ring = layoutRing(canvas, geometry)
        val tap = Offset(ring.center.x, ring.center.y - ring.midRadius)
        val sleep = PreviewDialTasks.first { it.id.value == "sleep" }
        assertEquals(sleep.id, hit(tap, nowMinute = 60f, half = DialHalf.AM)?.id)
    }

    @Test
    fun overnightTask_laidOutPmSlice_stillHits() {
        val sleep = PreviewDialTasks.first { it.id.value == "sleep" }
        val laidOut = sleep.withRange(22 * 60, 24 * 60)
        val ring = layoutRing(canvas, geometry)
        val tap = Offset(ring.center.x, ring.center.y - ring.midRadius)
        assertEquals(
            sleep.id,
            hit(tap, listOf(laidOut), nowMinute = 23 * 60f, half = DialHalf.PM)?.id,
        )
    }

    @Test
    fun offset90_hitAtThreeOClock_matchesNowTask() {
        val ring = layoutRing(canvas, geometry)
        val tapAtThree = Offset(ring.center.x + ring.midRadius, ring.center.y)
        val work = PreviewDialTasks.first { it.id.value == "work" }
        assertEquals(
            work.id,
            hit(tapAtThree, nowMinute = 9 * 60f, half = DialHalf.AM, userRotationOffset = 90f)?.id,
        )
    }

    @Test
    fun offset90_twelveOClock_doesNotHitNowTask() {
        val ring = layoutRing(canvas, geometry)
        val tapAtTwelve = Offset(ring.center.x, ring.center.y - ring.midRadius)
        val work = PreviewDialTasks.first { it.id.value == "work" }
        val result = hit(tapAtTwelve, nowMinute = 9 * 60f, half = DialHalf.AM, userRotationOffset = 90f)
        assertTrue(result == null || result.id != work.id)
    }

    @Test
    fun amFace_whenNowIsPm_twelveHitsNearestMorningTask() {
        val ring = layoutRing(canvas, geometry)
        val tap = Offset(ring.center.x, ring.center.y - ring.midRadius)
        val work = PreviewDialTasks.first { it.id.value == "work" }
        assertEquals(work.id, hit(tap, nowMinute = 18 * 60f, half = DialHalf.AM)?.id)
    }

    @Test
    fun resizeHandle_isNearTrailingEdgeAtThreeWhenNowIsNine() {
        val work = PreviewDialTasks.first { it.id.value == "work" }
        assertTrue(
            isNearResizeHandle(
                pointerClockDeg = 90f,
                task = work,
                half = DialHalf.AM,
                anchorMinute = 9 * 60f,
                userRotationOffsetDeg = 0f,
                slopDeg = 14f,
            ),
        )
        assertTrue(
            !isNearResizeHandle(
                pointerClockDeg = 0f,
                task = work,
                half = DialHalf.AM,
                anchorMinute = 9 * 60f,
                userRotationOffsetDeg = 0f,
                slopDeg = 14f,
            ),
        )
    }
}
