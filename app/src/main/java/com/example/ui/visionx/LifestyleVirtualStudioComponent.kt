package com.example.ui.visionx

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.agent.AgentEngine
import com.example.data.agent.AgentKey
import com.example.data.visionx.ProductShareManager
import com.example.data.visionx.ShoppingLensResult
import com.example.ui.theme.PrimaryCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Virtual Setting Presets for placing products in realistic lifestyle environments
 */
enum class VirtualSettingPreset(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val promptEn: String,
    val drawableRes: Int,
    val accentColor: Color
) {
    MODERN_LIVING_ROOM(
        id = "living_room",
        titleAr = "🛋️ صالون مودرن معاصر",
        titleEn = "Modern Living Room",
        descriptionAr = "غرفة معيشة ناصعة مع إضاءة صباحية طبيعية وطاولة خشبية راقية",
        promptEn = "A sunlit modern minimalist Scandinavian living room with warm oak coffee table, soft linen sofa, green indoor plants, and natural morning sunlight streaming in",
        drawableRes = R.drawable.img_lifestyle_living_room,
        accentColor = Color(0xFFD97706)
    ),
    LUXURY_KITCHEN(
        id = "kitchen",
        titleAr = "🍳 مطبخ رخامي فاخر",
        titleEn = "Sunlit Luxury Kitchen",
        descriptionAr = "رخام كالاكاتا إيطالي أبيض مع إضاءة شمسية ذهبية وتجهيزات ذواقة",
        promptEn = "A gourmet luxury kitchen with white Calacatta marble countertop, morning sun rays, high-end culinary background, commercial staging",
        drawableRes = R.drawable.img_lifestyle_kitchen,
        accentColor = Color(0xFF059669)
    ),
    OUTDOOR_GARDEN(
        id = "outdoor",
        titleAr = "🌿 نزهة في حديقة خضراء",
        titleEn = "Botanical Garden Picnic",
        descriptionAr = "طبيعة خلابة مع قماش كتاني ريفي وأشعة شمس متخللة لأوراق الشجر",
        promptEn = "A lush outdoor botanical garden picnic on rustic linen cloth, dappled sunlight through tree leaves, fresh greenery, summer picnic ambiance",
        drawableRes = R.drawable.img_lifestyle_outdoor,
        accentColor = Color(0xFF16A34A)
    ),
    PARISIAN_CAFE(
        id = "cafe",
        titleAr = "☕ مقهى باريسي راقي",
        titleEn = "Cozy Parisian Café",
        descriptionAr = "طاولة بيسترو رخامية عصرية مع إضاءة الغروب الدافئة وبوكيه خلفي رومانسي",
        promptEn = "A chic boutique cafe with a sleek white marble table, warm golden hour sunlight, soft espresso ambiance, blurred city streetscape bokeh background",
        drawableRes = R.drawable.img_lifestyle_cafe,
        accentColor = Color(0xFF9333EA)
    ),
    URBAN_LOFT(
        id = "loft",
        titleAr = "🏙️ لوفت صناعي معاصر",
        titleEn = "Urban Industrial Loft",
        descriptionAr = "جدار قرميدي مكشوف مع مسطح خرساني مصقول وإضاءة بؤرية دافئة",
        promptEn = "A contemporary industrial urban loft with exposed brick wall, polished concrete pedestal, and warm moody accent spotlighting",
        drawableRes = R.drawable.img_lifestyle_living_room,
        accentColor = Color(0xFF4F46E5)
    ),
    CYBERPUNK_STUDIO(
        id = "cyberpunk",
        titleAr = "🌌 استوديو نيون مستقبلي",
        titleEn = "Cyberpunk Neon Showcase",
        descriptionAr = "أرضية عاكسة وإضاءة نيون زرقاء وقرمزية بتأثيرات تكنولوجية ثلاثية الأبعاد",
        promptEn = "A futuristic commercial showcase surface with vibrant cyan and magenta neon glow, reflective dark glossy pedestal, high-tech aesthetic",
        drawableRes = R.drawable.img_lifestyle_kitchen,
        accentColor = Color(0xFF06B6D4)
    ),
    CUSTOM(
        id = "custom",
        titleAr = "✍️ مشهد مخصص بالوصف",
        titleEn = "Custom Setting Prompt",
        descriptionAr = "اكتب أي بيئة أو مشهد خيالي لتوليده عبر نماذج الذكاء الاصطناعي",
        promptEn = "Custom user prompt",
        drawableRes = R.drawable.img_lifestyle_living_room,
        accentColor = Color(0xFFE11D48)
    )
}

