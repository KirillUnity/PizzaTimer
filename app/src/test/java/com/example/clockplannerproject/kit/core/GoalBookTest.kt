package com.example.clockplannerproject.kit.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class GoalBookTest {
    @Test
    fun oneMain_andListStaysOffTheOpenDial() {
        val book = GoalBook().withMain("Первая").withMain("Вторая")
        assertEquals("Вторая", book.mainTitle)
        assertFalse(GoalBook.showOnDial(sessionOpen = true))
    }

    @Test
    fun unlinkedTask_andInheritedNext() {
        val book = GoalBook().addIntermediate("Глава", id = "g1")
        val unlinked = book.link("task", null)
        assertNull(unlinked.taskLinks["task"])
        val linked = book.link("task", "g1")
        val inherited = linked.inherit("task", "next")
        assertEquals("g1", inherited.taskLinks["next"])
        assertEquals("g1", inherited.taskLinks["task"])
    }
}
