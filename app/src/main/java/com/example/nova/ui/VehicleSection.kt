package com.example.nova.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.nova.ui.theme.CyberBlack
import com.example.nova.ui.theme.CyberDarkSurface
import com.example.nova.ui.theme.CyberSurfaceBorder
import com.example.nova.ui.theme.CyberSurfaceCard
import com.example.nova.ui.theme.NeonAmber
import com.example.nova.ui.theme.NeonCyan
import com.example.nova.ui.theme.NeonGreen
import com.example.nova.ui.theme.NeonPink
import com.example.nova.ui.theme.NeonRed
import com.example.nova.ui.theme.TextCyan
import com.example.nova.ui.theme.TextGray
import com.example.nova.ui.theme.TextMuted
import com.example.nova.ui.theme.TextWhite
import com.example.nova.vehicle.VehicleRecord
import kotlinx.coroutines.launch

@Composable
fun VehicleSection(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    var queryText by remember { mutableStateOf("7NOVA42") }
    var simulateGatewayOutage by remember { mutableStateOf(false) }

    val isQuerying by viewModel.vehicleCheckManager.isQuerying.collectAsState()
    val lastResult by viewModel.vehicleCheckManager.lastResult.collectAsState()
    val queryError by viewModel.vehicleCheckManager.queryError.collectAsState()
    val history by viewModel.vehicleCheckManager.history.collectAsState()

    val scope = rememberCoroutineScope()

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
                        text = "OFFICIAL VEHICLE REGISTRY",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonPink
                    )
                    Text(
                        text = "Department of Motor Vehicles & NCIC Stolen Check",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }

                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = NeonPink,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Search Input Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vehicle_search_card"),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ENTER LICENSE PLATE OR 17-CHAR VIN",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = queryText,
                            onValueChange = { queryText = it.uppercase() },
                            placeholder = { Text("e.g. 7NOVA42 or 9BAD999") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("vehicle_query_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonPink,
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
                                scope.launch {
                                    viewModel.vehicleCheckManager.queryRegistration(queryText, simulateGatewayOutage)
                                }
                            },
                            enabled = !isQuerying,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("vehicle_search_button")
                        ) {
                            if (isQuerying) {
                                CircularProgressIndicator(color = CyberBlack, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null, tint = CyberBlack)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gateway Outage Simulation Toggle (Testing resilience)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "SIMULATE GATEWAY OUTAGE", style = MaterialTheme.typography.labelSmall, color = TextGray)
                            Text(text = "Tests graceful handling of server 503 errors", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                        }
                        Switch(
                            checked = simulateGatewayOutage,
                            onCheckedChange = { simulateGatewayOutage = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonAmber,
                                checkedTrackColor = NeonAmber.copy(alpha = 0.3f),
                                uncheckedTrackColor = CyberSurfaceBorder
                            )
                        )
                    }
                }
            }
        }

        // Query Error / Offline Warning Banner
        item {
            AnimatedVisibility(visible = queryError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NeonAmber.copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = NeonAmber)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = queryError ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextWhite
                        )
                    }
                }
            }
        }

        // Active Vehicle Record Result Card
        item {
            lastResult?.let { record ->
                VehicleDetailCard(record = record)
            }
        }

        // Verification History
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VERIFICATION AUDIT LOG",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.Default.History, contentDescription = null, tint = TextMuted)
            }
        }

        items(history) { record ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            viewModel.vehicleCheckManager.queryRegistration(record.licensePlate)
                        }
                    },
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
                        Text(
                            text = "${record.modelYear} ${record.make} ${record.model}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "PLATE: ${record.licensePlate} • ${record.fuelType}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (record.stolenReported) NeonRed.copy(alpha = 0.2f)
                                else if (record.registrationStatus == "ACTIVE") NeonGreen.copy(alpha = 0.2f)
                                else NeonAmber.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (record.stolenReported) "STOLEN" else record.registrationStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (record.stolenReported) NeonRed
                            else if (record.registrationStatus == "ACTIVE") NeonGreen
                            else NeonAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleDetailCard(record: VehicleRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (record.stolenReported) NeonRed
            else if (record.registrationStatus == "ACTIVE") NeonGreen
            else NeonAmber
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${record.modelYear} ${record.make.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = record.model,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (record.stolenReported) NeonRed
                            else if (record.registrationStatus == "ACTIVE") NeonGreen
                            else NeonAmber
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (record.stolenReported) "NCIC STOLEN FLAG" else "REG: ${record.registrationStatus}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberBlack,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Specs Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceCard)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "LICENSE PLATE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(text = record.licensePlate, style = MaterialTheme.typography.titleMedium, color = NeonCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(text = "COLOR", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(text = record.color, style = MaterialTheme.typography.bodyMedium, color = TextWhite)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "POWERTRAIN", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(text = record.fuelType, style = MaterialTheme.typography.bodyMedium, color = TextCyan)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "CHASSIS VIN: ${record.vin}", style = MaterialTheme.typography.labelSmall, color = TextGray, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "ROAD TAX EXPIRY: ${record.roadTaxExpiry}", style = MaterialTheme.typography.labelSmall, color = TextWhite)
            Text(text = "INSPECTION: ${record.safetyInspectionExpiry}", style = MaterialTheme.typography.labelSmall, color = TextWhite)

            Spacer(modifier = Modifier.height(8.dp))

            // Source verification banner
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Registry: ${record.verifiedRegistrySource}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen,
                    fontSize = 10.sp
                )
            }
        }
    }
}
