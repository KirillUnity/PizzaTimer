package com.example.clockplannerproject.kit.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.example.clockplannerproject.kit.compose.PreviewDialTasks
import com.example.clockplannerproject.kit.core.DialHalf
import com.example.clockplannerproject.kit.core.ViewMode
import com.example.clockplannerproject.kit.core.config.TimeDialConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PizzaHitTestTest {

    private val canvas = Size(200f, 200f)
    private val config = TimeDialConfig.Default.copy(viewMode = ViewMode.PIZZA)

    @Test
    fun pizzaCenter_hitsTaskAtTwelve() {
        val work = PreviewDialTasks.first { it.id.value == "work" }
        val hit = hitTestTask(
            offset = Offset(100f, 100f),
            tasks = PreviewDialTasks,
            config = config,
            nowMinute = 9 * 60f,
            canvasSize = canvas,
            half = DialHalf.AM,
        )
        assertNotNull(hit)
        assertEquals(work.id, hit?.id)
    }
}
