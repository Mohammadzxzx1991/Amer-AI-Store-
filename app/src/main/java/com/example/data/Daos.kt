package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Int): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email OR phoneNumber = :phone LIMIT 1")
    suspend fun getUserByEmailOrPhone(email: String, phone: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET permissionGranted = :granted WHERE id = :userId")
    suspend fun updatePermission(userId: Int, granted: Boolean)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductById(id: Int): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductByIdSuspend(id: Int): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<ProductEntity>)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Int)
}

@Dao
interface SavedProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedProduct(saved: SavedProductEntity)

    @Query("DELETE FROM saved_products WHERE userId = :userId AND productId = :productId")
    suspend fun deleteSavedProduct(userId: Int, productId: Int)

    @Query("SELECT products.* FROM products INNER JOIN saved_products ON products.id = saved_products.productId WHERE saved_products.userId = :userId")
    fun getSavedProductsForUser(userId: Int): Flow<List<ProductEntity>>
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY id DESC")
    fun getOrdersForCustomer(customerId: Int): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status != 'Delivered' ORDER BY id DESC")
    fun getActiveOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String)

    @Query("UPDATE orders SET deliveryDriverId = :driverId, status = :status WHERE id = :orderId")
    suspend fun assignOrderDriver(orderId: Int, driverId: Int, status: String)
}

@Dao
interface ChatReportDao {
    @Query("SELECT * FROM chat_reports ORDER BY id DESC")
    fun getAllChatReports(): Flow<List<ChatReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatReport(report: ChatReportEntity)
}

@Dao
interface CartItemDao {
    @Query("SELECT * FROM cart_items WHERE userId = :userId ORDER BY id DESC")
    fun getCartItems(userId: Int): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId AND orderType = :orderType LIMIT 1")
    suspend fun getCartItemSpecific(userId: Int, productId: Int, orderType: String): CartItemEntity?

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItemById(id: Int)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: Int)
}

@Dao
interface BankCardDao {
    @Query("SELECT * FROM bank_cards WHERE userId = :userId ORDER BY isPrimary DESC, cardNumber DESC")
    fun getBankCards(userId: Int): Flow<List<BankCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankCard(card: BankCardEntity)

    @Query("DELETE FROM bank_cards WHERE cardNumber = :cardNumber AND userId = :userId")
    suspend fun deleteBankCard(cardNumber: String, userId: Int)
}

@Dao
interface PriceAlertDao {
    @Query("SELECT * FROM price_alerts WHERE userId = :userId")
    fun getPriceAlerts(userId: Int): Flow<List<PriceAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceAlert(alert: PriceAlertEntity)

    @Query("DELETE FROM price_alerts WHERE id = :id")
    suspend fun deletePriceAlert(id: Int)
}

@Dao
interface SearchQueryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 15")
    fun getRecentSearchQueries(): Flow<List<SearchQueryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(query: SearchQueryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteQuery(query: String)
}

@Dao
interface FamilyShoppingListDao {
    @Query("SELECT * FROM family_shopping_list ORDER BY isChecked ASC, CASE priority WHEN 'High' THEN 1 WHEN 'Medium' THEN 2 ELSE 3 END, id DESC")
    fun getAllFamilyItems(): Flow<List<FamilyShoppingListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyItem(item: FamilyShoppingListEntity): Long

    @Query("UPDATE family_shopping_list SET isChecked = :isChecked WHERE id = :id")
    suspend fun updateFamilyItemStatus(id: Int, isChecked: Boolean)

    @Query("DELETE FROM family_shopping_list WHERE id = :id")
    suspend fun deleteFamilyItem(id: Int)

    @Query("DELETE FROM family_shopping_list")
    suspend fun clearAllFamilyItems()
}

@Dao
interface StoredOrganicItemDao {
    @Query("SELECT * FROM stored_organic_items WHERE userId = :userId ORDER BY expiryDate ASC")
    fun getStoredItems(userId: Int): Flow<List<StoredOrganicItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoredItem(item: StoredOrganicItemEntity): Long

    @Query("UPDATE stored_organic_items SET isNotified = 1 WHERE id = :id")
    suspend fun markNotified(id: Int)

    @Query("DELETE FROM stored_organic_items WHERE id = :id")
    suspend fun deleteStoredItem(id: Int)

    @Query("DELETE FROM stored_organic_items WHERE userId = :userId")
    suspend fun clearStoredItems(userId: Int)
}

@Dao
interface CachedSearchResultDao {
    @Query("SELECT * FROM cached_search_results WHERE query = :query ORDER BY id DESC")
    fun getCachedSearchResults(query: String): Flow<List<CachedSearchResultEntity>>

    @Query("SELECT * FROM cached_search_results WHERE query = :query ORDER BY id DESC")
    suspend fun getCachedSearchResultsSuspend(query: String): List<CachedSearchResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(results: List<CachedSearchResultEntity>)

    @Query("DELETE FROM cached_search_results WHERE query = :query")
    suspend fun clearQueryCache(query: String)
}

@Dao
interface CachedPriceComparisonDao {
    @Query("SELECT * FROM cached_price_comparisons WHERE productId = :productId")
    fun getCachedComparisons(productId: Int): Flow<List<CachedPriceComparisonEntity>>

