package com.example.ui.visionx

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.agent.AgentEngine
import com.example.data.agent.AgentKey
import java.util.UUID

/**
 * AI Enhancement Preset Models
 */
enum class AiImagePreset(
    val titleAr: String,
    val titleEn: String,
    val defaultBrightness: Float,
    val defaultContrast: Float,
    val defaultSaturation: Float,
    val studioBgColor: Color
) {
    STUDIO_CLEAN("استوديو أبيض ناصع", "Clean Studio", 0.15f, 1.25f, 1.15f, Color(0xFFF8FAFC)),
    WARM_ELEGANCE("إضاءة دافئة راقية", "Warm Luxury", 0.08f, 1.20f, 1.30f, Color(0xFFFEF3C7)),
    VIVID_FRESH("ألوان حيوية طازجة", "Vivid Fresh", 0.10f, 1.35f, 1.45f, Color(0xFFECFDF5)),
    HDR_POP("تباين سينمائي عالي", "HDR Pop", 0.20f, 1.40f, 1.20f, Color(0xFFEEF2FF)),
    DARK_MINIMAL("عزل داكن فاخر", "Midnight Minimal", -0.05f, 1.30f, 1.10f, Color(0xFF1E293B))
}

data class SampleProductPhoto(
    val id: String,
    val nameAr: String,
    val categoryAr: String,
    val emoji: String,
    val baseTint: Color
)

/**
 * Simple, intuitive AI Image Editing Interface for Product Photos within Creative Studio
 */
