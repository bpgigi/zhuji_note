package com.zhuji.note.stage1

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.domain.util.ColorPalette
import com.zhuji.note.domain.util.ExportFormatter
import com.zhuji.note.domain.util.MarkdownExtractor
import com.zhuji.note.domain.util.NoteFormatter
import com.zhuji.note.domain.util.NoteSearch
import com.zhuji.note.domain.util.PomodoroTimer
import org.junit.Test

class FormatterTest {

    @Test fun `timestampLabel just now`() {
        val now = 1_700_000_000_000L
        assertThat(NoteFormatter.timestampLabel(now - 1, now)).isEqualTo("刚刚")
    }

    @Test fun `timestampLabel minutes`() {
        val now = 1_700_000_000_000L
        assertThat(NoteFormatter.timestampLabel(now - 5 * 60_000, now)).isEqualTo("5 分钟前")
    }

    @Test fun `timestampLabel hours`() {
        val now = 1_700_000_000_000L
        assertThat(NoteFormatter.timestampLabel(now - 3 * 3_600_000, now)).isEqualTo("3 小时前")
    }

    @Test fun `timestampLabel days`() {
        val now = 1_700_000_000_000L
        assertThat(NoteFormatter.timestampLabel(now - 4L * 86_400_000, now)).isEqualTo("4 天前")
    }

    @Test fun `timestampLabel weeks`() {
        val now = 1_700_000_000_000L
        assertThat(NoteFormatter.timestampLabel(now - 14L * 86_400_000, now)).isEqualTo("2 周前")
    }

    @Test fun `timestampLabel future falls back to date`() {
        val now = 1_700_000_000_000L
        val r = NoteFormatter.timestampLabel(now + 3_600_000, now)
        assertThat(r).contains(":")
    }

    @Test fun `shortPreview empty stays empty`() {
        assertThat(NoteFormatter.shortPreview("")).isEmpty()
    }

    @Test fun `shortPreview truncates with ellipsis`() {
        val long = "a".repeat(120)
        val r = NoteFormatter.shortPreview(long, maxChars = 30)
        assertThat(r.length).isAtMost(30)
        assertThat(r.endsWith("…")).isTrue()
    }

    @Test fun `shortPreview collapses whitespace`() {
        assertThat(NoteFormatter.shortPreview("a   \n  b\n c")).isEqualTo("a b c")
    }

    @Test fun `titleFromContent uses first non-blank line`() {
        val r = NoteFormatter.titleFromContent("\n\n# 标题\n正文")
        assertThat(r).isEqualTo("标题")
    }

    @Test fun `titleFromContent caps length`() {
        val r = NoteFormatter.titleFromContent("一二三四五六七八九十一二三四五六七八九十", maxChars = 8)
        assertThat(r.length).isAtMost(8)
    }

    @Test fun `wordCount rating buckets`() {
        assertThat(NoteFormatter.ratingForWordCount(10)).isEqualTo("草稿")
        assertThat(NoteFormatter.ratingForWordCount(100)).isEqualTo("短文")
        assertThat(NoteFormatter.ratingForWordCount(300)).isEqualTo("中篇")
        assertThat(NoteFormatter.ratingForWordCount(800)).isEqualTo("长文")
        assertThat(NoteFormatter.ratingForWordCount(2000)).isEqualTo("巨作")
    }
}

class MarkdownExtractorTest {
    @Test fun `extract chinese tags`() {
        val tags = MarkdownExtractor.extractTags("讨论 #工作 #学习 #想法 还有 #工作 重复")
        assertThat(tags).containsExactly("工作", "学习", "想法").inOrder()
    }

    @Test fun `extract markdown links`() {
        val links = MarkdownExtractor.extractLinks("see [docs](https://x.com) and [home](/)")
        assertThat(links).hasSize(2)
        assertThat(links.first()).isEqualTo("docs" to "https://x.com")
    }

