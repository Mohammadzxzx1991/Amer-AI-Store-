package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val role: String, // "Customer", "Merchant", "Delivery", "Admin"
    val permissionGranted: Boolean = false,
    val loyaltyPoints: Int = 0,
    val badges: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val loginStreak: Int = 0,
    val lastLoginDate: String = ""
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val retailPrice: Double,
    val wholesalePrice: Double,
    val imageUrl: String,
    val description: String = "",
    val ingredients: String = "",
    val stockQuantity: Int = 50,
    val isRegisteredMerchant: Boolean = true, // Registered merchant vs AI Social Media Scraped Virtual Merchant
    val merchantName: String = "AI Smart Marketplace",
    val salesHistory: Int = 0,
    val aisle: String = "General",
    val bestPrice: Double = 0.0
)

@Entity(tableName = "saved_products")
data class SavedProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val productName: String,
    val targetPrice: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val customerName: String,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val totalPrice: Double,
    val orderType: String, // "Retail" or "Wholesale"
    val merchantName: String,
    val status: String = "Pending", // "Pending", "Preparing", "Out For Delivery", "Delivered"
    val deliveryDriverId: Int = 0,
    val routeAddress: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_reports")
data class ChatReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val customerMessage: String,
    val merchantMessage: String,
    val disputeReason: String,
    val resolvedTip: String = ""
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val productName: String,
    val imageUrl: String,
    val price: Double,
    var quantity: Int,
    val orderType: String // "Retail" or "Wholesale"
)

@Entity(tableName = "bank_cards")
data class BankCardEntity(
    @PrimaryKey val cardNumber: String,
    val userId: Int,
    val cardHolder: String,
    val expiryDate: String,
    val cvv: String,
    val isPrimary: Boolean = false
)

@Entity(tableName = "search_history")
data class SearchQueryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "family_shopping_list")
data class FamilyShoppingListEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val addedBy: String,
    val quantity: Int = 1,
    val category: String = "Organic",
    val priority: String = "Medium", // "High", "Medium", "Low"
    val isChecked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stored_organic_items")
data class StoredOrganicItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val productName: String,
    val purchaseDate: Long = System.currentTimeMillis(),
    val expiryDate: Long, // timestamp
    val quantity: Int = 1,
    val isNotified: Boolean = false
)

@Entity(tableName = "cached_search_results")
data class CachedSearchResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val query: String,
    val productId: Int,
    val productName: String,
    val category: String,
    val price: Double,
    val imageUrl: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_price_comparisons")
data class CachedPriceComparisonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val retailerName: String,
    val price: Double,
    val shippingFee: Double = 0.0,
    val isBestDeal: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "price_histories")
data class PriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val productName: String,
    val merchantName: String,
    val retailPrice: Double,
    val wholesalePrice: Double? = null,
    val source: String = "Marketplace API",
    val isBestDeal: Boolean = false,
    val isRegisteredMerchant: Boolean = true,
    val recordedAt: Long = System.currentTimeMillis(),
    val evidenceId: String = ""
)

@Entity(tableName = "viral_product_mentions")
data class ViralProductMentionEntity(
    @PrimaryKey val id: String,
    val productId: Int,
    val productNameAr: String,
    val productNameEn: String,
    val category: String,
    val retailPrice: Double,
    val originalPrice: Double,
    val imageUrl: String,
    val platform: String,
    val creatorHandle: String,
    val creatorName: String,
    val postCaption: String,
    val hashtags: String,
    val videoUrl: String,
    val postUrl: String,
    val viewCount: Long,
    val likeCount: Long,
    val shareCount: Long,
    val viralVelocity: Double,
    val sentimentScore: Double,
    val viralReasonAr: String,
    val discoveredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_interaction_histories")
data class UserInteractionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 1,
    val productId: Int,
    val productName: String,
    val category: String,
    val interactionType: String, // "VIEW", "CLICK", "ADD_TO_CART", "PURCHASE", "WISHLIST"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "recently_viewed_products")
data class RecentlyViewedProductEntity(
    @PrimaryKey val productId: Int,
    val productName: String,
    val category: String,
    val retailPrice: Double,
    val imageUrl: String,
    val viewedAt: Long = System.currentTimeMillis()
)



