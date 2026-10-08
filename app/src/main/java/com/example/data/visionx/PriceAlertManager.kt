package com.example.data.visionx

import android.content.Context
import android.util.Log
import com.example.data.ProductEntity
import com.example.data.agent.AgentEngine
import com.example.data.agent.AgentKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Price Alert Manager
 * Manages user-defined target price alerts for products saved in Room database,
 * integrates with Firebase Cloud Messaging topics, and dispatches automated push notifications.
 */
object PriceAlertManager {
    private const val TAG = "PriceAlertManager"

    // Map of productId to target price
    private val targetPrices = mutableMapOf<Int, Double>()
    // Map of productId to tracking active state
    private val trackingStates = mutableMapOf<Int, Boolean>()

    private val _activeAlerts = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val activeAlerts: StateFlow<Map<Int, Double>> = _activeAlerts.asStateFlow()

    private val _activeTracking = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val activeTracking: StateFlow<Map<Int, Boolean>> = _activeTracking.asStateFlow()

    fun isTrackingActive(productId: Int): Boolean {
        return trackingStates[productId] ?: (targetPrices.containsKey(productId))
    }

    fun setTargetPrice(context: Context, product: ProductEntity, targetPrice: Double) {
        setTargetPrice(context, product.id, product.name, targetPrice)
    }

    fun setTargetPrice(context: Context, productId: Int, productName: String, targetPrice: Double) {
        targetPrices[productId] = targetPrice
        trackingStates[productId] = true
        _activeAlerts.value = targetPrices.toMap()
        _activeTracking.value = trackingStates.toMap()

        // Subscribe to FCM topic for this product
        PriceRadarMessagingService.subscribeToProductPriceAlerts(productId) { success ->
            Log.d(TAG, "Subscribed to FCM topic for product $productId: $success")
        }

        // Record immutable evidence in 19-Agent Governance Ledger
        AgentEngine.recordEvidence(
            operationId = "ALERT_SET_${productId}_" + UUID.randomUUID().toString().take(4),
            actorId = AgentKey.DATA_GROWTH_INTELLIGENCE.key,
            type = "PRICE_ALERT_TARGET_SET",
            source = "Room Database & FCM Price Alert Manager",
            payloadSummary = "Target price set to $${String.format("%.2f", targetPrice)} for tracked product '$productName' (ID: $productId)."
        )
    }

    fun toggleTracking(context: Context, productId: Int, productName: String, defaultTargetPrice: Double): Boolean {
        val current = isTrackingActive(productId)
        return if (current) {
            removeTargetPrice(context, productId)
            false
        } else {
            val target = targetPrices[productId] ?: defaultTargetPrice
            setTargetPrice(context, productId, productName, target)
            true
        }
    }

    fun removeTargetPrice(context: Context, productId: Int) {
        targetPrices.remove(productId)
        trackingStates[productId] = false
        _activeAlerts.value = targetPrices.toMap()
        _activeTracking.value = trackingStates.toMap()
        PriceRadarMessagingService.unsubscribeFromProductPriceAlerts(productId)

        AgentEngine.recordEvidence(
            operationId = "ALERT_REM_${productId}_" + UUID.randomUUID().toString().take(4),
            actorId = AgentKey.DATA_GROWTH_INTELLIGENCE.key,
            type = "PRICE_ALERT_REMOVED",
            source = "Room Database & FCM Price Alert Manager",
            payloadSummary = "Price alert removed for product ID: $productId."
        )
    }

    fun getTargetPrice(productId: Int): Double? {
        return targetPrices[productId]
    }

    /**
     * Checks if a product's current price in the Price Radar cache has dropped below the set target price,
     * and triggers the Firebase Cloud Messaging notification if triggered.
     */
    fun checkAndTriggerAlertIfNeeded(
        context: Context,
        product: ProductEntity,
        currentPrice: Double
    ) {
        checkAndTriggerAlertIfNeeded(context, product.id, product.name, currentPrice)
    }

    fun checkAndTriggerAlertIfNeeded(
        context: Context,
        productId: Int,
        productName: String,
        currentPrice: Double
    ) {
        val target = targetPrices[productId] ?: return
        if (currentPrice <= target) {
            PriceRadarMessagingService.dispatchTargetPriceReachedPushNotification(
                context = context,
                productId = productId,
                productName = productName,
                currentPrice = currentPrice,
                targetPrice = target
            )

            // Record immutable evidence in 19-Agent Governance Ledger
            AgentEngine.recordEvidence(
                operationId = "ALERT_FIRED_${productId}_" + UUID.randomUUID().toString().take(4),
                actorId = AgentKey.DATA_GROWTH_INTELLIGENCE.key,
                type = "PRICE_ALERT_FCM_TRIGGERED",
                source = "Firebase Cloud Messaging (PriceAlertManager)",
                payloadSummary = "Price drop alert triggered for '$productName': current $${String.format("%.2f", currentPrice)} <= target $${String.format("%.2f", target)}"
            )
        }
    }

    /**
     * Simulates hitting the target price threshold for testing and immediate user feedback.
     */
    fun simulateTargetPriceHit(
        context: Context,
        productId: Int,
        productName: String,
        simulatedDroppedPrice: Double? = null
    ) {
        val target = targetPrices[productId] ?: (simulatedDroppedPrice ?: 19.99)
        val droppedPrice = simulatedDroppedPrice ?: (target * 0.95)

        PriceRadarMessagingService.dispatchTargetPriceReachedPushNotification(
            context = context,
            productId = productId,
            productName = productName,
            currentPrice = droppedPrice,
            targetPrice = target
        )

        AgentEngine.recordEvidence(
            operationId = "ALERT_TEST_${productId}_" + UUID.randomUUID().toString().take(4),
            actorId = AgentKey.DATA_GROWTH_INTELLIGENCE.key,
            type = "PRICE_ALERT_FCM_SIMULATION",
            source = "Firebase Cloud Messaging Test Trigger",
            payloadSummary = "Simulated real-time FCM target price alert for '$productName' at $${String.format("%.2f", droppedPrice)} (target: $${String.format("%.2f", target)})."
        )
    }
}
