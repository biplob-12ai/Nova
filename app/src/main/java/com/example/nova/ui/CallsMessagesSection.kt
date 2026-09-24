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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.nova.calls.CallDecision
import com.example.nova.calls.ScreenedCall
import com.example.nova.calls.ScreeningRule
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

@Composable
fun CallsMessagesSection(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val screenedCalls by viewModel.callScreeningManager.screenedCalls.collectAsState()
    val rules by viewModel.callScreeningManager.screeningRules.collectAsState()

    var whatsAppPhone by remember { mutableStateOf("+1") }
    var whatsAppText by remember { mutableStateOf("Nova Beacon: Coordinates received. Standby.") }
    var validationWarning by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "COMMS & TELECOM SENTINEL",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan
                )
                Text(
                    text = "Telecom Call Screening & WhatsApp Privacy Bridge",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs: Call Screening vs WhatsApp Bridge
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CyberDarkSurface,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonCyan
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("TELECOM SCREENING", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("WHATSAPP BRIDGE", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // Telecom Call Screening Tab
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Simulator Actions Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "CALL SCREENING SIMULATOR",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.callScreeningManager.simulateIncomingCall("+1 (800) 412-9988", true)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.25f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("TEST SPAM CALL", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.callScreeningManager.simulateIncomingCall("+1 (555) 302-8819", false)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(alpha = 0.25f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("TEST VIP CONTACT", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Screening Rules
                item {
                    Text(
                        text = "ACTIVE TELECOM POLICIES",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(rules) { rule ->
                    RuleRow(rule = rule, onToggle = { viewModel.callScreeningManager.toggleRule(rule.id) })
                }

                // Screened Call Logs
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "RECENT INBOUND CALL INTERCEPTIONS",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(screenedCalls) { call ->
                    ScreenedCallCard(call = call)
                }
            }
        } else {
            // WhatsApp Bridge Tab
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("whatsapp_bridge_card"),
                        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "WHATSAPP CIPHER BRIDGE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Validates recipient E.164 phone formatting and scrubs sensitive credential leaks before dispatching external WhatsApp intent.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGray
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = whatsAppPhone,
                                onValueChange = {
                                    whatsAppPhone = it
                                    val res = viewModel.messageManager.validateMessage(it, whatsAppText)
                                    validationWarning = res.privacyWarning
                                },
                                label = { Text("Recipient Phone (E.164)") },
                                placeholder = { Text("+1 (555) 000-0000") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("whatsapp_phone_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CyberSurfaceBorder,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedContainerColor = CyberSurfaceCard,
                                    unfocusedContainerColor = CyberSurfaceCard
                                ),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = whatsAppText,
                                onValueChange = {
                                    whatsAppText = it
                                    val res = viewModel.messageManager.validateMessage(whatsAppPhone, it)
                                    validationWarning = res.privacyWarning
                                },
                                label = { Text("Message Body") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("whatsapp_text_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CyberSurfaceBorder,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedContainerColor = CyberSurfaceCard,
                                    unfocusedContainerColor = CyberSurfaceCard
                                ),
                                shape = RoundedCornerShape(10.dp),
                                minLines = 3
                            )

                            AnimatedVisibility(visible = validationWarning != null) {
                                Text(
                                    text = validationWarning ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonAmber,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    viewModel.messageManager.launchWhatsAppBridge(whatsAppPhone, whatsAppText)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("whatsapp_send_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = CyberBlack)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("LAUNCH WHATSAPP INTENT", color = CyberBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RuleRow(rule: ScreeningRule, onToggle: () -> Unit) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(text = rule.title, style = MaterialTheme.typography.titleMedium, color = TextWhite)
                Text(text = rule.description, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            Switch(
                checked = rule.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonCyan,
                    checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                    uncheckedTrackColor = CyberSurfaceCard
                )
            )
        }
    }
}

@Composable
fun ScreenedCallCard(call: ScreenedCall) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (call.decision) {
                CallDecision.BLOCK_SPAM -> NeonRed
                CallDecision.ALLOW -> NeonGreen
                else -> NeonAmber
            }
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (call.decision) {
                            CallDecision.BLOCK_SPAM -> Icons.Default.Block
                            CallDecision.ALLOW -> Icons.Default.CheckCircle
                            else -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = when (call.decision) {
                            CallDecision.BLOCK_SPAM -> NeonRed
                            CallDecision.ALLOW -> NeonGreen
                            else -> NeonAmber
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = call.callerName ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = call.decision.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (call.decision) {
                        CallDecision.BLOCK_SPAM -> NeonRed
                        CallDecision.ALLOW -> NeonGreen
                        else -> NeonAmber
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = call.phoneNumber,
                style = MaterialTheme.typography.labelSmall,
                color = TextCyan,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = call.aiScreeningSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )
        }
    }
}
