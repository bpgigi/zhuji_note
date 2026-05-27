package com.zhuji.note.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zhuji.note.ui.theme.ZhujiCornerTokens
import androidx.compose.material3.Text
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = ZhujiCornerTokens.NoteCard,
    tint: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    val border = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier
            .clip(shape)
            .background(tint)
            .border(0.5.dp, border, shape)
    ) { content() }
}

@Composable
fun GradientText(
    text: String,
    style: TextStyle = MaterialTheme.typography.displayMedium,
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary
    ),
    modifier: Modifier = Modifier,
) {
    val brush = Brush.linearGradient(colors)
    Text(text = text, style = style.copy(brush = brush, fontWeight = FontWeight.SemiBold), modifier = modifier)
}

@Composable
fun AuroraBackground(alpha: Float = 0.18f, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "aurora")
    val t1 by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(18000, easing = LinearEasing)), label = "t1")
    val t2 by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(26000, easing = LinearEasing)), label = "t2")
    val c1 = MaterialTheme.colorScheme.primary
    val c2 = MaterialTheme.colorScheme.secondary
    val c3 = MaterialTheme.colorScheme.tertiary
    Canvas(modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        fun blob(color: Color, cx: Float, cy: Float, r: Float) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(color.copy(alpha = alpha), Color.Transparent),
                    center = Offset(cx, cy), radius = r
                ),
                radius = r, center = Offset(cx, cy)
            )
        }
        blob(c1, w * (0.2f + 0.6f * t1), h * (0.15f + 0.3f * t2), w * 0.65f)
        blob(c2, w * (0.85f - 0.5f * t2), h * (0.7f + 0.2f * t1), w * 0.55f)
        blob(c3, w * (0.5f + 0.3f * sin(t1 * PI).toFloat()), h * (0.45f + 0.4f * t2), w * 0.45f)
    }
}

@Composable
fun TypingDot(color: Color = MaterialTheme.colorScheme.onSurfaceVariant, dotSize: Dp = 6.dp, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "dot")
    val a1 by infinite.animateFloat(
        0.3f, 1f,
        infiniteRepeatable(tween(600, delayMillis = 0), repeatMode = RepeatMode.Reverse), label = "a1"
    )
    val a2 by infinite.animateFloat(
        0.3f, 1f,
        infiniteRepeatable(tween(600, delayMillis = 120), repeatMode = RepeatMode.Reverse), label = "a2"
    )
    val a3 by infinite.animateFloat(
        0.3f, 1f,
        infiniteRepeatable(tween(600, delayMillis = 240), repeatMode = RepeatMode.Reverse), label = "a3"
    )
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf(a1, a2, a3).forEach { a ->
            Box(
                Modifier
                    .size(dotSize)
                    .graphicsLayer { alpha = a; scaleX = 0.7f + 0.3f * a; scaleY = 0.7f + 0.3f * a }
                    .background(color, CircleShape)
            )
        }
    }
}
