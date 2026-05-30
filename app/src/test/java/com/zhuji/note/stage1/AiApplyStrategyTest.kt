package com.zhuji.note.stage1

import com.zhuji.note.ai.AiAction
import com.zhuji.note.ai.AiApplyStrategy
import org.junit.Assert.assertEquals
import org.junit.Test

class AiApplyStrategyTest {

    private val title = "原标题"
    private val content = "原正文内容"
    private val answer = "AI 输出结果"

    @Test
    fun autoTitle_replaces_title_keeps_content() {
        val r = AiApplyStrategy.merge(AiAction.AutoTitle, title, content, answer)
        assertEquals(answer, r.title)
        assertEquals(content, r.content)
    }

    @Test
    fun autoTitle_takes_first_line_only() {
        val r = AiApplyStrategy.merge(AiAction.AutoTitle, title, content, "第一行标题\n多余的第二行")
        assertEquals("第一行标题", r.title)
    }

    @Test
    fun polish_replaces_content_keeps_title() {
        val r = AiApplyStrategy.merge(AiAction.Polish, title, content, answer)
        assertEquals(title, r.title)
        assertEquals(answer, r.content)
    }

    @Test
    fun translate_replaces_content_keeps_title() {
        val r = AiApplyStrategy.merge(AiAction.Translate, title, content, answer)
        assertEquals(title, r.title)
        assertEquals(answer, r.content)
    }

    @Test
    fun summarize_appends_to_content() {
        val r = AiApplyStrategy.merge(AiAction.Summarize, title, content, answer)
        assertEquals(title, r.title)
        assertEquals("$content\n\n$answer", r.content)
    }

    @Test
    fun continueWriting_appends_to_content() {
        val r = AiApplyStrategy.merge(AiAction.Continue, title, content, answer)
        assertEquals("$content\n\n$answer", r.content)
    }

    @Test
    fun append_into_blank_content_has_no_leading_newlines() {
        val r = AiApplyStrategy.merge(AiAction.Summarize, title, "", answer)
        assertEquals(answer, r.content)
    }

    @Test
    fun blank_answer_is_noop() {
        val r = AiApplyStrategy.merge(AiAction.Polish, title, content, "   ")
        assertEquals(title, r.title)
        assertEquals(content, r.content)
    }

    @Test
    fun answer_is_trimmed() {
        val r = AiApplyStrategy.merge(AiAction.Translate, title, content, "  译文  ")
        assertEquals("译文", r.content)
    }

    @Test
    fun null_action_appends() {
        val r = AiApplyStrategy.merge(null, title, content, answer)
        assertEquals("$content\n\n$answer", r.content)
    }
}
