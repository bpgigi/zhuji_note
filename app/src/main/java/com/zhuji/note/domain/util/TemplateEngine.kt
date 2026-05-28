package com.zhuji.note.domain.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object TemplateEngine {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun render(template: String): String {
        val today = LocalDate.now()
        return template
            .replace("{{date}}", today.format(dateFormatter))
            .replace("{{year}}", today.year.toString())
            .replace("{{month}}", today.monthValue.toString().padStart(2, '0'))
            .replace("{{day}}", today.dayOfMonth.toString().padStart(2, '0'))
            .replace("{{weekday}}", today.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() })
    }
}
