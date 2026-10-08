package com.example.data.visionx

import java.util.UUID

data class RetailProductLink(
    val retailerName: String,
    val productUrl: String,
    val apiSource: String,
    val initialEstimatedPrice: Double,
    val isPrimary: Boolean = false
)

/**
 * Vision X Universal Shopping Lens Models
 */
data class ShoppingLensResult(
    val lensId: String = "lens_" + UUID.randomUUID().toString().take(8),
    val detectedProductNameAr: String,
    val detectedProductNameEn: String,
    val category: String,
    val confidenceScore: Double, // e.g. 0.96 (96%)
    val matchedProductIds: List<Int>,
    val estimatedMarketPrice: Double,
    val bestNearbyStore: String,
    val distanceKm: Double,
    val evidenceId: String,
    val analyzedAt: Long = System.currentTimeMillis(),
    val brand: String = "Amer Certified Organic",
    val barcodeOrSku: String? = null,
    val specifications: List<String> = emptyList(),
    val nutritionalHighlights: List<String> = emptyList(),
    val packagingType: String = "عبوة زجاجية محكمة الغلق (Glass Jar)",
    val freshnessOrQuality: String = "درجة أولى ممتازة (Grade A Premium)",
    val storageAdvice: String = "يحفظ في مكان بارد وجاف بعيداً عن الرطوبة وأشعة الشمس",
    val ingredients: List<String> = emptyList(),
    val rawGeminiAnalysis: String? = null,
    val productLinks: List<RetailProductLink> = emptyList()
)

/**
 * Price Radar + Watchlist Models
 */
data class PriceQuote(
    val providerName: String,
    val price: Double,
    val wholesalePrice: Double? = null,
    val distanceKm: Double = 1.5,
    val isRegisteredMerchant: Boolean = true,
    val stockAvailable: Boolean = true,
    val isBestDeal: Boolean = false,
    val source: String = "Physical Store Scrape & API",
    val observedAt: Long = System.currentTimeMillis(),
    val productUrl: String? = null,
    val deliveryEta: String = "توصيل خلال ساعتين",
    val stockStatus: String = "متوفر في المخزون (In Stock)",
    val rating: Double = 4.8
)

data class MonitoredPriceComparison(
    val monitorId: String = "mon_" + UUID.randomUUID().toString().take(8),
    val productName: String,
    val originalLensId: String,
    val primaryProductUrl: String,
    val lowestPriceFound: Double,
    val lowestPriceRetailer: String,
    val averageMarketPrice: Double,
    val totalSavings: Double,
    val quotes: List<PriceQuote>,
    val isAlertActive: Boolean = true,
    val targetThresholdPrice: Double? = null,
    val lastMonitoredAt: Long = System.currentTimeMillis(),
    val evidenceId: String
)

data class PriceRadarItem(
    val productId: Int,
    val productNameAr: String,
    val productNameEn: String,
    val currentRetailPrice: Double,
    val marketAveragePrice: Double,
    val quotes: List<PriceQuote>,
    val priceChange24h: Double = -0.08, // -8%
    val evidenceId: String,
    val isWatched: Boolean = false,
    val targetAlertPrice: Double? = null
)

/**
 * Smart Basket Optimizer Models
 */
enum class OptimizationObjective(val labelAr: String, val labelEn: String) {
    CHEAPEST("الأرخص سعراً (أقصى توفير)", "Cheapest Total Cost"),
    FASTEST("الأسرع توصيلاً (أقل وقت)", "Fastest Delivery ETA"),
    FEWEST_DELIVERIES("أقل عدد شحنات (شحنة واحدة)", "Fewest Deliveries"),
    BALANCED("متوازن (سعر + جودة + سرعة)", "Balanced Optimal")
}

data class OptimizedStoreSplit(
    val storeName: String,
    val itemNames: List<String>,
    val subtotal: Double,
    val deliveryFee: Double,
    val etaMinutes: Int
)

data class BasketOptimizationResult(
    val optimizationId: String = "opt_" + UUID.randomUUID().toString().take(8),
    val objective: OptimizationObjective,
    val originalTotal: Double,
    val optimizedTotal: Double,
    val totalSavings: Double,
    val storeSplits: List<OptimizedStoreSplit>,
    val strategyExplanationAr: String,
    val strategyExplanationEn: String,
    val evidenceId: String,
    val isApplied: Boolean = false
)

