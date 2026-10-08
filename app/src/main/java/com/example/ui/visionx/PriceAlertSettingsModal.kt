package com.example.ui.visionx

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.ProductEntity
import com.example.ui.MarketViewModel
import com.example.ui.TrendingProduct

/**
 * PriceAlertSettings Modal
 * Allows users to define a target price for a product in their wishlist,
 * saving the alert configuration to Firestore and subscribing to Firebase Cloud Messaging (FCM)
 * push notification topics via PriceRadarMessagingService.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceAlertSettingsModal(
    product: ProductEntity,
    viewModel: MarketViewModel,
    onDismiss: () -> Unit,
    onAlertSet: (Double) -> Unit = {}
) {
    val context = LocalContext.current
    val currentAlerts by viewModel.targetPriceAlerts.collectAsState()
    val existingAlert = currentAlerts.find { it.productId == product.id }

    var targetPriceInput by remember(product, existingAlert) {
        mutableStateOf(
            if (existingAlert != null) {
                String.format("%.2f", existingAlert.targetPrice)
            } else {
                String.format("%.2f", product.retailPrice * 0.90) // Default 10% discount target
            }
        )
    }

    var selectedPercentageDiscount by remember { mutableIntStateOf(10) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessBanner by remember { mutableStateOf(false) }

    val currentRetail = product.retailPrice
    val targetVal = targetPriceInput.toDoubleOrNull() ?: currentRetail
    val savingsAmount = (currentRetail - targetVal).coerceAtLeast(0.0)
    val savingsPercent = if (currentRetail > 0) ((savingsAmount / currentRetail) * 100).toInt() else 0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("modal_price_alert_settings")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with title and close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "إعدادات تنبيه السعر",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Price Radar Target Alert (FCM)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("btn_close_price_alert_modal")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Product mini card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.name,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "السعر الحالي: ",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$${String.format("%.2f", product.retailPrice)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }

                // Quick percentage target chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "اختر نسبة الخصم المستهدفة:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 20, 25).forEach { pct ->
                            val isSelected = selectedPercentageDiscount == pct
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedPercentageDiscount = pct
                                    val calculated = product.retailPrice * (1.0 - (pct / 100.0))
                                    targetPriceInput = String.format("%.2f", calculated)
                                },
                                label = {
                                    Text(
                                        text = "-$pct%",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Custom Target Price Input Field
                OutlinedTextField(
                    value = targetPriceInput,
                    onValueChange = { input ->
                        targetPriceInput = input
                        selectedPercentageDiscount = 0
                    },
                    label = { Text("السعر المستهدف ($)") },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_target_price")
                )

                // Projected Savings badge
                if (savingsAmount > 0) {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "التوفير المتوقع عند التنبيه:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF065F46)
                                )
                            }
                            Text(
                                text = "$${String.format("%.2f", savingsAmount)} ($savingsPercent%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }

                // Firebase Cloud Messaging Info note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "يتم حفظ التنبيه في Firestore ومزامنة إشعارات FCM فور وصول السعر للهدف.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(visible = showSuccessBanner) {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "تم تفعيل رادار الأسعار والاشتراك في إشعارات FCM بنجاح!",
                                fontSize = 11.sp,
                                color = Color(0xFF065F46),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Test simulate drop button
                    OutlinedButton(
                        onClick = {
                            val price = targetPriceInput.toDoubleOrNull() ?: (product.retailPrice * 0.85)
                            viewModel.simulatePriceDropTrigger(product.id, price, context)
                            Toast.makeText(context, "تم إرسال إشعار تجريبي عبر رادار الأسعار ⚡", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_test_price_radar_push")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("تجربة الإشعار", fontSize = 11.sp)
                    }

                    // Save / Activate Button
                    Button(
                        onClick = {
                            val finalTarget = targetPriceInput.toDoubleOrNull()
                            if (finalTarget != null && finalTarget > 0) {
                                isSaving = true
                                viewModel.setTargetPriceAlert(product, finalTarget)
                                onAlertSet(finalTarget)
                                showSuccessBanner = true
                                isSaving = false
                            } else {
                                Toast.makeText(context, "يرجى إدخال سعر مستهدف صحيح", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_save_price_alert")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("تفعيل التنبيه 🔔", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Overload for Wishlist TrendingProduct
 */
@Composable
fun PriceAlertSettingsModal(
    trendingProduct: TrendingProduct,
    viewModel: MarketViewModel,
    onDismiss: () -> Unit,
    onAlertSet: (Double) -> Unit = {}
) {
    val entity = remember(trendingProduct) {
        ProductEntity(
            id = trendingProduct.id,
            name = trendingProduct.name,
            category = trendingProduct.category,
            retailPrice = trendingProduct.retailPrice,
            wholesalePrice = trendingProduct.wholesalePrice,
            imageUrl = trendingProduct.imageUrl,
            description = trendingProduct.description,
            ingredients = trendingProduct.aiInsight,
            stockQuantity = 50,
            isRegisteredMerchant = false,
            merchantName = trendingProduct.trendPlatform,
            salesHistory = 120
        )
    }

    PriceAlertSettingsModal(
        product = entity,
        viewModel = viewModel,
        onDismiss = onDismiss,
        onAlertSet = onAlertSet
    )
}
