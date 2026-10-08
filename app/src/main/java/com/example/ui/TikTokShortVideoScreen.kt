package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TikTokShortVideoScreen(viewModel: MarketViewModel, lang: String, onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 5 })

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            when (page % 3) {
                                0 -> Color(0xFF0B0F19)
                                1 -> Color(0xFF1E1B4B)
                                else -> Color(0xFF022C22)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(72.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == "ar") "فيديو قصير رقم ${page + 1} (TikTok Style)" else "Short Video #${page + 1} (TikTok Style)",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Overlay details (TikTok style right actions & bottom caption)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (lang == "ar") "🔥 مقاطع الفيديو الرائجة" else "🔥 Trending Shorts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        // Right actions & Bottom caption
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Bottom Caption
                            Column(modifier = Modifier.fillMaxWidth(0.75f)) {
                                Text(text = "@creator_space_${page + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = if (lang == "ar") "تجربة تسوق ذكية ومذهلة عبر تطبيق عامر للذكاء الاصطناعي! 🚀✨ #اكسبلور #تسوق" else "Amazing smart shopping experience on Amer AI Store! 🚀✨ #explore #shopping", color = Color(0xFFE0F7FA), fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(14.dp))
                                    Text(text = "Original Sound - Amer AI Studio", color = Color(0xFF00F5D4), fontSize = 11.sp)
                                }
                            }

                            // Right Side Actions (Like, Comment, Share)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(onClick = { }, modifier = Modifier.size(44.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                                        Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color(0xFFEF4444))
                                    }
                                    Text(text = "24.5K", color = Color.White, fontSize = 10.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(onClick = { }, modifier = Modifier.size(44.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                                        Icon(Icons.Default.Comment, contentDescription = "Comment", tint = Color.White)
                                    }
                                    Text(text = "1,420", color = Color.White, fontSize = 10.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(onClick = { }, modifier = Modifier.size(44.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                                    }
                                    Text(text = "340", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
