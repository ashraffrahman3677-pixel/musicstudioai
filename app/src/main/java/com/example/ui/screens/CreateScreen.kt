package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudioGenreCatalog
import com.example.ui.MainViewModel
import com.example.ui.StudioTab
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val composerMode by viewModel.composerMode.collectAsState()
    val promptText by viewModel.promptText.collectAsState()
    val songTitle by viewModel.songTitle.collectAsState()
    val lyricsText by viewModel.lyricsText.collectAsState()
    val songLanguage by viewModel.songLanguage.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()
    val selectedBpm by viewModel.selectedBpm.collectAsState()
    val selectedKey by viewModel.selectedKey.collectAsState()
    val selectedDuration by viewModel.selectedDuration.collectAsState()
    val vocalType by viewModel.vocalType.collectAsState()
    val selectedVoiceProfile by viewModel.selectedVoiceProfile.collectAsState()
    val voiceProfiles by viewModel.voiceProfiles.collectAsState()
    val isPlanning by viewModel.isPlanning.collectAsState()
    val isWritingLyrics by viewModel.isWritingLyrics.collectAsState()
    val plannedDirectorOutput by viewModel.plannedDirectorOutput.collectAsState()
    val creditsBalance by viewModel.authRepo.creditsBalance.collectAsState()

    var showLyricsEditor by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Header / Studio Title & Frosted Credit Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI Music Studio",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = FrostedTextPrimary
                    )
                )
                Text(
                    text = if (songLanguage == "ms") "Cipta lagu dengan AI & Suara Sendiri" else "Create songs with AI & Your Own Voice",
                    style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary)
                )
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = FrostedGlassCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Credits",
                        tint = FrostedAccentAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$creditsBalance Kr",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = FrostedTextPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simple Mode vs Advanced Mode Frosted Switcher
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = FrostedGlassSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorderSubtle)
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                Button(
                    onClick = { viewModel.setComposerMode("SIMPLE") },
                    modifier = Modifier.weight(1f).testTag("mode_simple_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (composerMode == "SIMPLE") FrostedPrimaryDark else Color.Transparent,
                        contentColor = if (composerMode == "SIMPLE") Color.White else FrostedTextSecondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (songLanguage == "ms") "Mod Mudah" else "Simple Mode", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { viewModel.setComposerMode("ADVANCED") },
                    modifier = Modifier.weight(1f).testTag("mode_advanced_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (composerMode == "ADVANCED") FrostedPrimaryDark else Color.Transparent,
                        contentColor = if (composerMode == "ADVANCED") Color.White else FrostedTextSecondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (songLanguage == "ms") "Mod Terperinci" else "Advanced Mode", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prompt Input Glass Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = FrostedGlassCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (songLanguage == "ms") "Terangkan Lagu Idaman Anda" else "Describe Your Song Idea",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = FrostedPrimaryDark
                        )
                    )

                    // Language Selector Chips
                    Row {
                        FilterChip(
                            selected = (songLanguage == "ms"),
                            onClick = { viewModel.setSongLanguage("ms") },
                            label = { Text("Melayu", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FrostedPrimaryDark,
                                selectedLabelColor = Color.White,
                                containerColor = FrostedGlassInput,
                                labelColor = FrostedTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = (songLanguage == "ms"),
                                borderColor = FrostedBorder
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            selected = (songLanguage == "en"),
                            onClick = { viewModel.setSongLanguage("en") },
                            label = { Text("English", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FrostedPrimaryDark,
                                selectedLabelColor = Color.White,
                                containerColor = FrostedGlassInput,
                                labelColor = FrostedTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = (songLanguage == "en"),
                                borderColor = FrostedBorder
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = promptText,
                    onValueChange = { viewModel.setPromptText(it) },
                    modifier = Modifier.fillMaxWidth().testTag("song_prompt_input"),
                    placeholder = {
                        Text(
                            if (songLanguage == "ms") "cth: Buat lagu rap Melayu tentang depression, hidup susah tetapi ada harapan..."
                            else "e.g.: Create an emotional hip hop song about overcoming struggles and finding hope...",
                            color = FrostedTextMuted
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FrostedPrimary,
                        unfocusedBorderColor = FrostedBorder,
                        focusedTextColor = FrostedTextPrimary,
                        unfocusedTextColor = FrostedTextPrimary,
                        focusedContainerColor = FrostedGlassCardElevated,
                        unfocusedContainerColor = FrostedGlassInput
                    ),
                    shape = RoundedCornerShape(18.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gemini Song Director Button (Frosted Style)
                Button(
                    onClick = { viewModel.runSongDirector() },
                    modifier = Modifier.fillMaxWidth().testTag("song_director_button"),
                    enabled = !isPlanning && promptText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FrostedGlassCardElevated,
                        disabledContainerColor = FrostedGlassInput
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isPlanning) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = FrostedPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (songLanguage == "ms") "AI Song Director sedang menyusun..." else "AI Song Director Planning...",
                            color = FrostedPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = FrostedPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (songLanguage == "ms") "Susun Lagu dengan Gemini Song Director" else "Arrange with Gemini Song Director",
                            color = FrostedPrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Genre Presets
        Text(
            text = if (songLanguage == "ms") "Pilihan Genre Popular" else "Popular Genre Presets",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = FrostedTextPrimary)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(StudioGenreCatalog.presets) { preset ->
                val isSelected = (selectedGenre == preset.title)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) FrostedPrimaryDark else FrostedGlassCardElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) FrostedPrimaryMedium else FrostedBorder
                    ),
                    shadowElevation = if (isSelected) 4.dp else 1.dp,
                    modifier = Modifier.clickable { viewModel.applyGenrePreset(preset) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else FrostedPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (songLanguage == "ms") preset.titleMs else preset.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else FrostedTextPrimary
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vocal Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = FrostedGlassCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (songLanguage == "ms") "Pilihan Vokal Lagu" else "Vocal Selection",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = FrostedPrimaryDark
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // AI Vocal
                    Surface(
                        modifier = Modifier.weight(1f).clickable { viewModel.setVocalType("AI_VOCAL") },
                        shape = RoundedCornerShape(20.dp),
                        color = if (vocalType == "AI_VOCAL") FrostedPrimaryDark else FrostedGlassCardElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (vocalType == "AI_VOCAL") FrostedPrimaryMedium else FrostedBorder
                        ),
                        shadowElevation = if (vocalType == "AI_VOCAL") 4.dp else 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (vocalType == "AI_VOCAL") FrostedPrimary else FrostedPrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = if (vocalType == "AI_VOCAL") Color.White else FrostedPrimaryDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "AI Vocal",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (vocalType == "AI_VOCAL") Color.White else FrostedTextPrimary
                                )
                            )
                        }
                    }

                    // My Voice
                    Surface(
                        modifier = Modifier.weight(1f).clickable { viewModel.setVocalType("USER_VOICE") },
                        shape = RoundedCornerShape(20.dp),
                        color = if (vocalType == "USER_VOICE") FrostedPrimaryDark else FrostedGlassCardElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (vocalType == "USER_VOICE") FrostedPrimaryMedium else FrostedBorder
                        ),
                        shadowElevation = if (vocalType == "USER_VOICE") 4.dp else 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (vocalType == "USER_VOICE") FrostedPrimary else FrostedPrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = if (vocalType == "USER_VOICE") Color.White else FrostedPrimaryDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                if (songLanguage == "ms") "Suara Saya" else "My Voice",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (vocalType == "USER_VOICE") Color.White else FrostedTextPrimary
                                )
                            )
                        }
                    }

                    // Instrumental
                    Surface(
                        modifier = Modifier.weight(1f).clickable { viewModel.setVocalType("INSTRUMENTAL") },
                        shape = RoundedCornerShape(20.dp),
                        color = if (vocalType == "INSTRUMENTAL") FrostedPrimaryDark else FrostedGlassCardElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (vocalType == "INSTRUMENTAL") FrostedPrimaryMedium else FrostedBorder
                        ),
                        shadowElevation = if (vocalType == "INSTRUMENTAL") 4.dp else 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (vocalType == "INSTRUMENTAL") FrostedPrimary else FrostedPrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Piano,
                                    contentDescription = null,
                                    tint = if (vocalType == "INSTRUMENTAL") Color.White else FrostedPrimaryDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Instrumental",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (vocalType == "INSTRUMENTAL") Color.White else FrostedTextPrimary
                                )
                            )
                        }
                    }
                }

                // If My Voice is chosen, show profile info or enroll button
                if (vocalType == "USER_VOICE") {
                    Spacer(modifier = Modifier.height(14.dp))
                    if (voiceProfiles.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = FrostedGlassCardElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, FrostedPrimaryLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = FrostedAccentEmerald)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = selectedVoiceProfile?.name ?: voiceProfiles.first().name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = FrostedTextPrimary)
                                        )
                                        Text(
                                            text = "Profil Suara Sah • Skor Kualiti: 98%",
                                            style = MaterialTheme.typography.bodySmall.copy(color = FrostedAccentEmerald, fontSize = 11.sp)
                                        )
                                    }
                                }

                                TextButton(onClick = { viewModel.setTab(StudioTab.MY_VOICE) }) {
                                    Text("Tukar", color = FrostedPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.prepareNewVoiceEnrollment()
                                viewModel.setTab(StudioTab.MY_VOICE)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FrostedPrimaryDark),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (songLanguage == "ms") "Daftar & Sahkan Suara Anda Dahulu" else "Enroll & Verify Your Voice First", color = Color.White)
                        }
                    }
                }
            }
        }

        // Advanced Mode Configuration
        if (composerMode == "ADVANCED") {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = FrostedGlassCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (songLanguage == "ms") "Parameter Muzik Terperinci" else "Advanced Music Parameters",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = FrostedPrimaryDark
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title Field
                    OutlinedTextField(
                        value = songTitle,
                        onValueChange = { viewModel.setSongTitle(it) },
                        label = { Text(if (songLanguage == "ms") "Tajuk Lagu" else "Song Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FrostedPrimary,
                            unfocusedBorderColor = FrostedBorder,
                            focusedTextColor = FrostedTextPrimary,
                            unfocusedTextColor = FrostedTextPrimary,
                            focusedContainerColor = FrostedGlassCardElevated,
                            unfocusedContainerColor = FrostedGlassInput
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // BPM
                        OutlinedTextField(
                            value = "$selectedBpm",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("BPM Tempo") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FrostedBorder,
                                unfocusedBorderColor = FrostedBorder,
                                focusedTextColor = FrostedTextPrimary,
                                unfocusedTextColor = FrostedTextPrimary,
                                focusedContainerColor = FrostedGlassInput,
                                unfocusedContainerColor = FrostedGlassInput
                            )
                        )

                        // Key
                        OutlinedTextField(
                            value = selectedKey,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Skala Nada (Key)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FrostedBorder,
                                unfocusedBorderColor = FrostedBorder,
                                focusedTextColor = FrostedTextPrimary,
                                unfocusedTextColor = FrostedTextPrimary,
                                focusedContainerColor = FrostedGlassInput,
                                unfocusedContainerColor = FrostedGlassInput
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Duration Slider
                    Text(
                        text = "${if (songLanguage == "ms") "Durasi Lagu:" else "Song Duration:"} $selectedDuration saat (${selectedDuration / 60}m ${selectedDuration % 60}s)",
                        style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary)
                    )
                    Slider(
                        value = selectedDuration.toFloat(),
                        onValueChange = { },
                        valueRange = 60f..240f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = FrostedPrimaryDark,
                            activeTrackColor = FrostedPrimary,
                            inactiveTrackColor = FrostedPrimaryLight
                        )
                    )
                }
            }
        }

        // Planned Director Summary Card (if available)
        if (plannedDirectorOutput != null) {
            Spacer(modifier = Modifier.height(16.dp))
            val plan = plannedDirectorOutput!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = FrostedGlassCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, FrostedAccentEmerald.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = FrostedAccentEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pelan Pengarah Muzik Gemini",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = FrostedAccentEmerald)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Tajuk: ${plan.title}\n• Genre: ${plan.genre} (${plan.subgenre})\n• Mood: ${plan.mood.joinToString(", ")}\n• Instrumen: ${plan.instrumentation.joinToString(", ")}\n• Struktur: ${plan.structure.joinToString(" → ")}",
                        style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextPrimary, lineHeight = 20.sp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lyrics Generator & Inline Editor Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = FrostedGlassCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (songLanguage == "ms") "Lirik Lagu AI & Suntingan" else "AI Lyrics & Editor",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = FrostedPrimaryDark
                        )
                    )

                    Button(
                        onClick = { viewModel.generateLyricsOnly() },
                        enabled = !isWritingLyrics,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FrostedGlassCardElevated,
                            contentColor = FrostedPrimaryDark
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isWritingLyrics) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = FrostedPrimary, strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp), tint = FrostedPrimaryDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (lyricsText.isBlank()) "Jana Lirik" else "Tulis Semula", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FrostedPrimaryDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = lyricsText,
                    onValueChange = { viewModel.setLyricsText(it) },
                    placeholder = {
                        Text(
                            if (songLanguage == "ms") "Lirik akan dijana secara automatik, atau anda boleh taip lirik sendiri di sini..."
                            else "Lyrics will be AI-generated or you can enter custom lyrics here...",
                            color = FrostedTextMuted
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("lyrics_editor_field"),
                    minLines = 4,
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FrostedPrimary,
                        unfocusedBorderColor = FrostedBorder,
                        focusedTextColor = FrostedTextPrimary,
                        unfocusedTextColor = FrostedTextPrimary,
                        focusedContainerColor = FrostedGlassCardElevated,
                        unfocusedContainerColor = FrostedGlassInput
                    ),
                    shape = RoundedCornerShape(18.dp)
                )

                if (lyricsText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (songLanguage == "ms") "Penambahbaikan AI Cepat:" else "Quick AI Lyrics Polish:",
                        style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary, fontSize = 11.sp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = { viewModel.refineLyrics("rhyme") },
                            label = { Text("Perkemas Rima", fontSize = 11.sp, color = FrostedTextPrimary) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = FrostedGlassCardElevated),
                            border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = FrostedBorder),
                            shape = RoundedCornerShape(12.dp)
                        )
                        SuggestionChip(
                            onClick = { viewModel.refineLyrics("emotional") },
                            label = { Text("Lebih Emosional", fontSize = 11.sp, color = FrostedTextPrimary) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = FrostedGlassCardElevated),
                            border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = FrostedBorder),
                            shape = RoundedCornerShape(12.dp)
                        )
                        SuggestionChip(
                            onClick = { viewModel.refineLyrics("aggressive") },
                            label = { Text("Lebih Bertenaga", fontSize = 11.sp, color = FrostedTextPrimary) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = FrostedGlassCardElevated),
                            border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = FrostedBorder),
                            shape = RoundedCornerShape(12.dp)
                        )
                        SuggestionChip(
                            onClick = { viewModel.refineLyrics("chorus") },
                            label = { Text("Korus Power", fontSize = 11.sp, color = FrostedTextPrimary) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = FrostedGlassCardElevated),
                            border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = FrostedBorder),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Big Master Generate Button (Pill Frosted Primary Button)
        val creditCost = if (vocalType == "USER_VOICE") 15 else if (vocalType == "INSTRUMENTAL") 8 else 10

        Button(
            onClick = { viewModel.startGeneratingSong() },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(CircleShape)
                .testTag("generate_song_master_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = FrostedPrimaryDark,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            enabled = promptText.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircleFilled,
                contentDescription = "Generate",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "${if (songLanguage == "ms") "Jana Lagu Sekarang" else "Generate Song Now"} ($creditCost Kredit)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom player
    }
}
