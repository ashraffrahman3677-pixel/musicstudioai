package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SongEntity
import com.example.ui.MainViewModel
import com.example.ui.StudioTab
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val songsList by viewModel.songsList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val libraryFilter by viewModel.libraryFilter.collectAsState()
    val selectedLanguage by viewModel.authRepo.selectedLanguage.collectAsState()

    var songToRename by remember { mutableStateOf<SongEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var songToDelete by remember { mutableStateOf<SongEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = if (selectedLanguage == "ms") "Perpustakaan Lagu" else "Music Library",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = FrostedTextPrimary
            )
        )
        Text(
            text = "${songsList.size} ${if (selectedLanguage == "ms") "lagu dihasilkan" else "songs created"}",
            style = MaterialTheme.typography.bodySmall.copy(color = FrostedTextSecondary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Frosted Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = {
                Text(
                    if (selectedLanguage == "ms") "Cari mengikut tajuk, lirik, atau genre..." else "Search by title, lyrics, genre...",
                    color = FrostedTextMuted
                )
            },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = FrostedTextSecondary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = FrostedTextSecondary)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("library_search_field"),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FrostedPrimary,
                unfocusedBorderColor = FrostedBorder,
                focusedTextColor = FrostedTextPrimary,
                unfocusedTextColor = FrostedTextPrimary,
                focusedContainerColor = FrostedGlassCardElevated,
                unfocusedContainerColor = FrostedGlassInput
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Frosted Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = (libraryFilter == "ALL"),
                onClick = { viewModel.setLibraryFilter("ALL") },
                label = { Text(if (selectedLanguage == "ms") "Semua" else "All", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FrostedPrimaryDark,
                    selectedLabelColor = Color.White,
                    containerColor = FrostedGlassCardElevated,
                    labelColor = FrostedTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = (libraryFilter == "ALL"), borderColor = FrostedBorder),
                shape = RoundedCornerShape(14.dp)
            )
            FilterChip(
                selected = (libraryFilter == "FAVORITES"),
                onClick = { viewModel.setLibraryFilter("FAVORITES") },
                label = { Text(if (selectedLanguage == "ms") "Kegemaran" else "Favorites", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FrostedPrimaryDark,
                    selectedLabelColor = Color.White,
                    containerColor = FrostedGlassCardElevated,
                    labelColor = FrostedTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = (libraryFilter == "FAVORITES"), borderColor = FrostedBorder),
                shape = RoundedCornerShape(14.dp)
            )
            FilterChip(
                selected = (libraryFilter == "MY_VOICE"),
                onClick = { viewModel.setLibraryFilter("MY_VOICE") },
                label = { Text(if (selectedLanguage == "ms") "Suara Saya" else "My Voice", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FrostedPrimaryDark,
                    selectedLabelColor = Color.White,
                    containerColor = FrostedGlassCardElevated,
                    labelColor = FrostedTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = (libraryFilter == "MY_VOICE"), borderColor = FrostedBorder),
                shape = RoundedCornerShape(14.dp)
            )
            FilterChip(
                selected = (libraryFilter == "INSTRUMENTAL"),
                onClick = { viewModel.setLibraryFilter("INSTRUMENTAL") },
                label = { Text("Instrumental", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FrostedPrimaryDark,
                    selectedLabelColor = Color.White,
                    containerColor = FrostedGlassCardElevated,
                    labelColor = FrostedTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = (libraryFilter == "INSTRUMENTAL"), borderColor = FrostedBorder),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (songsList.isEmpty()) {
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
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = null,
                            tint = FrostedPrimaryDark,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedLanguage == "ms") "Tiada lagu ditemui" else "No songs found",
                        style = MaterialTheme.typography.bodyLarge.copy(color = FrostedTextSecondary, fontWeight = FontWeight.Medium)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.setTab(StudioTab.CREATE) },
                        colors = ButtonDefaults.buttonColors(containerColor = FrostedPrimaryDark),
                        shape = CircleShape,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(if (selectedLanguage == "ms") "Cipta Lagu Baharu" else "Create New Song", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(songsList, key = { it.id }) { song ->
                    SongCardItem(
                        song = song,
                        onPlay = {
                            viewModel.player.playSong(song)
                            viewModel.setTab(StudioTab.PLAYER)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(song) },
                        onRename = {
                            songToRename = song
                            renameInput = song.title
                        },
                        onDelete = { songToDelete = song },
                        onDownload = {
                            viewModel.downloadSong(song) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Rename Dialog (Frosted Glass)
    if (songToRename != null) {
        AlertDialog(
            onDismissRequest = { songToRename = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = FrostedGlassCardElevated,
            title = { Text("Namakan Semula Lagu", color = FrostedTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tajuk Baru") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FrostedPrimary,
                        unfocusedBorderColor = FrostedBorder,
                        focusedTextColor = FrostedTextPrimary,
                        unfocusedTextColor = FrostedTextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            viewModel.renameSong(songToRename!!, renameInput)
                        }
                        songToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FrostedPrimaryDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { songToRename = null }) {
                    Text("Batal", color = FrostedTextSecondary)
                }
            }
        )
    }

    // Delete Dialog (Frosted Glass)
    if (songToDelete != null) {
        AlertDialog(
            onDismissRequest = { songToDelete = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = FrostedGlassCardElevated,
            title = { Text("Padam Lagu?", color = FrostedTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Adakah anda pasti mahu memadam '${songToDelete!!.title}' dari peranti dan perpustakaan?", color = FrostedTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSong(songToDelete!!)
                        songToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FrostedAccentCrimson),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Padam", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { songToDelete = null }) {
                    Text("Batal", color = FrostedTextSecondary)
                }
            }
        )
    }
}

@Composable
fun SongCardItem(
    song: SongEntity,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDownload: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("song_item_${song.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FrostedGlassCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play Icon Cover Art
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (song.vocalType == "USER_VOICE") FrostedAccentRose else FrostedPrimaryDark
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = FrostedTextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FrostedPrimaryLight
                    ) {
                        Text(
                            text = song.genre,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = FrostedPrimaryDark,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    if (song.vocalType == "USER_VOICE") {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FrostedSecondaryContainer
                        ) {
                            Text(
                                text = "Suara Saya",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = FrostedAccentRose,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = "${song.bpm} BPM • ${song.durationSeconds}s",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = FrostedTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Favorite Button
            IconButton(onClick = { onToggleFavorite() }) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (song.isFavorite) FrostedAccentRose else FrostedTextSecondary
                )
            }

            // More Options Menu
            Box {
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = FrostedTextSecondary
                    )
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier.background(FrostedGlassCardElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("Muat Turun WAV", color = FrostedTextPrimary, fontWeight = FontWeight.Medium) },
                        onClick = {
                            expandedMenu = false
                            onDownload()
                        },
                        leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = FrostedPrimary) }
                    )
                    DropdownMenuItem(
                        text = { Text("Namakan Semula", color = FrostedTextPrimary, fontWeight = FontWeight.Medium) },
                        onClick = {
                            expandedMenu = false
                            onRename()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = FrostedTextSecondary) }
                    )
                    DropdownMenuItem(
                        text = { Text("Padam", color = FrostedAccentCrimson, fontWeight = FontWeight.Medium) },
                        onClick = {
                            expandedMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = FrostedAccentCrimson) }
                    )
                }
            }
        }
    }
}
