package com.example.clockplannerproject.ui.day

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Locks the day-strip words for today, yesterday, and tomorrow.
 */
class RelativeDayStringsTest {

    @Test
    fun relativeDayWords_matchBothCatalogs() {
        val (english, russian) = catalogs()
        assertEquals("Today", value(english, "day_relative_today"))
        assertEquals("Yesterday", value(english, "day_relative_yesterday"))
        assertEquals("Tomorrow", value(english, "day_relative_tomorrow"))
        assertEquals("Сегодня", value(russian, "day_relative_today"))
        assertEquals("Вчера", value(russian, "day_relative_yesterday"))
        assertEquals("Завтра", value(russian, "day_relative_tomorrow"))
    }

    @Test
    fun dayStrip_hidesTheMediumDateLine() {
        val header = dayScreenSource()
            .substringAfter("PaperDateHeader(")
            .substringBefore("Row(")
        assertTrue(header.contains("showDate = false"))
    }

    private fun value(xml: String, name: String): String {
        val match = Regex("""<string name="$name">([^<]*)</string>""").find(xml)
            ?: error("missing string $name")
        return match.groupValues[1]
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
