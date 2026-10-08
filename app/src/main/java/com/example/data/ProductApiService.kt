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
import com.squareup.moshi.JsonClass

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

data class RetailApiQuoteDto(
    val retailerName: String,
    val apiSource: String,
    val price: Double,
    val wholesalePrice: Double,
    val shippingFee: Double,
    val deliveryEta: String,
    val stockStatus: String,
    val rating: Double,
    val productUrl: String,
    val isBestDeal: Boolean
)

data class RetailApiComparisonResponse(
    val queryUrl: String,
    val productName: String,
    val lowestPriceFound: Double,
    val lowestPriceRetailer: String,
    val averageMarketPrice: Double,
    val totalSavings: Double,
    val quotes: List<RetailApiQuoteDto>
)

@JsonClass(generateAdapter = true)
data class ViralProductMentionDto(
    val id: String,
    val productId: Int,
    val productNameAr: String,
    val productNameEn: String,
    val category: String,
    val retailPrice: Double,
    val originalPrice: Double,
    val imageUrl: String,
    val platform: String,
    val creatorHandle: String,
    val creatorName: String,
    val creatorAvatarUrl: String,
    val postCaption: String,
    val hashtags: List<String>,
    val videoUrl: String,
    val postUrl: String,
    val viewCount: Long,
    val likeCount: Long,
    val shareCount: Long,
    val viralVelocity: Double,
    val sentimentScore: Double,
    val viralReasonAr: String,
    val viralReasonEn: String
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

    @GET("api/v1/radar/compare-links")
    suspend fun compareRetailLinks(
        @Query("url") productUrl: String,
        @Query("name") productName: String? = null,
        @Query("basePrice") basePrice: Double? = null
    ): Response<RetailApiComparisonResponse>

    @GET("api/v1/social/viral-mentions")
    suspend fun getViralProductMentions(
        @Query("platform") platform: String? = null,
        @Query("category") category: String? = null
    ): Response<List<ViralProductMentionDto>>

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
                    urlStr.contains("compare-links") -> {
                        val reqUrl = request.url.queryParameter("url") ?: ""
                        val prodName = request.url.queryParameter("name") ?: "منتج متتبع من Vision X"
                        val base = request.url.queryParameter("basePrice")?.toDoubleOrNull() ?: 12.50
                        val p1 = Math.round(base * 1.04 * 100.0) / 100.0
                        val p2 = Math.round(base * 0.93 * 100.0) / 100.0
                        val p3 = Math.round(base * 0.97 * 100.0) / 100.0
                        val p4 = Math.round(base * 0.88 * 100.0) / 100.0
                        val p5 = Math.round(base * 0.78 * 100.0) / 100.0
                        val lowest = p5
                        val avg = Math.round(((p1 + p2 + p3 + p4 + p5) / 5.0) * 100.0) / 100.0
                        val savings = Math.round((avg - lowest) * 100.0) / 100.0

                        """
                        {
                            "queryUrl": "$reqUrl",
                            "productName": "$prodName",
                            "lowestPriceFound": $lowest,
                            "lowestPriceRetailer": "تعاونية المزارعين وسوق الجملة (Wholesale Co-op)",
                            "averageMarketPrice": $avg,
                            "totalSavings": $savings,
                            "quotes": [
                                {
                                    "retailerName": "Amazon Fresh",
                                    "apiSource": "Amazon Product Advertising API v5",
                                    "price": $p1,
                                    "wholesalePrice": ${Math.round(p1 * 0.85 * 100.0) / 100.0},
                                    "shippingFee": 0.0,
                                    "deliveryEta": "توصيل خلال ساعتين (Prime)",
                                    "stockStatus": "متوفر في المخزون (In Stock)",
                                    "rating": 4.8,
                                    "productUrl": "https://www.amazon.com/dp/sample",
                                    "isBestDeal": false
                                },
                                {
                                    "retailerName": "Walmart Grocery",
                                    "apiSource": "Walmart Open API Gateway",
                                    "price": $p2,
                                    "wholesalePrice": ${Math.round(p2 * 0.84 * 100.0) / 100.0},
                                    "shippingFee": 1.99,
                                    "deliveryEta": "توصيل اليوم نفسه (Walmart+)",
                                    "stockStatus": "متوفر (عرض ترويجي)",
                                    "rating": 4.6,
                                    "productUrl": "https://www.walmart.com/ip/sample",
                                    "isBestDeal": false
                                },
                                {
                                    "retailerName": "كارفور هايبرماركت (Carrefour)",
                                    "apiSource": "Carrefour Retail Enterprise API",
                                    "price": $p3,
                                    "wholesalePrice": ${Math.round(p3 * 0.86 * 100.0) / 100.0},
                                    "shippingFee": 0.0,
                                    "deliveryEta": "توصيل فوري خلال 60 دقيقة",
                                    "stockStatus": "متوفر بالمستودع الإقليمي",
                                    "rating": 4.7,
                                    "productUrl": "https://www.carrefour.com/product/sample",
                                    "isBestDeal": false
                                },
                                {
                                    "retailerName": "أسواق العثيم المركزية (Al-Othaim)",
                                    "apiSource": "Al-Othaim Direct Commerce API",
                                    "price": $p4,
                                    "wholesalePrice": ${Math.round(p4 * 0.79 * 100.0) / 100.0},
                                    "shippingFee": 1.50,
                                    "deliveryEta": "توصيل مجدول خلال ساعتين",
                                    "stockStatus": "متوفر بخصم الولاء",
                                    "rating": 4.5,
                                    "productUrl": "https://www.othaimmarkets.com/sample",
                                    "isBestDeal": false
                                },
                                {
                                    "retailerName": "تعاونية المزارعين وسوق الجملة (Wholesale Co-op)",
                                    "apiSource": "Amer Wholesale Direct Ledger API",
                                    "price": $p5,
                                    "wholesalePrice": ${Math.round(p5 * 0.90 * 100.0) / 100.0},
                                    "shippingFee": 2.00,
                                    "deliveryEta": "شحن مباشر من المزرعة",
                                    "stockStatus": "مخزون جملة حي معتمد",
                                    "rating": 4.9,
                                    "productUrl": "https://coop.amer.ai/sample",
                                    "isBestDeal": true
                                }
                            ]
                        }
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
                    urlStr.contains("viral-mentions") -> {
                        """
                        [
                            {
                                "id": "viral_tiktok_101",
                                "productId": 1,
                                "productNameAr": "عسل السدر الجبلي الملكي الطبيعي 100%",
                                "productNameEn": "Royal Mountain Sidr Honey 100% Pure",
                                "category": "Organic Foods",
                                "retailPrice": 38.50,
                                "originalPrice": 48.00,
                                "imageUrl": "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=500",
                                "platform": "TikTok",
                                "creatorHandle": "@taste_of_nature",
                                "creatorName": "سارة الشمري - لايف ستايل صحي",
                                "creatorAvatarUrl": "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=120",
                                "postCaption": "أخيراً لقيت عسل سدر بلدي حقيقي بدون أي سكر مضاف! 🍯 الفرق في الطاقة والمناعة يجنن جربوه مع القهوة الصباحية #عسل_سدر #صحة #تغذية_عضوية",
                                "hashtags": ["#عسل_سدر", "#تغذية_عضوية", "#تريند_تيكتوك", "#صحة_عامة", "#عامر_ستور"],
                                "videoUrl": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                                "postUrl": "https://tiktok.com/@taste_of_nature/video/7391028392",
                                "viewCount": 3850000,
                                "likeCount": 248000,
                                "shareCount": 42100,
                                "viralVelocity": 9.8,
                                "sentimentScore": 0.94,
                                "viralReasonAr": "تريند غذائي فيروسي: ارتفاع الطلب بنسبة +320% بعد ريفيو وتجربة فطور صحي شوهدت 3.8 مليون مرة.",
                                "viralReasonEn": "Viral nutrition wave: +320% order surge following organic breakfast routine video with 3.8M views."
                            },
                            {
                                "id": "viral_reels_102",
                                "productId": 2,
                                "productNameAr": "زيت زيتون بكر ممتاز عصرة أولى على البارد 1 لتر",
                                "productNameEn": "Cold Pressed Extra Virgin Olive Oil 1L",
                                "category": "Organic Foods",
                                "retailPrice": 14.99,
                                "originalPrice": 19.50,
                                "imageUrl": "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=500",
                                "platform": "Instagram",
                                "creatorHandle": "@chef_kareem_omari",
                                "creatorName": "الشيف كريم العمري",
                                "creatorAvatarUrl": "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=120",
                                "postCaption": "سر النكهة المتوسطية الأصيلة في المطاعم العالمية! 🌿 عصرة أولى حقيقية ونسبة حموضة أقل من 0.3%، رهيب مع السلطات والمشاوي. #زيت_زيتون #طبخ_صحي #ريلز_انستغرام",
                                "hashtags": ["#زيت_زيتون", "#طبخ_صحي", "#ريلز", "#أكلات_سريعة", "#مطبخ_عامر"],
                                "videoUrl": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                                "postUrl": "https://instagram.com/reel/C89xZa201pL",
                                "viewCount": 2190000,
                                "likeCount": 164000,
                                "shareCount": 28900,
                                "viralVelocity": 8.7,
                                "sentimentScore": 0.91,
                                "viralReasonAr": "توصية مباشرة من كبار طهاة إنستغرام: صعود المبيعات بنسبة +190% وتقييم جودة استثنائي 4.9/5.",
                                "viralReasonEn": "Chef endorsement reel: +190% conversions with top-tier culinary rating 4.9/5."
                            },
                            {
                                "id": "viral_shorts_103",
                                "productId": 3,
                                "productNameAr": "بن يمني خولاني فاخر محمص وسط 500 جرام",
                                "productNameEn": "Artisan Yemeni Khawlani Special Roast Coffee 500g",
                                "category": "Spiced Coffee & Tea",
                                "retailPrice": 22.00,
                                "originalPrice": 28.00,
                                "imageUrl": "https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=500",
                                "platform": "YouTube",
                                "creatorHandle": "@coffee_maverick",
                                "creatorName": "سلطان الباريستا - تجارب القهوة",
                                "creatorAvatarUrl": "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=120",
                                "postCaption": "هل يستحق البن اليمني هذا السعر؟ تجربة استخلاص V60 وإسبريسو على الهواء مباشرة! الإيحاءات العطرية فاكهية لا تصدق ☕🔥 #يوتيوب_شورتس #قهوة_مختصة",
                                "hashtags": ["#قهوة_مختصة", "#بن_يمني", "#شورتس", "#باريستا", "#محبي_القهوة"],
                                "videoUrl": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                                "postUrl": "https://youtube.com/shorts/kL92jF_01qA",
                                "viewCount": 1840000,
                                "likeCount": 142000,
                                "shareCount": 19500,
                                "viralVelocity": 8.2,
                                "sentimentScore": 0.96,
                                "viralReasonAr": "تريند تجارب الشورتس: حصد 1.8M مشاهدة ومقارنة تفوق على قهوة المقاهي العالمية بخصم 21%.",
                                "viralReasonEn": "YouTube Shorts tasting challenge: Outperforming specialty cafe beans with high consumer demand."
                            },
                            {
                                "id": "viral_x_104",
                                "productId": 7,
                                "productNameAr": "سماعات لاسلكية عازلة للضوضاء برو نانو",
                                "productNameEn": "Wireless Noise Cancelling Pro Earbuds",
                                "category": "Electronics",
                                "retailPrice": 49.99,
                                "originalPrice": 75.00,
                                "imageUrl": "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=500",
                                "platform": "X",
                                "creatorHandle": "@tech_insider_arabia",
                                "creatorName": "أخبار التقنية والعروض",
                                "creatorAvatarUrl": "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120",
                                "postCaption": "ثيرد المقارنة: جربنا 5 سماعات تحت 200 ريال، وهذه السماعة اكتسحت في عزل الصوت والمايك أثناء المكالمات! كود خصم عامر شغال عليها حالياً 🎧⚡ #ترند_التقنية #تويتر",
                                "hashtags": ["#تقنية", "#سماعات", "#عروض_اليوم", "#اكس", "#توفير"],
                                "videoUrl": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                                "postUrl": "https://x.com/tech_insider_arabia/status/1789230192",
                                "viewCount": 980000,
                                "likeCount": 67000,
                                "shareCount": 14300,
                                "viralVelocity": 7.9,
                                "sentimentScore": 0.88,
                                "viralReasonAr": "تغريدة فيروسية على إكس (تويتر): إشادة قوية بجودة العزل الصوتي مقارنة بالسعر وتداول واسع للكود.",
                                "viralReasonEn": "Viral X/Twitter thread: High engagement praising active noise cancellation value for money."
                            },
                            {
                                "id": "viral_tiktok_105",
                                "productId": 10,
                                "productNameAr": "سيروم الهيالورونيك وفيتامين سي لنضارة البشرة",
                                "productNameEn": "Hyaluronic & Vitamin C Glowing Skin Serum",
                                "category": "Pharmacy & Medical",
                                "retailPrice": 18.50,
                                "originalPrice": 26.00,
                                "imageUrl": "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=500",
                                "platform": "TikTok",
                                "creatorHandle": "@glow_with_nour",
                                "creatorName": "نور - عناية وبشرة زجاجية",
                                "creatorAvatarUrl": "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=120",
                                "postCaption": "روتين الـ Glass Skin الكوري بأقل من نصف التكلفة! ✨ النتيجة بعد 7 أيام واضحة جداً وترطيب فوري بدون أي ملمس دهني. #تيك_توك #عناية_بالبشرة #نضارة",
                                "hashtags": ["#عناية_بالبشرة", "#تريند_الجمال", "#بشرة_صحية", "#تيكتوك", "#سيروم"],
                                "videoUrl": "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                                "postUrl": "https://tiktok.com/@glow_with_nour/video/7402910481",
                                "viewCount": 4200000,
                                "likeCount": 312000,
                                "shareCount": 51000,
                                "viralVelocity": 9.9,
                                "sentimentScore": 0.95,
                                "viralReasonAr": "تريند الجمال الأول: تحدي نضارة البشرة خلال 7 أيام حقق 4.2 مليون مشاهدة وتكرار طلبات مرتفع.",
                                "viralReasonEn": "Top beauty trend: 7-day glass skin challenge generated 4.2M views with intense repeat purchases."
                            }
                        ]
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
