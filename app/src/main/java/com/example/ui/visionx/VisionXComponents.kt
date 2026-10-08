package com.example.ui.visionx

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CartItemEntity
import com.example.data.OrderEntity
import com.example.data.ProductEntity
import com.example.data.PriceHistoryEntity
import com.example.ui.TrendingProduct
import com.example.data.visionx.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Living Store: Command Orb Floating Action Button & Radial Launcher
 */
@Composable
fun CommandOrb(
    onOpenLens: () -> Unit,
    onOpenPriceRadar: () -> Unit,
    onOpenBasketOptimizer: () -> Unit,
    onOpenShoppingMission: () -> Unit,
    onOpenReceiptScan: () -> Unit,
    onOpenCreativeStudio: () -> Unit,
    onOpenAgentHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {
        // Expanded Quick Menu
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 72.dp, end = 4.dp)
                    .width(260.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Command Orb • مركز العمليات الذكي",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    CommandOrbItem(
                        icon = Icons.Default.CameraAlt,
                        title = "عدسة الشراء الذكية (Lens)",
                        subtitle = "تصوير ومطابقة المنتج والأسعار",
                        tint = Color(0xFF3B82F6),
                        onClick = { isExpanded = false; onOpenLens() }
                    )

                    CommandOrbItem(
                        icon = Icons.Default.Radar,
                        title = "رادار الأسعار (Price Radar)",
                        subtitle = "مقارنة فورية بين المتاجر وتنبيهات",
                        tint = Color(0xFF10B981),
                        onClick = { isExpanded = false; onOpenPriceRadar() }
                    )

                    CommandOrbItem(
                        icon = Icons.Default.ShoppingBasket,
                        title = "محسن السلة (Basket Optimizer)",
                        subtitle = "أقصى توفير وأسرع توصيل",
                        tint = Color(0xFFF59E0B),
                        onClick = { isExpanded = false; onOpenBasketOptimizer() }
                    )

                    CommandOrbItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "مهام الشراء بالذكاء الاصطناعي",
                        subtitle = "تخطيط العزائم والميزانيات آلياً",
                        tint = Color(0xFF8B5CF6),
                        onClick = { isExpanded = false; onOpenShoppingMission() }
                    )

                    CommandOrbItem(
                        icon = Icons.Default.ReceiptLong,
                        title = "تحليل الفواتير الذكي (Receipts)",
                        subtitle = "كشف الفروقات وطلب استرداد فوري",
                        tint = Color(0xFFEC4899),
                        onClick = { isExpanded = false; onOpenReceiptScan() }
                    )

                    CommandOrbItem(
                        icon = Icons.Default.Palette,
                        title = "استوديو الابتكار (Creative Studio)",
                        subtitle = "توليد تصاميم وإعلانات AR/EN",
                        tint = Color(0xFF06B6D4),
                        onClick = { isExpanded = false; onOpenCreativeStudio() }
                    )

                    CommandOrbItem(
                        icon = Icons.Default.Hub,
                        title = "منظومة الـ 19 وكيلاً (Agent OS)",
                        subtitle = "مراقبة وإدارة الحوكمة الذاتية",
                        tint = Color(0xFF6366F1),
                        onClick = { isExpanded = false; onOpenAgentHub() }
                    )
                }
            }
        }

        // Orb Trigger Button
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier
                .size(58.dp)
                .shadow(8.dp, CircleShape)
                .testTag("btn_command_orb"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.AutoAwesome,
                contentDescription = "Command Orb",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun CommandOrbItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.width(10.dp))

        Column {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Universal Shopping Lens Dialog with CameraX Lifecycle & Gemini Product Recognition
 */
@Composable
fun ShoppingLensDialog(
    catalog: List<ProductEntity>,
    onDismiss: () -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    onOpenCreativeStudio: ((ShoppingLensResult) -> Unit)? = null
) {
    var isCameraMode by remember { mutableStateOf(true) }
    var searchHint by remember { mutableStateOf("عسل طبيعي عضوي") }
    var capturedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var lensResult by remember { mutableStateOf<ShoppingLensResult?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var showFullScreenCamera by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    if (showFullScreenCamera) {
        Dialog(
            onDismissRequest = { 
                showFullScreenCamera = false
                onDismiss()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            CameraScreen(
                catalog = catalog,
                onDismiss = { 
                    showFullScreenCamera = false
                    onDismiss()
                },
                onAddToCart = { product ->
                    onAddToCart(product)
                    showFullScreenCamera = false
                    onDismiss()
                },
                onProductDetected = { result ->
                    lensResult = result
                    onOpenCreativeStudio?.invoke(result)
                }
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF3B82F6))
                        Spacer(Modifier.width(8.dp))
                        Text("عدسة الشراء الذكية (Shopping Lens)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Text(
                    "تصوير ومطابقة المنتج بالذكاء الاصطناعي (Gemini & CameraX) والبحث في الكتالوج المعتمد.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Mode Selector Tabs (Camera vs Manual Query)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(2.dp)
                ) {
                    TabButton(
                        title = "كاميرا العدسة (CameraX)",
                        icon = Icons.Default.Camera,
                        selected = isCameraMode,
                        onClick = { isCameraMode = true },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        title = "بحث يدوي",
                        icon = Icons.Default.Search,
                        selected = !isCameraMode,
                        onClick = { isCameraMode = false },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isCameraMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "مستشعر الكاميرا المباشر (CameraX)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FilledTonalButton(
                            onClick = { showFullScreenCamera = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp).testTag("btn_open_fullscreen_camera")
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("شاشة كاملة وكشف حي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Live CameraX Viewfinder
                    ShoppingLensCameraXView(
                        onFrameCaptured = { bitmap ->
                            capturedBitmap = bitmap
                            scope.launch {
                                isAnalyzing = true
                                lensResult = VisionXService.analyzeShoppingLens(
                                    hint = searchHint,
                                    bitmap = bitmap,
                                    catalog = catalog
                                )
                                isAnalyzing = false
                            }
                        }
                    )
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = searchHint,
                            onValueChange = { searchHint = it },
                            label = { Text("اسم أو وصف المنتج") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    isAnalyzing = true
                                    lensResult = VisionXService.analyzeShoppingLens(searchHint, null, catalog)
                                    isAnalyzing = false
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text("مسح")
                        }
                    }
                }

                if (isAnalyzing) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                            Text("جاري تحليل الإطار ومطابقته بواسطة Gemini...", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (lensResult != null) {
                    val result = lensResult!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF3B82F6))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(result.detectedProductNameAr, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Surface(
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "دقة المطابقة: ${(result.confidenceScore * 100).toInt()}%",
                                        color = Color(0xFF10B981),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text("العلامة التجارية: ${result.brand} • التصنيف: ${result.category}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (result.barcodeOrSku != null) {
                                Text("رمز الباركود / SKU: ${result.barcodeOrSku}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                            }
                            Text("العبوة والجودة: ${result.packagingType} • ${result.freshnessOrQuality}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            if (result.specifications.isNotEmpty()) {
                                Text("المواصفات: ${result.specifications.joinToString(" • ")}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (result.nutritionalHighlights.isNotEmpty()) {
                                Text("القيم الغذائية: ${result.nutritionalHighlights.joinToString(" • ")}", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)
                            }
                            Text("السعر التقديري بالكتالوج: $${String.format("%.2f", result.estimatedMarketPrice)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Text("أفضل متجر قريب: ${result.bestNearbyStore} (يبعد ${result.distanceKm} كم)", fontSize = 12.sp)

                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Evidence ID: ${result.evidenceId}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(6.dp)
                                )
                            }

                            // Price Radar Retail Links Comparison
                            var showPriceRadarComparison by remember { mutableStateOf(false) }
                            var liveComparison by remember { mutableStateOf<MonitoredPriceComparison?>(null) }
                            var isComparingLinks by remember { mutableStateOf(false) }

                            OutlinedButton(
                                onClick = {
                                    showPriceRadarComparison = !showPriceRadarComparison
                                    if (showPriceRadarComparison && liveComparison == null) {
                                        scope.launch {
                                            isComparingLinks = true
                                            liveComparison = PriceRadarService.monitorVisionXProductLinks(result)
                                            isComparingLinks = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_lens_price_radar_monitor"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                                border = BorderStroke(1.dp, Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (showPriceRadarComparison) "إخفاء رادار مقارنة الروابط" else "مقارنة الروابط عبر رادار الأسعار (Price Radar)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (showPriceRadarComparison) {
                                PriceComparisonUI(
                                    detectedItemName = result.detectedProductNameAr,
                                    basePrice = result.estimatedMarketPrice,
                                    category = result.category,
                                    sku = result.barcodeOrSku,
                                    brand = result.brand,
                                    onAddToCart = { rate ->
                                        val matchedProduct = catalog.find { it.id in result.matchedProductIds } ?: catalog.firstOrNull()
                                        if (matchedProduct != null) {
                                            onAddToCart(matchedProduct.copy(retailPrice = rate.price))
                                            onDismiss()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    val matchedProduct = catalog.find { it.id in result.matchedProductIds }
                    if (matchedProduct != null) {
                        Button(
                            onClick = {
                                onAddToCart(matchedProduct)
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_lens_add_to_cart"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("إضافة المنتج المطابق (${matchedProduct.name}) إلى السلة")
                        }
                    }

                    if (onOpenCreativeStudio != null) {
                        OutlinedButton(
                            onClick = {
                                onOpenCreativeStudio(result)
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_lens_open_creative_studio"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("توليد صور لايف ستايل في استوديو الذكاء الاصطناعي 🎨", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                title,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Price Radar & Live Quotes Dialog with Recharts Trend Visualization and Real-Time FCM Alerts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceRadarDialog(
    catalog: List<ProductEntity>,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val radarItems = remember(catalog) { VisionXService.getPriceRadarItems(catalog) }
    val monitoredLinks by PriceRadarService.monitoredComparisons.collectAsState()
    val activeAlerts by PriceAlertManager.activeAlerts.collectAsState()
    val activeTracking by PriceAlertManager.activeTracking.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Radar, 1: Recharts Trend Line, 2: FCM Alerts
    var selectedChartProductId by remember { mutableIntStateOf(radarItems.firstOrNull()?.productId ?: (catalog.firstOrNull()?.id ?: 101)) }
    var expandedChartProductId by remember { mutableStateOf<Int?>(null) }

    // Collect historical price data from Room database for the selected product
    val priceHistoryDao = remember { PriceRadarService.getPriceHistoryDao() }
    val historyFlow = remember(selectedChartProductId, priceHistoryDao) {
        priceHistoryDao?.getHistoryForProduct(selectedChartProductId) ?: flowOf(emptyList())
    }
    val historyFromRoom by historyFlow.collectAsState(initial = emptyList())

    // Ensure at least 7 historical snapshots exist in Room if not populated
    LaunchedEffect(selectedChartProductId) {
        val selectedProduct = catalog.find { it.id == selectedChartProductId }
        val target = activeAlerts[selectedChartProductId]
        if (selectedProduct != null) {
            PriceRadarService.ensureHistoricalPriceDataInRoom(
                productId = selectedProduct.id,
                productName = selectedProduct.name,
                basePrice = selectedProduct.retailPrice,
                targetThreshold = target
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("dialog_price_radar")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Radar, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(Modifier.width(8.dp))
                        Text("رادار الأسعار المباشر (Price Radar)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Text(
                    "مقارنة حية لأسعار التجزئة والجملة، مخطط مسار السعر Recharts، وإشعارات FCM فور وصول السعر المستهدف.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(10.dp))

                // Navigation Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("📊 رادار الأسعار", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("📈 مسار Recharts", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎯 تنبيهات FCM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                if (activeAlerts.isNotEmpty()) {
                                    Spacer(Modifier.width(4.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                "${activeAlerts.size}",
                                                fontSize = 9.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))

                when (selectedTab) {
                    // TAB 0: LIVE PRICE RADAR COMPARISONS
                    0 -> {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 500.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (monitoredLinks.isNotEmpty()) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0284C7).copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                "روابط المنتجات المراقبة عبر عدسة Vision X (${monitoredLinks.size})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF38BDF8)
                                            )
                                        }
                                    }
                                }

                                items(monitoredLinks) { mon ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF0F172A).copy(alpha = 0.9f)
                                        ),
                                        border = BorderStroke(1.5.dp, Color(0xFF10B981))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(mon.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                                Surface(
                                                    color = Color(0xFF10B981),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        "أرخص سعر: $${String.format("%.2f", mon.lowestPriceFound)}",
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF065F46).copy(alpha = 0.4f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "المتجر الأقل سعراً: ${mon.lowestPriceRetailer}",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFFD1FAE5),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        "وفر $${String.format("%.2f", mon.totalSavings)}",
                                                        fontSize = 9.sp,
                                                        color = Color(0xFFFBBF24),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                mon.quotes.forEach { q ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            "${q.providerName} (${q.source})",
                                                            fontSize = 9.sp,
                                                            color = if (q.isBestDeal) Color(0xFF34D399) else Color(0xFF94A3B8),
                                                            fontWeight = if (q.isBestDeal) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                        Text(
                                                            "$${String.format("%.2f", q.price)}",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (q.isBestDeal) Color(0xFF34D399) else Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }

                            items(radarItems) { item ->
                                val target = activeAlerts[item.productId] ?: (item.currentRetailPrice * 0.88)
                                val isTracking = activeTracking[item.productId] == true
                                val targetReached = item.currentRetailPrice <= target
                                val isExpandedChart = expandedChartProductId == item.productId

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    border = if (targetReached) BorderStroke(1.5.dp, Color(0xFF10B981)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Product title & lowest price
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(item.productNameAr, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                if (targetReached) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            "🎯 وصل السعر المستهدف!",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF059669),
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    "أرخص عرض: $${item.currentRetailPrice}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF10B981)
                                                )
                                                Text(
                                                    "متوسط السوق: $${String.format("%.2f", item.marketAveragePrice)}",
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Multi-merchant live quotes
                                        item.quotes.forEach { quote ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (quote.isBestDeal) Color(0xFF10B981).copy(alpha = 0.12f) else Color.Transparent,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "${quote.providerName} (${quote.distanceKm} كم)",
                                                        fontSize = 11.sp,
                                                        fontWeight = if (quote.isBestDeal) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                    Text(text = "المصدر: ${quote.source}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = "$${quote.price}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (quote.isBestDeal) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (quote.isBestDeal) {
                                                        Text("أفضل سعر ⭐", fontSize = 8.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }

                                        // Target Price Threshold Bar
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.TrackChanges, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        "الهدف: $${String.format("%.2f", target)}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFEF4444)
                                                    )
                                                }

                                                // Quick target adjustment chips
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    listOf(5, 10, 20).forEach { pct ->
                                                        val calc = item.currentRetailPrice * (1.0 - (pct / 100.0))
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .pointerInput(Unit) {
                                                                    detectTapGestures {
                                                                        PriceAlertManager.setTargetPrice(context, item.productId, item.productNameAr, calc)
                                                                        android.widget.Toast.makeText(context, "تم تحديد الهدف: -$pct% ($${String.format("%.2f", calc)})", android.widget.Toast.LENGTH_SHORT).show()
                                                                    }
                                                                }
                                                        ) {
                                                            Text(
                                                                "-$pct%",
                                                                fontSize = 9.sp,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Action Buttons: Recharts Trend Toggle + FCM Tracking + Test Push Trigger
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Toggle FCM Tracking
                                            FilledTonalButton(
                                                onClick = {
                                                    PriceAlertManager.toggleTracking(context, item.productId, item.productNameAr, target)
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp).weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = if (isTracking) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp),
                                                    tint = if (isTracking) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(Modifier.width(3.dp))
                                                Text(
                                                    text = if (isTracking) "تتبع FCM نشط 🔔" else "تتبع FCM",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Recharts Trend Line Toggle
                                            OutlinedButton(
                                                onClick = {
                                                    expandedChartProductId = if (isExpandedChart) null else item.productId
                                                    selectedChartProductId = item.productId
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp).weight(1.1f)
                                            ) {
                                                Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF0284C7))
                                                Spacer(Modifier.width(3.dp))
                                                Text(if (isExpandedChart) "إخفاء Recharts" else "مسار Recharts 📈", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Simulate Real-time FCM Notification
                                            OutlinedButton(
                                                onClick = {
                                                    PriceAlertManager.simulateTargetPriceHit(
                                                        context = context,
                                                        productId = item.productId,
                                                        productName = item.productNameAr,
                                                        simulatedDroppedPrice = target * 0.98
                                                    )
                                                    android.widget.Toast.makeText(context, "تم إرسال إشعار FCM فوري للهدف 🎯", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp).weight(0.9f)
                                            ) {
                                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(10.dp))
                                                Spacer(Modifier.width(3.dp))
                                                Text("تجربة FCM", fontSize = 9.sp)
                                            }
                                        }

                                        // Expandable Recharts Trend Line Chart
                                        AnimatedVisibility(visible = isExpandedChart) {
                                            Column(modifier = Modifier.padding(top = 4.dp)) {
                                                RechartsPriceTrendChart(
                                                    productId = item.productId,
                                                    productName = item.productNameAr,
                                                    targetPriceThreshold = target,
                                                    priceHistory = historyFromRoom,
                                                    onSimulateHit = {
                                                        PriceAlertManager.simulateTargetPriceHit(
                                                            context = context,
                                                            productId = item.productId,
                                                            productName = item.productNameAr,
                                                            simulatedDroppedPrice = target * 0.97
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 1: DEDICATED RECHARTS PRICE TREND VISUALIZER
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "اختر منتجاً لعرض مسار السعر التاريخي المخزن في Room عبر مكتبة Recharts:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Product Selector Chips
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 100.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(catalog) { prod ->
                                    val isSelected = prod.id == selectedChartProductId
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF10B981) else Color.Transparent),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .pointerInput(Unit) {
                                                detectTapGestures { selectedChartProductId = prod.id }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(prod.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                            Text("$${prod.retailPrice}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                        }
                                    }
                                }
                            }

                            val selectedProd = catalog.find { it.id == selectedChartProductId } ?: catalog.firstOrNull()
                            val target = activeAlerts[selectedChartProductId] ?: ((selectedProd?.retailPrice ?: 20.0) * 0.88)

                            if (selectedProd != null) {
                                RechartsPriceTrendChart(
                                    productId = selectedProd.id,
                                    productName = selectedProd.name,
                                    targetPriceThreshold = target,
                                    priceHistory = historyFromRoom,
                                    onSimulateHit = {
                                        PriceAlertManager.simulateTargetPriceHit(
                                            context = context,
                                            productId = selectedProd.id,
                                            productName = selectedProd.name,
                                            simulatedDroppedPrice = target * 0.96
                                        )
                                        android.widget.Toast.makeText(context, "تم إرسال إشعار FCM للهدف 🎯", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    // TAB 2: TARGET PRICE ALERTS & REAL-TIME FCM MONITORING
                    2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "نظام إشعارات Firebase Messaging اللحظي يراقب الأسعار في كاش رادار الأسعار ويرسل تنبيهاً فورياً عند وصول السعر للهدف.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF065F46)
                                    )
                                }
                            }

                            if (activeAlerts.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("لا توجد تنبيهات أسعار نشطة حالياً", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("قم بتفعيل التنبيه لأي منتج من تبويب 'رادار الأسعار'", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 340.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(activeAlerts.entries.toList()) { entry ->
                                        val prodId = entry.key
                                        val targetP = entry.value
                                        val prod = catalog.find { it.id == prodId }
                                        val currentPrice = prod?.retailPrice ?: 0.0
                                        val targetReached = currentPrice <= targetP

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            ),
                                            border = if (targetReached) BorderStroke(1.dp, Color(0xFF10B981)) else null
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(prod?.name ?: "منتج #$prodId", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("السعر: $${currentPrice}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Spacer(Modifier.width(8.dp))
                                                        Text("الهدف: $${String.format("%.2f", targetP)}", fontSize = 10.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                                    }
                                                    if (targetReached) {
                                                        Text("🔥 وصل السعر المستهدف!", fontSize = 9.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    // Test Push Trigger
                                                    IconButton(
                                                        onClick = {
                                                            PriceAlertManager.simulateTargetPriceHit(
                                                                context = context,
                                                                productId = prodId,
                                                                productName = prod?.name ?: "منتج",
                                                                simulatedDroppedPrice = targetP * 0.95
                                                            )
                                                            android.widget.Toast.makeText(context, "تم إرسال إشعار FCM فوري 🚀", android.widget.Toast.LENGTH_SHORT).show()
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.Send, contentDescription = "Test Push", tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                                    }

                                                    // Remove alert
                                                    IconButton(
                                                        onClick = {
                                                            PriceAlertManager.removeTargetPrice(context, prodId)
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Smart Basket Optimizer Dialog powered by BasketOptimizerViewModel
 */
@Composable
fun BasketOptimizerDialog(
    cartItems: List<CartItemEntity>,
    onDismiss: () -> Unit,
    onApplyOptimization: (BasketOptimizationResult) -> Unit,
    wishlistProducts: List<TrendingProduct> = emptyList(),
    viewModel: BasketOptimizerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val selectedObjective by viewModel.selectedObjective.collectAsState()
    val optimizationResult by viewModel.optimizationResult.collectAsState()
    val sourceComparisons by viewModel.sourceComparisons.collectAsState()
    val isOptimizing by viewModel.isOptimizing.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var useWishlistSource by remember { mutableStateOf(wishlistProducts.isNotEmpty() && cartItems.isEmpty()) }

    LaunchedEffect(cartItems, wishlistProducts, useWishlistSource) {
        if (useWishlistSource && wishlistProducts.isNotEmpty()) {
            viewModel.optimizeWishlistBasket(wishlistProducts)
        } else {
            viewModel.optimizeBasket(cartItems)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingBasket, contentDescription = null, tint = Color(0xFFF59E0B))
                        Spacer(Modifier.width(8.dp))
                        Text("محسن السلة الذكي (Basket Optimizer)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Text(
                    "مقارنة حية لأسعار السلة بين مصادر السوق (جملة، تجزئة، مزارع) وإعادة توزيع ذكية لتوفير أقصى مبلغ.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Sub-tabs: Optimized Splits vs Source Comparisons
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("خطة التوزيع", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("مقارنة الأسعار", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("✨ سلع مكملة وترندات", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                    )
                }

                // Source Switcher (Active Cart vs Wishlist)
                if (wishlistProducts.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            onClick = {
                                useWishlistSource = false
                                viewModel.optimizeBasket(cartItems)
                            },
                            color = if (!useWishlistSource) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "🛒 سلة الشراء الحالية (${cartItems.size})",
                                fontSize = 11.sp,
                                fontWeight = if (!useWishlistSource) FontWeight.Bold else FontWeight.Normal,
                                color = if (!useWishlistSource) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }

                        Surface(
                            onClick = {
                                useWishlistSource = true
                                viewModel.optimizeWishlistBasket(wishlistProducts)
                            },
                            color = if (useWishlistSource) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "❤️ قائمة الأمنيات المحفوظة (${wishlistProducts.size})",
                                fontSize = 11.sp,
                                fontWeight = if (useWishlistSource) FontWeight.Bold else FontWeight.Normal,
                                color = if (useWishlistSource) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                // Objectives Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(OptimizationObjective.entries) { obj ->
                        FilterChip(
                            selected = selectedObjective == obj,
                            onClick = {
                                if (useWishlistSource && wishlistProducts.isNotEmpty()) {
                                    viewModel.setObjectiveForWishlist(obj, wishlistProducts)
                                } else {
                                    viewModel.setObjective(obj, cartItems)
                                }
                            },
                            label = { Text(obj.labelAr, fontSize = 11.sp) }
                        )
                    }
                }

                if (isOptimizing) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else {
                    when (selectedTab) {
                        0 -> {
                            // Optimal Splits View
                            optimizationResult?.let { res ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("السعر الأصلي", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("$${String.format("%.2f", res.originalTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Column {
                                                Text("السعر بعد التحسين", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("$${String.format("%.2f", res.optimizedTotal)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                            }
                                            Column {
                                                Text("إجمالي التوفير", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("-$${String.format("%.2f", res.totalSavings)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                            }
                                        }

                                        Text("خطة التنفيذ: ${res.strategyExplanationAr}", fontSize = 11.sp)

                                        // Store splits preview
                                        res.storeSplits.forEach { split ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(split.storeName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        Text("${split.itemNames.size} عناصر • توصيل خلال ${split.etaMinutes} دقيقة", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                    Text("$${String.format("%.2f", split.subtotal + split.deliveryFee)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }

                                        Text(
                                            "Evidence ID: ${res.evidenceId}",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.applyOptimization {
                                            onApplyOptimization(res)
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("اعتماد وتطبيق خطة التوفير على السلة")
                                }
                            }
                        }
                        1 -> {
                            // Multi-Source Comparisons View
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 280.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(sourceComparisons) { comp ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (comp.isBestPrice) Color(0xFFECFDF5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        border = if (comp.isBestPrice) BorderStroke(1.dp, Color(0xFF10B981)) else null
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(comp.sourceNameAr, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    if (comp.isBestPrice) {
                                                        Spacer(Modifier.width(4.dp))
                                                        Surface(color = Color(0xFF10B981), shape = RoundedCornerShape(4.dp)) {
                                                            Text("أفضل سعر", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                        }
                                                    }
                                                    if (comp.isFastest) {
                                                        Spacer(Modifier.width(4.dp))
                                                        Surface(color = Color(0xFF3B82F6), shape = RoundedCornerShape(4.dp)) {
                                                            Text("الأسرع", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                        }
                                                    }
                                                }
                                                Text("الفئة: ${comp.sourceCategory} • وصول: ${comp.etaMinutes} دقيقة", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                if (comp.savingsVsCurrent > 0) {
                                                    Text("وفر: $${String.format("%.2f", comp.savingsVsCurrent)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                                }
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("$${String.format("%.2f", comp.totalCost)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Text("رسوم التوصيل: $${String.format("%.2f", comp.deliveryFee)}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Complementary Items & Cross-Store Marketplace Savings View
                            val complementaryItems by viewModel.complementaryItems.collectAsState()
                            if (complementaryItems.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                                    Text("أضف منتجات إلى السلة لعرض السلع المكملة وترندات التوفير", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 280.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(complementaryItems) { comp ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth().testTag("dialog_comp_${comp.productId}"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(comp.productNameAr, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Text("مكمل لـ: ${comp.pairedWithCartItemName} • ${comp.category}", fontSize = 10.sp, color = Color(0xFF38BDF8))
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text("$${String.format("%.2f", comp.crossStoreBestPrice)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                                        Surface(color = Color(0xFF10B981).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                                            Text("وفر $${String.format("%.2f", comp.crossStoreSavingsVsRetail)}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                        }
                                                    }
                                                }
                                                Text(comp.pairingReasonAr, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF0F172A))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(comp.marketplaceTrendText, fontSize = 9.sp, color = Color(0xFFFBBF24), modifier = Modifier.weight(1f))
                                                    Spacer(Modifier.width(6.dp))
                                                    Button(
                                                        onClick = { viewModel.addComplementaryItemToBasket(comp) },
                                                        shape = RoundedCornerShape(6.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(26.dp)
                                                    ) {
                                                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                        Spacer(Modifier.width(3.dp))
                                                        Text("إضافة 🛒", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * AI Shopping Missions Dialog
 */
@Composable
fun ShoppingMissionDialog(
    catalog: List<ProductEntity>,
    onDismiss: () -> Unit,
    onAddMissionItemsToCart: (List<MissionItem>) -> Unit
) {
    var missionPrompt by remember { mutableStateOf("عزومة عشاء لـ 10 أشخاص بميزانية 70 دينار") }
    var budgetValue by remember { mutableDoubleStateOf(70.0) }
    var missionResult by remember { mutableStateOf<ShoppingMission?>(null) }

    LaunchedEffect(missionPrompt, budgetValue) {
        missionResult = VisionXService.planShoppingMission(missionPrompt, budgetValue, catalog)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF8B5CF6))
                        Spacer(Modifier.width(8.dp))
                        Text("مهام الشراء بالذكاء الاصطناعي (Missions)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Text(
                    "حدد هدفك وميزانيتك، وتقوم منظومة الوكلاء بحساب وتجميع كافة الاحتياجات كمسودة جاهزة.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = missionPrompt,
                    onValueChange = { missionPrompt = it },
                    label = { Text("الهدف الشرائي") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                missionResult?.let { mission ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("الميزانية المحددة: $${mission.targetBudget}", fontSize = 12.sp)
                                Text("الإجمالي المحسوب: $${String.format("%.2f", mission.calculatedTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }

                            Text("العناصر المقترحة (${mission.draftItems.size} أصناف):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            mission.draftItems.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("• ${item.name} (${item.quantity}x)", fontSize = 11.sp)
                                    Text("$${item.totalPrice}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(Modifier.height(4.dp))
                            Text("رأي الوكلاء المعتمدين:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            mission.agentInsights.take(3).forEach { insight ->
                                Text("• ${insight.agentNameAr}: ${insight.insightTextAr}", fontSize = 10.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            onAddMissionItemsToCart(mission.draftItems)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("اعتماد وإضافة مسودة المهمة للسلة")
                    }
                }
            }
        }
    }
}

/**
 * Receipt Intelligence Dialog
 */
@Composable
fun ReceiptIntelligenceDialog(
    onDismiss: () -> Unit,
    onSubmitClaim: (String) -> Unit
) {
    val scanResult = remember { VisionXService.scanReceiptIntelligence("", null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFFEC4899))
                        Spacer(Modifier.width(8.dp))
                        Text("تحليل الفواتير الذكي (Receipts)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Text(
                    "مطابقة بنود الفاتورة مع أسعار الكتالوج المعتمدة وكشف أي زيادة سعر أو تلاعب تلقائياً.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("المتجر: ${scanResult.merchantName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("تاريخ الفاتورة: ${scanResult.purchaseTimestamp}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        scanResult.lineItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (item.hasDiscrepancy) Color(0xFFEF4444).copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(4.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.itemName} (${item.quantity}x)", fontSize = 11.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("المحسوب: $${item.billedPrice}", fontSize = 11.sp, fontWeight = if (item.hasDiscrepancy) FontWeight.Bold else FontWeight.Normal, color = if (item.hasDiscrepancy) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface)
                                    if (item.hasDiscrepancy) {
                                        Text("الأصل: $${item.expectedCatalogPrice}", fontSize = 10.sp, color = Color(0xFF10B981))
                                    }
                                }
                            }
                        }

                        if (scanResult.disputeDraftReady) {
                            Surface(
                                color = Color(0xFFEF4444).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    scanResult.disputeRecommendationAr,
                                    fontSize = 11.sp,
                                    color = Color(0xFFEF4444),
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                }

                if (scanResult.disputeDraftReady) {
                    Button(
                        onClick = {
                            onSubmitClaim(scanResult.evidenceId)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("تقديم طلب استرداد فوري مع الإثبات")
                    }
                }
            }
        }
    }
}

/**
 * Order Digital Twin Dialog
 */
@Composable
fun OrderDigitalTwinDialog(
    order: OrderEntity,
    onDismiss: () -> Unit
) {
    val twin = remember(order) { VisionXService.getOrderDigitalTwin(order) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF3B82F6))
                        Spacer(Modifier.width(8.dp))
                        Text("التوأم الرقمي للطلب (Digital Twin)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                Text(
                    "الطلب #${twin.orderId}: ${twin.productName} (${twin.quantity}x) • السائق: ${twin.driverName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الحالة الحالية: ${twin.currentStage.titleAr}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF3B82F6))
                        if (twin.etaMinutes > 0) {
                            Text("الوصول خلال: ${twin.etaMinutes} دقيقة", fontSize = 11.sp, color = Color(0xFF3B82F6))
                        }
                    }
                }

                Text("سجل الأحداث الموثق رقمياً بالأدلة:", fontSize = 11.sp, fontWeight = FontWeight.Bold)

                LazyColumn(
                    modifier = Modifier.heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(twin.events) { ev ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ev.stage.titleAr, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(ev.noteAr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Ev: ${ev.evidenceId}", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF10B981))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * AI Creative Studio Dialog with Ad Copy and AI Image Editor
 */
@Composable
fun CreativeStudioDialog(
    onDismiss: () -> Unit
) {
    var studioTab by remember { mutableIntStateOf(0) }
    var selectedType by remember { mutableStateOf(CreativeType.AD_COPY_AR) }
    var briefPrompt by remember { mutableStateOf("عسل طبيعي جبلي وزيت زيتون بكر فاخر") }
    var jobResult by remember { mutableStateOf<CreativeJob?>(null) }

    LaunchedEffect(selectedType, briefPrompt) {
        jobResult = VisionXService.createCreativeJob(selectedType, briefPrompt)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFF06B6D4))
                        Spacer(Modifier.width(8.dp))
                        Text("استوديو الإبداع (Creative Studio)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                }

                // Studio Mode Tabs
                TabRow(
                    selectedTabIndex = studioTab,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = studioTab == 0,
                        onClick = { studioTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("محرر الصور الذكي", fontSize = 12.sp, fontWeight = if (studioTab == 0) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                    Tab(
                        selected = studioTab == 1,
                        onClick = { studioTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("الموجز الإعلاني", fontSize = 12.sp, fontWeight = if (studioTab == 1) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }

                when (studioTab) {
                    0 -> {
                        // AI Product Photo Editor Interface
                        AiProductImageEditor(
                            onEnhancedApplied = { prodName, evId ->
                                // Handled in component
                            }
                        )
                    }
                    1 -> {
                        // Ad Copy & Campaign Generation Interface
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "توليد محتوى ترويجي ثنائي اللغة AR/EN ومواد بصرية مع وثيقة الإثبات الرقمي.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(CreativeType.entries) { type ->
                                    FilterChip(
                                        selected = selectedType == type,
                                        onClick = { selectedType = type },
                                        label = { Text(type.titleAr, fontSize = 11.sp) }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = briefPrompt,
                                onValueChange = { briefPrompt = it },
                                label = { Text("الموجز الإعلاني / تفاصيل المنتج") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            jobResult?.let { job ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("المخرجات الإبداعية المعتمدة:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(job.generatedArtifact, fontSize = 12.sp)

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        Text(
                                            "Evidence ID: ${job.evidenceId}",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
