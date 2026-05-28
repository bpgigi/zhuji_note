package com.zhuji.note.stage1

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.domain.util.MarkdownToolbar
import org.junit.Test

class MarkdownToolbarExtendedTest {
    @Test fun `heading 1`() {
        val (t, _) = MarkdownToolbar.applyHeading("hello", 0, 5, 1)
        assertThat(t).startsWith("# ")
    }
    @Test fun `heading 2`() {
        val (t, _) = MarkdownToolbar.applyHeading("hello", 0, 5, 2)
        assertThat(t).startsWith("## ")
    }
    @Test fun `heading 3`() {
        val (t, _) = MarkdownToolbar.applyHeading("hello", 0, 5, 3)
        assertThat(t).startsWith("### ")
    }
    @Test fun `bold wraps`() {
        val (t, _) = MarkdownToolbar.applyBold("hello", 0, 5)
        assertThat(t).isEqualTo("**hello**")
    }
    @Test fun `italic wraps`() {
        val (t, _) = MarkdownToolbar.applyItalic("hello", 0, 5)
        assertThat(t).isEqualTo("_hello_")
    }
    @Test fun `inline code wraps`() {
        val (t, _) = MarkdownToolbar.applyInlineCode("hello", 0, 5)
        assertThat(t).isEqualTo("`hello`")
    }
    @Test fun `strike wraps`() {
        val (t, _) = MarkdownToolbar.applyStrike("hello", 0, 5)
        assertThat(t).isEqualTo("~~hello~~")
    }
    @Test fun `bullet list`() {
        val (t, _) = MarkdownToolbar.applyBulletList("hello", 0, 5)
        assertThat(t).contains("- ")
    }
    @Test fun `numbered list`() {
        val (t, _) = MarkdownToolbar.applyNumberedList("hello", 0, 5)
        assertThat(t).contains("1. ")
    }
    @Test fun `checklist`() {
        val (t, _) = MarkdownToolbar.applyChecklist("hello", 0, 5)
        assertThat(t).contains("- [ ] ")
    }
    @Test fun `quote`() {
        val (t, _) = MarkdownToolbar.applyQuote("hello", 0, 5)
        assertThat(t).contains("> ")
    }
    @Test fun `link`() {
        val (t, _) = MarkdownToolbar.applyLink("hello", 0, 5)
        assertThat(t).contains("[hello]")
        assertThat(t).contains("](https://")
    }
    @Test fun `code block`() {
        val (t, _) = MarkdownToolbar.applyCodeBlock("hello", 0, 5)
        assertThat(t).contains("```")
    }
    @Test fun `toggle checkbox unchecked to checked`() {
        val (t, _) = MarkdownToolbar.toggleCheckbox("- [ ] task", 3)
        assertThat(t).contains("- [x] task")
    }
    @Test fun `toggle checkbox checked to unchecked`() {
        val (t, _) = MarkdownToolbar.toggleCheckbox("- [x] task", 3)
        assertThat(t).contains("- [ ] task")
    }
    @Test fun `bold empty selection inserts placeholder`() {
        val (t, _) = MarkdownToolbar.applyBold("hello", 2, 2)
        assertThat(t).contains("**")
    }
    @Test fun `multiline bullet list`() {
        val (t, _) = MarkdownToolbar.applyBulletList("a\nb\nc", 0, 5)
        assertThat(t.lines().filter { it.startsWith("- ") }.size).isAtLeast(2)
    }
}
