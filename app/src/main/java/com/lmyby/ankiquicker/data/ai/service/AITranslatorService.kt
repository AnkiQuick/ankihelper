package com.lmyby.ankiquicker.data.ai.service

import android.util.Log
import com.lmyby.ankiquicker.data.ai.AIErrorType
import com.lmyby.ankiquicker.data.ai.AIException
import com.lmyby.ankiquicker.data.ai.AITranslatorConfig
import com.lmyby.ankiquicker.data.ai.LLMConfig
import com.lmyby.ankiquicker.data.ai.cache.AICacheRepository
import com.lmyby.ankiquicker.data.ai.cache.AITranslatorCache
import org.json.JSONObject
import java.io.IOException

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class AITranslatorService {
    private val aiService: AIService = AIService()

    @Throws(IOException::class, AIException::class)
    fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        config: AITranslatorConfig,
        llmConfig: LLMConfig
    ): String {
        // Check cache first
        val cachedResult = AICacheRepository.getTranslatorCache(text, sourceLanguage, targetLanguage, llmConfig.id)
        if (cachedResult != null) {
            Log.d(TAG, "Returning cached translation for text: $text")
            return cachedResult.translatedText
        }

        // Convert language codes to full names for better LLM understanding
        val sourceLanguageName = getLanguageName(sourceLanguage)
        val targetLanguageName = getLanguageName(targetLanguage)

        Log.d(TAG, "Translating from $sourceLanguageName ($sourceLanguage) to $targetLanguageName ($targetLanguage)")

        // Prepare the system and user messages with full language names
        val systemMessage = "You are an experienced translator. " +
                "Your task is to translate text from $sourceLanguageName to $targetLanguageName " +
                "accurately and fluently. You should be able to handle complex sentences and idioms. " +
                "Your responses should be clear, concise, and easy to understand. " +
                "Key Points: Accuracy is Paramount; Fluent and Natural Writing; Standardized Terminology. " +
                "IMPORTANT: You MUST respond with valid JSON format. " +
                "Your response should be a JSON object with a 'translation' map containing " +
                "a translation object, which contains: " +
                "translatedText (the translated text in $targetLanguageName), " +
                "sourceLanguage (language code: $sourceLanguage), " +
                "targetLanguage (language code: $targetLanguage)."

        val userMessage = "Please provide the translation of the following text " +
                "from $sourceLanguageName to $targetLanguageName: \"$text\""

        // Call the LLM with system and user messages
        val response = aiService.callLLM(llmConfig, systemMessage, userMessage)
        Log.e(TAG, "Raw LLM response for translation: $response")

        // Parse the response
        val parsedResponse = parseTranslationResponse(response)
        val translatedText = parsedResponse.optString("translatedText", text)
        val parsedSourceLanguage = parsedResponse.optString("sourceLanguage", sourceLanguage)
        val parsedTargetLanguage = parsedResponse.optString("targetLanguage", targetLanguage)

        // Cache the result
        val cache = AITranslatorCache().apply {
            sourceText = text
            this.sourceLanguage = parsedSourceLanguage
            this.targetLanguage = parsedTargetLanguage
            this.translatedText = translatedText
            llmConfigId = llmConfig.id
            timestamp = System.currentTimeMillis()
        }
        AICacheRepository.saveTranslatorCache(cache)

        return translatedText
    }

    @Throws(IOException::class, AIException::class)
    private fun parseTranslationResponse(response: String): JSONObject {
        var translationObject = JSONObject()
        try {
            // Clean up markdown formatting if present
            Log.d(TAG, "Attempting to parse LLM response: $response")

            // Try to parse the JSON response
            val jsonResponse = JSONObject(response)

            // Check if response is an error
            if (jsonResponse.has("error")) {
                val errorObj = jsonResponse.getJSONObject("error")
                handleErrorResponse(errorObj)
                return translationObject // Should not reach here as handleErrorResponse throws exception
            }

            when {
                jsonResponse.has("choices") -> {
                    val choices = jsonResponse.getJSONArray("choices")
                    if (choices.length() > 0) {
                        val choice = choices.getJSONObject(0)
                        if (choice.has("message")) {
                            val message = choice.getJSONObject("message")
                            if (message.has("content")) {
                                val content = message.getString("content")
                                val cleanedContent = cleanMarkdownFormatting(content)
                                val cleanedContentObject = JSONObject(cleanedContent)
                                if (cleanedContentObject.has("translation")) {
                                    translationObject = cleanedContentObject.getJSONObject("translation")
                                }
                            }
                        }
                    }
                }
                jsonResponse.has("content") -> {
                    // Direct content response
                    val content = jsonResponse.getString("content")
                    val cleanedContent = cleanMarkdownFormatting(content)
                    val cleanedContentObject = JSONObject(cleanedContent)
                    if (cleanedContentObject.has("translation")) {
                        translationObject = cleanedContentObject.getJSONObject("translation")
                    }
                }
                jsonResponse.has("text") -> {
                    // Direct text response
                    val content = jsonResponse.getString("text")
                    val cleanedContent = cleanMarkdownFormatting(content)
                    val cleanedContentObject = JSONObject(cleanedContent)
                    if (cleanedContentObject.has("translation")) {
                        translationObject = cleanedContentObject.getJSONObject("translation")
                    }
                }
                else -> {
                    // Try to parse the entire response as content
                    translationObject = jsonResponse.getJSONObject("translation")
                }
            }
            return translationObject
        } catch (e: AIException) {
            throw e // Re-throw AI exceptions
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing translation response", e)
            throw AIException(AIErrorType.INVALID_RESPONSE, "Error parsing translation response", e)
        }
    }

    private fun cleanMarkdownFormatting(content: String?): String {
        if (content.isNullOrEmpty()) {
            return content ?: ""
        }

        var cleaned = content.trim()

        // Remove markdown code block markers
        when {
            cleaned.startsWith("```json") -> cleaned = cleaned.substring(7) // Remove ```json
            cleaned.startsWith("```") -> cleaned = cleaned.substring(3) // Remove ```
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length - 3) // Remove trailing ```
        }

        // Handle case where content is wrapped in triple quotes ("""json""" or just """)
        if (cleaned.startsWith("\"\"\"") && cleaned.endsWith("\"\"\"")) {
            cleaned = cleaned.substring(3, cleaned.length - 3) // Remove surrounding triple quotes
        }

        // Remove any remaining leading/trailing whitespace
        cleaned = cleaned.trim()

        return cleaned
    }

    @Throws(AIException::class)
    private fun handleErrorResponse(errorObj: JSONObject) {
        val errorType = errorObj.optString("type", "unknown")
        val errorMessage = errorObj.optString("message", "Unknown error")

        val type = try {
            AIErrorType.valueOf(errorType.uppercase())
        } catch (e: IllegalArgumentException) {
            AIErrorType.UNKNOWN
        }

        throw AIException(type, errorMessage)
    }

    /**
     * Convert language code to full language name
     */
    private fun getLanguageName(languageCode: String): String {
        // Handle "auto" for automatic language detection
        if (languageCode.lowercase() == "auto") {
            return "auto-detect"
        }

        return LANGUAGE_NAMES[languageCode.lowercase()] ?: languageCode.uppercase()
    }

    companion object {
        private const val TAG = "AITranslatorService"

        private val LANGUAGE_NAMES = mapOf(
            "en" to "English",
            "zh" to "Chinese",
            "ja" to "Japanese",
            "ko" to "Korean",
            "fr" to "French",
            "de" to "German",
            "es" to "Spanish",
            "it" to "Italian",
            "pt" to "Portuguese",
            "ru" to "Russian",
            "ar" to "Arabic",
            "hi" to "Hindi"
        )
    }
}
