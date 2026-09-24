package com.example.nova.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.nova.ui.theme.CyberBlack
import com.example.nova.ui.theme.CyberDarkSurface
import com.example.nova.ui.theme.CyberSurfaceBorder
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
import com.example.nova.vision.DetectedObject
import java.util.concurrent.Executors

@Composable
fun CameraVisionScreen(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val telemetry by viewModel.visionManager.telemetry.collectAsState()
    val detectedObjects by viewModel.visionManager.detectedObjects.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var cameraControl: Camera? by remember { mutableStateOf(null) }
    var isTorchOn by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        if (hasCameraPermission) {
            // CameraX Viewfinder Preview
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(Executors.newSingleThreadExecutor(), viewModel.visionManager)
                            }

                        try {
                            cameraProvider.unbindAll()
                            cameraControl = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("CameraVision", "Use case binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("camerax_viewfinder")
            )

            // Holographic HUD Canvas Overlay
            HolographicHudOverlay(
                detectedObjects = detectedObjects,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission fallback message
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Camera Permission Required",
                        tint = NeonAmber,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "OPTICAL HARDWARE RECEPTOR LOCKED",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Camera permission is required to analyze live optical targets and render holographic HUD.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("AUTHORIZE CAMERA", color = CyberBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Top Telemetry Bar Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CyberDarkSurface.copy(alpha = 0.85f))
                .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CAM-X OPTICAL HUD",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "FPS: ${telemetry.fps} | LUX: ${telemetry.luxEstimated.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextCyan,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row {
                // Torch toggle
                IconButton(
                    onClick = {
                        isTorchOn = !isTorchOn
                        cameraControl?.cameraControl?.enableTorch(isTorchOn)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isTorchOn) NeonAmber else CyberSurfaceBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Flashlight",
                        tint = if (isTorchOn) CyberBlack else TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Lens switch
                IconButton(
                    onClick = {
                        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Bottom Detected Target Overviews
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(
                text = "ACQUIRED TARGET VECTORS (${detectedObjects.size})",
                style = MaterialTheme.typography.labelSmall,
                color = TextCyan,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(detectedObjects) { obj ->
                    TargetInfoCard(obj = obj)
                }
            }
        }
    }
}

@Composable
fun TargetInfoCard(obj: DetectedObject) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberDarkSurface.copy(alpha = 0.9f)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (obj.isVerified) NeonGreen else NeonAmber
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = obj.id,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
                // Verified Fact vs Unverified Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (obj.isVerified) Icons.Default.Verified else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (obj.isVerified) NeonGreen else NeonAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (obj.isVerified) "VERIFIED FACT" else "ESTIMATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (obj.isVerified) NeonGreen else NeonAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = obj.label,
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Dist: ~${obj.distanceMeters}m | Conf: ${(obj.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = TextCyan,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = obj.verificationNotes,
                style = MaterialTheme.typography.labelSmall,
                color = TextGray,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun HolographicHudOverlay(
    detectedObjects: List<DetectedObject>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Center reticle crosshairs
        val cx = w / 2f
        val cy = h / 2f
        val reticleRadius = 40.dp.toPx()

        drawCircle(
            color = NeonCyan.copy(alpha = 0.4f),
            radius = reticleRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 1.dp.toPx())
        )

        // Crosshair lines
        drawLine(
            color = NeonCyan.copy(alpha = 0.5f),
            start = Offset(cx - reticleRadius - 15f, cy),
            end = Offset(cx - reticleRadius + 15f, cy),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = NeonCyan.copy(alpha = 0.5f),
            start = Offset(cx + reticleRadius - 15f, cy),
            end = Offset(cx + reticleRadius + 15f, cy),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = NeonCyan.copy(alpha = 0.5f),
            start = Offset(cx, cy - reticleRadius - 15f),
            end = Offset(cx, cy - reticleRadius + 15f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = NeonCyan.copy(alpha = 0.5f),
            start = Offset(cx, cy + reticleRadius - 15f),
            end = Offset(cx, cy + reticleRadius + 15f),
            strokeWidth = 2.dp.toPx()
        )

        // Draw Bounding Boxes for detected objects
        detectedObjects.forEach { obj ->
            val rectLeft = obj.normalizedBounds.left * w
            val rectTop = obj.normalizedBounds.top * h
            val rectWidth = obj.normalizedBounds.width * w
            val rectHeight = obj.normalizedBounds.height * h

            val color = if (obj.isVerified) NeonGreen else NeonCyan

            // Bounding box frame
            drawRect(
                color = color.copy(alpha = 0.7f),
                topLeft = Offset(rectLeft, rectTop),
                size = Size(rectWidth, rectHeight),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f))
                )
            )

            // Corner brackets
            val cornerLen = 20f
            // Top Left
            drawLine(color, Offset(rectLeft, rectTop), Offset(rectLeft + cornerLen, rectTop), 3.dp.toPx())
            drawLine(color, Offset(rectLeft, rectTop), Offset(rectLeft, rectTop + cornerLen), 3.dp.toPx())
            // Top Right
            drawLine(color, Offset(rectLeft + rectWidth, rectTop), Offset(rectLeft + rectWidth - cornerLen, rectTop), 3.dp.toPx())
            drawLine(color, Offset(rectLeft + rectWidth, rectTop), Offset(rectLeft + rectWidth, rectTop + cornerLen), 3.dp.toPx())
            // Bottom Left
            drawLine(color, Offset(rectLeft, rectTop + rectHeight), Offset(rectLeft + cornerLen, rectTop + rectHeight), 3.dp.toPx())
            drawLine(color, Offset(rectLeft, rectTop + rectHeight), Offset(rectLeft, rectTop + rectHeight - cornerLen), 3.dp.toPx())
            // Bottom Right
            drawLine(color, Offset(rectLeft + rectWidth, rectTop + rectHeight), Offset(rectLeft + rectWidth - cornerLen, rectTop + rectHeight), 3.dp.toPx())
            drawLine(color, Offset(rectLeft + rectWidth, rectTop + rectHeight), Offset(rectLeft + rectWidth, rectTop + rectHeight - cornerLen), 3.dp.toPx())
        }
    }
}
