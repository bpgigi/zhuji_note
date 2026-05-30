package com.zhuji.note.ai

object AiApplyStrategy {

    data class Result(val title: String, val content: String)

    fun merge(action: AiAction?, currentTitle: String, currentContent: String, answer: String): Result {
        val trimmed = answer.trim()
        if (trimmed.isBlank()) return Result(currentTitle, currentContent)
        return when (action) {
            AiAction.AutoTitle ->
                Result(trimmed.lineSequence().firstOrNull()?.trim().orEmpty(), currentContent)
            AiAction.Polish, AiAction.Translate ->
                Result(currentTitle, trimmed)
            else -> {
                val combined = if (currentContent.isBlank()) trimmed else "$currentContent\n\n$trimmed"
                Result(currentTitle, combined)
            }
        }
    }
}
