package com.zhuji.note.domain.util

object MarkdownToolbar {
    fun applyHeading(text: String, selStart: Int, selEnd: Int, level: Int): Pair<String, IntRange> {
        val (lineStart, lineEnd) = lineBounds(text, selStart)
        val prefix = "#".repeat(level.coerceIn(1, 6)) + " "
        val current = text.substring(lineStart, lineEnd).trimStart('#', ' ')
        val replaced = prefix + current
        return text.replaceRange(lineStart, lineEnd, replaced) to (lineStart..lineStart + replaced.length)
    }

    fun applyBold(text: String, selStart: Int, selEnd: Int): Pair<String, IntRange> = wrap(text, selStart, selEnd, "**", "**")
    fun applyItalic(text: String, selStart: Int, selEnd: Int): Pair<String, IntRange> = wrap(text, selStart, selEnd, "_", "_")
    fun applyInlineCode(text: String, selStart: Int, selEnd: Int): Pair<String, IntRange> = wrap(text, selStart, selEnd, "`", "`")
    fun applyStrike(text: String, selStart: Int, selEnd: Int): Pair<String, IntRange> = wrap(text, selStart, selEnd, "~~", "~~")

    fun applyBulletList(text: String, selStart: Int, selEnd: Int): Pair<String, IntRange> = prefixLines(text, selStart, selEnd, "- ")
    fun applyNumberedList(text: String, selStart: Int, selEnd: Int): Pair<String, IntRange> {
        val (s, e) = blockBounds(text, selStart, selEnd)
        val lines = text.substring(s, e).split("\n")
        val out = lines.mapIndexed { idx, line ->
            if (line.isBlank()) line else "${idx + 1}. ${line.trimStart('-', ' ')}"
        }.joinToString("\n")
        return text.replaceRange(s, e, out) to (s..s + out.length)
    }
    fun applyChecklist(text: String, selStart: Int, selEnd: Int): Pair<String, IntRange> = prefixLines(text, selStart, selEnd, "- [ ] ")
    fun applyQuote(text: String, selStart: Int, selEnd: Int): Pair<String, IntRange> = prefixLines(text, selStart, selEnd, "> ")

    fun applyCodeBlock(text: String, selStart: Int, selEnd: Int): Pair<String, IntRange> {
        val (s, e) = blockBounds(text, selStart, selEnd)
        val body = text.substring(s, e)
        val replaced = "```\n${body.ifBlank { "" }}\n```"
        return text.replaceRange(s, e, replaced) to (s..s + replaced.length)
    }

    fun applyLink(text: String, selStart: Int, selEnd: Int, url: String = "https://"): Pair<String, IntRange> {
        val sel = text.substring(selStart.coerceAtMost(selEnd), selStart.coerceAtLeast(selEnd))
        val replaced = "[${sel.ifBlank { "标签" }}]($url)"
        val a = minOf(selStart, selEnd); val b = maxOf(selStart, selEnd)
        return text.replaceRange(a, b, replaced) to (a..a + replaced.length)
    }

    fun toggleCheckbox(text: String, lineOffset: Int): Pair<String, IntRange> {
        val (s, e) = lineBounds(text, lineOffset)
        val line = text.substring(s, e)
        val replaced = when {
            "[ ]" in line -> line.replaceFirst("[ ]", "[x]")
            "[x]" in line -> line.replaceFirst("[x]", "[ ]")
            "[X]" in line -> line.replaceFirst("[X]", "[ ]")
            else -> line
        }
        return text.replaceRange(s, e, replaced) to (s..s + replaced.length)
    }

    private fun wrap(text: String, a: Int, b: Int, l: String, r: String): Pair<String, IntRange> {
        val s = minOf(a, b); val e = maxOf(a, b)
        val sub = text.substring(s, e)
        val replaced = "$l${sub.ifBlank { "文本" }}$r"
        return text.replaceRange(s, e, replaced) to (s..s + replaced.length)
    }

    private fun prefixLines(text: String, a: Int, b: Int, p: String): Pair<String, IntRange> {
        val (s, e) = blockBounds(text, a, b)
        val out = text.substring(s, e).split("\n").joinToString("\n") { line ->
            if (line.isBlank()) line else "$p${line.trimStart('-', ' ')}"
        }
        return text.replaceRange(s, e, out) to (s..s + out.length)
    }

    private fun lineBounds(text: String, offset: Int): Pair<Int, Int> {
        if (text.isEmpty()) return 0 to 0
        val o = offset.coerceIn(0, text.length)
        val start = text.lastIndexOf('\n', (o - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        val end = text.indexOf('\n', o).let { if (it < 0) text.length else it }
        return start to end
    }

    private fun blockBounds(text: String, a: Int, b: Int): Pair<Int, Int> {
        val s = minOf(a, b); val e = maxOf(a, b)
        val (ls, _) = lineBounds(text, s)
        val (_, le) = lineBounds(text, (e - 1).coerceAtLeast(s))
        return ls to le
    }
}
