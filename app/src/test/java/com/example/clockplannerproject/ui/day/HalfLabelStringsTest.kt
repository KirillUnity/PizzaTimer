package com.example.clockplannerproject.ui.day

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Locks visible half labels to AM/PM and keeps spoken names as before noon / after noon.
 */
class HalfLabelStringsTest {

    @Test
    fun halfLabels_useAmPmAbbreviations() {
        val (english, russian) = catalogs()
        assertEquals("AM", value(english, "dial_am"))
        assertEquals("PM", value(english, "dial_pm"))
        assertEquals("Before noon", value(english, "cd_half_am"))
        assertEquals("After noon", value(english, "cd_half_pm"))
        assertEquals("AM", value(russian, "dial_am"))
        assertEquals("PM", value(russian, "dial_pm"))
        assertEquals("до полудня", value(russian, "cd_half_am"))
        assertEquals("после полудня", value(russian, "cd_half_pm"))
    }

    @Test
    fun headerPlacesMenuThenHalves() {
        val source = dayScreenSource()
        val row = source.substringAfter("horizontalArrangement = Arrangement.SpaceBetween")
            .substringBefore("if (!sessionRunning)")
        val menuAt = row.indexOf("Icons.Filled.Menu")
        val halvesAt = row.indexOf("AmPmRow(")
        assertTrue("menu button must be composed before AmPmRow", menuAt in 0 until halvesAt)
    }

    private fun value(xml: String, name: String): String {
        val match = Regex("""<string name="$name">([^<]*)</string>""").find(xml)
            ?: error("missing string $name")
        val text = match.groupValues[1] ?: ""
        return text
    }

    private fun catalogs(): Pair<String, String> {
        var dir: File? = File(System.getProperty("user.dir"))
        repeat(6) {
            val current = dir ?: return@repeat
            val english = listOf(
                File(current, "app/src/main/res/values/strings.xml"),
                File(current, "src/main/res/values/strings.xml"),
            ).firstOrNull { it.isFile }
            if (english != null) {
                val resDir = english.parentFile?.parentFile
                    ?: error("string catalog has no res directory")
                val russian = File(resDir, "values-ru/strings.xml")
                return english.readText() to russian.readText()
            }
            dir = current.parentFile
        }
        error("strings.xml not found from ${System.getProperty("user.dir")}")
    }

    private fun dayScreenSource(): String {
        var dir: File? = File(System.getProperty("user.dir"))
        repeat(6) {
            val current = dir ?: return@repeat
            val file = listOf(
                File(current, "app/src/main/java/com/example/clockplannerproject/ui/day/DayScreen.kt"),
                File(current, "src/main/java/com/example/clockplannerproject/ui/day/DayScreen.kt"),
            ).firstOrNull { it.isFile }
            if (file != null) return file.readText()
            dir = current.parentFile
        }
        error("DayScreen.kt not found from ${System.getProperty("user.dir")}")
    }
}
