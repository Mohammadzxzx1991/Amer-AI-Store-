package com.example.ui.agent

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Robot emotional and functional visual expressions
 */
enum class RobotExpression {
    HAPPY,
    SCANNING,
    WINK,
    TALKING,
    EXCITED,
    RESTING
}

/**
 * Reusable AuraRobot Composable
 * Crafted with Jetpack Compose Canvas and Animatable states.
 * Defines the robot's visual identity:
 * - Dynamic energy aura with radial pulse
 * - Antigravity hover thruster with energetic repulsor waves
 * - Futuristic floating cybernetic head & helmet with magnetic ear pods & beacon antenna
 * - Glossy dark visor with specular reflection & subtle scanline matrix
 * - Reactive eye expressions (happy arcs, laser scanning reticle, playful wink, luminous pupils)
 * - Reactive LED mouth (voice equalizer frequency bars, happy smile arc, data packet pulse)
 * - Holographic downward scanning beam with laser plane and data glyphs
 */
@Composable
fun AuraRobot(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    expression: RobotExpression = RobotExpression.HAPPY,
    isScanning: Boolean = false,
    isTalking: Boolean = false,
    auraColor: Color = Color(0xFF00E676),
    secondaryAuraColor: Color = Color(0xFF00B0FF),
    torsoVisible: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    // Animatable States for Organic Personality
    val eyeBlink = remember { Animatable(1f) }
    val mouthWave = remember { Animatable(0.3f) }
    val scanSweep = remember { Animatable(0f) }
    val headTilt = remember { Animatable(0f) }
    val auraPulse = remember { Animatable(0.95f) }
    val antennaBeacon = remember { Animatable(0.4f) }
    val hoverY = remember { Animatable(0f) }

    // Coroutine for Periodic Eye Blinking
    LaunchedEffect(expression) {
        while (true) {
            delay(3200L)
            // Blink: scale eye height to almost 0, then restore
            eyeBlink.animateTo(
                targetValue = 0.08f,
                animationSpec = tween(durationMillis = 90, easing = LinearEasing)
            )
            eyeBlink.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 110, easing = LinearEasing)
            )
        }
    }

    // Coroutine for Talking Mouth Modulation
    LaunchedEffect(isTalking, expression) {
        if (isTalking || expression == RobotExpression.TALKING) {
            while (true) {
                mouthWave.animateTo(
                    targetValue = 0.95f,
                    animationSpec = tween(140, easing = FastOutSlowInEasing)
                )
                mouthWave.animateTo(
                    targetValue = 0.25f,
                    animationSpec = tween(120, easing = FastOutSlowInEasing)
                )
            }
        } else {
            mouthWave.snapTo(0.2f)
        }
    }

    // Coroutine for Scanner Sweep Line
    LaunchedEffect(isScanning, expression) {
        if (isScanning || expression == RobotExpression.SCANNING) {
            while (true) {
                scanSweep.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1100, easing = LinearEasing)
                )
                scanSweep.snapTo(0f)
            }
        } else {
            scanSweep.snapTo(0f)
        }
    }

    // Coroutine for Subtle Floating & Head Tilt
    LaunchedEffect(expression) {
        launch {
            while (true) {
                hoverY.animateTo(
                    targetValue = -5f,
                    animationSpec = tween(1200, easing = FastOutSlowInEasing)
                )
                hoverY.animateTo(
                    targetValue = 5f,
                    animationSpec = tween(1200, easing = FastOutSlowInEasing)
                )
            }
        }
        launch {
            while (true) {
                val targetAngle = when (expression) {
                    RobotExpression.EXCITED -> 6f
                    RobotExpression.WINK -> -5f
                    RobotExpression.TALKING -> 3f
                    else -> 2f
                }
                headTilt.animateTo(targetAngle, animationSpec = tween(1000, easing = FastOutSlowInEasing))
                headTilt.animateTo(-targetAngle, animationSpec = tween(1000, easing = FastOutSlowInEasing))
            }
        }
        launch {
            while (true) {
                auraPulse.animateTo(1.08f, animationSpec = tween(800, easing = LinearEasing))
                auraPulse.animateTo(0.95f, animationSpec = tween(800, easing = LinearEasing))
            }
        }
        launch {
            while (true) {
                antennaBeacon.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
                antennaBeacon.animateTo(0.3f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                enabled = onClick != null,
                interactionSource = interactionSource,
                indication = null
            ) { onClick?.invoke() }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val baseCy = h * 0.45f
            val cy = baseCy + hoverY.value

            // 1. AURA GLOW & ENERGY FIELD
            val pulse = auraPulse.value
            val auraRadius = w * 0.52f * pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        auraColor.copy(alpha = 0.42f * pulse),
                        secondaryAuraColor.copy(alpha = 0.16f * pulse),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = auraRadius
                ),
                radius = auraRadius,
                center = Offset(cx, cy)
            )

            // Outer Orbiting Energy Dust / Ring
            drawCircle(
                color = auraColor.copy(alpha = 0.25f),
                radius = auraRadius * 0.88f,
                center = Offset(cx, cy),
                style = Stroke(
                    width = 1.8f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 16f), phase = scanSweep.value * 50f)
                )
            )

            // 2. HOLOGRAPHIC SCANNING CONE BEAM (If Scanning)
            if (isScanning || expression == RobotExpression.SCANNING) {
                val beamPath = Path().apply {
                    moveTo(cx - w * 0.14f, cy + h * 0.18f)
                    lineTo(cx + w * 0.14f, cy + h * 0.18f)
                    lineTo(cx + w * 0.48f, h * 0.98f)
                    lineTo(cx - w * 0.48f, h * 0.98f)
                    close()
                }

                drawPath(
                    path = beamPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            auraColor.copy(alpha = 0.55f),
                            secondaryAuraColor.copy(alpha = 0.28f),
                            Color.Transparent
                        ),
                        startY = cy + h * 0.18f,
                        endY = h * 0.98f
                    )
                )

                // Laser Sweep Plane Line moving downward
                val sweepY = (cy + h * 0.18f) + (h * 0.80f - (cy + h * 0.18f)) * scanSweep.value
                val sweepSpread = (sweepY - (cy + h * 0.18f)) / (h * 0.80f - (cy + h * 0.18f))
                val sweepHalfWidth = (w * 0.14f) + (w * 0.34f) * sweepSpread

                drawLine(
                    color = Color.White,
                    start = Offset(cx - sweepHalfWidth, sweepY),
                    end = Offset(cx + sweepHalfWidth, sweepY),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = auraColor.copy(alpha = 0.8f),
                    start = Offset(cx - sweepHalfWidth, sweepY),
                    end = Offset(cx + sweepHalfWidth, sweepY),
                    strokeWidth = 5.5f,
                    cap = StrokeCap.Round
                )
            }

            // 3. TORSO & ANTIGRAVITY HOVER THRUSTER (If visible)
            if (torsoVisible) {
                val torsoTop = cy + h * 0.24f
                val torsoWidth = w * 0.42f
                val torsoHeight = h * 0.20f
                val torsoLeft = cx - torsoWidth / 2f

                // Hover Repulsor Energy Ring
                val thrusterY = torsoTop + torsoHeight + 2f
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            secondaryAuraColor.copy(alpha = 0.75f),
                            auraColor.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = Offset(cx, thrusterY),
                        radius = w * 0.22f
                    ),
                    topLeft = Offset(cx - w * 0.20f, thrusterY - 4f),
                    size = Size(w * 0.40f, 14f)
                )

                // Torso Cybernetic Chassis
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF243042), Color(0xFF131B26), Color(0xFF0B1017))
                    ),
                    topLeft = Offset(torsoLeft, torsoTop),
                    size = Size(torsoWidth, torsoHeight),
                    cornerRadius = CornerRadius(14f, 14f)
                )
                // Torso Accent Border
                drawRoundRect(
                    color = auraColor.copy(alpha = 0.45f),
                    topLeft = Offset(torsoLeft, torsoTop),
                    size = Size(torsoWidth, torsoHeight),
                    cornerRadius = CornerRadius(14f, 14f),
                    style = Stroke(width = 1.5f)
                )

                // Central Arc Reactor Core
                drawCircle(
                    color = Color(0xFF0B1017),
                    radius = 9f,
                    center = Offset(cx, torsoTop + torsoHeight * 0.48f)
                )
                drawCircle(
                    color = auraColor,
                    radius = 6f * pulse,
                    center = Offset(cx, torsoTop + torsoHeight * 0.48f)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5f,
                    center = Offset(cx, torsoTop + torsoHeight * 0.48f)
                )
            }

            // 4. FLOATING HEAD & HELMET WITH TILT
            withTransform({
                rotate(degrees = headTilt.value, pivot = Offset(cx, cy))
            }) {
                val headWidth = w * 0.62f
                val headHeight = h * 0.46f
                val headLeft = cx - headWidth / 2f
                val headTop = cy - headHeight / 2f

                // TOP ANTENNA & BEACON ORB
                val antennaTopY = headTop - h * 0.12f
                drawLine(
                    brush = Brush.verticalGradient(listOf(auraColor, Color(0xFF64748B))),
                    start = Offset(cx, headTop),
                    end = Offset(cx, antennaTopY),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                // Beacon Halo
                drawCircle(
                    color = auraColor.copy(alpha = 0.5f * antennaBeacon.value),
                    radius = 9f * antennaBeacon.value,
                    center = Offset(cx, antennaTopY)
                )
                // Beacon Light Core
                drawCircle(
                    color = Color(0xFFE0F2FE),
                    radius = 4.5f,
                    center = Offset(cx, antennaTopY)
                )

                // SIDE MAGNETIC EAR PODS (Left & Right)
                val podWidth = w * 0.08f
                val podHeight = headHeight * 0.45f
                val podTop = cy - podHeight / 2f

                // Left Pod
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF334155))),
                    topLeft = Offset(headLeft - podWidth * 0.7f, podTop),
                    size = Size(podWidth, podHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawCircle(
                    color = secondaryAuraColor,
                    radius = 3.5f,
                    center = Offset(headLeft - podWidth * 0.2f, cy)
                )

                // Right Pod
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))),
                    topLeft = Offset(headLeft + headWidth - podWidth * 0.3f, podTop),
                    size = Size(podWidth, podHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawCircle(
                    color = secondaryAuraColor,
                    radius = 3.5f,
                    center = Offset(headLeft + headWidth + podWidth * 0.2f, cy)
                )

                // HEAD CHASSIS (Sleek Cyber Helmet Armor)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3B485C),
                            Color(0xFF1E293B),
                            Color(0xFF0F172A)
                        )
                    ),
                    topLeft = Offset(headLeft, headTop),
                    size = Size(headWidth, headHeight),
                    cornerRadius = CornerRadius(headWidth * 0.28f, headWidth * 0.28f)
                )

                // Specular Light Sheen along Top Bevel
                val sheenPath = Path().apply {
                    moveTo(headLeft + headWidth * 0.20f, headTop + 2.5f)
                    lineTo(headLeft + headWidth * 0.80f, headTop + 2.5f)
                }
                drawPath(
                    path = sheenPath,
                    color = Color.White.copy(alpha = 0.55f),
                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                )

                // Outer Neon Trim Ring
                drawRoundRect(
                    brush = Brush.sweepGradient(
                        listOf(auraColor, secondaryAuraColor, auraColor),
                        center = Offset(cx, cy)
                    ),
                    topLeft = Offset(headLeft, headTop),
                    size = Size(headWidth, headHeight),
                    cornerRadius = CornerRadius(headWidth * 0.28f, headWidth * 0.28f),
                    style = Stroke(width = 1.8f)
                )

                // 5. DEEP OLED GLASS VISOR
                val visorMargin = w * 0.055f
                val visorWidth = headWidth - visorMargin * 2
                val visorHeight = headHeight - visorMargin * 2
                val visorLeft = headLeft + visorMargin
                val visorTop = headTop + visorMargin

                // Dark Visor Glass
                drawRoundRect(
                    color = Color(0xFF050811),
                    topLeft = Offset(visorLeft, visorTop),
                    size = Size(visorWidth, visorHeight),
                    cornerRadius = CornerRadius(visorWidth * 0.22f, visorWidth * 0.22f)
                )

                // Subtle Visor Scanline Matrix (Horizontal micro lines)
                val lineStep = 4.5f
                var yScan = visorTop + 2f
                while (yScan < visorTop + visorHeight - 2f) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.035f),
                        start = Offset(visorLeft + 4f, yScan),
                        end = Offset(visorLeft + visorWidth - 4f, yScan),
                        strokeWidth = 1f
                    )
                    yScan += lineStep
                }

                // Curved Glass Specular Highlight across Top-Left
                val glassGleam = Path().apply {
                    moveTo(visorLeft + 8f, visorTop + 6f)
                    lineTo(visorLeft + visorWidth * 0.45f, visorTop + 6f)
                    lineTo(visorLeft + visorWidth * 0.20f, visorTop + visorHeight * 0.45f)
                    lineTo(visorLeft + 8f, visorTop + visorHeight * 0.20f)
                    close()
                }
                drawPath(
                    path = glassGleam,
                    brush = Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        start = Offset(visorLeft, visorTop),
                        end = Offset(visorLeft + visorWidth * 0.4f, visorTop + visorHeight * 0.4f)
                    )
                )

                // 6. REACTIVE EYE EXPRESSIONS (DRAWN ON CANVAS)
                val eyeY = cy - headHeight * 0.06f
                val eyeSpacing = headWidth * 0.22f
                val eyeScale = eyeBlink.value

                when (expression) {
                    RobotExpression.HAPPY -> {
                        // Upward cute curved digital smile eyes: ^  ^
                        val eyeRadius = 9f
                        val leftEyePath = Path().apply {
                            moveTo(cx - eyeSpacing - eyeRadius, eyeY + 4f * eyeScale)
                            quadraticBezierTo(
                                cx - eyeSpacing,
                                eyeY - 8f * eyeScale,
                                cx - eyeSpacing + eyeRadius,
                                eyeY + 4f * eyeScale
                            )
                        }
                        val rightEyePath = Path().apply {
                            moveTo(cx + eyeSpacing - eyeRadius, eyeY + 4f * eyeScale)
                            quadraticBezierTo(
                                cx + eyeSpacing,
                                eyeY - 8f * eyeScale,
                                cx + eyeSpacing + eyeRadius,
                                eyeY + 4f * eyeScale
                            )
                        }
                        drawPath(leftEyePath, color = auraColor, style = Stroke(width = 3.6f, cap = StrokeCap.Round))
                        drawPath(rightEyePath, color = auraColor, style = Stroke(width = 3.6f, cap = StrokeCap.Round))
                    }

                    RobotExpression.SCANNING -> {
                        // Concentric laser scanning reticle with active sweep line
                        val reticleRadius = 10f * eyeScale
                        // Left eye reticle
                        drawCircle(color = Color(0xFFFF1744).copy(alpha = 0.3f), radius = reticleRadius, center = Offset(cx - eyeSpacing, eyeY))
                        drawCircle(color = Color(0xFFFF1744), radius = reticleRadius, center = Offset(cx - eyeSpacing, eyeY), style = Stroke(width = 2f))
                        drawCircle(color = Color.White, radius = 3.5f, center = Offset(cx - eyeSpacing, eyeY))
                        // Right eye reticle
                        drawCircle(color = Color(0xFFFF1744).copy(alpha = 0.3f), radius = reticleRadius, center = Offset(cx + eyeSpacing, eyeY))
                        drawCircle(color = Color(0xFFFF1744), radius = reticleRadius, center = Offset(cx + eyeSpacing, eyeY), style = Stroke(width = 2f))
                        drawCircle(color = Color.White, radius = 3.5f, center = Offset(cx + eyeSpacing, eyeY))

                        // Crosshair ticks
                        drawLine(Color(0xFFFF5252), Offset(cx - eyeSpacing - reticleRadius - 3f, eyeY), Offset(cx - eyeSpacing + reticleRadius + 3f, eyeY), 1.5f)
                        drawLine(Color(0xFFFF5252), Offset(cx + eyeSpacing - reticleRadius - 3f, eyeY), Offset(cx + eyeSpacing + reticleRadius + 3f, eyeY), 1.5f)
                    }

                    RobotExpression.WINK -> {
                        // Left eye playful wink arch (^), right eye horizontal wink dash (-)
                        val eyeRadius = 9f
                        val leftEyePath = Path().apply {
                            moveTo(cx - eyeSpacing - eyeRadius, eyeY + 4f)
                            quadraticBezierTo(cx - eyeSpacing, eyeY - 8f, cx - eyeSpacing + eyeRadius, eyeY + 4f)
                        }
                        drawPath(leftEyePath, color = auraColor, style = Stroke(width = 3.6f, cap = StrokeCap.Round))
                        // Right eye wink line
                        drawLine(
                            color = secondaryAuraColor,
                            start = Offset(cx + eyeSpacing - eyeRadius, eyeY),
                            end = Offset(cx + eyeSpacing + eyeRadius, eyeY),
                            strokeWidth = 3.8f,
                            cap = StrokeCap.Round
                        )
                    }

                    RobotExpression.EXCITED -> {
                        // Radiant starburst diamond eyes
                        val eyeRadius = 9f * eyeScale
                        drawCircle(color = auraColor, radius = eyeRadius, center = Offset(cx - eyeSpacing, eyeY))
                        drawCircle(color = Color.White, radius = 3.5f, center = Offset(cx - eyeSpacing, eyeY))

                        drawCircle(color = auraColor, radius = eyeRadius, center = Offset(cx + eyeSpacing, eyeY))
                        drawCircle(color = Color.White, radius = 3.5f, center = Offset(cx + eyeSpacing, eyeY))

                        // Star sparkle points
                        drawCircle(color = Color.White, radius = 2f, center = Offset(cx - eyeSpacing + 3f, eyeY - 3f))
                        drawCircle(color = Color.White, radius = 2f, center = Offset(cx + eyeSpacing + 3f, eyeY - 3f))
                    }

                    RobotExpression.RESTING -> {
                        // Relaxed horizontal gentle curves: ~  ~
                        val eyeRadius = 8f
                        drawLine(
                            color = Color(0xFF64748B),
                            start = Offset(cx - eyeSpacing - eyeRadius, eyeY),
                            end = Offset(cx - eyeSpacing + eyeRadius, eyeY),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFF64748B),
                            start = Offset(cx + eyeSpacing - eyeRadius, eyeY),
                            end = Offset(cx + eyeSpacing + eyeRadius, eyeY),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }

                    else -> {
                        // TALKING or DEFAULT: Luminous open digital oval eyes
                        val eyeRadiusX = 8.5f
                        val eyeRadiusY = 10f * eyeScale
                        drawOval(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, auraColor),
                                center = Offset(cx - eyeSpacing, eyeY),
                                radius = eyeRadiusX
                            ),
                            topLeft = Offset(cx - eyeSpacing - eyeRadiusX, eyeY - eyeRadiusY),
                            size = Size(eyeRadiusX * 2f, eyeRadiusY * 2f)
                        )
                        drawOval(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, auraColor),
                                center = Offset(cx + eyeSpacing, eyeY),
                                radius = eyeRadiusX
                            ),
                            topLeft = Offset(cx + eyeSpacing - eyeRadiusX, eyeY - eyeRadiusY),
                            size = Size(eyeRadiusX * 2f, eyeRadiusY * 2f)
                        )
                    }
                }

                // 7. REACTIVE DIGITAL LED MOUTH
                val mouthY = cy + headHeight * 0.16f
                if (isTalking || expression == RobotExpression.TALKING) {
                    // Audio Equalizer Spectrum Bars (5 bars fluctuating)
                    val barWidth = 3f
                    val barSpacing = 4.5f
                    val heights = listOf(
                        0.4f * mouthWave.value,
                        0.8f * mouthWave.value,
                        1.0f * mouthWave.value,
                        0.7f * mouthWave.value,
                        0.5f * mouthWave.value
                    )
                    val totalMouthWidth = (5 * barWidth) + (4 * barSpacing)
                    val startX = cx - totalMouthWidth / 2f

                    heights.forEachIndexed { index, hFactor ->
                        val barHeight = 3f + (14f * hFactor)
                        val bx = startX + index * (barWidth + barSpacing)
                        drawRoundRect(
                            color = auraColor,
                            topLeft = Offset(bx, mouthY - barHeight / 2f),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(1.5f, 1.5f)
                        )
                    }
                } else if (expression == RobotExpression.HAPPY || expression == RobotExpression.WINK || expression == RobotExpression.EXCITED) {
                    // Glowing curved smile arc
                    val smilePath = Path().apply {
                        moveTo(cx - 10f, mouthY)
                        quadraticBezierTo(cx, mouthY + 7f, cx + 10f, mouthY)
                    }
                    drawPath(
                        path = smilePath,
                        color = secondaryAuraColor,
                        style = Stroke(width = 2.8f, cap = StrokeCap.Round)
                    )
                } else if (expression == RobotExpression.SCANNING) {
                    // Digital data packet dots
                    val dotRadius = 2f
                    for (i in -2..2) {
                        val alpha = if (i == 0) 1f else 0.5f
                        drawCircle(
                            color = Color(0xFFFF5252).copy(alpha = alpha),
                            radius = dotRadius,
                            center = Offset(cx + (i * 7f), mouthY)
                        )
                    }
                } else {
                    // Calm neat LED line
                    drawLine(
                        color = secondaryAuraColor.copy(alpha = 0.7f),
                        start = Offset(cx - 8f, mouthY),
                        end = Offset(cx + 8f, mouthY),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
