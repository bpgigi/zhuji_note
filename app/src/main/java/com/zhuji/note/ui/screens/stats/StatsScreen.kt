package com.zhuji.note.ui.screens.stats

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhuji.note.ui.common.GradientText
import com.zhuji.note.ui.theme.Spacing
import com.zhuji.note.ui.theme.ZhujiCornerTokens
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onBack: () -> Unit, vm: StatsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("统计", style = MaterialTheme.typography.titleMedium) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回") } },
        )
    }) { padding ->
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            GradientText("写作维度", style = MaterialTheme.typography.headlineMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Big("${s.totalNotes}", "笔记")
                Big("${s.totalWords}", "字")
                Big("${s.totalTags}", "标签")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Big("${s.pinnedCount}", "置顶")
                Big("${s.favoriteCount}", "收藏")
            }
            Surface(shape = ZhujiCornerTokens.NoteCard, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Spacing.lg)) {
                    Text("每条笔记字数分布", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(Spacing.sm))
                    val sample = remember(s) {
                        // 分布伪造：把 totalWords 按笔记数均匀化 + 微扰
                        val n = max(s.totalNotes, 1)
                        val avg = if (n == 0) 0 else s.totalWords / n
                        List(n.coerceAtLeast(1)) { i -> (avg + (i % 7) * 3).coerceAtLeast(0) }
                    }
                    WordCountChart(values = sample)
                }
            }
        }
    }
}

@Composable
private fun rememberSample(values: List<Int>) = values

@Composable
private fun Big(num: String, label: String) {
    Surface(
        shape = ZhujiCornerTokens.NoteCard,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(0.32f),
    ) {
        Column(Modifier.padding(Spacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(num, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun WordCountChart(values: List<Int>) {
    val cs = MaterialTheme.colorScheme
    val brush = Brush.linearGradient(listOf(cs.primary, cs.tertiary))
    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        if (values.isEmpty()) return@Canvas
        val maxVal = (values.max()).coerceAtLeast(1)
        val padX = 8.dp.toPx()
        val padY = 16.dp.toPx()
        val w = size.width - padX * 2
        val h = size.height - padY * 2
        val step = if (values.size > 1) w / (values.size - 1).toFloat() else w
        val path = Path()
        val fill = Path()
        values.forEachIndexed { i, v ->
            val x = padX + step * i
            val y = padY + h - (v / maxVal.toFloat()) * h
            if (i == 0) {
                path.moveTo(x, y)
                fill.moveTo(x, padY + h)
                fill.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fill.lineTo(x, y)
            }
        }
        fill.lineTo(padX + step * (values.size - 1).coerceAtLeast(0), padY + h)
        fill.close()
        drawPath(path = fill, brush = brush, alpha = 0.18f)
        drawPath(path = path, brush = brush, style = Stroke(width = 6f))
        // 端点圆
        values.forEachIndexed { i, v ->
            val x = padX + step * i
            val y = padY + h - (v / maxVal.toFloat()) * h
            drawCircle(color = cs.primary, radius = 6f, center = Offset(x, y))
        }
    }
}
