package com.example.clockplannerproject.kit.core

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShortFlowScaleTest {
    @Test
    fun skippedForm_keepsState() {
        val saved = TaskCompletion.orNull(
            stateAfter = 7,
            engagement = 4,
            skill = 8,
            easier = "",
            mark = DemandMark.CHALLENGE,
            thoughts = "",
            reframe = "",
            done = true,
            progress = "",
            shortFlow = null,
        )
        val decoded = TaskCompletionCodec.decode(TaskCompletionCodec.encode(saved!!))
        assertEquals(7, decoded?.stateAfter)
        assertNull(decoded?.shortFlow)
    }

    @Test
    fun profile_isSeparateFromProxyMean() {
        val flow = List(ShortFlowScale.ITEM_COUNT) { 4 }
        val completion = TaskCompletion(
            stateAfter = 3,
            engagement = 4,
            skill = 8,
            easier = "",
            mark = DemandMark.CHALLENGE,
            thoughts = "",
            reframe = "",
            done = true,
            progress = "",
            shortFlow = flow,
        )
        val task = Task(
            id = TaskId("a"),
            title = "a",
            colorArgb = 1L,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = LocalDate(2026, 11, 20),
        )
        val report = TaskReport(
            id = TaskCompletionCodec.reportId(task.id),
            taskId = task.id,
            createdAtEpochMillis = 1L,
            text = TaskCompletionCodec.encode(completion),
        )
        val stats = DayStateStats.of(listOf(task), listOf(report))
        assertEquals(8.0, stats.meanSkill, 0.001)
        assertEquals(listOf(4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0), stats.formProfile)
        assertEquals(3, stats.states.single())
    }
}
