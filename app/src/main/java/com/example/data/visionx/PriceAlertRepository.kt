package com.example.data.visionx

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class PriceAlert(
    val productId: Int,
    val userId: String,
    val targetPrice: Double
)

class PriceAlertRepository(private val db: FirebaseFirestore) {
    private val alertsCollection = db.collection("price_alerts")

    suspend fun savePriceAlert(alert: PriceAlert) {
        alertsCollection.document("${alert.userId}_${alert.productId}").set(alert).await()
    }

    suspend fun getPriceAlert(userId: String, productId: Int): PriceAlert? {
        val snapshot = alertsCollection.document("${userId}_${productId}").get().await()
        return snapshot.toObject(PriceAlert::class.java)
    }

    suspend fun deletePriceAlert(userId: String, productId: Int) {
        alertsCollection.document("${userId}_${productId}").delete().await()
    }
}
