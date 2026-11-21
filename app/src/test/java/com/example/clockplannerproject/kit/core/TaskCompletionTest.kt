package com.example.clockplannerproject.kit.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskCompletionTest {
    @Test
    fun roundTrip_keepsProxies_andOneMark() {
        val completion = TaskCompletion.orNull(
            stateAfter = 6,
            engagement = 4,
            skill = 8,
            easier = "короче",
            mark = DemandMark.CHALLENGE,
            thoughts = "",
            reframe = "не нужно",
            done = false,
            progress = "абзац",
        )
        val decoded = TaskCompletionCodec.decode(TaskCompletionCodec.encode(completion!!))
        assertEquals(completion, decoded)
        assertEquals(DemandMark.CHALLENGE, decoded?.mark)
    }

    @Test
    fun missingState_doesNotDecode() {
        assertNull(
            TaskCompletion.orNull(
                stateAfter = null,
                engagement = 5,
                skill = 5,
                easier = "",
                mark = DemandMark.HINDRANCE,
                thoughts = "",
                reframe = "",
                done = true,
                progress = "",
            ),
        )
        val body = """
            timedial-completion/v1
            engagement=5
            skill=5
            mark=HINDRANCE
            done=true
        """.trimIndent()
        assertNull(TaskCompletionCodec.decode(body))
    }
}
