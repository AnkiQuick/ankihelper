package com.lmyby.ankiquicker.data.ai

import com.lmyby.ankiquicker.MyApplication
import com.lmyby.ankiquicker.data.ai.cache.AIDictionaryCache
import com.lmyby.ankiquicker.data.ai.service.AIDictionaryService
import com.lmyby.ankiquicker.data.ai.service.AITranslatorService
import java.io.IOException

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class AIManager private constructor() {
    private val dictionaryService: AIDictionaryService = AIDictionaryService()
    private val translatorService: AITranslatorService = AITranslatorService()

    @Throws(IOException::class, AIException::class)
    fun getWordDefinition(word: String, dictionaryConfigId: Long): List<AIDictionaryCache> {
        // Check network connectivity
        if (!NetworkUtil.isNetworkAvailable(MyApplication.getContext())) {
            throw IOException("No network connection. Please check your internet connection and try again.")
        }

        val dictionaryConfig = AIConfigRepository.getAIDictionaryConfigById(dictionaryConfigId)
            ?: throw IOException("AI Dictionary configuration not found")

        val llmConfig = AIConfigRepository.getLLMConfigById(dictionaryConfig.llmId)
            ?: throw IOException("LLM configuration not found")

        return dictionaryService.getWordDefinition(word, dictionaryConfig, llmConfig)
    }

    @Throws(IOException::class, AIException::class)
    fun translateText(text: String, translatorConfigId: Long): String {
        // Check network connectivity
        if (!NetworkUtil.isNetworkAvailable(MyApplication.getContext())) {
            throw IOException("No network connection. Please check your internet connection and try again.")
        }

        val translatorConfig = AIConfigRepository.getAITranslatorConfigById(translatorConfigId)
            ?: throw IOException("AI Translator configuration not found")

        val llmConfig = AIConfigRepository.getLLMConfigById(translatorConfig.llmId)
            ?: throw IOException("LLM configuration not found")

        return translatorService.translateText(
            text,
            translatorConfig.sourceLanguage ?: "auto",
            translatorConfig.targetLanguage ?: "en",
            translatorConfig,
            llmConfig
        )
    }

    @Throws(IOException::class, AIException::class)
    fun translateTextWithDefaultTranslator(text: String): String {
        // Check network connectivity
        if (!NetworkUtil.isNetworkAvailable(MyApplication.getContext())) {
            throw IOException("No network connection. Please check your internet connection and try again.")
        }

        val translatorConfig = AIConfigRepository.getDefaultAITranslatorConfig()
            ?: throw IOException("No default AI Translator configuration found")

        val llmConfig = AIConfigRepository.getLLMConfigById(translatorConfig.llmId)
            ?: throw IOException("LLM configuration not found")

        return translatorService.translateText(
            text,
            translatorConfig.sourceLanguage ?: "auto",
            translatorConfig.targetLanguage ?: "en",
            translatorConfig,
            llmConfig
        )
    }

    companion object {
        @Volatile
        private var instance: AIManager? = null

        @JvmStatic
        fun getInstance(): AIManager {
            return instance ?: synchronized(this) {
                instance ?: AIManager().also { instance = it }
            }
        }
    }
}
