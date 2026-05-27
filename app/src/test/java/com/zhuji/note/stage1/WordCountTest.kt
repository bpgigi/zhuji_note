package com.zhuji.note.stage1

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.data.repository.wordCount
import org.junit.Test

class WordCountTest {
    @Test fun `blank text yields 0`() { assertThat(wordCount("")).isEqualTo(0); assertThat(wordCount("   ")).isEqualTo(0) }
    @Test fun `pure english tokens`() { assertThat(wordCount("hello world")).isEqualTo(2) }
    @Test fun `punctuation does not count`() { assertThat(wordCount("hi, world!")).isEqualTo(2) }
    @Test fun `chinese counted by character`() { assertThat(wordCount("你好世界")).isEqualTo(4) }
    @Test fun `mixed cn en`() { assertThat(wordCount("你好 world")).isEqualTo(3) }
    @Test fun `multiple separators`() { assertThat(wordCount("hello\n\nworld\tagain")).isEqualTo(3) }
    @Test fun `digits as tokens`() { assertThat(wordCount("3 plus 4 = 7")).isEqualTo(4) }
    @Test fun `long english passage`() { assertThat(wordCount("The quick brown fox jumps over the lazy dog")).isEqualTo(9) }
    @Test fun `mixed punctuation chinese`() { assertThat(wordCount("中文：测试，结果。")).isEqualTo(6) }
    @Test fun `repeated whitespace`() { assertThat(wordCount("  a   b   c  ")).isEqualTo(3) }
    @Test fun `tabs and newlines`() { assertThat(wordCount("a\tb\nc d")).isEqualTo(4) }
    @Test fun `chinese passage`() { assertThat(wordCount("助记是一款移动笔记应用")).isEqualTo(11) }
}
