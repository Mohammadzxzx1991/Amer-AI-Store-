package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductEntity
import com.example.ui.theme.*
import com.example.ui.visionx.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreFeaturesSheet(
    onDismiss: () -> Unit,
    viewModel: MarketViewModel,
    lang: String,
    productsList: List<ProductEntity>,
    onOpenTikTok: () -> Unit,
    onOpenSnapchat: () -> Unit,
    onOpenMarketPulse: () -> Unit,
    onOpenArViewer: (ProductEntity) -> Unit
) {
    val isAr = lang == "ar"
    val cardBg = CardDarkBg
    val slateBg = SlateDarkBg

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = slateBg,
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PrimaryCyan, SecondaryMint))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = SlateDarkBg, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text(
                            text = if (isAr) "قائمة المزايا والخدمات الذكية" else "Smart Features & Services Hub",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = if (isAr) "استكشف العروض، وضع العائلة، والتوصيات في مكان واحد" else "Explore offers, family mode & AI tools in one clean hub",
                            fontSize = 11.sp,
                            color = SoftGrayText
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            HorizontalDivider(color = PrimaryCyan.copy(alpha = 0.2f))

            // Scrollable Hub Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Market Pulse & Trends
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenMarketPulse() }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(28.dp))
                                Column {
                                    Text(if (isAr) "لوحة نبض السوق (Market Pulse)" else "Market Pulse Dashboard", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                    Text(if (isAr) "تحليلات الأسعار واتجاهات الفئات لآخر 30 يوماً" else "30-day price trends & category analytics", fontSize = 10.sp, color = SoftGrayText)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PrimaryCyan)
                        }
                    }
                }

                // 2. TikTok Commerce Feed
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF22D3EE).copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTikTok() }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFF22D3EE), modifier = Modifier.size(28.dp))
                                Column {
                                    Text(if (isAr) "مقاطع تيك توك للتسوق" else "TikTok Commerce Feed", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                    Text(if (isAr) "شاهد المنتجات عبر فيديوهات قصيرة تفاعلية" else "Watch shoppable short video reels", fontSize = 10.sp, color = SoftGrayText)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF22D3EE))
                        }
                    }
                }

                // 3. Snapchat Stories Feed
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFFC00).copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSnapchat() }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFFFFFC00), modifier = Modifier.size(28.dp))
                                Column {
                                    Text(if (isAr) "قصص وسناب شات الاجتماعي" else "Snapchat Social Stories", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                    Text(if (isAr) "اكتشف أحدث العروض والقصص المجتمعية" else "Discover stories & social deals", fontSize = 10.sp, color = SoftGrayText)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFFFFC00))
                        }
                    }
                }

                // 4. Family Shopping Mode
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, SecondaryMint.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            FamilyShoppingModeCard(lang = lang, viewModel = viewModel, productsList = productsList)
                        }
                    }
                }

                // 5. Offline Recommendations (Room DB)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            com.example.ui.recommendations.OfflineRecommendationsView(
                                marketViewModel = viewModel,
                                lang = lang,
                                allProducts = productsList,
                                onProductClick = { p -> onOpenArViewer(p) }
                            )
                        }
                    }
                }
            }
        }
    }
}
