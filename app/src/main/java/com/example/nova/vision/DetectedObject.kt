package com.example.nova.vision

import androidx.compose.ui.geometry.Rect

data class DetectedObject(
    val id: String,
    val label: String,
    val category: String, // "Human", "Vehicle", "Cyber Device", "Document", "Obstacle"
    val confidence: Float, // 0.0 to 1.0
    val normalizedBounds: Rect, // 0f..1f for x, y, width, height
    val isVerified: Boolean, // True = hardware sensor or trusted registry verified; False = algorithmic estimate
    val verificationNotes: String, // e.g., "Hardware optical signature match" vs "Heuristic AI classification (unverified)"
    val distanceMeters: Float,
    val timestamp: Long = System.currentTimeMillis()
)
