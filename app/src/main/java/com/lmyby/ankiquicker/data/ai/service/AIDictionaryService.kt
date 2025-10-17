package com.lmyby.ankiquicker.data.ai.service

import android.util.Log
import com.lmyby.ankiquicker.data.ai.AIDictionaryConfig
import com.lmyby.ankiquicker.data.ai.AIErrorType
import com.lmyby.ankiquicker.data.ai.AIException
import com.lmyby.ankiquicker.data.ai.LLMConfig
import com.lmyby.ankiquicker.data.ai.cache.AICacheRepository
import com.lmyby.ankiquicker.data.ai.cache.AIDictionaryCache
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.math.min

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class AIDictionaryService {
    private val aiService: AIService = AIService()

    @Throws(IOException::class, AIException::class)
    fun getWordDefinition(word: String, config: AIDictionaryConfig, llmConfig: LLMConfig): List<AIDictionaryCache> {
        Log.d(TAG, "Starting word definition lookup for: $word")

        try {
            // Check cache first
            val cachedResults = AICacheRepository.getDictionaryCache(word, llmConfig.id)
            if (cachedResults.isNotEmpty()) {
                Log.d(TAG, "Returning cached results for word: $word, count: ${cachedResults.size}")
                return cachedResults
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking cache for word: $word, continuing with LLM call", e)
        }

        Log.d(TAG, "No cached results found for word: $word, calling LLM")

        // Get language names from codes
        val sourceLanguage = config.sourceLanguage ?: "en"
        val targetLanguage = config.targetLanguage ?: "zh"
        val sourceLanguageName = getLanguageName(sourceLanguage)
        val targetLanguageName = getLanguageName(targetLanguage)

        Log.d(
            TAG,
            "Using languages: source=$sourceLanguageName ($sourceLanguage), " +
                "target=$targetLanguageName ($targetLanguage)"
        )

        // Prepare the system and user messages with configurable languages
        val systemMessage = "You are an experienced dictionary assistant. " +
                "Your task is to provide accurate and comprehensive definitions for words " +
                "and phrases in $sourceLanguageName. " +
                "You should provide translations and explanations in $targetLanguageName. " +
                "You should be able to handle complex queries and provide detailed explanations. " +
                "Your responses should be clear, concise, and easy to understand. " +
                "IMPORTANT: You MUST respond with valid JSON format. " +
                "Your response should be a JSON object with a 'definitions' array " +
                "containing definition objects. " +
                "Each definition object should have: 'headword', 'phrase', 'sense', " +
                "'phonetics', 'def_en', 'def_cn', and 'example' fields. " +
                "headword: the key word to look up in $sourceLanguageName. " +
                "phrase: the phrase that the word belongs to. " +
                "If not empty, the definitions will be for the entire phrase. " +
                "sense: the Part of Speech (grammatical category), such as nouns, verbs, " +
                "adjectives, adverbs, pronouns, prepositions, conjunctions, and interjections. " +
                "phonetics: contains phonetic transcription " +
                "(e.g., 'UK/kaɪnd/ US/kaɪnd/' for English words). " +
                "def_en: definition in $sourceLanguageName. " +
                "def_cn: definition/translation in $targetLanguageName. " +
                "example: example sentence in $sourceLanguageName."

        val userMessage = "Please provide the definitions of the word or phrase \"$word\" " +
                "(in $sourceLanguageName) in JSON format with a 'definitions' array " +
                "containing definition objects. " +
                "Each definition should have: 'headword', 'phrase', 'sense', 'phonetics', " +
                "'def_en' ($sourceLanguageName definition), " +
                "'def_cn' ($targetLanguageName translation), and 'example' fields."

        Log.d(TAG, "Calling LLM with system message: $systemMessage")
        Log.d(TAG, "Calling LLM with user message: $userMessage")

        // Call the LLM with system and user messages
        val response = aiService.callLLM(llmConfig, systemMessage, userMessage)

        Log.d(TAG, "Received response from LLM: $response")

        // Parse the response
        val results = parseDictionaryResponse(response, word, llmConfig.id)

        Log.d(TAG, "Parsed ${results.size} results from response")

        try {
            // Cache the results
            for (result in results) {
                result.timestamp = System.currentTimeMillis()
                AICacheRepository.saveDictionaryCache(result)
            }
            Log.d(TAG, "Saved ${results.size} results to cache")
        } catch (e: Exception) {
            Log.w(TAG, "Error saving results to cache for word: $word, continuing without caching", e)
        }

        return results
    }

    @Throws(IOException::class, AIException::class)
    private fun parseDictionaryResponse(response: String, word: String, llmConfigId: Long): List<AIDictionaryCache> {
        val results = mutableListOf<AIDictionaryCache>()

        try {
            Log.d(TAG, "Attempting to parse LLM response: $response")

            // Try to parse the JSON response
            val jsonResponse = JSONObject(response)

            // Check if response is an error
            if (jsonResponse.has("error")) {
                val errorObj = jsonResponse.getJSONObject("error")
                handleErrorResponse(errorObj)
                return results // Should not reach here as handleErrorResponse throws exception
            }

            // Handle different response formats
            when {
                jsonResponse.has("choices") -> {
                    // Standard OpenAI-style response
                    val choices = jsonResponse.getJSONArray("choices")
                    if (choices.length() > 0) {
                        val choice = choices.getJSONObject(0)
                        val content = when {
                            choice.has("message") -> {
                                val message = choice.getJSONObject("message")
                                message.getString("content")
                            }
                            choice.has("text") -> choice.getString("text")
                            else -> choice.toString()
                        }

                        Log.d(TAG, "Extracted content from LLM response: $content")
                        parseContent(content, results, word, llmConfigId)
                    }
                }
                jsonResponse.has("content") -> {
                    // Direct content response
                    val content = jsonResponse.getString("content")
                    parseContent(content, results, word, llmConfigId)
                }
                jsonResponse.has("text") -> {
                    // Direct text response
                    val content = jsonResponse.getString("text")
                    parseContent(content, results, word, llmConfigId)
                }
                else -> {
                    // Try to parse the entire response as content
                    parseContent(response, results, word, llmConfigId)
                }
            }
        } catch (e: AIException) {
            throw e // Re-throw AI exceptions
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing dictionary response. Raw response: $response", e)
            // Try to create a basic result from the raw response
            try {
                val cache = AIDictionaryCache().apply {
                    hwd = word
                    defEn = "Response from AI: ${response.substring(0, min(200, response.length))}"
                    this.llmConfigId = llmConfigId
                    timestamp = System.currentTimeMillis()
                }
                results.add(cache)
                Log.w(TAG, "Created fallback result from raw response")
            } catch (fallbackE: Exception) {
                Log.e(TAG, "Error creating fallback result", fallbackE)
            }
            throw AIException(
                AIErrorType.INVALID_RESPONSE,
                "Error parsing dictionary response. Raw response: $response",
                e
            )
        }

        Log.d(TAG, "Successfully parsed ${results.size} results")
        return results
    }

    @Throws(Exception::class)
    private fun parseContent(content: String, results: MutableList<AIDictionaryCache>, word: String, llmConfigId: Long) {
        Log.d(TAG, "Parsing content: $content")

        if (content.trim().isEmpty()) {
            Log.w(TAG, "Empty content received from LLM")
            return
        }

        // Clean up markdown formatting if present
        val cleanedContent = cleanMarkdownFormatting(content)
        Log.d(TAG, "Cleaned content: $cleanedContent")

        // Try to parse content as JSON
        try {
            // First try to parse as JSON object
            val contentJson = JSONObject(cleanedContent)
            Log.d(TAG, "Successfully parsed content as JSON object. Keys: ${contentJson.keys()}")

            when {
                contentJson.has("definitions") -> {
                    // Handle the case where content contains a definitions array
                    val definitions = contentJson.getJSONArray("definitions")
                    Log.d(TAG, "Found definitions array with ${definitions.length()} items")
                    parseDefinitionsArray(definitions, results, word, llmConfigId)
                    Log.d(TAG, "Parsed ${definitions.length()} definitions from definitions array")
                }
                contentJson.has("headword") || contentJson.has("def_en") || contentJson.has("defEn") -> {
                    // Check if it's a single definition object
                    val definitions = JSONArray().apply { put(contentJson) }
                    parseDefinitionsArray(definitions, results, word, llmConfigId)
                    Log.d(TAG, "Parsed single definition object")
                }
                else -> {
                    // Treat as a generic JSON object and convert to string definition
                    val cache = AIDictionaryCache().apply {
                        hwd = word
                        defEn = contentJson.toString()
                        this.llmConfigId = llmConfigId
                        timestamp = System.currentTimeMillis()
                    }
                    results.add(cache)
                    Log.d(TAG, "Treated content as generic JSON object")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Content is not a JSON object, trying as JSON array", e)
            // If not JSON object, try as JSON array
            try {
                val contentArray = JSONArray(cleanedContent)
                Log.d(TAG, "Successfully parsed content as JSON array with ${contentArray.length()} items")
                parseDefinitionsArray(contentArray, results, word, llmConfigId)
                Log.d(TAG, "Parsed ${contentArray.length()} definitions from JSON array")
            } catch (arrayE: Exception) {
                Log.d(TAG, "Content is not a JSON array, treating as plain text", arrayE)
                // If not JSON at all, treat as plain text
                Log.d(TAG, "Treating content as plain text")
                val cache = AIDictionaryCache().apply {
                    hwd = word
                    defEn = cleanedContent
                    this.llmConfigId = llmConfigId
                    timestamp = System.currentTimeMillis()
                }
                results.add(cache)
            }
        }
    }

    /**
     * Cleans up markdown formatting from LLM responses
     * Removes leading/trailing whitespace, markdown code block markers, and other formatting
     *
     * @param content The raw content from LLM
     * @return Cleaned content ready for JSON parsing
     */
    private fun cleanMarkdownFormatting(content: String?): String {
        if (content.isNullOrEmpty()) {
            return content ?: ""
        }

        var cleaned = content.trim()

        // Remove leading word if it's just the search term (common with some LLMs)
        if (cleaned.startsWith("Watch")) {
            // Check if the rest is JSON or markdown-wrapped JSON
            val rest = cleaned.substring(5).trim()
            if (rest.startsWith("```json") || rest.startsWith("```")) {
                cleaned = rest
            }
        }

        // Remove markdown code block markers
        when {
            cleaned.startsWith("```json") -> cleaned = cleaned.substring(7) // Remove ```json
            cleaned.startsWith("```") -> cleaned = cleaned.substring(3) // Remove ```
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length - 3) // Remove trailing ```
        }

        // Remove any remaining leading/trailing whitespace
        cleaned = cleaned.trim()

        Log.d(TAG, "Cleaned markdown formatting. Original: $content | Cleaned: $cleaned")

        return cleaned
    }

    @Throws(Exception::class)
    private fun parseDefinitionsArray(
        definitions: JSONArray,
        results: MutableList<AIDictionaryCache>,
        word: String,
        llmConfigId: Long
    ) {
        for (i in 0 until definitions.length()) {
            val definition = definitions.getJSONObject(i)

            val cache = AIDictionaryCache().apply {
                hwd = definition.optString("headword", word)
                phrase = definition.optString("phrase", "")
                sense = definition.optString("sense", "")
                phonetics = definition.optString("phonetics", "")
                defEn = definition.optString("def_en", definition.optString("defEn", ""))
                defCn = definition.optString("def_cn", definition.optString("defCn", ""))
                example = definition.optString("example", "")
                this.llmConfigId = llmConfigId
            }

            results.add(cache)
        }
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
        return LANGUAGE_NAMES[languageCode.lowercase()] ?: languageCode.uppercase()
    }

    companion object {
        private const val TAG = "AIDictionaryService"

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
