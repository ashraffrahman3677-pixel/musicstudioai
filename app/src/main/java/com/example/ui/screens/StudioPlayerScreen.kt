package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.StudioTab
import com.example.ui.components.RealTimeStudioVisualizer
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.*

@Composable
fun StudioPlayerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentSong by viewModel.player.currentSong.collectAsState()
    val isPlaying by viewModel.player.isPlaying.collectAsState()
    val currentPosMs by viewModel.player.currentPositionMs.collectAsState()
    val durationMs by viewModel.player.durationMs.collectAsState()
    val isLooping by viewModel.player.isLooping.collectAsState()
    val playbackSpeed by viewModel.player.playbackSpeed.collectAsState()
    val selectedLanguage by viewModel.authRepo.selectedLanguage.collectAsState()

    var showLyricsTab by remember { mutableStateOf(false) }

    if (currentSong == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(FrostedPrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = FrostedPrimaryDark,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (selectedLanguage == "ms") "Tiada lagu sedang dimainkan" else "No track currently playing",
                    style = MaterialTheme.typography.bodyLarge.copy(color = FrostedTextSecondary, fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { viewModel.setTab(StudioTab.CREATE) },
                    colors = ButtonDefaults.buttonColors(containerColor = FrostedPrimaryDark),
                    shape = CircleShape,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(if (selectedLanguage == "ms") "Cipta Lagu Baharu" else "Create a Song", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val song = currentSong!!
    val progress = if (durationMs > 0) (currentPosMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = FrostedGlassCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder)
            ) {
                IconButton(onClick = { viewModel.setTab(StudioTab.LIBRARY) }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = FrostedTextPrimary)
                }
            }

            Text(
                text = "Studio Audio Master",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = FrostedPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            )

            Surface(
                shape = CircleShape,
                color = FrostedGlassCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder)
            ) {
                IconButton(onClick = { viewModel.toggleFavorite(song) }) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (song.isFavorite) FrostedAccentRose else FrostedTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hero Cover Visualizer Artwork Glass Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = FrostedGlassCard),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, FrostedBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Banner Image
                Image(
                    painter = painterResource(id = R.drawable.hero_banner),
                    contentDescription = "Cover Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Frosted Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    FrostedPrimaryDark.copy(alpha = 0.40f),
                                    FrostedPrimaryDark.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Track Badge Info inside cover
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (song.vocalType == "USER_VOICE") FrostedAccentRose else FrostedPrimaryMedium,
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder)
                    ) {
                        Text(
                            text = if (song.vocalType == "USER_VOICE") "Suara Disahkan Pemilik" else if (song.isInstrumental) "Instrumental Murni" else "Studio AI Vocal",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (song.vocalType == "USER_VOICE") Color.White else FrostedPrimaryDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title and Genre Details
        Text(
            text = song.title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = FrostedTextPrimary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${song.genre} • ${song.musicalKey} • ${song.bpm} BPM",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = FrostedPrimary,
                fontWeight = FontWeight.SemiBold
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Real-Time Studio DSP Audio Visualizer
        RealTimeStudioVisualizer(
            visualizerEngine = viewModel.player.visualizerEngine,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Seek Slider
        Slider(
            value = currentPosMs.toFloat(),
            onValueChange = { viewModel.player.seekTo(it.toInt()) },
            valueRange = 0f..(durationMs.toFloat().coerceAtLeast(1000f)),
            colors = SliderDefaults.colors(
                thumbColor = FrostedPrimaryDark,
                activeTrackColor = FrostedPrimary,
                inactiveTrackColor = FrostedPrimaryLight
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Timestamps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val curSec = (currentPosMs / 1000)
            val durSec = (durationMs / 1000)
            Text(
                text = "%d:%02d".format(curSec / 60, curSec % 60),
                style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary, fontWeight = FontWeight.Medium)
            )
            Text(
                text = "%d:%02d".format(durSec / 60, durSec % 60),
                style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary, fontWeight = FontWeight.Medium)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playback Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed Chip
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FrostedGlassCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            ) {
                TextButton(onClick = {
                    val nextSpeed = when (playbackSpeed) {
                        1.0f -> 1.25f
                        1.25f -> 1.5f
                        1.5f -> 0.75f
                        else -> 1.0f
                    }
                    viewModel.player.setSpeed(nextSpeed)
                }) {
                    Text("${playbackSpeed}x", color = FrostedPrimaryDark, fontWeight = FontWeight.Bold)
                }
            }

            // Loop Button
            Surface(
                shape = CircleShape,
                color = if (isLooping) FrostedPrimaryLight else FrostedGlassCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder)
            ) {
                IconButton(onClick = { viewModel.player.toggleLoop() }) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Loop",
                        tint = if (isLooping) FrostedPrimaryDark else FrostedTextSecondary
                    )
                }
            }

            // Play / Pause Master Button
            IconButton(
                onClick = { viewModel.player.togglePlayPause() },
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(FrostedPrimaryDark)
                    .testTag("player_center_play_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            // Lyrics Toggle Button
            Surface(
                shape = CircleShape,
                color = if (showLyricsTab) FrostedPrimaryLight else FrostedGlassCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder)
            ) {
                IconButton(onClick = { showLyricsTab = !showLyricsTab }) {
                    Icon(
                        imageVector = Icons.Default.Lyrics,
                        contentDescription = "Lyrics",
                        tint = if (showLyricsTab) FrostedPrimaryDark else FrostedTextSecondary
                    )
                }
            }

            // Download Button
            Surface(
                shape = CircleShape,
                color = FrostedGlassCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder)
            ) {
                IconButton(onClick = {
                    viewModel.downloadSong(song) { _, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download WAV",
                        tint = FrostedPrimaryDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Lyrics Section Glass Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = FrostedGlassCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Lirik Lagu & Struktur Vokal",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = FrostedPrimaryDark
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = song.lyrics.ifBlank { "(Tiada lirik - Trek Instrumental)" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = FrostedTextPrimary,
                        lineHeight = 24.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