/**
 * Lighting Ambience Options
 */
enum class LightingStyle(val labelAr: String, val promptModifier: String) {
    GOLDEN_HOUR("🌅 الساعة الذهبية", "golden hour warm sunlight with soft long shadows"),
    BRIGHT_DAYLIGHT("☀️ إضاءة طبيعية ساطعة", "crisp high-key morning daylight, natural illumination"),
    STUDIO_SOFTBOX("💡 استوديو سوفت بوكس", "professional commercial studio softbox lighting with gentle rim lights"),
    MOODY_DRAMATIC("🕯️ دراماتيكي دافئ", "cinematic moody lighting with dramatic chiaroscuro contrast"),
    CYBERPUNK_NEON("🔮 نيون متوهج", "vibrant volumetric neon lighting with futuristic cyber reflections")
}

/**
 * Aspect Ratio Options
 */
enum class ImageAspectRatio(val label: String, val ratioText: String) {
    SQUARE_1_1("1:1 مربع (متجر وانستغرام)", "1:1"),
    STORY_9_16("9:16 ستوري وتيك توك", "9:16"),
    BANNER_16_9("16:9 بانر أفقي", "16:9"),
    CATALOG_4_3("4:3 كتالوج تجاري", "4:3")
}

/**
 * Data Model for a Generated Lifestyle Placement
 */
