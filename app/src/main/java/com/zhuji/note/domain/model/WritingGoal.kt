package com.zhuji.note.domain.model

data class WritingGoal(
    val dailyWordTarget: Int = 500,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val todayWords: Int = 0,
    val weeklyWords: List<Int> = List(7) { 0 },
) {
    val todayProgress: Float get() = if (dailyWordTarget <= 0) 0f else (todayWords.toFloat() / dailyWordTarget).coerceIn(0f, 1f)
    val todayReached: Boolean get() = todayWords >= dailyWordTarget
}
