package com.example.clockplannerproject.kit.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskTagsTest {

    @Test
    fun normalize_trimsDropsBlanksAndDedupesIgnoringCase() {
        val tags = TaskTags.normalize(listOf("  Focus  ", "", "focus", "  ", "дом"))
        assertEquals(listOf("Focus", "дом"), tags)
    }

    @Test
    fun normalize_capsCountAndLength() {
        val long = "я".repeat(TaskTags.MAX_LENGTH + 5)
        val many = listOf(long) + (1..TaskTags.MAX_COUNT + 3).map { index -> "tag$index" }
        val tags = TaskTags.normalize(many)
        assertEquals(TaskTags.MAX_COUNT, tags.size)
        assertEquals(TaskTags.MAX_LENGTH, tags.first().length)
        assertTrue(tags.none { it.length > TaskTags.MAX_LENGTH })
    }

    @Test
    fun encodeDecode_roundTrip() {
        val tags = listOf("чтение", "спорт")
        assertEquals(tags, TaskTags.decode(TaskTags.encode(tags)))
        assertEquals(emptyList<String>(), TaskTags.decode(null))
        assertEquals(emptyList<String>(), TaskTags.decode("   "))
    }
}