@Composable
fun AiProductImageEditor(
    onEnhancedApplied: (String, String) -> Unit = { _, _ -> }
) {
    val sampleProducts = remember {
        listOf(
            SampleProductPhoto("prod_honey", "عسل سدر جبلي طبيعي", "عسل ومربيات", "🍯", Color(0xFFD97706)),
            SampleProductPhoto("prod_olive_oil", "زيت زيتون بكر ممتاز", "زيوت وتموين", "🫒", Color(0xFF65A30D)),
            SampleProductPhoto("prod_coffee", "قهوة عربية فاخرة بالهيل", "مشروبات وبن", "☕", Color(0xFF78350F)),
            SampleProductPhoto("prod_apples", "تفاح سكري طازج", "فواكه طازجة", "🍎", Color(0xFFDC2626)),
            SampleProductPhoto("prod_cheese", "جبنة بيضاء بلدية", "ألبان وأجبان", "🧀", Color(0xFFEAB308))
        )
    }

    var selectedProduct by remember { mutableStateOf(sampleProducts[0]) }
    var selectedPreset by remember { mutableStateOf(AiImagePreset.STUDIO_CLEAN) }
    var aiIntensity by remember { mutableFloatStateOf(85f) }
    var brightness by remember { mutableFloatStateOf(selectedPreset.defaultBrightness) }
    var contrast by remember { mutableFloatStateOf(selectedPreset.defaultContrast) }
    var saturation by remember { mutableFloatStateOf(selectedPreset.defaultSaturation) }
    var showPromoBadge by remember { mutableStateOf(true) }
    var promoBadgeText by remember { mutableStateOf("⭐ العرض الذهبي -30%") }
    var showOriginal by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var lastSavedEvidenceId by remember { mutableStateOf<String?>(null) }

    // Update adjustment values when preset changes
    LaunchedEffect(selectedPreset) {
        brightness = selectedPreset.defaultBrightness
        contrast = selectedPreset.defaultContrast
        saturation = selectedPreset.defaultSaturation
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_product_image_editor"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Product Selector Strip
        Text(
            text = "اختر صورة المنتج المراد تحسينها:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sampleProducts) { prod ->
                val isSelected = prod.id == selectedProduct.id
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                    modifier = Modifier
                        .clickable {
                            selectedProduct = prod
                            lastSavedEvidenceId = null
                        }
                        .testTag("sample_product_${prod.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(prod.emoji, fontSize = 18.sp)
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(prod.nameAr, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            Text(prod.categoryAr, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Image Canvas / Preview Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .testTag("image_preview_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (showOriginal) Color(0xFFE2E8F0) else selectedPreset.studioBgColor
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Studio Lighting or Gradient
                if (!showOriginal) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.85f),
                                        selectedPreset.studioBgColor
                                    )
                                )
                            )
                    )
                }

                // Center Product Art with dynamic AI visual rendering
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Studio Pedestal / Ground Shadow
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (showOriginal) Color(0xFFCBD5E1)
                                else selectedProduct.baseTint.copy(alpha = 0.18f + (saturation * 0.05f))
                            )
                            .border(
                                width = if (showOriginal) 0.dp else 2.dp,
                                color = if (showOriginal) Color.Transparent else selectedProduct.baseTint.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedProduct.emoji,
                            fontSize = 62.sp,
                            modifier = Modifier
                                .shadow(
                                    elevation = if (showOriginal) 0.dp else (8 * (contrast - 0.5f)).dp,
                                    shape = CircleShape
                                )
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = selectedProduct.nameAr,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (selectedPreset == AiImagePreset.DARK_MINIMAL && !showOriginal) Color.White else Color(0xFF0F172A)
                    )

                    // Tag
                    Text(
                        text = if (showOriginal) "📷 الصورة الأصلية (دون تحسين)" else "✨ معززة بذكاء اصطناعي (دقة 4K)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (showOriginal) Color(0xFF64748B) else Color(0xFF059669)
                    )
                }

                // Promotional Badge Overlay
                if (showPromoBadge && !showOriginal) {
                    Surface(
                        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                        color = Color(0xFFEF4444),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 12.dp)
                    ) {
                        Text(
                            text = promoBadgeText,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Before / After Toggle Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clickable { showOriginal = !showOriginal }
                        .testTag("btn_toggle_original")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (showOriginal) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (showOriginal) "العودة للتحسين" else "مقارنة بالأصل",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Presets Strip
        Text(
            text = "أنماط التحسين الجاهزة (AI Presets):",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(AiImagePreset.entries) { preset ->
                val isSelected = preset == selectedPreset
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedPreset = preset },
                    label = { Text(preset.titleAr, fontSize = 11.sp) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null
                )
            }
        }

        // Fine Tuning Sliders
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("شدة المعالجة الذكية (AI Intensity):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${aiIntensity.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = aiIntensity,
                onValueChange = { aiIntensity = it },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("الإضاءة والسطوع (Brightness):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(String.format("%.2f", brightness), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = brightness,
                onValueChange = { brightness = it },
                valueRange = -0.3f..0.5f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Badge Toggle Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Loyalty, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("إضافة شريط العرض الترويجي (Promo Ribbon)", fontSize = 11.sp)
            }
            Switch(
                checked = showPromoBadge,
                onCheckedChange = { showPromoBadge = it }
            )
        }

        // Apply Enhancement Button
        Button(
            onClick = {
                isProcessing = true
                val evidenceId = "EVD_IMG_${UUID.randomUUID().toString().take(8)}"
                lastSavedEvidenceId = evidenceId

                AgentEngine.recordEvidence(
                    operationId = "IMG_ENHANCE_${selectedProduct.id}_${UUID.randomUUID().toString().take(6)}",
                    actorId = AgentKey.DESIGN_PRESENTATION.key,
                    type = "AI_STUDIO_ENHANCED_IMAGE",
                    source = "Creative Studio (AiProductImageEditor)",
                    payloadSummary = "Applied AI enhancement preset '${selectedPreset.titleAr}' to product '${selectedProduct.nameAr}' (Intensity: ${aiIntensity.toInt()}%, Brightness: $brightness). Output: 4K Studio Isolated."
                )

                onEnhancedApplied(selectedProduct.nameAr, evidenceId)
                isProcessing = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_apply_ai_enhancement"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0284C7)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AutoFixHigh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("تطبيق التحسين واعتماد الصورة في المتجر")
        }

        // Evidence Confirmation Box
        AnimatedVisibility(visible = lastSavedEvidenceId != null) {
            lastSavedEvidenceId?.let { evId ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    border = BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("تم تطبيق التحسين وتوثيقه كدليل معتمد في النظام!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            Text("Evidence ID: $evId", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF047857))
                        }
                    }
                }
            }
        }
    }
}
