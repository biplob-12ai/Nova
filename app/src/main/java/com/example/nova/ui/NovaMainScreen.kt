package com.example.nova.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nova.ui.theme.CyberBlack
import com.example.nova.ui.theme.CyberDarkSurface
import com.example.nova.ui.theme.CyberSurfaceBorder
import com.example.nova.ui.theme.CyberSurfaceCard
import com.example.nova.ui.theme.NeonAmber
import com.example.nova.ui.theme.NeonCyan
import com.example.nova.ui.theme.NeonGreen
import com.example.nova.ui.theme.NeonPink
import com.example.nova.ui.theme.NeonRed
import com.example.nova.ui.theme.NeonViolet
import com.example.nova.ui.theme.TextCyan
import com.example.nova.ui.theme.TextGray
import com.example.nova.ui.theme.TextMuted
import com.example.nova.ui.theme.TextWhite
import com.example.nova.voice.OrbState
import java.util.Locale

@Composable
fun NovaMainScreen(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val orbState by viewModel.voiceManager.orbState.collectAsState()
    val rmsLevel by viewModel.voiceManager.rmsLevel.collectAsState()
    val transcript by viewModel.voiceManager.transcript.collectAsState()
    val aiResponse by viewModel.aiResponseText.collectAsState()
    val gpsTelemetry by viewModel.locationManager.telemetry.collectAsState()
    val gForce by viewModel.safetyManager.currentGForce.collectAsState()
    val battery by viewModel.safetyManager.batteryTelemetry.collectAsState()

    val scrollState = rememberScrollState()

    val quickCommands = listOf(
        "Where am I?",
        "Scan Environment",
        "Play Synthwave",
        "Check Vehicle 7NOVA42",
        "Screen Calls",
        "System Status",
        "Emergency SOS"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Cyber Telemetry Header HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberDarkSurface)
                .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NOVA CORE ONLINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "PWR: ${battery.levelPercent}%",
                style = MaterialTheme.typography.labelSmall,
                color = if (battery.levelPercent > 20) NeonCyan else NeonRed
            )

            Text(
                text = "GPS: ${if (gpsTelemetry.isLiveFix) "LOCKED" else "STANDBY"}",
                style = MaterialTheme.typography.labelSmall,
                color = if (gpsTelemetry.isLiveFix) NeonCyan else NeonAmber
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Ambient Floating AI Orb
        FloatingOrb(
            orbState = orbState,
            rmsLevel = rmsLevel,
            sizeDp = 200.dp,
            onClick = {
                if (orbState == OrbState.IDLE) {
                    viewModel.voiceManager.startListening()
                } else if (orbState == OrbState.LISTENING) {
                    viewModel.voiceManager.stopListening()
                } else if (orbState == OrbState.SPEAKING) {
                    viewModel.voiceManager.stopSpeaking()
                }
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Live Transcript & AI Speech Bubble
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_response_card"),
            colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (transcript.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "USER INPUT:",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = transcript,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "NOVA AI:",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = aiResponse,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextCyan,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Quick Command Chips Row
        Text(
            text = "QUICK NEURAL DIRECTIVES",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickCommands) { cmd ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CyberSurfaceCard)
                        .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(20.dp))
                        .clickable { viewModel.handleVoiceCommand(cmd) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cmd,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Telemetry Cards Matrix (2x2 Grid)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: GPS Position
            CyberStatCard(
                title = "GPS TELEMETRY",
                value = "${String.format(Locale.US, "%.3f", gpsTelemetry.latitude)}, ${String.format(Locale.US, "%.3f", gpsTelemetry.longitude)}",
                subtitle = gpsTelemetry.cyberGridCoord,
                icon = Icons.Default.MyLocation,
                accentColor = NeonCyan,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.setScreen(CyberScreen.LOCATION) }
            )

            // Card 2: Kinetic G-Force
            CyberStatCard(
                title = "SHOCK SENSOR",
                value = "${String.format(Locale.US, "%.2f", gForce)} G",
                subtitle = if (gForce > 2.0f) "IMPACT DETECTED" else "NORMAL GRAVITY",
                icon = Icons.Default.Emergency,
                accentColor = if (gForce > 2.0f) NeonRed else NeonViolet,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.setScreen(CyberScreen.SAFETY) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 3: Camera Vision
            CyberStatCard(
                title = "OPTICAL VISION",
                value = "CAM-X HUD",
                subtitle = "Optical target detection",
                icon = Icons.Default.Videocam,
                accentColor = NeonGreen,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.setScreen(CyberScreen.VISION) }
            )

            // Card 4: Vehicle Registry
            CyberStatCard(
                title = "VEHICLE PORTAL",
                value = "DMV REGISTRY",
                subtitle = "Plate & VIN check",
                icon = Icons.Default.DirectionsCar,
                accentColor = NeonPink,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.setScreen(CyberScreen.VEHICLE) }
            )
        }
    }
}

@Composable
fun CyberStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 9.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextGray,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
