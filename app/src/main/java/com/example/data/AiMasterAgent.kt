package com.example.data

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AiToolType(val displayNameAr: String, val displayNameEn: String, val model: String) {
    VOICE_LIVE("محادثة صوتية حية (Live Voice)", "Live Voice Conversations", "gemini-3.1-flash-live-preview"),
    HIGH_THINKING("تفكير عميق واستنتاج متقدم", "High Thinking Mode", "gemini-3.1-pro-preview"),
    IMAGE_STUDIO("توليد وتعديل الصور بدقة ونسب مخصصة", "Image Studio & Aspect Ratios", "gemini-3-pro-image-preview / gemini-3.1-flash-image-preview"),
    VEO_VIDEO("توليد فيديو وتحريك الصور (Veo)", "Veo Video Generation & Image Animation", "veo-3.1-fast-generate-preview"),
    LYRIA_MUSIC("تأليف وتوليد الموسيقى (Lyria)", "Lyria Music Generation", "lyria-3-clip-preview / lyria-3-pro-preview"),
    SEARCH_GROUNDING("بحث جوجل المباشر الموثق", "Google Search Grounding", "gemini-3.5-flash"),
    MAPS_GROUNDING("خرائط ومواقع المتاجر الموثقة", "Google Maps Grounding", "gemini-3.5-flash"),
    IMAGE_ANALYSIS("تحليل وفهم الصور المتقدم", "Image Understanding", "gemini-3.1-pro-preview"),
    VIDEO_ANALYSIS("تحليل وفهم محتوى الفيديو", "Video Understanding", "gemini-3.1-pro-preview"),
    AUDIO_TRANSCRIPTION("تفريغ الصوت إلى نصوص بدقة", "Audio Transcription", "gemini-3.5-flash"),
    FAST_LITE("استجابة فائقة السرعة", "Low-Latency Fast Mode", "gemini-3.1-flash-lite"),
    CHATBOT("مساعد محادثة متعدد الأدوار", "Multi-turn Role Chatbot", "gemini-3.5-flash / gemini-3.1-pro-preview")
}

data class AiAgentActionRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val toolType: AiToolType,
    val prompt: String,
    val resultText: String,
    val mediaUrlOrData: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val appliedToFeature: String = "Global Market Assistant"
)

object AiMasterAgentOrchestrator {
    private const val TAG = "AiMasterAgent"

    private val _agentActivityHistory = MutableStateFlow<List<AiAgentActionRecord>>(emptyList())
    val agentActivityHistory: StateFlow<List<AiAgentActionRecord>> = _agentActivityHistory.asStateFlow()

    private val _isAgentBusy = MutableStateFlow(false)
    val isAgentBusy: StateFlow<Boolean> = _isAgentBusy.asStateFlow()

    private val _currentActiveTool = MutableStateFlow<AiToolType?>(null)
    val currentActiveTool: StateFlow<AiToolType?> = _currentActiveTool.asStateFlow()

