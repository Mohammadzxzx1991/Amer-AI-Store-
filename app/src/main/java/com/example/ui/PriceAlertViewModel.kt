package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.visionx.PriceAlert
import com.example.data.visionx.PriceAlertRepository
import kotlinx.coroutines.launch

class PriceAlertViewModel(private val priceAlertRepository: PriceAlertRepository) : ViewModel() {

    fun setPriceAlert(userId: String, productId: Int, targetPrice: Double) {
        viewModelScope.launch {
            priceAlertRepository.savePriceAlert(PriceAlert(productId, userId, targetPrice))
        }
    }
}
