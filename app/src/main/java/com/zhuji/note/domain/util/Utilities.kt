package com.zhuji.note.domain.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NoteFormatter {
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val dateTimeFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)

    fun timestampLabel(ts: Long, now: Long = System.currentTimeMillis()): String {
        val diff = now - ts
        return when {
            diff < 0 -> dateTimeFmt.format(Date(ts))
            diff < 60_000 -> "刚刚"
            diff < 3_600_000 -> "${diff / 60_000} 分钟前"
            diff < 86_400_000 -> "${diff / 3_600_000} 小时前"
            diff < 7L * 86_400_000 -> "${diff / 86_400_000} 天前"
            diff < 30L * 86_400_000 -> "${diff / (7L * 86_400_000)} 周前"
            else -> dateFmt.format(Date(ts))
        }
    }

    fun shortPreview(content: String, maxChars: Int = 80): String {
        if (content.isBlank()) return ""
        val cleaned = content.replace(Regex("\\s+"), " ").trim()
        return if (cleaned.length <= maxChars) cleaned else cleaned.take(maxChars - 1) + "…"
    }

    fun titleFromContent(content: String, maxChars: Int = 14): String {
        val firstLine = content.lineSequence().firstOrNull { it.isNotBlank() } ?: ""
        val stripped = firstLine.trimStart('#', ' ', '*', '-', '>').trim()
        return if (stripped.length <= maxChars) stripped else stripped.take(maxChars).trim()
    }

    fun ratingForWordCount(words: Int): String = when {
        words < 50 -> "草稿"
        words < 200 -> "短文"
        words < 600 -> "中篇"
        words < 1500 -> "长文"
        else -> "巨作"
    }
}

object MarkdownExtractor {
    private val tagRegex = Regex("(?<![\\w/#])#([\\w\\u4e00-\\u9fa5]+)")
    private val linkRegex = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)")

    fun extractTags(text: String): List<String> = tagRegex.findAll(text).map { it.groupValues[1] }.distinct().toList()
    fun extractLinks(text: String): List<Pair<String, String>> =
        linkRegex.findAll(text).map { it.groupValues[1] to it.groupValues[2] }.toList()

    fun stripMarkdown(text: String): String = text
        .replace(Regex("`{1,3}[^`]*`{1,3}"), "")
        .replace(Regex("[*_]{1,2}([^*_]+)[*_]{1,2}"), "$1")
        .replace(linkRegex, "$1")
        .replace(Regex("^#+\\s*", RegexOption.MULTILINE), "")
        .trim()

    fun toFlashcards(text: String): List<Pair<String, String>> {
        val out = mutableListOf<Pair<String, String>>()
        val lines = text.lines()
        var i = 0
        while (i < lines.size - 1) {
            val q = lines[i].trim()
            val a = lines[i + 1].trim()
            if (q.startsWith("Q:") && a.startsWith("A:")) {
                out += q.removePrefix("Q:").trim() to a.removePrefix("A:").trim()
                i += 2
            } else i += 1
        }
        return out
    }
}

object PomodoroTimer {
    const val FOCUS_MINUTES = 25L
    const val BREAK_MINUTES = 5L

    fun nextTransition(currentEpochMs: Long, focusing: Boolean): Long {
        val span = if (focusing) FOCUS_MINUTES else BREAK_MINUTES
        return currentEpochMs + span * 60_000
    }

    fun progress(now: Long, start: Long, focusing: Boolean): Float {
        val span = if (focusing) FOCUS_MINUTES * 60_000 else BREAK_MINUTES * 60_000
        val elapsed = now - start
        if (elapsed <= 0) return 0f
        if (elapsed >= span) return 1f
        return elapsed / span.toFloat()
    }
}

object NoteSearch {
    fun match(note: String, query: String): Boolean {
        if (query.isBlank()) return true
        val parts = query.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        val haystack = note.lowercase()
        return parts.all { haystack.contains(it) }
    }

    fun rank(notes: List<String>, query: String): List<Int> {
        if (query.isBlank()) return notes.indices.toList()
        val q = query.lowercase()
        return notes.mapIndexed { idx, n ->
            val score = countOccurrences(n.lowercase(), q)
            idx to score
        }.filter { it.second > 0 }.sortedByDescending { it.second }.map { it.first }
    }

    private fun countOccurrences(haystack: String, needle: String): Int {
        if (needle.isEmpty()) return 0
        var i = 0; var c = 0
        while (true) {
            val k = haystack.indexOf(needle, i)
            if (k < 0) return c
            c += 1; i = k + needle.length
        }
    }
}

object ExportFormatter {
    fun toMarkdown(title: String, content: String, tags: List<String>, ts: Long): String = buildString {
        append("# ${title.ifBlank { "未命名" }}\n\n")
        if (tags.isNotEmpty()) append("> 标签：${tags.joinToString("、")}\n\n")
        append("> 时间：${NoteFormatter.timestampLabel(ts)}\n\n")
        append(content)
    }

    fun toPlainText(title: String, content: String): String = "${title.ifBlank { "未命名" }}\n\n$content"

    fun toHtml(title: String, content: String): String = buildString {
        append("<!doctype html><meta charset=utf-8><title>")
        append(escapeHtml(title))
        append("</title><body><h1>")
        append(escapeHtml(title.ifBlank { "未命名" }))
        append("</h1><pre>")
        append(escapeHtml(content))
        append("</pre></body>")
    }

    private fun escapeHtml(s: String) = s
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}

object ColorPalette {
    val noteColors = intArrayOf(
        0xFFCC785C.toInt(), 0xFFD97757.toInt(), 0xFF5DB8A6.toInt(),
        0xFF6A9BBC.toInt(), 0xFFFB9CE5.toInt(), 0xFF34E8BB.toInt(),
        0xFFA95AF8.toInt(),
    )

    fun pickByHash(seed: String): Int {
        if (seed.isBlank()) return noteColors.first()
        var h = 0
        seed.forEach { h = h * 31 + it.code }
        return noteColors[((h % noteColors.size) + noteColors.size) % noteColors.size]
    }
}
