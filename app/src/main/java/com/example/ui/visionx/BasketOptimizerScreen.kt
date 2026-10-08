package com.example.ui.visionx

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
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
import com.example.data.CartItemEntity
import com.example.data.ProductEntity
import com.example.data.visionx.*
import com.example.ui.MarketViewModel
import com.example.ui.theme.*

/**
 * Basket Optimizer Screen
 * Analyzes multiple products added by the user and suggests the most cost-effective
 * merchant combination using Firestore merchant & price comparison data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasketOptimizerScreen(
    marketViewModel: MarketViewModel,
    basketViewModel: BasketOptimizerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit,
    onApplyToCart: (BasketOptimizationResult) -> Unit = {}
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val lang by marketViewModel.appLanguage.collectAsState()
    val isDark by marketViewModel.appDarkMode.collectAsState()
    val allCatalogProducts by marketViewModel.products.collectAsState()
    val currentCartItems by marketViewModel.cartItems.collectAsState()
    val wishlistProducts by marketViewModel.savedProducts.collectAsState()

    val basketItems by basketViewModel.basketItems.collectAsState()
    val selectedObjective by basketViewModel.selectedObjective.collectAsState()
    val optimizationResult by basketViewModel.optimizationResult.collectAsState()
    val sourceComparisons by basketViewModel.sourceComparisons.collectAsState()
    val isOptimizing by basketViewModel.isOptimizing.collectAsState()
    val isFirestoreSyncing by basketViewModel.isFirestoreSyncing.collectAsState()
    val firestoreSyncMessage by basketViewModel.firestoreSyncMessage.collectAsState()
    val appliedStatus by basketViewModel.appliedStatus.collectAsState()
    val savingSuggestions by basketViewModel.savingSuggestions.collectAsState()
    val freeShippingProgress by basketViewModel.freeShippingProgress.collectAsState()
    val priceRadarAlternatives by basketViewModel.priceRadarAlternatives.collectAsState()
    val totalSuggestedSavings by basketViewModel.totalSuggestedSavings.collectAsState()
    val complementaryItems by basketViewModel.complementaryItems.collectAsState()
    val capturedVisionItems by basketViewModel.capturedVisionItems.collectAsState()
    val bundleDiscounts by basketViewModel.bundleDiscounts.collectAsState()
    val budgetAlternatives by basketViewModel.budgetAlternatives.collectAsState()
    val calculationSummary by basketViewModel.calculationSummary.collectAsState()
    val trackedBasketItems by basketViewModel.trackedBasketItems.collectAsState()
    val fcmNotificationStatus by basketViewModel.fcmNotificationStatus.collectAsState()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var itemToTrackForAlert by remember { mutableStateOf<CartItemEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showFirestoreSavedBanner by remember { mutableStateOf(false) }

    // Continuously monitor the user's current shopping cart and optimize against marketplace trends
    LaunchedEffect(currentCartItems) {
        if (currentCartItems.isNotEmpty()) {
            basketViewModel.loadFromCart(currentCartItems)
        } else if (basketItems.isEmpty() && allCatalogProducts.isNotEmpty()) {
            basketViewModel.loadFromProductEntities(allCatalogProducts.take(3))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (lang == "ar") "محسن السلة الذكي" else "Basket Optimizer",
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Firestore Data",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (lang == "ar") "اقتراح أفضل تشكيلة تجار موفرة عبر السحابة" else "Optimal cost-effective merchant split via Firestore",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_basket_optimizer")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            basketViewModel.syncFirestoreMerchantData {
                                android.widget.Toast.makeText(context, "تم تحديث بيانات التجار من Firestore!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("btn_sync_firestore")
                    ) {
                        if (isFirestoreSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = "Sync Firestore", tint = PrimaryCyan)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (optimizationResult != null && basketItems.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (lang == "ar") "التكلفة المحسّنة (موزعة):" else "Optimized Combo Total:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$${String.format("%.2f", optimizationResult!!.optimizedTotal)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        color = Color(0xFF10B981)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "$${String.format("%.2f", optimizationResult!!.originalTotal)}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                        )
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        basketViewModel.saveOptimizationPlanToFirestore(optimizationResult!!) { success ->
                                            showFirestoreSavedBanner = true
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, PrimaryCyan),
                                    modifier = Modifier.testTag("btn_save_firestore_plan")
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (lang == "ar") "حفظ في Firestore" else "Save Plan", fontSize = 12.sp, color = PrimaryCyan)
                                }

                                Button(
                                    onClick = {
                                        val res = optimizationResult ?: return@Button
                                        basketViewModel.applyOptimization {
                                            // Transfer items to cart
                                            basketItems.forEach { item ->
                                                val found = allCatalogProducts.find { it.id == item.productId } ?: ProductEntity(
                                                    id = item.productId,
                                                    name = item.productName,
                                                    category = "General",
                                                    retailPrice = item.price * 0.82,
                                                    wholesalePrice = item.price * 0.70,
                                                    imageUrl = item.imageUrl
                                                )
                                                marketViewModel.addProductToCart(found, item.quantity, "Retail")
                                            }
                                            onApplyToCart(res)
                                            android.widget.Toast.makeText(context, "تم تطبيق السلة الموزعة ونقل المنتجات لسلتك!", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    modifier = Modifier.testTag("btn_apply_optimized_basket")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (lang == "ar") "تطبيق التوزيع 🚀" else "Apply Split 🚀", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Status and sync banner
            item {
                Spacer(Modifier.height(4.dp))
                AnimatedVisibility(visible = firestoreSyncMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = firestoreSyncMessage ?: "",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (showFirestoreSavedBanner) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "ar") "تم تسجيل وحفظ خطة المتاجر في Firestore بنجاح!" else "Optimization plan persisted to Firestore!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                            IconButton(onClick = { showFirestoreSavedBanner = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 1.5 Applied Feedback Banner
            if (appliedStatus != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Celebration, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = appliedStatus ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }

            // 1.6 FCM Price Drop Push Notification Status Banner
            if (fcmNotificationStatus != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth().testTag("fcm_notification_status_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = fcmNotificationStatus ?: "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 1.7 Tracked Basket Items with FCM Price Drop Radar
            if (trackedBasketItems.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().testTag("tracked_basket_items_section")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Radar, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (lang == "ar") "🔔 سلع متتبعة في السلة (إشعارات FCM)" else "🔔 Tracked Items (FCM Alerts)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${trackedBasketItems.size} متتبع",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            trackedBasketItems.forEach { tracked ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = {
                                        if (it == SwipeToDismissBoxValue.EndToStart) {
                                            basketViewModel.stopTrackingBasketItem(tracked.productId)
                                            true
                                        } else false
                                    }
                                )
                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = {
                                        val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(color)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                        }
                                    },
                                    content = {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF1E293B),
                                            border = BorderStroke(1.dp, Color(0xFF334155)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(tracked.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White, maxLines = 1)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("الحالي: $${String.format("%.2f", tracked.currentPrice)}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                                        Spacer(Modifier.width(8.dp))
                                                        Text("← الهدف: $${String.format("%.2f", tracked.targetThresholdPrice)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                                    }
                                                    Text("موضوع الإشعار: ${tracked.fcmTopic}", fontSize = 9.sp, color = Color(0xFF64748B))
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    // Test simulate price drop FCM notification button
                                                    FilledTonalButton(
                                                        onClick = {
                                                            basketViewModel.simulatePriceDropNotification(
                                                                context = context,
                                                                productId = tracked.productId,
                                                                simulatedNewPrice = tracked.targetThresholdPrice * 0.95
                                                            )
                                                        },
                                                        shape = RoundedCornerShape(6.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                                        modifier = Modifier.height(28.dp).testTag("btn_simulate_drop_${tracked.productId}")
                                                    ) {
                                                        Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                                                        Spacer(Modifier.width(3.dp))
                                                        Text("تجربة الإشعار ⚡", fontSize = 9.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                                    }

                                                    IconButton(
                                                        onClick = { basketViewModel.stopTrackingBasketItem(tracked.productId) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.NotificationsOff, contentDescription = "Stop Tracking", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // AI Money-Saving Suggestions Section: Free Shipping + Price Radar Cheaper Alternatives
            if (basketItems.isNotEmpty() && (freeShippingProgress != null || priceRadarAlternatives.isNotEmpty())) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF06B6D4), Color(0xFF10B981)))),
                        modifier = Modifier.fillMaxWidth().testTag("ai_savings_suggestions_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
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
                                            .background(Color(0xFF06B6D4).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Savings, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (lang == "ar") "💡 مقترحات الذكاء الاصطناعي للتوفير" else "💡 AI Money-Saving Insights",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (lang == "ar") "فرص توفير متاحة بقيمة: $${String.format("%.2f", totalSuggestedSavings)}" else "Potential savings found: $${String.format("%.2f", totalSuggestedSavings)}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (priceRadarAlternatives.isNotEmpty()) {
                                    Button(
                                        onClick = { basketViewModel.applyAllCheaperAlternatives() },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("btn_swap_all_alternatives")
                                    ) {
                                        Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (lang == "ar") "استبدال الكل الأوفر ⚡" else "Swap All ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            // 1. Free Shipping Progress & Smart Fillers
                            freeShippingProgress?.let { fs ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (fs.isQualified) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFF0F172A),
                                    border = BorderStroke(1.dp, if (fs.isQualified) Color(0xFF10B981) else Color(0xFF334155)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (fs.isQualified) Icons.Default.CheckCircle else Icons.Default.LocalShipping,
                                                    contentDescription = null,
                                                    tint = if (fs.isQualified) Color(0xFF10B981) else Color(0xFF38BDF8),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = if (fs.isQualified)
                                                        (if (lang == "ar") "🎉 شحن مجاني مفعل بالكامل (وفرت $4.99)" else "🎉 Free Shipping Qualified (Saved $4.99)")
                                                    else
                                                        (if (lang == "ar") "🚚 تبقّى $${String.format("%.2f", fs.remainingAmount)} للتأهل للشحن المجاني" else "🚚 Add $${String.format("%.2f", fs.remainingAmount)} for Free Shipping"),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (fs.isQualified) Color(0xFF10B981) else Color.White
                                                )
                                            }

                                            Text(
                                                text = "$${String.format("%.2f", fs.currentSubtotal)} / $${String.format("%.2f", fs.freeShippingThreshold)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }

                                        // Progress bar
                                        val progress = (fs.currentSubtotal / fs.freeShippingThreshold).coerceIn(0.0, 1.0).toFloat()
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                            color = Color(0xFF10B981),
                                            trackColor = Color(0xFF334155)
                                        )

                                        // Smart Fillers Suggestions to Reach Free Shipping
                                        if (!fs.isQualified && fs.remainingAmount <= 15.0) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = if (lang == "ar") "اختر منتجاً تكميلياً ذكياً لإلغاء كلفة التوصيل ($4.99):" else "Add a smart complementary item to unlock free delivery ($4.99):",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF94A3B8)
                                                )
                                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    items(fs.smartFillers) { (name, price) ->
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = Color(0xFF1E293B),
                                                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                                                            modifier = Modifier.clickable {
                                                                basketViewModel.addSmartFiller(name, price)
                                                            }
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text("+ $name", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                                Spacer(Modifier.width(4.dp))
                                                                Text("($${String.format("%.2f", price)})", fontSize = 10.sp, color = Color(0xFF38BDF8))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Price Radar Cheaper Alternatives
                            if (priceRadarAlternatives.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (lang == "ar") "🔍 بدائل أرخص من قاعدة بيانات رادار الأسعار (Price Radar):" else "🔍 Cheaper Alternatives from Price Radar DB:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8)
                                        )
                                        Text(
                                            text = "${priceRadarAlternatives.size} بديل متوفر",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }

                                    priceRadarAlternatives.forEach { alt ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF0F172A),
                                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(alt.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White, maxLines = 1)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("السعر الحالي: $${String.format("%.2f", alt.currentCartPrice)}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                                        Spacer(Modifier.width(6.dp))
                                                        Text("← رادار الأسعار: $${String.format("%.2f", alt.priceRadarPrice)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                                    }
                                                    Text("المتجر: ${alt.cheaperMerchant}", fontSize = 9.sp, color = Color(0xFF64748B))
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFF10B981).copy(alpha = 0.2f)
                                                    ) {
                                                        Text(
                                                            text = "وفر $${String.format("%.2f", alt.totalSavings)}",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = Color(0xFF10B981),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(Modifier.height(4.dp))
                                                    OutlinedButton(
                                                        onClick = { basketViewModel.swapWithCheaperAlternative(alt) },
                                                        shape = RoundedCornerShape(6.dp),
                                                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(26.dp)
                                                    ) {
                                                        Text("استبدال ⚡", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 3. Complementary Items & Cross-Store Marketplace Savings Section
                                if (complementaryItems.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = if (lang == "ar") "✨ سلع مكملة لسلتك مع توفير عبر المتاجر" else "✨ Complementary Items & Cross-Store Deals",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFF59E0B)
                                                )
                                            }
                                            Text(
                                                text = if (lang == "ar") "بناءً على ترندات السوق 📈" else "Marketplace Trends 📈",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }

                                        complementaryItems.forEach { comp ->
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFF0F172A),
                                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)),
                                                modifier = Modifier.fillMaxWidth().testTag("comp_item_${comp.productId}")
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.weight(1f),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            AsyncImage(
                                                                model = comp.imageUrl.ifEmpty { "https://images.unsplash.com/photo-1542838132-92c53300491e" },
                                                                contentDescription = comp.productNameAr,
                                                                modifier = Modifier
                                                                    .size(44.dp)
                                                                    .clip(RoundedCornerShape(8.dp)),
                                                                contentScale = ContentScale.Crop
                                                            )
                                                            Spacer(Modifier.width(10.dp))
                                                            Column {
                                                                Text(
                                                                    text = comp.productNameAr,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 12.sp,
                                                                    color = Color.White
                                                                )
                                                                Text(
                                                                    text = "مكمل لـ: ${comp.pairedWithCartItemName} • ${comp.category}",
                                                                    fontSize = 10.sp,
                                                                    color = Color(0xFF38BDF8)
                                                                )
                                                            }
                                                        }

                                                        Column(horizontalAlignment = Alignment.End) {
                                                            Text(
                                                                text = "$${String.format("%.2f", comp.crossStoreBestPrice)}",
                                                                fontWeight = FontWeight.Black,
                                                                fontSize = 13.sp,
                                                                color = Color(0xFF10B981)
                                                            )
                                                            Surface(
                                                                shape = RoundedCornerShape(4.dp),
                                                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                                                            ) {
                                                                Text(
                                                                    text = "وفر $${String.format("%.2f", comp.crossStoreSavingsVsRetail)}",
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFF10B981),
                                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Text(
                                                        text = comp.pairingReasonAr,
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF94A3B8)
                                                    )

                                                    // Marketplace Trend & Store Price Comparison Badge
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFF1E293B))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = comp.marketplaceTrendText,
                                                            fontSize = 9.sp,
                                                            color = Color(0xFFFBBF24),
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Button(
                                                            onClick = {
                                                                val matching = allCatalogProducts.find { it.id == comp.productId }
                                                                if (matching != null) {
                                                                    marketViewModel.addProductToCart(matching, 1, "Retail")
                                                                }
                                                                basketViewModel.addComplementaryItemToBasket(comp)
                                                            },
                                                            shape = RoundedCornerShape(6.dp),
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(26.dp)
                                                        ) {
                                                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                            Spacer(Modifier.width(3.dp))
                                                            Text("إضافة للسلة 🛒", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
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

            // 2. Added Products Header & Quick Import Controls
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                        .background(PrimaryCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (lang == "ar") "منتجات سلة التحليل" else "Products in Basket",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (lang == "ar") "${basketItems.size} منتجات مضافة للتحسين" else "${basketItems.size} items added to analyze",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { showAddProductDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                                modifier = Modifier.testTag("btn_add_product_to_basket")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (lang == "ar") "إضافة منتج +" else "Add Item +", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // Quick Import actions row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { basketViewModel.loadFromCart(currentCartItems) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (lang == "ar") "استيراد السلة" else "Import Cart", fontSize = 11.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    val mapped = wishlistProducts.map {
                                        CartItemEntity(
                                            userId = 0,
                                            productId = it.id,
                                            productName = it.name,
                                            imageUrl = it.imageUrl,
                                            price = it.retailPrice,
                                            quantity = 1,
                                            orderType = "Retail"
                                        )
                                    }
                                    basketViewModel.loadFromCart(mapped)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (lang == "ar") "استيراد الأمنيات" else "Import Saved", fontSize = 11.sp)
                            }

                            if (basketItems.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { basketViewModel.clearBasket() },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 3. Basket Product Items List
            if (basketItems.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(40.dp))
                            Text(
                                text = if (lang == "ar") "سلة التحليل فارغة حالياً" else "Your analysis basket is empty",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (lang == "ar") "أضف منتجاتك لتحديد المتاجر الأوفر وتوفير ميزانيتك بنقرة واحدة." else "Add products to calculate the most cost-effective merchant split.",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { showAddProductDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                            ) {
                                Text(if (lang == "ar") "تصفح وأضف منتجات +" else "Browse & Add Items +", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(basketItems) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("basket_item_${item.productId}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.productName,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.productName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "$${String.format("%.2f", item.price)} / وحدة",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "الإجمالي: $${String.format("%.2f", item.price * item.quantity)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryCyan
                                )
                            }

                            // Quantity controls
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = { basketViewModel.updateQuantity(item.productId, item.quantity - 1) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                                }

                                Text(
                                    text = "${item.quantity}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                FilledTonalIconButton(
                                    onClick = { basketViewModel.updateQuantity(item.productId, item.quantity + 1) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                                }

                                // FCM Target Price Tracking Trigger
                                val isTracked = trackedBasketItems.any { it.productId == item.productId }
                                IconButton(
                                    onClick = { itemToTrackForAlert = item },
                                    modifier = Modifier.size(28.dp).testTag("btn_track_basket_item_${item.productId}")
                                ) {
                                    Icon(
                                        imageVector = if (isTracked) Icons.Default.NotificationsActive else Icons.Default.NotificationAdd,
                                        contentDescription = "Track Price Drop",
                                        tint = if (isTracked) Color(0xFF10B981) else Color(0xFFF59E0B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { basketViewModel.removeProduct(item.productId) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 4. Strategy Selector Chips
            if (basketItems.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (lang == "ar") "🎯 هدف التحسين المفضل" else "🎯 Optimization Objective",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(OptimizationObjective.values()) { obj ->
                                val isSelected = selectedObjective == obj
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { basketViewModel.setObjective(obj, basketItems) },
                                    label = {
                                        Text(
                                            text = if (lang == "ar") obj.labelAr else obj.labelEn,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when (obj) {
                                                OptimizationObjective.CHEAPEST -> Icons.Default.Savings
                                                OptimizationObjective.FASTEST -> Icons.Default.Bolt
                                                OptimizationObjective.FEWEST_DELIVERIES -> Icons.Default.LocalShipping
                                                OptimizationObjective.BALANCED -> Icons.Default.Balance
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // 5. Hero Optimization Results Card
                item {
                    val result = optimizationResult
                    if (result != null) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFD1FAE5)
                            ),
                            border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = if (lang == "ar") "التشكيلة الأكثر توفيراً بين التجار" else "Cost-Effective Merchant Combo",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isDark) Color.White else Color(0xFF065F46)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF10B981)
                                    ) {
                                        val savingsPct = if (result.originalTotal > 0) (result.totalSavings / result.originalTotal) * 100 else 0.0
                                        Text(
                                            text = "توفير ${String.format("%.1f", savingsPct)}%",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.1f))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = if (lang == "ar") "متجر واحد تقليدي" else "Single Merchant", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "$${String.format("%.2f", result.originalTotal)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(30.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = if (lang == "ar") "التشكيلة المقترحة" else "Optimized Combo", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "$${String.format("%.2f", result.optimizedTotal)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(30.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = if (lang == "ar") "صافي التوفير 💰" else "Net Savings 💰", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "$${String.format("%.2f", result.totalSavings)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }

                                Text(
                                    text = if (lang == "ar") result.strategyExplanationAr else result.strategyExplanationEn,
                                    fontSize = 12.sp,
                                    color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF047857),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // 6. Detailed Merchant Splits Breakdown
                item {
                    val result = optimizationResult
                    if (result != null && result.storeSplits.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (lang == "ar") "🏪 تفاصيل الشراء حسب المتاجر المختارة" else "🏪 Allocation Across Recommended Merchants",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            result.storeSplits.forEach { split ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Storefront, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(text = split.storeName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = "⏱️ ${split.etaMinutes} دقيقة",
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "المنتجات (${split.itemNames.size}): ${split.itemNames.joinToString(", ")}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(text = "$${String.format("%.2f", split.subtotal)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text(text = "+ توصيل $${String.format("%.2f", split.deliveryFee)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. Full Comparison with Individual Merchant Sources
                item {
                    if (sourceComparisons.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (lang == "ar") "📊 مقارنة التكلفة إذا اشتريت السلة كاملة من تاجر واحد" else "📊 Cost if Purchased Entirely from Single Merchant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            sourceComparisons.forEach { source ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                                    border = BorderStroke(
                                        width = if (source.isBestPrice) 1.5.dp else 1.dp,
                                        color = if (source.isBestPrice) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = source.sourceNameAr, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                if (source.isBestPrice) {
                                                    Spacer(Modifier.width(4.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFF10B981).copy(alpha = 0.2f)
                                                    ) {
                                                        Text("الأفضل مفرداً", fontSize = 9.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                    }
                                                }
                                            }
                                            Text(text = "${source.sourceCategory} • توصيل $${String.format("%.2f", source.deliveryFee)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(text = "$${String.format("%.2f", source.totalCost)}", fontWeight = FontWeight.Black, fontSize = 14.sp)
                                            if (source.savingsVsCurrent > 0) {
                                                Text(text = "توفير $${String.format("%.2f", source.savingsVsCurrent)}", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(40.dp))
            }
        }
    }

    // Modal to add products to the basket from store catalog
    if (showAddProductDialog) {
        Dialog(onDismissRequest = { showAddProductDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
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
                            text = if (lang == "ar") "إضافة منتجات لسلة التحليل" else "Add Items to Analyze",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = { showAddProductDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(if (lang == "ar") "ابحث عن منتج..." else "Search products...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    val filteredProducts = allCatalogProducts.filter {
                        searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProducts) { prod ->
                            val isAlreadyInBasket = basketItems.any { it.productId == prod.id }
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = prod.imageUrl,
                                        contentDescription = prod.name,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = prod.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                        Text(text = "$${String.format("%.2f", prod.retailPrice)} • ${prod.category}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(
                                        onClick = {
                                            basketViewModel.addProduct(prod, 1)
                                            showAddProductDialog = false
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isAlreadyInBasket) Color(0xFF10B981) else PrimaryCyan
                                        )
                                    ) {
                                        Text(
                                            text = if (isAlreadyInBasket) (if (lang == "ar") "مضاف ✓" else "Added ✓") else (if (lang == "ar") "إضافة +" else "Add +"),
                                            color = if (isAlreadyInBasket) Color.White else Color.Black,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
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

    // Modal to set user-defined target threshold and start FCM price drop tracking
    itemToTrackForAlert?.let { item ->
        BasketItemPriceTrackingDialog(
            item = item,
            lang = lang,
            onDismiss = { itemToTrackForAlert = null },
            onConfirmTrack = { targetPrice ->
                basketViewModel.trackBasketItemForPriceDrop(
                    productId = item.productId,
                    productName = item.productName,
                    currentPrice = item.price,
                    targetThresholdPrice = targetPrice,
                    imageUrl = item.imageUrl
                )
                itemToTrackForAlert = null
            },
            onSimulateDrop = { targetPrice ->
                basketViewModel.trackBasketItemForPriceDrop(
                    productId = item.productId,
                    productName = item.productName,
                    currentPrice = item.price,
                    targetThresholdPrice = targetPrice,
                    imageUrl = item.imageUrl
                )
                basketViewModel.simulatePriceDropNotification(
                    context = context,
                    productId = item.productId,
                    simulatedNewPrice = targetPrice * 0.95
                )
                itemToTrackForAlert = null
            }
        )
    }
}

/**
 * Dialog to configure user-defined threshold and subscribe to Firebase Cloud Messaging (FCM)
 */
