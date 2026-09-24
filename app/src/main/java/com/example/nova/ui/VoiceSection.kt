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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.nova.voice.OrbState

@Composable
fun VoiceSection(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val orbState by viewModel.voiceManager.orbState.collectAsState()
    val rmsLevel by viewModel.voiceManager.rmsLevel.collectAsState()
    val transcript by viewModel.voiceManager.transcript.collectAsState()
    val logs by viewModel.terminalLogs.collectAsState()

    var manualTextInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp)
    ) {
        // Voice Header Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NEURAL VOICE TERMINAL",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan
                )
                Text(
                    text = "Acoustic Receptor & Neural TTS Synthesizer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }

            // Visualizer Mic Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (orbState == OrbState.LISTENING) NeonAmber else CyberDarkSurface)
                    .border(1.dp, if (orbState == OrbState.LISTENING) NeonAmber else CyberSurfaceBorder, CircleShape)
                    .clickable {
                        if (orbState == OrbState.LISTENING) {
                            viewModel.voiceManager.stopListening()
                        } else {
                            viewModel.voiceManager.startListening()
                        }
                    }
                    .testTag("voice_toggle_button")
            ) {
                Icon(
                    imageVector = if (orbState == OrbState.LISTENING) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Voice Toggle",
                    tint = if (orbState == OrbState.LISTENING) CyberBlack else NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mini Audio Frequency Bar Visualizer
        AudioSpectrumBar(rmsLevel = rmsLevel, orbState = orbState)

        Spacer(modifier = Modifier.height(16.dp))

        // Speech Terminal Log Console
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TERMINAL TRANSCRIPT STREAM",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "STATUS: ${orbState.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = orbState.primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs) { log ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "[${log.timestamp}] ",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${log.sender}: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = when (log.sender) {
                                    "USER" -> NeonAmber
                                    "NOVA" -> NeonCyan
                                    else -> NeonViolet
                                },
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = log.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextWhite,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Field for Manual Directives
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = manualTextInput,
                onValueChange = { manualTextInput = it },
                placeholder = { Text("Enter directive or voice command...", color = TextMuted) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("voice_manual_text_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CyberSurfaceBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = CyberDarkSurface,
                    unfocusedContainerColor = CyberDarkSurface
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (manualTextInput.isNotBlank()) {
                        viewModel.handleVoiceCommand(manualTextInput)
                        manualTextInput = ""
                        keyboardController?.hide()
                    }
                })
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (manualTextInput.isNotBlank()) {
                        viewModel.handleVoiceCommand(manualTextInput)
                        manualTextInput = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCyan)
                    .testTag("voice_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Command",
                    tint = CyberBlack
                )
            }
        }
    }
}

@Composable
fun AudioSpectrumBar(rmsLevel: Float, orbState: OrbState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CyberDarkSurface)
            .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barCount = 24
        val baseColor = when (orbState) {
            OrbState.IDLE -> NeonCyan.copy(alpha = 0.4f)
            OrbState.LISTENING -> NeonAmber
            OrbState.PROCESSING -> NeonViolet
            OrbState.SPEAKING -> NeonPink
        }

        for (i in 0 until barCount) {
            val factor = kotlin.math.sin((i / barCount.toFloat()) * Math.PI).toFloat()
            val heightPct = if (orbState == OrbState.IDLE) {
                0.2f
            } else {
                (0.2f + (rmsLevel * factor * 0.8f)).coerceIn(0.15f, 1f)
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((24 * heightPct).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(baseColor)
            )
        }
    }
}
