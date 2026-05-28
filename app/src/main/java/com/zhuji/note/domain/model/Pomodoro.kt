package com.zhuji.note.domain.model

data class PomodoroState(
    val isRunning: Boolean = false,
    val remainingMs: Long = 25 * 60 * 1000L,
    val totalMs: Long = 25 * 60 * 1000L,
    val sessionsCompleted: Int = 0,
    val isBreak: Boolean = false,
) {
    val progress: Float get() = 1f - (remainingMs.toFloat() / totalMs.toFloat())
    val minutes: Int get() = (remainingMs / 60_000).toInt()
    val seconds: Int get() = ((remainingMs % 60_000) / 1000).toInt()
    val display: String get() = "%02d:%02d".format(minutes, seconds)
}

enum class PomodoroPreset(val label: String, val workMin: Int, val breakMin: Int) {
    Classic("经典 25/5", 25, 5),
    Short("短冲 15/3", 15, 3),
    Long("深度 50/10", 50, 10),
}
