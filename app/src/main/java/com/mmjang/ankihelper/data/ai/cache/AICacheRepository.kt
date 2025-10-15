package com.mmjang.ankihelper.data.ai.cache

import android.util.Log
import com.mmjang.ankihelper.MyApplication
import com.mmjang.ankihelper.data.database.AppDatabase
import com.mmjang.ankihelper.data.dict.CoroutineHelper

object AICacheRepository {

    // Helper method to get database instance
    private fun getDatabase(): AppDatabase {
        return AppDatabase.getInstance(MyApplication.getContext())
    }

    // AI Dictionary Cache methods
    @JvmStatic
    fun getDictionaryCache(word: String, llmConfigId: Long): List<AIDictionaryCache> {
        Log.d("AICacheRepository", "Querying cache for word: $word, llmConfigId: $llmConfigId")
        return try {
            val results = CoroutineHelper.executeBlocking {
                getDatabase().aiDictionaryCacheDao().getCacheByHeadwordAndLLM(word, llmConfigId)
            }
            Log.d("AICacheRepository", "Found ${results.size} cached results for word: $word")
            results
        } catch (e: Exception) {
            Log.e("AICacheRepository", "Error querying cache for word: $word", e)
            // Return empty list on error
            emptyList()
        }
    }

    @JvmStatic
    fun saveDictionaryCache(cache: AIDictionaryCache) {
        try {
            Log.d("AICacheRepository", "Saving cache entry for word: ${cache.hwd}")
            CoroutineHelper.executeBlocking {
                getDatabase().aiDictionaryCacheDao().insertCache(cache)
            }
            Log.d("AICacheRepository", "Successfully saved cache entry for word: ${cache.hwd}")
        } catch (e: Exception) {
            Log.e("AICacheRepository", "Error saving cache entry for word: ${cache.hwd}", e)
        }
    }

    @JvmStatic
    fun clearExpiredDictionaryCache(expirationTime: Long) {
        CoroutineHelper.executeBlocking {
            getDatabase().aiDictionaryCacheDao().deleteOldCache(expirationTime)
        }
    }

    @JvmStatic
    fun deleteOldestDictionaryCache(count: Int): Int {
        return CoroutineHelper.executeBlocking {
            getDatabase().aiDictionaryCacheDao().deleteOldestCache(count)
        }
    }

    @JvmStatic
    fun deleteAllDictionaryCache(): Int {
        CoroutineHelper.executeBlocking {
            getDatabase().aiDictionaryCacheDao().deleteAllCache()
        }
        return 1 // Room's delete returns void, so we return success indicator
    }

    @JvmStatic
    fun getDictionaryCacheCount(): Int {
        return CoroutineHelper.executeBlocking {
            getDatabase().aiDictionaryCacheDao().getCount()
        }
    }

    // AI Translator Cache methods
    @JvmStatic
    fun getTranslatorCache(
        sourceText: String,
        sourceLanguage: String,
        targetLanguage: String,
        llmConfigId: Long
    ): AITranslatorCache? {
        return CoroutineHelper.executeBlocking {
            getDatabase().aiTranslatorCacheDao().getCacheByTextAndLLM(
                sourceText, sourceLanguage, targetLanguage, llmConfigId
            )
        }
    }

    @JvmStatic
    fun saveTranslatorCache(cache: AITranslatorCache) {
        CoroutineHelper.executeBlocking {
            getDatabase().aiTranslatorCacheDao().insertCache(cache)
        }
    }

    @JvmStatic
    fun clearExpiredTranslatorCache(expirationTime: Long) {
        CoroutineHelper.executeBlocking {
            getDatabase().aiTranslatorCacheDao().deleteOldCache(expirationTime)
        }
    }

    @JvmStatic
    fun deleteOldestTranslatorCache(count: Int): Int {
        return CoroutineHelper.executeBlocking {
            getDatabase().aiTranslatorCacheDao().deleteOldestCache(count)
        }
    }

    @JvmStatic
    fun deleteAllTranslatorCache(): Int {
        CoroutineHelper.executeBlocking {
            getDatabase().aiTranslatorCacheDao().deleteAllCache()
        }
        return 1 // Room's delete returns void, so we return success indicator
    }

    @JvmStatic
    fun getTranslatorCacheCount(): Int {
        return CoroutineHelper.executeBlocking {
            getDatabase().aiTranslatorCacheDao().getCount()
        }
    }
}
