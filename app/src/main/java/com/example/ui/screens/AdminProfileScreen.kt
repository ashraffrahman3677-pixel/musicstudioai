package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val userName by viewModel.authRepo.userName.collectAsState()
    val userEmail by viewModel.authRepo.userEmail.collectAsState()
    val userPlan by viewModel.authRepo.userPlan.collectAsState()
    val creditsBalance by viewModel.authRepo.creditsBalance.collectAsState()
    val selectedLanguage by viewModel.authRepo.selectedLanguage.collectAsState()
    val emergencyKillSwitch by viewModel.authRepo.emergencyKillSwitch.collectAsState()
    val transactionHistory by viewModel.authRepo.transactionHistory.collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Profile Header Card (Frosted Glass)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = FrostedGlassCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(FrostedPrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = FrostedPrimaryDark,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = FrostedTextPrimary
                        )
                    )
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FrostedPrimaryDark
                    ) {
                        Text(
                            text = userPlan,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Credits & Balance Card (Frosted Glass)
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
                    Column {
                        Text(
                            text = if (selectedLanguage == "ms") "Baki Kredit Studio" else "Studio Credits Balance",
                            style = MaterialTheme.typography.labelMedium.copy(color = FrostedTextSecondary, fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "$creditsBalance Kredit",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = FrostedTextPrimary
                            )
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.authRepo.purchasePlan("Creator Pro", 100)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FrostedPrimaryDark),
                        shape = CircleShape
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = FrostedAccentAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+100 Kredit", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language & Localization Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = FrostedGlassCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (selectedLanguage == "ms") "Tetapan Bahasa Aplikasi" else "Application Language Settings",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = FrostedPrimaryDark)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { viewModel.authRepo.setLanguage("ms") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedLanguage == "ms") FrostedPrimaryDark else FrostedGlassCardElevated
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedLanguage == "ms") FrostedPrimaryDark else FrostedBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Bahasa Melayu", color = if (selectedLanguage == "ms") Color.White else FrostedTextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.authRepo.setLanguage("en") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedLanguage == "en") FrostedPrimaryDark else FrostedGlassCardElevated
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedLanguage == "en") FrostedPrimaryDark else FrostedBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("English", color = if (selectedLanguage == "en") Color.White else FrostedTextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Admin Cost Controls & System Health
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = FrostedGlassCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = FrostedPrimaryDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kawalan Pentadbir & Kos API",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = FrostedPrimaryDark)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Emergency Kill Switch",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = FrostedTextPrimary)
                        )
                        Text(
                            text = "Hentikan semua penjanaan baharu jika berlaku kecemasan kos API.",
                            style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary, fontSize = 11.sp)
                        )
                    }

                    Switch(
                        checked = emergencyKillSwitch,
                        onCheckedChange = { viewModel.authRepo.toggleEmergencyKillSwitch() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = FrostedAccentCrimson,
                            uncheckedTrackColor = FrostedGlassInput
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // System Health status indicators
                Text(
                    text = "Status Kesihatan Enjin & Pembekal:",
                    style = MaterialTheme.typography.labelMedium.copy(color = FrostedTextPrimary, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HealthStatusRow("Gemini 3.5 AI Song Director", "OPERASIONAL", FrostedAccentEmerald)
                    HealthStatusRow("Lyria Multi-Track DSP Synthesizer", "OPERASIONAL", FrostedAccentEmerald)
                    HealthStatusRow("Voice Profile Formant Engine", "OPERASIONAL", FrostedAccentEmerald)
                    HealthStatusRow("Database & RLS Isolation", "OPERASIONAL", FrostedAccentEmerald)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Immutable Credit Ledger
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = FrostedGlassCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Lejar Transaksi Kredit (Audit Trail)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = FrostedPrimaryDark)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (transactionHistory.isEmpty()) {
                    Text(
                        text = "Belum ada transaksi direkodkan.",
                        style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextMuted)
                    )
                } else {
                    val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                    transactionHistory.take(5).forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = tx.description,
                                    style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextPrimary, fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = sdf.format(Date(tx.timestamp)),
                                    style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextMuted, fontSize = 10.sp)
                                )
                            }
                            Text(
                                text = if (tx.amount > 0) "+${tx.amount} Kr" else "${tx.amount} Kr",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.amount > 0) FrostedAccentEmerald else FrostedAccentCrimson
                                )
                            )
                        }
                        HorizontalDivider(color = FrostedBorderSubtle)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun HealthStatusRow(name: String, status: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary, fontSize = 11.sp))
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall.copy(color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
