package com.zhuji.note.stage2

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.domain.model.WritingGoal
import org.junit.Test

class WritingGoalTest {
    @Test fun `default target is 500`() {
        assertThat(WritingGoal().dailyWordTarget).isEqualTo(500)
    }
    @Test fun `progress clamped to 1`() {
        val g = WritingGoal(dailyWordTarget = 100, todayWords = 999)
        assertThat(g.todayProgress).isEqualTo(1f)
    }
    @Test fun `zero target does not crash`() {
        val g = WritingGoal(dailyWordTarget = 0, todayWords = 0)
        assertThat(g.todayProgress).isAtLeast(0f)
        assertThat(g.todayProgress).isAtMost(1f)
    }
    @Test fun `streak tracking`() {
        val g = WritingGoal(currentStreak = 7, longestStreak = 14)
        assertThat(g.currentStreak).isEqualTo(7)
        assertThat(g.longestStreak).isEqualTo(14)
    }
    @Test fun `weekly words list size`() {
        assertThat(WritingGoal().weeklyWords).hasSize(7)
    }
}
