package com.example.ui

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AiAgentActionRecord
import com.example.data.AiToolType
import com.example.ui.theme.*
import com.example.ui.agent.AuraRobot
import com.example.ui.agent.RobotExpression

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMasterAgentDialog(
    viewModel: MarketViewModel,
    lang: String,
    onDismiss: () -> Unit
) {
    val isBusy by viewModel.isMasterAgentBusy.collectAsState()
    val activeTool by viewModel.currentActiveAgentTool.collectAsState()
    val history by viewModel.masterAgentActivityHistory.collectAsState()
    val latestRecord by viewModel.latestMasterAgentRecord.collectAsState()

    var promptInput by remember { mutableStateOf("") }
    var selectedTool by remember { mutableStateOf<AiToolType?>(null) }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var selectedResolution by remember { mutableStateOf("1K") }
    var selectedVideoAspect by remember { mutableStateOf("16:9") }
    var isProModel by remember { mutableStateOf(false) }

    val aspectRatios = listOf("1:1", "16:9", "9:16", "4:3", "3:4", "3:2", "2:3", "21:9")
    val resolutions = listOf("1K", "2K", "4K")
    val videoAspects = listOf("16:9", "9:16")

    val isAr = lang == "ar"

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(PrimaryCyan, SecondaryMint)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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
                        AuraRobot(
                            size = 52.dp,
                            expression = if (isBusy) RobotExpression.SCANNING else RobotExpression.TALKING,
                            isScanning = isBusy,
                            isTalking = isBusy,
                            auraColor = Color(0xFF00E676),
                            secondaryAuraColor = Color(0xFF00B0FF),
                            torsoVisible = false
                        )
                        Column {
                            Text(
                                text = if (isAr) "وكيل الذكاء الاصطناعي الشامل" else "AI Master Orchestrator Agent",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAr) "منصة ربط جميع نماذج وأدوات Gemini & Firebase" else "Unified Hub for all Gemini models & Firebase",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Tool Selection Chips
                    item {
                        Text(
                            text = if (isAr) "اختر الأداة أو اترك الوكيل يحدد تلقائياً:" else "Select Tool (or auto-detect by Agent):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedTool == null,
                                    onClick = { selectedTool = null },
                                    label = { Text(if (isAr) "✨ ذكاء تلقائي شامل" else "✨ Auto Orchestrator") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = PrimaryCyan
                                    )
                                )
                            }
                            items(AiToolType.values()) { tool ->
                                FilterChip(
                                    selected = selectedTool == tool,
                                    onClick = { selectedTool = tool },
                                    label = {
                                        Text(
                                            text = if (isAr) tool.displayNameAr else tool.displayNameEn,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SecondaryMint.copy(alpha = 0.25f),
                                        selectedLabelColor = SecondaryMint
                                    )
                                )
                            }
                        }
                    }

                    // Dynamic Sub-controls (Aspect ratio, Resolution, etc.)
                    if (selectedTool == AiToolType.IMAGE_STUDIO || selectedTool == null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (isAr) "🎨 إعدادات الصور (Aspect Ratio & Resolution):" else "🎨 Image Settings (Ratio & Size):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        aspectRatios.take(4).forEach { ratio ->
                                            AssistChip(
                                                onClick = { selectedAspectRatio = ratio },
                                                label = { Text(ratio, fontSize = 10.sp) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = if (selectedAspectRatio == ratio) PrimaryCyan.copy(alpha = 0.2f) else Color.Transparent
                                                )
                                            )
                                        }
                                        resolutions.forEach { res ->
                                            AssistChip(
                                                onClick = { selectedResolution = res },
                                                label = { Text(res, fontSize = 10.sp) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = if (selectedResolution == res) SecondaryMint.copy(alpha = 0.2f) else Color.Transparent
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (selectedTool == AiToolType.VEO_VIDEO) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isAr) "🎬 أبعاد فيديو Veo 3:" else "🎬 Veo 3 Video Ratio:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        videoAspects.forEach { vAspect ->
                                            FilterChip(
                                                selected = selectedVideoAspect == vAspect,
                                                onClick = { selectedVideoAspect = vAspect },
                                                label = { Text(vAspect, fontSize = 10.sp) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Input Field
                    item {
                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            placeholder = {
                                Text(
                                    text = if (isAr) "اكتب طلبك لوكيل الذكاء الاصطناعي (مثل: قارن الأسعار، ألف لحن تسوق، حلل المكونات، صمم صورة، لخص...)" else "Ask AI Master Agent (e.g., price check, make grocery song, analyze nutrition, generate image...)",
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    // Quick Suggested Prompts
                    item {
                        Text(
                            text = if (isAr) "⚡ أوامر سريعة مقترحة للوكيل:" else "⚡ Quick Agent Actions:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                if (isAr) "تأليف أغنية تسوق هادئة" else "Make grocery jingle",
                                if (isAr) "تفكير عميق: خطة توفير شهرية" else "High Thinking: Monthly savings plan",
                                if (isAr) "بحث مباشر عن أسعار الزيت اليوم" else "Search Grounding: Today's oil prices",
                                if (isAr) "مواقع متاجر المنتجات العضوية القريبة" else "Maps: Nearby organic stores",
                                if (isAr) "توليد صورة سلة فواكه طبيعية" else "Generate organic fruit basket image"
                            ).forEach { quickPrompt ->
                                SuggestionChip(
                                    onClick = { promptInput = quickPrompt },
                                    label = { Text(quickPrompt, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    // Execute Button
                    item {
                        Button(
                            onClick = {
                                if (promptInput.isNotBlank()) {
                                    viewModel.executeUnifiedAiAgentRequest(
                                        prompt = promptInput,
                                        explicitTool = selectedTool,
                                        aspectRatio = selectedAspectRatio,
                                        imageResolution = selectedResolution,
                                        isPro = isProModel,
                                        videoAspect = selectedVideoAspect,
                                        appliedFeature = "Global Market Master Agent"
                                    )
                                }
                            },
                            enabled = promptInput.isNotBlank() && !isBusy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryCyan
                            )
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isAr) "جاري تشغيل الوكيل الذكي..." else "Agent Executing...",
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Execute")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAr) "تنفيذ عبر الوكيل الشامل" else "Run AI Master Agent",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Latest Result Card
                    latestRecord?.let { record ->
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                ),
                                border = BorderStroke(1.dp, PrimaryCyan.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Success",
                                                tint = SecondaryMint,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = if (isAr) record.toolType.displayNameAr else record.toolType.displayNameEn,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Badge(
                                            containerColor = PrimaryCyan.copy(alpha = 0.2f),
                                            contentColor = PrimaryCyan
                                        ) {
                                            Text(record.toolType.model, fontSize = 9.sp, modifier = Modifier.padding(2.dp))
                                        }
                                    }

                                    Text(
                                        text = record.resultText,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp
                                    )

                                    // Action to apply to app tools
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = {
                                                viewModel.addFamilyShoppingItem(
                                                    productName = if (record.prompt.length > 25) record.prompt.take(25) + "..." else record.prompt,
                                                    category = "AI Recommendations",
                                                    priority = "High",
                                                    quantity = 1
                                                )
                                            }
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Add to shopping list", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isAr) "إضافة لقائمة التسوق" else "Add to List", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Activity History
                    if (history.isNotEmpty()) {
                        item {
                            Text(
                                text = if (isAr) "سجل نشاط الوكيل الشامل:" else "Agent Execution History:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        items(history.take(4)) { histItem ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = histItem.prompt,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = histItem.resultText.take(120) + if (histItem.resultText.length > 120) "..." else "",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
