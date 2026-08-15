package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FrostedPrimary
import com.example.ui.theme.FrostedPrimaryDark
import com.example.ui.theme.FrostedAccentRose
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 32,
    realtimeBands: FloatArray? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(48.dp)) {
        val width = size.width
        val height = size.height
        val barWidth = width / (barCount * 1.5f)
        val spacing = barWidth * 0.5f

        val gradient = Brush.verticalGradient(
            colors = listOf(FrostedAccentRose, FrostedPrimary, FrostedPrimaryDark)
        )

        for (i in 0 until barCount) {
            val normalizedI = i.toFloat() / barCount
            val wave = if (realtimeBands != null && isPlaying && realtimeBands.isNotEmpty()) {
                val bandIdx = (normalizedI * realtimeBands.size).toInt().coerceIn(0, realtimeBands.size - 1)
                realtimeBands[bandIdx].coerceIn(0.12f, 0.98f)
            } else if (isPlaying) {
                (sin(normalizedI * 10f + phase) * 0.4f + sin(normalizedI * 4f - phase * 0.8f) * 0.3f + 0.35f).coerceIn(0.15f, 0.95f)
            } else {
                (sin(normalizedI * 6f) * 0.2f + 0.25f).coerceIn(0.1f, 0.4f)
            }

            val barHeight = height * wave
            val x = i * (barWidth + spacing)
            val y = (height - barHeight) / 2f

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
