package com.example.data

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response as OkHttpResponse
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class ApiProductResponse(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val wholesalePrice: Double = price * 0.8,
    val retailer: String = "Global Market",
    val imageUrl: String = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=500",
    val description: String = ""
)

data class RetailerPriceDto(
    val retailerName: String,
    val price: Double,
    val shippingFee: Double = 0.0,
    val shippingDays: Int = 1,
    val isBestDeal: Boolean = false,
    val productUrl: String = ""
)

data class PriceComparisonResponse(
    val productId: Int,
    val productName: String,
    val lowestPrice: Double,
    val averagePrice: Double,
    val priceSpread: Double,
    val prices: List<RetailerPriceDto>
)

interface ProductApiService {
    @GET("api/v1/products/search")
    suspend fun searchProducts(
        @Query("q") query: String
    ): Response<List<ApiProductResponse>>

    @GET("api/v1/products/{id}/compare")
    suspend fun getPriceComparison(
        @Path("id") productId: Int
    ): Response<PriceComparisonResponse>

    companion object {
        private const val BASE_URL = "https://api.smartmarket.example.com/"

        fun create(): ProductApiService {
            val mockInterceptor = Interceptor { chain ->
                val request = chain.request()
                val urlStr = request.url.toString()

                val jsonResponse = when {
                    urlStr.contains("products/search") -> {
                        val queryParam = request.url.queryParameter("q") ?: ""
                        """
                        [
                            {
                                "id": 101,
                                "name": "Organic Milk 1L - Fresh Direct",
                                "category": "Dairy",
                                "price": 2.49,
                                "wholesalePrice": 1.95,
                                "retailer": "BioFarm Market",
                                "imageUrl": "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=500",
                                "description": "100% Pure Grade A Organic Whole Milk."
                            },
                            {
                                "id": 102,
                                "name": "Extra Virgin Olive Oil 500ml",
                                "category": "Oils & Vinegars",
                                "price": 8.99,
                                "wholesalePrice": 6.80,
                                "retailer": "Mediterranean Harvest",
                                "imageUrl": "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=500",
                                "description": "Cold-pressed extra virgin olive oil."
                            },
                            {
                                "id": 103,
                                "name": "Fresh Organic Bananas (Bunch)",
                                "category": "Fruits",
                                "price": 1.29,
                                "wholesalePrice": 0.90,
                                "retailer": "Eco Grocers",
                                "imageUrl": "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=500",
                                "description": "Sustainably farmed organic bananas."
                            }
                        ]
                        """.trimIndent()
                    }
                    urlStr.contains("compare") -> {
                        """
                        {
                            "productId": 101,
                            "productName": "Organic Milk 1L",
                            "lowestPrice": 2.25,
                            "averagePrice": 2.58,
                            "priceSpread": 0.60,
                            "prices": [
                                {
                                    "retailerName": "Direct Farmer Outlet",
                                    "price": 2.25,
                                    "shippingFee": 0.0,
                                    "shippingDays": 1,
                                    "isBestDeal": true
                                },
                                {
                                    "retailerName": "Eco Supermarket",
                                    "price": 2.49,
                                    "shippingFee": 1.5,
                                    "shippingDays": 2,
                                    "isBestDeal": false
                                },
                                {
                                    "retailerName": "City Express Grocer",
                                    "price": 2.85,
                                    "shippingFee": 0.0,
                                    "shippingDays": 1,
                                    "isBestDeal": false
                                }
                            ]
                        }
                        """.trimIndent()
                    }
                    else -> "{}"
                }

                OkHttpResponse.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(jsonResponse.toResponseBody("application/json".toMediaType()))
                    .build()
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(mockInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            return retrofit.create(ProductApiService::class.java)
        }
    }
}
