package com.example.clockplannerproject.kit.core

import com.example.clockplannerproject.data.sample.SampleTasks
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisibleListTasksTest {

    private val day = SampleTasks.typicalDay(LocalDate(2026, 10, 6))

    @Test
    fun hideCompleted_filtersDoneWithoutChangingOrderOfRest() {
        val visible = visibleListTasks(day, hideCompleted = true)
        assertTrue(visible.none { it.status == TaskStatus.DONE })
        assertEquals(day.filter { it.status != TaskStatus.DONE }.map { it.id }, visible.map { it.id })
    }

    @Test
    fun showAll_keepsDone() {
        val visible = visibleListTasks(day, hideCompleted = false)
        assertEquals(day.size, visible.size)
        assertTrue(visible.any { it.status == TaskStatus.DONE })
    }
}
