package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.MusicPlayerManager
import com.example.ui.theme.*

@Composable
fun PersistentPlayerBar(
    playerManager: MusicPlayerManager,
    onExpandPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by playerManager.currentSong.collectAsState()
    val isPlaying by playerManager.isPlaying.collectAsState()
    val currentPos by playerManager.currentPositionMs.collectAsState()
    val duration by playerManager.durationMs.collectAsState()
    val visualizerSnapshot by playerManager.visualizerEngine.snapshot.collectAsState()

    if (currentSong == null) return

    val song = currentSong!!
    val progress = if (duration > 0) (currentPos.toFloat() / duration).coerceIn(0f, 1f) else 0f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable { onExpandPlayer() }
            .testTag("persistent_player_bar"),
        color = FrostedGlassCardElevated,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder)
    ) {
        Column {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = FrostedPrimary,
                trackColor = FrostedPrimaryLight
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Disc / Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FrostedPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (song.vocalType == "USER_VOICE") Icons.Default.RecordVoiceOver else Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Song Title & Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = FrostedTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${song.genre} • ${if (song.vocalType == "USER_VOICE") "Suara Saya" else if (song.isInstrumental) "Instrumental" else "AI Vocal"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = FrostedTextSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Waveform mini with real-time audio bands
                WaveformVisualizer(
                    isPlaying = isPlaying,
                    modifier = Modifier.width(56.dp).height(24.dp),
                    barCount = 10,
                    realtimeBands = visualizerSnapshot.frequencyBands
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Play / Pause Button
                IconButton(
                    onClick = { playerManager.togglePlayPause() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FrostedPrimaryDark)
                        .testTag("player_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
