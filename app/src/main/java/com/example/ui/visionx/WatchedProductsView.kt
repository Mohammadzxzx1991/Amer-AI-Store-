package com.example.ui.visionx

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.ProductEntity
import com.example.ui.MarketViewModel
import com.example.ui.theme.PrimaryCyan

/**
 * Watched Products & Price Alerts View
 * Displays products tracked by the user with target price points,
 * and allows triggering real Firebase Messaging push notifications.
 */
@Composable
fun WatchedProductsView(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier,
    onNavigateToProduct: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val lang by viewModel.appLanguage.collectAsState()
    val isDark by viewModel.appDarkMode.collectAsState()
    val watchedAlerts by viewModel.targetPriceAlerts.collectAsState()
    val allProducts by viewModel.products.collectAsState()

    var productForEditAlert by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddWatchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Provide default watched items if list is empty so user can immediately experience the feature
    LaunchedEffect(allProducts) {
        if (watchedAlerts.isEmpty() && allProducts.isNotEmpty()) {
            val sampleProduct1 = allProducts.firstOrNull()
            val sampleProduct2 = allProducts.getOrNull(1)
            sampleProduct1?.let {
                viewModel.setTargetPriceAlert(it, it.retailPrice * 0.85) // 15% discount target
            }
            sampleProduct2?.let {
                viewModel.setTargetPriceAlert(it, it.retailPrice * 0.90) // 10% discount target
            }
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("section_watched_products")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (lang == "ar") "قائمة المراقبة وتنبيهات السعر" else "Watched List & Price Alerts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF3B82F6).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Firebase Messaging",
                                    color = Color(0xFF3B82F6),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (lang == "ar") "إشعارات فورية عبر FCM عند وصول السعر للهدف" else "Instant push alerts when target price is reached",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { showAddWatchDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_add_to_watched_list")
                ) {
                    Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (lang == "ar") "مراقبة منتج +" else "Watch Item +", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick sync / check button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (lang == "ar") "${watchedAlerts.size} منتجات قيد المراقبة الآن" else "${watchedAlerts.size} items actively monitored",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = {
                        viewModel.checkAllWatchedTargetPrices(context)
                        android.widget.Toast.makeText(context, "جاري فحص أسعار المراقبة ومزامنة Firebase...", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = PrimaryCyan)
                    Spacer(Modifier.width(4.dp))
                    Text(if (lang == "ar") "فحص الأسعار الآن" else "Check Prices", fontSize = 11.sp, color = PrimaryCyan, fontWeight = FontWeight.Bold)
                }
            }

            // Watched Items Cards
            if (watchedAlerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                        Text(
                            text = if (lang == "ar") "لا توجد منتجات في قائمة المراقبة حالياً" else "No items currently in your Watched list",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    watchedAlerts.forEach { alert ->
                        val matchingProd = allProducts.find { it.id == alert.productId }
                        val currentPrice = matchingProd?.retailPrice ?: alert.currentPrice
                        val isTargetReached = currentPrice <= alert.targetPrice
                        val savingsNeeded = (currentPrice - alert.targetPrice).coerceAtLeast(0.0)

                        // 30-day Price History state for Recharts
                        var isChartExpanded by remember { mutableStateOf(false) }
                        val priceHistoryDao = remember { com.example.data.visionx.PriceRadarService.getPriceHistoryDao() }
                        val historyList by (priceHistoryDao?.getHistoryForProductAsc(alert.productId) ?: kotlinx.coroutines.flow.flowOf(emptyList()))
                            .collectAsState(initial = emptyList())

                        LaunchedEffect(isChartExpanded) {
                            if (isChartExpanded) {
                                com.example.data.visionx.PriceRadarService.ensureHistoricalPriceDataInRoom(
                                    productId = alert.productId,
                                    productName = alert.productName,
                                    basePrice = currentPrice,
                                    targetThreshold = alert.targetPrice
                                )
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTargetReached) Color(0xFF10B981).copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            border = BorderStroke(
                                width = if (isTargetReached) 1.5.dp else 1.dp,
                                color = if (isTargetReached) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("watched_card_${alert.productId}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = matchingProd?.imageUrl ?: "https://images.unsplash.com/photo-1542838132-92c53300491e",
                                        contentDescription = alert.productName,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = alert.productName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "السعر الحالي: $${String.format("%.2f", currentPrice)}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = "الهدف: $${String.format("%.2f", alert.targetPrice)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isTargetReached) Color(0xFF10B981) else Color(0xFFF59E0B)
                                            )
                                        }
                                        if (!isTargetReached && savingsNeeded > 0) {
                                            Text(
                                                text = "يتبقى $${String.format("%.2f", savingsNeeded)} للوصول للهدف",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                        }
                                    }

                                    // Status Badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isTargetReached) Color(0xFF10B981) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isTargetReached) Icons.Default.CheckCircle else Icons.Default.Timer,
                                                contentDescription = null,
                                                tint = if (isTargetReached) Color.White else Color(0xFFF59E0B),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = if (isTargetReached) "وصل للهدف 🎯" else "مراقب 🔔",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isTargetReached) Color.White else Color(0xFFF59E0B)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Action Buttons: Test Push Notification, Recharts 30-Day Chart Toggle, Edit Target, Remove
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        // Trigger Push Notification Test
                                        Button(
                                            onClick = {
                                                // Simulate price reaching target price point and trigger Firebase Messaging notification
                                                viewModel.simulatePriceDropTrigger(
                                                    productId = alert.productId,
                                                    newDroppedPrice = alert.targetPrice,
                                                    context = context
                                                )
                                                android.widget.Toast.makeText(context, "تم إرسال إشعار Firebase Messaging التجريبي بنجاح! 🔔", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp).testTag("btn_test_notification_${alert.productId}")
                                        ) {
                                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(3.dp))
                                            Text(if (lang == "ar") "اختبار 🔔" else "Alert 🔔", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        // 30-Day Recharts Chart Toggle
                                        OutlinedButton(
                                            onClick = { isChartExpanded = !isChartExpanded },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isChartExpanded) Color(0xFF0284C7).copy(alpha = 0.15f) else Color.Transparent
                                            ),
                                            border = BorderStroke(1.dp, if (isChartExpanded) Color(0xFF0284C7) else MaterialTheme.colorScheme.outlineVariant),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp).testTag("btn_toggle_recharts_${alert.productId}")
                                        ) {
                                            Icon(
                                                Icons.Default.ShowChart,
                                                contentDescription = "Recharts",
                                                tint = if (isChartExpanded) Color(0xFF0284C7) else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = if (isChartExpanded) "إخفاء Recharts" else "مخطط 30 يوم (Recharts)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isChartExpanded) Color(0xFF0284C7) else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                productForEditAlert = matchingProd ?: ProductEntity(
                                                    id = alert.productId,
                                                    name = alert.productName,
                                                    category = "General",
                                                    retailPrice = alert.currentPrice,
                                                    wholesalePrice = alert.currentPrice * 0.8,
                                                    imageUrl = ""
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(if (lang == "ar") "تعديل" else "Edit", fontSize = 10.sp)
                                        }

                                        IconButton(
                                            onClick = { viewModel.removeTargetPriceAlert(alert.productId) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                // Expandable 30-Day Recharts Price Trend Line Chart Component
                                AnimatedVisibility(visible = isChartExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    ) {
                                        RechartsPriceTrendChart(
                                            productId = alert.productId,
                                            productName = alert.productName,
                                            targetPriceThreshold = alert.targetPrice,
                                            priceHistory = historyList,
                                            onSimulateHit = {
                                                viewModel.simulatePriceDropTrigger(
                                                    productId = alert.productId,
                                                    newDroppedPrice = alert.targetPrice,
                                                    context = context
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
        }
    }

    // Modal to set or edit target price point
    productForEditAlert?.let { prod ->
        PriceAlertSettingsModal(
            product = prod,
            viewModel = viewModel,
            onDismiss = { productForEditAlert = null },
            onAlertSet = { newPrice ->
                productForEditAlert = null
            }
        )
    }

    // Dialog to pick and add product to watched list
    if (showAddWatchDialog) {
        Dialog(onDismissRequest = { showAddWatchDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "ar") "اختر منتجاً لمراقبته" else "Select Product to Watch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        IconButton(onClick = { showAddWatchDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(if (lang == "ar") "بحث عن منتج..." else "Search product...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    val filtered = allProducts.filter {
                        searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered) { p ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = p.imageUrl,
                                        contentDescription = p.name,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = p.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                        Text(text = "$${String.format("%.2f", p.retailPrice)} • ${p.category}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(
                                        onClick = {
                                            showAddWatchDialog = false
                                            productForEditAlert = p
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                    ) {
                                        Text(if (lang == "ar") "تحديد الهدف 🔔" else "Set Target 🔔", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
