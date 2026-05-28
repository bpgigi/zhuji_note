package com.zhuji.note.domain.util

object NoteStatistics {
    fun readingTimeMinutes(wordCount: Int, wpm: Int = 200): Int =
        (wordCount / wpm.coerceAtLeast(1)).coerceAtLeast(1)

    fun sentenceCount(text: String): Int =
        text.split(Regex("[.!?。！？]+")).count { it.isNotBlank() }

    fun paragraphCount(text: String): Int =
        text.split(Regex("\n{2,}")).count { it.isNotBlank() }

    fun headingCount(text: String): Int =
        text.lines().count { it.trimStart().startsWith("#") }

    fun checkboxStats(text: String): Pair<Int, Int> {
        val total = Regex("""-\s*\[[ xX]]""").findAll(text).count()
        val checked = Regex("""-\s*\[[xX]]""").findAll(text).count()
        return checked to total
    }

    fun linkCount(text: String): Int {
        val mdLinks = Regex("""\[([^\]]+)]\(([^)]+)\)""").findAll(text).count()
        val stripped = text.replace(Regex("""\[([^\]]+)]\(([^)]+)\)"""), "")
        val bareUrls = Regex("""https?://\S+""").findAll(stripped).count()
        return mdLinks + bareUrls
    }

    fun codeBlockCount(text: String): Int =
        Regex("```").findAll(text).count() / 2
}
