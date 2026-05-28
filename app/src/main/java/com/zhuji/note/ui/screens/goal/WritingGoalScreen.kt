package com.zhuji.note.ui.screens.goal

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
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zhuji.note.domain.model.WritingGoal
import com.zhuji.note.ui.theme.Spacing
import com.zhuji.note.ui.theme.ZhujiCornerTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingGoalScreen(onBack: () -> Unit, goal: WritingGoal = WritingGoal(dailyWordTarget = 500, todayWords = 230, currentStreak = 5, longestStreak = 12)) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("写作目标", style = MaterialTheme.typography.titleMedium) },
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
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                val progress by animateFloatAsState(goal.todayProgress, tween(800), label = "gp")
                val primary = MaterialTheme.colorScheme.primary
                val track = MaterialTheme.colorScheme.surfaceVariant
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    val pad = 14.dp.toPx()
                    val arcSize = Size(size.width - pad * 2, size.height - pad * 2)
                    val topLeft = Offset(pad, pad)
                    drawArc(track, 0f, 360f, false, topLeft, arcSize, style = stroke)
                    drawArc(primary, -90f, 360f * progress, false, topLeft, arcSize, style = stroke)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${goal.todayWords}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Text("/ ${goal.dailyWordTarget} 字", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Surface(shape = ZhujiCornerTokens.NoteCard, color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Row(Modifier.padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocalFireDepartment, null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.padding(start = Spacing.xs))
                        Column {
                            Text("${goal.currentStreak} 天", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("当前连续", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Surface(shape = ZhujiCornerTokens.NoteCard, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(Modifier.padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocalFireDepartment, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.padding(start = Spacing.xs))
                        Column {
                            Text("${goal.longestStreak} 天", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("最长连续", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            Surface(shape = ZhujiCornerTokens.NoteCard, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Spacing.lg)) {
                    Text("本周字数", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(Spacing.sm))
                    val days = listOf("一", "二", "三", "四", "五", "六", "日")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        goal.weeklyWords.forEachIndexed { i, w ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$w", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(days.getOrElse(i) { "" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
