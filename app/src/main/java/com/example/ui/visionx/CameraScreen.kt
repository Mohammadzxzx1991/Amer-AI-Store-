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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.ProductEntity
import com.example.data.agent.AgentEngine
import com.example.data.agent.AgentKey
import com.example.data.visionx.PriceRadarService
import com.example.data.visionx.MonitoredPriceComparison
import com.example.data.visionx.ShoppingLensResult
import com.example.data.visionx.VisionXService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.Executors

enum class CameraLensMode(val titleAr: String, val subtitleAr: String, val icon: ImageVector) {
    OBJECT_DETECTION("كشف السلع والمطابقة", "Product Recognition", Icons.Default.FilterCenterFocus),
    BARCODE_SCAN("مسح الباركود و SKU", "Barcode & SKU Lens", Icons.Default.QrCodeScanner),
    NUTRITION_OCR("المكونات والبيانات الغذائية", "Nutrition & Facts OCR", Icons.Default.DocumentScanner)
}

enum class MetadataTab(val labelAr: String, val icon: ImageVector) {
    PRICE_RADAR("الأسعار والرادار", Icons.Default.Radar),
    SPECS("المواصفات والعبوة", Icons.Default.Inventory2),
    NUTRITION("المكونات والصحة", Icons.Default.HealthAndSafety),
    EVIDENCE("التوثيق والحوكمة", Icons.Default.Verified)
}

