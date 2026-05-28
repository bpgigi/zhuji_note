package com.zhuji.note.stage1

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.domain.model.BuiltInTemplates
import com.zhuji.note.domain.model.PomodoroPreset
import com.zhuji.note.domain.model.PomodoroState
import com.zhuji.note.domain.model.WritingGoal
import com.zhuji.note.domain.util.TemplateEngine
import org.junit.Test

class TemplateAndPomodoroTest {
    @Test fun `builtin templates has 8 entries`() {
        assertThat(BuiltInTemplates.all).hasSize(8)
    }
    @Test fun `each template has non-blank name and icon`() {
        BuiltInTemplates.all.forEach {
            assertThat(it.name).isNotEmpty()
            assertThat(it.icon).isNotEmpty()
            assertThat(it.id).isNotEmpty()
        }
    }
    @Test fun `template engine replaces date`() {
        val rendered = TemplateEngine.render("Today is {{date}}")
        assertThat(rendered).doesNotContain("{{date}}")
        assertThat(rendered).matches("Today is \\d{4}-\\d{2}-\\d{2}")
    }
    @Test fun `template engine replaces year month day`() {
        val rendered = TemplateEngine.render("{{year}}/{{month}}/{{day}}")
        assertThat(rendered).matches("\\d{4}/\\d{2}/\\d{2}")
    }
    @Test fun `pomodoro state display format`() {
        val s = PomodoroState(remainingMs = 5 * 60_000L + 30_000L, totalMs = 25 * 60_000L)
        assertThat(s.display).isEqualTo("05:30")
        assertThat(s.minutes).isEqualTo(5)
        assertThat(s.seconds).isEqualTo(30)
    }
    @Test fun `pomodoro progress calculation`() {
        val s = PomodoroState(remainingMs = 12_500L, totalMs = 25_000L)
        assertThat(s.progress).isWithin(0.01f).of(0.5f)
    }
    @Test fun `pomodoro presets have correct values`() {
        assertThat(PomodoroPreset.Classic.workMin).isEqualTo(25)
        assertThat(PomodoroPreset.Short.breakMin).isEqualTo(3)
        assertThat(PomodoroPreset.Long.workMin).isEqualTo(50)
    }
    @Test fun `writing goal progress`() {
        val g = WritingGoal(dailyWordTarget = 1000, todayWords = 500)
        assertThat(g.todayProgress).isWithin(0.01f).of(0.5f)
        assertThat(g.todayReached).isFalse()
    }
    @Test fun `writing goal reached`() {
        val g = WritingGoal(dailyWordTarget = 100, todayWords = 150)
        assertThat(g.todayReached).isTrue()
        assertThat(g.todayProgress).isEqualTo(1f)
    }
}
