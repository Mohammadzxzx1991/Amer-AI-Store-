package com.example.ui.visionx

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

/**
 * CameraX Lifecycle Integrated Preview, ImageAnalysis & Frame Capture for Vision X Shopping Lens
 */
@Composable
fun ShoppingLensCameraXView(
    onFrameCaptured: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashEnabled by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var hasCameraError by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var isAutoAnalyzing by remember { mutableStateOf(false) }

    // Animated scanning beam effect
    val infiniteTransition = rememberInfiniteTransition(label = "scan_beam")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_y"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F172A))
    ) {
        if (!hasCameraPermission) {
            // Permission Request Card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "تفعيل مستشعر عدسة Vision X",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        "يتطلب مسح المنتجات بالذكاء الاصطناعي إذن الوصول إلى الكاميرا لالتقاط الإطارات ومطابقتها عبر Gemini Vision.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_request_camera_perm")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("منح إذن الكاميرا", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (!hasCameraError) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .setTargetRotation(previewView.display?.rotation ?: android.view.Surface.ROTATION_0)
                                .build()

                            imageCapture = capture

                            // Real-time ImageAnalysis pipeline for live Gemini frames
                            var lastFrameAnalyzedTime = 0L
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            val analyzerExecutor = Executors.newSingleThreadExecutor()
                            imageAnalysis.setAnalyzer(analyzerExecutor) { imageProxy ->
                                val now = System.currentTimeMillis()
                                if (isAutoAnalyzing && now - lastFrameAnalyzedTime > 2500L) {
                                    lastFrameAnalyzedTime = now
                                    val bitmap = imageProxyToBitmap(imageProxy)
                                    if (bitmap != null) {
                                        ContextCompat.getMainExecutor(ctx).execute {
                                            onFrameCaptured(bitmap)
                                        }
                                    }
                                }
                                imageProxy.close()
                            }

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture,
                                imageAnalysis
                            )
                            cameraControl = camera.cameraControl
                        } catch (e: Exception) {
                            Log.e("ShoppingLensCamera", "CameraX binding failed: ${e.message}")
                            hasCameraError = true
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // High-fidelity fallback for emulators without camera hardware
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "وضع المحاكاة البصري الذكي (Lens Simulator)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        "اضغط على زر التقاط الإطار بالأسفل لإرسال الإطار إلى Gemini Vision مباشرة",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Viewfinder Targeting Box & Scanning Ray
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(200.dp)
                .border(2.dp, Color(0xFF38BDF8).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
        ) {
            // Scanning beam
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .offset(y = (200 * scanProgress).dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFF38BDF8),
                                Color(0xFF60A5FA),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Reticle Corners
            Text("•", color = Color(0xFF38BDF8), fontSize = 24.sp, modifier = Modifier.align(Alignment.TopStart).padding(4.dp))
            Text("•", color = Color(0xFF38BDF8), fontSize = 24.sp, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
            Text("•", color = Color(0xFF38BDF8), fontSize = 24.sp, modifier = Modifier.align(Alignment.BottomStart).padding(4.dp))
            Text("•", color = Color(0xFF38BDF8), fontSize = 24.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp))
        }

        // Camera Controls Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isAutoAnalyzing) Color(0xFF10B981) else Color(0xFF38BDF8))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isAutoAnalyzing) "Gemini Live Stream ⚡" else "CameraX Ready",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Auto Stream Analysis Toggle
                IconButton(
                    onClick = { isAutoAnalyzing = !isAutoAnalyzing },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("btn_toggle_auto_stream")
                ) {
                    Icon(
                        if (isAutoAnalyzing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Auto Stream",
                        tint = if (isAutoAnalyzing) Color(0xFF10B981) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Flashlight toggle
                IconButton(
                    onClick = {
                        flashEnabled = !flashEnabled
                        cameraControl?.enableTorch(flashEnabled)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = if (flashEnabled) Color(0xFFFACC15) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Lens switch
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.FlipCameraAndroid,
                        contentDescription = "Switch Camera",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Bottom Capture Shutter Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Surface(
                onClick = {
                    try {
                        val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vibrator?.vibrate(android.os.VibrationEffect.createOneShot(45, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(45)
                        }
                    } catch (_: Exception) {}

                    if (isCapturing) return@Surface
                    isCapturing = true

                    val capture = imageCapture
                    if (capture != null && !hasCameraError && hasCameraPermission) {
                        val executor = Executors.newSingleThreadExecutor()
                        capture.takePicture(
                            executor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                    val bitmap = imageProxyToBitmap(imageProxy)
                                    imageProxy.close()
                                    ContextCompat.getMainExecutor(context).execute {
                                        isCapturing = false
                                        if (bitmap != null) {
                                            onFrameCaptured(bitmap)
                                        } else {
                                            onFrameCaptured(createSyntheticSampleFrame("Sidr Honey Frame"))
                                        }
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("ShoppingLensCamera", "Capture error: ${exception.message}")
                                    ContextCompat.getMainExecutor(context).execute {
                                        isCapturing = false
                                        onFrameCaptured(createSyntheticSampleFrame("Organic Grocery Item"))
                                    }
                                }
                            }
                        )
                    } else {
                        // Fallback sample frame for emulator/sandbox
                        isCapturing = false
                        onFrameCaptured(createSyntheticSampleFrame("Sidr Honey Frame"))
                    }
                },
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(4.dp, Color(0xFF38BDF8)),
                modifier = Modifier
                    .size(64.dp)
                    .testTag("btn_camera_shutter")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = Color(0xFF38BDF8),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Camera,
                            contentDescription = "Capture Frame",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Converts CameraX ImageProxy to Android Bitmap with proper orientation
 */
fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    return try {
        val bitmap = imageProxy.toBitmap()
        if (imageProxy.imageInfo.rotationDegrees != 0) {
            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    } catch (e: Exception) {
        try {
            val buffer = imageProxy.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e2: Exception) {
            Log.e("ShoppingLensCamera", "Failed to convert ImageProxy: ${e2.message}")
            null
        }
    }
}

/**
 * Generates a high-quality synthetic frame representation for testing
 */
fun createSyntheticSampleFrame(label: String = "Vision X Shopping Frame"): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(15, 23, 42)
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, 400f, 400f, paint)

    val circlePaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(2, 132, 199)
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(200f, 200f, 120f, circlePaint)

    val textPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 24f
        textAlign = android.graphics.Paint.Align.CENTER
    }
    canvas.drawText(label, 200f, 205f, textPaint)
    return bitmap
}