    /**
     * Master Agent Entry Point: Intelligent Intent Router that executes the appropriate AI tool
     */
    suspend fun executeMasterAgentRequest(
        userPrompt: String,
        explicitTool: AiToolType? = null,
        bitmap: Bitmap? = null,
        aspectRatio: String = "1:1",
        imageResolution: String = "1K",
        isPro: Boolean = false,
        videoAspect: String = "16:9",
        appliedFeature: String = "E-Commerce Market Co-Pilot"
    ): AiAgentActionRecord {
        _isAgentBusy.value = true
        val resolvedTool = explicitTool ?: resolveToolFromPrompt(userPrompt, bitmap != null)
        _currentActiveTool.value = resolvedTool

        val resultText = try {
            when (resolvedTool) {
                AiToolType.HIGH_THINKING -> {
                    ModelService.executeHighThinkingReasoning(userPrompt)
                }
                AiToolType.IMAGE_STUDIO -> {
                    ModelService.generateOrEditImage(
                        prompt = userPrompt,
                        aspectRatio = aspectRatio,
                        imageSize = imageResolution,
                        isPro = isPro,
                        sourceBitmap = bitmap
                    )
                }
                AiToolType.VEO_VIDEO -> {
                    ModelService.generateVideoWithVeo(
                        prompt = userPrompt,
                        aspectRatio = videoAspect,
                        imageBitmap = bitmap
                    )
                }
                AiToolType.LYRIA_MUSIC -> {
                    ModelService.generateMusic(
                        prompt = userPrompt,
                        isPro = isPro
                    )
                }
                AiToolType.SEARCH_GROUNDING -> {
                    ModelService.queryWithGoogleSearchGrounding(userPrompt)
                }
                AiToolType.MAPS_GROUNDING -> {
                    ModelService.queryWithGoogleMapsGrounding(userPrompt)
                }
                AiToolType.IMAGE_ANALYSIS -> {
                    if (bitmap != null) {
                        ModelService.analyzeImageWithPro(bitmap, userPrompt)
                    } else {
                        ModelService.generateAiWithModel(userPrompt, model = "gemini-3.1-pro-preview")
                    }
                }
                AiToolType.VIDEO_ANALYSIS -> {
                    ModelService.analyzeVideo("content://sample_market_video.mp4", userPrompt)
                }
                AiToolType.AUDIO_TRANSCRIPTION -> {
                    ModelService.transcribeAudio(userPrompt)
                }
                AiToolType.FAST_LITE -> {
                    ModelService.executeFastLiteQuery(userPrompt)
                }
                AiToolType.VOICE_LIVE -> {
                    ModelService.liveVoiceConversationTurn(userPrompt, emptyList())
                }
                AiToolType.CHATBOT -> {
                    ModelService.generateAiContent(userPrompt)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Master Agent execution error for $resolvedTool", e)
            "Error executing ${resolvedTool.name}: ${e.message}"
        } finally {
            _isAgentBusy.value = false
            _currentActiveTool.value = null
        }

        val record = AiAgentActionRecord(
            toolType = resolvedTool,
            prompt = userPrompt,
            resultText = resultText,
            appliedToFeature = appliedFeature
        )

        _agentActivityHistory.value = listOf(record) + _agentActivityHistory.value
        return record
    }

    private fun resolveToolFromPrompt(prompt: String, hasImage: Boolean): AiToolType {
        val lower = prompt.lowercase()
        return when {
            hasImage && (lower.contains("video") || lower.contains("تحريك") || lower.contains("انيميشن") || lower.contains("animate")) -> AiToolType.VEO_VIDEO
            hasImage -> AiToolType.IMAGE_ANALYSIS
            lower.contains("music") || lower.contains("موسيقى") || lower.contains("لحن") || lower.contains("نغمة") || lower.contains("lyria") || lower.contains("song") -> AiToolType.LYRIA_MUSIC
            lower.contains("video") || lower.contains("فيديو") || lower.contains("veo") || lower.contains("movie") || lower.contains("clip") -> AiToolType.VEO_VIDEO
            lower.contains("صورة") || lower.contains("image") || lower.contains("draw") || lower.contains("رسم") || lower.contains("صمم") || lower.contains("poster") -> AiToolType.IMAGE_STUDIO
            lower.contains("فكر") || lower.contains("think") || lower.contains("تحليل عميق") || lower.contains("استنتاج") || lower.contains("دراسة جدوى") -> AiToolType.HIGH_THINKING
            lower.contains("موقع") || lower.contains("خريطة") || lower.contains("map") || lower.contains("متجر قريب") || lower.contains("عنوان") || lower.contains("near me") -> AiToolType.MAPS_GROUNDING
            lower.contains("بحث") || lower.contains("search") || lower.contains("جوجل") || lower.contains("سعر اليوم") || lower.contains("أخبار") || lower.contains("latest") -> AiToolType.SEARCH_GROUNDING
            lower.contains("صوت") || lower.contains("تحدث") || lower.contains("voice") || lower.contains("محادثة صوتية") || lower.contains("live") -> AiToolType.VOICE_LIVE
            lower.contains("تفريغ") || lower.contains("transcribe") || lower.contains("نص صوتي") || lower.contains("تسجيل") -> AiToolType.AUDIO_TRANSCRIPTION
            lower.contains("سريع") || lower.contains("fast") || lower.contains("lite") || lower.contains("فوري") -> AiToolType.FAST_LITE
            else -> AiToolType.CHATBOT
        }
    }
}
