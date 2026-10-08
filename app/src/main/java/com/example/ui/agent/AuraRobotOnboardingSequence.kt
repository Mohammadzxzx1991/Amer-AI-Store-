package com.example.ui.agent

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Data structure representing a Waypoint along AuraRobot's dynamic scanning path
 */
data class ScanWaypoint(
    val time: Float, // 0f to 1f
    val xNorm: Float, // Normalized 0f..1f across screen width
    val yNorm: Float, // Normalized 0f..1f across screen height
    val expression: RobotExpression,
    val isScanning: Boolean,
    val isTalking: Boolean,
    val messageAr: String,
    val messageEn: String,
    val hudTitleAr: String,
    val hudTitleEn: String
)

/**
 * AuraRobotOnboardingSequence
 * Implements an onboarding sequence where the 'AuraRobot' moves dynamically across
 * the screen using a continuous Path transition to simulate scanning the UI
 * before settling into its resting position.
 */
@Composable
fun AuraRobotOnboardingSequence(
    modifier: Modifier = Modifier,
    isAr: Boolean = true,
    onComplete: () -> Unit = {},
    onStartRegistration: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Path transition progress: 0f (entry) -> 1f (settled in resting position)
    val pathProgress = remember { Animatable(0f) }
    var isSettled by remember { mutableStateOf(false) }

    // Multi-waypoint continuous path definition
    val waypoints = remember {
        listOf(
            ScanWaypoint(
                time = 0.0f,
                xNorm = 0.50f,
                yNorm = -0.12f,
                expression = RobotExpression.EXCITED,
                isScanning = false,
                isTalking = false,
                messageAr = "جاري تهيئة رفيقك الآلي أورا...",
                messageEn = "Initializing Aura Robot...",
                hudTitleAr = "بدء تشغيل أورا",
                hudTitleEn = "Aura Initialization"
            ),
            ScanWaypoint(
                time = 0.20f,
                xNorm = 0.50f,
                yNorm = 0.16f,
                expression = RobotExpression.EXCITED,
                isScanning = false,
                isTalking = true,
                messageAr = "مرحباً بك! أنا الروبوت أورا 🤖 رفيقك الذكي. سأقوم بمسح وفحص عناصر الواجهة لك الآن!",
                messageEn = "Welcome! I am Aura 🤖 your cybernetic companion. Scanning the UI interface for you!",
                hudTitleAr = "الترحيب الحركي",
                hudTitleEn = "Aura Greeting"
            ),
            ScanWaypoint(
                time = 0.45f,
                xNorm = 0.22f,
                yNorm = 0.42f,
                expression = RobotExpression.SCANNING,
                isScanning = true,
                isTalking = false,
                messageAr = "فحص مصفوفة الأمان، والتشفير اللامركزي، وهوية المستخدم الذكية... 🔍",
                messageEn = "Scanning security matrix, cryptography, and smart identity... 🔍",
                hudTitleAr = "مسح الأمان والحوكمة",
                hudTitleEn = "Security Scanning"
            ),
            ScanWaypoint(
                time = 0.70f,
                xNorm = 0.78f,
                yNorm = 0.56f,
                expression = RobotExpression.SCANNING,
                isScanning = true,
                isTalking = false,
                messageAr = "تدقيق رادار الأسعار المباشر، وربط الوكلاء الـ 19 بشبكة الخدمات السحابية! ⚡",
                messageEn = "Inspecting Price Radar & syncing 19 AI agents with cloud services! ⚡",
                hudTitleAr = "مسح رادار الأسعار والوكلاء",
                hudTitleEn = "Price Radar Matrix"
            ),
            ScanWaypoint(
                time = 0.88f,
                xNorm = 0.50f,
                yNorm = 0.65f,
                expression = RobotExpression.WINK,
                isScanning = false,
                isTalking = true,
                messageAr = "اكتمل المسح بنجاح 100%! تم التحقق من سلامة كافة أجزاء النظام. ✨",
                messageEn = "UI Scan 100% complete! All platform features and services verified. ✨",
                hudTitleAr = "اكتمال الفحص الشامل",
                hudTitleEn = "Scan Succeeded"
            ),
            ScanWaypoint(
                time = 1.00f,
                xNorm = 0.50f,
                yNorm = 0.18f,
                expression = RobotExpression.HAPPY,
                isScanning = false,
                isTalking = true,
                messageAr = "أنا جاهز تماماً في موقعي الإرشادي! اضغط للتسجيل أو التجول وسأرافقك خطوة بخطوة 🚀",
                messageEn = "Settled in my guide post! Tap to register or explore and I'll assist you 🚀",
                hudTitleAr = "الاستقرار في الموضع الدائم",
                hudTitleEn = "Resting Position"
            )
        )
    }

    // Launch continuous Path transition animation
    LaunchedEffect(Unit) {
        pathProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 8500,
                easing = FastOutSlowInEasing
            )
        )
        isSettled = true
        onComplete()
    }

    // Evaluate current Waypoint state and Interpolate (x, y) along path
    val t = pathProgress.value

    // Find bounding waypoints
    val (wpA, wpB, segProgress) = remember(t) {
        var a = waypoints.first()
        var b = waypoints.last()
        for (i in 0 until waypoints.size - 1) {
            if (t >= waypoints[i].time && t <= waypoints[i + 1].time) {
                a = waypoints[i]
                b = waypoints[i + 1]
                break
            }
        }
        val segDuration = (b.time - a.time).coerceAtLeast(0.001f)
        val p = ((t - a.time) / segDuration).coerceIn(0f, 1f)
        Triple(a, b, p)
    }

    // Smooth cubic Hermite / smoothstep interpolation for coordinates
    val smoothP = segProgress * segProgress * (3f - 2f * segProgress)
    val curNormX = wpA.xNorm + (wpB.xNorm - wpA.xNorm) * smoothP
    val curNormY = wpA.yNorm + (wpB.yNorm - wpA.yNorm) * smoothP

    val robotPosX = curNormX * screenWidthPx
    val robotPosY = curNormY * screenHeightPx

    val activeWaypoint = if (segProgress < 0.5f) wpA else wpB
    val currentExpression = activeWaypoint.expression
    val isScanning = activeWaypoint.isScanning
    val isTalking = activeWaypoint.isTalking
    val speechText = if (isAr) activeWaypoint.messageAr else activeWaypoint.messageEn
    val hudTitle = if (isAr) activeWaypoint.hudTitleAr else activeWaypoint.hudTitleEn

    // Grid Radar Sweep animation
    val infiniteTransition = rememberInfiniteTransition(label = "radar_grid")
    val gridPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "gridPhase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16).copy(alpha = if (isSettled) 0.65f else 0.88f))
            .testTag("aura_robot_onboarding_sequence")
    ) {
        // 1. BACKGROUND HOLOGRAPHIC SCANNER GRID & PATH TRAJECTORY
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Cyber grid pattern
            val gridSize = 45f
            var gx = 0f
            while (gx < w) {
                drawLine(
                    color = Color(0xFF00E676).copy(alpha = 0.04f),
                    start = Offset(gx, 0f),
                    end = Offset(gx, h),
                    strokeWidth = 1f
                )
                gx += gridSize
            }
            var gy = gridPhase % gridSize
            while (gy < h) {
                drawLine(
                    color = Color(0xFF00B0FF).copy(alpha = 0.04f),
                    start = Offset(0f, gy),
                    end = Offset(w, gy),
                    strokeWidth = 1f
                )
                gy += gridSize
            }

            // Draw Dynamic Scanning Path Trajectory
            val path = androidx.compose.ui.graphics.Path().apply {
                waypoints.forEachIndexed { idx, wp ->
                    val px = wp.xNorm * w
                    val py = wp.yNorm * h
                    if (idx == 0) moveTo(px, py)
                    else lineTo(px, py)
                }
            }

            // Draw glowing trajectory track
            drawPath(
                path = path,
                color = Color(0xFF00E676).copy(alpha = 0.15f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), phase = gridPhase * 2)
                )
            )

            // Scanning reticle pulse circle beneath AuraRobot
            if (isScanning) {
                drawCircle(
                    color = Color(0xFFFF1744).copy(alpha = 0.18f),
                    radius = 90f + 15f * sin(gridPhase.toDouble()).toFloat(),
                    center = Offset(robotPosX, robotPosY + 50f)
                )
                drawCircle(
                    color = Color(0xFFFF1744).copy(alpha = 0.45f),
                    radius = 90f + 15f * sin(gridPhase.toDouble()).toFloat(),
                    center = Offset(robotPosX, robotPosY + 50f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            }
        }

        // 2. TOP BAR CONTROLS (HUD Status, Skip, Rescan, Language)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // HUD Status Badge
            Surface(
                color = Color(0xFF131D2E).copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isScanning) Color(0xFFFF1744) else Color(0xFF00E676))
                    )
                    Text(
                        text = hudTitle,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Re-Scan Button
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            isSettled = false
                            pathProgress.snapTo(0f)
                            pathProgress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(8500, easing = FastOutSlowInEasing)
                            )
                            isSettled = true
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00B0FF)),
                    border = BorderStroke(1.dp, Color(0xFF00B0FF).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Re-Scan", modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = if (isAr) "إعادة المسح" else "Re-Scan", fontSize = 11.sp)
                }

                // Skip / Settle Button
                if (!isSettled) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pathProgress.animateTo(1f, animationSpec = tween(400))
                                isSettled = true
                                onComplete()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = if (isAr) "تخطي الفحص" else "Skip",
                            color = Color(0xFF090D16),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. AURAROBOT MOVING ALONG DYNAMIC PATH
        val robotSizeDp = 110.dp
        val robotHalfPx = with(density) { (robotSizeDp / 2).toPx() }

        Box(
            modifier = Modifier
                .offset(
                    x = with(density) { (robotPosX - robotHalfPx).toDp() },
                    y = with(density) { (robotPosY - robotHalfPx).toDp() }
                )
                .size(robotSizeDp)
        ) {
            AuraRobot(
                size = robotSizeDp,
                expression = currentExpression,
                isScanning = isScanning,
                isTalking = isTalking,
                auraColor = if (isScanning) Color(0xFFFF1744) else Color(0xFF00E676),
                secondaryAuraColor = Color(0xFF00B0FF),
                torsoVisible = true
            )
        }

        // 4. REACTIVE SPEECH BUBBLE COMPONENT LINKED TO ROBOT
        val bubbleYOffset = if (curNormY > 0.55f) {
            // Display speech bubble above robot
            (robotPosY - with(density) { 150.dp.toPx() }).coerceAtLeast(with(density) { 70.dp.toPx() })
        } else {
            // Display speech bubble below robot
            robotPosY + with(density) { 75.dp.toPx() }
        }

        AnimatedVisibility(
            visible = t > 0.05f,
            enter = fadeIn(tween(300)) + scaleIn(tween(300)),
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = with(density) { bubbleYOffset.toDp() })
                .padding(horizontal = 24.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10192A).copy(alpha = 0.95f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, if (isScanning) Color(0xFFFF5252).copy(alpha = 0.6f) else Color(0xFF00E676).copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isScanning) Icons.Default.Search else Icons.Default.VolumeUp,
                            contentDescription = "Voice Indicator",
                            tint = if (isScanning) Color(0xFFFF5252) else Color(0xFF00E676),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isAr) "الروبوت أورا (Aura Cyber Companion)" else "Aura Cyber Companion",
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = speechText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    // Settled Action CTA
                    if (isSettled || t >= 0.95f) {
                        Spacer(Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onStartRegistration,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E676)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color(0xFF090D16), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isAr) "بدء التسجيل التفاعلي" else "Start Interactive Signup",
                                    color = Color(0xFF090D16),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            OutlinedButton(
                                onClick = onComplete,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isAr) "تصفح المنصة" else "Explore",
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. BOTTOM STEP / PROGRESS DOTS INDICATOR
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            waypoints.forEachIndexed { index, wp ->
                val isActive = t >= wp.time
                val isCurrent = (index == 0 && t < waypoints[1].time) ||
                        (index > 0 && index < waypoints.size - 1 && t >= wp.time && t < waypoints[index + 1].time) ||
                        (index == waypoints.size - 1 && t >= wp.time)

                Box(
                    modifier = Modifier
                        .size(width = if (isCurrent) 24.dp else 8.dp, height = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                isCurrent -> Color(0xFF00E676)
                                isActive -> Color(0xFF00B0FF)
                                else -> Color.White.copy(alpha = 0.2f)
                            }
                        )
                )
            }
        }
    }
}
