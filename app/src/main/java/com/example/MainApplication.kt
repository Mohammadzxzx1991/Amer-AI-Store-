package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ensureFirebaseInitialized(this)
    }

    companion object {
        fun ensureFirebaseInitialized(context: android.content.Context) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    val app = FirebaseApp.initializeApp(context)
                    if (app == null) {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId("1:123456789012:android:abcdef123456")
                            .setProjectId("ameraistore-1c81d")
                            .setApiKey("AIzaSyDummyKeyForSandboxTesting1234")
                            .build()
                        FirebaseApp.initializeApp(context, options)
                    }
                }
            } catch (e: Exception) {
                try {
                    if (FirebaseApp.getApps(context).isEmpty()) {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId("1:123456789012:android:abcdef123456")
                            .setProjectId("ameraistore-1c81d")
                            .setApiKey("AIzaSyDummyKeyForSandboxTesting1234")
                            .build()
                        FirebaseApp.initializeApp(context, options)
                    }
                } catch (e2: Exception) {
                    Log.e("MainApplication", "Firebase initialization fallback error: ${e2.message}")
                }
            }
        }
    }
}
