package com.example.nova.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.nova.ui.theme.NeonPink
import com.example.nova.ui.theme.NeonRed
import com.example.nova.ui.theme.NeonViolet
import com.example.nova.ui.theme.TextCyan
import com.example.nova.ui.theme.TextMuted
import com.example.nova.ui.theme.TextWhite
import com.example.nova.voice.OrbState

@Composable
fun DashboardScreen(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val orbState by viewModel.voiceManager.orbState.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack),
        topBar = {
            CyberTopBar(
                currentScreen = currentScreen,
                orbState = orbState,
                onSelectScreen = { viewModel.setScreen(it) }
            )
        },
        bottomBar = {
            CyberBottomDock(
                currentScreen = currentScreen,
                onSelectScreen = { viewModel.setScreen(it) }
            )
        },
        containerColor = CyberBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                CyberScreen.MAIN -> NovaMainScreen(viewModel = viewModel)
                CyberScreen.VOICE -> VoiceSection(viewModel = viewModel)
                CyberScreen.VISION -> CameraVisionScreen(viewModel = viewModel)
                CyberScreen.LOCATION -> LocationSection(viewModel = viewModel)
                CyberScreen.SAFETY -> SafetySection(viewModel = viewModel)
                CyberScreen.CALLS_MESSAGES -> CallsMessagesSection(viewModel = viewModel)
                CyberScreen.VEHICLE -> VehicleSection(viewModel = viewModel)
                CyberScreen.MUSIC -> MusicSection(viewModel = viewModel)
                CyberScreen.PERMISSIONS -> PermissionsSection(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CyberTopBar(
    currentScreen: CyberScreen,
    orbState: OrbState,
    onSelectScreen: (CyberScreen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberDarkSurface)
            .border(1.dp, CyberSurfaceBorder)
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        // App Title Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "NOVA",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonViolet.copy(alpha = 0.2f))
                        .border(1.dp, NeonViolet, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "CYBER AI HUD",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonViolet,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(orbState.primaryColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = orbState.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = orbState.primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Futuristic Nav Pills Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cyber_nav_pills_row"),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CyberScreen.entries.toTypedArray()) { screen ->
                val isSelected = screen == currentScreen
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) NeonCyan else CyberSurfaceCard)
                        .border(1.dp, if (isSelected) NeonCyan else CyberSurfaceBorder, RoundedCornerShape(20.dp))
                        .clickable { onSelectScreen(screen) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) CyberBlack else TextWhite,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CyberBottomDock(
    currentScreen: CyberScreen,
    onSelectScreen: (CyberScreen) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberDarkSurface)
            .border(1.dp, CyberSurfaceBorder)
            .padding(vertical = 6.dp, horizontal = 10.dp)
            .testTag("cyber_bottom_dock"),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DockIconItem(
            icon = Icons.Default.Home,
            label = "Desk",
            isSelected = currentScreen == CyberScreen.MAIN,
            onClick = { onSelectScreen(CyberScreen.MAIN) }
        )
        DockIconItem(
            icon = Icons.Default.Mic,
            label = "Voice",
            isSelected = currentScreen == CyberScreen.VOICE,
            onClick = { onSelectScreen(CyberScreen.VOICE) }
        )
        DockIconItem(
            icon = Icons.Default.Videocam,
            label = "Vision",
            isSelected = currentScreen == CyberScreen.VISION,
            onClick = { onSelectScreen(CyberScreen.VISION) }
        )
        DockIconItem(
            icon = Icons.Default.MyLocation,
            label = "GPS",
            isSelected = currentScreen == CyberScreen.LOCATION,
            onClick = { onSelectScreen(CyberScreen.LOCATION) }
        )
        DockIconItem(
            icon = Icons.Default.Emergency,
            label = "Safety",
            isSelected = currentScreen == CyberScreen.SAFETY,
            onClick = { onSelectScreen(CyberScreen.SAFETY) }
        )
        DockIconItem(
            icon = Icons.Default.Phone,
            label = "Comms",
            isSelected = currentScreen == CyberScreen.CALLS_MESSAGES,
            onClick = { onSelectScreen(CyberScreen.CALLS_MESSAGES) }
        )
        DockIconItem(
            icon = Icons.Default.DirectionsCar,
            label = "Vehicle",
            isSelected = currentScreen == CyberScreen.VEHICLE,
            onClick = { onSelectScreen(CyberScreen.VEHICLE) }
        )
        DockIconItem(
            icon = Icons.Default.Headphones,
            label = "Media",
            isSelected = currentScreen == CyberScreen.MUSIC,
            onClick = { onSelectScreen(CyberScreen.MUSIC) }
        )
    }
}

@Composable
fun DockIconItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) NeonCyan else TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) NeonCyan else TextMuted,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
