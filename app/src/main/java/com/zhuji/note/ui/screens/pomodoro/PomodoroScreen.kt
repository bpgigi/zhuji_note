package com.zhuji.note.ui.screens.pomodoro

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zhuji.note.domain.model.PomodoroPreset
import com.zhuji.note.domain.model.PomodoroState
import com.zhuji.note.ui.theme.Spacing
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(onBack: () -> Unit) {
    var preset by remember { mutableStateOf(PomodoroPreset.Classic) }
    var state by remember { mutableStateOf(PomodoroState(totalMs = preset.workMin * 60_000L, remainingMs = preset.workMin * 60_000L)) }
    var lastTick by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(state.isRunning) {
        if (!state.isRunning) return@LaunchedEffect
        lastTick = System.currentTimeMillis()
        while (state.isRunning && state.remainingMs > 0) {
            delay(200)
            val now = System.currentTimeMillis()
            val elapsed = now - lastTick
            lastTick = now
            state = state.copy(remainingMs = (state.remainingMs - elapsed).coerceAtLeast(0))
        }
        if (state.remainingMs <= 0) {
            val wasBreak = state.isBreak
            val sessions = if (!wasBreak) state.sessionsCompleted + 1 else state.sessionsCompleted
            val nextBreak = !wasBreak
            val nextMs = if (nextBreak) preset.breakMin * 60_000L else preset.workMin * 60_000L
            state = PomodoroState(isRunning = false, remainingMs = nextMs, totalMs = nextMs, sessionsCompleted = sessions, isBreak = nextBreak)
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(if (state.isBreak) "休息中" else "专注写作", style = MaterialTheme.typography.titleMedium) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) } },
        )
    }) { padding ->
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .fillMaxSize()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                PomodoroPreset.entries.forEach { p ->
                    AssistChip(
                        onClick = {
                            preset = p
                            state = PomodoroState(totalMs = p.workMin * 60_000L, remainingMs = p.workMin * 60_000L)
                        },
                        label = { Text(p.label) },
                        enabled = !state.isRunning,
                    )
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                val progress by animateFloatAsState(state.progress, tween(300, easing = LinearEasing), label = "arc")
                val primary = MaterialTheme.colorScheme.primary
                val track = MaterialTheme.colorScheme.surfaceVariant
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    val pad = 12.dp.toPx()
                    val arcSize = Size(size.width - pad * 2, size.height - pad * 2)
                    val topLeft = Offset(pad, pad)
                    drawArc(track, 0f, 360f, false, topLeft, arcSize, style = stroke)
                    drawArc(primary, -90f, 360f * progress, false, topLeft, arcSize, style = stroke)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.display, style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
                    Text("已完成 ${state.sessionsCompleted} 轮", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                FilledIconButton(onClick = { state = state.copy(isRunning = !state.isRunning) }, modifier = Modifier.size(64.dp)) {
                    Icon(if (state.isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, null, modifier = Modifier.size(32.dp))
                }
                FilledIconButton(onClick = {
                    state = PomodoroState(totalMs = preset.workMin * 60_000L, remainingMs = preset.workMin * 60_000L)
                }, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Outlined.Stop, null, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.height(Spacing.md))
            Text("专注时写下的每一个字都算入今日字数目标。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
