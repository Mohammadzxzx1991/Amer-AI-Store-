package com.example.ui.visionx

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MarketViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPulseDashboard(
    // In a real app, inject the viewmodel properly.
    viewModel: MarketPulseViewModel
) {
    val trends by viewModel.categoryTrends.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Market Pulse") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(trends) { trend ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = trend.category, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Simple Line Chart using Canvas
                        Canvas(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                            if (trend.trendData.isEmpty()) return@Canvas
                            val maxPrice = trend.trendData.maxOf { it.second }.toFloat()
                            val minPrice = trend.trendData.minOf { it.second }.toFloat()
                            val range = if (maxPrice == minPrice) 1f else maxPrice - minPrice
                            
                            val path = Path()
                            trend.trendData.forEachIndexed { index, pair ->
                                val x = size.width * index / (trend.trendData.size - 1)
                                val y = size.height - (size.height * (pair.second.toFloat() - minPrice) / range)
                                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(path, Color.Green, style = Stroke(width = 4f))
                        }
                    }
                }
            }
        }
    }
}