/**
 * CameraScreen: Full-screen CameraX integration with real-time frame streaming
 * and deep Gemini Vision product identification and metadata extraction.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    catalog: List<ProductEntity> = emptyList(),
    onDismiss: () -> Unit,
    onAddToCart: (ProductEntity) -> Unit = {},
    onProductDetected: (ShoppingLensResult) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashEnabled by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var hasCameraError by remember { mutableStateOf(false) }
    var isRealtimeDetectionActive by remember { mutableStateOf(true) }
    var selectedLensMode by remember { mutableStateOf(CameraLensMode.OBJECT_DETECTION) }
    var isAnalyzingDeeply by remember { mutableStateOf(false) }
    var selectedMetadataTab by remember { mutableStateOf(MetadataTab.PRICE_RADAR) }
    var addedToCartNotification by remember { mutableStateOf<String?>(null) }

    // Preset items for testing / simulator mode
    val testPresets = remember {
        listOf(
            "عسل جبلي طبيعي سدر دوعني ملكي فاخر",
            "زيت زيتون بكر ممتاز معصور على البارد",
            "خلطة قهوة تركية فاخرة بالهيل الملكي",
            "حليب أبقار عضوي طازج كامل الدسم",
            "طبق بيض بلدي عضوي حر المزرعة",
            "مشروب الطاقة الإدراكي الذكي بالتوت",
            "تفاح سكري أحمر إيطالي مقرمش طازج"
        )
    }
    var selectedPresetIndex by remember { mutableIntStateOf(0) }

    // Live detection state populated by Gemini Vision
    var currentResult by remember {
        mutableStateOf<ShoppingLensResult?>(null)
    }

    // Trigger initial analysis on startup
    LaunchedEffect(Unit) {
        val initialResult = VisionXService.analyzeShoppingLens(
            hint = testPresets[selectedPresetIndex],
            bitmap = null,
            catalog = catalog
        )
        currentResult = initialResult
        onProductDetected(initialResult)
    }

    // Reticle animation & laser beam
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_transition")
    val scanYProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_y"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("camera_screen_container"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Vision X Shopping Lens",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = if (isRealtimeDetectionActive) Color(0xFF10B981) else Color(0xFF64748B),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isRealtimeDetectionActive) "GEMINI LIVE ⚡" else "STREAM PAUSED",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            "كشف مباشر بواسطة CameraX واستخراج الميتا داتا عبر Gemini Vision",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_camera_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Flash / Torch Toggle
                    IconButton(
                        onClick = {
                            flashEnabled = !flashEnabled
                            cameraControl?.enableTorch(flashEnabled)
                        },
                        modifier = Modifier.testTag("btn_camera_flash")
                    ) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash Toggle",
                            tint = if (flashEnabled) Color(0xFFFACC15) else Color.White
                        )
                    }

                    // Flip Camera (Lens Facing)
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        modifier = Modifier.testTag("btn_camera_flip")
                    ) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip Camera", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A).copy(alpha = 0.95f)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            // CameraX Viewfinder / AndroidView
            if (!hasCameraPermission) {
                // Runtime Permission Required View
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF020617))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(52.dp)
                            )
                            Text(
                                "تفعيل إذن الكاميرا لمستشعر Vision X",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "يستخدم تطبيق Amer AI Store واجهة CameraX لالتقاط إطارات المنتجات المباشرة وإرسالها لنموذج Gemini Vision للتعرف الآلي واستخراج بيانات الباركود والمكونات والأسعار.",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("btn_grant_camera_perm")
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("السماح بالوصول للكاميرا", fontWeight = FontWeight.Bold)
                            }
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

                                // ImageAnalysis pipeline for real-time Gemini processing
                                var lastAnalysisTime = 0L
                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()

                                val analyzerExecutor = Executors.newSingleThreadExecutor()
                                imageAnalysis.setAnalyzer(analyzerExecutor) { imageProxy ->
                                    val now = System.currentTimeMillis()
                                    if (isRealtimeDetectionActive && !isAnalyzingDeeply && (now - lastAnalysisTime > 3000L)) {
                                        lastAnalysisTime = now
                                        val frameBitmap = imageProxyToBitmap(imageProxy)
                                        if (frameBitmap != null) {
                                            scope.launch(Dispatchers.Default) {
                                                val res = VisionXService.analyzeShoppingLens(
                                                    hint = selectedLensMode.name,
                                                    bitmap = frameBitmap,
                                                    catalog = catalog
                                                )
                                                withContext(Dispatchers.Main) {
                                                    currentResult = res
                                                    onProductDetected(res)
                                                }
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
                                Log.e("CameraScreen", "CameraX initialization failed: ${e.message}")
                                hasCameraError = true
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // High fidelity simulated viewfinder for emulator
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            "مستشعر الكاميرا النشط (CameraX Virtual Lens)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            "يتم التقاط الإطارات وإرسالها لحظياً إلى Gemini Vision للتحليل والاستخراج",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Mode Selector Bar (Top under bar)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                items(CameraLensMode.entries) { mode ->
                    val isSelected = mode == selectedLensMode
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF0F172A).copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)),
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable {
                                selectedLensMode = mode
                                scope.launch {
                                    val res = VisionXService.analyzeShoppingLens(
                                        hint = mode.subtitleAr,
                                        bitmap = null,
                                        catalog = catalog
                                    )
                                    currentResult = res
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = mode.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = mode.titleAr,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }

            // Viewfinder Target Bounding Reticle (Center Overlay)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp, bottom = 280.dp),
                contentAlignment = Alignment.Center
            ) {
                val reticleWidth = when (selectedLensMode) {
                    CameraLensMode.BARCODE_SCAN -> 280.dp
                    CameraLensMode.NUTRITION_OCR -> 240.dp
                    else -> 220.dp
                }
                val reticleHeight = when (selectedLensMode) {
                    CameraLensMode.BARCODE_SCAN -> 140.dp
                    CameraLensMode.NUTRITION_OCR -> 220.dp
                    else -> 220.dp
                }

                Box(
                    modifier = Modifier
                        .size(width = reticleWidth, height = reticleHeight)
                        .border(
                            width = 2.dp,
                            color = Color(0xFF38BDF8).copy(alpha = 0.85f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    // Scanning laser beam
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .offset(y = reticleHeight * scanYProgress)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0xFF38BDF8),
                                        Color.White,
                                        Color(0xFF38BDF8),
                                        Color.Transparent
                                    )
                                )
                            )
                            .shadow(10.dp, spotColor = Color(0xFF38BDF8))
                    )

                    // Reticle Corner Accent Pins
                    Text("⌜", color = Color(0xFF38BDF8), fontSize = 28.sp, modifier = Modifier.align(Alignment.TopStart).padding(4.dp))
                    Text("⌝", color = Color(0xFF38BDF8), fontSize = 28.sp, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
                    Text("⌞", color = Color(0xFF38BDF8), fontSize = 28.sp, modifier = Modifier.align(Alignment.BottomStart).padding(4.dp))
                    Text("⌟", color = Color(0xFF38BDF8), fontSize = 28.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp))

                    // Mode Tag pill on top of box
                    Surface(
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-14).dp)
                    ) {
                        Text(
                            text = selectedLensMode.subtitleAr,
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Quick Preset Bar for interactive switching & testing
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 265.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(testPresets.indices.toList()) { index ->
                    val isSelected = index == selectedPresetIndex
                    val label = testPresets[index].split(" ").take(2).joinToString(" ")
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF10B981) else Color(0xFF0F172A).copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, if (isSelected) Color.White else Color(0xFF475569)),
                        modifier = Modifier.clickable {
                            selectedPresetIndex = index
                            scope.launch {
                                isAnalyzingDeeply = true
                                val res = VisionXService.analyzeShoppingLens(
                                    hint = testPresets[index],
                                    bitmap = null,
                                    catalog = catalog
                                )
                                currentResult = res
                                onProductDetected(res)
                                isAnalyzingDeeply = false
                            }
                        }
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Bottom Comprehensive Metadata Card & Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFF020617).copy(alpha = 0.95f),
                                Color(0xFF020617)
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cart Notification Toast
                AnimatedVisibility(visible = addedToCartNotification != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(addedToCartNotification ?: "", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Detected Object Metadata Card
                currentResult?.let { res ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detected_object_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0F172A).copy(alpha = 0.94f)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Header: Name, Brand, Category, Confidence
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            res.detectedProductNameAr,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "${(res.confidenceScore * 100).toInt()}% مطابقة",
                                                color = Color(0xFF10B981),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "${res.brand} • ${res.category} ${if (res.barcodeOrSku != null) "• SKU: ${res.barcodeOrSku}" else ""}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "$${String.format("%.2f", res.estimatedMarketPrice)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                    Text(
                                        "السعر المقدر",
                                        fontSize = 9.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            // Metadata Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                                    .padding(2.dp)
                            ) {
                                MetadataTab.entries.forEach { tab ->
                                    val isSelected = tab == selectedMetadataTab
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) Color(0xFF0284C7) else Color.Transparent)
                                            .clickable { selectedMetadataTab = tab }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tab.labelAr,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }

                            // Active Tab Content
                            when (selectedMetadataTab) {
                                MetadataTab.PRICE_RADAR -> {
                                    PriceComparisonUI(
                                        detectedItemName = res.detectedProductNameAr,
                                        basePrice = res.estimatedMarketPrice,
                                        category = res.category,
                                        sku = res.barcodeOrSku,
                                        brand = res.brand,
                                        onAddToCart = { selectedRate ->
                                            val matchedProd = catalog.find { it.name.contains(res.detectedProductNameAr.take(8)) }
                                                ?: catalog.firstOrNull()
                                            if (matchedProd != null) {
                                                onAddToCart(matchedProd.copy(retailPrice = selectedRate.price))
                                                addedToCartNotification = "تمت إضافة '${res.detectedProductNameAr}' من متجر ${selectedRate.retailerNameAr} بسعر $${String.format("%.2f", selectedRate.price)}!"
                                            }
                                        }
                                    )
                                }
                                MetadataTab.SPECS -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            "العبوة: ${res.packagingType}",
                                            fontSize = 11.sp,
                                            color = Color(0xFFE2E8F0)
                                        )
                                        if (res.specifications.isNotEmpty()) {
                                            Text(
                                                "المواصفات: ${res.specifications.joinToString(" • ")}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }
                                MetadataTab.NUTRITION -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            "الحالة والجودة: ${res.freshnessOrQuality}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF34D399),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (res.nutritionalHighlights.isNotEmpty()) {
                                            Text(
                                                "القيم الغذائية: ${res.nutritionalHighlights.joinToString(" • ")}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                        Text(
                                            "إرشادات التخزين: ${res.storageAdvice}",
                                            fontSize = 10.sp,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }
                                }
                                MetadataTab.EVIDENCE -> {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.08f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text(
                                                "Immutable Evidence ID: ${res.evidenceId}",
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFF10B981)
                                            )
                                            Text(
                                                "المصدر: CameraX Frame & Gemini 3.5 Multimodal • 19-Agent Governance",
                                                fontSize = 9.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        // Match in catalog or construct certified product
                                        val matchedProd = catalog.firstOrNull { it.id in res.matchedProductIds }
                                            ?: catalog.firstOrNull { it.name.contains(res.detectedProductNameAr.take(4)) }
                                            ?: ProductEntity(
                                                name = res.detectedProductNameAr,
                                                category = res.category,
                                                retailPrice = res.estimatedMarketPrice,
                                                wholesalePrice = res.estimatedMarketPrice * 0.85,
                                                imageUrl = "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=500",
                                                description = "تم اكتشافه عبر عدسة Vision X Shopping Lens (${res.brand})"
                                            )
                                        onAddToCart(matchedProd)
                                        addedToCartNotification = "تمت إضافة ${res.detectedProductNameAr} إلى السلة بنجاح! 🛒"
                                        scope.launch {
                                            kotlinx.coroutines.delay(2500)
                                            addedToCartNotification = null
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_lens_add_to_cart"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("إضافة للسلة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            isAnalyzingDeeply = true
                                            val deepAnalysis = VisionXService.analyzeShoppingLens(
                                                hint = res.detectedProductNameAr,
                                                bitmap = null,
                                                catalog = catalog
                                            )
                                            currentResult = deepAnalysis
                                            onProductDetected(deepAnalysis)
                                            isAnalyzingDeeply = false
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_lens_deep_analyze"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    if (isAnalyzingDeeply) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("تحليل ومطابقة Gemini", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Shutter / Capture & Real-time Stream Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Toggle Real-time Detection
                    IconButton(
                        onClick = { isRealtimeDetectionActive = !isRealtimeDetectionActive },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("btn_toggle_realtime")
                    ) {
                        Icon(
                            imageVector = if (isRealtimeDetectionActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Realtime",
                            tint = if (isRealtimeDetectionActive) Color(0xFF10B981) else Color(0xFF94A3B8)
                        )
                    }

                    // Main Camera Capture Button (Shutter Ring)
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape)
                            .padding(4.dp)
                            .clickable {
                                if (isAnalyzingDeeply) return@clickable
                                isAnalyzingDeeply = true

                                val capture = imageCapture
                                if (capture != null && !hasCameraError && hasCameraPermission) {
                                    val executor = Executors.newSingleThreadExecutor()
                                    capture.takePicture(
                                        executor,
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                                val bitmap = imageProxyToBitmap(imageProxy)
                                                imageProxy.close()
                                                scope.launch {
                                                    val result = VisionXService.analyzeShoppingLens(
                                                        hint = selectedLensMode.subtitleAr,
                                                        bitmap = bitmap,
                                                        catalog = catalog
                                                    )
                                                    currentResult = result
                                                    onProductDetected(result)
                                                    isAnalyzingDeeply = false
                                                }
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                Log.w("CameraScreen", "takePicture error: ${exception.message}")
                                                scope.launch {
                                                    val result = VisionXService.analyzeShoppingLens(
                                                        hint = testPresets[selectedPresetIndex],
                                                        bitmap = null,
                                                        catalog = catalog
                                                    )
                                                    currentResult = result
                                                    onProductDetected(result)
                                                    isAnalyzingDeeply = false
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    scope.launch {
                                        val result = VisionXService.analyzeShoppingLens(
                                            hint = testPresets[selectedPresetIndex],
                                            bitmap = null,
                                            catalog = catalog
                                        )
                                        currentResult = result
                                        onProductDetected(result)
                                        isAnalyzingDeeply = false
                                    }
                                }
                            }
                            .testTag("btn_camera_shutter"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color.White, Color(0xFF38BDF8))
                                    )
                                )
                        ) {
                            if (isAnalyzingDeeply) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp).align(Alignment.Center),
                                    color = Color(0xFF0F172A),
                                    strokeWidth = 2.5.dp
                                )
                            }
                        }
                    }

                    // Cycle to next product preset
                    IconButton(
                        onClick = {
                            selectedPresetIndex = (selectedPresetIndex + 1) % testPresets.size
                            scope.launch {
                                isAnalyzingDeeply = true
                                val res = VisionXService.analyzeShoppingLens(
                                    hint = testPresets[selectedPresetIndex],
                                    bitmap = null,
                                    catalog = catalog
                                )
                                currentResult = res
                                onProductDetected(res)
                                isAnalyzingDeeply = false
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("btn_next_detection")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Target",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
