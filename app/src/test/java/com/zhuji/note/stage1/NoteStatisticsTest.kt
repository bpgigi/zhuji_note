package com.zhuji.note.stage1

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.domain.util.NoteStatistics
import org.junit.Test

class NoteStatisticsTest {
    @Test fun `reading time 200 words = 1 min`() {
        assertThat(NoteStatistics.readingTimeMinutes(200)).isEqualTo(1)
    }
    @Test fun `reading time 600 words = 3 min`() {
        assertThat(NoteStatistics.readingTimeMinutes(600)).isEqualTo(3)
    }
    @Test fun `sentence count`() {
        assertThat(NoteStatistics.sentenceCount("Hello. World! Yes?")).isEqualTo(3)
    }
    @Test fun `sentence count chinese`() {
        assertThat(NoteStatistics.sentenceCount("你好。世界！是的？")).isEqualTo(3)
    }
    @Test fun `paragraph count`() {
        assertThat(NoteStatistics.paragraphCount("a\n\nb\n\nc")).isEqualTo(3)
    }
    @Test fun `heading count`() {
        assertThat(NoteStatistics.headingCount("# H1\n## H2\ntext\n### H3")).isEqualTo(3)
    }
    @Test fun `checkbox stats`() {
        val (checked, total) = NoteStatistics.checkboxStats("- [x] done\n- [ ] todo\n- [X] also done")
        assertThat(total).isEqualTo(3)
        assertThat(checked).isEqualTo(2)
    }
    @Test fun `link count`() {
        assertThat(NoteStatistics.linkCount("[a](http://x.com) and https://y.com")).isEqualTo(2)
    }
    @Test fun `code block count`() {
        assertThat(NoteStatistics.codeBlockCount("```\ncode\n```\n```\nmore\n```")).isEqualTo(2)
    }
    @Test fun `empty text`() {
        assertThat(NoteStatistics.sentenceCount("")).isEqualTo(0)
        assertThat(NoteStatistics.paragraphCount("")).isEqualTo(0)
        assertThat(NoteStatistics.headingCount("")).isEqualTo(0)
    }
}
