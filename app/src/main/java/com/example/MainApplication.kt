package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ensureFirebaseInitialized(this)
    }

    companion object {
        private const val TAG = "MainApplication"
        var isFirebaseConfigured: Boolean = false
            private set

        fun ensureFirebaseInitialized(context: android.content.Context): Boolean {
            return try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    val app = FirebaseApp.initializeApp(context)
                    isFirebaseConfigured = app != null && isAppProperlyConfigured(app)
                } else {
                    val app = FirebaseApp.getInstance()
                    isFirebaseConfigured = isAppProperlyConfigured(app)
                }
                if (!isFirebaseConfigured) {
                    Log.i(TAG, "Firebase configuration required: online Firebase services remain disabled safely.")
                }
                isFirebaseConfigured
            } catch (e: Exception) {
                isFirebaseConfigured = false
                Log.i(TAG, "Firebase configuration absent or uninitialized: online services disabled safely.")
                false
            }
        }

        fun isAppProperlyConfigured(app: FirebaseApp): Boolean {
            val apiKey = app.options.apiKey
            val appId = app.options.applicationId
            return apiKey.isNotBlank() &&
                    !apiKey.contains("DummyKey") &&
                    apiKey != "MY_GEMINI_API_KEY" &&
                    appId.isNotBlank() &&
                    !appId.contains("123456789012")
        }
    }
}
