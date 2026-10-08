package com.example.ui.agent

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * AnimatedTalkingRobotAssistant Component
 * The essential moving and talking AI robot companion with personality,
 * featuring dynamic floating animations, speech bubbles, and step-by-step guidance
 * during registration and across screen areas.
 */
@Composable
fun AnimatedTalkingRobotAssistant(
    lang: String = "ar",
    customMessage: String? = null
) {
    val isAr = lang == "ar"

    // Infinite animation for robot floating/movement
    val infiniteTransition = rememberInfiniteTransition(label = "robot_float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Rotating personality dialogues
    val messagesAr = listOf(
        "أهلاً بك في أمير للذكاء الاصطناعي! 🤖 أنا روبوتك الذكي المرافق، جاهز لمساعدتك في التسجيل وبدء الجولة الحركية.",
        "سجل حسابك الآن وسأتولى ربطك بشبكة وكلائنا الـ 19 ونظام رادار الأسعار المتقدم!",
        "كل خطوة تخطوها موثقة بسجل الحوكمة والأدلة المشفرة. دعنا نبدأ التسجيل بذكاء!",
        "أنا متحرك، متكلم، وأرافقك في كل مساحات الشاشة لأضمن لك تجربة استثنائية!"
    )
    val messagesEn = listOf(
        "Welcome to Amer AI Store! 🤖 I am your intelligent companion robot, ready to guide your registration.",
        "Sign up now and I'll connect you with our 19 AI agents and Price Radar network!",
        "Every step is secured by our governance ledger. Let's start smart registration!",
        "I'm mobile, conversational, and guiding you across all app screen areas!"
    )

    val currentMessages = if (isAr) messagesAr else messagesEn
    var messageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            messageIndex = (messageIndex + 1) % currentMessages.size
        }
    }

    val activeMessage = customMessage ?: currentMessages[messageIndex]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .offset(y = offsetY.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Aura Robot Avatar with Canvas and Animatable Expressions
        AuraRobot(
            size = 64.dp,
            expression = RobotExpression.TALKING,
            isTalking = true,
            isScanning = false,
            auraColor = Color(0xFF00E676),
            secondaryAuraColor = Color(0xFF00B0FF),
            torsoVisible = false
        )

        // Talking Speech Bubble with Personality
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.4f)),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Talking",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = activeMessage,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * FloatingTalkingRobotCompanion
 * A floating, moving and talking robot avatar designed for MainLayout,
 * with animated floating movement, pulsating aura, speech bubble on tap or auto-cycle,
 * and direct interaction with the AI Master Agent Hub.
 */
@Composable
fun FloatingTalkingRobotCompanion(
    lang: String = "ar",
    onClick: () -> Unit
) {
    val isAr = lang == "ar"
    val infiniteTransition = rememberInfiniteTransition(label = "floating_robot")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.offset(y = floatOffset.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.95f),
            border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.6f)),
            shadowElevation = 6.dp,
            modifier = Modifier
                .padding(bottom = 6.dp)
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (isAr) "أنا رفيقك الذكي 🤖 اضغط للتحدث!" else "I'm your AI Robot 🤖 Tap me!",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .size(62.dp)
                .scale(glowScale)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        listOf(
                            Color(0xFF00E676),
                            Color(0xFF00B0FF),
                            Color(0xFF7C4DFF),
                            Color(0xFF00E676)
                        )
                    )
                )
                .clickable { onClick() }
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI Robot Companion",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = if (isAr) "روبوت AI" else "AI Robot",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
