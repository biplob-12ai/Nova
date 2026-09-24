package com.example.nova.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nova.music.CyberTrack
import com.example.nova.ui.theme.CyberBlack
import com.example.nova.ui.theme.CyberDarkSurface
import com.example.nova.ui.theme.CyberSurfaceBorder
import com.example.nova.ui.theme.CyberSurfaceCard
import com.example.nova.ui.theme.NeonAmber
import com.example.nova.ui.theme.NeonCyan
import com.example.nova.ui.theme.NeonPink
import com.example.nova.ui.theme.NeonViolet
import com.example.nova.ui.theme.TextCyan
import com.example.nova.ui.theme.TextGray
import com.example.nova.ui.theme.TextMuted
import com.example.nova.ui.theme.TextWhite

@Composable
fun MusicSection(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val currentTrack by viewModel.musicControlManager.currentTrack.collectAsState()
    val isPlaying by viewModel.musicControlManager.isPlaying.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CYBER MEDIA & YOUTUBE DISPATCHER",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonViolet
                    )
                    Text(
                        text = "Hardware AudioManager & Deep Intent Player",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }

                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    tint = NeonViolet,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Active Player Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("media_player_card"),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACTIVE AUDIO DISPATCH",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentTrack.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${currentTrack.artist} • ${currentTrack.genre}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeonCyan
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Transport controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.musicControlManager.dispatchMediaPrevious() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CyberSurfaceCard)
                        ) {
                            Icon(Icons.Default.FastRewind, contentDescription = "Previous", tint = TextWhite)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = { viewModel.musicControlManager.dispatchMediaPlayPause() },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(NeonCyan)
                                .testTag("media_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = CyberBlack,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = { viewModel.musicControlManager.dispatchMediaNext() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CyberSurfaceCard)
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "Next", tint = TextWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Volume adjusters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.musicControlManager.lowerVolume() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceCard),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.VolumeDown, contentDescription = null, tint = TextWhite)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("VOL -", color = TextWhite, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.musicControlManager.raiseVolume() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceCard),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TextWhite)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("VOL +", color = TextWhite, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Custom Search Input
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search song, artist, stream on YouTube...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("music_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = CyberSurfaceBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = CyberSurfaceCard,
                            unfocusedContainerColor = CyberSurfaceCard
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                viewModel.musicControlManager.openYouTubeSearch(searchQuery)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextWhite)
                    }
                }
            }
        }

        // Curated Futuristic Playlists
        item {
            Text(
                text = "CURATED CYBER SOUNDTRACK PRESETS",
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
        }

        items(viewModel.musicControlManager.curatedPresets) { track ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.musicControlManager.selectPreset(track) },
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = track.title, style = MaterialTheme.typography.titleMedium, color = TextWhite, fontWeight = FontWeight.Bold)
                        Text(text = "${track.artist} • ${track.genre}", style = MaterialTheme.typography.labelSmall, color = TextGray)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonViolet.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonViolet)
                    }
                }
            }
        }
    }
}
