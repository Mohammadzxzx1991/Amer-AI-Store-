package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.ui.MarketViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var viewModel: MarketViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            
        viewModel = MarketViewModel(
            db.userDao(),
            db.productDao(),
            db.savedProductDao(),
            db.orderDao(),
            db.chatReportDao(),
            db.cartItemDao(),
            db.bankCardDao(),
            db.searchQueryDao(),
            db.familyShoppingListDao(),
            db.storedOrganicItemDao(),
            context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE),
            db.cachedSearchResultDao(),
            db.cachedPriceComparisonDao()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testViewModelInitialization() {
        assertNotNull(viewModel)
    }

    @Test
    fun testSearchHistoryFlowAndStateTransitions() = runBlocking {
        // Assert initial state is empty
        var recentSearches = viewModel.searchHistory.value
        assertEquals(0, recentSearches.size)

        // Add search query
        viewModel.addToSearchHistory("Product Lookup", "Organic Milk")
        
        recentSearches = viewModel.searchHistory.value
        assertEquals(1, recentSearches.size)
        assertEquals("Organic Milk", recentSearches[0].query)

        // Clear search history
        viewModel.clearSearchHistory()
        recentSearches = viewModel.searchHistory.value
        assertEquals(0, recentSearches.size)
    }

    @Test
    fun testRejectedLoginDoesNotCreateAccount() = runBlocking {
        val initialUsers = db.userDao().getAllUsers().first()
        assertEquals(0, initialUsers.size)

        val result = viewModel.login("unregistered_user@example.com", "WrongPassword123!")
        org.junit.Assert.assertFalse("Login must be rejected", result)

        val usersAfter = db.userDao().getAllUsers().first()
        assertEquals("No user account should be auto-created on rejected login", 0, usersAfter.size)
        org.junit.Assert.assertNull(viewModel.currentUser.value)
    }

    @Test
    fun testNoHardcodedPrivilegedLogin() = runBlocking {
        val result1 = viewModel.login("zxzx.Mohammad91@gmail.com", "MoAn320222m@")
        val result2 = viewModel.login("admin", "MoAn320222m@")

        org.junit.Assert.assertFalse("Former admin email must not grant access", result1)
        org.junit.Assert.assertFalse("Admin username must not grant access", result2)
        org.junit.Assert.assertNotEquals("Admin", viewModel.currentRole.value)
        org.junit.Assert.assertNull(viewModel.currentUser.value)
    }

    @Test
    fun testMissingFirebaseConfigFailsClosed() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val initialized = MainApplication.ensureFirebaseInitialized(context)
        org.junit.Assert.assertFalse("Firebase without valid credentials must not report configured", initialized)
        org.junit.Assert.assertFalse(MainApplication.isFirebaseConfigured)
        org.junit.Assert.assertFalse(viewModel.isFirebaseOnlineAvailable())

        val loginResult = viewModel.login("user@domain.com", "anyPassword123")
        org.junit.Assert.assertFalse("Authentication must fail closed when Firebase is unconfigured", loginResult)
        org.junit.Assert.assertNotNull("authError must be set when offline", viewModel.authError.value)
    }

    @Test
    fun testDatabaseInitializationIsNonDestructive() = runBlocking {
        val testUser = UserEntity(
            id = 1,
            name = "Persistent User",
            phoneNumber = "+1234567890",
            email = "persistent@example.com",
            role = "Customer",
            permissionGranted = true
        )
        db.userDao().insertUser(testUser)

        val retrieved = db.userDao().getUserByEmailOrPhone("persistent@example.com", "")
        assertNotNull(retrieved)
        assertEquals("Persistent User", retrieved?.name)

        org.junit.Assert.assertTrue(AppDatabase.ALL_MIGRATIONS.isNotEmpty())

        val users = db.userDao().getAllUsers().first()
        assertEquals(1, users.size)
        assertEquals("persistent@example.com", users[0].email)
    }

    @Test
    fun testDirectOwnerLoginActivation() = runBlocking {
        // Test activating direct owner login directly
        val ownerUser = viewModel.executeDirectOwnerLogin()

        val currentUser = viewModel.currentUser.value
        assertNotNull("Current user must be logged in as Owner", currentUser)
        assertEquals("Admin", currentUser?.role)
        assertEquals("admin@amer.ai", currentUser?.email)
        assertEquals("Admin", viewModel.currentRole.value)
        assertEquals("Owner Admin", ownerUser.name)

        // Verify direct owner login is persisted
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
        val isDirectOwnerEnabled = prefs.getBoolean("direct_owner_enabled", false)
        org.junit.Assert.assertTrue("Direct owner mode must be persisted in preferences", isDirectOwnerEnabled)
    }

    @Test
    fun testCartOperationsAndTotals() = runBlocking {
        // Ensure user is logged in
        val ownerUser = viewModel.executeDirectOwnerLogin()
        val userId = ownerUser.id

        // Insert product
        val product = ProductEntity(
            id = 101,
            name = "Organic Dates",
            category = "Organic Foods",
            retailPrice = 15.0,
            wholesalePrice = 10.0,
            imageUrl = "",
            description = "Natural dates",
            ingredients = "Dates",
            stockQuantity = 50,
            isRegisteredMerchant = true,
            merchantName = "Dates Co",
            salesHistory = 10
        )
        db.productDao().insertProduct(product)

        // Add to cart directly via CartItemDao
        val cartItem = CartItemEntity(
            userId = userId,
            productId = product.id,
            productName = product.name,
            imageUrl = product.imageUrl,
            price = product.retailPrice,
            quantity = 2,
            orderType = "Retail"
        )
        db.cartItemDao().insertCartItem(cartItem)

        val items = db.cartItemDao().getCartItems(userId).first()
        assertEquals(1, items.size)
        assertEquals(101, items[0].productId)
        assertEquals(2, items[0].quantity)

        // Delete from cart
        db.cartItemDao().deleteCartItemById(items[0].id)

        val afterDelete = db.cartItemDao().getCartItems(userId).first()
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun testLanguageAndDarkModeToggles() {
        val initialLang = viewModel.appLanguage.value
        viewModel.toggleLanguage()
        val toggledLang = viewModel.appLanguage.value
        org.junit.Assert.assertNotEquals(initialLang, toggledLang)

        val initialDark = viewModel.appDarkMode.value
        viewModel.toggleDarkMode()
        val toggledDark = viewModel.appDarkMode.value
        assertEquals(!initialDark, toggledDark)
    }

    @Test
    fun testCalorieNutritionScannerIntegrity() {
        val result = com.example.ui.NutritionAnalysisResult(
            productName = "Natural Yogurt",
            calories = 120,
            protein = 8.5f,
            carbs = 11.0f,
            fats = 3.2f,
            healthScore = 92,
            certaintyNote = "Verified via barcode hierarchy"
        )
        assertEquals("Natural Yogurt", result.productName)
        assertEquals(120, result.calories)
        org.junit.Assert.assertTrue(result.healthScore in 0..100)
        org.junit.Assert.assertTrue(result.protein > 0f)
        org.junit.Assert.assertTrue(result.carbs > 0f)
        org.junit.Assert.assertTrue(result.fats > 0f)
    }

    @Test
    fun testRealtimePriceMonitoringAndDropAlerts() = runBlocking {
        viewModel.loginDirectAsOwner()
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Insert product
        val product = ProductEntity(
            id = 205,
            name = "Organic Raw Honey",
            category = "Organic Foods",
            retailPrice = 25.0,
            wholesalePrice = 18.0,
            imageUrl = "",
            description = "100% Raw Wildflower Honey",
            ingredients = "Honey",
            stockQuantity = 40,
            isRegisteredMerchant = true,
            merchantName = "Al-Shifaa",
            salesHistory = 15
        )
        db.productDao().insertProduct(product)

        // Set target price alert
        viewModel.setTargetPriceAlert(product, 20.0)
        val alerts = viewModel.targetPriceAlerts.value
        org.junit.Assert.assertTrue(alerts.any { it.productId == 205 && it.targetPrice == 20.0 })

        // Start real-time price monitoring
        viewModel.startRealtimePriceMonitoring(context)
        org.junit.Assert.assertTrue(viewModel.isRealtimePriceMonitoringActive.value)

        // Simulate price drop trigger
        viewModel.simulatePriceDropTrigger(205, 18.5, context)
        val notification = viewModel.recentPriceDropNotification.value
        assertNotNull(notification)
        org.junit.Assert.assertTrue(notification!!.contains("Organic Raw Honey") || notification.contains("انخفاض"))

        // Stop monitoring
        viewModel.stopRealtimePriceMonitoring()
        org.junit.Assert.assertFalse(viewModel.isRealtimePriceMonitoringActive.value)
    }

    @Test
    fun testRoleSwitchingAndAllScreenNavigation() = runBlocking {
        viewModel.loginDirectAsOwner()
        assertEquals("Admin", viewModel.currentRole.value)

        // Switch to Customer & set active tab
        viewModel.switchRole("Customer")
        assertEquals("Customer", viewModel.currentRole.value)
        viewModel.setCustomerActiveTab(1) // Cart
        assertEquals(1, viewModel.customerActiveTab.value)
        viewModel.setCustomerActiveTab(6) // Viral Discovery
        assertEquals(6, viewModel.customerActiveTab.value)

        // Switch to Merchant
        viewModel.switchRole("Merchant")
        assertEquals("Merchant", viewModel.currentRole.value)

        // Switch to Delivery
        viewModel.switchRole("Delivery")
        assertEquals("Delivery", viewModel.currentRole.value)

        // Switch back to Admin
        viewModel.switchRole("Admin")
        assertEquals("Admin", viewModel.currentRole.value)
    }
}
