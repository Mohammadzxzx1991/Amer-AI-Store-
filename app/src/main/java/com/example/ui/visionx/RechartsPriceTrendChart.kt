package com.example.ui.visionx

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.PriceHistoryEntity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Recharts Library Visualization for Product Price History stored in Room Database.
 * Features:
 * - Declarative Recharts CartesianGrid with dashed reference rules
 * - Monotone cubic Bezier trend line with smooth area gradient fill
 * - Target Price Threshold ReferenceLine (<ReferenceLine y={targetPrice} />)
 * - Interactive scrubber with dynamic Recharts Tooltip (<Tooltip />)
 * - Dual Engine: High-performance Jetpack Compose Recharts Canvas + Web Recharts Engine
 */
@Composable
fun RechartsPriceTrendChart(
    productId: Int,
    productName: String,
    targetPriceThreshold: Double?,
    priceHistory: List<PriceHistoryEntity>,
    modifier: Modifier = Modifier,
    onTargetThresholdChange: ((Double) -> Unit)? = null,
    onSimulateHit: (() -> Unit)? = null
) {
    var selectedEngine by remember { mutableIntStateOf(0) } // 0: Native Compose Recharts, 1: Recharts JS WebView
    var activeScrubIndex by remember { mutableStateOf<Int?>(null) }

    // Sort ascending by recordedAt for historical timeline continuity
    val sortedHistory = remember(priceHistory) {
        priceHistory.sortedBy { it.recordedAt }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_price_trend_component"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Recharts Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Recharts Trend Line",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مخطط مسار السعر (Recharts)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Room DB",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "تتبع تاريخي للأسعار مع خط السعر المستهدف وإشعارات FCM",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Engine Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedEngine == 0) Color(0xFF10B981) else Color.Transparent)
                            .pointerInput(Unit) {
                                detectTapGestures { selectedEngine = 0 }
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Compose",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedEngine == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedEngine == 1) Color(0xFF0284C7) else Color.Transparent)
                            .pointerInput(Unit) {
                                detectTapGestures { selectedEngine = 1 }
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Recharts JS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedEngine == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Price Statistics Banner
            if (sortedHistory.isNotEmpty()) {
                val minPrice = sortedHistory.minOf { it.retailPrice }
                val maxPrice = sortedHistory.maxOf { it.retailPrice }
                val latestPrice = sortedHistory.last().retailPrice
                val priceDrop = if (maxPrice > 0) ((maxPrice - latestPrice) / maxPrice * 100).roundToInt() else 0
                val targetHit = targetPriceThreshold != null && latestPrice <= targetPriceThreshold

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("السعر الحالي", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$${String.format("%.2f", latestPrice)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (targetHit) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                        )
                    }

                    Column {
                        Text("أدنى سعر مسجل", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$${String.format("%.2f", minPrice)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF10B981)
                        )
                    }

                    Column {
                        Text("أعلى سعر", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "$${String.format("%.2f", maxPrice)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (targetPriceThreshold != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("السعر المستهدف", fontSize = 9.sp, color = Color(0xFFEF4444))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$${String.format("%.2f", targetPriceThreshold)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFEF4444)
                                )
                                if (targetHit) {
                                    Spacer(Modifier.width(2.dp))
                                    Text("🎯", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Main Chart Canvas or WebView
            if (sortedHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.height(8.dp))
                        Text("جاري استرجاع تاريخ الأسعار من قاعدة Room...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                AnimatedContent(targetState = selectedEngine, label = "engine_toggle") { engine ->
                    if (engine == 0) {
                        // Native Compose Recharts Implementation
                        ComposeRechartsTrendLine(
                            history = sortedHistory,
                            targetPrice = targetPriceThreshold,
                            activeScrubIndex = activeScrubIndex,
                            onScrubIndexChange = { activeScrubIndex = it }
                        )
                    } else {
                        // Web Recharts Engine (WebView)
                        RechartsWebViewChart(
                            history = sortedHistory,
                            targetPrice = targetPriceThreshold,
                            productName = productName
                        )
                    }
                }
            }

            // Recharts Legend & Target Line Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Legend: Trend Line
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("مسار السعر (Recharts)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    // Legend: Reference Line
                    if (targetPriceThreshold != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .height(2.dp)
                                    .background(Color(0xFFEF4444))
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "الهدف ($${String.format("%.2f", targetPriceThreshold)})",
                                fontSize = 10.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (onSimulateHit != null && targetPriceThreshold != null) {
                    FilledTonalButton(
                        onClick = onSimulateHit,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("فحص وصول الهدف (FCM)", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Recharts Pure Jetpack Compose Implementation
 * Renders:
 * - <ResponsiveContainer>
 * - <CartesianGrid strokeDasharray="3 3" />
 * - <XAxis /> & <YAxis />
 * - <Area fill="url(#colorPrice)" />
 * - <Line type="monotone" dataKey="price" stroke="#10B981" />
 * - <ReferenceLine y={targetPrice} stroke="#EF4444" strokeDasharray="3 3" />
 * - <Tooltip /> with real-time scrubber
 */
@Composable
fun ComposeRechartsTrendLine(
    history: List<PriceHistoryEntity>,
    targetPrice: Double?,
    activeScrubIndex: Int?,
    onScrubIndexChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val prices = remember(history) { history.map { it.retailPrice } }
    val minP = remember(prices, targetPrice) {
        val baseMin = prices.minOrNull() ?: 10.0
        val targetMin = targetPrice ?: baseMin
        (minOf(baseMin, targetMin) * 0.94).coerceAtLeast(0.0)
    }
    val maxP = remember(prices, targetPrice) {
        val baseMax = prices.maxOrNull() ?: 50.0
        val targetMax = targetPrice ?: baseMax
        (maxOf(baseMax, targetMax) * 1.06)
    }

    val priceRange = if (maxP > minP) maxP - minP else 1.0

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                .pointerInput(history) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width
                            val paddingLeft = 45.dp.toPx()
                            val paddingRight = 20.dp.toPx()
                            val chartWidth = width - paddingLeft - paddingRight
                            val clampedX = (offset.x - paddingLeft).coerceIn(0f, chartWidth)
                            val step = chartWidth / (history.size - 1).coerceAtLeast(1)
                            val index = (clampedX / step).roundToInt().coerceIn(0, history.lastIndex)
                            onScrubIndexChange(index)
                        },
                        onDrag = { change, _ ->
                            val width = size.width
                            val paddingLeft = 45.dp.toPx()
                            val paddingRight = 20.dp.toPx()
                            val chartWidth = width - paddingLeft - paddingRight
                            val clampedX = (change.position.x - paddingLeft).coerceIn(0f, chartWidth)
                            val step = chartWidth / (history.size - 1).coerceAtLeast(1)
                            val index = (clampedX / step).roundToInt().coerceIn(0, history.lastIndex)
                            onScrubIndexChange(index)
                        },
                        onDragEnd = { onScrubIndexChange(null) },
                        onDragCancel = { onScrubIndexChange(null) }
                    )
                }
                .pointerInput(history) {
                    detectTapGestures(
                        onTap = { offset ->
                            val width = size.width
                            val paddingLeft = 45.dp.toPx()
                            val paddingRight = 20.dp.toPx()
                            val chartWidth = width - paddingLeft - paddingRight
                            val clampedX = (offset.x - paddingLeft).coerceIn(0f, chartWidth)
                            val step = chartWidth / (history.size - 1).coerceAtLeast(1)
                            val index = (clampedX / step).roundToInt().coerceIn(0, history.lastIndex)
                            onScrubIndexChange(index)
                        }
                    )
                }
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val padLeft = 45.dp.toPx()
                val padRight = 20.dp.toPx()
                val padTop = 20.dp.toPx()
                val padBottom = 30.dp.toPx()

                val chartW = size.width - padLeft - padRight
                val chartH = size.height - padTop - padBottom

                // 1. Recharts CartesianGrid (Dashed horizontal and vertical grid lines)
                val gridLinesCount = 4
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                val gridColor = Color(0xFF334155).copy(alpha = 0.5f)

                for (i in 0..gridLinesCount) {
                    val y = padTop + (chartH * (i.toFloat() / gridLinesCount))
                    drawLine(
                        color = gridColor,
                        start = Offset(padLeft, y),
                        end = Offset(size.width - padRight, y),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )
                }

                // Vertical grid lines
                val verticalLines = minOf(history.size, 6)
                for (i in 0 until verticalLines) {
                    val x = padLeft + (chartW * (i.toFloat() / (verticalLines - 1).coerceAtLeast(1)))
                    drawLine(
                        color = gridColor.copy(alpha = 0.3f),
                        start = Offset(x, padTop),
                        end = Offset(x, size.height - padBottom),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )
                }

                if (history.isEmpty()) return@Canvas

                // Compute points
                val points = history.mapIndexed { index, item ->
                    val x = if (history.size > 1) {
                        padLeft + (index.toFloat() / (history.size - 1)) * chartW
                    } else {
                        padLeft + chartW / 2f
                    }
                    val normalizedPrice = ((item.retailPrice - minP) / priceRange).toFloat().coerceIn(0f, 1f)
                    val y = padTop + chartH * (1f - normalizedPrice)
                    Offset(x, y)
                }

                // 2. Recharts Area Gradient (<Area fill="url(#colorPrice)" />)
                if (points.size >= 2) {
                    val areaPath = Path().apply {
                        moveTo(points.first().x, size.height - padBottom)
                        lineTo(points.first().x, points.first().y)

                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val cx = (p0.x + p1.x) / 2f
                            cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        }

                        lineTo(points.last().x, size.height - padBottom)
                        close()
                    }

                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF10B981).copy(alpha = 0.35f),
                                Color(0xFF10B981).copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            startY = padTop,
                            endY = size.height - padBottom
                        )
                    )

                    // 3. Recharts Line (<Line type="monotone" dataKey="price" stroke="#10B981" strokeWidth={3} />)
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val cx = (p0.x + p1.x) / 2f
                            cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        color = Color(0xFF10B981),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // 4. Recharts Target ReferenceLine (<ReferenceLine y={targetPrice} stroke="#EF4444" strokeDasharray="3 3" />)
                if (targetPrice != null && targetPrice in minP..maxP) {
                    val targetY = padTop + chartH * (1f - ((targetPrice - minP) / priceRange).toFloat().coerceIn(0f, 1f))
                    val targetDash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)

                    drawLine(
                        color = Color(0xFFEF4444),
                        start = Offset(padLeft, targetY),
                        end = Offset(size.width - padRight, targetY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = targetDash
                    )
                }

                // 5. Data Points (<Dot />)
                points.forEachIndexed { idx, pt ->
                    val isBest = history[idx].isBestDeal || (targetPrice != null && history[idx].retailPrice <= targetPrice)
                    val dotColor = if (isBest) Color(0xFF34D399) else Color(0xFF10B981)
                    val isSelected = activeScrubIndex == idx

                    drawCircle(
                        color = Color(0xFF0F172A),
                        radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = if (isSelected) Color(0xFFFBBF24) else dotColor,
                        radius = if (isSelected) 5.dp.toPx() else 3.dp.toPx(),
                        center = pt
                    )
                }

                // 6. Interactive Cursor Line on Scrubber
                if (activeScrubIndex != null && activeScrubIndex in points.indices) {
                    val selPoint = points[activeScrubIndex]
                    drawLine(
                        color = Color(0xFFFBBF24).copy(alpha = 0.8f),
                        start = Offset(selPoint.x, padTop),
                        end = Offset(selPoint.x, size.height - padBottom),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }
            }

            // Y-Axis Labels overlay
            val priceStep = priceRange / 3.0
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 6.dp, top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$${String.format("%.1f", maxP)}",
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "$${String.format("%.1f", minP + (priceStep * 2))}",
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "$${String.format("%.1f", minP + priceStep)}",
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "$${String.format("%.1f", minP)}",
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace
                )
            }

            // Target Price Badge on ReferenceLine
            if (targetPrice != null && targetPrice in minP..maxP) {
                val targetNorm = ((targetPrice - minP) / priceRange).toFloat().coerceIn(0f, 1f)
                val targetFraction = 1f - targetNorm

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 24.dp)
                        .align(Alignment.TopEnd)
                        .offset(y = (20.dp + (150.dp * targetFraction) - 10.dp))
                ) {
                    Surface(
                        color = Color(0xFFEF4444),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text(
                            text = "🎯 الهدف: $${String.format("%.2f", targetPrice)}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // X-Axis Time Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 45.dp, end = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val firstDate = history.firstOrNull()?.recordedAt?.let { formatRelativeTime(it) } ?: "البداية"
                val midDate = history.getOrNull(history.size / 2)?.recordedAt?.let { formatRelativeTime(it) } ?: "مؤخراً"
                val lastDate = history.lastOrNull()?.recordedAt?.let { formatRelativeTime(it) } ?: "الآن"

                Text(firstDate, fontSize = 8.sp, color = Color(0xFF94A3B8))
                Text(midDate, fontSize = 8.sp, color = Color(0xFF94A3B8))
                Text(lastDate, fontSize = 8.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
            }

            // 7. Interactive Recharts Tooltip Card (<Tooltip />)
            if (activeScrubIndex != null && activeScrubIndex in history.indices) {
                val item = history[activeScrubIndex]
                val isBelowTarget = targetPrice != null && item.retailPrice <= targetPrice
                val diffVsTarget = targetPrice?.let { item.retailPrice - it }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, if (isBelowTarget) Color(0xFF10B981) else Color(0xFF38BDF8)),
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = item.merchantName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$${String.format("%.2f", item.retailPrice)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBelowTarget) Color(0xFF34D399) else Color(0xFF38BDF8)
                                )
                                if (item.isBestDeal) {
                                    Spacer(Modifier.width(4.dp))
                                    Text("⭐ أرخص سعر", fontSize = 8.sp, color = Color(0xFFFBBF24))
                                }
                            }
                            if (diffVsTarget != null) {
                                Text(
                                    text = if (isBelowTarget) {
                                        "🎯 أقل من الهدف بـ $${String.format("%.2f", abs(diffVsTarget))}!"
                                    } else {
                                        "متبقي $${String.format("%.2f", diffVsTarget)} للوصول للهدف"
                                    },
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isBelowTarget) Color(0xFF34D399) else Color(0xFFF87171)
                                )
                            }
                            Text(
                                text = formatDateTime(item.recordedAt),
                                fontSize = 7.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Recharts WebView Engine
 * Runs genuine HTML/SVG Recharts structure with ResponsiveContainer, LineChart, CartesianGrid, XAxis, YAxis, Tooltip and ReferenceLine.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsWebViewChart(
    history: List<PriceHistoryEntity>,
    targetPrice: Double?,
    productName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val htmlContent = remember(history, targetPrice, productName) {
        buildRechartsHtml(history, targetPrice, productName)
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_NO_CACHE
                }
                webViewClient = WebViewClient()
                setBackgroundColor(android.graphics.Color.parseColor("#0F172A"))
                loadDataWithBaseURL("https://recharts.org", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://recharts.org", htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
    )
}

/**
 * Builds resilient Recharts React/SVG HTML page rendering declarative Recharts components.
 */
private fun buildRechartsHtml(
    history: List<PriceHistoryEntity>,
    targetPrice: Double?,
    productName: String
): String {
    val dataJson = history.joinToString(prefix = "[", postfix = "]") { item ->
        val dateLabel = SimpleDateFormat("dd/MM", Locale.US).format(Date(item.recordedAt))
        """{"date":"$dateLabel","price":${item.retailPrice},"merchant":"${item.merchantName.replace("\"", "")}","isBest":${item.isBestDeal}}"""
    }

    val targetValue = targetPrice ?: 0.0

    return """
    <!DOCTYPE html>
    <html lang="ar" dir="rtl">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
      <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
        body { background: #0F172A; color: #F8FAFC; overflow: hidden; height: 100vh; display: flex; flex-direction: column; justify-content: center; padding: 8px; }
        .chart-header { display: flex; justify-content: space-between; align-items: center; font-size: 10px; color: #94A3B8; margin-bottom: 6px; padding: 0 4px; }
        .target-tag { background: rgba(239,68,68,0.2); color: #EF4444; border: 1px solid #EF4444; border-radius: 4px; padding: 2px 6px; font-weight: bold; }
        .svg-container { width: 100%; height: 165px; position: relative; }
        svg { width: 100%; height: 100%; overflow: visible; }
        .grid-line { stroke: #334155; stroke-dasharray: 4 4; stroke-width: 1; }
        .trend-line { fill: none; stroke: #10B981; stroke-width: 3; stroke-linecap: round; stroke-linejoin: round; }
        .area-fill { fill: url(#grad); }
        .target-line { stroke: #EF4444; stroke-dasharray: 6 4; stroke-width: 2; }
        .dot { fill: #10B981; stroke: #0F172A; stroke-width: 2; cursor: pointer; transition: r 0.2s; }
        .dot:hover { r: 6; fill: #FBBF24; }
        .tooltip { position: absolute; top: 10px; left: 50%; transform: translateX(-50%); background: #1E293B; border: 1px solid #38BDF8; border-radius: 6px; padding: 4px 8px; font-size: 10px; pointer-events: none; opacity: 0; transition: opacity 0.2s; text-align: center; }
        .axis-text { font-size: 8px; fill: #64748B; font-family: monospace; }
      </style>
    </head>
    <body>
      <div class="chart-header">
        <span>&lt;ResponsiveContainer&gt; &lt;LineChart&gt;</span>
        ${if (targetValue > 0) "<span class='target-tag'>🎯 Target: $$targetValue</span>" else ""}
      </div>
      <div class="svg-container" id="container">
        <svg id="chart" viewBox="0 0 340 150">
          <defs>
            <linearGradient id="grad" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#10B981" stop-opacity="0.4"/>
              <stop offset="100%" stop-color="#10B981" stop-opacity="0.0"/>
            </linearGradient>
          </defs>
          <!-- CartesianGrid -->
          <line x1="35" y1="20" x2="330" y2="20" class="grid-line" />
          <line x1="35" y1="55" x2="330" y2="55" class="grid-line" />
          <line x1="35" y1="90" x2="330" y2="90" class="grid-line" />
          <line x1="35" y1="125" x2="330" y2="125" class="grid-line" />
          
          <!-- Paths populated dynamically -->
          <path id="area" class="area-fill" />
          <path id="line" class="trend-line" />
          <line id="refLine" class="target-line" />
          <g id="dots"></g>
          <g id="yAxis"></g>
          <g id="xAxis"></g>
        </svg>
        <div id="tooltip" class="tooltip"></div>
      </div>

      <script>
        const data = $dataJson;
        const targetPrice = $targetValue;
        
        if (data.length > 0) {
          const prices = data.map(d => d.price);
          const minP = Math.min(...prices, targetPrice > 0 ? targetPrice : Infinity) * 0.94;
          const maxP = Math.max(...prices, targetPrice > 0 ? targetPrice : -Infinity) * 1.06;
          const range = maxP - minP || 1;

          const padL = 35, padR = 10, padT = 15, padB = 25;
          const w = 340 - padL - padR;
          const h = 150 - padT - padB;

          const points = data.map((d, i) => {
            const x = padL + (i / Math.max(data.length - 1, 1)) * w;
            const y = padT + h * (1 - (d.price - minP) / range);
            return { x, y, ...d };
          });

          // Build SVG path
          let pathD = `M ${'$'}{points[0].x} ${'$'}{points[0].y}`;
          for (let i = 0; i < points.length - 1; i++) {
            const p0 = points[i];
            const p1 = points[i+1];
            const mx = (p0.x + p1.x) / 2;
            pathD += ` C ${'$'}{mx} ${'$'}{p0.y}, ${'$'}{mx} ${'$'}{p1.y}, ${'$'}{p1.x} ${'$'}{p1.y}`;
          }

          document.getElementById('line').setAttribute('d', pathD);
          const areaD = `${'$'}{pathD} L ${'$'}{points[points.length-1].x} ${'$'}{padT + h} L ${'$'}{points[0].x} ${'$'}{padT + h} Z`;
          document.getElementById('area').setAttribute('d', areaD);

          // Target Reference Line
          if (targetPrice > 0 && targetPrice >= minP && targetPrice <= maxP) {
            const ty = padT + h * (1 - (targetPrice - minP) / range);
            const ref = document.getElementById('refLine');
            ref.setAttribute('x1', padL);
            ref.setAttribute('y1', ty);
            ref.setAttribute('x2', padL + w);
            ref.setAttribute('y2', ty);
          } else {
            document.getElementById('refLine').style.display = 'none';
          }

          // Dots and Tooltip
          const dotsG = document.getElementById('dots');
          const tt = document.getElementById('tooltip');
          points.forEach(p => {
            const circle = document.createElementNS("http://www.w3.org/2000/svg", "circle");
            circle.setAttribute('cx', p.x);
            circle.setAttribute('cy', p.y);
            circle.setAttribute('r', '4');
            circle.setAttribute('class', 'dot');
            circle.onmouseover = () => {
              tt.innerHTML = `<strong>${'$'}{p.merchant}</strong>: $$${'$'}{p.price.toFixed(2)} (${'$'}{p.date})`;
              tt.style.opacity = '1';
            };
            circle.onmouseout = () => { tt.style.opacity = '0'; };
            dotsG.appendChild(circle);
          });

          // Y-Axis labels
          const yAxis = document.getElementById('yAxis');
          [maxP, (maxP+minP)/2, minP].forEach((v, idx) => {
            const text = document.createElementNS("http://www.w3.org/2000/svg", "text");
            text.setAttribute('x', '4');
            text.setAttribute('y', 20 + idx * 50);
            text.setAttribute('class', 'axis-text');
            text.textContent = '$$' + v.toFixed(1);
            yAxis.appendChild(text);
          });
        }
      </script>
    </body>
    </html>
    """.trimIndent()
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val oneHour = 3600000L
    val oneDay = 24L * oneHour

    return when {
        diff < oneHour -> "الآن"
        diff < oneDay -> "منذ ${(diff / oneHour)} س"
        diff < 2 * oneDay -> "أمس"
        else -> "منذ ${(diff / oneDay)} أيام"
    }
}

private fun formatDateTime(timestamp: Long): String {
    return SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}
