package com.mmjang.ankihelper.data.ai

import com.mmjang.ankihelper.MyApplication
import com.mmjang.ankihelper.data.ai.cache.AICacheRepository
import com.mmjang.ankihelper.data.database.AppDatabase
import com.mmjang.ankihelper.data.dict.CoroutineHelper

object AIConfigRepository {

    // Default prompts
    const val DEFAULT_DICTIONARY_PROMPT = """You are an experienced dictionary assistant. Your task is to provide accurate and comprehensive definitions for words and phrases. You should be able to handle complex queries and provide detailed explanations. Your responses should be clear, concise, and easy to understand.

Please provide the definitions of the word or phrase "{query}" in the following JSON format:

{
  "definitions": [
    {
      "headword": "headword (the main word being defined)",
      "phrase": "fixed phrase containing the word (only for phrases, empty for single words)",
      "sense": "part of speech (noun, verb, adjective, adverb, etc.) - empty for phrases",
      "phonetics": "British and American English phonetics (format: 'UK: /pronunciation/ US: /pronunciation/')",
      "def_en": "English definition",
      "def_cn": "Chinese definition",
      "example": "example sentence using the word or phrase"
    }
  ]
}

Guidelines:
1. For single words:
   - Provide all meanings/definitions of the word
   - For each word meaning:
     * headword: the word itself
     * phrase: empty (since it's not a phrase)
     * sense: part of speech (noun, verb, etc.)
     * phonetics: both UK and US phonetics (e.g., "UK: /ɡʊd/ US: /ɡʊd/")
     * def_en/def_cn: definitions for this meaning
     * example: example sentence for this meaning

2. For phrases containing the word:
   - If the word is part of common phrases or phrasal verbs, include these as separate entries
   - For each phrase:
     * headword: the original query word
     * phrase: the complete phrase (e.g., "word up", "break word")
     * sense: empty (phrases don't have part of speech)
     * phonetics: empty (phrases don't have phonetics)
     * def_en/def_cn: definitions of the phrase
     * example: example sentence using the phrase

3. Return all definitions and phrases for the given word/phrase
4. Each entry will be stored as a separate row in the cache table"""

    const val DEFAULT_TRANSLATOR_PROMPT = """You are an experienced translator. Your task is to translate text from one language to another accurately and fluently. You should be able to handle complex sentences and idioms. Your responses should be clear, concise, and easy to understand.

Please provide the translation of the text "{text}" from {sourceLanguage} to {targetLanguage} in the following JSON format:

{
  "translated_text": "translated text",
  "source_language": "{sourceLanguage}",
  "target_language": "{targetLanguage}"
}"""

    // Helper method to get database instance
    private fun getDatabase(): AppDatabase {
        return AppDatabase.getInstance(MyApplication.getContext())
    }

    // LLM Config methods
    @JvmStatic
    fun getAllLLMConfigs(): List<LLMConfig> {
        return CoroutineHelper.executeBlocking {
            getDatabase().llmConfigDao().getAllLLMConfigs()
        }
    }

    @JvmStatic
    fun getLLMConfigById(id: Long): LLMConfig? {
        return CoroutineHelper.executeBlocking {
            getDatabase().llmConfigDao().getLLMConfigById(id)
        }
    }

    @JvmStatic
    fun saveLLMConfig(config: LLMConfig) {
        // Encrypt the API token before saving
        if (config.apiToken?.isNotEmpty() == true) {
            config.apiToken = EncryptionUtil.encrypt(config.apiToken)
        }
        CoroutineHelper.executeBlocking {
            if (config.id > 0) {
                getDatabase().llmConfigDao().updateLLMConfig(config)
            } else {
                getDatabase().llmConfigDao().insertLLMConfig(config)
            }
        }
    }

    @JvmStatic
    fun deleteLLMConfig(id: Long) {
        CoroutineHelper.executeBlocking {
            getDatabase().llmConfigDao().deleteLLMConfigById(id)
        }
    }

    // TTS Config methods
    @JvmStatic
    fun getAllTTSConfigs(): List<TTSConfig> {
        return CoroutineHelper.executeBlocking {
            getDatabase().ttsConfigDao().getAllTTSConfigs()
        }
    }

