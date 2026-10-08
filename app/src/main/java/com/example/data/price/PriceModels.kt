package com.example.data.price

import java.util.UUID

/**
 * DataModel layer for handling real-time market rates and price comparison data.
 */

data class MarketRate(
    val id: String = UUID.randomUUID().toString().take(8),
    val retailerName: String,
    val retailerNameAr: String,
    val retailerLogo: String = "🏪",
    val price: Double,
    val originalPrice: Double? = null,
    val discountPercentage: Int = 0,
    val shippingFee: Double = 0.0,
    val deliveryTimeEstimate: String = "1-2 Business Days",
    val deliveryTimeEstimateAr: String = "توصيل خلال يوم إلى يومين",
    val rating: Double = 4.8,
    val reviewCount: Int = 120,
    val isBestDeal: Boolean = false,
    val isLowestPrice: Boolean = false,
    val isFastestDelivery: Boolean = false,
    val stockStatus: String = "In Stock",
    val stockStatusAr: String = "متوفر في المخزون",
    val merchantTier: String = "Verified Merchant",
    val merchantTierAr: String = "تاجر موثق ومعتمد",
    val productUrl: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

data class DetectedItemPriceData(
    val detectedItemId: String = "item_" + UUID.randomUUID().toString().take(8),
    val detectedItemNameAr: String,
    val detectedItemNameEn: String,
    val category: String,
    val brand: String = "Organic Certified",
    val sku: String? = null,
    val basePrice: Double,
    val lowestPrice: Double,
    val averageMarketPrice: Double,
    val highestPrice: Double,
    val potentialSavings: Double,
    val savingsPercentage: Int,
    val bestRetailerName: String,
    val rates: List<MarketRate>,
    val priceSpread: Double = highestPrice - lowestPrice,
    val fetchedAt: Long = System.currentTimeMillis()
)
