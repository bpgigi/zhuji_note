package com.zhuji.note.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Indeterminate, gradient progress bar for streaming/network operations. */
@Composable
fun GradientProgressBar(visible: Boolean, modifier: Modifier = Modifier) {
    if (!visible) return
    val infinite = rememberInfiniteTransition(label = "gp")
    val shift by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "shift",
    )
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.tertiary
    Box(
        modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            Modifier
                .fillMaxWidth(0.4f)
                .height(3.dp)
                .graphicsLayer { translationX = (size.width * (shift * 2.5f - 0.4f)) }
                .background(
                    brush = Brush.linearGradient(listOf(primary, secondary, primary))
                )
        )
    }
}

/** Three-dot busy indicator with bouncing animation. */
@Composable
fun BouncingDots(label: String? = null, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TypingDot()
        if (!label.isNullOrBlank()) {
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Linear loading line for top app bars. */
@Composable
fun TopLoadingLine(loading: Boolean, modifier: Modifier = Modifier) {
    if (!loading) return
    LinearProgressIndicator(
        modifier = modifier.fillMaxWidth().height(2.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = Color.Transparent,
    )
}

@Composable
fun StaggeredFadeIn(
    delayMs: Long = 0L,
    durationMs: Int = 320,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs)
        visible = true
    }
    val a by animateFloatAsState(
        if (visible) 1f else 0f,
        animationSpec = tween(durationMs),
        label = "stagger-a",
    )
    val ty by animateFloatAsState(
        if (visible) 0f else 22f,
        animationSpec = tween(durationMs),
        label = "stagger-ty",
    )
    Box(
        Modifier.graphicsLayer { alpha = a; translationY = ty }
    ) { content() }
}

@Composable
fun ShimmerLine(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "sh")
    val t by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1400, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "tt",
    )
    val cs = MaterialTheme.colorScheme
    Box(
        modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(cs.surfaceVariant)
            .graphicsLayer { alpha = 0.4f + 0.5f * t }
    )
}

