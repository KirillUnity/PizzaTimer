package com.example.clockplannerproject.kit.core.practice

import com.example.clockplannerproject.data.InMemoryPracticeProgressStore
import com.example.clockplannerproject.kit.core.Task
import com.example.clockplannerproject.kit.core.TaskId
import com.example.clockplannerproject.kit.core.TimeBlock
import com.example.clockplannerproject.ui.navigation.AppDestinations
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeProgressTest {

    @Test
    fun threeFlags_readAsThreeOfFive_andRepeatStaysTried() = runTest {
        val store = InMemoryPracticeProgressStore()
        store.markTried(PracticeSkill.DIAL)
        store.markTried(PracticeSkill.START)
        store.markTried(PracticeSkill.CHECKPOINT)
        store.markTried(PracticeSkill.DIAL)

        val tried = store.observe().first()
        assertEquals(3, PracticeProgress.triedCount(tried))
        assertEquals("3 из 5", PracticeProgress.triedOfFive(PracticeProgress.triedCount(tried)))
        assertEquals(PracticeProgress.SKILL_TOTAL, PracticeSkill.entries.size)
    }

    @Test
    fun closedSixtyFiveMinutes_landOnThatDay_openBlockDoesNot() {
        val today = LocalDate(2026, 11, 19)
        val wednesday = LocalDate(2026, 11, 18)
        val task = Task(
            id = TaskId("deep"),
            title = "Deep work",
            colorArgb = 0xFF2E7D52,
            blocks = listOf(
                TimeBlock(10 * 60, 10 * 60 + 65),
                TimeBlock(12 * 60, null),
            ),
            date = wednesday,
        )
        val elsewhere = task.copy(
            id = TaskId("other-week"),
            date = LocalDate(2026, 11, 9),
            blocks = listOf(TimeBlock(9 * 60, 10 * 60)),
        )

        val columns = WeekClosedMinutes.columns(today, listOf(task, elsewhere))

        assertEquals(7, columns.size)
        assertEquals(LocalDate(2026, 11, 16), columns.first().date)
        assertEquals(LocalDate(2026, 11, 22), columns.last().date)
        assertTrue(columns.any { it.date == today })
        assertEquals(65, columns.first { it.date == wednesday }.closedMinutes)
        assertEquals(0, columns.first { it.date == today }.closedMinutes)
        assertEquals(0, WeekClosedMinutes.closedMinutes(TimeBlock(12 * 60, null)))
    }

    @Test
    fun topLevelDestinations_stayFour() {
        assertEquals(4, AppDestinations.entries.size)
        assertFalse(AppDestinations.entries.any { it.name == "PRACTICE" })
    }
}
