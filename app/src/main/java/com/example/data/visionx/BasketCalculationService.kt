package com.example.data.visionx

import com.example.data.CartItemEntity
import com.example.data.ProductEntity
import java.util.UUID

/**
 * Data models for Vision Captured Items, Bundle Discounts, and Budget-Friendly Alternatives
 */
data class VisionCapturedItem(
    val id: String = "vis_" + UUID.randomUUID().toString().take(8),
    val productNameAr: String,
    val productNameEn: String,
    val category: String,
    val confidenceScore: Double = 0.96,
    val estimatedPrice: Double,
    val imageUrl: String = "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=500",
    val barcodeOrSku: String? = null,
    val merchantName: String = "سوق عامر للذكاء الاصطناعي",
    val capturedAt: Long = System.currentTimeMillis(),
    val isInBasket: Boolean = false
)

data class BundleDiscountSuggestion(
    val bundleId: String = "bnd_" + UUID.randomUUID().toString().take(8),
    val bundleNameAr: String,
    val bundleNameEn: String,
    val itemsIncluded: List<String>,
    val matchedCartItemIds: List<Int>,
    val originalSubtotal: Double,
    val bundleDiscountPercent: Int, // e.g. 15, 20, 25
    val discountAmount: Double,
    val discountedBundlePrice: Double,
    val savingsExplanationAr: String,
    val badgeText: String = "وفر $bundleDiscountPercent%",
    val isApplied: Boolean = false
)

data class BudgetFriendlyAlternative(
    val alternativeId: String = "alt_" + UUID.randomUUID().toString().take(8),
    val targetCartItemId: Int,
    val originalItemName: String,
    val originalPrice: Double,
    val alternativeItemName: String,
    val alternativePrice: Double,
    val alternativeCategory: String,
    val alternativeMerchant: String = "تعاونية المزارعين وسوق الجملة",
    val alternativeImageUrl: String = "",
    val savingsPerUnit: Double = (originalPrice - alternativePrice).coerceAtLeast(0.0),
    val savingsPercent: Int = if (originalPrice > 0) (((originalPrice - alternativePrice) / originalPrice) * 100).toInt() else 0,
    val qualityMatchScore: Int = 96,
    val matchReasonAr: String = "نفس الجودة العضوية من المزارع مباشرة بسعر الجملة",
    val isSwapped: Boolean = false
)

data class CalculationSummary(
    val originalTotal: Double,
    val bundleSavings: Double,
    val alternativeSavings: Double,
    val shippingSavings: Double,
    val totalSavings: Double,
    val finalOptimizedTotal: Double,
    val effectiveDiscountPercent: Int
)

/**
 * Basket Calculation Service
 * Algorithmic calculation service for generating bundle discounts,
 * budget-friendly alternatives, and real-time basket optimization totals.
 */
object BasketCalculationService {