    @Test fun `strip markdown removes formatting`() {
        val out = MarkdownExtractor.stripMarkdown("# Title\n**bold** _italic_ `code` [link](u)")
        assertThat(out).contains("Title")
        assertThat(out).contains("bold")
        assertThat(out).doesNotContain("**")
    }

    @Test fun `flashcards parses Q A pairs`() {
        val cards = MarkdownExtractor.toFlashcards("Q: what?\nA: this\nfoo\nQ: who?\nA: me")
        assertThat(cards).hasSize(2)
        assertThat(cards[0]).isEqualTo("what?" to "this")
        assertThat(cards[1]).isEqualTo("who?" to "me")
    }
}

class PomodoroTimerTest {
    @Test fun `next transition adds focus minutes`() {
        val now = 1_000L
        val n = PomodoroTimer.nextTransition(now, focusing = true)
        assertThat(n).isEqualTo(now + 25L * 60_000)
    }

    @Test fun `next transition adds break minutes`() {
        assertThat(PomodoroTimer.nextTransition(0L, focusing = false)).isEqualTo(5L * 60_000)
    }

    @Test fun `progress is zero before start`() {
        assertThat(PomodoroTimer.progress(0L, 1_000L, true)).isEqualTo(0f)
    }

    @Test fun `progress saturates at 1`() {
        val p = PomodoroTimer.progress(now = 100_000_000L, start = 0L, focusing = true)
        assertThat(p).isEqualTo(1f)
    }

    @Test fun `progress mid range`() {
        val span = 25L * 60_000
        val p = PomodoroTimer.progress(now = span / 2, start = 0L, focusing = true)
        assertThat(p).isWithin(0.01f).of(0.5f)
    }
}

class NoteSearchTest {
    @Test fun `empty query matches anything`() {
        assertThat(NoteSearch.match("hello", "")).isTrue()
    }

    @Test fun `multi-token AND match`() {
        assertThat(NoteSearch.match("Compose Hilt Room", "compose room")).isTrue()
        assertThat(NoteSearch.match("Compose Hilt", "compose dagger")).isFalse()
    }

    @Test fun `rank returns indices ordered by occurrence`() {
        val notes = listOf("a a a b", "a c", "a a c d")
        val r = NoteSearch.rank(notes, "a")
        assertThat(r).containsExactly(0, 2, 1).inOrder()
    }

    @Test fun `rank filters non matching`() {
        val r = NoteSearch.rank(listOf("apple", "banana"), "kiwi")
        assertThat(r).isEmpty()
    }
}

class ExportFormatterTest {
    @Test fun `markdown export contains title and tags`() {
        val s = ExportFormatter.toMarkdown("学习", "正文", listOf("生活", "想法"), 1_700_000_000_000L)
        assertThat(s).contains("# 学习")
        assertThat(s).contains("生活")
    }

    @Test fun `plain text falls back to default title`() {
        val s = ExportFormatter.toPlainText("", "内容")
        assertThat(s).contains("未命名")
    }

    @Test fun `html escapes special chars`() {
        val s = ExportFormatter.toHtml("<a>", "&\"")
        assertThat(s).contains("&lt;a&gt;")
        assertThat(s).contains("&amp;")
    }
}

class ColorPaletteTest {
    @Test fun `palette has at least 6 colors`() {
        assertThat(ColorPalette.noteColors.size).isAtLeast(6)
    }

    @Test fun `pickByHash deterministic`() {
        assertThat(ColorPalette.pickByHash("hello")).isEqualTo(ColorPalette.pickByHash("hello"))
    }

    @Test fun `pickByHash blank uses first`() {
        assertThat(ColorPalette.pickByHash("")).isEqualTo(ColorPalette.noteColors.first())
    }

    @Test fun `pickByHash distributes`() {
        val all = ('a'..'z').map { ColorPalette.pickByHash(it.toString()) }.toSet()
        assertThat(all.size).isAtLeast(2)
    }
}