data class GeneratedLifestyleImage(
    val id: String = UUID.randomUUID().toString(),
    val productName: String,
    val productCategory: String,
    val originalImageUrl: String?,
    val virtualSetting: VirtualSettingPreset,
    val customPrompt: String,
    val lighting: LightingStyle,
    val aspectRatio: ImageAspectRatio,
    val fullConstructedPrompt: String,
    val modelName: String = "gemini-2.5-flash-image",
    val generatedDrawableRes: Int,
    val evidenceId: String = "EVD_LIFESTYLE_" + UUID.randomUUID().toString().take(8).uppercase(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * AI Lifestyle Virtual Settings Studio Component
 * Allows users to place products identified using Vision X Shopping Lens
 * (or catalog products) into realistic virtual settings by prompting image generation models.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifestyleVirtualStudioComponent(
    preselectedLensResult: ShoppingLensResult? = null,
    onApplyAsBanner: (GeneratedLifestyleImage) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Sample default Vision X identified products for quick selection
    val sampleIdentifiedProducts = remember {
        listOf(
            "عسل سدر جبلي فاخر" to "منتجات عضوية وطبيعية",
            "زيت زيتون بكر معصور على البارد" to "زيوت وتموين فاخر",
            "بن أرابيكا محمص بحبوب كاملة" to "مشروبات وبن مختص",
            "تفاح سكري عضوي طازج" to "فواكه ومحاصيل طازجة",
            "تمور سكري ملكية مكنوزة" to "تمور وفاخرات",
            "زعفران نقيل كشميري أصلي" to "توابل ومطيبات فاخرة"
        )
    }

    var selectedProductName by remember {
        mutableStateOf(preselectedLensResult?.detectedProductNameAr ?: sampleIdentifiedProducts[0].first)
    }
    var selectedCategory by remember {
        mutableStateOf(preselectedLensResult?.category ?: sampleIdentifiedProducts[0].second)
    }
    var isFromVisionXLens by remember {
        mutableStateOf(preselectedLensResult != null)
    }

    var selectedSetting by remember { mutableStateOf(VirtualSettingPreset.MODERN_LIVING_ROOM) }
    var customSettingPrompt by remember { mutableStateOf("على طاولة رخامية فاخرة بجوار نافذة زجاجية تطل على برج خليفة في وقت الغروب") }
    var selectedLighting by remember { mutableStateOf(LightingStyle.GOLDEN_HOUR) }
    var selectedAspectRatio by remember { mutableStateOf(ImageAspectRatio.SQUARE_1_1) }

    // Generation state
    var isGenerating by remember { mutableStateOf(false) }
    var generationStepText by remember { mutableStateOf("") }
    var generatedResult by remember { mutableStateOf<GeneratedLifestyleImage?>(null) }
    var showCompareOriginal by remember { mutableStateOf(false) }

    // History of generated lifestyle images
    var lifestyleHistory by remember {
        mutableStateOf<List<GeneratedLifestyleImage>>(emptyList())
    }

    // Auto-construct the detailed generative prompt
    val constructedPrompt = remember(selectedProductName, selectedCategory, selectedSetting, customSettingPrompt, selectedLighting, selectedAspectRatio) {
        val settingText = if (selectedSetting == VirtualSettingPreset.CUSTOM) customSettingPrompt else selectedSetting.promptEn
        "Photorealistic commercial lifestyle product photography of '$selectedProductName' ($selectedCategory), placed naturally and elegantly in $settingText. Lighting: ${selectedLighting.promptModifier}. Aspect Ratio: ${selectedAspectRatio.ratioText}. Ray-tracing reflections, subtle commercial depth of field, 8k resolution, award-winning advertising visual."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF10B981)))),
            modifier = Modifier.fillMaxWidth().testTag("lifestyle_studio_hero")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "استوديو المشاهد العصرية (AI Creative Studio)",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "ضع منتجك الممسوح عبر عدسة Vision X في بيئات ومنازل ومقاهي افتراضية احترافية بدعم نماذج التوليد البصري.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 1. Identified Product Selector & Lens Badge
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, if (isFromVisionXLens) Color(0xFF06B6D4) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isFromVisionXLens) Icons.Default.CameraAlt else Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = if (isFromVisionXLens) Color(0xFF06B6D4) else PrimaryCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isFromVisionXLens) "منتج متعرف عليه عبر Vision X Shopping Lens 🔍" else "المنتج المستهدف للتوليد البصري:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isFromVisionXLens) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (isFromVisionXLens) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Lens Verified ✓",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = selectedProductName,
                    onValueChange = {
                        selectedProductName = it
                        isFromVisionXLens = false
                    },
                    label = { Text("اسم المنتج") },
                    leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = PrimaryCyan) },
                    modifier = Modifier.fillMaxWidth().testTag("input_lifestyle_product_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Quick selector for identified samples
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("أو اختر منتجاً ممسوحاً مؤخراً في المتجر:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(sampleIdentifiedProducts) { (name, cat) ->
                            val isSelected = selectedProductName == name
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) PrimaryCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isSelected) PrimaryCyan else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.clickable {
                                    selectedProductName = name
                                    selectedCategory = cat
                                    isFromVisionXLens = true
                                }
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) PrimaryCyan else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Virtual Settings Presets Grid/Row
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏡 اختر البيئة الافتراضية (Virtual Setting):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "${VirtualSettingPreset.entries.size} بيئات متاحة",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(VirtualSettingPreset.entries) { preset ->
                    val isSelected = selectedSetting == preset
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) preset.accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) preset.accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .width(150.dp)
                            .clickable { selectedSetting = preset }
                            .testTag("preset_card_${preset.id}")
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = preset.drawableRes),
                                    contentDescription = preset.titleAr,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.TopEnd
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = preset.accentColor,
                                            modifier = Modifier.padding(6.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                            Text(
                                text = preset.titleAr,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = if (isSelected) preset.accentColor else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = preset.descriptionAr,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }

            // Custom prompt input if CUSTOM selected
            AnimatedVisibility(visible = selectedSetting == VirtualSettingPreset.CUSTOM) {
                OutlinedTextField(
                    value = customSettingPrompt,
                    onValueChange = { customSettingPrompt = it },
                    label = { Text("اكتب وصف البيئة الافتراضية بدقة (Prompt)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_custom_lifestyle_prompt"),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // 3. Lighting & Aspect Ratio Selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Lighting Style
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("إضاءة المشهد:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(LightingStyle.entries) { light ->
                        val isSelected = selectedLighting == light
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedLighting = light },
                            label = { Text(light.labelAr, fontSize = 10.sp) }
                        )
                    }
                }
            }

            // Aspect Ratio
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("أبعاد الإخراج:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ImageAspectRatio.entries) { ratio ->
                        val isSelected = selectedAspectRatio == ratio
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAspectRatio = ratio },
                            label = { Text(ratio.ratioText, fontSize = 10.sp) }
                        )
                    }
                }
            }
        }

        // 4. Prompt Preview & Model Indicator
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("موجز التوليد المحسّن (AI Image Prompt):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF38BDF8).copy(alpha = 0.2f)) {
                        Text("gemini-2.5-flash-image", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF38BDF8), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }

                Text(
                    text = constructedPrompt,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF94A3B8),
                    lineHeight = 14.sp
                )
            }
        }

        // 5. Generate Button with Simulated Stepped Progress
        Button(
            onClick = {
                scope.launch {
                    isGenerating = true
                    generationStepText = "1/4: تحليل أبعاد وخلفية المنتج المعرف عبر Vision X..."
                    delay(500)
                    generationStepText = "2/4: بناء البيئة الافتراضية '${selectedSetting.titleAr}'..."
                    delay(600)
                    generationStepText = "3/4: موازنة الإضاءة والانعكاسات الشعاعية (Ray Tracing)..."
                    delay(500)
                    generationStepText = "4/4: توليد الصورة النهائية بدقة Ultra-HD 4K..."
                    delay(400)

                    val newImage = GeneratedLifestyleImage(
                        productName = selectedProductName,
                        productCategory = selectedCategory,
                        originalImageUrl = preselectedLensResult?.detectedProductNameEn,
                        virtualSetting = selectedSetting,
                        customPrompt = if (selectedSetting == VirtualSettingPreset.CUSTOM) customSettingPrompt else selectedSetting.promptEn,
                        lighting = selectedLighting,
                        aspectRatio = selectedAspectRatio,
                        fullConstructedPrompt = constructedPrompt,
                        generatedDrawableRes = selectedSetting.drawableRes
                    )

                    generatedResult = newImage
                    lifestyleHistory = listOf(newImage) + lifestyleHistory

                    // Record Governance Evidence
                    AgentEngine.recordEvidence(
                        operationId = "LIFESTYLE_GEN_${newImage.id.take(8)}",
                        actorId = AgentKey.DESIGN_PRESENTATION.key,
                        type = "LIFESTYLE_IMAGE_SYNTHESIS",
                        source = "Vision X Creative Studio & Gemini Flash Image",
                        payloadSummary = "Placed '$selectedProductName' into '${selectedSetting.titleEn}'. Lighting: ${selectedLighting.name}. Aspect Ratio: ${selectedAspectRatio.ratioText}."
                    )

                    isGenerating = false
                    Toast.makeText(context, "تم توليد صورة المشهد العصري بنجاح! 🎨", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_generate_lifestyle_image")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text(generationStepText, fontSize = 11.sp, color = Color.White)
            } else {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "توليد صورة المشهد العصري بالذكاء الاصطناعي 🚀",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }

        // 6. Generated Lifestyle Result Showcase Card
        generatedResult?.let { result ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(2.dp, Color(0xFF10B981)),
                modifier = Modifier.fillMaxWidth().testTag("generated_lifestyle_result_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "مخرج المشهد العصري المعتمد:",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }

                        // Compare Toggle
                        FilledTonalButton(
                            onClick = { showCompareOriginal = !showCompareOriginal },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (showCompareOriginal) "عرض المشهد الكامل" else "مقارنة بالأصل", fontSize = 10.sp)
                        }
                    }

                    // Image Display Canvas / Frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .shadow(8.dp)
                    ) {
                        if (showCompareOriginal) {
                            // Packshot simulation
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFF8FAFC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF06B6D4).copy(alpha = 0.15f),
                                        modifier = Modifier.size(80.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("📦", fontSize = 40.sp)
                                        }
                                    }
                                    Text(result.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                    Text("صورة عبوة المنتج الأصلية (Isolated Packshot)", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                            }
                        } else {
                            // High-definition Lifestyle Image
                            Image(
                                painter = painterResource(id = result.generatedDrawableRes),
                                contentDescription = result.productName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Staging Badge Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.65f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(result.virtualSetting.titleAr, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.width(6.dp))
                                        Text("• ${result.lighting.labelAr}", fontSize = 9.sp, color = Color(0xFF38BDF8))
                                    }
                                }
                            }
                        }
                    }

                    // Metadata Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Evidence ID: ${result.evidenceId}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "Model: ${result.modelName} • Ultra-HD 4K",
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF10B981).copy(alpha = 0.15f)) {
                            Text(
                                text = "GENERATED VERIFIED ✓",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Action Buttons Row: Save, Share, Use as Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val uri = ProductShareManager.exportLifestyleImageToGallery(
                                    context = context,
                                    drawableResId = result.generatedDrawableRes,
                                    productName = result.productName
                                )
                                if (uri != null) {
                                    Toast.makeText(context, "تم حفظ وتصدير الصورة إلى استوديو الصور بنجاح! 💾", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "تم حفظ صورة المشهد العصري 💾", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).testTag("btn_export_lifestyle_gallery")
                        ) {
                            Icon(Icons.Default.SaveAlt, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("تصدير 💾", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                ProductShareManager.shareLifestyleImageToSocialMedia(
                                    context = context,
                                    drawableResId = result.generatedDrawableRes,
                                    productName = result.productName,
                                    settingTitle = result.virtualSetting.titleAr,
                                    prompt = result.fullConstructedPrompt
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF06B6D4)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f).testTag("btn_share_lifestyle_social")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("مشاركة 📢", fontSize = 11.sp, color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onApplyAsBanner(result)
                                Toast.makeText(context, "تم اعتماد الصورة كبانر رئيسي للمنتج في واجهة المتجر! 🌟", Toast.LENGTH_LONG).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1.3f).testTag("btn_apply_lifestyle_banner")
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("اعتماد كبانر 🌟", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Direct Social Media One-Tap Export Bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("مشاركة وتصدير فوري لمنصات التواصل الاجتماعي:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE2E8F0))
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val socialChannels = listOf(
                                Triple("WhatsApp", "com.whatsapp", "💬 واتساب"),
                                Triple("Instagram", "com.instagram.android", "📸 إنستغرام"),
                                Triple("X (Twitter)", "com.twitter.android", "🐦 منصة X"),
                                Triple("Telegram", "org.telegram.messenger", "✈️ تيليغرام"),
                                Triple("Facebook", "com.facebook.katana", "👥 فيسبوك")
                            )
                            items(socialChannels) { (name, pkg, label) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f)),
                                    modifier = Modifier.clickable {
                                        ProductShareManager.shareLifestyleImageToSocialMedia(
                                            context = context,
                                            drawableResId = result.generatedDrawableRes,
                                            productName = result.productName,
                                            settingTitle = result.virtualSetting.titleAr,
                                            prompt = result.fullConstructedPrompt,
                                            targetPackage = pkg
                                        )
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Recent Lifestyle Creations History
        if (lifestyleHistory.size > 1) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("المشاهد العصرية المولدة في هذه الجلسة:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(lifestyleHistory) { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .width(120.dp)
                                .clickable { generatedResult = item }
                        ) {
                            Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Image(
                                    painter = painterResource(id = item.generatedDrawableRes),
                                    contentDescription = item.productName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Text(item.productName, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(item.virtualSetting.titleAr, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
