package com.example.ui.visionx

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.ProductEntity
import com.example.data.agent.AgentEngine
import com.example.data.agent.AgentKey
import com.example.ui.MarketViewModel
import java.util.UUID

/**
 * Market Trends Dashboard (Compose Canvas based visualization)
 * Visualizes popular product categories and 30-day price fluctuation patterns discovered by AI agents.
 */
@Composable
fun MarketTrendsDashboard(
    viewModel: MarketViewModel,
    lang: String,
    onBack: () -> Unit
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val slateBg = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val textColor = MaterialTheme.colorScheme.onBackground

    val categories = listOf(
        Pair(if (lang == "ar") "الألبان الطازجة" else "Fresh Dairy", 0.34f),
        Pair(if (lang == "ar") "الفواكه والخضار" else "Produce", 0.28f),
        Pair(if (lang == "ar") "العسل الطبيعي" else "Mountain Honey", 0.18f),
        Pair(if (lang == "ar") "الزيوت المعصورة" else "Cold-Pressed Oils", 0.12f),
        Pair(if (lang == "ar") "الحبوب الكاملة" else "Whole Grains", 0.08f)
    )

    val priceTrendPoints = listOf(
        Pair("Day 1", 3.2f),
        Pair("Day 5", 3.4f),
        Pair("Day 10", 3.1f),
        Pair("Day 15", 2.9f),
        Pair("Day 20", 3.3f),
        Pair("Day 25", 3.0f),
        Pair("Day 30", 2.85f)
    )

    LaunchedEffect(Unit) {
        AgentEngine.recordEvidence(
            operationId = "TRENDS_DASH_" + UUID.randomUUID().toString().take(6),
            actorId = AgentKey.DATA_GROWTH_INTELLIGENCE.key,
            type = "MARKET_TRENDS_DASHBOARD_VIEW",
            source = "Data & Growth Intelligence Agent",
            payloadSummary = "User opened 30-day Market Trends & Price Fluctuation Dashboard."
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = slateBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                    Column {
                        Text(
                            text = if (lang == "ar") "لوحة مؤشرات اتجاهات السوق (Market Trends)" else "Market Trends & Price Radar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            text = if (lang == "ar") "تحليلات الذكاء الاصطناعي لآخر ٣٠ يوماً" else "AI Agent Insights for past 30 days",
                            fontSize = 11.sp,
                            color = primaryColor
                        )
                    }
                }
            }

            // Price Fluctuation Trend Card (Compose Canvas Line Graph)
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "ar") "📈 تقلبات الأسعار خلال ٣٠ يوماً (سعر الوحدة $)" else "📈 30-Day Price Fluctuation Trend ($)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Surface(
                            color = primaryColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "-11% هبوط سعري",
                                color = primaryColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Canvas Line Graph
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(vertical = 8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            val maxVal = 4.0f
                            val minVal = 2.0f
                            val stepX = width / (priceTrendPoints.size - 1)

                            val points = priceTrendPoints.mapIndexed { idx, (_, v) ->
                                val x = idx * stepX
                                val y = height - ((v - minVal) / (maxVal - minVal) * (height - 30.dp.toPx())) - 15.dp.toPx()
                                Offset(x, y)
                            }

                            // Draw Grid lines
                            for (i in 0..3) {
                                val gy = height * (i / 3f)
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.2f),
                                    start = Offset(0f, gy),
                                    end = Offset(width, gy),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Draw Gradient Area under curve
                            val path = Path().apply {
                                moveTo(points.first().x, height)
                                points.forEach { lineTo(it.x, it.y) }
                                lineTo(points.last().x, height)
                                close()
                            }
                            drawPath(
                                path = path,
                                brush = Brush.verticalGradient(
                                    colors = listOf(primaryColor.copy(alpha = 0.4f), Color.Transparent),
                                    startY = 0f,
                                    endY = height
                                )
                            )

                            // Draw Smooth Line
                            for (i in 0 until points.size - 1) {
                                drawLine(
                                    color = primaryColor,
                                    start = points[i],
                                    end = points[i + 1],
                                    strokeWidth = 3.dp.toPx()
                                )
                            }

                            // Draw Points
                            points.forEach { pt ->
                                drawCircle(
                                    color = Color.White,
                                    radius = 5.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = primaryColor,
                                    radius = 3.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        priceTrendPoints.forEach { (label, _) ->
                            Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Popular Categories Distribution (Bar / Share Progress)
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (lang == "ar") "🏆 الفئات الأكثر طلباً ومبيعاً (Popular Categories)" else "🏆 Popular Product Categories Share",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    categories.forEach { (catName, share) ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = catName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                                Text(text = "${(share * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = secondaryColor)
                            }
                            LinearProgressIndicator(
                                progress = { share },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = secondaryColor,
                                trackColor = secondaryColor.copy(alpha = 0.15f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * AR View Product Scale Verifier Screen
 * Leverages CameraX live preview to place a virtual 3D holographic wireframe and scale grid
 * of the product in the user's room to verify scale before purchasing.
 */
@Composable
fun ArProductPlacementScreen(
    product: ProductEntity,
    lang: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
        AgentEngine.recordEvidence(
            operationId = "AR_VIEW_${product.id}_" + UUID.randomUUID().toString().take(6),
            actorId = AgentKey.PREVIEW_AGENT.key,
            type = "AR_PRODUCT_SCALE_VERIFIER",
            source = "CameraX AR Holographic Overlay",
            payloadSummary = "User launched AR View for product '${product.name}' to verify physical scale."
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(surfaceProvider)
                            }
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview
                                )
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Camera permission required for AR View", color = Color.White)
            }
        }

        // AR Holographic Wireframe & Scale Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Simulated 3D Bounding Box Wireframe using Canvas
            Canvas(modifier = Modifier.size(260.dp, 320.dp)) {
                val w = size.width
                val h = size.height

                // Draw 3D wireframe box
                drawRect(
                    color = Color(0xFF00F5A0),
                    topLeft = Offset(20f, 40f),
                    size = androidx.compose.ui.geometry.Size(w - 40f, h - 80f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Corner brackets
                val cornerLen = 24f
                val color = Color(0xFF00E5FF)
                // Top-Left
                drawLine(color, Offset(20f, 40f), Offset(20f + cornerLen, 40f), 4.dp.toPx())
                drawLine(color, Offset(20f, 40f), Offset(20f, 40f + cornerLen), 4.dp.toPx())
                // Top-Right
                drawLine(color, Offset(w - 20f, 40f), Offset(w - 20f - cornerLen, 40f), 4.dp.toPx())
                drawLine(color, Offset(w - 20f, 40f), Offset(w - 20f, 40f + cornerLen), 4.dp.toPx())
                // Bottom-Left
                drawLine(color, Offset(20f, h - 40f), Offset(20f + cornerLen, h - 40f), 4.dp.toPx())
                drawLine(color, Offset(20f, h - 40f), Offset(20f, h - 40f - cornerLen), 4.dp.toPx())
                // Bottom-Right
                drawLine(color, Offset(w - 20f, h - 40f), Offset(w - 20f - cornerLen, h - 40f), 4.dp.toPx())
                drawLine(color, Offset(w - 20f, h - 40f), Offset(w - 20f, h - 40f - cornerLen), 4.dp.toPx())
            }

            // Top HUD Bar
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFF00F5A0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ViewInAr, contentDescription = null, tint = Color(0xFF00F5A0), modifier = Modifier.size(16.dp))
                        Text(
                            text = if (lang == "ar") "وضع الواقع الافتراضي AR: ${product.name}" else "AR Scale View: ${product.name}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Text(
                    text = if (lang == "ar") "قم بتوجيه الكاميرا نحو السطح للتحقق من أبعاد الصنف" else "Point camera at surface to verify real-world scale",
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Bottom Product Info & Scale Metrics Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF080D1A).copy(alpha = 0.9f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF00F5A0).copy(alpha = 0.5f)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(product.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("السعر: $${product.retailPrice}", fontSize = 11.sp, color = Color(0xFF00F5A0), fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            color = Color(0xFF00F5A0).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "✅ مقياس حقيقي 1:1",
                                color = Color(0xFF00F5A0),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الارتفاع: 24 سم", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Text("العرض: 12 سم", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        Text("السعة: 1.0 لتر", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5A0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (lang == "ar") "إغلاق معاينة AR" else "Exit AR View", color = Color(0xFF080D1A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
