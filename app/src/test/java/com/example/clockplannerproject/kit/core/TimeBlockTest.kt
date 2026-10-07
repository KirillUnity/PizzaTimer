package com.example.clockplannerproject.kit.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeBlockTest {

    @Test
    fun sameDay_closedAfterEnd() {
        val block = TimeBlock(8 * 60, 9 * 60)
        assertFalse(block.isClosed(8 * 60 + 30))
        assertTrue(block.isClosed(9 * 60))
        assertTrue(block.isClosed(10 * 60))
    }

    @Test
    fun openTimer_isNeverClosed() {
        val running = TimeBlock(12 * 60, endMinute = null)
        assertTrue(running.isOpen)
        assertFalse(running.isClosed(12 * 60))
        assertFalse(running.isClosed(13 * 60))
        assertEquals(13 * 60, running.resolvedEndMinute(13 * 60))
    }

    @Test
    fun overnight_staysOpenThroughNight() {
        val sleep = TimeBlock(22 * 60, 6 * 60)
        assertFalse(sleep.isClosed(23 * 60))
        assertFalse(sleep.isClosed(2 * 60))
        assertTrue(sleep.isClosed(6 * 60))
        assertTrue(sleep.isClosed(7 * 60))
        assertFalse(sleep.isClosed(22 * 60))
    }
}
