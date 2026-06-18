package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.BackgroundStyle
import com.example.ui.theme.*

@Composable
fun DynamicBackground(modifier: Modifier = Modifier, style: BackgroundStyle) {
    when (style) {
        BackgroundStyle.Orbs -> AnimatedBackgroundOrbs(modifier)
        BackgroundStyle.Ripple -> GentleRippleFlow(modifier)
        BackgroundStyle.Gradient -> SlowGradientFlow(modifier)
        BackgroundStyle.Pulse -> PulseFlow(modifier)
    }
}


@Composable
fun SlowGradientFlow(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_flow")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "offset"
    )

    Canvas(modifier = modifier.fillMaxSize().background(DeepSpace)) {
        val brush = Brush.linearGradient(
            colors = listOf(CyanGlow.copy(alpha = 0.2f), PurpleAccent.copy(alpha = 0.2f), NeonPink.copy(alpha = 0.2f)),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width * offset, size.height * offset)
        )
        drawRect(brush = brush)
    }
}

@Composable
fun GentleRippleFlow(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "scale"
    )

    Canvas(modifier = modifier.fillMaxSize().background(DeepSpace)) {
        drawCircle(
            color = CyanGlow.copy(alpha = 0.15f * (1f - (scale / 2f))),
            radius = size.width * scale,
            center = center
        )
    }
}

@Composable
fun PulseFlow(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize().background(DeepSpace)) {
        drawRect(color = DeepSpace.copy(alpha = alpha))
    }
}
