package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.StudioTab
import com.example.ui.components.FrostedBackgroundContainer
import com.example.ui.components.GenerationProgressOverlay
import com.example.ui.components.PersistentPlayerBar
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIMusicStudioTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val generationState by viewModel.generationState.collectAsState()
    val currentSong by viewModel.player.currentSong.collectAsState()
    val selectedLanguage by viewModel.authRepo.selectedLanguage.collectAsState()

    FrostedBackgroundContainer {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Floating persistent player bar (shown if a song is loaded and we are not on the player screen)
                    if (currentSong != null && currentTab != StudioTab.PLAYER) {
                        PersistentPlayerBar(
                            playerManager = viewModel.player,
                            onExpandPlayer = { viewModel.setTab(StudioTab.PLAYER) }
                        )
                    }

                    // Frosted Glass Main Studio Navigation Bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = FrostedBorder,
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            )
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        color = FrostedGlassNavBar,
                        tonalElevation = 6.dp,
                        shadowElevation = 12.dp
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            contentColor = FrostedTextPrimary,
                            tonalElevation = 0.dp,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            NavigationBarItem(
                                selected = (currentTab == StudioTab.CREATE),
                                onClick = { viewModel.setTab(StudioTab.CREATE) },
                                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Create") },
                                label = { Text(if (selectedLanguage == "ms") "Cipta" else "Create", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = FrostedPrimaryDark,
                                    selectedTextColor = FrostedPrimaryDark,
                                    unselectedIconColor = FrostedTextMuted,
                                    unselectedTextColor = FrostedTextMuted,
                                    indicatorColor = FrostedPrimaryLight
                                ),
                                modifier = Modifier.testTag("nav_tab_create")
                            )

                            NavigationBarItem(
                                selected = (currentTab == StudioTab.LIBRARY),
                                onClick = { viewModel.setTab(StudioTab.LIBRARY) },
                                icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                                label = { Text(if (selectedLanguage == "ms") "Lagu" else "Library", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = FrostedPrimaryDark,
                                    selectedTextColor = FrostedPrimaryDark,
                                    unselectedIconColor = FrostedTextMuted,
                                    unselectedTextColor = FrostedTextMuted,
                                    indicatorColor = FrostedPrimaryLight
                                ),
                                modifier = Modifier.testTag("nav_tab_library")
                            )

                            NavigationBarItem(
                                selected = (currentTab == StudioTab.MY_VOICE),
                                onClick = { viewModel.setTab(StudioTab.MY_VOICE) },
                                icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = "My Voice") },
                                label = { Text(if (selectedLanguage == "ms") "Suara" else "Voice", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = FrostedPrimaryDark,
                                    selectedTextColor = FrostedPrimaryDark,
                                    unselectedIconColor = FrostedTextMuted,
                                    unselectedTextColor = FrostedTextMuted,
                                    indicatorColor = FrostedPrimaryLight
                                ),
                                modifier = Modifier.testTag("nav_tab_my_voice")
                            )

                            NavigationBarItem(
                                selected = (currentTab == StudioTab.PLAYER),
                                onClick = { viewModel.setTab(StudioTab.PLAYER) },
                                icon = { Icon(Icons.Default.GraphicEq, contentDescription = "Studio Player") },
                                label = { Text(if (selectedLanguage == "ms") "Pemain" else "Player", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = FrostedPrimaryDark,
                                    selectedTextColor = FrostedPrimaryDark,
                                    unselectedIconColor = FrostedTextMuted,
                                    unselectedTextColor = FrostedTextMuted,
                                    indicatorColor = FrostedPrimaryLight
                                ),
                                modifier = Modifier.testTag("nav_tab_player")
                            )

                            NavigationBarItem(
                                selected = (currentTab == StudioTab.ADMIN),
                                onClick = { viewModel.setTab(StudioTab.ADMIN) },
                                icon = { Icon(Icons.Default.Tune, contentDescription = "Admin") },
                                label = { Text(if (selectedLanguage == "ms") "Akaun" else "Admin", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = FrostedPrimaryDark,
                                    selectedTextColor = FrostedPrimaryDark,
                                    unselectedIconColor = FrostedTextMuted,
                                    unselectedTextColor = FrostedTextMuted,
                                    indicatorColor = FrostedPrimaryLight
                                ),
                                modifier = Modifier.testTag("nav_tab_admin")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    StudioTab.CREATE -> CreateScreen(viewModel = viewModel)
                    StudioTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
                    StudioTab.MY_VOICE -> VoiceProfilesScreen(viewModel = viewModel)
                    StudioTab.PLAYER -> StudioPlayerScreen(viewModel = viewModel)
                    StudioTab.ADMIN -> AdminProfileScreen(viewModel = viewModel)
                }

                // Generation Progress Modal Dialog
                GenerationProgressOverlay(
                    state = generationState,
                    onDismiss = { viewModel.generationRepo.resetState() }
                )
            }
        }
    }
}