    /**
     * Identifies eligible product bundles in the basket and calculates bundle discounts.
     */
    fun calculateBundleDiscounts(items: List<CartItemEntity>): List<BundleDiscountSuggestion> {
        if (items.isEmpty()) return emptyList()

        val suggestions = mutableListOf<BundleDiscountSuggestion>()
        val itemNames = items.map { it.productName.lowercase() }

        // 1. Breakfast Bundle (Honey + Milk / Dairy / Bread)
        val hasHoney = items.any { it.productName.contains("عسل") || it.productName.contains("honey", ignoreCase = true) }
        val hasDairyOrBakery = items.any {
            it.productName.contains("حليب") || it.productName.contains("لبن") || it.productName.contains("بيض") ||
            it.productName.contains("جبن") || it.productName.contains("خبز") || it.productName.contains("milk", ignoreCase = true)
        }

        if (hasHoney && hasDairyOrBakery) {
            val matched = items.filter {
                it.productName.contains("عسل") || it.productName.contains("حليب") ||
                it.productName.contains("بيض") || it.productName.contains("جبن") ||
                it.productName.contains("honey", ignoreCase = true) || it.productName.contains("milk", ignoreCase = true)
            }
            val subtotal = matched.sumOf { it.price * it.quantity }
            val discountPct = 20
            val discountAmt = Math.round(subtotal * (discountPct / 100.0) * 100.0) / 100.0
            suggestions.add(
                BundleDiscountSuggestion(
                    bundleId = "bnd_breakfast",
                    bundleNameAr = "حزمة الإفطار العضوي الملكي 🍯",
                    bundleNameEn = "Royal Organic Breakfast Bundle",
                    itemsIncluded = matched.map { it.productName },
                    matchedCartItemIds = matched.map { it.id },
                    originalSubtotal = subtotal,
                    bundleDiscountPercent = discountPct,
                    discountAmount = discountAmt,
                    discountedBundlePrice = subtotal - discountAmt,
                    savingsExplanationAr = "خصم حصري 20% عند شراء العسل مع منتجات الألبان والمخبوزات العضوية معاً.",
                    badgeText = "توفير الحزمة 20% 🎁"
                )
            )
        }

        // 2. Cooking & Pantry Bundle (Olive Oil + Spices / Vinegar / Herbs)
        val hasOil = items.any { it.productName.contains("زيت") || it.productName.contains("oil", ignoreCase = true) }
        val hasPantry = items.any {
            it.productName.contains("بهارات") || it.productName.contains("زعتر") || it.productName.contains("ملح") ||
            it.productName.contains("خل") || it.productName.contains("قهوة")
        }

        if (hasOil && (hasPantry || items.size >= 3)) {
            val matched = items.filter {
                it.productName.contains("زيت") || it.productName.contains("بهارات") ||
                it.productName.contains("زعتر") || it.productName.contains("ملح") ||
                it.productName.contains("oil", ignoreCase = true)
            }.ifEmpty { items.take(2) }

            val subtotal = matched.sumOf { it.price * it.quantity }
            val discountPct = 15
            val discountAmt = Math.round(subtotal * (discountPct / 100.0) * 100.0) / 100.0
            suggestions.add(
                BundleDiscountSuggestion(
                    bundleId = "bnd_cooking",
                    bundleNameAr = "حزمة مؤونة المطبخ والطهي الصحي 🫒",
                    bundleNameEn = "Healthy Kitchen Pantry Bundle",
                    itemsIncluded = matched.map { it.productName },
                    matchedCartItemIds = matched.map { it.id },
                    originalSubtotal = subtotal,
                    bundleDiscountPercent = discountPct,
                    discountAmount = discountAmt,
                    discountedBundlePrice = subtotal - discountAmt,
                    savingsExplanationAr = "خصم 15% على مستلزمات المائدة والزيوت عند جمعها في شحنة واحدة.",
                    badgeText = "توفير المونة 15% 🫒"
                )
            )
        }

        // 3. Multi-Item Bulk Savings (3+ items)
        if (items.size >= 3 && suggestions.size < 2) {
            val allSubtotal = items.sumOf { it.price * it.quantity }
            val discountPct = 12
            val discountAmt = Math.round(allSubtotal * (discountPct / 100.0) * 100.0) / 100.0
            suggestions.add(
                BundleDiscountSuggestion(
                    bundleId = "bnd_family_pack",
                    bundleNameAr = "حزمة العائلة للتسوق الذكي 🧺",
                    bundleNameEn = "Smart Family Grocery Pack",
                    itemsIncluded = items.map { it.productName },
                    matchedCartItemIds = items.map { it.id },
                    originalSubtotal = allSubtotal,
                    bundleDiscountPercent = discountPct,
                    discountAmount = discountAmt,
                    discountedBundlePrice = allSubtotal - discountAmt,
                    savingsExplanationAr = "توفير 12% إضافي لتجميع السلة بالكامل في طلبية موحدة.",
                    badgeText = "خصم السلة 12% ⚡"
                )
            )
        }

        return suggestions
    }