@Composable
fun BasketItemPriceTrackingDialog(
    item: CartItemEntity,
    lang: String,
    onDismiss: () -> Unit,
    onConfirmTrack: (Double) -> Unit,
    onSimulateDrop: (Double) -> Unit
) {
    var thresholdInput by remember { mutableStateOf(String.format("%.2f", item.price * 0.85)) }
    var selectedDiscountPct by remember { mutableIntStateOf(15) }

    val currentPrice = item.price
    val targetVal = thresholdInput.toDoubleOrNull() ?: (currentPrice * 0.85)
    val savings = (currentPrice - targetVal).coerceAtLeast(0.0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("modal_basket_price_tracking")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
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
                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (lang == "ar") "تتبع انخفاض السعر (FCM)" else "Track Price Drop (FCM)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (lang == "ar") "إشعار فوري عند هبوط السعر عن هدفك" else "Push alert when price drops below threshold",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                // Item info summary card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.productName,
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                            Text("السعر الحالي: $${String.format("%.2f", item.price)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Quick percentage discount chips
                Text(
                    text = if (lang == "ar") "اختر نسبة الانخفاض المستهدفة:" else "Target Discount Level:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(10, 15, 20, 30).forEach { pct ->
                        val isSelected = selectedDiscountPct == pct
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDiscountPct = pct
                                thresholdInput = String.format("%.2f", item.price * (1.0 - pct / 100.0))
                            },
                            label = { Text("-$pct%", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Target price threshold input
                OutlinedTextField(
                    value = thresholdInput,
                    onValueChange = {
                        thresholdInput = it
                        selectedDiscountPct = 0
                    },
                    label = { Text(if (lang == "ar") "السعر المستهدف للتنبيه ($)" else "Target Threshold ($)") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF10B981)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_basket_threshold_price")
                )

                // Projected Savings badge
                if (savings > 0) {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("التوفير عند وصول الإشعار:", fontSize = 11.sp, color = Color(0xFF065F46))
                            Text(
                                text = "وفّر $${String.format("%.2f", savings)} (-${((savings / currentPrice) * 100).toInt()}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }

                // Information note
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "يتم إرسال إشعار فوري من Firebase Cloud Messaging عند وصول السعر للهدف.",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSimulateDrop(targetVal) },
                        modifier = Modifier.weight(1f).testTag("btn_test_basket_fcm_drop")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (lang == "ar") "تجربة الإشعار" else "Test Push", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onConfirmTrack(targetVal) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1.2f).testTag("btn_confirm_track_basket_item")
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (lang == "ar") "تفعيل التتبع 🔔" else "Activate 🔔", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
