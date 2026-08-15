package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FrostedCanvasBackground
import com.example.ui.theme.FrostedOrbLavender
import com.example.ui.theme.FrostedOrbLilac

/**
 * Renders the signature atmospheric Frosted Glass canvas with luminous, soft-diffused
 * radial gradient glowing orbs in the background.
 */
@Composable
fun FrostedBackgroundContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FrostedCanvasBackground)
    ) {
        // Ambient soft glowing orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-left soft lilac glow orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        FrostedOrbLilac.copy(alpha = 0.55f),
                        FrostedOrbLilac.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(x = width * 0.05f, y = height * 0.08f),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(x = width * 0.05f, y = height * 0.08f)
            )

            // Middle-right soft lavender glow orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        FrostedOrbLavender.copy(alpha = 0.60f),
                        FrostedOrbLavender.copy(alpha = 0.30f),
                        Color.Transparent
                    ),
                    center = Offset(x = width * 0.95f, y = height * 0.48f),
                    radius = width * 0.60f
                ),
                radius = width * 0.60f,
                center = Offset(x = width * 0.95f, y = height * 0.48f)
            )

            // Bottom-left subtle ambient lavender glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        FrostedOrbLilac.copy(alpha = 0.40f),
                        FrostedOrbLavender.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(x = width * 0.2f, y = height * 0.88f),
                    radius = width * 0.55f
                ),
                radius = width * 0.55f,
                center = Offset(x = width * 0.2f, y = height * 0.88f)
            )
        }

        content()
    }
}
