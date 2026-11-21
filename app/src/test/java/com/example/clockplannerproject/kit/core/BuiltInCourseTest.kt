package com.example.clockplannerproject.kit.core

import com.example.clockplannerproject.ui.navigation.AppDestinations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltInCourseTest {
    @Test
    fun catalog_isThreePractices_andNavigationStaysFour() {
        assertEquals(3, BuiltInCourse.lessons.size)
        assertEquals(
            listOf(
                BuiltInPractice.HEARTFULNESS,
                BuiltInPractice.YOGA_PAUSE,
                BuiltInPractice.MICRO_QIGONG,
            ),
            BuiltInCourse.lessons.map { it.practice },
        )
        BuiltInCourse.lessons.forEach { lesson ->
            assertTrue(lesson.stepIds.isNotEmpty())
            assertTrue(lesson.citation.isNotBlank())
        }
        assertEquals(4, AppDestinations.entries.size)
    }

    @Test
    fun secondPass_appends() {
        val once = CourseHistory.append(emptyList(), BuiltInPractice.HEARTFULNESS, 1L)
        val twice = CourseHistory.append(once, BuiltInPractice.HEARTFULNESS, 2L)
        assertEquals(2, twice.size)
        assertEquals(1L, twice[0].atEpochMillis)
        assertEquals(2L, twice[1].atEpochMillis)
        assertTrue(twice.all { it.practice == BuiltInPractice.HEARTFULNESS })
    }

    @Test
    fun openWorkInterval_hidesOffer() {
        assertFalse(BuiltInCourse.offer(workOpen = true, onBreak = true, needsConcentration = true))
        assertTrue(BuiltInCourse.offer(workOpen = false, onBreak = true, needsConcentration = false))
        assertTrue(BuiltInCourse.offer(workOpen = false, onBreak = false, needsConcentration = true))
        assertFalse(BuiltInCourse.offer(workOpen = false, onBreak = false, needsConcentration = false))
    }
}
