package com.zhuji.note.stage2

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.domain.model.PomodoroPreset
import com.zhuji.note.domain.model.PomodoroState
import org.junit.Test

class PomodoroStateTest {
    @Test fun `initial state not running`() {
        val s = PomodoroState()
        assertThat(s.isRunning).isFalse()
        assertThat(s.isBreak).isFalse()
        assertThat(s.sessionsCompleted).isEqualTo(0)
    }
    @Test fun `progress at start is 0`() {
        val s = PomodoroState(remainingMs = 25 * 60_000L, totalMs = 25 * 60_000L)
        assertThat(s.progress).isEqualTo(0f)
    }
    @Test fun `progress at end is 1`() {
        val s = PomodoroState(remainingMs = 0, totalMs = 25 * 60_000L)
        assertThat(s.progress).isEqualTo(1f)
    }
    @Test fun `display format correct`() {
        val s = PomodoroState(remainingMs = 3 * 60_000L + 7_000L, totalMs = 25 * 60_000L)
        assertThat(s.display).isEqualTo("03:07")
    }
    @Test fun `all presets have positive work and break`() {
        PomodoroPreset.entries.forEach {
            assertThat(it.workMin).isGreaterThan(0)
            assertThat(it.breakMin).isGreaterThan(0)
        }
    }
}
