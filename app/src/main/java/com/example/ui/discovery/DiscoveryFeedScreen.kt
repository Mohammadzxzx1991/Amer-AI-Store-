package com.example.ui.discovery

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ProductEntity
import com.example.data.ViralProductMentionEntity
import com.example.data.discovery.SocialViralDiscoveryService
import com.example.ui.MarketViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryFeedScreen(
    marketViewModel: MarketViewModel,
    onBack: () -> Unit,
    onProductClick: (ProductEntity) -> Unit = {},
    onAddToCart: (ProductEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lang by marketViewModel.appLanguage.collectAsState()
    val isDark by marketViewModel.appDarkMode.collectAsState()
    val allCatalogProducts by marketViewModel.products.collectAsState()

    // Discovery Service instance
    val discoveryService = remember { SocialViralDiscoveryService.getInstance() }

    // Screen State
    var selectedPlatform by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Velocity") } // Velocity, Views, Likes, Price
    var isRefreshing by remember { mutableStateOf(false) }
    var viralMentionsList by remember { mutableStateOf<List<ViralProductMentionEntity>>(emptyList()) }
    var userLikedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Fetch Initial Data
    LaunchedEffect(Unit) {
        isRefreshing = true
        val result = discoveryService.fetchCurrentViralMentions()
        viralMentionsList = result.getOrDefault(emptyList())
        isRefreshing = false
    }

    // Filter & Sort Logic
    val filteredMentions = remember(viralMentionsList, selectedPlatform, selectedCategory, sortBy) {
        var list = viralMentionsList

        if (selectedPlatform != "All") {
            list = list.filter { it.platform.equals(selectedPlatform, ignoreCase = true) }
        }

        if (selectedCategory != "All") {
            list = list.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }

        when (sortBy) {
            "Velocity" -> list.sortedByDescending { it.viralVelocity }
            "Views" -> list.sortedByDescending { it.viewCount }
            "Likes" -> list.sortedByDescending { it.likeCount }
            "Price" -> list.sortedBy { it.retailPrice }
            else -> list
        }
    }

    // Metric Aggregations
    val totalViews = remember(viralMentionsList) { viralMentionsList.sumOf { it.viewCount } }
    val avgVelocity = remember(viralMentionsList) {
        if (viralMentionsList.isEmpty()) 0.0 else viralMentionsList.map { it.viralVelocity }.average()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (lang == "ar") "🔥 رادار الاستكشاف والتريندات" else "🔥 Viral Discovery Feed",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = PolarLight
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentCoral.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, AccentCoral)
                            ) {
                                Text(
                                    text = if (lang == "ar") "مباشر ⚡" else "LIVE ⚡",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentCoral,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (lang == "ar") "سوشيال ميديا: TikTok • Reels • Shorts • X" else "Live Social Media APIs Pulse",
                            fontSize = 10.sp,
                            color = SoftGrayText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("discovery_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PolarLight
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isRefreshing = true
                                val res = discoveryService.fetchCurrentViralMentions(forceRefresh = true)
                                viralMentionsList = res.getOrDefault(emptyList())
                                isRefreshing = false
                                statusMessage = if (lang == "ar") "تم تحديث تريندات التواصل الاجتماعي بنجاح! 🚀" else "Social trends refreshed! 🚀"
                            }
                        },
                        modifier = Modifier.testTag("discovery_refresh_button")
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryCyan
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = PrimaryCyan
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateDarkBg
                )
            )
        },
        containerColor = SlateDarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status Snackbar / Banner
            if (statusMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SecondaryMint.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, SecondaryMint.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SecondaryMint, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(statusMessage!!, color = PolarLight, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            IconButton(onClick = { statusMessage = null }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = SoftGrayText, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // Hero Live Stats Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDarkBg),
                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(AccentCoral.copy(alpha = 0.5f), PrimaryCyan.copy(alpha = 0.5f))))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌐", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (lang == "ar") "نبض المنتجات الأكثر انتشاراً" else "Viral Social Velocity Radar",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PolarLight
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryCyan.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, PrimaryCyan)
                            ) {
                                Text(
                                    text = "AAS-UP 19-Agent",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMiniBadge(
                                label = if (lang == "ar") "إجمالي المشاهدات" else "Total Views",
                                value = formatCompactNumber(totalViews),
                                icon = "👁️",
                                color = SecondaryMint
                            )
                            StatMiniBadge(
                                label = if (lang == "ar") "منتجات فيروسية" else "Viral Products",
                                value = "${viralMentionsList.size}",
                                icon = "🔥",
                                color = AccentCoral
                            )
                            StatMiniBadge(
                                label = if (lang == "ar") "معدل الانتشار" else "Velocity Score",
                                value = String.format(Locale.US, "%.1fx", avgVelocity),
                                icon = "⚡",
                                color = PrimaryCyan
                            )
                            StatMiniBadge(
                                label = if (lang == "ar") "معدل الرضا" else "Sentiment",
                                value = "94%",
                                icon = "⭐",
                                color = Color(0xFFF59E0B)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (lang == "ar")
                                "💡 يقوم وكيل الأفكار ووكيل التسويق برصد الفيديوهات والمنشورات الأكثر تفاعلاً لتحويل التريندات إلى عروض مخفضة فورية."
                            else
                                "💡 AAS-UP Ideas & Marketing agents continuously ingest trending social media mentions and map them to direct discounted offers.",
                            fontSize = 10.sp,
                            color = SoftGrayText,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Platform Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (lang == "ar") "تصفية حسب المنصة الاجتماعية:" else "Filter by Social Platform:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolarLight
                    )
                    val platforms = listOf(
                        Triple("All", if (lang == "ar") "الكل 🌐" else "All 🌐", PrimaryCyan),
                        Triple("TikTok", "TikTok 🎵", Color(0xFF00F2FE)),
                        Triple("Instagram", "Reels 📸", Color(0xFFE1306C)),
                        Triple("YouTube", "Shorts ▶️", Color(0xFFFF0000)),
                        Triple("X", "X / Twitter 𝕏", Color(0xFF1DA1F2))
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(platforms) { (id, title, color) ->
                            val isSel = selectedPlatform == id
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedPlatform = id },
                                label = {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color.copy(alpha = 0.25f),
                                    selectedLabelColor = PolarLight,
                                    containerColor = CardDarkBg,
                                    labelColor = SoftGrayText
                                ),
                                border = BorderStroke(
                                    width = if (isSel) 1.5.dp else 0.5.dp,
                                    color = if (isSel) color else SoftGrayText.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }

            // Category & Sort Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Chips (Horizontal Mini)
                    val categories = listOf("All", "Organic Foods", "Spiced Coffee & Tea", "Electronics", "Pharmacy & Medical")
                    val catLabelsAr = mapOf(
                        "All" to "الكل 🛍️",
                        "Organic Foods" to "أغذية عضوية 🍯",
                        "Spiced Coffee & Tea" to "قهوة ☕",
                        "Electronics" to "تقنية 📱",
                        "Pharmacy & Medical" to "عناية 💊"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(categories) { cat ->
                            val isSel = selectedCategory == cat
                            val label = if (lang == "ar") catLabelsAr[cat] ?: cat else cat
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) PrimaryCyan.copy(alpha = 0.2f) else CardDarkBg,
                                border = BorderStroke(0.5.dp, if (isSel) PrimaryCyan else Color.Transparent),
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) PrimaryCyan else SoftGrayText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Sort Selector
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CardDarkBg,
                        border = BorderStroke(0.5.dp, SoftGrayText.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable {
                            sortBy = when (sortBy) {
                                "Velocity" -> "Views"
                                "Views" -> "Likes"
                                "Likes" -> "Price"
                                else -> "Velocity"
                            }
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = null, tint = SecondaryMint, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = when (sortBy) {
                                    "Velocity" -> if (lang == "ar") "⚡ السرعة" else "⚡ Velocity"
                                    "Views" -> if (lang == "ar") "👁️ المشاهدات" else "👁️ Views"
                                    "Likes" -> if (lang == "ar") "❤️ الإعجابات" else "❤️ Likes"
                                    else -> if (lang == "ar") "💲 السعر" else "💲 Price"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryMint
                            )
                        }
                    }
                }
            }

            // Results Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == "ar") "نتائج الرصد الفيروسي (${filteredMentions.size}):" else "Discovered Viral Mentions (${filteredMentions.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolarLight
                    )
                    Text(
                        text = if (lang == "ar") "تحديث مستمر 24/7" else "Live 24/7 Indexing",
                        fontSize = 10.sp,
                        color = SoftGrayText
                    )
                }
            }

            // Viral Mention Cards
            if (filteredMentions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDarkBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔍", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (lang == "ar") "لا توجد نتائج تطابق الفلاتر المحددة" else "No viral mentions match filters",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolarLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (lang == "ar") "جرب اختيار منصة أخرى أو إعادة تعيين الفلاتر" else "Try clearing filters to see all viral trends",
                                fontSize = 11.sp,
                                color = SoftGrayText
                            )
                        }
                    }
                }
            } else {
                items(filteredMentions, key = { it.id }) { mention ->
                    ViralMentionCard(
                        mention = mention,
                        lang = lang,
                        isLiked = userLikedIds.contains(mention.id),
                        onLike = {
                            coroutineScope.launch {
                                discoveryService.likeViralMention(mention.id)
                                userLikedIds = userLikedIds + mention.id
                                statusMessage = if (lang == "ar") "تم تسجيل إعجابك بالتريند! ❤️" else "Liked viral trend! ❤️"
                            }
                        },
                        onShare = {
                            coroutineScope.launch {
                                discoveryService.recordShare(mention.id)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, mention.productNameAr)
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "🔥 تريند فيروسي على عامر ستور: ${mention.productNameAr}\nالسعر المخفض: $${mention.retailPrice}\n${mention.postCaption}"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Viral Trend"))
                            }
                        },
                        onOpenOriginalPost = {
                            try {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mention.postUrl))
                                context.startActivity(browserIntent)
                            } catch (e: Exception) {
                                statusMessage = if (lang == "ar") "رابط المنشور: ${mention.postUrl}" else "Post URL: ${mention.postUrl}"
                            }
                        },
                        onAddToCart = {
                            // Find matching product in catalog or generate temporary entity
                            val matchingProd = allCatalogProducts.find { it.id == mention.productId }
                                ?: ProductEntity(
                                    id = mention.productId,
                                    name = if (lang == "ar") mention.productNameAr else mention.productNameEn,
                                    category = mention.category,
                                    retailPrice = mention.retailPrice,
                                    wholesalePrice = mention.retailPrice * 0.8,
                                    imageUrl = mention.imageUrl,
                                    description = mention.postCaption,
                                    stockQuantity = 50,
                                    merchantName = "${mention.platform} Viral Trends"
                                )
                            onAddToCart(matchingProd)
                            statusMessage = if (lang == "ar") "تمت إضافة ${mention.productNameAr} إلى السلة! 🛒" else "Added ${mention.productNameEn} to cart! 🛒"
                        },
                        onViewDetails = {
                            val matchingProd = allCatalogProducts.find { it.id == mention.productId }
                                ?: ProductEntity(
                                    id = mention.productId,
                                    name = if (lang == "ar") mention.productNameAr else mention.productNameEn,
                                    category = mention.category,
                                    retailPrice = mention.retailPrice,
                                    wholesalePrice = mention.retailPrice * 0.8,
                                    imageUrl = mention.imageUrl,
                                    description = mention.postCaption,
                                    stockQuantity = 50,
                                    merchantName = "${mention.platform} Viral Trends"
                                )
                            onProductClick(matchingProd)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ViralMentionCard(
    mention: ViralProductMentionEntity,
    lang: String,
    isLiked: Boolean,
    onLike: () -> Unit,
    onShare: () -> Unit,
    onOpenOriginalPost: () -> Unit,
    onAddToCart: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val platformColor = when (mention.platform.lowercase()) {
        "tiktok" -> Color(0xFF00F2FE)
        "instagram" -> Color(0xFFE1306C)
        "youtube" -> Color(0xFFFF0000)
        "x" -> Color(0xFF1DA1F2)
        else -> PrimaryCyan
    }

    val platformIcon = when (mention.platform.lowercase()) {
        "tiktok" -> "🎵 TikTok"
        "instagram" -> "📸 Reels"
        "youtube" -> "▶️ Shorts"
        "x" -> "𝕏 Post"
        else -> "🌐 Social"
    }

    val discountPercent = if (mention.originalPrice > mention.retailPrice) {
        (((mention.originalPrice - mention.retailPrice) / mention.originalPrice) * 100).toInt()
    } else 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("viral_mention_card_${mention.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg),
        border = BorderStroke(1.dp, platformColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Creator Profile & Platform Badge
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
                            .border(1.dp, platformColor, CircleShape)
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100",
                            contentDescription = mention.creatorName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = mention.creatorName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolarLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = mention.creatorHandle,
                            fontSize = 10.sp,
                            color = SoftGrayText
                        )
                    }
                }

                // Platform & Velocity Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = platformColor.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, platformColor)
                    ) {
                        Text(
                            text = platformIcon,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = platformColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, Color(0xFFF59E0B))
                    ) {
                        Text(
                            text = "⚡ ${mention.viralVelocity}x",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Product & Post Media Presentation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlateDarkBg.copy(alpha = 0.6f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Thumbnail with Discount Badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(0.5.dp, PrimaryCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clickable { onViewDetails() }
                ) {
                    AsyncImage(
                        model = mention.imageUrl,
                        contentDescription = mention.productNameAr,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (discountPercent > 0) {
                        Surface(
                            shape = RoundedCornerShape(bottomEnd = 6.dp),
                            color = AccentCoral,
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "-$discountPercent%",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Product Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == "ar") mention.productNameAr else mention.productNameEn,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolarLight,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = mention.category,
                        fontSize = 9.sp,
                        color = PrimaryCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$${mention.retailPrice}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = SecondaryMint
                        )
                        if (mention.originalPrice > mention.retailPrice) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$${mention.originalPrice}",
                                fontSize = 11.sp,
                                textDecoration = TextDecoration.LineThrough,
                                color = SoftGrayText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Post Caption
            Text(
                text = mention.postCaption,
                fontSize = 11.sp,
                color = PolarLight,
                lineHeight = 16.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Hashtags Row
            if (mention.hashtags.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mention.hashtags.replace(",", " "),
                    fontSize = 10.sp,
                    color = platformColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI Agent Viral Reason Insight Box
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDarkBg),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, PrimaryCyan.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("💡", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (lang == "ar") "تحليل ذكاء الأعمال (AAS-UP Agent Insight):" else "AAS-UP Agent Insight:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCyan
                        )
                        Text(
                            text = mention.viralReasonAr,
                            fontSize = 10.sp,
                            color = PolarLight,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Social Metrics Bar (Views, Likes, Shares)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Views
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = SoftGrayText, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(formatCompactNumber(mention.viewCount), fontSize = 10.sp, color = SoftGrayText)
                }

                // Likes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLike() }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) AccentCoral else SoftGrayText,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = formatCompactNumber(mention.likeCount + if (isLiked) 1 else 0),
                        fontSize = 10.sp,
                        color = if (isLiked) AccentCoral else SoftGrayText,
                        fontWeight = if (isLiked) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Shares
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onShare() }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = SoftGrayText, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(formatCompactNumber(mention.shareCount), fontSize = 10.sp, color = SoftGrayText)
                }

                // Original Post Link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenOriginalPost() }
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = platformColor, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (lang == "ar") "شاهد المنشور" else "Watch Post",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = platformColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row (Add to Cart & Details)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PolarLight),
                    border = BorderStroke(0.8.dp, PrimaryCyan)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (lang == "ar") "تفاصيل السلعة" else "Details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryMint,
                        contentColor = SlateDarkBg
                    )
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (lang == "ar") "شراء السلعة 🛒" else "Add to Cart 🛒",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun StatMiniBadge(
    label: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SlateDarkBg,
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = color
                )
            }
            Text(
                text = label,
                fontSize = 8.sp,
                color = SoftGrayText,
                maxLines = 1
            )
        }
    }
}

fun formatCompactNumber(number: Long): String {
    return when {
        number >= 1_000_000 -> String.format(Locale.US, "%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format(Locale.US, "%.1fK", number / 1_000.0)
        else -> number.toString()
    }
}
