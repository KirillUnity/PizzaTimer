package com.example.clockplannerproject.kit.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReportMediaPathsTest {

    @Test
    fun relativePath_stripsTraversal() {
        val relative = ReportMediaPaths.relativePath("../r1", "../../notes/photo.jpg")
        assertEquals("report-media/r1-photo.jpg", relative)
    }

    @Test
    fun resolve_rejectsEscape() {
        val root = File("build/report-media-test").apply { mkdirs() }
        val escaped = runCatching {
            ReportMediaPaths.resolve(root, "report-media/../../secret.txt")
        }
        assertTrue(escaped.isFailure)
    }
}
