package com.example.clockplannerproject.kit.core

import com.example.clockplannerproject.data.InMemoryCustomCourseStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomCourseStoreTest {

    @Test
    fun add_thenRefresh_keepsTitle() = runTest {
        val store = InMemoryCustomCourseStore()
        store.add("  Мой курс  ")
        store.add("   ")
        val rows = store.observe().first()
        assertEquals(1, rows.size)
        assertEquals("Мой курс", rows.single().title)
        assertTrue(rows.single().id.isNotBlank())
        val again = store.observe().first()
        assertEquals(rows, again)
    }
}
