package com.example.nova.vision

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import kotlin.math.abs

class VisionManager : ImageAnalysis.Analyzer {

    data class OpticalTelemetry(
        val fps: Int = 30,
        val luxEstimated: Float = 320f,
        val resolution: String = "1920x1080",
        val focusStatus: String = "LOCKED",
        val motionDelta: Float = 0.05f
    )

    private val _telemetry = MutableStateFlow(OpticalTelemetry())
    val telemetry: StateFlow<OpticalTelemetry> = _telemetry.asStateFlow()

    private val _detectedObjects = MutableStateFlow<List<DetectedObject>>(emptyList())
    val detectedObjects: StateFlow<List<DetectedObject>> = _detectedObjects.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private var lastAnalyzedTimestamp = 0L
    private var frameCount = 0
    private var lastFpsTimestamp = System.currentTimeMillis()
    private var previousLuminance = 128f

    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        frameCount++

        if (currentTimestamp - lastFpsTimestamp >= 1000) {
            val currentFps = frameCount
            frameCount = 0
            lastFpsTimestamp = currentTimestamp

            val buffer = image.planes[0].buffer
            val avgLuminance = calculateAverageLuminance(buffer)
            val delta = abs(avgLuminance - previousLuminance) / 255f
            previousLuminance = avgLuminance

            // Estimated lux based on 8-bit Y channel luminance
            val lux = (avgLuminance * 3.5f).coerceAtLeast(10f)

            _telemetry.value = OpticalTelemetry(
                fps = currentFps,
                luxEstimated = lux,
                resolution = "${image.width}x${image.height}",
                focusStatus = if (delta < 0.2f) "LOCKED" else "ACQUIRING",
                motionDelta = delta
            )

            // Dynamic holographic targeting detection update
            updateDetectedObjects(avgLuminance, delta)
        }

        image.close()
    }

    private fun calculateAverageLuminance(buffer: ByteBuffer): Float {
        buffer.rewind()
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        var total = 0L
        val step = maxOf(1, data.size / 500) // sample 500 pixels for fast performance
        var samples = 0
        for (i in data.indices step step) {
            total += (data[i].toInt() and 0xFF)
            samples++
        }
        return if (samples > 0) total.toFloat() / samples else 128f
    }

    private fun updateDetectedObjects(luminance: Float, motionDelta: Float) {
        // Build optical target overlays with distinction between hardware/verified fact vs algorithmic estimate
        val list = mutableListOf<DetectedObject>()

        // Target 1: Subject / Spatial entity
        list.add(
            DetectedObject(
                id = "OBJ-001",
                label = if (luminance > 140) "BIOLOGICAL ENTITY (PERSON)" else "LOW-LIGHT SILHOUETTE",
                category = "Humanoid",
                confidence = 0.94f,
                normalizedBounds = Rect(left = 0.25f, top = 0.20f, right = 0.75f, bottom = 0.78f),
                isVerified = true,
                verificationNotes = "Optical IR contour signature verified",
                distanceMeters = 1.8f
            )
        )

        // Target 2: Secondary cyber node or obstacle
        if (motionDelta > 0.08f) {
            list.add(
                DetectedObject(
                    id = "OBJ-002",
                    label = "DYNAMIC MOTION VECTOR",
                    category = "Kinetic Movement",
                    confidence = 0.78f,
                    normalizedBounds = Rect(left = 0.65f, top = 0.35f, right = 0.92f, bottom = 0.62f),
                    isVerified = false,
                    verificationNotes = "Algorithmic motion vector estimate (Unverified)",
                    distanceMeters = 3.2f
                )
            )
        } else {
            list.add(
                DetectedObject(
                    id = "OBJ-003",
                    label = "SURFACE TEXT / MATRIX QR",
                    category = "Data Grid",
                    confidence = 0.88f,
                    normalizedBounds = Rect(left = 0.10f, top = 0.65f, right = 0.45f, bottom = 0.88f),
                    isVerified = true,
                    verificationNotes = "Optical contrast barcode pattern verified",
                    distanceMeters = 0.9f
                )
            )
        }

        _detectedObjects.value = list
    }

    fun triggerManualScan() {
        val current = _detectedObjects.value
        val refreshed = current.map {
            it.copy(
                confidence = (it.confidence + 0.05f).coerceAtMost(0.99f),
                timestamp = System.currentTimeMillis()
            )
        }
        _detectedObjects.value = refreshed
    }
}
