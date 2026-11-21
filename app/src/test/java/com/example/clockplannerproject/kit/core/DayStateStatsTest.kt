package com.example.clockplannerproject.kit.core

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DayStateStatsTest {
    private val date = LocalDate(2026, 11, 20)

    @Test
    fun twoCompletions_keepTheirNumbers() {
        val first = task("a", blocks = listOf(TimeBlock(9 * 60, 10 * 60)))
        val second = task("b", blocks = listOf(TimeBlock(11 * 60, 12 * 60)))
        val idle = task("c")
        val reports = listOf(
            record(first.id, state = 3, skill = 8, mark = DemandMark.CHALLENGE, thoughts = "тихо", easier = "короче"),
            record(second.id, state = 9, skill = 4, mark = DemandMark.HINDRANCE, thoughts = "шум", easier = ""),
        )
        val stats = DayStateStats.of(listOf(first, second, idle), reports)
        assertEquals(listOf(3, 9), stats.states)
        assertEquals(1, stats.challengeCount)
        assertEquals(1, stats.hindranceCount)
        assertEquals(listOf("тихо", "шум"), stats.thoughts)
        assertEquals(6.0, stats.meanSkill, 0.001)
        assertEquals(listOf("короче"), stats.easier)
        assertEquals(2, stats.startedCount)
    }

    @Test
    fun emptyDay_isZeros() {
        val stats = DayStateStats.of(emptyList(), emptyList())
        assertEquals(DayStateStats.Empty, stats)
        assertEquals(0, stats.hindranceCount)
        assertEquals(0, stats.challengeCount)
        assertEquals(0.0, stats.meanSkill, 0.001)
        assertEquals(0, stats.startedCount)
        assertTrue(stats.thoughts.isEmpty())
        assertTrue(stats.easier.isEmpty())
        assertTrue(stats.states.isEmpty())
    }

    @Test
    fun summary_staysOffTheOpenDial() {
        assertFalse(DayStateStats.showOnDial(sessionOpen = true))
    }

    private fun task(id: String, blocks: List<TimeBlock> = emptyList()) = Task(
        id = TaskId(id),
        title = id,
        colorArgb = 0xFF607D8B,
        blocks = blocks,
        date = date,
    )

    private fun record(
        taskId: TaskId,
        state: Int,
        skill: Int,
        mark: DemandMark,
        thoughts: String,
        easier: String,
    ): TaskReport {
        val completion = TaskCompletion(
            stateAfter = state,
            engagement = 5,
            skill = skill,
            easier = easier,
            mark = mark,
            thoughts = thoughts,
            reframe = "",
            done = true,
            progress = "",
        )
        return TaskReport(
            id = TaskCompletionCodec.reportId(taskId),
            taskId = taskId,
            createdAtEpochMillis = 0L,
            text = TaskCompletionCodec.encode(completion),
        )
    }
}
