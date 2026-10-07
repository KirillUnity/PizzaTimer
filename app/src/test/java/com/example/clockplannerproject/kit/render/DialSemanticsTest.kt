package com.example.clockplannerproject.kit.render

import com.example.clockplannerproject.data.sample.SampleTasks
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Test

class DialSemanticsTest {

    @Test
    fun overnightTask_labelKeepsCivilRange() {
        val sleep = SampleTasks.typicalDay(LocalDate(2026, 10, 6))
            .first { it.id.value == "sleep" }
        val label = dialTaskTalkBackLabel(sleep)
        assertTrue(label.contains("Sleep"))
        assertTrue(label.contains("22:00"))
        assertTrue(label.contains("06:00"))
    }
}
