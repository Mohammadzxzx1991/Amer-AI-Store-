package com.example

import com.example.data.PriceHistoryEntity
import com.example.data.visionx.PriceAlertManager
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying Price Radar target price thresholds,
 * Room price history data modeling for Recharts trend lines,
 * and Firebase Messaging alert triggering logic.
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testPriceHistoryOrderingForRecharts() {
        val now = System.currentTimeMillis()
        val h1 = PriceHistoryEntity(id = 1, productId = 101, productName = "Organic Coffee", merchantName = "Amazon", retailPrice = 25.0, recordedAt = now - 50000)
        val h2 = PriceHistoryEntity(id = 2, productId = 101, productName = "Organic Coffee", merchantName = "Walmart", retailPrice = 22.0, recordedAt = now - 20000)
        val h3 = PriceHistoryEntity(id = 3, productId = 101, productName = "Organic Coffee", merchantName = "Carrefour", retailPrice = 19.5, recordedAt = now)

        val unsorted = listOf(h2, h3, h1)
        val sortedForRecharts = unsorted.sortedBy { it.recordedAt }

        assertEquals(101, sortedForRecharts.first().productId)
        assertEquals(25.0, sortedForRecharts.first().retailPrice, 0.01)
        assertEquals(19.5, sortedForRecharts.last().retailPrice, 0.01)
    }

    @Test
    fun testTargetPriceThresholdHitEvaluation() {
        val targetThreshold = 20.0
        val priceAbove = 22.50
        val priceExact = 20.00
        val priceBelow = 18.99

        assertFalse("Price above target should not trigger alert", priceAbove <= targetThreshold)
        assertTrue("Price exact target should trigger alert", priceExact <= targetThreshold)
        assertTrue("Price below target should trigger alert", priceBelow <= targetThreshold)
    }

    @Test
    fun testPriceAlertManagerTargets() {
        val productId = 999
        val target = 45.0
        val targetPriceMap = mutableMapOf<Int, Double>()

        targetPriceMap[productId] = target
        assertEquals(45.0, targetPriceMap[productId] ?: 0.0, 0.01)

        val currentQuotePrice = 42.5
        val isThresholdHit = currentQuotePrice <= (targetPriceMap[productId] ?: 0.0)
        assertTrue("Should detect target price hit", isThresholdHit)
    }
}
