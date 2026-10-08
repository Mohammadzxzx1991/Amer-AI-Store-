package com.example.data.visionx

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.ProductEntity
import com.example.data.agent.AgentEngine
import com.example.data.agent.AgentKey
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Product & Lifestyle Creative Share Manager
 * Supports:
 * 1. Deep link & product sharing for Smart Wishlist items.
 * 2. Lifestyle image export to Gallery / MediaStore with high-res PNG/JPEG encoding.
 * 3. Direct social media export & sharing (WhatsApp, Instagram, Twitter/X, Telegram, Facebook, and system share sheet).
 */
object ProductShareManager {

    private const val TAG = "ProductShareManager"

    /**
     * Generates a structured deep link for a product.
     */
    fun generateProductDeepLink(product: ProductEntity): String {
        val safeName = product.name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
        return "https://amer.ai/store/item?id=${product.id}&slug=$safeName&ref=wishlist_share"
    }

    /**
     * Launches the Android system share sheet to share product details and deep link to social media.
     */
    fun shareProductToSocialMedia(context: Context, product: ProductEntity) {
        val deepLink = generateProductDeepLink(product)
        val shareMessage = buildString {
            append("🛒 منتج مميز من أسواق جوافة (Guava Mall):\n\n")
            append("📦 *${product.name}*\n")
            append("💰 السعر: $${String.format("%.2f", product.retailPrice)}\n")
            append("🏷️ التصنيف: ${product.category}\n\n")
            append("🔗 رابط المنتج المعزز:\n$deepLink\n\n")
            append("✨ تسوق بذكاء مع أسواق جوافة!")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "مشاركة منتج: ${product.name}")
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }

        val chooser = Intent.createChooser(intent, "مشاركة المنتج عبر:")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)

        // Record audit evidence in 19-Agent Governance Ledger
        AgentEngine.recordEvidence(
            operationId = "SHARE_PROD_${product.id}_" + UUID.randomUUID().toString().take(4),
            actorId = AgentKey.MARKETPLACE_MERCHANT_SUCCESS.key,
            type = "PRODUCT_WISHLIST_SOCIAL_SHARE",
            source = "ProductShareManager Android Intent",
            payloadSummary = "Shared saved product '${product.name}' (ID: ${product.id}) with deep link: $deepLink"
        )
    }

    /**
     * Exports a drawable resource bitmap into the device's public Pictures / MediaStore gallery.
     */
    fun exportLifestyleImageToGallery(
        context: Context,
        drawableResId: Int,
        productName: String
    ): Uri? {
        return try {
            val bitmap = BitmapFactory.decodeResource(context.resources, drawableResId) ?: return null
            val filename = "AmerAI_Lifestyle_${System.currentTimeMillis()}.jpg"

            var outputUri: Uri? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AmerAIStore")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    outputUri = uri
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val amerDir = File(picturesDir, "AmerAIStore").apply { if (!exists()) mkdirs() }
                val imageFile = File(amerDir, filename)
                FileOutputStream(imageFile).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                }
                outputUri = Uri.fromFile(imageFile)
            }

            AgentEngine.recordEvidence(
                operationId = "EXPORT_IMG_" + UUID.randomUUID().toString().take(6),
                actorId = AgentKey.EXECUTION_AGENT.key,
                type = "LIFESTYLE_IMAGE_GALLERY_EXPORT",
                source = "ProductShareManager MediaStore",
                payloadSummary = "Exported lifestyle image for '$productName' to user Gallery (URI: $outputUri)"
            )

            outputUri
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export image to gallery: ${e.message}")
            null
        }
    }

    /**
     * Shares an AI-generated lifestyle image directly or via system chooser to social media.
     * Extracts the image to a FileProvider cache URI and builds ACTION_SEND with image/jpeg.
     */
    fun shareLifestyleImageToSocialMedia(
        context: Context,
        drawableResId: Int,
        productName: String,
        settingTitle: String,
        prompt: String,
        targetPackage: String? = null
    ) {
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, drawableResId)
            if (bitmap == null) {
                Toast.makeText(context, "تعذر تحميل الصورة للمشاركة", Toast.LENGTH_SHORT).show()
                return
            }

            // Save to cached files folder for FileProvider
            val imagesFolder = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
            val file = File(imagesFolder, "lifestyle_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, fos)
            fos.flush()
            fos.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareCaption = buildString {
                append("✨ مشهد عصري استثنائي تم توليده بالذكاء الاصطناعي عبر أسواق جوافة (Guava Mall)!\n\n")
                append("🛍️ المنتج: $productName\n")
                append("🎨 البيئة الافتراضية: $settingTitle\n")
                append("💡 الوصف الإبداعي: $prompt\n\n")
                append("🚀 استكشف التسوق الذكي مع أسواق جوافة: https://guavamall.ai")
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, shareCaption)
                putExtra(Intent.EXTRA_SUBJECT, "أسواق جوافة ستوديو الإبداع: $productName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (!targetPackage.isNullOrEmpty()) {
                    setPackage(targetPackage)
                }
            }

            if (!targetPackage.isNullOrEmpty()) {
                try {
                    val packageManager = context.packageManager
                    val resolveInfo = packageManager.resolveActivity(shareIntent, 0)
                    if (resolveInfo != null) {
                        context.startActivity(shareIntent)
                    } else {
                        // Fall back to system chooser if specific app isn't installed
                        val chooser = Intent.createChooser(shareIntent, "مشاركة المشهد الإعلاني العصري")
                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(chooser)
                    }
                } catch (pe: Exception) {
                    val chooser = Intent.createChooser(shareIntent, "مشاركة المشهد الإعلاني العصري")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(chooser)
                }
            } else {
                val chooser = Intent.createChooser(shareIntent, "مشاركة المشهد الإعلاني العصري")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(chooser)
            }

            AgentEngine.recordEvidence(
                operationId = "SHARE_LIFESTYLE_" + UUID.randomUUID().toString().take(6),
                actorId = AgentKey.MARKETING_AGENT.key,
                type = "LIFESTYLE_IMAGE_SOCIAL_SHARE",
                source = "ProductShareManager FileProvider",
                payloadSummary = "Shared lifestyle image for '$productName' to ${targetPackage ?: "All Social Platforms"}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share lifestyle image: ${e.message}")
            Toast.makeText(context, "حدث خطأ أثناء إعداد الصورة للمشاركة: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
