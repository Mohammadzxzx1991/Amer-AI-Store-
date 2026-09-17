package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ProductEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollapsibleSection(
    title: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .clickable { expanded = !expanded }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 8.dp)) { content() }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchFocused: Boolean,
    onSearchFocusChange: (Boolean) -> Unit,
    recentQueries: List<com.example.data.SearchQueryEntity>,
    onQuerySelect: (String) -> Unit,
    onDeleteQuery: (String) -> Unit,
    onClearAllQueries: () -> Unit,
    onCameraClick: () -> Unit,
    onVoiceClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search products, categories, or deals...", color = Color(0xFF64748B), fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF06B6D4)) },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onVoiceClick) {
                        Text("🎙️", fontSize = 16.sp)
                    }
                    IconButton(onClick = onCameraClick) {
                        Icon(Icons.Default.Build, contentDescription = "Visual Search", tint = Color(0xFF34D399), modifier = Modifier.size(18.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    onSearchFocusChange(focusState.isFocused)
                }
                .testTag("customer_search_input_dialog"),
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF1E293B),
                focusedBorderColor = Color(0xFF06B6D4),
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        if (isSearchFocused) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Recent Queries
                    if (recentQueries.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                                Text(
                                    text = "Recent Searches",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Clear All",
                                color = Color(0xFFEF4444),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable { onClearAllQueries() }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            recentQueries.take(8).forEach { q ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F172A))
                                        .clickable {
                                            onQuerySelect(q.query)
                                            focusManager.clearFocus()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = q.query, fontSize = 10.sp, color = Color(0xFFF1F5F9))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete search query",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clickable { onDeleteQuery(q.query) }
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No recent searches. Try searching for organic goods!",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Divider(color = Color(0xFF334155), thickness = 0.5.dp)

                    // Trending Categories
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(12.dp))
                            Text(
                                text = "Trending Categories",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val trendingCats = listOf(
                            "Organic Foods" to "🥬",
                            "Dairy" to "🥛",
                            "Snacks" to "🍿",
                            "Health Drinks" to "🥤",
                            "Electronics" to "📱",
                            "Perfumes & Fragrances" to "🌸"
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            trendingCats.forEach { (cat, emoji) ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F172A))
                                        .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            onQuerySelect(cat)
                                            focusManager.clearFocus()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = emoji, fontSize = 10.sp)
                                    Text(text = cat, fontSize = 10.sp, color = Color(0xFFF1F5F9), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedCarousel(
    products: List<ProductEntity>,
    onProductClick: (ProductEntity) -> Unit = {},
    onAddToCartClick: (ProductEntity) -> Unit = {}
) {
    val featuredProducts = remember(products) {
        products.filter { it.salesHistory > 200 }.take(4).ifEmpty { products.take(4) }
    }

    if (featuredProducts.isEmpty()) return

    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(featuredProducts) {
        while (true) {
            delay(4000)
            if (featuredProducts.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % featuredProducts.size
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                },
                label = "carousel_transition",
                modifier = Modifier.fillMaxSize()
            ) { slideIndex ->
                val product = featuredProducts[slideIndex]
                Box(modifier = Modifier.fillMaxSize().clickable { onProductClick(product) }) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .fillMaxWidth(0.65f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF5722), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚡ DEAL OF THE DAY",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = product.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = product.description.ifEmpty { "Exclusive daily promotional deal" },
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "$${product.retailPrice}",
                                color = Color(0xFF34D399),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "$${(product.retailPrice * 1.4).toInt()}",
                                color = Color(0xFF64748B),
                                fontSize = 10.sp,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }

                        Button(
                            onClick = { onAddToCartClick(product) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(
                                text = "Shop Now",
                                color = Color(0xFF0F172A),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                featuredProducts.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (idx == currentIndex) 6.dp else 4.dp)
                            .clip(CircleShape)
                            .background(if (idx == currentIndex) Color(0xFFFF5722) else Color.White.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    modifier: Modifier = Modifier,
    tag: String = "",
    tagColor: Color = Color(0xFFFF5722),
    isSaved: Boolean = false,
    isCompared: Boolean = false,
    onCompareClick: ((ProductEntity) -> Unit)? = null,
    onProductClick: (ProductEntity) -> Unit = {},
    onAddToCartClick: (ProductEntity) -> Unit = {},
    onWishlistClick: (ProductEntity) -> Unit = {},
    onImageClick: (String) -> Unit = {},
    onQuickViewClick: (ProductEntity) -> Unit = {},
    onStoreClick: ((String) -> Unit)? = null,
    onSetTargetPriceAlert: ((ProductEntity) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val cartButtonScale = remember { Animatable(1f) }
    val heartButtonScale = remember { Animatable(1f) }
    var quantityInCard by remember { mutableIntStateOf(1) }
    var showStoreDialog by remember { mutableStateOf(false) }

    // Calculate discount percentage if available
    val hasDiscount = product.wholesalePrice > 0 || product.retailPrice < product.wholesalePrice * 1.5
    val discountPercent = if (product.retailPrice < product.wholesalePrice * 1.5) {
        30
    } else {
        (((product.retailPrice * 1.35 - product.retailPrice) / (product.retailPrice * 1.35)) * 100).toInt().coerceIn(15, 45)
    }

    if (showStoreDialog) {
        AlertDialog(
            onDismissRequest = { showStoreDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🏪", fontSize = 18.sp)
                    Text(product.merchantName.ifEmpty { "المتجر المعتمد" }, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("📍 القسم / الممر: ${product.aisle}", fontSize = 12.sp, color = Color.LightGray)
                    Text("📦 المخزون المتاح: ${product.stockQuantity} وحدة", fontSize = 12.sp, color = Color.LightGray)
                    Text("✨ جودة المنتجات: عضوية ومفحوصة بالذكاء الاصطناعي", fontSize = 11.sp, color = Color(0xFF34D399))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showStoreDialog = false
                    onStoreClick?.invoke(product.merchantName)
                }) {
                    Text("زيارة المتجر ↗", color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStoreDialog = false }) {
                    Text("إغلاق", color = Color.Gray)
                }
            }
        )
    }

    Card(
        modifier = modifier
            .padding(4.dp)
            .clickable { onProductClick(product) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column {
            // Product Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onImageClick(product.imageUrl) },
                    contentScale = ContentScale.Crop
                )

                // Hot Tag if present
                if (tag.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .background(tagColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Compact Product Info Area (Price & Discount, and requested Action Icons row)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Product Name
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFFF1F5F9)
                )

                // 1. Price & Discount Percentage if available
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$${product.retailPrice}",
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        if (hasDiscount) {
                            Text(
                                text = "$${(product.retailPrice * 1.35).toInt()}",
                                color = Color(0xFF64748B),
                                fontSize = 10.sp,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }

                    // Discount percentage badge
                    if (hasDiscount) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF5722).copy(alpha = 0.9f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "-$discountPercent%",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // 2. Action Icons Row (Cart/Stepper, AI Compare, AI Info, Wishlist, Store Link)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // (1) Add to Cart & Stepper (+ / -)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF334155))
                                .clickable {
                                    if (quantityInCard > 1) quantityInCard--
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "$quantityInCard",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 1.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34D399))
                                .graphicsLayer {
                                    scaleX = cartButtonScale.value
                                    scaleY = cartButtonScale.value
                                }
                                .clickable {
                                    coroutineScope.launch {
                                        cartButtonScale.animateTo(0.75f, animationSpec = tween(50))
                                        cartButtonScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                                    }
                                    quantityInCard++
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onAddToCartClick(product)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add to Cart / Qty",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // (2) AI Benchmark / Compare Icon (المفاضلة بالذكاء الاصطناعي)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isCompared) Color(0xFFD946EF).copy(alpha = 0.35f) else Color(0xFF334155))
                            .border(1.dp, if (isCompared) Color(0xFFD946EF) else Color.Transparent, CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCompareClick?.invoke(product)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚖️",
                            fontSize = 10.sp
                        )
                    }

                    // (3) AI Product Info / Insights Icon (معلومات الصنف بالذكاء الاصطناعي)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF06B6D4).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f), CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onQuickViewClick(product)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✨",
                            fontSize = 10.sp
                        )
                    }

                    // (4) Wishlist / Favorite Icon (أضف للمفضلة)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSaved) Color(0xFFFF5722).copy(alpha = 0.2f) else Color(0xFF334155))
                            .border(1.dp, if (isSaved) Color(0xFFFF5722) else Color.Transparent, CircleShape)
                            .graphicsLayer {
                                scaleX = heartButtonScale.value
                                scaleY = heartButtonScale.value
                            }
                            .clickable {
                                coroutineScope.launch {
                                    heartButtonScale.animateTo(0.75f, animationSpec = tween(50))
                                    heartButtonScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onWishlistClick(product)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isSaved) Color(0xFFFF5722) else Color(0xFFCBD5E1),
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // (5) Navigate to Store / Merchant Icon (الانتقال لمتجر الصنف)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f), CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showStoreDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏪",
                            fontSize = 10.sp
                        )
                    }

                    // (6) Target Price Alert Icon (تنبيه هبوط السعر المستهدف)
                    if (onSetTargetPriceAlert != null) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSetTargetPriceAlert.invoke(product)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔔",
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkeletonItem(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )
    Card(
        modifier = modifier
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.Gray.copy(alpha = alpha))
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Box(modifier = Modifier.fillMaxWidth(0.8f).height(14.dp).background(Color.Gray.copy(alpha = alpha)))
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp).background(Color.Gray.copy(alpha = alpha)))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductGrid(
    products: List<ProductEntity>,
    isLoading: Boolean,
    aiRecommendations: List<com.example.ui.AIProductRecommendation> = emptyList(),
    isAiLoading: Boolean = false,
    savedProducts: List<ProductEntity> = emptyList(),
    comparedProducts: List<ProductEntity> = emptyList(),
    onCompareClick: ((ProductEntity) -> Unit)? = null,
    onGenerateAiRecommendations: () -> Unit = {},
    onProductClick: (ProductEntity) -> Unit = {},
    onAddToCartClick: (ProductEntity) -> Unit = {},
    onWishlistClick: (ProductEntity) -> Unit = {},
    onSetTargetPriceAlert: ((ProductEntity, Double) -> Unit)? = null,
    onTriggerHighThinkingComparison: ((List<ProductEntity>) -> Unit)? = null,
    highThinkingComparisonResult: String? = null,
    isComparingWithHighThinking: Boolean = false,
    onSimulatePriceDrop: ((Int, Double) -> Unit)? = null,
    onClearComparison: (() -> Unit)? = null
) {
    var selectedCategoryTab by remember { mutableStateOf(0) } // 0: Deals & Offers, 1: AI Interests, 2: AI For You
    var activeLightboxImageUrl by remember { mutableStateOf<String?>(null) }
    var quickViewProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var selectedSortIntent by remember { mutableStateOf("Default") }
    var gridSortOrder by remember { mutableStateOf("Latest") }
    var availabilityFilter by remember { mutableStateOf("All") } // "All", "In Stock", "Low Stock"
    var discountFilter by remember { mutableStateOf("All") } // "All", "Any Discount", ">20% Off", ">35% Off"
    var targetPriceAlertProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var showComparisonModal by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCategoryTab) {
        if (selectedCategoryTab == 2 && aiRecommendations.isEmpty() && !isAiLoading) {
            onGenerateAiRecommendations()
        }
    }

    if (activeLightboxImageUrl != null) {
        LightboxDialog(imageUrl = activeLightboxImageUrl!!) {
            activeLightboxImageUrl = null
        }
    }

    if (targetPriceAlertProduct != null) {
        TargetPriceAlertDialog(
            product = targetPriceAlertProduct!!,
            onDismiss = { targetPriceAlertProduct = null },
            onSetTargetPrice = { product, targetPrice ->
                onSetTargetPriceAlert?.invoke(product, targetPrice)
                targetPriceAlertProduct = null
            },
            onSimulateDrop = { product, droppedPrice ->
                onSimulatePriceDrop?.invoke(product.id, droppedPrice)
            }
        )
    }

    if (showComparisonModal && comparedProducts.isNotEmpty()) {
        ProductComparisonDialog(
            comparedProducts = comparedProducts,
            onRemoveProduct = { onCompareClick?.invoke(it) },
            onAddToCartClick = onAddToCartClick,
            onDismiss = { showComparisonModal = false },
            onTriggerHighThinking = onTriggerHighThinkingComparison,
            highThinkingResult = highThinkingComparisonResult,
            isThinking = isComparingWithHighThinking,
            onSetTargetPrice = { prod -> targetPriceAlertProduct = prod }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(vertical = 8.dp)
    ) {
        // High engagement filter tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { selectedCategoryTab = 0 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedCategoryTab == 0) Color(0xFFFF5722) else Color(0xFF334155),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("Deals & Offers", fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1)
                }
            }

            Button(
                onClick = { selectedCategoryTab = 1 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedCategoryTab == 1) Color(0xFF06B6D4) else Color(0xFF334155),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("Your Interests", fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1)
                }
            }

            Button(
                onClick = { selectedCategoryTab = 2 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedCategoryTab == 2) Color(0xFFD946EF) else Color(0xFF334155),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("AI For You 🤖", fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Dynamic Sorting and Active Filter Header
        var isSortDropdownExpanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "⚡", fontSize = 14.sp)
                Text(
                    text = "Sort & Dynamic Filter:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        .clickable { isSortDropdownExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = when (gridSortOrder) {
                                "Price: Low to High" -> "Price: Low to High 📈"
                                "Price: High to Low" -> "Price: High to Low 📉"
                                "Highest Discount %" -> "Discount % 🔥"
                                "Best Sellers" -> "Best Sellers 🏆"
                                else -> "Latest 🆕"
                            },
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expand Sort",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = isSortDropdownExpanded,
                    onDismissRequest = { isSortDropdownExpanded = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    listOf("Latest", "Price: Low to High", "Price: High to Low", "Highest Discount %", "Best Sellers").forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = when (option) {
                                        "Price: Low to High" -> "Price: Low to High 📈"
                                        "Price: High to Low" -> "Price: High to Low 📉"
                                        "Highest Discount %" -> "Highest Discount % 🔥"
                                        "Best Sellers" -> "Best Sellers 🏆"
                                        else -> "Latest 🆕"
                                    },
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            onClick = {
                                gridSortOrder = option
                                isSortDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Dynamic Filter Chips Row (Availability & Discount)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row 1: Availability Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📦 Availability:", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                listOf("All" to "All Items", "In Stock" to "✅ In Stock", "Low Stock" to "⏳ Low Stock (<15)").forEach { (key, label) ->
                    val isSelected = availabilityFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF10B981) else Color(0xFF1E293B))
                            .border(1.dp, if (isSelected) Color.Transparent else Color(0xFF334155), RoundedCornerShape(8.dp))
                            .clickable { availabilityFilter = key }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color(0xFF0F172A) else Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Row 2: Discount Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🏷️ Discount:", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                listOf("All" to "All", "Any Discount" to "⚡ Any Discount", ">20% Off" to "🔥 >20% Off", ">35% Off" to "💥 >35% Off").forEach { (key, label) ->
                    val isSelected = discountFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFFFF5722) else Color(0xFF1E293B))
                            .border(1.dp, if (isSelected) Color.Transparent else Color(0xFF334155), RoundedCornerShape(8.dp))
                            .clickable { discountFilter = key }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // AI Intent Sorting Bar (Choice Chips)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "AI Intent Sort",
                tint = Color(0xFF06B6D4),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "AI Intent:",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                val intents = listOf(
                    "Default" to "🎯 Default",
                    "Best Value" to "💎 Best Value",
                    "Fastest Shipping" to "⚡ Fast Delivery",
                    "Most Sustainable" to "🌱 Eco-Choice",
                    "Best Sellers" to "🔥 Best Seller"
                )
                intents.forEach { (key, label) ->
                    val isSelected = selectedSortIntent == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF06B6D4) else Color(0xFF1E293B))
                            .border(1.dp, if (isSelected) Color.Transparent else Color(0xFF334155), RoundedCornerShape(12.dp))
                            .clickable { selectedSortIntent = key }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color(0xFF0F172A) else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Floating Comparison Action Bar if products are selected
        if (comparedProducts.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD946EF).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("⚖️", fontSize = 14.sp)
                        Text(
                            text = "${comparedProducts.size} Items in Compare",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                            onClick = { onClearComparison?.invoke() },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Clear", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        }
                        Button(
                            onClick = { showComparisonModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD946EF)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("View Comparison ⚖️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        AnimatedContent(
            targetState = selectedCategoryTab,
            transitionSpec = {
                slideInHorizontally(animationSpec = tween(300)) { width -> if (targetState > initialState) width else -width } + fadeIn(animationSpec = tween(300)) togetherWith
                slideOutHorizontally(animationSpec = tween(300)) { width -> if (targetState > initialState) -width else width } + fadeOut(animationSpec = tween(300))
            },
            label = "category_transition"
        ) { targetTab ->
            when (targetTab) {
                2 -> {
                    if (isAiLoading) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    SkeletonItem(modifier = Modifier.weight(1f))
                                    SkeletonItem(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else if (aiRecommendations.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD946EF).copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Build Your AI Profile",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Interact with products, use the search bar, or make purchases, and our neural engine will tailor a highly specialized selection for you!",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Button(
                                    onClick = onGenerateAiRecommendations,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD946EF)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Load Recommendations", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    } else {
                        val aiAsProducts = remember(aiRecommendations, selectedSortIntent, gridSortOrder, availabilityFilter, discountFilter) {
                            var baseList = aiRecommendations.map { rec ->
                                ProductEntity(
                                    id = rec.id,
                                    name = rec.name,
                                    category = rec.category,
                                    retailPrice = rec.price,
                                    wholesalePrice = rec.price * 0.8,
                                    imageUrl = rec.imageUrl,
                                    description = rec.reason,
                                    isRegisteredMerchant = true,
                                    merchantName = "AI Recommended"
                                )
                            }
                            
                            // Availability filtering
                            baseList = when (availabilityFilter) {
                                "In Stock" -> baseList.filter { it.stockQuantity > 0 }
                                "Low Stock" -> baseList.filter { it.stockQuantity in 1..15 }
                                else -> baseList
                            }

                            // Discount filtering
                            baseList = when (discountFilter) {
                                "Any Discount" -> baseList.filter { getProductDiscountPercent(it) > 0 }
                                ">20% Off" -> baseList.filter { getProductDiscountPercent(it) >= 20 }
                                ">35% Off" -> baseList.filter { getProductDiscountPercent(it) >= 35 }
                                else -> baseList
                            }

                            val sortedByIntent = when (selectedSortIntent) {
                                "Best Value" -> baseList.sortedByDescending { getBestValueScore(it) }
                                "Fastest Shipping" -> baseList.sortedBy { getShippingTimeHours(it) }
                                "Most Sustainable" -> baseList.sortedByDescending { getSustainabilityScore(it) }
                                "Best Sellers" -> baseList.sortedByDescending { it.salesHistory }
                                else -> baseList
                            }

                            when (gridSortOrder) {
                                "Price: Low to High" -> sortedByIntent.sortedBy { it.retailPrice }
                                "Price: High to Low" -> sortedByIntent.sortedByDescending { it.retailPrice }
                                "Highest Discount %" -> sortedByIntent.sortedByDescending { getProductDiscountPercent(it) }
                                "Best Sellers" -> sortedByIntent.sortedByDescending { it.salesHistory }
                                "Latest" -> sortedByIntent.sortedByDescending { it.id }
                                else -> sortedByIntent
                            }
                        }

                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("🤖 Neural Recs", color = Color(0xFFD946EF), fontWeight = FontWeight.Black, fontSize = 12.sp)
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFD946EF).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("${aiAsProducts.size} Items", color = Color(0xFFD946EF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    "Refresh Recs",
                                    color = Color(0xFF06B6D4),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .clickable { onGenerateAiRecommendations() }
                                        .padding(4.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                aiAsProducts.chunked(2).forEach { rowPair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        rowPair.forEach { product ->
                                            val (smartTag, smartColor) = getSmartTagForProduct(product)
                                            ProductCard(
                                                product = product,
                                                modifier = Modifier.weight(1f),
                                                tag = smartTag,
                                                tagColor = smartColor,
                                                isSaved = savedProducts.any { it.id == product.id },
                                                isCompared = comparedProducts.any { it.id == product.id },
                                                onCompareClick = onCompareClick,
                                                onProductClick = onProductClick,
                                                onAddToCartClick = onAddToCartClick,
                                                onWishlistClick = onWishlistClick,
                                                onImageClick = { activeLightboxImageUrl = it },
                                                onQuickViewClick = { quickViewProduct = it },
                                                onSetTargetPriceAlert = { targetPriceAlertProduct = it }
                                            )
                                        }
                                        if (rowPair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f).padding(4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    if (isLoading) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(3) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    SkeletonItem(modifier = Modifier.weight(1f))
                                    SkeletonItem(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        // Filter products based on selected tab and apply AI-driven sorting & dynamic filters
                        val filteredProducts = remember(products, targetTab, selectedSortIntent, gridSortOrder, availabilityFilter, discountFilter) {
                            var baseList = if (targetTab == 0) {
                                products.sortedBy { it.retailPrice }
                            } else {
                                products.sortedByDescending { it.salesHistory }
                            }

                            // Dynamic Availability Filter
                            baseList = when (availabilityFilter) {
                                "In Stock" -> baseList.filter { it.stockQuantity > 0 }
                                "Low Stock" -> baseList.filter { it.stockQuantity in 1..15 }
                                else -> baseList
                            }

                            // Dynamic Discount Filter
                            baseList = when (discountFilter) {
                                "Any Discount" -> baseList.filter { getProductDiscountPercent(it) > 0 }
                                ">20% Off" -> baseList.filter { getProductDiscountPercent(it) >= 20 }
                                ">35% Off" -> baseList.filter { getProductDiscountPercent(it) >= 35 }
                                else -> baseList
                            }
                            
                            val sortedByIntent = when (selectedSortIntent) {
                                "Best Value" -> baseList.sortedByDescending { getBestValueScore(it) }
                                "Fastest Shipping" -> baseList.sortedBy { getShippingTimeHours(it) }
                                "Most Sustainable" -> baseList.sortedByDescending { getSustainabilityScore(it) }
                                "Best Sellers" -> baseList.sortedByDescending { it.salesHistory }
                                else -> baseList
                            }

                            when (gridSortOrder) {
                                "Price: Low to High" -> sortedByIntent.sortedBy { it.retailPrice }
                                "Price: High to Low" -> sortedByIntent.sortedByDescending { it.retailPrice }
                                "Highest Discount %" -> sortedByIntent.sortedByDescending { getProductDiscountPercent(it) }
                                "Best Sellers" -> sortedByIntent.sortedByDescending { it.salesHistory }
                                "Latest" -> sortedByIntent.sortedByDescending { it.id }
                                else -> sortedByIntent
                            }
                        }

                        if (filteredProducts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No products match this selection.", color = Color.White)
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                filteredProducts.chunked(2).forEach { rowPair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        rowPair.forEach { product ->
                                            val (smartTag, smartColor) = getSmartTagForProduct(product)
                                            ProductCard(
                                                product = product,
                                                modifier = Modifier.weight(1f),
                                                tag = smartTag,
                                                tagColor = smartColor,
                                                isSaved = savedProducts.any { it.id == product.id },
                                                isCompared = comparedProducts.any { it.id == product.id },
                                                onCompareClick = onCompareClick,
                                                onProductClick = onProductClick,
                                                onAddToCartClick = onAddToCartClick,
                                                onWishlistClick = onWishlistClick,
                                                onImageClick = { activeLightboxImageUrl = it },
                                                onQuickViewClick = { quickViewProduct = it },
                                                onSetTargetPriceAlert = { targetPriceAlertProduct = it }
                                            )
                                        }
                                        if (rowPair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f).padding(4.dp))
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

    if (quickViewProduct != null) {
        QuickViewDialog(
            product = quickViewProduct!!,
            onDismiss = { quickViewProduct = null },
            onAddToCart = onAddToCartClick
        )
    }
}

fun getProductDiscountPercent(product: ProductEntity): Int {
    return if (product.wholesalePrice > 0 && product.retailPrice > product.wholesalePrice) {
        (((product.retailPrice - product.wholesalePrice) / product.retailPrice) * 100).toInt().coerceIn(5, 50)
    } else if (product.bestPrice > 0 && product.retailPrice > product.bestPrice) {
        (((product.retailPrice - product.bestPrice) / product.retailPrice) * 100).toInt().coerceIn(5, 50)
    } else {
        0
    }
}

// Full screen Lightbox/Zoom visual overlay dialog
@Composable
fun LightboxDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.95f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Zoomed Product View",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun getSustainabilityScore(product: ProductEntity): Int {
    var score = 0
    val desc = product.description.lowercase()
    val name = product.name.lowercase()
    val category = product.category.lowercase()
    val ingredients = product.ingredients.lowercase()
    
    if (desc.contains("organic") || ingredients.contains("organic")) score += 30
    if (desc.contains("sustainable") || name.contains("sustainable")) score += 30
    if (desc.contains("eco") || desc.contains("green") || name.contains("eco")) score += 20
    if (category.contains("organic") || category.contains("green")) score += 20
    if (desc.contains("natural") || ingredients.contains("natural")) score += 15
    if (desc.contains("vegan") || desc.contains("plant-based")) score += 15
    
    return if (score == 0) (product.id % 5) * 5 else score
}

fun getShippingTimeHours(product: ProductEntity): Int {
    return if (product.isRegisteredMerchant) {
        when (product.aisle.lowercase()) {
            "fresh", "organic", "bakery" -> 1
            "dairy", "beverages" -> 2
            else -> 4
        }
    } else {
        (product.id % 5) * 12 + 12
    }
}

fun getBestValueScore(product: ProductEntity): Double {
    val standardPrice = product.retailPrice * 1.4
    val savings = standardPrice - product.retailPrice
    val savingsPercentage = (savings / standardPrice) * 100.0
    return savingsPercentage + (if (product.wholesalePrice > 0) (product.retailPrice - product.wholesalePrice) / product.retailPrice * 10 else 0.0)
}

fun getSmartTagForProduct(product: ProductEntity): Pair<String, Color> {
    val descLower = product.description.lowercase()
    val categoryLower = product.category.lowercase()
    
    if (descLower.contains("organic") || descLower.contains("eco") || descLower.contains("natural") || descLower.contains("sustainable") || categoryLower.contains("organic")) {
        return Pair("🌱 Sustainable Choice", Color(0xFF10B981)) // Emerald Green
    }
    
    if (product.salesHistory >= 15) {
        return Pair("🔥 Best Seller", Color(0xFFF59E0B)) // Amber
    }
    
    if (product.id % 3 == 0) {
        return Pair("⭐️ Top Rated", Color(0xFF3B82F6)) // Royal Blue
    }
    
    if (product.wholesalePrice > 0 && product.retailPrice <= product.wholesalePrice * 1.25) {
        return Pair("💎 Best Value", Color(0xFF06B6D4)) // Cyan
    }
    
    return Pair("✨ AI Choice", Color(0xFFD946EF)) // Fuchsia
}

@Composable
fun QuickViewDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onAddToCart: (ProductEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onAddToCart(product)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                    Text("Add to Cart ($${product.retailPrice})", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFF0F172A),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔍 Quick View",
                    color = Color(0xFF06B6D4),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFF34D399).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = product.category,
                        color = Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = product.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "by ${product.merchantName}",
                            color = Color(0xFF06B6D4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$${product.retailPrice} USD",
                            color = Color(0xFF34D399),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Divider(color = Color(0xFF334155), thickness = 1.dp)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Description",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = product.description.ifEmpty { "This premium grade product is carefully sourced and verified by our AI marketplace engines to ensure top-tier quality and perfect customer satisfaction." },
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "⚙️ Specifications",
                        color = Color(0xFF06B6D4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val purityScore = remember(product.id) { 95 + (product.id % 5) + 0.4 }
                    val weight = remember(product.id) {
                        when (product.id % 3) {
                            0 -> "500g Pack"
                            1 -> "1.0 Liters"
                            else -> "1 Unit Standard"
                        }
                    }
                    val shippingSpeed = remember(product.id) {
                        if (product.isRegisteredMerchant) "Same Day Delivery ⚡" else "1-2 Days standard delivery"
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SpecRow(label = "Fulfillment Aisle", value = product.aisle)
                        SpecRow(label = "Package Weight/Size", value = weight)
                        SpecRow(label = "Neural Purity Index", value = "$purityScore% Verified")
                        SpecRow(label = "Delivery Lead Time", value = shippingSpeed)
                        if (product.ingredients.isNotEmpty()) {
                            SpecRow(label = "Key Ingredients", value = product.ingredients)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "💬 Customer Reviews & Sentiment",
                        color = Color(0xFFD946EF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val reviews = remember(product.id) {
                        listOf(
                            Triple("Amina Al-K.", "⭐️⭐️⭐️⭐️⭐️", "Absolutely outstanding! The sustainability level is top tier, and it was delivered within hours."),
                            Triple("James L.", "⭐️⭐️⭐️⭐️☆", "Great value. Perfectly matches the AI description. Will definitely buy again!")
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        reviews.forEach { (author, rating, text) ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = author, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(text = rating, color = Color(0xFFF59E0B), fontSize = 10.sp)
                                }
                                Text(text = text, color = Color(0xFF94A3B8), fontSize = 9.sp, lineHeight = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 10.sp)
        Text(text = value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TrendingTopicsComponent(
    products: List<ProductEntity>,
    onCategorySelected: (String) -> Unit,
    currentSelectedCategory: String = ""
) {
    // Extract categories dynamically from the product list to show actual counts
    val categoryCounts = remember(products) {
        products.groupBy { it.category }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(6)
    }

    // Map categories to modern emojis
    val categoryEmojis = mapOf(
        "Electronics" to "💻",
        "Kitchen / Living" to "🍳",
        "Wellness / Kitchen" to "🍵",
        "Home & Garden" to "🏡",
        "Organic Foods" to "🌱",
        "Spiced Coffee & Tea" to "☕",
        "Dairy" to "🥛",
        "Health Drinks" to "🥤",
        "Snacks" to "🍿",
        "Men's Clothing" to "👔",
        "Women's Clothing" to "👗"
    )

    if (categoryCounts.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text("🔥", fontSize = 14.sp)
                Text(
                    text = "Trending Topics & Categories",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• Recent activity spikes",
                    color = Color(0xFF34D399),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Horizontally scrollable row of dynamic capsules
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categoryCounts.forEach { (cat, count) ->
                    val emoji = categoryEmojis[cat] ?: "📦"
                    val isSelected = currentSelectedCategory.equals(cat, ignoreCase = true)
                    
                    // Generate a deterministic trend spike based on the category name
                    val trendPercentage = remember(cat) { (cat.hashCode() % 61 + 25).coerceIn(15, 95) }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Color(0xFFFF5722)
                                else Color(0xFF1E293B)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFFFF5722)
                                else Color(0xFF334155),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (isSelected) onCategorySelected("") else onCategorySelected(cat)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = emoji, fontSize = 12.sp)
                            Column {
                                Text(
                                    text = cat,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "+$trendPercentage% spike (${count} items)",
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF34D399),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class SpecRowItem(
    val title: String,
    val values: List<String>,
    val color: Color = Color.White
)

@Composable
fun ProductComparisonDialog(
    comparedProducts: List<ProductEntity>,
    onRemoveProduct: (ProductEntity) -> Unit,
    onAddToCartClick: (ProductEntity) -> Unit,
    onDismiss: () -> Unit,
    onTriggerHighThinking: ((List<ProductEntity>) -> Unit)? = null,
    highThinkingResult: String? = null,
    isThinking: Boolean = false,
    onSetTargetPrice: ((ProductEntity) -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚖️ Side-by-Side Comparison", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFD946EF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("${comparedProducts.size} Selected", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            if (comparedProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text("No products selected. Check ⚖️ on product cards to compare.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            } else {
                val minPrice = comparedProducts.minOfOrNull { it.retailPrice } ?: 0.0

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Swipe horizontally below to compare specifications and highlighted differences.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )

                    // Gemini 3.1 Pro Thinking Mode Comparison Action Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("🧠", fontSize = 14.sp)
                                    Text(
                                        "Gemini 3.1 Pro Deep Thinking",
                                        color = Color(0xFFC7D2FE),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF6366F1).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Thinking Level: High", color = Color(0xFFA5B4FC), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (isThinking) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF312E81), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFA5B4FC),
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        "Thinking & analyzing specifications, price delta, and best value tradeoffs...",
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            } else if (highThinkingResult != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("✨", fontSize = 12.sp)
                                        Text("AI Verdict & Comprehensive Breakdown:", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                    Text(
                                        text = highThinkingResult,
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onTriggerHighThinking?.invoke(comparedProducts) },
                                enabled = !isThinking,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (isThinking) "Evaluating Tradeoffs..." else "Generate AI Deep Comparison 🧠",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Horizontally scrollable comparison table
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                            // Header row: Image, Name, Lowest Price Badge, and Action buttons
                            Row(
                                modifier = Modifier
                                    .background(Color(0xFF0F172A))
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Label Column
                                Box(modifier = Modifier.width(110.dp).padding(8.dp)) {
                                    Text("Features", fontWeight = FontWeight.Bold, color = Color(0xFF64748B), fontSize = 11.sp)
                                }
                                
                                comparedProducts.forEach { product ->
                                    val isLowestPrice = product.retailPrice == minPrice
                                    Card(
                                        modifier = Modifier
                                            .width(160.dp)
                                            .padding(horizontal = 4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isLowestPrice) Color(0xFF1E293B).copy(alpha = 0.9f) else Color(0xFF1E293B)
                                        ),
                                        border = if (isLowestPrice) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981)) else null,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            Column(modifier = Modifier.padding(6.dp)) {
                                                AsyncImage(
                                                    model = product.imageUrl,
                                                    contentDescription = product.name,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(70.dp)
                                                        .clip(RoundedCornerShape(6.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                if (isLowestPrice) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFF10B981), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("🏆 Lowest Price", color = Color(0xFF0F172A), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                }
                                                Text(
                                                    text = product.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = Color.White
                                                )
                                            }
                                            IconButton(
                                                onClick = { onRemoveProduct(product) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(20.dp)
                                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Remove",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Specifications rows with highlighted differences
                            val specRows = listOf(
                                SpecRowItem("Price", comparedProducts.map { prod ->
                                    val isCheapest = prod.retailPrice == minPrice
                                    val delta = prod.retailPrice - minPrice
                                    if (isCheapest) {
                                        "$${prod.retailPrice} 🏆"
                                    } else {
                                        val deltaStr = String.format("%.2f", delta)
                                        "$${prod.retailPrice} (+$deltaStr)"
                                    }
                                }, Color(0xFF34D399)),
                                SpecRowItem("Discount %", comparedProducts.map { prod ->
                                    val disc = getProductDiscountPercent(prod)
                                    if (disc > 0) "$disc% OFF 🔥" else "Standard Price"
                                }, Color(0xFFFF5722)),
                                SpecRowItem("Category", comparedProducts.map { it.category }),
                                SpecRowItem("Aisle / Tag", comparedProducts.map { it.aisle.ifEmpty { "General" } }),
                                SpecRowItem("Stock Left", comparedProducts.map { prod ->
                                    if (prod.stockQuantity <= 0) "Out of stock ❌"
                                    else if (prod.stockQuantity <= 15) "${prod.stockQuantity} (Low Stock ⏳)"
                                    else "${prod.stockQuantity} in stock ✅"
                                }, Color(0xFFF59E0B)),
                                SpecRowItem("Sales History", comparedProducts.map { "${it.salesHistory} orders" }),
                                SpecRowItem("Merchant", comparedProducts.map { it.merchantName }),
                                SpecRowItem("Shipping Time", comparedProducts.map { "${getShippingTimeHours(it)} hrs ⚡" }),
                                SpecRowItem("Best Value Score", comparedProducts.map {
                                    val score = getBestValueScore(it)
                                    String.format("%.1f pts", score)
                                }, Color(0xFF06B6D4)),
                                SpecRowItem("Description", comparedProducts.map { it.description.ifEmpty { "AI Store Choice" } })
                            )

                            specRows.forEach { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(0.5.dp, Color(0xFF334155))
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Spec Label
                                    Box(
                                        modifier = Modifier
                                            .width(110.dp)
                                            .padding(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = row.title,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp
                                        )
                                    }

                                    // Spec values for each product
                                    row.values.forEach { valStr ->
                                        Box(
                                            modifier = Modifier
                                                .width(160.dp)
                                                .padding(horizontal = 8.dp)
                                        ) {
                                            Text(
                                                text = valStr,
                                                color = row.color,
                                                fontSize = 10.sp,
                                                maxLines = 4,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Bottom Actions Row (Add to Cart & Set Alert for each)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.width(110.dp))
                                
                                comparedProducts.forEach { product ->
                                    Column(
                                        modifier = Modifier
                                            .width(160.dp)
                                            .padding(horizontal = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Button(
                                            onClick = { onAddToCartClick(product) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().height(28.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(10.dp))
                                                Text("Add to Cart", color = Color(0xFF0F172A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        if (onSetTargetPrice != null) {
                                            OutlinedButton(
                                                onClick = { onSetTargetPrice(product) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth().height(28.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text("🔔", fontSize = 8.sp)
                                                    Text("Set Price Alert", fontSize = 8.sp, fontWeight = FontWeight.Bold)
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close Comparison", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun TargetPriceAlertDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onSetTargetPrice: (ProductEntity, Double) -> Unit,
    onSimulateDrop: (ProductEntity, Double) -> Unit
) {
    val initialTarget = remember(product) { String.format("%.2f", product.retailPrice * 0.85) }
    var targetPriceInput by remember { mutableStateOf(initialTarget) }
    var isSubscribed by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🔔 Price Drop Alert", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(product.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        Text("Current Price: $${product.retailPrice}", color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Text(
                    "Set your desired target threshold. When price drops via Firebase Messaging, you'll be notified automatically:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )

                // Quick target discounts (10%, 20%, 30%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(10, 20, 30).forEach { percent ->
                        val calculatedPrice = product.retailPrice * (1.0 - percent / 100.0)
                        val formattedPrice = String.format("%.1f", calculatedPrice)
                        Button(
                            onClick = { targetPriceInput = String.format("%.2f", calculatedPrice) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("-$percent% ($$formattedPrice)", fontSize = 9.sp, color = Color.White)
                        }
                    }
                }

                OutlinedTextField(
                    value = targetPriceInput,
                    onValueChange = { targetPriceInput = it },
                    label = { Text("Target Price ($)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF59E0B),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFFF59E0B),
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isSubscribed) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("✅ Subscribed to FCM topic: price_drop_${product.id}", color = Color(0xFF6EE7B7), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("You will receive instant alerts when this item drops below $$targetPriceInput", color = Color.White, fontSize = 9.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = {
                        val parsed = targetPriceInput.toDoubleOrNull() ?: (product.retailPrice * 0.85)
                        onSetTargetPrice(product, parsed)
                        isSubscribed = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Alert 🔔", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }

                if (isSubscribed) {
                    TextButton(
                        onClick = {
                            val parsed = targetPriceInput.toDoubleOrNull() ?: (product.retailPrice * 0.85)
                            onSimulateDrop(product, parsed - 1.0)
                        }
                    ) {
                        Text("🚀 Test Immediate Price Drop Alert", color = Color(0xFF38BDF8), fontSize = 10.sp)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(20.dp)
    )
}
