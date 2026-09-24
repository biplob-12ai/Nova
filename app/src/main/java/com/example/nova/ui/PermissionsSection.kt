package com.example.nova.ui

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nova.permissions.PermissionItem
import com.example.nova.ui.theme.CyberBlack
import com.example.nova.ui.theme.CyberDarkSurface
import com.example.nova.ui.theme.CyberSurfaceBorder
import com.example.nova.ui.theme.NeonAmber
import com.example.nova.ui.theme.NeonCyan
import com.example.nova.ui.theme.NeonGreen
import com.example.nova.ui.theme.NeonRed
import com.example.nova.ui.theme.TextCyan
import com.example.nova.ui.theme.TextGray
import com.example.nova.ui.theme.TextMuted
import com.example.nova.ui.theme.TextWhite

@Composable
fun PermissionsSection(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val permissions by viewModel.permissionManager.permissionsState.collectAsState()

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
                        text = "SECURITY & PERMISSIONS AUDIT",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonCyan
                    )
                    Text(
                        text = "Least-Privilege Principle & Hardware Sandboxing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }

                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Overview Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("permissions_summary_card"),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val grantedCount = permissions.count { it.isGranted }
                    val totalCount = permissions.size

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE ACCESS RIGHTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "$grantedCount OF $totalCount GRANTED",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (grantedCount == totalCount) NeonGreen else NeonAmber,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.permissionManager.refreshPermissions() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberDarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AUDIT", color = NeonCyan, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.permissionManager.openAppSettings() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_settings_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = CyberBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("OPEN SYSTEM APP SETTINGS", color = CyberBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Permission Items List
        item {
            Text(
                text = "SANDBOXED SUBSYSTEMS",
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
        }

        items(permissions) { perm ->
            PermissionItemCard(perm = perm)
        }
    }
}

@Composable
fun PermissionItemCard(perm: PermissionItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (perm.isGranted) NeonGreen.copy(alpha = 0.5f) else NeonAmber.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (perm.isGranted) NeonGreen.copy(alpha = 0.2f) else NeonAmber.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = if (perm.isGranted) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (perm.isGranted) NeonGreen else NeonAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = perm.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (perm.isGranted) NeonGreen.copy(alpha = 0.15f) else NeonAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (perm.isGranted) "AUTHORIZED" else "STANDBY / DENIED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (perm.isGranted) NeonGreen else NeonAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = perm.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row {
                Text(
                    text = "PURPOSE: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 9.sp
                )
                Text(
                    text = perm.purpose,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextCyan,
                    fontSize = 9.sp
                )
            }
        }
    }
}
