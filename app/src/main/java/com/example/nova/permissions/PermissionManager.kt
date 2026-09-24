package com.example.nova.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PermissionItem(
    val permission: String,
    val title: String,
    val description: String,
    val purpose: String,
    val isGranted: Boolean,
    val isCritical: Boolean
)

class PermissionManager(private val context: Context) {

    private val _permissionsState = MutableStateFlow<List<PermissionItem>>(emptyList())
    val permissionsState: StateFlow<List<PermissionItem>> = _permissionsState.asStateFlow()

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        val list = listOf(
            PermissionItem(
                permission = Manifest.permission.RECORD_AUDIO,
                title = "Acoustic Receptor (Microphone)",
                description = "Enables real-time speech-to-text voice command parsing and RMS wave pulse.",
                purpose = "Neural voice interface",
                isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED,
                isCritical = true
            ),
            PermissionItem(
                permission = Manifest.permission.CAMERA,
                title = "Optical Sensor (CameraX)",
                description = "Enables live viewfinder HUD with spatial detection bounding boxes and lux metering.",
                purpose = "Holographic vision analysis",
                isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
                isCritical = true
            ),
            PermissionItem(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                title = "Global Telemetry (Hardware GPS)",
                description = "Computes high-precision latitude, longitude, bearing vectors, and cyber grid coordinates.",
                purpose = "Spatial waypoint vectors & safety",
                isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED,
                isCritical = false
            ),
            PermissionItem(
                permission = Manifest.permission.VIBRATE,
                title = "Haptic Actuator (Vibrator)",
                description = "Delivers tactile shock warnings and SOS pulse alarms. Normal install-time permission.",
                purpose = "Tactile alert feedback",
                isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED,
                isCritical = false
            )
        )
        _permissionsState.value = list
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
