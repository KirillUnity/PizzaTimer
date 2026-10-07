package com.example.clockplannerproject.kit.core

import com.example.clockplannerproject.data.sample.SampleTasks
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DialHalfTest {

    private val date = LocalDate(2026, 10, 6)

    @Test
    fun workBlock_isAmOnly() {
        val work = SampleTasks.typicalDay(date).first { it.id.value == "work" }
        val am = clipTaskToHalf(work, DialHalf.AM)
        val pm = clipTaskToHalf(work, DialHalf.PM)
        assertEquals(1, am.size)
        assertEquals(9 * 60, am.single().startMinute)
        assertEquals(12 * 60, am.single().endMinute)
        assertTrue(pm.isEmpty())
    }

    @Test
    fun lunch_isPmOnly() {
        val lunch = SampleTasks.typicalDay(date).first { it.id.value == "lunch" }
        assertTrue(clipTaskToHalf(lunch, DialHalf.AM).isEmpty())
        val pm = clipTaskToHalf(lunch, DialHalf.PM)
        assertEquals(12 * 60, pm.single().startMinute)
        assertEquals(13 * 60, pm.single().endMinute)
    }

    @Test
    fun overnightSleep_splitsAcrossMidnight() {
        val sleep = SampleTasks.typicalDay(date).first { it.id.value == "sleep" }
        val am = clipTaskToHalf(sleep, DialHalf.AM)
        val pm = clipTaskToHalf(sleep, DialHalf.PM)
        assertEquals(1, am.size)
        assertEquals(0, am.single().startMinute)
        assertEquals(6 * 60, am.single().endMinute)
        assertEquals(1, pm.size)
        assertEquals(22 * 60, pm.single().startMinute)
        assertEquals(24 * 60, pm.single().endMinute)
    }

    @Test
    fun pmSliceEndingAt1440_doesNotRewrapOvernight() {
        val sleep = SampleTasks.typicalDay(date).first { it.id.value == "sleep" }
        val pmSlice = clipTaskToHalf(sleep, DialHalf.PM).single()
        val again = clipTaskToHalf(
            sleep.withRange(pmSlice.startMinute, pmSlice.endMinute),
            DialHalf.PM,
        )
        assertEquals(1, again.size)
        assertEquals(22 * 60, again.single().startMinute)
        assertEquals(24 * 60, again.single().endMinute)
    }

    @Test
    fun spanNoon_splitsAt720() {
        val task = SampleTasks.typicalDay(date).first { it.id.value == "work" }
            .withRange(10 * 60, 14 * 60)
        val am = clipTaskToHalf(task, DialHalf.AM).single()
        val pm = clipTaskToHalf(task, DialHalf.PM).single()
        assertEquals(10 * 60, am.startMinute)
        assertEquals(12 * 60, am.endMinute)
        assertEquals(12 * 60, pm.startMinute)
        assertEquals(14 * 60, pm.endMinute)
    }
}
