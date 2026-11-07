package com.example.clockplannerproject.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FilesDirReportStoreTest {

    @Test
    fun import_writesUnderReportMedia_andDeleteRemovesIt() {
        val root = File("build/filesdir-report-store").apply {
            deleteRecursively()
            mkdirs()
        }
        val store = FilesDirReportStore(root)
        val attachment = store.import(
            input = "hello".byteInputStream(),
            reportId = "r1",
            displayName = "note.txt",
            mimeType = "text/plain",
        )
        val file = File(root, attachment.relativePath)
        assertTrue(file.exists())
        assertEquals("hello", file.readText())
        assertTrue(attachment.relativePath.startsWith("report-media/"))
        store.delete(attachment.relativePath)
        assertFalse(file.exists())
    }
}
