package com.example.ui.visionx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProductDao
import com.example.data.PriceHistoryDao
import com.example.data.PriceHistoryEntity
import com.example.data.ProductEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class CategoryTrend(
    val category: String,
    val trendData: List<Pair<Long, Double>>
)

class MarketPulseViewModel(
    private val productDao: ProductDao,
    private val priceHistoryDao: PriceHistoryDao
) : ViewModel() {

    private val _categoryTrends = MutableStateFlow<List<CategoryTrend>>(emptyList())
    val categoryTrends = _categoryTrends.asStateFlow()

    init {
        fetchMarketTrends()
    }

    private fun fetchMarketTrends() {
        viewModelScope.launch {
            productDao.getAllProducts().collect { products ->
                val categories = products.map { it.category }.distinct()
                val trends = categories.map { category ->
                    val categoryProducts = products.filter { it.category == category }
                    val history = categoryProducts.flatMap { product ->
                        priceHistoryDao.getHistoryForProductAscSuspend(product.id)
                    }
                    
                    val grouped = mutableMapOf<Long, MutableList<PriceHistoryEntity>>()
                    for (item in history) {
                        if (item.recordedAt > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) {
                            val day = item.recordedAt / TimeUnit.DAYS.toMillis(1)
                            grouped.getOrPut(day) { mutableListOf() }.add(item)
                        }
                    }

                    val aggregated = grouped.map { entry ->
                        val day = entry.key
                        val avgPrice = entry.value.map { it.retailPrice }.average()
                        day * TimeUnit.DAYS.toMillis(1) to avgPrice
                    }.sortedBy { it.first }

                    CategoryTrend(category, aggregated)
                }
                _categoryTrends.value = trends
            }
        }
    }
}
