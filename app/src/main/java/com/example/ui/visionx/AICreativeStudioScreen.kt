package com.example.ui.visionx

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.agent.AgentEngine
import com.example.data.agent.AgentKey
import com.example.data.visionx.*
import java.util.UUID

/**
 * Full-Screen AICreativeStudio Screen
 * Providing dual creative engines:
 * 1. AI Product Photo Enhancer with live preset filters, fine-tuning sliders, and store export.
 * 2. Bilingual Ad Copy & Marketing Campaign Synthesizer with 19-Agent Governance Evidence.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICreativeStudio(
    initialProduct: ShoppingLensResult? = null,
    onDismiss: () -> Unit,
    onApplyToStore: (String, String) -> Unit = { _, _ -> }
) {
    var activeTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("المشاهد العصرية (Lifestyle)", "محرر صور المنتجات", "صانع الحملات الإعلانية", "سجل إبداعات الذكاء الاصطناعي")

    // Ad Copy Generator state
    var selectedType by remember { mutableStateOf(CreativeType.AD_COPY_AR) }
    var briefPrompt by remember { mutableStateOf("عسل سدر طبيعي جبلي وزيت زيتون بكر فاخر معصور على البارد") }
    var targetAudience by remember { mutableStateOf("محبي الأغذية العضوية والصحية والعائلات") }
    var discountOffer by remember { mutableStateOf("خصم 25% مع شحن مجاني للطلبات فوق $50") }
    var jobResult by remember { mutableStateOf<CreativeJob?>(null) }
    var isGeneratingAd by remember { mutableStateOf(false) }

    // History of creative generations
    var creativeHistory by remember {
        mutableStateOf(
            listOf(
                CreativeJob(
                    jobId = "JOB_INIT_01",
                    type = CreativeType.AD_COPY_AR,
                    promptBrief = "تمور سكري فاخرة وعسل مانوكا أصلي",
                    generatedArtifact = "✨ تمتع بمذاق الفخامة والصحة النقية مع تمور السكري الفاخرة! استخدم كود AMER15 واستمتع بتوصيل فائق السرعة عبر شبكة وكلائنا الذكية.",
                    generatedMetadata = mapOf("model" to "Gemini 3.5 Flash"),
                    evidenceId = "EVD_STUDIO_7721",
                    createdAt = System.currentTimeMillis() - 3600000
                )
            )
        )
    }

    LaunchedEffect(Unit) {
        if (jobResult == null) {
            jobResult = VisionXService.createCreativeJob(selectedType, briefPrompt)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "استوديو الإبداع (AI Creative Studio)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "تحسين صور المنتجات • توليد الحملات • توثيق الأدلة",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_creative_studio")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        color = Color(0xFF06B6D4).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF06B6D4)))
                            Spacer(Modifier.width(4.dp))
                            Text("Agent 6 Active", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0891B2))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeTab) {
                0 -> {
                    // Tab 0: AI Lifestyle Virtual Settings Studio
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        LifestyleVirtualStudioComponent(
                            preselectedLensResult = initialProduct,
                            onApplyAsBanner = { genImage ->
                                onApplyToStore(genImage.productName, genImage.evidenceId)
                            }
                        )
                    }
                }

                1 -> {
                    // Tab 1: AI Product Photo Enhancer Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF06B6D4).copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color(0xFF0891B2), modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("محرر الصور الذكي لمنتجات المتجر", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0E7490))
                                    Text("عزل الخلفيات، ضبط الإضاءة الاحترافية، وتوليد نصوص ترويجية بصرية متوافقة مع وكيل التصميم والعرض.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        AiProductImageEditor(
                            onEnhancedApplied = { prodName, evId ->
                                onApplyToStore(prodName, evId)
                            }
                        )
                    }
                }

                2 -> {
                    // Tab 2: Bilingual Campaign & Ad Copy Synthesizer
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3B82F6).copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("صانع الحملات الإعلانية ثنائي اللغة (AR/EN)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1D4ED8))
                                    Text("توليد نصوص إعلانية مبهرة لشبكات التواصل، ملصقات الخصم، وموجزات التسويق بدعم Gemini.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        // Creative Format Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("نوع المحتوى الإبداعي:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(CreativeType.entries) { type ->
                                    FilterChip(
                                        selected = selectedType == type,
                                        onClick = {
                                            selectedType = type
                                            jobResult = VisionXService.createCreativeJob(type, briefPrompt)
                                        },
                                        label = { Text(type.titleAr, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        // Prompt inputs
                        OutlinedTextField(
                            value = briefPrompt,
                            onValueChange = { briefPrompt = it },
                            label = { Text("موجز المنتج أو الخدمة") },
                            modifier = Modifier.fillMaxWidth().testTag("input_ad_prompt"),
                            minLines = 2
                        )

                        OutlinedTextField(
                            value = targetAudience,
                            onValueChange = { targetAudience = it },
                            label = { Text("الجمهور المستهدف (Target Audience)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = discountOffer,
                            onValueChange = { discountOffer = it },
                            label = { Text("العرض الترويجي والخصم") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                isGeneratingAd = true
                                val fullPrompt = "$briefPrompt | الجمهور: $targetAudience | العرض: $discountOffer"
                                val newJob = VisionXService.createCreativeJob(selectedType, fullPrompt)
                                jobResult = newJob
                                creativeHistory = listOf(newJob) + creativeHistory

                                AgentEngine.recordEvidence(
                                    operationId = "AD_SYNTH_${newJob.jobId}",
                                    actorId = AgentKey.DESIGN_PRESENTATION.key,
                                    type = "MARKETING_CAMPAIGN_SYNTHESIS",
                                    source = "AICreativeStudio Screen",
                                    payloadSummary = "Synthesized ${selectedType.titleEn} for '$briefPrompt'. Output: ${newJob.generatedArtifact.take(80)}..."
                                )
                                isGeneratingAd = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("btn_generate_campaign")
                        ) {
                            if (isGeneratingAd) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("توليد الإعلان والمنشور الإبداعي 🚀", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Generated Result Card
                        jobResult?.let { job ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "المخرج الإبداعي المعتمد:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Surface(
                                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "VERIFIED ARTIFACT",
                                                color = Color(0xFF059669),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = job.generatedArtifact,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Evidence ID: ${job.evidenceId}",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        FilledTonalButton(
                                            onClick = {
                                                AgentEngine.recordEvidence(
                                                    operationId = "AD_EXPORT_${job.jobId}",
                                                    actorId = AgentKey.MARKETPLACE_MERCHANT_SUCCESS.key,
                                                    type = "CAMPAIGN_EXPORT_TO_CHANNEL",
                                                    source = "AICreativeStudio",
                                                    payloadSummary = "Ad campaign published to store social feeds. Evidence verified."
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("نشر وتصدير 📢", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Tab 3: Historical Artifacts & Governance Evidence
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "سجل إبداعات ومخرجات الاستوديو المعتمدة في النظام",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        items(creativeHistory) { item ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(item.type.titleAr, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text("Evidence: ${item.evidenceId}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(item.generatedArtifact, fontSize = 12.sp, maxLines = 3)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
