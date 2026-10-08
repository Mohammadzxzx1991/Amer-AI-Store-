package com.example.data.visionx

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.agent.AgentEngine
import com.example.data.agent.AgentKey
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.UUID

/**
 * Firebase Cloud Messaging Service for Price Radar Automated Push Notifications.
 * Handles incoming FCM messages when tracked products reach or drop below their target price.
 */
class PriceRadarMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Device Registration Token generated.")
        // Automatically subscribe to the global price radar alerts topic
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(GLOBAL_PRICE_RADAR_TOPIC)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully subscribed to $GLOBAL_PRICE_RADAR_TOPIC")
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error subscribing to $GLOBAL_PRICE_RADAR_TOPIC: ${e.message}")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val productId = data["productId"]?.toIntOrNull() ?: 0
        val productName = data["productName"] ?: notification?.title ?: "منتج متتبع"
        val currentPrice = data["currentPrice"]?.toDoubleOrNull() ?: 0.0
        val targetPrice = data["targetPrice"]?.toDoubleOrNull() ?: 0.0
        val storeName = data["storeName"] ?: "متجر معتمد"

        val title = notification?.title ?: "🔥 انخفاض سعر في رادار الأسعار!"
        val body = notification?.body ?: "وصل سعر '$productName' إلى $${String.format("%.2f", currentPrice)} في $storeName (هدفك: $${String.format("%.2f", targetPrice)})!"

        showPriceRadarNotification(
            context = applicationContext,
            productId = productId,
            title = title,
            body = body,
            productName = productName,
            currentPrice = currentPrice
        )

        // Record evidence in 19-Agent Governance Engine
        AgentEngine.recordEvidence(
            operationId = "FCM_NOTIF_${productId}_${UUID.randomUUID().toString().take(6)}",
            actorId = AgentKey.DATA_GROWTH_INTELLIGENCE.key,
            type = "FCM_PRICE_RADAR_NOTIFICATION",
            source = "Firebase Messaging (PriceRadarMessagingService)",
            payloadSummary = "Push notification delivered for '$productName': current $${String.format("%.2f", currentPrice)} <= target $${String.format("%.2f", targetPrice)}"
        )
    }

    companion object {
        private const val TAG = "PriceRadarFCM"
        const val CHANNEL_ID = "price_radar_alerts"
        const val CHANNEL_NAME = "Price Radar Target Price Alerts"
        const val GLOBAL_PRICE_RADAR_TOPIC = "price_radar_alerts"

        /**
         * Dispatches a high-priority system push notification to the user's status bar
         * when a tracked product hits or drops below the user's target price.
         */
        fun showPriceRadarNotification(
            context: Context,
            productId: Int,
            title: String,
            body: String,
            productName: String,
            currentPrice: Double
        ) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Automated push notifications for tracked products reaching target prices"
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TO", "PRICE_RADAR")
                putExtra("PRODUCT_ID", productId)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                productId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationId = 20000 + (productId % 10000)
            notificationManager.notify(notificationId, builder.build())
            Log.d(TAG, "Notification displayed for product: $productName (ID: $productId)")
        }

        /**
         * Dispatches high-priority push notification for watched products reaching their target price point
         */
        fun dispatchTargetPriceReachedPushNotification(
            context: Context,
            productId: Int,
            productName: String,
            currentPrice: Double,
            targetPrice: Double
        ) {
            val title = "🎯 وصل السعر المستهدف: $productName"
            val body = "انخفض سعر '$productName' الآن إلى $${String.format("%.2f", currentPrice)} وهو أقل أو يساوي سعرك المستهدف ($${String.format("%.2f", targetPrice)})! اضغط للشراء بأفضل سعر."
            showPriceRadarNotification(
                context = context,
                productId = productId,
                title = title,
                body = body,
                productName = productName,
                currentPrice = currentPrice
            )
        }

        /**
         * Subscribes the device to FCM topic for a specific tracked product
         */
        fun subscribeToProductPriceAlerts(productId: Int, onComplete: ((Boolean) -> Unit)? = null) {
            try {
                FirebaseMessaging.getInstance().subscribeToTopic("price_drop_$productId")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Subscribed to topic price_drop_$productId")
                            onComplete?.invoke(true)
                        } else {
                            Log.w(TAG, "Failed to subscribe to topic price_drop_$productId: ${task.exception?.message ?: "unknown"}")
                            onComplete?.invoke(false)
                        }
                    }
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseMessaging not available for subscription: ${e.message}")
                onComplete?.invoke(false)
            }
        }

        /**
         * Unsubscribes from FCM topic for a specific tracked product
         */
        fun unsubscribeFromProductPriceAlerts(productId: Int) {
            try {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("price_drop_$productId")
                    .addOnCompleteListener {
                        Log.d(TAG, "Unsubscribed from topic price_drop_$productId")
                    }
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseMessaging unsubscribe error: ${e.message}")
            }
        }
    }
}
