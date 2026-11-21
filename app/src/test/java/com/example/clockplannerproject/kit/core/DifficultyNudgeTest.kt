package com.example.clockplannerproject.kit.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyNudgeTest {
    @Test
    fun threshold_and_skip() {
        val two = listOf(row("a", 1), row("b", 2))
        assertNull(DifficultyNudge.offer(two, emptySet()))

        val three = two + row("c", 3)
        val offer = DifficultyNudge.offer(three, emptySet())
        assertEquals("a|b|c", offer?.key)
        assertNull(DifficultyNudge.offer(three, setOf("a|b|c")))

        val task = Task(
            id = TaskId("next"),
            title = "Черновик",
            colorArgb = 1L,
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
            date = null,
        )
        val hardened = DifficultyNudge.apply(task, HardenKind.NARROWER_RESULT)
        assertEquals(task.blocks, hardened.blocks)
        assertTrue(hardened.description.contains("NARROWER_RESULT"))
        assertEquals(8, three.first().completion.skill)
        assertEquals(4, three.first().completion.engagement)
    }

    private fun row(id: String, at: Long) = DifficultyNudge.Row(
        taskId = id,
        title = "Черновик",
        completion = TaskCompletion(
            stateAfter = 5,
            engagement = 4,
            skill = 8,
            easier = "",
            mark = DemandMark.CHALLENGE,
            thoughts = "",
            reframe = "",
            done = true,
            progress = "",
        ),
        at = at,
    )
}
