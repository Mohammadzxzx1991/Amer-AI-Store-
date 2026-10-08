package com.example.ui.agent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SpaceTurquoiseInteractiveRobot(
    modifier: Modifier = Modifier,
    isAr: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Position state for free movement across every millimeter of the screen
    var posX by remember { mutableStateOf(200f) }
    var posY by remember { mutableStateOf(600f) }
    
    val animX = remember { Animatable(200f) }
    val animY = remember { Animatable(600f) }

    // Avatar state (Snapchat-style selfie or emoji)
    var selectedEmoji by remember { mutableStateOf("🤖") }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    var showAvatarStudio by remember { mutableStateOf(false) }
    var robotMessage by remember { mutableStateOf(if (isAr) "أنا رفيقك الفضائي التركوازي المتفاعل 🌌 اضغط أو اسحبني في أي مكان!" else "I'm your interactive Space Turquoise companion 🌌 Tap or drag anywhere!") }
    
    // Photo picker launcher for selfie
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selfieUri = uri
            robotMessage = if (isAr) "رائع! تمت توليد شخصية السيلفي بنجاح! ✨" else "Awesome! Selfie avatar generated successfully! ✨"
        }
    }

    // Floating hover & wave animations
    val infiniteTransition = rememberInfiniteTransition(label = "robot_anim")
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover"
    )
    val handWaveAngle by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave"
    )
    val auraGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        posX = offset.x.coerceIn(50f, size.width.toFloat() - 100f)
                        posY = offset.y.coerceIn(100f, size.height.toFloat() - 150f)
                        coroutineScope.launch {
                            launch { animX.animateTo(posX, tween(450, easing = FastOutSlowInEasing)) }
                            launch { animY.animateTo(posY, tween(450, easing = FastOutSlowInEasing)) }
                        }
                        robotMessage = if (isAr) "أتجه نحو لمستك! 🚀 حرية مطلقة في كل ملم!" else "Sailing to your touch! 🚀 Absolute freedom in every mm!"
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        posX = (posX + dragAmount.x).coerceIn(40f, size.width.toFloat() - 140f)
                        posY = (posY + dragAmount.y).coerceIn(40f, size.height.toFloat() - 200f)
                        coroutineScope.launch {
                            animX.snapTo(posX)
                            animY.snapTo(posY)
                        }
                    }
                )
            }
    ) {
        // Floating Robot Container
        Box(
            modifier = Modifier
                .offset { IntOffset(animX.value.roundToInt(), (animY.value + hoverOffset).roundToInt()) }
                .size(130.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            posX = (posX + dragAmount.x)
                            posY = (posY + dragAmount.y)
                            coroutineScope.launch {
                                animX.snapTo(posX)
                                animY.snapTo(posY)
                            }
                        }
                    )
                }
        ) {
            // Space Turquoise Aura Glow background
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .scale(auraGlowScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00F5D4).copy(alpha = 0.5f),
                                Color(0xFF00E5FF).copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                // Speech Bubble
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0B0F19).copy(alpha = 0.95f),
                    border = BorderStroke(1.5.dp, Color(0xFF00F5D4)),
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .widthIn(max = 210.dp)
                ) {
                    Text(
                        text = robotMessage,
                        color = Color(0xFFE0F7FA),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Robot Full Body Structure (Head, Hands, Torso, Legs)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Left Arm (waving)
                    Box(
                        modifier = Modifier
                            .size(16.dp, 36.dp)
                            .rotate(handWaveAngle)
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFF00F5D4), Color(0xFF00838F))),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Main Robot Body / Torso with Snapchat-style Head
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. Robot Head (Snapchat Bitmoji / Selfie Avatar / Emoji Head)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(10.dp, CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF00F5D4), Color(0xFF7C4DFF), Color(0xFF00E5FF))
                                    ),
                                    shape = CircleShape
                                )
                                .border(2.5.dp, Color.White, CircleShape)
                                .clickable { showAvatarStudio = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selfieUri != null) {
                                AsyncImage(
                                    model = selfieUri,
                                    contentDescription = "Selfie Avatar",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = selectedEmoji,
                                    fontSize = 28.sp
                                )
                            }
                            // Antenna Beacon on top of head
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = (-6).dp)
                                    .size(6.dp)
                                    .background(Color(0xFF00FFFF), CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // 2. Torso with Space Turquoise Reactor Core
                        Box(
                            modifier = Modifier
                                .size(48.dp, 40.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(Color(0xFF00838F), Color(0xFF0B0F19))
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .border(1.5.dp, Color(0xFF00F5D4), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Pulsing Reactor Core
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .scale(auraGlowScale)
                                    .background(Color(0xFF00F5D4), CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // 3. Articulated Legs
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp, 24.dp)
                                    .background(Color(0xFF006064), RoundedCornerShape(5.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp, 24.dp)
                                    .background(Color(0xFF006064), RoundedCornerShape(5.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Right Arm
                    Box(
                        modifier = Modifier
                            .size(16.dp, 36.dp)
                            .rotate(-handWaveAngle)
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFF00F5D4), Color(0xFF00838F))),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
            }
        }
    }

    // Snapchat-Style Selfie Avatar & Emoji Creator Dialog
    if (showAvatarStudio) {
        AlertDialog(
            onDismissRequest = { showAvatarStudio = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Face, contentDescription = null, tint = Color(0xFF00F5D4))
                    Text(
                        text = if (isAr) "استوديو شخصية سناب (Bitmoji & Selfie)" else "Snapchat Avatar & Selfie Studio",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isAr) "اختر إيموجي مميز أو التقط صورة سيلفي لتوليد رأس الروبوت الخاص بك!" else "Pick a custom avatar emoji or take a selfie to generate your robot head!",
                        color = Color(0xFFE0F7FA),
                        fontSize = 13.sp
                    )

                    // Emoji Grid Selector
                    val emojis = listOf("🤖", "🦊", "🦁", "🐯", "👻", "👑", "👽", "😎", "🦄", "🚀", "💎", "⭐")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(emojis.size) { index ->
                            val emoji = emojis[index]
                            Surface(
                                shape = CircleShape,
                                color = if (selectedEmoji == emoji) Color(0xFF00F5D4) else Color(0xFF1E293B),
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable {
                                        selectedEmoji = emoji
                                        selfieUri = null
                                    },
                                tonalElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
                        }
                    }

                    // Selfie Capture Button
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4))
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAr) "التقاط صورة سيلفي من المعرض/الكاميرا" else "Take Selfie from Gallery/Camera",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAvatarStudio = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                ) {
                    Text(if (isAr) "تم حفظ الشخصية" else "Save Avatar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0B0F19),
            tonalElevation = 8.dp
        )
    }
}
