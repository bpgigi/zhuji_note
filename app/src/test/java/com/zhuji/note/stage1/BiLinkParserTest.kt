package com.zhuji.note.stage1

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.domain.util.BiLinkParser
import org.junit.Test

class BiLinkParserTest {
    @Test fun `extract single link`() {
        assertThat(BiLinkParser.extractLinks("see [[My Note]] for details")).containsExactly("My Note")
    }
    @Test fun `extract multiple links`() {
        assertThat(BiLinkParser.extractLinks("[[A]] and [[B]] and [[A]]")).containsExactly("A", "B")
    }
    @Test fun `no links returns empty`() {
        assertThat(BiLinkParser.extractLinks("no links here")).isEmpty()
    }
    @Test fun `replace links with resolver`() {
        val result = BiLinkParser.replaceLinks("see [[Note1]] ok") { if (it == "Note1") 42L else null }
        assertThat(result).isEqualTo("see [Note1](#note/42) ok")
    }
    @Test fun `replace unresolved link stays`() {
        val result = BiLinkParser.replaceLinks("see [[Unknown]]") { null }
        assertThat(result).isEqualTo("see [[Unknown]]")
    }
    @Test fun `insert link at position`() {
        val (text, cursor) = BiLinkParser.insertLink("hello world", 5, "Ref")
        assertThat(text).isEqualTo("hello[[Ref]] world")
        assertThat(cursor).isEqualTo(12)
    }
}
