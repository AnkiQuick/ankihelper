package com.lmyby.ankihelper.data.ai.cache

import androidx.room.*

@Dao
interface AITranslatorCacheDao {
    @Query("SELECT * FROM aitranslatorcache WHERE sourceText = :sourceText AND sourceLanguage = :sourceLang AND targetLanguage = :targetLang LIMIT 1")
    suspend fun getCacheByText(sourceText: String, sourceLang: String, targetLang: String): AITranslatorCache?

    @Query("SELECT * FROM aitranslatorcache WHERE sourceText = :sourceText AND sourceLanguage = :sourceLang AND targetLanguage = :targetLang AND llmConfigId = :llmConfigId LIMIT 1")
    suspend fun getCacheByTextAndLLM(sourceText: String, sourceLang: String, targetLang: String, llmConfigId: Long): AITranslatorCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: AITranslatorCache): Long

    @Delete
    suspend fun deleteCache(cache: AITranslatorCache): Int

    @Query("DELETE FROM aitranslatorcache WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldCache(beforeTimestamp: Long): Int

    @Query("DELETE FROM aitranslatorcache WHERE id IN (SELECT id FROM aitranslatorcache ORDER BY timestamp ASC LIMIT :count)")
    suspend fun deleteOldestCache(count: Int): Int

    @Query("DELETE FROM aitranslatorcache")
    suspend fun deleteAllCache()

    @Query("SELECT COUNT(*) FROM aitranslatorcache")
    suspend fun getCount(): Int
}