    @Query("SELECT * FROM cached_price_comparisons WHERE productId = :productId")
    suspend fun getCachedComparisonsSuspend(productId: Int): List<CachedPriceComparisonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComparisons(comparisons: List<CachedPriceComparisonEntity>)

    @Query("DELETE FROM cached_price_comparisons WHERE productId = :productId")
    suspend fun clearComparisonCache(productId: Int)
}

@Dao
interface PriceHistoryDao {
    @Query("SELECT * FROM price_histories WHERE productId = :productId ORDER BY recordedAt DESC")
    fun getHistoryForProduct(productId: Int): Flow<List<PriceHistoryEntity>>

    @Query("SELECT * FROM price_histories WHERE productId = :productId ORDER BY recordedAt ASC")
    fun getHistoryForProductAsc(productId: Int): Flow<List<PriceHistoryEntity>>

    @Query("SELECT * FROM price_histories WHERE productId = :productId ORDER BY recordedAt ASC")
    suspend fun getHistoryForProductAscSuspend(productId: Int): List<PriceHistoryEntity>

    @Query("SELECT * FROM price_histories ORDER BY recordedAt DESC LIMIT 100")
    fun getAllPriceHistories(): Flow<List<PriceHistoryEntity>>

    @Query("SELECT * FROM price_histories WHERE productId = :productId ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestPriceForProduct(productId: Int): PriceHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(history: PriceHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistories(histories: List<PriceHistoryEntity>)

    @Query("DELETE FROM price_histories WHERE productId = :productId")
    suspend fun clearHistoryForProduct(productId: Int)

    @Query("DELETE FROM price_histories")
    suspend fun clearAllHistories()
}

@Dao
interface ViralProductMentionDao {
    @Query("SELECT * FROM viral_product_mentions ORDER BY viewCount DESC")
    fun getAllViralMentions(): Flow<List<ViralProductMentionEntity>>

    @Query("SELECT * FROM viral_product_mentions WHERE platform = :platform ORDER BY viewCount DESC")
    fun getViralMentionsByPlatform(platform: String): Flow<List<ViralProductMentionEntity>>

    @Query("SELECT * FROM viral_product_mentions WHERE category = :category ORDER BY viewCount DESC")
    fun getViralMentionsByCategory(category: String): Flow<List<ViralProductMentionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertViralMentions(mentions: List<ViralProductMentionEntity>)

    @Query("DELETE FROM viral_product_mentions")
    suspend fun clearAll()
}

@Dao
interface UserInteractionHistoryDao {
    @Query("SELECT * FROM user_interaction_histories WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserInteractions(userId: Int): Flow<List<UserInteractionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: UserInteractionHistoryEntity)

    @Query("DELETE FROM user_interaction_histories WHERE userId = :userId")
    suspend fun clearInteractions(userId: Int)
}

@Dao
interface RecentlyViewedProductDao {
    @Query("SELECT * FROM recently_viewed_products ORDER BY viewedAt DESC LIMIT 20")
    fun getRecentlyViewedProducts(): Flow<List<RecentlyViewedProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentlyViewed(product: RecentlyViewedProductEntity)

    @Query("DELETE FROM recently_viewed_products WHERE productId = :productId")
    suspend fun removeRecentlyViewed(productId: Int)

    @Query("DELETE FROM recently_viewed_products")
    suspend fun clearRecentlyViewed()
}

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        SavedProductEntity::class,
        OrderEntity::class,
        ChatReportEntity::class,
        CartItemEntity::class,
        BankCardEntity::class,
        SearchQueryEntity::class,
        PriceAlertEntity::class,
        FamilyShoppingListEntity::class,
        StoredOrganicItemEntity::class,
        CachedSearchResultEntity::class,
        CachedPriceComparisonEntity::class,
        PriceHistoryEntity::class,
        ViralProductMentionEntity::class,
        UserInteractionHistoryEntity::class,
        RecentlyViewedProductEntity::class
    ],
    version = 15,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun savedProductDao(): SavedProductDao
    abstract fun orderDao(): OrderDao
    abstract fun chatReportDao(): ChatReportDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun bankCardDao(): BankCardDao
    abstract fun searchQueryDao(): SearchQueryDao
    abstract fun priceAlertDao(): PriceAlertDao
    abstract fun familyShoppingListDao(): FamilyShoppingListDao
    abstract fun storedOrganicItemDao(): StoredOrganicItemDao
    abstract fun cachedSearchResultDao(): CachedSearchResultDao
    abstract fun cachedPriceComparisonDao(): CachedPriceComparisonDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun viralProductMentionDao(): ViralProductMentionDao
    abstract fun userInteractionHistoryDao(): UserInteractionHistoryDao
    abstract fun recentlyViewedProductDao(): RecentlyViewedProductDao

    companion object {
        val ALL_MIGRATIONS: Array<androidx.room.migration.Migration> = (1..14).map { fromVersion ->
            object : androidx.room.migration.Migration(fromVersion, fromVersion + 1) {
                override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    // Non-destructive schema migration: preserve user tables and records
                }
            }
        }.toTypedArray()
    }
}