    /**
     * Calculates budget-friendly alternatives for each item in the basket,
     * prioritizing items captured from the vision tool or high-cost retail items.
     */
    fun calculateBudgetAlternatives(
        items: List<CartItemEntity>,
        catalog: List<ProductEntity>
    ): List<BudgetFriendlyAlternative> {
        if (items.isEmpty()) return emptyList()

        val alternatives = mutableListOf<BudgetFriendlyAlternative>()

        items.forEach { cartItem ->
            val name = cartItem.productName
            val price = cartItem.price

            // Determine suitable alternative based on product type
            val (altName, altMultiplier, matchReason) = when {
                name.contains("عسل") || name.contains("honey", ignoreCase = true) -> {
                    Triple("عسل زهور برية نقي - عبوة التوفير (تعاونية المزارعين)", 0.68, "عسل طبيعي نقي 100% معتمد بسعر تعاوني مباشر بدون تكاليف التغليف الفاخر")
                }
                name.contains("زيت") || name.contains("oil", ignoreCase = true) -> {
                    Triple("زيت زيتون بلدي عصرة أولى على البارد - عبوة اقتصادية 1 لتر", 0.72, "معصور على البارد من مزارع الجوف مباشرة مع توفير 28% بالحجم الاقتصادي")
                }
                name.contains("قهوة") || name.contains("coffee", ignoreCase = true) -> {
                    Triple("بن هرري إثيوبي محمص طازج - تعبئة الجملة المباشرة", 0.75, "حبوب بن درجة أولى ممتازة من المستورد مباشرة بسعر أقل من العلامات التجارية")
                }
                name.contains("حليب") || name.contains("milk", ignoreCase = true) -> {
                    Triple("حليب أبقار طازج معقم - كرتون التوفير الأسبوعي (4 لتر)", 0.80, "وفر 20% عند شراء كرتون الأسبوع دفعة واحدة بنفس الطزاجة اليومية")
                }
                name.contains("بيض") || name.contains("egg", ignoreCase = true) -> {
                    Triple("طبق بيض بلدي حر المزرعة (30 بيضة) - سعر المزرعة", 0.76, "طازج يومياً من مزارع الدواجن الحرة مباشرة إلى باب منزلك")
                }
                else -> {
                    Triple("بديل الجملة المعتمد: $name (سعر التكلفة المباشر)", 0.78, "نفس مواصفات الجودة العضوية من موزع الجملة الرئيسي المعتمد")
                }
            }

            val altPrice = Math.round(price * altMultiplier * 100.0) / 100.0
            val savingsUnit = (price - altPrice).coerceAtLeast(0.0)
            val savingsPct = if (price > 0) ((savingsUnit / price) * 100).toInt() else 0

            alternatives.add(
                BudgetFriendlyAlternative(
                    alternativeId = "alt_${cartItem.id}_" + cartItem.productId,
                    targetCartItemId = cartItem.id,
                    originalItemName = name,
                    originalPrice = price,
                    alternativeItemName = altName,
                    alternativePrice = altPrice,
                    alternativeCategory = "بدائل التوفير والجملة",
                    alternativeMerchant = "تعاونية المزارعين وسوق الجملة المركزي",
                    alternativeImageUrl = cartItem.imageUrl,
                    savingsPerUnit = savingsUnit,
                    savingsPercent = savingsPct,
                    qualityMatchScore = 95 + (cartItem.id % 5),
                    matchReasonAr = matchReason
                )
            )
        }

        return alternatives
    }

    /**
     * Computes the complete calculation summary for the basket with applied discounts.
     */
    fun computeCalculationSummary(
        items: List<CartItemEntity>,
        appliedBundles: List<BundleDiscountSuggestion>,
        appliedAlternatives: List<BudgetFriendlyAlternative>,
        isFreeShippingQualified: Boolean
    ): CalculationSummary {
        val originalTotal = items.sumOf { it.price * it.quantity }
        val bundleSavings = appliedBundles.filter { it.isApplied }.sumOf { it.discountAmount }
        val alternativeSavings = appliedAlternatives.filter { it.isSwapped }.sumOf { it.savingsPerUnit }
        val shippingSavings = if (isFreeShippingQualified) 4.99 else 0.0

        val totalSavings = Math.round((bundleSavings + alternativeSavings + shippingSavings) * 100.0) / 100.0
        val finalTotal = (originalTotal - bundleSavings - alternativeSavings).coerceAtLeast(0.0)
        val finalWithShipping = if (isFreeShippingQualified) finalTotal else (finalTotal + 4.99)

        val effDiscount = if (originalTotal > 0) ((totalSavings / (originalTotal + 4.99)) * 100).toInt() else 0

        return CalculationSummary(
            originalTotal = Math.round(originalTotal * 100.0) / 100.0,
            bundleSavings = Math.round(bundleSavings * 100.0) / 100.0,
            alternativeSavings = Math.round(alternativeSavings * 100.0) / 100.0,
            shippingSavings = shippingSavings,
            totalSavings = totalSavings,
            finalOptimizedTotal = Math.round(finalWithShipping * 100.0) / 100.0,
            effectiveDiscountPercent = effDiscount
        )
    }
}
