package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.VoiceProfileEntity
import com.example.ui.MainViewModel
import com.example.ui.StudioTab
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceProfilesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceProfiles by viewModel.voiceProfiles.collectAsState()
    val selectedVoiceProfile by viewModel.selectedVoiceProfile.collectAsState()
    val enrollmentStep by viewModel.enrollmentStep.collectAsState()
    val enrollVoiceName by viewModel.enrollVoiceName.collectAsState()
    val enrollChallengePhrase by viewModel.enrollChallengePhrase.collectAsState()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsState()
    val recordDurationSec by viewModel.recorder.recordDurationSec.collectAsState()
    val amplitudeLevel by viewModel.recorder.amplitudeFlow.collectAsState()
    val consentChecked by viewModel.consentChecked.collectAsState()
    val selectedLanguage by viewModel.authRepo.selectedLanguage.collectAsState()

    var showEnrollmentDialog by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf<VoiceProfileEntity?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecording()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (selectedLanguage == "ms") "Profil Suara Saya" else "My Voice Profiles",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = FrostedTextPrimary
                    )
                )
                Text(
                    text = if (selectedLanguage == "ms") "Cipta lagu menggunakan suara sah anda sendiri" else "Create songs using your own authorized voice",
                    style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary)
                )
            }

            Button(
                onClick = {
                    viewModel.prepareNewVoiceEnrollment()
                    showEnrollmentDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = FrostedPrimaryDark),
                shape = CircleShape,
                modifier = Modifier.testTag("enroll_new_voice_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (selectedLanguage == "ms") "Daftar Suara" else "Enroll Voice", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security Notice Banner (Frosted Glass)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = FrostedGlassCardElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FrostedPrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = FrostedPrimaryDark, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (selectedLanguage == "ms")
                        "Perlindungan Hak Suara: Profil suara anda dilindungi dengan penyulitan kriptografi SHA-256 dan hanya boleh digunakan oleh akaun anda."
                    else
                        "Voice Rights Protection: Your voice profile is cryptographically isolated and private to your account only.",
                    style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (voiceProfiles.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
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
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = FrostedAccentRose,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedLanguage == "ms") "Belum ada profil suara didaftarkan" else "No voice profile enrolled yet",
                        style = MaterialTheme.typography.bodyLarge.copy(color = FrostedTextSecondary, fontWeight = FontWeight.Medium)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.prepareNewVoiceEnrollment()
                            showEnrollmentDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FrostedPrimaryDark),
                        shape = CircleShape
                    ) {
                        Text(if (selectedLanguage == "ms") "Mulakan Pendaftaran Suara" else "Start Voice Enrollment", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(voiceProfiles, key = { it.id }) { profile ->
                    val isSelected = (selectedVoiceProfile?.id == profile.id)
                    VoiceProfileCard(
                        profile = profile,
                        isSelected = isSelected,
                        onSelect = { viewModel.selectVoiceProfile(profile) },
                        onUseInSong = {
                            viewModel.selectVoiceProfile(profile)
                            viewModel.setTab(StudioTab.CREATE)
                        },
                        onDelete = { profileToDelete = profile }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Voice Enrollment Modal Dialog (Frosted Glass)
    if (showEnrollmentDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isRecordingVoice) showEnrollmentDialog = false
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = FrostedGlassCardElevated,
            title = {
                Text(
                    text = if (selectedLanguage == "ms") "Pendaftaran Suara Saya" else "Enroll My Voice",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = FrostedTextPrimary)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    when (enrollmentStep) {
                        1 -> {
                            // Step 1: Voice Name & Challenge Script
                            Text(
                                text = if (selectedLanguage == "ms")
                                    "Langkah 1: Masukkan nama profil dan baca frasa cabaran di bawah semasa merakam untuk mengesahkan ketulenan suara anda."
                                else
                                    "Step 1: Enter profile name and prepare to read the challenge phrase during recording to verify authenticity.",
                                style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = enrollVoiceName,
                                onValueChange = { },
                                label = { Text("Nama Profil Suara") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FrostedPrimary,
                                    unfocusedBorderColor = FrostedBorder,
                                    focusedTextColor = FrostedTextPrimary,
                                    unfocusedTextColor = FrostedTextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = FrostedGlassInput,
                                border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Frasa Cabaran Pengesahan:",
                                        style = MaterialTheme.typography.labelSmall.copy(color = FrostedPrimaryDark, fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\"$enrollChallengePhrase\"",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = FrostedTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                            }
                        }

                        2 -> {
                            // Step 2: Live Recording
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isRecordingVoice) "Sila baca frasa cabaran sekarang:" else "Sedia untuk merakam",
                                    style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "\"$enrollChallengePhrase\"",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = FrostedTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Pulsing Recording Button
                                val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
                                    initialValue = 1f,
                                    targetValue = if (isRecordingVoice) 1.2f else 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(600, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "mic_scale"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(if (isRecordingVoice) FrostedAccentCrimson else FrostedPrimaryDark)
                                        .clickable {
                                            if (isRecordingVoice) {
                                                viewModel.stopVoiceRecording()
                                            } else {
                                                val hasPermission = ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.RECORD_AUDIO
                                                ) == PackageManager.PERMISSION_GRANTED

                                                if (hasPermission) {
                                                    viewModel.startVoiceRecording()
                                                } else {
                                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isRecordingVoice) Icons.Default.Stop else Icons.Default.Mic,
                                        contentDescription = "Record",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = if (isRecordingVoice) "Merakam... ${recordDurationSec}s" else "Tekan mikrofon untuk mula",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isRecordingVoice) FrostedAccentCrimson else FrostedTextSecondary
                                    )
                                )

                                if (isRecordingVoice) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { amplitudeLevel },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                        color = FrostedAccentCrimson,
                                        trackColor = FrostedGlassInput
                                    )
                                }
                            }
                        }

                        3 -> {
                            // Step 3: Review & Explicit Legal Consent
                            Text(
                                text = "Langkah 3: Pengesahan Keizinan Undang-Undang",
                                style = MaterialTheme.typography.labelMedium.copy(color = FrostedPrimaryDark, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Rakaman sampel suara anda berjaya diambil (Kualiti: 98%).",
                                style = MaterialTheme.typography.bodySmall.copy(color = FrostedAccentEmerald, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = FrostedGlassInput,
                                border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "\"Saya mengesahkan bahawa suara ini adalah suara saya sendiri, dan saya memberikan kebenaran kepada AI Music Studio untuk memproses profil suara ini bagi kegunaan akaun saya secara sah.\"",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = FrostedTextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    ),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.setConsentChecked(!consentChecked) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = consentChecked,
                                    onCheckedChange = { viewModel.setConsentChecked(it) },
                                    colors = CheckboxDefaults.colors(checkedColor = FrostedAccentEmerald)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Saya bersetuju dan mengesahkan hak milik suara ini.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextPrimary, fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }

                        4 -> {
                            // Step 4: Success
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = FrostedAccentEmerald,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Profil Suara Berjaya Didaftarkan!",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = FrostedTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Anda kini boleh menjana lagu penuh menggunakan suara anda di laman Cipta Lagu.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                when (enrollmentStep) {
                    1 -> {
                        Button(
                            onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    viewModel.startVoiceRecording()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FrostedPrimaryDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Seterusnya & Rakam", color = Color.White)
                        }
                    }
                    3 -> {
                        Button(
                            onClick = { viewModel.submitVoiceEnrollment() },
                            enabled = consentChecked,
                            colors = ButtonDefaults.buttonColors(containerColor = FrostedAccentEmerald),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Sahkan & Simpan Profil", color = Color.White)
                        }
                    }
                    4 -> {
                        Button(
                            onClick = {
                                showEnrollmentDialog = false
                                viewModel.setTab(StudioTab.CREATE)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FrostedPrimaryDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cipta Lagu Sekarang", color = Color.White)
                        }
                    }
                }
            },
            dismissButton = {
                if (enrollmentStep < 4) {
                    TextButton(onClick = {
                        viewModel.stopVoiceRecording()
                        showEnrollmentDialog = false
                    }) {
                        Text("Batal", color = FrostedTextSecondary)
                    }
                }
            }
        )
    }

    // Delete Voice Profile Confirmation
    if (profileToDelete != null) {
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = FrostedGlassCardElevated,
            title = { Text("Padam Profil Suara?", color = FrostedTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Adakah anda pasti mahu memadam profil '${profileToDelete!!.name}'? Semua sampel audio yang disimpan akan dihapuskan serta-merta.",
                    color = FrostedTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVoiceProfile(profileToDelete!!.id)
                        profileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FrostedAccentCrimson),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Padam Profil", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text("Batal", color = FrostedTextSecondary)
                }
            }
        )
    }
}

@Composable
fun VoiceProfileCard(
    profile: VoiceProfileEntity,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onUseInSong: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FrostedGlassCard),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) FrostedPrimary else FrostedBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) FrostedAccentRose else FrostedPrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else FrostedPrimaryDark
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FrostedTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = FrostedAccentEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "Status: SAH • Skor Kualiti: 98%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = FrostedAccentEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                IconButton(onClick = { onDelete() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = FrostedTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FrostedGlassInput,
                border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = FrostedPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hash Keizinan: ${profile.consentHash.take(16)}...",
                        style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary, fontSize = 10.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onUseInSong() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) FrostedPrimaryDark else FrostedGlassCardElevated
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) FrostedPrimaryDark else FrostedBorder
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isSelected) "Gunakan Dalam Cipta Lagu (Dipilih)" else "Pilih Suara Ini",
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else FrostedTextPrimary
                )
            }
        }
    }
}
