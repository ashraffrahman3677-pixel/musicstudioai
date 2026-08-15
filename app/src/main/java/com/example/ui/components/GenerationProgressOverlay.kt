package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.GenerationUiState
import com.example.ui.theme.*

@Composable
fun GenerationProgressOverlay(
    state: GenerationUiState,
    onDismiss: () -> Unit
) {
    if (state !is GenerationUiState.Generating) return

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("generation_progress_card"),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = FrostedGlassCardElevated),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, FrostedBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulsing Icon Ring (Frosted Style)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(FrostedPrimary, FrostedPrimaryLight, FrostedAccentRose, FrostedPrimary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(FrostedGlassCardElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Synthesizing",
                            tint = FrostedPrimaryDark,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "AI Studio Multi-Track DSP",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = FrostedPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = state.job.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = FrostedTextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = FrostedPrimaryDark,
                    trackColor = FrostedPrimaryLight
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Current Stage Text
                Text(
                    text = state.stageText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = FrostedTextSecondary,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${(state.progress * 100).toInt()}% Selesai",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = FrostedAccentEmerald,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Pipeline Stages Indicators
                val stages = listOf(
                    "Instrumen" to (state.progress >= 0.25f),
                    "Vokal AI" to (state.progress >= 0.55f),
                    "Mixing" to (state.progress >= 0.80f),
                    "Mastering" to (state.progress >= 0.95f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    stages.forEach { (label, isDone) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (isDone) FrostedAccentEmerald else FrostedBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = if (isDone) FrostedTextPrimary else FrostedTextMuted
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
