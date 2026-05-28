package com.zhuji.note.domain.util

object BiLinkParser {
    private val pattern = Regex("""\[\[(.+?)]]""")

    fun extractLinks(content: String): List<String> =
        pattern.findAll(content).map { it.groupValues[1].trim() }.distinct().toList()

    fun replaceLinks(content: String, resolver: (String) -> Long?): String =
        pattern.replace(content) { match ->
            val title = match.groupValues[1].trim()
            val id = resolver(title)
            if (id != null) "[$title](#note/$id)" else match.value
        }

    fun insertLink(content: String, cursorPos: Int, title: String): Pair<String, Int> {
        val link = "[[$title]]"
        val before = content.substring(0, cursorPos.coerceIn(0, content.length))
        val after = content.substring(cursorPos.coerceIn(0, content.length))
        return (before + link + after) to (cursorPos + link.length)
    }
}
