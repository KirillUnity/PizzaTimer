package com.example.clockplannerproject.kit.core

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FlowMemoryCodecTest {

    @Test
    fun checkpointRoundTrip_keepsSeparatorInText() {
        val note = PauseCheckpoint(
            text = "line\nwith \u001f and \\ slash",
            mood = PauseMood.Stuck,
        )
        val raw = FlowMemoryCodec.encodeCheckpoint(TaskId("work"), note)
        assertEquals(TaskId("work") to note, FlowMemoryCodec.decodeCheckpoint(raw))
    }

    @Test
    fun corruptEntry_isSkipped() {
        assertNull(FlowMemoryCodec.decodeCheckpoint("not-a-note"))
        assertNull(FlowMemoryCodec.decodeCheckpoint("id\u001fNope\u001ftext"))
        assertNull(FlowMemoryCodec.decodeDate("yesterday"))
        assertEquals(LocalDate(2026, 10, 6), FlowMemoryCodec.decodeDate("2026-10-06"))
    }
}
