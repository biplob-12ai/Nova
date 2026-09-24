package com.example.nova.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.nova.location.DistanceVector
import com.example.nova.ui.theme.CyberBlack
import com.example.nova.ui.theme.CyberDarkSurface
import com.example.nova.ui.theme.CyberSurfaceBorder
import com.example.nova.ui.theme.CyberSurfaceCard
import com.example.nova.ui.theme.NeonAmber
import com.example.nova.ui.theme.NeonCyan
import com.example.nova.ui.theme.NeonGreen
import com.example.nova.ui.theme.NeonViolet
import com.example.nova.ui.theme.TextCyan
import com.example.nova.ui.theme.TextGray
import com.example.nova.ui.theme.TextMuted
import com.example.nova.ui.theme.TextWhite
import java.util.Locale

@Composable
fun LocationSection(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val telemetry by viewModel.locationManager.telemetry.collectAsState()
    val vectors by viewModel.locationManager.distanceVectors.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        val granted = map[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                map[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            viewModel.locationManager.startLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.locationManager.startLocationUpdates()
        }
    }

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
                        text = "HARDWARE GPS TELEMETRY",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonCyan
                    )
                    Text(
                        text = "Fused Location Provider & Spatial Vector Triangulation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (telemetry.isLiveFix) NeonGreen.copy(alpha = 0.15f) else NeonAmber.copy(alpha = 0.15f))
                        .border(1.dp, if (telemetry.isLiveFix) NeonGreen else NeonAmber, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (telemetry.isLiveFix) "FIX ACQUIRED" else "STANDBY",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (telemetry.isLiveFix) NeonGreen else NeonAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Primary Coordinates Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("location_primary_card"),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PRECISION COORDINATES",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "LATITUDE", style = MaterialTheme.typography.labelSmall, color = TextGray)
                            Text(
                                text = String.format(Locale.US, "%.6f°", telemetry.latitude),
                                style = MaterialTheme.typography.titleLarge,
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "LONGITUDE", style = MaterialTheme.typography.labelSmall, color = TextGray)
                            Text(
                                text = String.format(Locale.US, "%.6f°", telemetry.longitude),
                                style = MaterialTheme.typography.titleLarge,
                                color = NeonCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary metrics row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurfaceCard)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem("ALTITUDE", "${telemetry.altitudeMeters.toInt()}m")
                        MetricItem("ACCURACY", "±${String.format(Locale.US, "%.1f", telemetry.accuracyMeters)}m")
                        MetricItem("SPEED", "${telemetry.speedKmh.toInt()} km/h")
                        MetricItem("HEADING", "${telemetry.bearingDegrees.toInt()}°")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "RESOLVED GEO-SECTOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = telemetry.formattedAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextWhite,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val mapUri = Uri.parse("geo:${telemetry.latitude},${telemetry.longitude}?q=${telemetry.latitude},${telemetry.longitude}(Nova+Location)")
                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                // Fallback browser
                                val webUri = Uri.parse("https://maps.google.com/?q=${telemetry.latitude},${telemetry.longitude}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = CyberBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LAUNCH MAP HUD SYSTEM", color = CyberBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Distance Vectors to Cyber Waypoints
        item {
            Text(
                text = "SPATIAL DISTANCE VECTORS",
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
        }

        items(vectors) { vec ->
            DistanceVectorRow(vector = vec)
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
        Text(text = value, style = MaterialTheme.typography.labelLarge, color = TextCyan, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun DistanceVectorRow(vector: DistanceVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Direction Arrow
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceCard)
                        .border(1.dp, CyberSurfaceBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(vector.bearingDegrees)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = vector.waypoint.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${vector.waypoint.designation} • Bearing ${vector.bearingDegrees.toInt()}° (${vector.cardinalDirection})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val formattedDistance = if (vector.distanceMeters >= 1000) {
                    String.format(Locale.US, "%.1f km", vector.distanceMeters / 1000f)
                } else {
                    "${vector.distanceMeters.toInt()} m"
                }
                Text(
                    text = formattedDistance,
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "VECTOR LOCK",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen,
                    fontSize = 9.sp
                )
            }
        }
    }
}
