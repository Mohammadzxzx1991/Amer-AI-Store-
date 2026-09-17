package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.google.firebase.FirebaseApp
import com.example.data.AppDatabase
import com.example.ui.AuthScreen
import com.example.ui.MainLayout
import com.example.ui.MarketViewModel
import com.example.ui.MarketViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class MainActivity : ComponentActivity() {

    private val requestCameraPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "Camera permission granted for CameraX visual search.")
        } else {
            android.util.Log.w("MainActivity", "Camera permission denied by user.")
        }
    }

    fun requestCameraPermissionForVisualSearch() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            var app = FirebaseApp.initializeApp(this)
            if (app == null) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:123456789012:android:abcdef123456")
                    .setProjectId("dummy-project")
                    .setApiKey("AIzaSyDummyKeyForSandboxTesting1234")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
            initializeAppFeatures()
        } catch (e: Exception) {
            try {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:123456789012:android:abcdef123456")
                    .setProjectId("dummy-project")
                    .setApiKey("AIzaSyDummyKeyForSandboxTesting1234")
                    .build()
                FirebaseApp.initializeApp(this, options)
                initializeAppFeatures()
            } catch (e2: Exception) {
                android.util.Log.e("MainActivity", "Firebase initialization deferred or sandboxed: ${e2.message}")
            }
        }
        enableEdgeToEdge()
        requestCameraPermissionForVisualSearch()
        setContent {
            val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "aicommerce.db").fallbackToDestructiveMigration().build()
            val viewModelFactory = MarketViewModelFactory(
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
                getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE),
                db.cachedSearchResultDao(),
                db.cachedPriceComparisonDao()
            )

            val viewModel: MarketViewModel = viewModel(factory = viewModelFactory)
            val isDark by viewModel.appDarkMode.collectAsState()
            val themeStyle by viewModel.appThemeStyle.collectAsState()
            
            MyApplicationTheme(darkTheme = isDark, themeStyle = themeStyle) {
                val currentUser by viewModel.currentUser.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Since AuthScreen and MainLayout fully handle internal paddings and window insets,
                    // we do not need to apply innerPadding explicitly here to prevent double padding issues,
                    // but we can pass it if necessary.
                    if (currentUser == null) {
                        AuthScreen(viewModel = viewModel)
                    } else {
                        MainLayout(viewModel = viewModel)
                    }
                }
            }
        }
    }

    private fun initializeAppFeatures() {
        try {
            val app = com.google.firebase.FirebaseApp.getInstance()
            val apiKey = app.options.apiKey
            if (apiKey.isEmpty() || apiKey.contains("DummyKey") || apiKey == "MY_GEMINI_API_KEY") {
                android.util.Log.i("MainActivity", "Firebase running in local sandboxed mode; skipping online remote config and FCM token requests.")
                return
            }

            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build()
            remoteConfig.setConfigSettingsAsync(configSettings)

            // Initialize Firestore
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            android.util.Log.d("Firebase", "Firestore initialized: \$firestore")

            // Initialize FCM
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    android.util.Log.d("FCM", "FCM Token: \$token")
                } else {
                    android.util.Log.e("FCM", "Fetching FCM registration token failed", task.exception)
                }
            }

            // 1. Set default values
            val defaults = mapOf(
                "welcome_message" to "مرحباً بك في تطبيقنا المميز!",
                "main_theme_color" to "#3498db",
                "enable_new_dashboard" to false
            )
            remoteConfig.setDefaultsAsync(defaults)

            // 2. Fetch and activate
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 3. Read updated values
                        val welcomeMessage = remoteConfig.getString("welcome_message")
                        val themeColor = remoteConfig.getString("main_theme_color")
                        val isNewDashboardEnabled = remoteConfig.getBoolean("enable_new_dashboard")

                        // You can now apply these values to your ViewModel or UI state dynamically
                        android.util.Log.d("RemoteConfig", "Fetched welcome message: $welcomeMessage")
                    } else {
                        android.util.Log.e("RemoteConfig", "فشل في تحميل الإعدادات الديناميكية")
                    }
                }
        } catch (error: Exception) {
            android.util.Log.e("RemoteConfig", "فشل في إعداد Remote Config: \${error.message}")
        }
    }
}