/**
 * AI Shopping Mission Models
 */
data class MissionItem(
    val productId: Int,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val category: String,
    val storeName: String
)

data class MultiAgentInsight(
    val agentKey: String,
    val agentNameAr: String,
    val insightTextAr: String,
    val isPassed: Boolean = true
)

data class ShoppingMission(
    val missionId: String = "msn_" + UUID.randomUUID().toString().take(8),
    val userGoal: String,
    val targetBudget: Double,
    val calculatedTotal: Double,
    val draftItems: List<MissionItem>,
    val agentInsights: List<MultiAgentInsight>,
    val status: String = "DRAFT", // DRAFT, USER_APPROVED, ORDER_CREATED
    val evidenceId: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Receipt Intelligence Models
 */
data class ReceiptLineItem(
    val itemName: String,
    val quantity: Int,
    val billedPrice: Double,
    val expectedCatalogPrice: Double,
    val hasDiscrepancy: Boolean = false
)

data class ReceiptScanResult(
    val receiptId: String = "rec_" + UUID.randomUUID().toString().take(8),
    val merchantName: String,
    val purchaseTimestamp: String,
    val lineItems: List<ReceiptLineItem>,
    val billedTotal: Double,
    val expectedTotal: Double,
    val discrepancyAmount: Double,
    val disputeDraftReady: Boolean,
    val disputeRecommendationAr: String,
    val evidenceId: String
)

/**
 * Order Digital Twin Models
 */
enum class OrderDigitalStage(val titleAr: String, val titleEn: String, val icon: String) {
    CREATED("تم إنشاء الطلب", "Order Created", "receipt_long"),
    CONFIRMED("تم تأكيد الدفع والطلب", "Confirmed & Paid", "verified"),
    PREPARING("جاري التجهيز والتعبئة", "Preparing Order", "inventory_2"),
    READY("جاهز للاستلام بالمستودع", "Ready for Pickup", "storefront"),
    PICKED_UP("استلمه مندوب التوصيل", "Picked Up by Courier", "local_shipping"),
    IN_TRANSIT("في الطريق إلى موقعك", "In Transit", "directions_bike"),
    DELIVERED("تم التسليم بنجاح", "Delivered", "task_alt")
}

data class OrderTimelineEvent(
    val stage: OrderDigitalStage,
    val timestamp: Long,
    val location: String,
    val noteAr: String,
    val evidenceId: String
)

data class OrderDigitalTwin(
    val orderId: Int,
    val customerName: String,
    val productName: String,
    val quantity: Int,
    val totalPrice: Double,
    val merchantName: String,
    val driverName: String,
    val currentStage: OrderDigitalStage,
    val etaMinutes: Int,
    val events: List<OrderTimelineEvent>
)

/**
 * AI Creative Studio Models
 */
enum class CreativeType(val titleAr: String, val titleEn: String) {
    MARKETING_IMAGE("صورة ترويجية مخصصة", "Marketing Visual"),
    AD_COPY_AR("نص إعلاني تسويقي (عربي)", "Arabic Ad Copy"),
    AD_COPY_EN("نص إعلاني تسويقي (إنجليزي)", "English Ad Copy"),
    AUDIO_JINGLE("نغمة / جينغل صوتي ترويجي", "Audio Jingle Concept"),
    VIDEO_STORYBOARD("سيناريو فيديو ترويجي (Veo)", "Video Storyboard Script")
}

data class CreativeJob(
    val jobId: String = "job_" + UUID.randomUUID().toString().take(8),
    val type: CreativeType,
    val promptBrief: String,
    val generatedArtifact: String,
    val generatedMetadata: Map<String, String>,
    val status: String = "COMPLETED",
    val evidenceId: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * AI Truth Panel Model
 */
data class AiTruthReport(
    val operationName: String,
    val aiProvider: String = "Google DeepMind / Gemini 3.5 & Agent Engine",
    val activeModel: String,
    val executionTimeMs: Long,
    val isGroundedWithRealCatalog: Boolean = true,
    val groundedSources: List<String>,
    val immutableEvidenceId: String,
    val verifiedTimestamp: Long = System.currentTimeMillis()
)
