package com.example.clockplannerproject.kit.compose

import com.example.clockplannerproject.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HoleTimerControlTest {

    @Test
    fun openWork_isPause_breakOrIdle_isPlay() {
        assertTrue(holeTimerShowsPause(workOpen = true))
        assertFalse(holeTimerShowsPause(workOpen = false))
    }

    @Test
    fun pause_showsBreakCaption() {
        assertEquals(R.string.break_title, holeSessionCaptionRes(paused = true))
        assertEquals(R.string.dial_this_session, holeSessionCaptionRes(paused = false))
    }
}
