package com.example.nova.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nova.ui.theme.CyberBlack
import com.example.nova.ui.theme.CyberDarkSurface
import com.example.nova.ui.theme.CyberSurfaceBorder
import com.example.nova.ui.theme.NeonAmber
import com.example.nova.ui.theme.NeonCyan
import com.example.nova.ui.theme.NeonPink
import com.example.nova.ui.theme.NeonViolet
import com.example.nova.ui.theme.TextCyan
import com.example.nova.ui.theme.TextMuted
import com.example.nova.voice.OrbState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FloatingOrb(
    orbState: OrbState,
    rmsLevel: Float, // 0f..1f audio reactive level
    modifier: Modifier = Modifier,
    sizeDp: Dp = 190.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbTransitions")

    // Continuous rotation angle (0..360 deg)
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (orbState) {
                    OrbState.PROCESSING -> 1800
                    OrbState.LISTENING -> 3500
                    OrbState.SPEAKING -> 2400
                    OrbState.IDLE -> 8000
                },
                easing = LinearEasing
            )
        ),
        label = "OrbRotation"
    )

    // Breathing pulse for outer aura
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbBreath"
    )

    // Wave ripple pulse for speaking
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "WavePhase"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(sizeDp)
                .testTag("floating_ai_orb")
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val baseRadius = this.size.minDimension * 0.28f

                // Reactive scaling based on RMS dB audio input
                val reactiveMultiplier = 1.0f + (rmsLevel * 0.45f)
                val effectiveRadius = baseRadius * reactiveMultiplier

                // Determine primary & secondary colors from state
                val (primaryCol, secondaryCol) = when (orbState) {
                    OrbState.IDLE -> Pair(NeonCyan, NeonViolet)
                    OrbState.LISTENING -> Pair(NeonCyan, NeonAmber)
                    OrbState.PROCESSING -> Pair(NeonViolet, NeonCyan)
                    OrbState.SPEAKING -> Pair(NeonPink, NeonCyan)
                }

                // 1. Outer Ambient Glow Halo
                val glowRadius = baseRadius * 1.6f * breathScale * (1f + rmsLevel * 0.3f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryCol.copy(alpha = 0.35f),
                            secondaryCol.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = center
                )

                // 2. Outer Cyber Gyroscopic HUD Ring
                val ringRadius = baseRadius * 1.35f
                val ringStroke = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 18f, 6f, 18f), rotationAngle)
                )
                drawCircle(
                    color = primaryCol.copy(alpha = 0.6f),
                    radius = ringRadius,
                    center = center,
                    style = ringStroke
                )

                // 3. Counter-rotating inner cyber arcs (especially dynamic during PROCESSING)
                val arcRadius = baseRadius * 1.15f
                val arcSweep = when (orbState) {
                    OrbState.PROCESSING -> 110f
                    OrbState.LISTENING -> 75f
                    OrbState.SPEAKING -> 90f
                    OrbState.IDLE -> 45f
                }
                drawArc(
                    color = secondaryCol.copy(alpha = 0.75f),
                    startAngle = -rotationAngle * 1.4f,
                    sweepAngle = arcSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                    size = Size(arcRadius * 2, arcRadius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = primaryCol.copy(alpha = 0.75f),
                    startAngle = -rotationAngle * 1.4f + 180f,
                    sweepAngle = arcSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                    size = Size(arcRadius * 2, arcRadius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // 4. Central Hologram Core (Radial sphere)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            primaryCol.copy(alpha = 0.85f),
                            secondaryCol.copy(alpha = 0.75f),
                            CyberBlack.copy(alpha = 0.9f)
                        ),
                        center = Offset(center.x - effectiveRadius * 0.2f, center.y - effectiveRadius * 0.2f),
                        radius = effectiveRadius
                    ),
                    radius = effectiveRadius,
                    center = center
                )

                // 5. State-Specific Overlays (Waveform / Particle nodes)
                if (orbState == OrbState.SPEAKING) {
                    // Neural voice waveform ripples
                    for (i in 1..4) {
                        val rippleRad = effectiveRadius + (i * 12.dp.toPx() * (rmsLevel + 0.3f))
                        drawCircle(
                            color = NeonPink.copy(alpha = (0.7f / i) * (rmsLevel + 0.2f)),
                            radius = rippleRad,
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                } else if (orbState == OrbState.LISTENING) {
                    // Acoustic radial audio nodes
                    val nodeCount = 12
                    for (i in 0 until nodeCount) {
                        val angle = (i * (360f / nodeCount) + rotationAngle) * (Math.PI.toFloat() / 180f)
                        val dist = baseRadius * (1.2f + rmsLevel * 0.4f)
                        val nx = center.x + cos(angle) * dist
                        val ny = center.y + sin(angle) * dist
                        drawCircle(
                            color = NeonAmber,
                            radius = (2.5f + rmsLevel * 4f).dp.toPx(),
                            center = Offset(nx, ny)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // State indicator badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(CyberDarkSurface)
                .background(
                    when (orbState) {
                        OrbState.IDLE -> NeonCyan.copy(alpha = 0.08f)
                        OrbState.LISTENING -> NeonAmber.copy(alpha = 0.15f)
                        OrbState.PROCESSING -> NeonViolet.copy(alpha = 0.15f)
                        OrbState.SPEAKING -> NeonPink.copy(alpha = 0.15f)
                    }
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = orbState.statusLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = orbState.primaryColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = orbState.subText,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}
