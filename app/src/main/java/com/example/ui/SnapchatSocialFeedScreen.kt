package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapchatSocialFeedScreen(viewModel: MarketViewModel, lang: String, onDismiss: () -> Unit) {
    var privacyMode by remember { mutableStateOf("Friends Only") } // Public, Friends Only, Private
    var showContactSyncDialog by remember { mutableStateOf(false) }
    var suggestedFriends by remember { mutableStateOf(listOf(
        FriendSuggestion("Sarah M.", "+962 79 123 4567", "Matched from Contacts"),
        FriendSuggestion("Ahmad K.", "+962 78 987 6543", "Matched from Contacts"),
        FriendSuggestion("Lina R.", "+962 77 555 1122", "Mutual Friends")
    )) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF030712)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(28.dp))
                    Text(
                        text = if (lang == "ar") "مجتمع سناب الاجتماعي" else "Snapchat Social Feed",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { showContactSyncDialog = true }) {
                        Icon(Icons.Default.Contacts, contentDescription = "Sync Contacts", tint = Color(0xFF00F5D4))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stories Rail (Snapchat Style)
            Text(text = if (lang == "ar") "القصص واللقطات (Stories)" else "Stories & Snaps", color = Color(0xFF00F5D4), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(6) { idx ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .border(2.5.dp, Color(0xFF00F5D4), CircleShape)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👤", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "User $idx", color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy & Feed Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (lang == "ar") "المنشورات والخصوصية" else "Posts & Privacy", color = Color(0xFF00F5D4), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = privacyMode, color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Feed Posts
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(4) { index ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.size(36.dp).background(Color(0xFF00F5D4), CircleShape), contentAlignment = Alignment.Center) {
                                    Text(text = "🤖", fontSize = 18.sp)
                                }
                                Column {
                                    Text(text = "Community Creator $index", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "2 hours ago • 🔒 Friends Only", color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Exploring the latest organic produce and discovering amazing products on Amer AI Store! ✨🚀", color = Color(0xFFE0F7FA), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(18.dp))
                                    Text(text = "142", color = Color.White, fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.ModeComment, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(18.dp))
                                    Text(text = "28", color = Color.White, fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF00F5D4), modifier = Modifier.size(18.dp))
                                    Text(text = "Share", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Contact Sync Dialog (Snapchat Style Friend Suggestions)
    if (showContactSyncDialog) {
        AlertDialog(
            onDismissRequest = { showContactSyncDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Contacts, contentDescription = null, tint = Color(0xFF00F5D4))
                    Text(text = if (lang == "ar") "مزامنة جهات الاتصال (اقتراح الأصدقاء)" else "Sync Contacts (Friend Suggestions)", color = Color.White, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = if (lang == "ar") "تعرف على أصدقائك عبر مزامنة دفتر جهات الاتصال بأمان تام مع الحفاظ على الخصوصية:" else "Discover friends by syncing your contacts securely with privacy controls:", color = Color(0xFFE0F7FA), fontSize = 12.sp)
                    suggestedFriends.forEach { friend ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0B0F19),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(text = friend.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = friend.reason, color = Color(0xFF00F5D4), fontSize = 10.sp)
                                }
                                Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4)), modifier = Modifier.height(32.dp)) {
                                    Text(text = "Add", color = Color.Black, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showContactSyncDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))) {
                    Text(text = "Done", color = Color.White)
                }
            },
            containerColor = Color(0xFF0B0F19)
        )
    }
}

data class FriendSuggestion(val name: String, val phone: String, val reason: String)
