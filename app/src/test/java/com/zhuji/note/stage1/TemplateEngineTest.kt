package com.zhuji.note.stage1

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.domain.util.TemplateEngine
import org.junit.Test

class TemplateEngineTest {
    @Test fun `weekday is capitalized`() {
        val r = TemplateEngine.render("{{weekday}}")
        assertThat(r.first().isUpperCase()).isTrue()
    }
    @Test fun `no leftover placeholders`() {
        val r = TemplateEngine.render("{{date}} {{year}} {{month}} {{day}} {{weekday}}")
        assertThat(r).doesNotContain("{{")
    }
    @Test fun `plain text unchanged`() {
        assertThat(TemplateEngine.render("hello")).isEqualTo("hello")
    }
    @Test fun `empty string unchanged`() {
        assertThat(TemplateEngine.render("")).isEmpty()
    }
}
