package com.example.nova.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nova.calls.CallScreeningManager
import com.example.nova.location.NovaLocationManager
import com.example.nova.messages.MessageManager
import com.example.nova.music.MusicControlManager
import com.example.nova.permissions.PermissionManager
import com.example.nova.safety.SafetyManager
import com.example.nova.vehicle.VehicleCheckManager
import com.example.nova.vision.VisionManager
import com.example.nova.voice.OrbState
import com.example.nova.voice.VoiceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CyberScreen(val title: String, val badge: String) {
    MAIN("DESKTOP", "AI-01"),
    VOICE("VOICE TERMINAL", "AUDIO"),
    VISION("OPTICAL VISION", "CAM-X"),
    LOCATION("GPS TELEMETRY", "NAV"),
    SAFETY("SHOCK & SOS", "SHOCK"),
    CALLS_MESSAGES("COMMS & SCREEN", "TEL"),
    VEHICLE("REGISTRY CHECK", "DMV"),
    MUSIC("CYBER MEDIA", "AUDIO"),
    PERMISSIONS("SYS SECURITY", "SEC")
}

data class TerminalLog(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String,
    val sender: String, // "USER", "NOVA", "SYSTEM"
    val message: String
)

class NovaViewModel(application: Application) : AndroidViewModel(application) {

    val voiceManager = VoiceManager(application)
    val visionManager = VisionManager()
    val locationManager = NovaLocationManager(application)
    val safetyManager = SafetyManager(application)
    val callScreeningManager = CallScreeningManager()
    val messageManager = MessageManager(application)
    val vehicleCheckManager = VehicleCheckManager()
    val musicControlManager = MusicControlManager(application)
    val permissionManager = PermissionManager(application)

    private val _currentScreen = MutableStateFlow(CyberScreen.MAIN)
    val currentScreen: StateFlow<CyberScreen> = _currentScreen.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<TerminalLog>>(
        listOf(
            TerminalLog(
                timestamp = currentTimestampString(),
                sender = "SYSTEM",
                message = "NOVA Quantum Core v2.4 initialized. All telemetry channels standing by."
            ),
            TerminalLog(
                timestamp = currentTimestampString(),
                sender = "NOVA",
                message = "Greetings. I am Nova. Tap the orb or speak to initiate neural commands."
            )
        )
    )
    val terminalLogs: StateFlow<List<TerminalLog>> = _terminalLogs.asStateFlow()

    private val _aiResponseText = MutableStateFlow("Standing by for audio receptor input.")
    val aiResponseText: StateFlow<String> = _aiResponseText.asStateFlow()

    init {
        voiceManager.onCommandRecognized = { command ->
            handleVoiceCommand(command)
        }
    }

    fun setScreen(screen: CyberScreen) {
        _currentScreen.value = screen
        if (screen == CyberScreen.LOCATION) {
            locationManager.startLocationUpdates()
        }
        if (screen == CyberScreen.PERMISSIONS) {
            permissionManager.refreshPermissions()
        }
    }

    fun handleVoiceCommand(rawInput: String) {
        val input = rawInput.trim()
        if (input.isBlank()) return

        addLog("USER", input)
        voiceManager.setOrbState(OrbState.PROCESSING)

        viewModelScope.launch {
            delay(400) // Brief neural computation pause
            val lower = input.lowercase(Locale.US)
            val response: String

            when {
                lower.contains("where am i") || lower.contains("location") || lower.contains("gps") || lower.contains("coordinates") -> {
                    val tel = locationManager.telemetry.value
                    response = "Hardware GPS fixed. Latitude: ${String.format(Locale.US, "%.4f", tel.latitude)}, Longitude: ${String.format(Locale.US, "%.4f", tel.longitude)}. Location: ${tel.formattedAddress}"
                    _currentScreen.value = CyberScreen.LOCATION
                    locationManager.startLocationUpdates()
                }

                lower.contains("camera") || lower.contains("vision") || lower.contains("scan") || lower.contains("viewfinder") -> {
                    response = "Activating CameraX optical analysis feed. Engaging target HUD."
                    _currentScreen.value = CyberScreen.VISION
                }

                lower.contains("safety") || lower.contains("shock") || lower.contains("g force") || lower.contains("sensor") -> {
                    val g = safetyManager.currentGForce.value
                    response = "Kinetic safety monitor active. Current accelerometer force is ${String.format(Locale.US, "%.2f", g)} G. Guardian protocol armed."
                    _currentScreen.value = CyberScreen.SAFETY
                }

                lower.contains("emergency") || lower.contains("sos") || lower.contains("mayday") || lower.contains("help") -> {
                    response = "CRITICAL EMERGENCY INITIATED. Initiating 10-second guardian countdown. Abort in Safety module if false alarm."
                    _currentScreen.value = CyberScreen.SAFETY
                    safetyManager.startSosCountdown()
                }

                lower.contains("vehicle") || lower.contains("car") || lower.contains("plate") || lower.contains("registration") || lower.contains("vin") -> {
                    val words = input.split(" ")
                    val candidate = words.lastOrNull()?.uppercase() ?: "7NOVA42"
                    response = "Querying official vehicle registration portal for record: $candidate"
                    _currentScreen.value = CyberScreen.VEHICLE
                    vehicleCheckManager.queryRegistration(candidate)
                }

                lower.contains("call") || lower.contains("screen") || lower.contains("spam") || lower.contains("phone") -> {
                    response = "Telecom Call Screening module open. Auto-blocking robocall threats."
                    _currentScreen.value = CyberScreen.CALLS_MESSAGES
                }

                lower.contains("whatsapp") || lower.contains("message") || lower.contains("text") -> {
                    response = "WhatsApp encryption bridge ready. Verifying recipient credentials."
                    _currentScreen.value = CyberScreen.CALLS_MESSAGES
                }

                lower.contains("play") || lower.contains("music") || lower.contains("song") || lower.contains("synthwave") -> {
                    val songQuery = if (lower.startsWith("play ")) input.substring(5) else "cyberpunk synthwave"
                    response = "Searching and dispatching audio playback intent for: $songQuery"
                    _currentScreen.value = CyberScreen.MUSIC
                    musicControlManager.openYouTubeSearch(songQuery)
                }

                lower.contains("status") || lower.contains("system") || lower.contains("diagnostics") -> {
                    val batt = safetyManager.batteryTelemetry.value
                    response = "All systems operational. Battery power at ${batt.levelPercent}%, thermal reading ${batt.temperatureCelsius}°C. All security subroutines verified."
                }

                lower.contains("hello") || lower.contains("hi nova") || lower.contains("who are you") -> {
                    response = "Online and listening. I am Nova, your ambient cybernetic AI assistant. How may I direct system resources?"
                }

                else -> {
                    response = "Command acknowledged: '$input'. Neural heuristic executed. Say 'help', 'location', 'camera', 'music', or 'vehicle' for dedicated functions."
                }
            }

            _aiResponseText.value = response
            addLog("NOVA", response)
            voiceManager.speak(response)
        }
    }

    fun addLog(sender: String, message: String) {
        val newLog = TerminalLog(
            timestamp = currentTimestampString(),
            sender = sender,
            message = message
        )
        _terminalLogs.value = (_terminalLogs.value + newLog).takeLast(25)
    }

    private fun currentTimestampString(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
        locationManager.stopLocationUpdates()
        safetyManager.unregisterSensors()
    }
}
