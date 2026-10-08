package com.example.ui.recommendations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ProductEntity
import com.example.data.RecentlyViewedProductEntity
import com.example.ui.MarketViewModel
import com.example.ui.theme.*

@Composable
fun OfflineRecommendationsView(
    marketViewModel: MarketViewModel,
    lang: String,
    allProducts: List<ProductEntity>,
    onProductClick: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val recentlyViewed by marketViewModel.recentlyViewed.collectAsState()
    val interactions by marketViewModel.userInteractions.collectAsState()

    // Offline-first recommendation logic:
    // Find categories the user interacted with most, then recommend products from those categories
    val recommendedProducts = remember(recentlyViewed, interactions, allProducts) {
        val viewedCategories = recentlyViewed.map { it.category }.toSet()
        val interactedCategories = interactions.map { it.category }.toSet()
        val preferredCategories = viewedCategories + interactedCategories

        if (preferredCategories.isNotEmpty()) {
            allProducts.filter { prod -> preferredCategories.contains(prod.category) }
                .shuffled()
                .take(6)
        } else {
            allProducts.take(6)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Recently Viewed Products Section (Room Database)
        if (recentlyViewed.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lang == "ar") "👀 المنتجات المشاهدة مؤخراً (محلي)" else "👀 Recently Viewed (Offline Room)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolarLight
                        )
                    }
                    Text(
                        text = "${recentlyViewed.size} items",
                        fontSize = 10.sp,
                        color = SoftGrayText
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(recentlyViewed, key = { it.productId }) { item ->
                        RecentlyViewedCard(
                            item = item,
                            lang = lang,
                            onClick = {
                                val match = allProducts.find { it.id == item.productId }
                                if (match != null) {
                                    onProductClick(match)
                                }
                            }
                        )
                    }
                }
            }
        }

        // 2. Offline-First Smart Recommendations Section (Room Database + Interaction History)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Recommend, contentDescription = null, tint = SecondaryMint, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (lang == "ar") "🎯 توصيات ذكية بدون إنترنت (تاريخ التصفح)" else "🎯 Offline Smart Recommendations",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolarLight
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SecondaryMint.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, SecondaryMint)
                ) {
                    Text(
                        text = if (lang == "ar") "محلي 100% 💾" else "100% Local 💾",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryMint,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (recommendedProducts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardDarkBg)
                ) {
                    Text(
                        text = if (lang == "ar") "تصفح المنتجات لبدء توليد توصيات ذكية محلية..." else "Browse products to generate local offline recommendations...",
                        fontSize = 11.sp,
                        color = SoftGrayText,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(recommendedProducts, key = { it.id }) { product ->
                        OfflineRecommendedProductCard(
                            product = product,
                            lang = lang,
                            onClick = { onProductClick(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentlyViewedCard(
    item: RecentlyViewedProductEntity,
    lang: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg),
        border = BorderStroke(0.5f.dp, PrimaryCyan.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateDarkBg)
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.productName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.productName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PolarLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$${item.retailPrice}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = SecondaryMint
            )
        }
    }
}

@Composable
fun OfflineRecommendedProductCard(
    product: ProductEntity,
    lang: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkBg),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(SecondaryMint.copy(alpha = 0.5f), PrimaryCyan.copy(alpha = 0.5f))))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateDarkBg)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    shape = RoundedCornerShape(bottomStart = 6.dp),
                    color = SecondaryMint,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "Match ✨",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = SlateDarkBg,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = product.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PolarLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = product.category,
                fontSize = 9.sp,
                color = PrimaryCyan,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$${product.retailPrice}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = SecondaryMint
            )
        }
    }
}
