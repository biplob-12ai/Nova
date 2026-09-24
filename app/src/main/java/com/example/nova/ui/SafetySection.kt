package com.example.nova.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nova.safety.ShockEvent
import com.example.nova.ui.theme.CyberBlack
import com.example.nova.ui.theme.CyberDarkSurface
import com.example.nova.ui.theme.CyberSurfaceBorder
import com.example.nova.ui.theme.CyberSurfaceCard
import com.example.nova.ui.theme.NeonAmber
import com.example.nova.ui.theme.NeonCyan
import com.example.nova.ui.theme.NeonGreen
import com.example.nova.ui.theme.NeonRed
import com.example.nova.ui.theme.NeonViolet
import com.example.nova.ui.theme.TextCyan
import com.example.nova.ui.theme.TextGray
import com.example.nova.ui.theme.TextMuted
import com.example.nova.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SafetySection(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val currentG by viewModel.safetyManager.currentGForce.collectAsState()
    val peakG by viewModel.safetyManager.maxGForce.collectAsState()
    val thresholdG by viewModel.safetyManager.shockThresholdG.collectAsState()
    val shocks by viewModel.safetyManager.shockHistory.collectAsState()
    val isSosActive by viewModel.safetyManager.isSosCountdownActive.collectAsState()
    val sosSeconds by viewModel.safetyManager.sosCountdownSeconds.collectAsState()
    val battery by viewModel.safetyManager.batteryTelemetry.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SAFETY GUARDIAN & SHOCK METER",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonRed
                    )
                    Text(
                        text = "Hardware Accelerometer & Crash Impact Sentinel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }

                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // 2. Emergency SOS Alert Banner / Countdown
        item {
            AnimatedVisibility(visible = isSosActive) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sos_active_banner"),
                    colors = CardDefaults.cardColors(containerColor = NeonRed.copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, NeonRed),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = NeonRed,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "EMERGENCY GUARDIAN ARMED",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dispatching emergency 911 telemetry in $sosSeconds seconds",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.safetyManager.abortSosCountdown() },
                            colors = ButtonDefaults.buttonColors(containerColor = TextWhite),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("ABORT FALSE ALARM", color = CyberBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Live Accelerometer G-Force Gauge Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shock_meter_card"),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACCELEROMETER SHOCK TELEMETRY",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        IconButton(
                            onClick = { viewModel.safetyManager.resetPeakG() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset Peak",
                                tint = TextGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(text = "CURRENT FORCE", style = MaterialTheme.typography.labelSmall, color = TextGray)
                            Text(
                                text = String.format(Locale.US, "%.2f G", currentG),
                                style = MaterialTheme.typography.displayMedium,
                                color = if (currentG >= thresholdG) NeonRed else NeonCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "PEAK RECORDED", style = MaterialTheme.typography.labelSmall, color = TextGray)
                            Text(
                                text = String.format(Locale.US, "%.2f G", peakG),
                                style = MaterialTheme.typography.titleLarge,
                                color = NeonAmber,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // G-Force Meter Bar (0 to 5G normalized)
                    LinearProgressIndicator(
                        progress = { (currentG / 5.0f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (currentG >= thresholdG) NeonRed else NeonCyan,
                        trackColor = CyberSurfaceCard
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Shock Threshold Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CRITICAL SHOCK THRESHOLD",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f G", thresholdG),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = thresholdG,
                        onValueChange = { viewModel.safetyManager.setThreshold(it) },
                        valueRange = 1.5f..5.5f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonAmber,
                            activeTrackColor = NeonAmber,
                            inactiveTrackColor = CyberSurfaceBorder
                        )
                    )

                    // Manual SOS Trigger Button
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.safetyManager.startSosCountdown() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Emergency, contentDescription = null, tint = TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TRIGGER EMERGENCY SOS GUARDIAN", color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Battery & Hardware Telemetry
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = NeonGreen)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "BATTERY", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "${battery.levelPercent}% ${if (battery.isCharging) "(CHG)" else ""}",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextWhite,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Thermostat, contentDescription = null, tint = NeonAmber)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "TEMPERATURE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "${battery.temperatureCelsius}°C",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextWhite,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = NeonViolet)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "HAPTICS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "ARMED",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 5. Recent Shock Events Log
        item {
            Text(
                text = "RECENT IMPACT LOGS (${shocks.size})",
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
        }

        if (shocks.isEmpty()) {
            item {
                Text(
                    text = "No kinetic shock events logged. Hardware sensor running smoothly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        } else {
            items(shocks.reversed()) { shock ->
                ShockLogRow(shock = shock)
            }
        }
    }
}

@Composable
fun ShockLogRow(shock: ShockEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(shock.timestamp))
                Text(
                    text = "$timeStr • Severity: ${shock.severity}",
                    style = MaterialTheme.typography.labelSmall,
                    color = when (shock.severity) {
                        "CRITICAL" -> NeonRed
                        "MODERATE" -> NeonAmber
                        else -> NeonCyan
                    },
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Vector X:${String.format(Locale.US, "%.1f", shock.x)}, Y:${String.format(Locale.US, "%.1f", shock.y)}, Z:${String.format(Locale.US, "%.1f", shock.z)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = String.format(Locale.US, "%.2f G", shock.magnitudeG),
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
