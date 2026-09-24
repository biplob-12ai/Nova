package com.example.nova.voice

import androidx.compose.ui.graphics.Color
import com.example.nova.ui.theme.NeonAmber
import com.example.nova.ui.theme.NeonCyan
import com.example.nova.ui.theme.NeonPink
import com.example.nova.ui.theme.NeonViolet

enum class OrbState(
    val statusLabel: String,
    val subText: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    IDLE(
        statusLabel = "SYSTEM ONLINE",
        subText = "Awaiting command",
        primaryColor = NeonCyan,
        secondaryColor = NeonViolet
    ),
    LISTENING(
        statusLabel = "AUDIO RECEPTOR ACTIVE",
        subText = "Listening to speech...",
        primaryColor = NeonCyan,
        secondaryColor = NeonAmber
    ),
    PROCESSING(
        statusLabel = "NEURAL COMPUTING",
        subText = "Evaluating voice telemetry...",
        primaryColor = NeonViolet,
        secondaryColor = NeonCyan
    ),
    SPEAKING(
        statusLabel = "SYNTHESIZING SPEECH",
        subText = "Voice transmission active",
        primaryColor = NeonPink,
        secondaryColor = NeonViolet
    )
}