    @JvmStatic
    fun getTTSConfigById(id: Long): TTSConfig? {
        return CoroutineHelper.executeBlocking {
            getDatabase().ttsConfigDao().getTTSConfigById(id)
        }
    }

    @JvmStatic
    fun saveTTSConfig(config: TTSConfig) {
        // Encrypt the API token before saving
        if (config.apiToken?.isNotEmpty() == true) {
            config.apiToken = EncryptionUtil.encrypt(config.apiToken)
        }
        CoroutineHelper.executeBlocking {
            if (config.id > 0) {
                getDatabase().ttsConfigDao().updateTTSConfig(config)
            } else {
                getDatabase().ttsConfigDao().insertTTSConfig(config)
            }
        }
    }

    @JvmStatic
    fun deleteTTSConfig(id: Long) {
        CoroutineHelper.executeBlocking {
            getDatabase().ttsConfigDao().deleteTTSConfigById(id)
        }
    }

    // AI Dictionary Config methods
    @JvmStatic
    fun getAllAIDictionaryConfigs(): List<AIDictionaryConfig> {
        return CoroutineHelper.executeBlocking {
            getDatabase().aiDictionaryConfigDao().getAllAIDictionaryConfigs()
        }
    }

    @JvmStatic
    fun getAIDictionaryConfigById(id: Long): AIDictionaryConfig? {
        return CoroutineHelper.executeBlocking {
            getDatabase().aiDictionaryConfigDao().getAIDictionaryConfigById(id)
        }
    }

    @JvmStatic
    fun saveAIDictionaryConfig(config: AIDictionaryConfig) {
        CoroutineHelper.executeBlocking {
            if (config.id > 0) {
                getDatabase().aiDictionaryConfigDao().updateAIDictionaryConfig(config)
            } else {
                getDatabase().aiDictionaryConfigDao().insertAIDictionaryConfig(config)
            }
        }
    }

    @JvmStatic
    fun deleteAIDictionaryConfig(id: Long) {
        CoroutineHelper.executeBlocking {
            getDatabase().aiDictionaryConfigDao().deleteAIDictionaryConfigById(id)
        }
    }

    // AI Translator Config methods
    @JvmStatic
    fun getAllAITranslatorConfigs(): List<AITranslatorConfig> {
        return CoroutineHelper.executeBlocking {
            getDatabase().aiTranslatorConfigDao().getAllAITranslatorConfigs()
        }
    }

    @JvmStatic
    fun getDefaultAITranslatorConfig(): AITranslatorConfig? {
        return CoroutineHelper.executeBlocking {
            getDatabase().aiTranslatorConfigDao().getDefaultAITranslatorConfig()
        }
    }

    @JvmStatic
    fun getAITranslatorConfigById(id: Long): AITranslatorConfig? {
        return CoroutineHelper.executeBlocking {
            getDatabase().aiTranslatorConfigDao().getAITranslatorConfigById(id)
        }
    }

    @JvmStatic
    fun saveAITranslatorConfig(config: AITranslatorConfig) {
        CoroutineHelper.executeBlocking {
            // If this is set as default, unset any existing default
            if (config.isDefault) {
                getDatabase().aiTranslatorConfigDao().clearAllDefaults()
            }
            if (config.id > 0) {
                getDatabase().aiTranslatorConfigDao().updateAITranslatorConfig(config)
            } else {
                getDatabase().aiTranslatorConfigDao().insertAITranslatorConfig(config)
            }
        }
    }

    @JvmStatic
    fun deleteAITranslatorConfig(id: Long) {
        CoroutineHelper.executeBlocking {
            getDatabase().aiTranslatorConfigDao().deleteAITranslatorConfigById(id)
        }
    }

    // AI Dictionary Cache methods
    @JvmStatic
    fun deleteOldestAIDictionaryCache(count: Int): Int {
        return AICacheRepository.deleteOldestDictionaryCache(count)
    }

    @JvmStatic
    fun deleteAllAIDictionaryCache(): Int {
        return AICacheRepository.deleteAllDictionaryCache()
    }

    // AI Translator Cache methods
    @JvmStatic
    fun deleteOldestAITranslatorCache(count: Int): Int {
        return AICacheRepository.deleteOldestTranslatorCache(count)
    }

    @JvmStatic
    fun deleteAllAITranslatorCache(): Int {
        return AICacheRepository.deleteAllTranslatorCache()
    }
}
